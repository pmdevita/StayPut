package nl.zandervdm.stayput.Utils;

import nl.zandervdm.stayput.Database.PlayerLocation;
import nl.zandervdm.stayput.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class RuleManager {

    protected Main plugin;
    HashSet<String> blacklistedWorlds;

    public RuleManager(Main plugin) {
        this.plugin = plugin;
        blacklistedWorlds = new HashSet<>(this.plugin.getConfig().getStringList("blacklisted-worlds"));
        plugin.debugLogger("Loaded blacklisted worlds " + Arrays.toString(blacklistedWorlds.toArray()));
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
                this.plugin.debugLogger("Not saving location because player did not jump worlds");
                return false;
            }
        }

        // If the world we are teleporting from is inside the configs blacklist, ignore
        if (this.worldIsBlacklisted(fromWorld)) {
            this.plugin.debugLogger("Not saving location because " + fromWorld.getName() + " is blacklisted");
            return false;
        }

        return true;
    }

    public PlayerLocation shouldTeleportPlayer(Player player, Location from, Location toLocation) {
        World toWorld = toLocation.getWorld();

        if (toWorld == null || from.getWorld() == null) {
            this.plugin.debugLogger("shouldTeleporPlayer was given locations with null world values???");
            return null;
        }

        // If this world is inside the configs blacklist, ignore
        if (this.worldIsBlacklisted(toWorld)) {
            this.plugin.debugLogger("Not redirecting teleport because this world is blacklisted");
            return null;
        }

        // If we didn't change worlds, ignore the teleport
        if (from.getWorld().equals(toLocation.getWorld())) {
            this.plugin.debugLogger("Not redirecting teleport because player did not jump worlds");
            return null;
        }

        // If we are teleporting to a defined location in a world, then it is a directed teleport, and we shouldn't touch it
//        Location to = toLocation.clone();
        Location vanillaSpawn = toLocation.getWorld().getSpawnLocation();
        Location spawn = this.plugin.getMultiverse().core.getMVWorldManager().getMVWorld(toLocation.getWorld()).getSpawnLocation();
        this.plugin.debugLogger("Vanilla Spawn: " + vanillaSpawn.toString() + " MV Spawn: " + spawn.toString());
//        Location MVSpawn = this.plugin.getMultiverse().core.getMVWorldManager().getMVWorld(to.getWorld()).getSpawnLocation();
//        this.plugin.debugLogger("MVSpawn is " + MVSpawn.toString());

        // For some dumbass reason, toLocation can have a null world which thwarts what should have been an easy equals check
        // But you can get the world from it. But you can't set the world to be anything but null.
        // Why would you do this to me
        // Turns out its a Multiverse SpawnLocation which subclasses Location and allows for weird world properties


        if (toLocation.equals(spawn)) {
            this.plugin.debugLogger("Appears to be a teleport to world spawn, will redirect if possible");
            this.plugin.debugLogger(toLocation + " == " + spawn);
        } else {
            this.plugin.debugLogger("Not redirecting teleport because the destination appears to be specific location in the world. (If this was supposed to be redirected, there may be some confusion about the spawn location between the vanilla server and plugins.)");
            this.plugin.debugLogger(toLocation + " != " + spawn);
            return null;
        }

        // If this is a teleport inside a world group, ignore it
        if (Objects.equals(plugin.getConfigManager().getWorldGroup(from.getWorld()), plugin.getConfigManager().getWorldGroup(toLocation.getWorld()))) {
            this.plugin.debugLogger("Teleport within world group, not redirecting");
            return null;
        }


        // In any other case, find the previous spot of the user in this world
        PlayerLocation previousLocation = this.plugin.getDatabase().getLocation(player, toWorld);

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
