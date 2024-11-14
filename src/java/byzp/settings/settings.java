package byzp.settings;

import mindustry.gen.Icon;
import arc.Core;
import static arc.Core.*;
import static mindustry.Vars.*;
import byzp.settings.randCfg;
import java.util.regex.*;
import mindustry.Vars;

public class settings {

    public static void set() {
        ui.settings.addCategory("@line.settings.settings", Icon.book, table -> {
            table.checkPref("@line.settings.on", true);
            table.checkPref("@line.settings.setClipboard",true);
           // table.checkPref("发送匿名统计数据",true);
            table.checkPref("@line.settings.priorityP2P", true);

            table.areaTextPref("frpc.toml", randCfg.getCfg());
            if(Core.settings.getBool("@line.settings.priorityP2P")){
                table.areaTextPref("frpcP2P.toml", randCfg.getCfgP2P());
                table.areaTextPref("@line.settings.p2p.name", "test");
                table.areaTextPref("@line.settings.p2p.token", "123456");
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
                Core.app.post(() -> {Vars.ui.showText("@line.settings.address", pth/*+"\n"+"@line.settings.clipboardTip" */);});
                Core.app.setClipboardText(pth);
            }else{
                Core.settings.put("@line.settings.setClipboard", false);
                Core.app.post(() -> {Vars.ui.showText("@line.error.error", "@line.settings.setClipboardError");});
            }
        }
        return;
    }
    
}
