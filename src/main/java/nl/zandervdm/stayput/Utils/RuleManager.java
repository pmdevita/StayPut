package nl.zandervdm.stayput.Utils;

import nl.zandervdm.stayput.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.List;

public class RuleManager {

    protected Main plugin;
    HashSet<String> blacklistedWorlds;

    public RuleManager(Main plugin) {
        this.plugin = plugin;
        blacklistedWorlds = new HashSet<>(this.plugin.getConfig().getStringList("blacklisted-worlds"));
    }

    public boolean shouldUpdateLocation(Player player, Location fromLocation, Location toLocation) {
        World fromWorld = fromLocation.getWorld();
        World toWorld = null;
        if (toLocation != null) {
            toWorld = toLocation.getWorld();
        }

        // If the player does not have the use permission, just ignore it and do nothing
        if (!player.hasPermission("stayput.use")) {
            this.plugin.debugLogger("Not saving location because player does not have permission (stayput.use)");
            return false;
        }

        // If the worlds are the same, ignore
        if (toWorld != null) {
            if (fromWorld.equals(toWorld)) {
                this.plugin.debugLogger("Not saving location because world was not switched");
                return false;
            }
        }

        // If the world we are teleporting from is inside the configs blacklist, ignore
        if (this.worldIsBlacklisted(fromWorld)) {
            this.plugin.debugLogger("Not saving location because this world is blacklisted");
            return false;
        }

        return true;
    }

    public Location shouldTeleportPlayer(Player player, Location toLocation) {
        World toWorld = toLocation.getWorld();

        // If this world is inside the configs blacklist, ignore
        if (this.worldIsBlacklisted(toWorld)) {
            this.plugin.debugLogger("Not redirecting teleport because this world is blacklisted");
            return null;
        }

        // If we are teleporting to a defined location in a world, then it is a directed teleport and we shouldn't touch it
        Location to = toLocation.clone();
        Location spawn = to.getWorld().getSpawnLocation();
        // For some dumbass reason, toLocation can have a null world which thwarts what should have been an easy equals check
        // But you can get the world from it. But you can't set the world to be anything but null.
        // Why would you do this to me
        if (to.getX() == spawn.getX() && to.getY() == spawn.getY() && to.getZ() == spawn.getZ() &&
                to.getPitch() == spawn.getPitch() && to.getYaw() == spawn.getYaw()) {
            this.plugin.debugLogger("Appears to be a teleport to world spawn, will redirect if possible");
            this.plugin.debugLogger(to.toString() + " == " + to.getWorld().getSpawnLocation().toString());
        } else {
            this.plugin.debugLogger("Not redirecting teleport because the destination appears to be specific location in the world");
            this.plugin.debugLogger(to.toString() + " != " + to.getWorld().getSpawnLocation().toString());
            return null;
        }

        // In any other case, find the previous spot of the user in this world
        Location previousLocation = this.plugin.getPositionRepository().getPreviousLocation(player, toWorld);

        // If there is no previous location for this world, just ignore it
        if (previousLocation == null) {
            this.plugin.debugLogger("Not teleporting player because there is no previous location found");
            return null;
        }

        return previousLocation;
    }

    public boolean worldIsBlacklisted(World world) {
        return blacklistedWorlds.contains(world.getName());
    }
}
