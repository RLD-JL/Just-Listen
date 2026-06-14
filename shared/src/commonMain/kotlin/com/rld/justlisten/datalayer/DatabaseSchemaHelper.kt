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
                println("SafeSchema: Database migration failed from version $oldVersion to $newVersion: ${e.message}")
                QueryResult.Value(Unit)
            }
        }
    }
}
