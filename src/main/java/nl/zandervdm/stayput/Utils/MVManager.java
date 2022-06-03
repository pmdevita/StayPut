package nl.zandervdm.stayput.Utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import nl.zandervdm.stayput.Main;
import org.bukkit.plugin.Plugin;

public class MVManager {
    MultiverseCore core;
    public MVManager(Main main) {
        Plugin plugin = main.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin instanceof MultiverseCore) {
            this.core = (MultiverseCore) plugin;
        } else {
            this.core = null;
            main.getLogger().severe("StayPut loaded but Multiverse did not, something is wrong.");
        }
    }

    public boolean loadedMultiverse() {
        return this.core != null;
    }

}
