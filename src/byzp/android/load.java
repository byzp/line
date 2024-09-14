package byzp.android;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.scene.ui.TextField;
import arc.util.Log;
import frp.Frp;
import go.Seq;
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
import byzp.main;

public class load{
    public void init_android(){
        try{
            String pth=load_file("libgojni.so");
           // Core.app.post(() -> Vars.ui.showText("",pth));
            Seq.iii(pth);
            frpc();
        } catch(IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> Log.infoTag("Scheme", sw.toString()));
            Core.app.post(() -> Vars.ui.showText("复制运行库时出错", sw.toString()));
        }
    }
    /*
    public String load_file(String name)throws IOException {
        try{
            Fi fi = Vars.mods.getMod(main.class).root.child(name);
            Fi toFi=Vars.tmpDirectory.child(fi.name());
		    fi.copyTo(toFi);
            File file = File.createTempFile(name,".tmp");
            file.deleteOnExit();
            copyFileUsingStream(toFi.file(),file);
            //file.setExecutable(true);
            return file.getPath();
        }finally{
            return "";
        }
    }
    */
    
    public String load_file(String name)throws IOException {
        try{
            Fi fi = Vars.mods.getMod(main.class).root.child(name);
            Fi toFi=Vars.tmpDirectory.child(fi.name());
		    fi.copyTo(toFi);
            File file = File.createTempFile(name,".so");
            file.deleteOnExit();
            copyFileUsingStream(toFi.file(),file);
            //file.setExecutable(true);
            return file.getPath();
        } catch(IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> Log.infoTag("Error", sw.toString()));
            //Core.app.post(() -> Vars.ui.showText("", sw.toString()));
            return "";
        }
    }
    
    void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }finally{
            is.close();
            os.close();
        }
    }
   

    void frpc(){
      // Core.app.post(() -> Vars.ui.showText("ip", addr+"\n(已复制)"));
       // Core.app.setClipboardText(addr);
        Thread tr=new Thread(()->{
            try{
                Frp.runFrpc("",load_file("frpc.toml"));
            }catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Core.app.post(() -> Log.infoTag("Scheme", sw.toString()));
                Core.app.post(() -> Vars.ui.showText("开启本地监听时出错", sw.toString()));
            }
        });
        tr.start();
    }
}
