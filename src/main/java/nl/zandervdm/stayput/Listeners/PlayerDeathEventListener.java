package nl.zandervdm.stayput.Listeners;

import nl.zandervdm.stayput.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathEventListener implements Listener {
    protected Main plugin;

    public PlayerDeathEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        this.plugin.getDatabase().deleteLocation(event.getEntity(), event.getEntity().getWorld());
    }

}
