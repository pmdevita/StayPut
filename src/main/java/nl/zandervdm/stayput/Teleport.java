package nl.zandervdm.stayput;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Teleport {
    private Main plugin;

    private static Set<Material> PRESSURE_PLATES = new HashSet<Material>(Arrays.asList(
            Material.STONE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE
    ));

    public Teleport(Main plugin) {
        this.plugin = plugin;
    }


    public Location handleTeleport(Player player, Location from, Location to) {
//        Location toLocation = null;
//        if (destination != null) {
//            toLocation = destination.getLocation(player);
//        }

        // Determine if we should update this player's now previous location
        if (this.plugin.getRuleManager().shouldUpdateLocation(player, from, to)) {
            this.plugin.getPositionRepository().updateLocationForPlayer(player, from);
        } else {
            // If we don't need to record position, we aren't going anywhere so return
            return null;
        }

        // Determine if we should teleport the player and if we should, get the new Location
        if (to != null) {

            Location previousLocation = this.plugin.getRuleManager().shouldTeleportPlayer(player, to);

            if (previousLocation != null) {
                if (this.isPressurePlate(previousLocation)) {
                    // Find a valid spot around the location
                    Location newLocation = this.findAvailableLocation(previousLocation);
                    if (newLocation != null) previousLocation = newLocation;
                }

                //There is a location, and the player should teleport, so teleport him
                this.plugin.debugLogger("Teleporting player to his previous location");
//                ourTeleports.add(new TeleportData(player, previousLocation));
//                player.teleport(previousLocation);

                return previousLocation;    // We teleported the player ourselves, cancel the event's teleport
            }
        }
        return null; // We didn't teleport the player ourselves, let the event continue normally
    }

    protected boolean isPressurePlate(Location toLocation) {
        Location blockBelow = new Location(toLocation.getWorld(), toLocation.getX(), toLocation.getY()-1, toLocation.getZ());
        if (PRESSURE_PLATES.contains(blockBelow.getBlock().getType()))
            return true;
        else if (PRESSURE_PLATES.contains(toLocation.getBlock().getRelative(BlockFace.DOWN).getType()))
            return true;
        else return PRESSURE_PLATES.contains(toLocation.getBlock().getType());
    }

    protected Location findAvailableLocation(Location location) {
        //The current given location isn't valid (most likely it is a pressure plate)
        //Get the location in front, back, left and right of it and check if it is air.
        //Also check the block above it to make sure the block can't suffocate.
        Location block1Down = new Location(location.getWorld(), location.getX()-1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location block2Down = new Location(location.getWorld(), location.getX()+1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location block3Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()-1, location.getYaw(), location.getPitch());
        Location block4Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()+1, location.getYaw(), location.getPitch());

        if(block1Down.getBlock().getType().equals(Material.AIR)){
            Location block1Up = new Location(location.getWorld(), location.getX()-1, location.getY()+1, location.getZ());
            if(block1Up.getBlock().getType().equals(Material.AIR)) return block1Down;
        }else if(block2Down.getBlock().getType().equals(Material.AIR)){
            Location block2Up = new Location(location.getWorld(), location.getX()+1, location.getY()+1, location.getZ());
            if(block2Up.getBlock().getType().equals(Material.AIR)) return block2Down;
        }else if(block3Down.getBlock().getType().equals(Material.AIR)){
            Location block3Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()-1);
            if(block3Up.getBlock().getType().equals(Material.AIR)) return block3Down;
        }else if(block4Down.getBlock().getType().equals(Material.AIR)){
            Location block4Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()+1);
            if(block4Up.getBlock().getType().equals(Material.AIR)) return block4Down;
        }
        return null;
    }

}
