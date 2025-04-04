#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstdint>
#include <cassert>
#include <map>
#include <vector>
#include <chrono>
#include <thread>
#include <mutex>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <jni.h>

//#define DEBUG  //////////

#pragma pack(push, 1)
struct FrameHeader {
    uint32_t conn_id;
    uint8_t flag;
    uint32_t length;
};
#pragma pack(pop)

enum FrameFlag {
    TCP_DATA = 0,
    UDP_DATA = 1,
    TCP_NEW  = 2,
    UDP_NEW  = 3,
    CLOSE    = 4
};

static int setNonBlocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

static void tcp_send_frame(int sock, uint32_t conn_id, uint8_t flag, const char *data, uint32_t len) {
    FrameHeader header;
    header.conn_id = conn_id;
    header.flag = flag;
    header.length = len;
    std::vector<char> buf(sizeof(FrameHeader) + len);
    memcpy(buf.data(), &header, sizeof(FrameHeader));
    if(len > 0 && data)
        memcpy(buf.data() + sizeof(FrameHeader), data, len);
    send(sock, buf.data(), buf.size(), 0);
}

struct TcpConn {
    int fd;
};

struct UdpConn {
    struct sockaddr_in peer;
};

std::mutex connMutex;
std::map<uint32_t, TcpConn> tcpConns;
std::map<uint32_t, UdpConn> udpConns;
std::map<std::string, uint32_t> udpPeerMap;
uint32_t g_conn_id = 1;

