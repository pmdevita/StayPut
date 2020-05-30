package nl.zandervdm.stayput.Listeners;

import nl.zandervdm.stayput.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private Main plugin;

    public PlayerQuitEventListener(Main plugin) {
        this.plugin = plugin;
    }

    // Save a player's location once they leave the server
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.plugin.debugLogger("onPlayerQuitEvent");
        Player who = event.getPlayer();
        this.plugin.getTeleport().handleTeleport(who, who.getLocation(), null);
    }
}
