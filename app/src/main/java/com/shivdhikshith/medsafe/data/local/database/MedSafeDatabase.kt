package com.shivdhikshith.medsafe.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shivdhikshith.medsafe.core.common.Constants
import com.shivdhikshith.medsafe.data.local.dao.AlertHistoryDao
import com.shivdhikshith.medsafe.data.local.dao.BatchDao
import com.shivdhikshith.medsafe.data.local.dao.MedicineDao
import com.shivdhikshith.medsafe.data.local.dao.PhotoDao
import com.shivdhikshith.medsafe.data.local.dao.ReminderDao
import com.shivdhikshith.medsafe.data.local.entity.AlertHistoryEntity
import com.shivdhikshith.medsafe.data.local.entity.BatchEntity
import com.shivdhikshith.medsafe.data.local.entity.MedicineEntity
import com.shivdhikshith.medsafe.data.local.entity.PhotoEntity
import com.shivdhikshith.medsafe.data.local.entity.ReminderEntity

@Database(
    entities = [
        MedicineEntity::class,
        BatchEntity::class,
        AlertHistoryEntity::class,
        ReminderEntity::class,
        PhotoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MedSafeDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun batchDao(): BatchDao
    abstract fun alertHistoryDao(): AlertHistoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: MedSafeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add composite index on alert_history(batchId, alertType) for fast duplicate checks
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alert_history_batchId_alertType` ON `alert_history` (`batchId`, `alertType`)")
                // Add index on reminders(isEnabled) for fast active reminder queries on boot
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isEnabled` ON `reminders` (`isEnabled`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `photos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `medicineId` INTEGER NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `caption` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`medicineId`) REFERENCES `medicines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_medicineId` ON `photos` (`medicineId`)")
            }
        }

        fun getInstance(context: Context): MedSafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedSafeDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
