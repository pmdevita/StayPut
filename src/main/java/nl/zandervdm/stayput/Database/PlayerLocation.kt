package nl.zandervdm.stayput.Database

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class PlayerLocation(val player: Player, val group: String, val location: Location) {
    constructor(player: Player, group: String, world: String, x: Float, y: Float, z: Float, yaw: Float, pitch: Float) : this(player, group, Location(Bukkit.getServer().getWorld(world),
        x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)) {
    }
    constructor(player: Player, group: String, world: World, x: Float, y: Float, z: Float, yaw: Float, pitch: Float) : this(player, group, Location(world,
        x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)) {
    }
    constructor(player: Player, group: String, world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) : this(player, group, Location(Bukkit.getServer().getWorld(world),
        x, y, z, yaw, pitch)) {
    }
    constructor(player: Player, group: String, world: World, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) : this(player, group, Location(world,
        x, y, z, yaw, pitch)) {
    }
}