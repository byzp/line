import arc.scene.ui.TextField;
import arc.*;
import arc.files.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.io.*;
import frpclib.Frpclib;
import go.*;
import java.util.Random;
import java.net.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.*;
import arc.assets.loaders.*;

public class main extends Mod{
    String addr="";
    String s = "139.196.113.128";
    int port = 6568;
    @Override
    public void init(){
        super.init();
        try{
            Fi fi = Vars.mods.getMod(main.class).root.child("libgojni.so");
            Fi toFi=Vars.tmpDirectory.child(fi.name());
			fi.copyTo(toFi);
            //Core.app.post(() -> Vars.ui.showText("xx", ""));
            //Thread.sleep(1);
            File file = File.createTempFile("frpc",".so");
            if(!file.exists()){
                //Core.app.post(() -> Vars.ui.showText("xx", ""));
                return;
            }
            file.deleteOnExit();
            copyFileUsingStream(toFi.file(),file);
            file.setExecutable(true);
            String pth=file.getPath();
            Seq.iii(pth);
            //Core.app.post(() -> Vars.ui.showText("xx", pth));
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> Log.infoTag("Scheme", sw.toString()));
            Core.app.post(() -> Vars.ui.showText("复制运行库时出错", sw.toString()));
        }
        //frpexec a=new frpexec();
        //soload.install(VMStack.getCallingClassLoader(),"/storage/emulated/0/Android/data/io.anuke.mindustry/files/");
       // a.ass();
//        Timer.schedule(this::lateInit, 3f);
        
        //Runtime.getRuntime().exec("/system/bin/rm /data/user/0/io.anuke.mindustry/libgojni.so");
        //Runtime.getRuntime().exec("/system/bin/cp /storage/emulated/0/Android/data/io.anuke.mindustry/files/libgojni.so /data/user/0/io.anuke.mindustry/libgojni.so");
        //Runtime.getRuntime().exec("/system/bin/chmod 777 /data/user/0/io.anuke.mindustry/libgojni.so");
           
        //System.load("/data/user/0/io.anuke.mindustry/libgojni.so");
        
        Events.run(HostEvent.class, ()->{
            if(!Frpclib.isRunning("0")){
                ass();
            }else{
                Core.app.post(() -> Vars.ui.showText("ip", addr));
            }
        });
        
    }
    private static void copyFileUsingStream(File source, File dest) throws IOException {
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
    } finally {
        is.close();
        os.close();
    }
}
    void ass(){
        //String i="";
        Thread t=new Thread(()->{
          try{
            
            Socket client = new Socket(s, port);
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
 
            out.writeUTF(Core.settings.getString("uuid", ""));
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            frpexec(in.readUTF());
            client.close();
          }catch(IOException e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> Log.infoTag("Scheme", sw.toString()));
            Core.app.post(() -> Vars.ui.showText("向服务器请求端口时出错",sw.toString()));
          }
        });
        t.start();
    }
        /*
        try{
            File file = new File("/data/user/0/io.anuke.mindustry/files/frpc");
            if(!file.exists()) {
                String fileUrl = "http://192.168.8.111:1234/sdcard/frpc";
                URL url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                String localFilePath = "/data/user/0/io.anuke.mindustry/files/frpc";
                FileOutputStream outputStream = new FileOutputStream(localFilePath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
            }
        } catch(Throwable e) {
            Core.app.post(() - > Vars.ui.showText("err", e.toString()));
        }
        Random r = new Random();
        int ii = r.nextInt(40000)+10000;
        */
    void frpexec(String ii){
        //final String ii=i;
        String iii="ip";
        TextField tf=new TextField();
        tf.setText(iii);
        addr=s+":"+ii;
        Core.app.post(() -> Vars.ui.showText("ip", addr));
        /*
        table(btns -> {
            btns.defaults().size(48f).padLeft(8f);
            btns.button(Icon.copy, Styles.clearNonei, () -> {
                app.setClipboardText(text);
                //ui.showInfoFade("已复制");
            });
        });
        */
        
        
        Thread tr=new Thread(()->{
            try{
                String con="[common]\nserver_addr = "+s+"\nserver_port = 7000\ntoken=2313945\n\n";
                con+="["+ii+"t]\ntype = tcp\nlocal_ip = 127.0.0.1\nlocal_port = 6567\nremote_port = "+ii+"\n\n";
                con+="["+ii+"u]\ntype = udp\nlocal_ip = 127.0.0.1\nlocal_port =  6567\nremote_port = "+ii;
                Frpclib.runContent("0",con);
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
