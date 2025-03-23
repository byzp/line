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

import byzp.android.*;

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
                String addr=Core.settings.getString("p2p.addr").split("/")[0];
                String ip="127.0.0.1";
                int port=6567;
                String[] addrs=addr.split("*");
                if(addrs.length==2){
                    
                    
                }else{
                    String[] address=addr.split(":");
                    ip=address[0];
                    if (address.length>1){
                        port=Integer.parseInt(address[1]);
                    }
                    
                }
                String finalip=ip;
                int finalport=port;
                
                ui.loadfrag.show("@connecting");
                ui.loadfrag.setButton(() -> {
                    ui.loadfrag.hide();
                    netClient.disconnectQuietly();
                });
                
                Time.runTask(2f, () -> {
                    logic.reset();
                    net.reset();
                    Vars.netClient.beginConnecting();
                    net.connect(finalip, finalport, () -> {
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
