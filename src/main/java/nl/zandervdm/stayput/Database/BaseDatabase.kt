package nl.zandervdm.stayput.Database

import nl.zandervdm.stayput.Main
import org.bukkit.World
import org.bukkit.entity.Player
import java.sql.Connection
import java.util.*
import java.util.regex.Pattern

const val OLD_TABLE = "stayput_old"


open class BaseDatabase(val plugin: Main) {
    protected var conn = getConnection()
    open val SCHEMA_VERSION = 0
    val table = "stayput_position"
    var hasOldTable = hasTable(OLD_TABLE)

    companion object {
        fun open(plugin: Main): BaseDatabase {
            val type = plugin.config.getString("database.type")?.lowercase(Locale.getDefault()) ?: throw Exception("No database type defined.")
            val database = when (type) {
                "mysql" -> MySQL(plugin)
                "mariadb" -> MySQL(plugin)
                "sqlite" -> SQLite(plugin)
                else -> throw Exception("Unknown database type $type")
            }
            database.initialize()
            return database
        }
    }

    fun isValidSQLIdentifier(s: String): Boolean {
        val p = Pattern.compile("^[a-zA-Z0-9_]{1,255}\$")
        return p.matcher(s).matches()
    }

    fun escapeString(s: String): String {
        val p = Pattern.compile("(\"|\'|\n|\r|\u0000|\u001A|\\\\)")
        return p.matcher(s).replaceAll("")
    }

    open fun getConnection(): Connection {
        throw NotImplementedError()
    }

    open fun initialize() {
        if (doesTableExist()) {
            val currentSchemaVersion = getSchemaVersion()
            if (currentSchemaVersion < SCHEMA_VERSION) {
                migrateTable(currentSchemaVersion)
            }
        } else {
            createTable()
        }
    }
    open fun getSchemaVersion() : Int {
        throw NotImplementedError()
    }

    open fun getSchema(table: String): String? {
        throw NotImplementedError()
    }

    // Does the given table exist?
    protected fun hasTable(table: String) : Boolean {
        val resultSet = conn.metaData.getTables(null, null, table, arrayOf("TABLE"))
        var count = 0
        while (resultSet.next()) {
            val name = resultSet.getString("TABLE_NAME")
            val schema = resultSet.getString("TABLE_SCHEM")
            println("Alternate schema??? $schema")
            count++
        }
        return count == 1
    }

    // Does the stayput_position table exist?
    fun doesTableExist(): Boolean {
        return hasTable(table)
    }

    open fun migrateTable(currentVersion: Int) {
        throw NotImplementedError()
    }

    open fun createTable() {
        throw NotImplementedError()
    }

    open fun getLocation(player: Player, world: World) : PlayerLocation? {
        throw NotImplementedError()
    }

    open fun setLocation(location: PlayerLocation) {
        val stmt = conn.createStatement()
        assert(location.location.world != null)
        val query = "replace into $table (uuid, world_group, world, x, y, z, yaw, pitch) " +
                "values ('${location.player.uniqueId}', '${escapeString(location.group)}', '${escapeString(location.location.world!!.name)}', ${location.location.x}, ${location.location.y}, ${location.location.z}, ${location.location.yaw}, ${location.location.pitch});"
        stmt.executeUpdate(query)
        plugin.debugLogger("Set location for ${location.player.name}!")
    }

}