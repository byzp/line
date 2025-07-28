package byzp;

import arc.Core;

import mindustry.Vars;
import mindustry.mod.Plugin;

import byzp.android.load;
import byzp.settings.settings;
import byzp.ui.linkp2p;

public class line extends Plugin {

    boolean isAndroidAarch64 = false;

    public line() {
        
    }

    @Override
    public void init() {
        super.init();
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");

        if (osName.contains("Linux") && osArch.contains("aarch64")) {
            isAndroidAarch64 = true;
            settings.init();
        }
        if(!isAndroidAarch64){
            Core.app.post(() -> {
                Vars.ui.showText("@line.error.error", "@line.unsupport");
            });
            return;
        }
        if (Core.settings.getBool("@line.settings.on")) {
            load l = new load();
            l.init_android();
            if(Core.settings.getBool("@line.settings.enableP2P")){
                linkp2p lp = new linkp2p();
            }
        }
    }
}
