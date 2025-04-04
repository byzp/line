# line
## 适用于mindustry的联机mod，使用frp

- 默认使用我的frps服务器，但你可以在设置更改
- 

### 编译

- 运行buildjni.sh单独编译动态库,随后gradle编译打包(编译动态库遇到问题请转用下面的方法)
- 创建lib文件夹,手动下载预编译的[frp动态库](https://github.com/HaidyCao/frp/releases/tag/0.61.0-android),解压arm64-v8a架构的libgojni.so至lib文件夹,随后gradle打包

``` bash
./buildjni.sh
./gradlew deploy
```

