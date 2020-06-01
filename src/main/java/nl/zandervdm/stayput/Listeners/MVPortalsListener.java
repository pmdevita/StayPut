package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import nl.zandervdm.stayput.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MVPortalsListener implements Listener {
    protected Main plugin;

    public MVPortalsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMVPortalEvent(MVPortalEvent event) {
        this.plugin.debugLogger("onMVPortalEvent");
//        Location newLocation = this.plugin.getTeleport().handleTeleport(event.getTeleportee(), event.getFrom(), event.getDestination().getLocation(event.getTeleportee()));
//        if(newLocation != null) {
//            event.setCancelled(true);
//            event.getTeleportee().teleport(newLocation);
//        }
    }

}
