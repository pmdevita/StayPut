package nl.zandervdm.stayput.Database

import nl.zandervdm.stayput.Main
import org.bukkit.World
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.DriverManager


class MySQL(plugin: Main) : BaseDatabase(plugin) {
    override val SCHEMA_VERSION = 2

    override fun getConnection(): Connection {
        val host = plugin.config.getString("database.host") ?: throw Exception("MySQL/MariaDB host not defined.")
        val port = plugin.config.getInt("database.port")
        val username = plugin.config.getString("database.username") ?: throw Exception("MySQL/MariaDB username not defined.")
        val password = plugin.config.getString("database.password") ?: throw Exception("MySQL/MariaDB password not defined.")
        val database = getDatabaseFromConfig()
        val jdbcURL = "jdbc:mysql://$host:$port/$database?autoReconnect=true&enabledTLSProtocols=TLSv1.2"
        plugin.debugLogger("Using url $jdbcURL")
        return DriverManager.getConnection(jdbcURL, username, password)
    }

    private fun getDatabaseFromConfig() : String {
        val database = plugin.config.getString("database.database") ?: throw java.lang.Exception("MySQL/MariaDB database not defined.")
        plugin.debugLogger("database in config is $database ")
        if (!isValidSQLIdentifier(database)) {
            throw Exception("MySQL/MariaDB database name is not alphanumeric/underscore.")
        }
        return database
    }

    override fun getSchema(table: String): String? {
        val stmt = conn.createStatement()
        val query = "show create table $table"
        val result = stmt.executeQuery(query)
        var schema : String? = null
        while (result.next()) {
            schema = result.getString("Create Table")
        }
        return schema
    }

    override fun getSchemaVersion(): Int {
        val schema = getSchema(table) ?: throw Exception("Hey dude theres no schema in here!!")
        plugin.debugLogger("Has old table: $hasOldTable")
        plugin.debugLogger("Current schema is:\n$schema")
        if (schema.startsWith(MYSQL_SCHEMA_VERSION_1.format(table))) {
            return 1
        }
        return 2
    }

    override fun migrateTable(currentVersion: Int) {
        if (currentVersion == 1) {
            // Rename old table
            val stmt = conn.createStatement()
            val query = "alter table $table rename to $OLD_TABLE"
            val result = stmt.executeUpdate(query)
            plugin.debugLogger("Migrating from v1 to v2...")
            createTable()
            // Set old table flag to true
            hasOldTable = true
        }
    }

    override fun createTable() {
        val stmt = conn.createStatement()
        val query = MYSQL_SCHEMA_VERSION_2.format(table)
        plugin.debugLogger("Creating table with schema $query")
        stmt.executeUpdate(query)
    }

    override fun getLocation(player: Player, world: World): PlayerLocation? {
        val group = plugin.configManager.getWorldGroup(world)
        // Attempt to get location from v2+ table
        var stmt = conn.createStatement()
        var query = "select * from $table where uuid = '${player.uniqueId.toString()}' and world_group = '${escapeString(group)}';"
        var result = stmt.executeQuery(query)
        var location : PlayerLocation? = null
        while (result.next()) {
            location = PlayerLocation(player, result.getString(2), result.getString(3), result.getDouble(4), result.getDouble(5),
                result.getDouble(6), result.getFloat(7), result.getFloat(8))
            plugin.debugLogger("Location found for ${player.name}")
        }
        // If location wasn't found, but we have the old table, look at that
        if (location == null && hasOldTable) {
            stmt = conn.createStatement()
            query = "select * from $OLD_TABLE where uuid = '${player.uniqueId.toString()}' and world_name = '${escapeString(world.name)}';"
            result = stmt.executeQuery(query)
            while (result.next()) {
                location = PlayerLocation(player, group, result.getString(4), result.getString(5).toDouble(), result.getString(6).toDouble(),
                    result.getString(7).toDouble(), result.getBigDecimal(8).toFloat(), result.getBigDecimal(9).toFloat())
                plugin.debugLogger("Location found for ${player.name} in old table")
            }
        }
        if (location == null) {
            plugin.debugLogger("No previous location was found for ${player.name}")
        }
        return location
    }

}

const val MYSQL_SCHEMA_VERSION_1 = "CREATE TABLE `%s` (\n" +
        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
        "  `uuid` text DEFAULT NULL,\n" +
        "  `player_name` text DEFAULT NULL,\n" +
        "  `world_name` text DEFAULT NULL,\n" +
        "  `coordinate_x` text DEFAULT NULL,\n" +
        "  `coordinate_y` text DEFAULT NULL,\n" +
        "  `coordinate_z` text DEFAULT NULL,\n" +
        "  `yaw` decimal(10,0) DEFAULT NULL,\n" +
        "  `pitch` decimal(10,0) DEFAULT NULL,\n" +
        "  PRIMARY KEY (`id`)\n" +
        ") "

const val MYSQL_SCHEMA_VERSION_2 = "CREATE TABLE `%s` (\n" +
        "  `uuid` varchar(36) NOT NULL,\n" +
        "  `world_group` varchar(50) NOT NULL,\n" +
        "  `world` tinytext NOT NULL,\n" +
        "  `x` double NOT NULL,\n" +
        "  `y` double NOT NULL,\n" +
        "  `z` double NOT NULL,\n" +
        "  `yaw` float NOT NULL,\n" +
        "  `pitch` float NOT NULL,\n" +
        "  PRIMARY KEY (`uuid`,`world_group`)\n" +
        ")"
