package byzp.android;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import byzp.main;
import frp.Frp;
import go.Seq;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import mindustry.Vars;


public class load {
    public void init_android() {
        try {
            String pth = load_file("libgojni.so");
            Seq.iii(pth);
            frpc();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> {
                Log.infoTag("Scheme", sw.toString());
            });
            Core.app.post(() -> {
                Vars.ui.showText("复制运行库时出错", sw.toString());
            });
        }
    }

    public String load_file(String name) throws IOException {
        try {
            Fi fi = Vars.mods.getMod(main.class).root.child(name);
            Fi toFi = Vars.tmpDirectory.child(fi.name());
            fi.copyTo(toFi);
            File file = File.createTempFile(name, ".so");
            file.deleteOnExit();
            copyFileUsingStream(toFi.file(), file);
            return file.getPath();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Core.app.post(() -> {
                Log.infoTag("Error", sw.toString());
            });
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
            while (true) {
                int length = is.read(buffer);
                if (length > 0) {
                    os.write(buffer, 0, length);
                } else {
                    is.close();
                    os.close();
                    return;
                }
            }
        } catch (Throwable th) {
            is.close();
            os.close();
            throw th;
        }
    }

    void frpc() {
        Thread tr = new Thread(() -> {
            try {
                File cfg = File.createTempFile("frpc", ".toml");
                FileOutputStream fop = new FileOutputStream(cfg);
                OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
                writer.append(Core.settings.getString("frpc.toml"));
                writer.close();
                fop.close();
                Frp.runFrpc("",cfg.getPath());
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Core.app.post(() -> {
                    Log.infoTag("Scheme", sw.toString());
                });
                Core.app.post(() -> {
                    Vars.ui.showText("开启本地监听时出错", sw.toString());
                });
            }
        });
        tr.start();
    }
}
