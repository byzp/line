package byzp;

import arc.Core;
import byzp.android.load;
import byzp.settings.settings;
import byzp.ui.*;
import mindustry.Vars;
import mindustry.mod.Mod;


public class main extends Mod {
    int stat = 0;

    public void init() {
        super.init();
        if (System.getProperty("os.name").contains("Linux")) {
            if (System.getProperty("os.arch").contains("aarch64")) {
                settings.init();
                if (Core.settings.getBool("@line.settings.on")){
                    load l = new load();
                    l.init_android();
                    linkp2p lp=new linkp2p();
                    return;
                }
                
            }
        }else{
            Core.app.post(() -> {Vars.ui.showText("@line.error.error", "@line.unsupport");});
        }
    }
}
