#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstdint>
#include <cassert>
#include <map>
#include <vector>
#include <string>
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
    int udp_fd;
};

struct ClientSession {
    int client_fd;
    std::map<uint32_t, TcpConn> tcpConns;
    std::map<uint32_t, UdpConn> udpConns;
};

std::mutex sessionMutex;
std::map<int, ClientSession*> sessions;
uint16_t targetPort = 6567;

extern "C"
JNIEXPORT int JNICALL
Java_byzp_android_load_sstart(JNIEnv* env, jobject) {
    int listen_fd = socket(AF_INET, SOCK_STREAM, 0);
    if(listen_fd < 0) { perror("socket"); exit(1); }
    int opt = 1;
    setsockopt(listen_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    struct sockaddr_in servAddr;
    memset(&servAddr, 0, sizeof(servAddr));
    servAddr.sin_family = AF_INET;
    servAddr.sin_port = htons(8002);
    servAddr.sin_addr.s_addr = INADDR_ANY;
    if(bind(listen_fd, (struct sockaddr*)&servAddr, sizeof(servAddr)) < 0) { perror("bind"); exit(1); }
    listen(listen_fd, 10);
    setNonBlocking(listen_fd);
    //printf("server started, listening on TCP 8002\n");

    struct sockaddr_in targetAddr;
    memset(&targetAddr, 0, sizeof(targetAddr));
    targetAddr.sin_family = AF_INET;
    targetAddr.sin_port = htons(targetPort);
    targetAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

    while(true) {
        fd_set readfds;
        FD_ZERO(&readfds);
        int maxfd = listen_fd;
        FD_SET(listen_fd, &readfds);
        {
            std::lock_guard<std::mutex> lock(sessionMutex);
            for(auto &pair : sessions) {
                ClientSession *session = pair.second;
                FD_SET(session->client_fd, &readfds);
                if(session->client_fd > maxfd) maxfd = session->client_fd;
                for(auto &tcpPair : session->tcpConns) {
                    FD_SET(tcpPair.second.fd, &readfds);
                    if(tcpPair.second.fd > maxfd) maxfd = tcpPair.second.fd;
                }
                for(auto &udpPair : session->udpConns) {
                    FD_SET(udpPair.second.udp_fd, &readfds);
                    if(udpPair.second.udp_fd > maxfd) maxfd = udpPair.second.udp_fd;
                }
            }
        }
        struct timeval tv;
        tv.tv_sec = 0;
        tv.tv_usec = 10000;
        int ret = select(maxfd+1, &readfds, NULL, NULL, &tv);
        if(ret < 0) { perror("select"); continue; }
        if(FD_ISSET(listen_fd, &readfds)) {
            struct sockaddr_in cliAddr;
            socklen_t addrlen = sizeof(cliAddr);
            int client_fd = accept(listen_fd, (struct sockaddr*)&cliAddr, &addrlen);
            if(client_fd >= 0) {
                setNonBlocking(client_fd);
                ClientSession *session = new ClientSession;
                session->client_fd = client_fd;
                {
                    std::lock_guard<std::mutex> lock(sessionMutex);
                    sessions[client_fd] = session;
                }
                #ifdef DEBUG
                    printf("New client session, fd=%d\n", client_fd);
                #endif
            }
        }
        {
            std::lock_guard<std::mutex> lock(sessionMutex);
            std::vector<int> toRemove;
            for(auto &pair : sessions) {
                ClientSession *session = pair.second;
                if(FD_ISSET(session->client_fd, &readfds)) {
                    char buf[2048];
                    int n = read(session->client_fd, buf, sizeof(buf));
                    if(n <= 0) {
                        close(session->client_fd);
                        toRemove.push_back(session->client_fd);
                        continue;
                    }
                    size_t offset = 0;
                    while(n - offset >= (int)sizeof(FrameHeader)) {
                        FrameHeader header;
                        memcpy(&header, buf + offset, sizeof(FrameHeader));
                        size_t frameSize = sizeof(FrameHeader) + header.length;
                        if(n - offset < (int)frameSize) break;
                        char *payload = buf + offset + sizeof(FrameHeader);
                        if(header.flag == TCP_NEW) {
                            int s = socket(AF_INET, SOCK_STREAM, 0);
                            if(s >= 0) {
                                setNonBlocking(s);
                                if(connect(s, (struct sockaddr*)&targetAddr, sizeof(targetAddr)) < 0 && errno != EINPROGRESS) {
                                    close(s);
                                } else {
                                    session->tcpConns[header.conn_id] = {s};
                                    #ifdef DEBUG
                                        printf("Established TCP mapping, conn_id=%u for client fd=%d\n", header.conn_id, session->client_fd);
                                    #endif
                                }
                            }
                        } else if(header.flag == UDP_NEW) {
                            int s = socket(AF_INET, SOCK_DGRAM, 0);
                            if(s >= 0) {
                                setNonBlocking(s);
                                connect(s, (struct sockaddr*)&targetAddr, sizeof(targetAddr));
                                session->udpConns[header.conn_id] = {s};
                                #ifdef DEBUG
                                    printf("Established UDP mapping, conn_id=%u for client fd=%d\n", header.conn_id, session->client_fd);
                                #endif
                            }
                        } else if(header.flag == TCP_DATA) {
                            if(session->tcpConns.count(header.conn_id)) {
                                write(session->tcpConns[header.conn_id].fd, payload, header.length);
                            }
                        } else if(header.flag == UDP_DATA) {
                            if(session->udpConns.count(header.conn_id)) {
                                write(session->udpConns[header.conn_id].udp_fd, payload, header.length);
                            }
                        } else if(header.flag == CLOSE) {
                            if(session->tcpConns.count(header.conn_id)) {
                                close(session->tcpConns[header.conn_id].fd);
                                session->tcpConns.erase(header.conn_id);
                                #ifdef DEBUG
                                    printf("Closed TCP mapping, conn_id=%u for client fd=%d\n", header.conn_id, session->client_fd);
                                #endif
                            }
                            if(session->udpConns.count(header.conn_id)) {
                                close(session->udpConns[header.conn_id].udp_fd);
                                session->udpConns.erase(header.conn_id);
                                #ifdef DEBUG
                                    printf("Closed UDP mapping, conn_id=%u for client fd=%d\n", header.conn_id, session->client_fd);
                                #endif
                            }
                        }
                        offset += frameSize;
                    }
                }
                std::vector<uint32_t> tcpToRemove;
                for(auto &tcpPair : session->tcpConns) {
                    int fd = tcpPair.second.fd;
                    if(FD_ISSET(fd, &readfds)) {
                        char buf[1500];
                        int n = read(fd, buf, sizeof(buf));
                        if(n > 0) {
                            tcp_send_frame(session->client_fd, tcpPair.first, TCP_DATA, buf, n);
                        } else if(n == 0 || (n < 0 && errno != EAGAIN)) {
                            tcp_send_frame(session->client_fd, tcpPair.first, CLOSE, nullptr, 0);
                            close(fd);
                            tcpToRemove.push_back(tcpPair.first);
                        }
                    }
                }
                for(auto conn_id : tcpToRemove)
                    session->tcpConns.erase(conn_id);
                std::vector<uint32_t> udpToRemove;
                for(auto &udpPair : session->udpConns) {
                    int fd = udpPair.second.udp_fd;
                    if(FD_ISSET(fd, &readfds)) {
                        char buf[1500];
                        int n = read(fd, buf, sizeof(buf));
                        if(n > 0) {
                            tcp_send_frame(session->client_fd, udpPair.first, UDP_DATA, buf, n);
                        }
                    }
                }
            }
            for(auto fd : toRemove) {
                sessions.erase(fd);
            }
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
    }
    close(listen_fd);
    return 0;
}
