package byzp.settings;

//import mindustry.gen.Icon;
import arc.Core;
import static arc.Core.*;
import static mindustry.Vars.*;
import byzp.settings.randCfg;
import java.util.regex.*;
import mindustry.Vars;

public class settings {

    public static void set() {
        ui.settings.addCategory("联机设置(line mod)", table -> {
            table.checkPref("启用", true);
            table.checkPref("启动时复制地址到剪切板",true);
           // table.checkPref("优先使用P2P(正在开发，目前无效)", false);

            table.areaTextPref("frpc.toml", randCfg.getCfg());
            
        });
        
    }
    
    public static void init() {
        set();
        
        String cfg = Core.settings.getString("frpc.toml");
        if ((cfg.charAt(cfg.length() - 1))!='\n'){
            Core.settings.put("frpc.toml", cfg + "\n");
            // return;
        }
        if (Core.settings.getBool("启用")&&Core.settings.getBool("启动时复制地址到剪切板")){
            Matcher ip = Pattern.compile("serverAddr = \"([\\d.]+)\"").matcher(cfg);
            Matcher port = Pattern.compile("remotePort = (\\d+)").matcher(cfg);
            if (ip.find()&&port.find()) {
                String pth=ip.group(1)+":"+port.group(1);
                Core.app.post(() -> {Vars.ui.showText("联机地址", pth+"\n已复制(可在设置关闭)");});
                Core.app.setClipboardText(pth);
            }else{
                Core.settings.put("启动时复制地址到剪切板", false);
                Core.app.post(() -> {Vars.ui.showText("Error", "无法匹配地址，请检查配置文件，自动复制功能已关闭");});
            }
        }
        return;
    }
    
}
