package byzp;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.scene.ui.TextField;
import arc.util.Log;
//import frp.Frp;
//import go.Seq;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import mindustry.Vars;
import mindustry.game.EventType.HostEvent;
import mindustry.mod.Mod;
import byzp.android.load;

public class main extends Mod{
    int stat=0;
    String addr="";
    String s = "139.196.113.128";
    int port = 6568;
    @Override
    public void init(){
        super.init();
        if(System.getProperty("os.name").contains("Linux")){
            if(System.getProperty("os.arch").contains("aarch64")){
                load l=new load();
                l.init_android();
            }else{
                Core.app.post(() -> Vars.ui.showText("error", "此操作系统或架构不受支持，仅支持Android aarch64和Windows amd64"));
                return;
            }
        }

    
    }
    }