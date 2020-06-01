package nl.zandervdm.stayput;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.logger.LoggerFactory.LogType;
import com.j256.ormlite.logger.LocalLog;
import nl.zandervdm.stayput.Commands.StayputCommand;
import nl.zandervdm.stayput.Listeners.MVPortalsListener;
import nl.zandervdm.stayput.Listeners.PlayerQuitEventListener;
import nl.zandervdm.stayput.Listeners.PlayerTeleportEventListener;
import nl.zandervdm.stayput.Models.Position;
import nl.zandervdm.stayput.Repositories.PositionRepository;
import nl.zandervdm.stayput.Utils.ConfigManager;
import nl.zandervdm.stayput.Utils.MVManager;
import nl.zandervdm.stayput.Utils.RuleManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
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
    protected ConnectionSource connectionSource;

    //Data mappers
    protected Dao<Position, Integer> positionMapper;

    //Repositories
    protected PositionRepository positionRepository;

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
        setupDao();
        setupTables();
        dbMigration();
    }

    @Override
    public void onDisable() {
        syncPlayersToDatabase();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public Dao<Position, Integer> getPositionMapper() {
        return this.positionMapper;
    }

    public PositionRepository getPositionRepository() {
        return this.positionRepository;
    }

    public MVManager getMultiverse() { return this.multiverse; }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    public Teleport getTeleport() {
        return this.teleport;
    }

    protected void setupClasses() {
        this.configManager = new ConfigManager(this);
        this.ruleManager = new RuleManager(this);
        this.positionRepository = new PositionRepository(this);
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
        getServer().getPluginManager().registerEvents(new PlayerTeleportEventListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(this), this);
        debugLogger("Setting up listeners");
        // Check if the MVPortals is loaded and then add an event handler for it
        try {
            Class cls = Class.forName("com.onarandombox.MultiversePortals.event.MVPortalEvent");
            debugLogger("MVPortals is loaded");
            getServer().getPluginManager().registerEvents(new MVPortalsListener(this), this);
        } catch (ClassNotFoundException e) {
            debugLogger("MVPortals is not loaded");
        }
    }

    protected void setupCommands() {
        this.getCommand("stayput").setExecutor(new StayputCommand(this));
    }

    protected void setupDatabase() {
        // Force ormlite to use internal log even if slf4j exists (v10Lift) (#4)
        System.setProperty("com.j256.ormlite.logger.type", LogType.LOCAL.toString());
        // Make ormlite shut up if not in debug mode
        String logLevel = "ERROR";
        if (this.debug) {
            logLevel = "INFO";
        }
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, logLevel);

        // SQLite setup
        if (Main.config.getString("type").equals("sqlite")) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            File file = new File(this.getDataFolder(), "database.db");
            String datasource = "jdbc:sqlite:" + file;
            connectionSource = null;
            try {
                connectionSource = new JdbcConnectionSource(datasource);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        // MySQL Setup
        } else if (Main.config.getString("type").equals("mysql")) {
            String host = Main.config.getString("mysql.host");
            int port = Main.config.getInt("mysql.port");
            String database = Main.config.getString("mysql.database");
            String username = Main.config.getString("mysql.username");
            String password = Main.config.getString("mysql.password");
            String datasource = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
            connectionSource = null;
            try {
                connectionSource = new JdbcPooledConnectionSource(datasource, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            getLogger().warning("Invalid database connection type chosen!");
        }
        debugLogger("Setting up database");
    }

    protected void setupDao() {
        positionMapper = null;
        try {
            positionMapper = DaoManager.createDao(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setupTables() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        debugLogger("Setting up tables");
    }

    protected void syncPlayersToDatabase() {
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        for(Player player : players) {
            teleport.handleTeleport(player, player.getLocation(), null);
        }
    }

    protected void dbMigration() {
//        DatabaseType dbType = positionMapper.getConnectionSource().getDatabaseType();
//        if (dbType instanceof MysqlDatabaseType) {
//            positionRepository.deleteDuplicates();
//        }
        positionRepository.doMigrations();
    }

    public void debugLogger(String message) {
        if (this.debug) {
            getLogger().info(message);
        }
    }

}
