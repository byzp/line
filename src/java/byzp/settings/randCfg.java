package byzp.settings;

import java.util.*;
import arc.Core;
import mindustry.Vars;


public class randCfg{

    public static String getCfg() {
        Random r = new Random();
        int port = r.nextInt(40000) + 10000;
        return /*"#这是frpc的配置文件。\n#这个模组被设计为开箱即用，但开发者的服务器资源有限，人多时可能造成卡顿掉线，你可以自定义frps服务器以实现更稳定的连接，SakuraFRP这类应该是兼容的\n\n*/"serverAddr = \"139.196.113.128\"\nserverPort = 7000\nauth.token = \"2313945\"\n\n[[proxies]]\nname = \"mdtu"+port+"\"\ntype = \"udp\"\nlocalIP = \"127.0.0.1\"\nlocalPort = 6567\nremotePort = "+port+"\n\n[[proxies]]\nname = \"mdtt"+port+"\"\ntype = \"tcp\"\nlocalIP = \"127.0.0.1\"\nlocalPort = 6567\nremotePort = "+port;
    }
}