extern "C"
JNIEXPORT int JNICALL
Java_byzp_android_load_cstart(JNIEnv* env, jobject /* this */) {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if(server_fd < 0){
        perror("socket");
        exit(1);
    }
    struct sockaddr_in servAddr;
    memset(&servAddr, 0, sizeof(servAddr));
    servAddr.sin_family = AF_INET;
    servAddr.sin_port = htons(8002);
    servAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    if(connect(server_fd, (struct sockaddr*)&servAddr, sizeof(servAddr)) < 0){
        perror("connect");
        exit(1);
    }
    setNonBlocking(server_fd);

    int tcp_listen_fd = socket(AF_INET, SOCK_STREAM, 0);
    if(tcp_listen_fd < 0){
        perror("socket");
        exit(1);
    }
    int opt = 1;
    setsockopt(tcp_listen_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    struct sockaddr_in localAddr;
    memset(&localAddr, 0, sizeof(localAddr));
    localAddr.sin_family = AF_INET;
    localAddr.sin_port = htons(8004);
    localAddr.sin_addr.s_addr = INADDR_ANY;
    if(bind(tcp_listen_fd, (struct sockaddr*)&localAddr, sizeof(localAddr)) < 0){
        perror("bind");
        exit(1);
    }
    listen(tcp_listen_fd, 10);
    setNonBlocking(tcp_listen_fd);

    int local_udp_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if(local_udp_fd < 0){
        perror("socket");
        exit(1);
    }
    if(bind(local_udp_fd, (struct sockaddr*)&localAddr, sizeof(localAddr)) < 0){
        perror("bind udp");
        exit(1);
    }
    setNonBlocking(local_udp_fd);

    std::vector<char> serverRecvBuf;
    serverRecvBuf.reserve(4096);

    printf("client started, listening on TCP/UDP 8004\n");

    while(true) {
        fd_set readfds;
        FD_ZERO(&readfds);
        int maxfd = 0;
        FD_SET(tcp_listen_fd, &readfds);
        maxfd = std::max(maxfd, tcp_listen_fd);
        FD_SET(local_udp_fd, &readfds);
        maxfd = std::max(maxfd, local_udp_fd);
        FD_SET(server_fd, &readfds);
        maxfd = std::max(maxfd, server_fd);
        {
            std::lock_guard<std::mutex> lock(connMutex);
            for(auto &it : tcpConns) {
                FD_SET(it.second.fd, &readfds);
                maxfd = std::max(maxfd, it.second.fd);
            }
        }
        struct timeval tv;
        tv.tv_sec = 0;
        tv.tv_usec = 10000;
        int ret = select(maxfd+1, &readfds, NULL, NULL, &tv);
        if(ret < 0) {
            perror("select");
            continue;
        }
        if(FD_ISSET(tcp_listen_fd, &readfds)) {
            struct sockaddr_in cliAddr;
            socklen_t addrlen = sizeof(cliAddr);
            int client_fd = accept(tcp_listen_fd, (struct sockaddr*)&cliAddr, &addrlen);
            if(client_fd >= 0) {
                setNonBlocking(client_fd);
                uint32_t conn_id = g_conn_id++;
                {
                    std::lock_guard<std::mutex> lock(connMutex);
                    tcpConns[conn_id] = {client_fd};
                }
                tcp_send_frame(server_fd, conn_id, TCP_NEW, nullptr, 0);
                #ifdef DEBUG
                    printf("New TCP connection, conn_id=%u\n", conn_id);
                #endif
            }
        }
        if(FD_ISSET(local_udp_fd, &readfds)) {
            char buf[1500];
            struct sockaddr_in peer;
            socklen_t len = sizeof(peer);
            int n = recvfrom(local_udp_fd, buf, sizeof(buf), 0, (struct sockaddr*)&peer, &len);
            if(n > 0) {
                char key[64];
                #ifdef DEBUG
                    snprintf(key, sizeof(key), "%s:%d", inet_ntoa(peer.sin_addr), ntohs(peer.sin_port));
                #endif
                uint32_t conn_id;
                {
                    std::lock_guard<std::mutex> lock(connMutex);
                    auto it = udpPeerMap.find(key);
                    if(it == udpPeerMap.end()){
                        conn_id = g_conn_id++;
                        udpPeerMap[key] = conn_id;
                        udpConns[conn_id] = {peer};
                        tcp_send_frame(server_fd, conn_id, UDP_NEW, nullptr, 0);
                        #ifdef DEBUG
                            printf("New UDP mapping from %s, assigned conn_id=%u\n", key, conn_id);
                        #endif
                    } else {
                        conn_id = it->second;
                    }
                }
                tcp_send_frame(server_fd, conn_id, UDP_DATA, buf, n);
                #ifdef DEBUG
                    printf("UDP datagram from %s, using conn_id=%u\n", key, conn_id);
                #endif
            }
        }
        {
            std::lock_guard<std::mutex> lock(connMutex);
            std::vector<uint32_t> toRemove;
            for(auto &it : tcpConns) {
                uint32_t cid = it.first;
                int fd = it.second.fd;
                if(FD_ISSET(fd, &readfds)) {
                    char buf[1500];
                    int n = read(fd, buf, sizeof(buf));
                    if(n > 0) {
                        tcp_send_frame(server_fd, cid, TCP_DATA, buf, n);
                    } else if(n == 0 || (n < 0 && errno != EAGAIN)) {
                        tcp_send_frame(server_fd, cid, CLOSE, nullptr, 0);
                        close(fd);
                        toRemove.push_back(cid);
                        #ifdef DEBUG
                            printf("TCP connection closed, conn_id=%u\n", cid);
                        #endif
                    }
                }
            }
            for(auto cid : toRemove) {
                tcpConns.erase(cid);
            }
        }
        if(FD_ISSET(server_fd, &readfds)) {
            char buf[2048];
            int n = read(server_fd, buf, sizeof(buf));
            if(n > 0) {
                serverRecvBuf.insert(serverRecvBuf.end(), buf, buf+n);
                while(serverRecvBuf.size() >= sizeof(FrameHeader)) {
                    FrameHeader header;
                    memcpy(&header, serverRecvBuf.data(), sizeof(FrameHeader));
                    size_t frameSize = sizeof(FrameHeader) + header.length;
                    if(serverRecvBuf.size() < frameSize) break;
                    char *payload = serverRecvBuf.data() + sizeof(FrameHeader);
                    if(header.flag == TCP_DATA) {
                        std::lock_guard<std::mutex> lock(connMutex);
                        if(tcpConns.count(header.conn_id)) {
                            write(tcpConns[header.conn_id].fd, payload, header.length);
                        }
                    } else if(header.flag == UDP_DATA) {
                        std::lock_guard<std::mutex> lock(connMutex);
                        if(udpConns.count(header.conn_id)) {
                            sendto(local_udp_fd, payload, header.length, 0, (struct sockaddr*)&udpConns[header.conn_id].peer, sizeof(udpConns[header.conn_id].peer));
                            #ifdef DEBUG
                                printf("Forwarded UDP data to peer, conn_id=%u\n", header.conn_id);
                            #endif
                        } else {
                            #ifdef DEBUG
                                printf("No UDP mapping for conn_id=%u\n", header.conn_id);
                            #endif
                        }
                    } else if(header.flag == CLOSE) {
                        std::lock_guard<std::mutex> lock(connMutex);
                        if(tcpConns.count(header.conn_id)) {
                            close(tcpConns[header.conn_id].fd);
                            tcpConns.erase(header.conn_id);
                            #ifdef DEBUG
                                printf("Received CLOSE for TCP conn_id=%u\n", header.conn_id);
                            #endif
                        }
                    }
                    serverRecvBuf.erase(serverRecvBuf.begin(), serverRecvBuf.begin() + frameSize);
                }
            }
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }
    close(tcp_listen_fd);
    close(local_udp_fd);
    close(server_fd);
    return 0;
}
