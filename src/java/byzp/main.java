package byzp;

import arc.Core;
import byzp.android.load;
import byzp.settings.settings;
import mindustry.Vars;
import mindustry.mod.Mod;


public class main extends Mod {
    int stat = 0;

    public void init() {
        super.init();
        if (System.getProperty("os.name").contains("Linux")) {
            if (System.getProperty("os.arch").contains("aarch64")) {
                settings.init();
                if (Core.settings.getBool("启用")){
                    load l = new load();
                    l.init_android();
                    return;
                }
            }
        }else{
            Core.app.post(() -> {Vars.ui.showText("error", "此操作系统或架构不受支持，仅支持Android aarch64(项目正在重构，Windows amd64暂时不支持)");});
        }
    }
}
