package nl.zandervdm.stayput;

//import com.j256.ormlite.dao.Dao;
//import com.j256.ormlite.dao.DaoManager;
//import com.j256.ormlite.support.ConnectionSource;
//import com.j256.ormlite.table.TableUtils;
//import com.j256.ormlite.logger.LoggerFactory.LogType;
//import com.j256.ormlite.logger.LocalLog;

import nl.zandervdm.stayput.Commands.StayputCommand;
import nl.zandervdm.stayput.Database.BaseDatabase;
import nl.zandervdm.stayput.Listeners.MVPortalsListener;
import nl.zandervdm.stayput.Listeners.PlayerDeathEventListener;
import nl.zandervdm.stayput.Listeners.PlayerQuitEventListener;
import nl.zandervdm.stayput.Listeners.PlayerTeleportEventListener;
import nl.zandervdm.stayput.Utils.ConfigManager;
import nl.zandervdm.stayput.Utils.MVManager;
import nl.zandervdm.stayput.Utils.RuleManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class Main extends JavaPlugin {
    private boolean debug;

    public static FileConfiguration config;

    //Util classes
    protected ConfigManager configManager;
    protected RuleManager ruleManager;
    protected Teleport teleport;
    protected MVManager multiverse;

    //Database connection stuff
//    protected ConnectionSource connectionSource;

    protected BaseDatabase database;

    /**
     * Permissions:
     * stayput.use
     * stayput.override
     * stayput.admin
     */

    @Override
    public void onEnable(){
        setupConfig();
        setupClasses();
        setupListeners();
        setupCommands();
        setupDatabase();
//        setupDao();
//        setupTables();
//        dbMigration();
    }

    @Override
    public void onDisable() {
        syncPlayersToDatabase();
    }

//    public ConnectionSource getConnectionSource() {
//        return this.connectionSource;
//    }

    public MVManager getMultiverse() { return this.multiverse; }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    public Teleport getTeleport() {
        return this.teleport;
    }

    public ConfigManager getConfigManager() { return this.configManager; }

    public BaseDatabase getDatabase() { return this.database; }

    protected void setupClasses() {
        this.configManager = new ConfigManager(this);
        this.ruleManager = new RuleManager(this);
        this.teleport = new Teleport(this);
        this.multiverse = new MVManager(this);
    }

    public void setupConfig() {
        this.saveDefaultConfig();
        config = getConfig();
        this.debug = this.getConfig().getBoolean("debug");
        debugLogger("Debug mode enabled");
    }

    protected void setupListeners() {
        debugLogger("Setting up listeners");
        getServer().getPluginManager().registerEvents(new PlayerTeleportEventListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(this), this);
        // Check if the MVPortals is loaded and then add an event handler for it
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Portals");

        if (plugin != null) {
            debugLogger("MVPortals is loaded");
            getServer().getPluginManager().registerEvents(new MVPortalsListener(this), this);
        } else {
            debugLogger("MVPortals is not loaded");
        }
    }

    protected void setupCommands() {
        this.getCommand("stayput").setExecutor(new StayputCommand(this));
    }

    protected void setupDatabase() {
        // Force ormlite to use internal log even if slf4j exists (v10Lift) (#4)
//        System.setProperty("com.j256.ormlite.logger.type", LogType.LOCAL.toString());
        // Make ormlite shut up if not in debug mode
        String logLevel = "ERROR";
        if (this.debug) {
            logLevel = "INFO";
        }
//        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, logLevel);
        this.database = BaseDatabase.Companion.open(this);
    }

    protected void syncPlayersToDatabase() {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        for(Player player : players) {
            teleport.handleTeleport(player, player.getLocation(), null);
        }
    }

    public void debugLogger(String message) {
        if (this.debug) {
            getLogger().info(message);
        }
    }

}
