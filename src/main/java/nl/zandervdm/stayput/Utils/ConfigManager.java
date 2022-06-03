package nl.zandervdm.stayput.Utils;
import nl.zandervdm.stayput.Main;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ConfigManager {

    protected Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void createConfig(){
        if(!this.plugin.getDataFolder().exists()){
            this.plugin.getDataFolder().mkdirs();
        }

        File file = new File(this.plugin.getDataFolder(), "config.yml");
        if(!file.exists()){
            this.plugin.getLogger().info("Config.yml not found, creating!");
            this.plugin.saveDefaultConfig();
        }

        config = this.plugin.getConfig();
    }

    public String getWorldGroup(@NotNull World world) {
        // For now, just remove either the nether or the_end
        if (world.getName().endsWith("_nether")) {
            return world.getName().substring(0, world.getName().length() - 7);
        }
        if (world.getName().endsWith("_the_end")) {
            return world.getName().substring(0, world.getName().length() - 8);
        }
        return world.getName();
    }


}
