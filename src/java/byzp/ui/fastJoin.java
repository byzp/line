package byzp.ui;

import mindustry.gen.Icon;
import mindustry.ui.dialogs.*;
import mindustry.net.Packets.*;

import arc.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Timer.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.versions.*;
import mindustry.net.*;
import mindustry.net.Packets.*;
import mindustry.ui.*;
import arc.struct.Seq;

import static mindustry.Vars.*;

public class fastJoin extends BaseDialog {
    
    public String Link = "";
    public String output="";
    public boolean valid;
    
    public fastJoin(){
        super("@line.fastJoin");
        /*
        cont.table(table -> {
            table.add("@line.settings.address").padLeft(5f).left();
            table.field(Link,this::setLink).size(450f, 54f).maxTextLength(100).valid(this::setLink);
        }).row();
        
        //cont.label(() -> "ip:port / name:token:[ip]").width(550f).left();
        */
        TextField field = cont.field(Core.settings.getString("p2p.addr"), text -> {
            Core.settings.put("p2p.addr", text);
        }).size(320f, 54f).maxTextLength(100).get();
        
        //field.cont.label(() -> output).width(550f).left();
        buttons.defaults().size(140f, 60f).pad(4f);
        
        buttons.button("@cancel", this::hide);
        buttons.button("@ok", () -> {
            try {
                if (player.name.trim().isEmpty()) {
                    ui.showInfo("@noname");
                    return;
                }
                
                ui.loadfrag.show("@connecting");
                ui.loadfrag.setButton(() -> {
                    ui.loadfrag.hide();
                    netClient.disconnectQuietly();
                });
                String[] addr=Core.settings.getString("p2p.addr").split(":");
                String ip=addr[0];
                int port=6567;
                
                if (addr.length>1){
                    port=Integer.parseInt(addr[1]);
                }
                int finalport=port;
                
                Time.runTask(2f, () -> {
                    logic.reset();
                    net.reset();
                    Vars.netClient.beginConnecting();
                    net.connect(ip, finalport, () -> {
                        if(net.client()){
                            hide();
                            //add.hide();
                        }
                    });
                });
                
                
            } catch (Throwable ignored) {
                ui.showErrorMessage(ignored.getMessage());
            }
        }); //.disabled(button -> Link.isEmpty() || net.active());
        
        var stack = (Stack) ui.join.getChildren().get(1);
        var root = (Table) stack.getChildren().get(1);
        //var root=ui.join.cont;
        root.button("@line.fastJoin", Icon.play, this::show);
        int num = root.getCells().size;
        //int fr=(int)((8-num)/2);
        root.getCells().insert(num-2, root.getCells().remove(num-1));
        
    }
    
    
    
    
    
    public boolean setLink(String link) {
        //if (Link.equals(link)) return false;

        try {
            
            valid = true;
        } catch (Throwable ignored) {
            output = ignored.getMessage();
            valid = false;
        }

        Link = link;
        return valid;
    }
    
    
    
    

}
