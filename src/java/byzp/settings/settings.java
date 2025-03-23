package byzp.settings;

import arc.Core;
import arc.func.Cons;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.SettingsMenuDialog;
import static mindustry.Vars.*;

public class settings {

    public static void set() {
        ui.settings.addCategory("@line.settings.settings", Icon.book, table -> {
            table.checkPref("@line.settings.on", true);
            table.checkPref("@line.settings.setClipboard",true);
           // table.checkPref("发送匿名统计数据",true);
            table.checkPref("@line.settings.priorityP2P", true);

            table.areaTextPref("frpc.toml", randCfg.getCfg());
            if(Core.settings.getBool("@line.settings.priorityP2P")){
                if(Core.settings.getString("@line.settings.p2p.name")==null /* || Core.settings.getString("@line.settings.p2p.secretKey")==null */){
                    Random r = new Random();
                    int randKey = r.nextInt(100000) + 100000;
                    Core.settings.put("@line.settings.p2p.name", "p2p"+randKey);
                    //Core.settings.put("@line.settings.p2p.secretKey", "p2p"+randKey);
                }
                table.areaTextPref("frpcP2P.toml", randCfg.getCfgP2P());
                table.areaTextPref("@line.settings.p2p.name", Core.settings.getString("@line.settings.p2p.name"));
                table.areaTextPref("@line.settings.p2p.secretKey", Core.settings.getString("@line.settings.p2p.secretKey"));
            }
        });
        
    }
    
    public static void init() {
        set();
        
        String cfg = Core.settings.getString("frpc.toml");
        if ((cfg.charAt(cfg.length() - 1))!='\n'){
            Core.settings.put("frpc.toml", cfg + "\n");
            // return;
        }
        if (Core.settings.getBool("@line.settings.on")&&Core.settings.getBool("@line.settings.setClipboard")){
            Matcher ip = Pattern.compile("serverAddr = \"([\\d.]+)\"").matcher(cfg);
            Matcher port = Pattern.compile("remotePort = (\\d+)").matcher(cfg);
            if (ip.find()&&port.find()) {
                String pth=ip.group(1)+":"+port.group(1);
                Core.app.post(() -> {Vars.ui.showText("@line.settings.address", "@line.settings.clipboardTip");});
                Core.app.setClipboardText(pth);
            }else{
                Core.settings.put("@line.settings.setClipboard", false);
                Core.app.post(() -> {Vars.ui.showText("@line.error.error", "@line.settings.setClipboardError");});
            }
        }
        return;
    }
    
}
