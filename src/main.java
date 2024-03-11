import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.scene.ui.TextField;
import arc.util.Log;
import frpclib.Frpclib;
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
			// toFi在公共路径，Android不允许加载，file在/data/data/
            File file = File.createTempFile("frpc",".so");
            file.deleteOnExit();
            copyFileUsingStream(toFi.file(),file);
            //file.setExecutable(true);
            String pth=file.getPath();
            Seq.iii(pth);
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> Log.infoTag("Scheme", sw.toString()));
            Core.app.post(() -> Vars.ui.showText("复制运行库时出错", sw.toString()));
        }
        
        Events.run(HostEvent.class, ()->{
            if(!Frpclib.isRunning("0")){
                ass();
            }else{
                Core.app.post(() -> Vars.ui.showText("ip", addr));
            }
        });
        
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
    
    void ass(){
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
    
    void frpexec(String ii){
        String iii="ip";
        TextField tf=new TextField();
        tf.setText(iii);
        addr=s+":"+ii;
        Core.app.post(() -> Vars.ui.showText("ip", addr+"\n(已复制)"));
        Core.app.setClipboardText(addr);
        Thread tr=new Thread(()->{
            try{
                String con="[common]\nserver_addr = "+s+"\nserver_port = 7000\ntoken=2313945\n\n";
                con+="["+ii+"t]\ntype = tcp\nlocal_ip = 127.0.0.1\nlocal_port = 6567\nremote_port = "+ii+"\nuse_encryption = true\nuse_compression = true\n\n";
                con+="["+ii+"u]\ntype = udp\nlocal_ip = 127.0.0.1\nlocal_port =  6567\nremote_port = "+ii+"\nuse_encryption = true\nuse_compression = true";
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
