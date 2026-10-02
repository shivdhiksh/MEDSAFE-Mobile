package com.shivdhikshith.medsafe.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.shivdhikshith.medsafe.data.local.database.MedSafeDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class MigrationTest {

    @Test
    fun `migration 1 to 2 executes expected index creation SQL without altering existing tables`() {
        val migration = MedSafeDatabase.MIGRATION_1_2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)

        val executedSql = mutableListOf<String>()
        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        migration.migrate(fakeDb)

        assertEquals(2, executedSql.size)
        // Verify alert_history index creation
        assertTrue(executedSql.any { it.contains("index_alert_history_batchId_alertType") && it.contains("alert_history") })
        // Verify reminders index creation
        assertTrue(executedSql.any { it.contains("index_reminders_isEnabled") && it.contains("reminders") })
        // Verify no DROP or destructive statements exist
        assertTrue(executedSql.none { it.uppercase().contains("DROP TABLE") })
    }

    @Test
    fun `migration 2 to 3 executes expected photo table and index creation SQL without altering existing tables`() {
        val migration = MedSafeDatabase.MIGRATION_2_3
        assertEquals(2, migration.startVersion)
        assertEquals(3, migration.endVersion)

        val executedSql = mutableListOf<String>()
        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        migration.migrate(fakeDb)

        assertEquals(2, executedSql.size)
        // Verify photos table creation
        assertTrue(executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS `photos`") && it.contains("filePath") })
        // Verify index creation
        assertTrue(executedSql.any { it.contains("index_photos_medicineId") && it.contains("photos") })
        // Verify no destructive statement
        assertTrue(executedSql.none { it.uppercase().contains("DROP TABLE") })
    }
}

