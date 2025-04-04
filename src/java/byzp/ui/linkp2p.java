package byzp.ui;

import arc.Core;
import arc.func.Cons;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import static mindustry.Vars.*;

import java.util.Random;

import byzp.android.*;
import byzp.settings.*;

public class linkp2p extends BaseDialog {
    
    public String Link = "";
    public String output="";
    public boolean valid;
    
    public linkp2p(){
        super("@line.linkp2p");
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
                Random r = new Random();
                int rand = r.nextInt(100000) + 100000;
                String cfg= randCfg.getCfgP2P().split("\n\n")[0]+
                "\n\n[[visitors]]\ntype = \"xtcp\"\nname = \""+rand+"\""+"\"\nbindAddr = \"127.0.0.1\"\nbindPort = 8004\n";
                String[] addr=Core.settings.getString("p2p.addr").split("#");
                String name=addr[0];
                if(addr.length==1){
                    cfg+="serverName = "+name;
                    cfg+="\nsecretKey = null";
                }
                if(addr.length==2){
                    String key=addr[1];
                    cfg+="serverName = "+name;
                    cfg+="\nsecretKey = "+key;
                }else{
                    
                }
                //Vars.ui.showText("a", cfg);
                
                load.frpc(cfg);
                
                new Thread(()->{
                    load.cstart();
                }).start();
                
                ui.loadfrag.show("@connecting");
                ui.loadfrag.setButton(() -> {
                    ui.loadfrag.hide();
                    netClient.disconnectQuietly();
                });
                
                Time.runTask(2f, () -> {
                    logic.reset();
                    net.reset();
                    Vars.netClient.beginConnecting();
                    net.connect("127.0.0.1", 8004, () -> {
                        if(net.client()){
                            hide();
                            //adyd.hide();
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
        root.button("@line.linkp2p", Icon.play, this::show);
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
