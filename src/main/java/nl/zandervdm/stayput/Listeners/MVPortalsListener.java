package nl.zandervdm.stayput.Listeners;

import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.PlayerTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.portals.event.MVPortalEvent;

import java.util.Objects;

public class MVPortalsListener implements Listener {
    protected Main plugin;

    public MVPortalsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMVPortalEvent(MVPortalEvent event) {
        // https://mvplugins.org/core/reference/destinations/
        // Only redirect World destinations
        if (!Objects.equals(event.getDestination().getIdentifier(), "w")) {
            this.plugin.debugLogger("Ignoring MVPortalEvent event with " + event.getDestination().getIdentifier() + " identifier");
            return;
        }
        Player player = event.getTeleportee();

        this.plugin.debugLogger("Checking redirect for MVPortalEvent event with " + event.getDestination().getIdentifier() + " identifier");

        Location newLocation = this.plugin.getTeleport().handleTeleport(
                player,
                event.getFrom(),
                event.getDestination().getLocation(event.getTeleportee()).getOrNull(),
                false
        );

        if (newLocation == null) {
            return;
        }

        // If we cancel the event here, it outputs an error message argghhhhh! We'll cancel it later.
        event.setCancelled(true);
        // plugin.getPlayerTeleports().add(new PlayerTeleport(player, event.getDestination().getLocation(event.getTeleportee()).getOrNull(), true));

        plugin.getPlayerTeleports().add(new PlayerTeleport(player, newLocation));
        event.getTeleportee().teleport(newLocation);
        plugin.getMultiverse().getCore().getSafetyTeleporter().to(newLocation).teleport(player);
    }

}
