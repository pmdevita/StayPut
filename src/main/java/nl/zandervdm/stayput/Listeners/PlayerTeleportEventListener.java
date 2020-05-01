package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import nl.zandervdm.stayput.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayerTeleportEventListener implements Listener {

    protected Main plugin;

    public PlayerTeleportEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(MVTeleportEvent event) {
        if (this.plugin.getTeleport().handleTeleport(event.getTeleportee(), event.getFrom(), event.getDestination(), false))
            event.setCancelled(true);
    }
}
