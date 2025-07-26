package nl.zandervdm.stayput;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.time.Instant;

public class PlayerTeleport {

    public Entity entity;
    public Location location;
    public Instant time;
    public boolean cancel;

    public PlayerTeleport(Entity entity, Location location) {
        this.entity = entity;
        this.location = location;
        this.time = Instant.now();
        this.cancel = false;
    }

    public PlayerTeleport(Entity entity, Location location, boolean cancel) {
        this.entity = entity;
        this.location = location;
        this.time = Instant.now();
        this.cancel = cancel;
    }
}
