package nl.zandervdm.stayput.Database

import nl.zandervdm.stayput.Main
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SQLite(plugin: Main) : BaseDatabase(plugin) {
    override fun getConnection(): Connection {
        val fileName = plugin.config.getString("database.filename", "database.db")
        val file = File(plugin.dataFolder, fileName!!)
        return DriverManager.getConnection("jdbc:sqlite:$file")
    }

    override fun getSchema(table: String): String? {
        val stmt = conn.createStatement()
        val query = "select * from sqlite_schema where name = '$table'"
        val result = stmt.executeQuery(query)
        var schema : String? = null
        while (result.next()) {
            schema = result.getString("sql")
        }
        return schema
    }

    override fun getSchemaVersion(): Int {
        val schema = getSchema(table) ?: throw Exception("Hey dude theres no schema in here!!")
        plugin.debugLogger("Has old table: $hasOldTable")
        plugin.debugLogger("Current schema is:\n$schema")
        if (schema.startsWith(SQLITE_SCHEMA_VERSION_1.format(table))) {
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
        val query = SQLITE_SCHEMA_VERSION_2.format(table)
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
                location = PlayerLocation(player, group, result.getString(4), result.getDouble(5), result.getDouble(6),
                    result.getDouble(7), result.getFloat(8), result.getFloat(9))
                plugin.debugLogger("Location found for ${player.name} in old table")
            }
        }
        if (location == null) {
            plugin.debugLogger("No previous location was found for ${player.name}")
        }
        return location
    }

}

const val SQLITE_SCHEMA_VERSION_1 = "CREATE TABLE `stayput_position` (" +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT , " +
        "`uuid` VARCHAR , " +
        "`player_name` VARCHAR , " +
        "`world_name` VARCHAR , " +
        "`coordinate_x` DOUBLE PRECISION , " +
        "`coordinate_y` DOUBLE PRECISION , " +
        "`coordinate_z` DOUBLE PRECISION , " +
        "`yaw` FLOAT , " +
        "`pitch` FLOAT )"

const val SQLITE_SCHEMA_VERSION_2 = "CREATE TABLE 'stayput_position' (" +
        "`uuid` varchar(36) NOT NULL, " +
        "`world_group` varchar(50) NOT NULL, " +
        "`world` tinytext NOT NULL, " +
        "`x` double NOT NULL, " +
        "`y` double NOT NULL, " +
        "`z` double NOT NULL, " +
        "`yaw` float NOT NULL, " +
        "`pitch` float NOT NULL, " +
        "PRIMARY KEY (`uuid`,`world_group`))"
