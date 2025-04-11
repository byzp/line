# line
## 适用于mindustry的联机mod，使用frp

- 默认使用我的frps服务器，但你可以在设置更改
- p2p功能仍在开发中

## 实现细节

- 加载frp动态库运行frpc打通一个tcp和一个udp隧道, 这是默认方案
- 主机在本地运行cpp组件的服务端, 监听8002端口的tcp连接, 转发请求到本地6567端口(tcp/udp), 加载frp动态库运行一个xtcp隧道等待p2p连接 ,客户机连接xtcp隧道, 随后启动cpp组件的客户端部分, 绑定本地8004端口, 转发数据至主机, 这是p2p实现

### 编译

- 运行buildjni.sh单独编译动态库,随后gradle编译打包(需配置go和android ndk)

``` bash
./buildjni.sh
./gradlew deploy
```

