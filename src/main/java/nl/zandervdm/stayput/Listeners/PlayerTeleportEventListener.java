package nl.zandervdm.stayput.Listeners;

import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.PlayerTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mvplugins.multiverse.core.event.MVTeleportDestinationEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Objects;

public class PlayerTeleportEventListener implements Listener {
    protected Main plugin;

    public PlayerTeleportEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMVTeleportDestinationEvent(MVTeleportDestinationEvent event) {
        // https://mvplugins.org/core/reference/destinations/
        // Only redirect World destinations
        if (!Objects.equals(event.getDestination().getIdentifier(), "w")) {
            this.plugin.debugLogger("Ignoring MVTeleportDestinationEvent event with " + event.getDestination().getIdentifier() + " identifier");
            return;
        }
        if (!(event.getTeleportee() instanceof Player)) {
            this.plugin.debugLogger("Not redirecting non-player teleportee");
            return;
        }
        Player player = (Player) event.getTeleportee();

        this.plugin.debugLogger("Checking redirect for MVTeleportDestinationEvent event with " + event.getDestination().getIdentifier() + " identifier");

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause.equals(PlayerTeleportEvent.TeleportCause.COMMAND) ||
                cause.equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            this.plugin.debugLogger("Got PlayerTeleportEvent for " + event.getPlayer() + " cause: " + event.getCause() + " to:" + event.getTo());

            Iterator<PlayerTeleport> iter = plugin.getPlayerTeleports().iterator();
            this.plugin.debugLogger("Player teleports " + plugin.getPlayerTeleports().size());
            while (iter.hasNext()) {
                PlayerTeleport tp = iter.next();
                if (tp.location.equals(event.getTo()) && tp.entity.equals(event.getPlayer())) {
                    iter.remove();
                    if (tp.cancel) {
                        this.plugin.debugLogger("Cancelling original Multiverse player teleport...");
                        event.setCancelled(true);
                        return;
                    }
                    this.plugin.debugLogger("Previously handled this teleport in the MVTeleportDestinationEvent");
                    return;
                }
                this.plugin.debugLogger("now minus five " + Instant.now().minus(5, ChronoUnit.MINUTES) + " tp time " + tp.time);
                if (Instant.now().minus(5, ChronoUnit.MINUTES).compareTo(tp.time) > 0) {
                    this.plugin.debugLogger("Removing old unhandled teleport from list");
                    iter.remove();
                }
            }

            Location newLocation = this.plugin.getTeleport().handleTeleport(event.getPlayer(), event.getFrom(), event.getTo());
            if (newLocation != null) {
                event.setTo(newLocation);
            }
        }
    }
}
