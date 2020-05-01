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
    public void onPlayerPortalEvent(MVPortalEvent event) {
        if(this.plugin.getTeleport().handleTeleport(event.getTeleportee(), event.getFrom(), event.getDestination(), true))
            event.setCancelled(true);
    }

}
