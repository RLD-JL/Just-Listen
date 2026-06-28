package com.rld.justlisten.datalayer

import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.AfterVersion
import com.rld.justlisten.LocalDb

object DatabaseSchemaHelper {
    val SafeSchema: SqlSchema<QueryResult.Value<Unit>> = object : SqlSchema<QueryResult.Value<Unit>> {
        override val version: Long get() = LocalDb.Schema.version
        
        override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
            return LocalDb.Schema.create(driver)
        }

        override fun migrate(
            driver: SqlDriver,
            oldVersion: Long,
            newVersion: Long,
            vararg callbacks: AfterVersion
        ): QueryResult.Value<Unit> {
            return try {
                LocalDb.Schema.migrate(driver, oldVersion, newVersion, *callbacks)
            } catch (e: Exception) {
                println("SafeSchema: Database migration failed from version $oldVersion to $newVersion: ${e.message}. Recreating database...")
                try {
                    val tables = listOf(
                        "SettingsInfo", "UserProfileOverrides", "BlockedUser", "HiddenComment",
                        "AddPlaylist", "Library", "PlayHistory", "PlaylistDetail",
                        "SearchScreenInfo", "SyncQueue"
                    )
                    tables.forEach { table ->
                        try {
                            driver.execute(null, "DROP TABLE IF EXISTS $table;", 0)
                        } catch (_: Exception) {}
                    }
                    LocalDb.Schema.create(driver)
                } catch (recreateEx: Exception) {
                    println("SafeSchema: Recreation failed: ${recreateEx.message}")
                }
                QueryResult.Value(Unit)
            }
        }
    }

    fun createDatabase(driver: SqlDriver): LocalDb {
        val db = LocalDb(
            driver,
            Repository.addPlaylistAdapter,
            Repository.libraryAdapter,
            Repository.playlistDetailAdapter,
            Repository.syncQueueAdapter
        )
        // Run a sanity check query. If it fails, clear all tables and recreate!
        try {
            db.syncQueueQueries.getPendingTasks().executeAsList()
            db.settingsScreenQueries.getSettingsInfo().executeAsOneOrNull()
        } catch (e: Exception) {
            println("LocalDb Sanity Check failed: ${e.message}. Recreating database...")
            try {
                val tables = listOf(
                    "SettingsInfo", "UserProfileOverrides", "BlockedUser", "HiddenComment",
                    "AddPlaylist", "Library", "PlayHistory", "PlaylistDetail",
                    "SearchScreenInfo", "SyncQueue"
                )
                tables.forEach { table ->
                    try {
                        driver.execute(null, "DROP TABLE IF EXISTS $table;", 0)
                    } catch (_: Exception) {}
                }
                LocalDb.Schema.create(driver)
            } catch (recreateEx: Exception) {
                println("LocalDb Sanity Check Recovery failed: ${recreateEx.message}")
            }
        }
        return db
    }
}
