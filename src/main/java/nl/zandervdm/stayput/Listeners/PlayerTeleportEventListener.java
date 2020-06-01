package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import nl.zandervdm.stayput.Main;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportEventListener implements Listener {
    protected Main plugin;

    public PlayerTeleportEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMVTeleportEvent(MVTeleportEvent event) {
        this.plugin.debugLogger("onPlayerMVTeleportEvent " + event.getDestination().getType() + " "
                + event.getDestination().getLocation(event.getTeleportee()).toString());
//        if (this.plugin.getTeleport().handleTeleport(event.getTeleportee(), event.getFrom(), event.getDestination().getLocation(event.getTeleportee())))
//            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause.equals(PlayerTeleportEvent.TeleportCause.COMMAND) ||
                cause.equals(PlayerTeleportEvent.TeleportCause.PLUGIN) || cause.equals(PlayerTeleportEvent.TeleportCause.UNKNOWN))
        {
            this.plugin.debugLogger("onPlayerTeleportEvent " + cause.toString());
            Location newLocation = this.plugin.getTeleport().handleTeleport(event.getPlayer(), event.getFrom(), event.getTo());
            if (newLocation != null) {
                event.setTo(newLocation);
            }
        }
    }
}
