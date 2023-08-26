# StayPut Remixed

[Spigot Plugin Page](https://www.spigotmc.org/resources/stayput-remixed.69564/)

Return to your last location when teleporting back to a multiverse world

The Bukkit/Spigot Multiverse plugin always sends you to a world's spawn when teleporting to it.
This can be problematic for those who want to pick up where they last left off in a world. This 
plugin fixes that by keeping track of where users were last and sending them there when they return
to a world. 

For keeping track of different world inventories, I recommend using [perworldinventory](https://www.spigotmc.org/resources/per-world-inventory.4482/).



## Help, I'm still getting teleported to spawn!

This usually happens because another plugin other than Multiverse is currently in charge of your world's spawn 
location. Turn on `debug` mode in the `config.yml` and look at the server console when you teleport. You might 
see something like this.

```
[StayPut] PlayerName: world1 --> world2
[StayPut] Not redirecting teleport because the destination appears to be a specific location in the world.
[StayPut] Location{world=CraftWorld(name=world2),x=0.5,y=135.0,z=0.5,pitch=0.0,yaw=-180.0} != Location{world=CraftWorld(name=null),x=0.0,y=135.0,z=0.0,pitch=0.0,yaw=0.0}
```

In this instance, we can see that the teleport took us to that first location on the third line. 
But, Minecraft reported the world spawn as that second location and we can see that the x, z, and yaw were different.

In order to fix this, we need to set the world spawn with Multiverse to be identical to the spawn we have set with 
our other plugin. You can do this by moving your character in game to the position you want to set the spawn, 
run `/mv set spawn` to set the Multiverse spawn position, then without moving your position or camera angle, 
run the command to on your other plugin to set the spawn on it.

To test it, move to somewhere else in the world and teleport out and back to it. Double-check the logs to see if 
it detects correctly now or if it still thinks the locations are different. If it does still think they are different, 
you can open a discussion or join the Discord for help https://discord.gg/ZRzpwRhnAa

### Extra info

The Spigot API doesn't make a distinction between a teleport to a world with a specific destination 
(ex. `/tp player1 player2` if it goes to another world) and a teleport to a world without a specific destination
(ex. `/mv tp player1 world`). This means that in order to tell if we should redirect a player to their last 
location, we need to check if they are getting sent to that world's spawn.

However, other plugins that can set spawn often do so without changing the world spawn on Spigot or Multiverse. This 
creates a disconnect where the other plugin sends you to its spawn location, but that location is different from 
what Spigot or Multiverse reports.


