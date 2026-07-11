package com.example.glucoserecordbook.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `readings_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `measurementDate` INTEGER NOT NULL,
                `recordedAt` INTEGER NOT NULL,
                `value` INTEGER,
                `status` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `modifiedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO `readings_new` (`id`, `measurementDate`, `recordedAt`, `value`, `status`, `type`, `createdAt`, `modifiedAt`)
            SELECT `id`, `measurementDate`, `recordedAt`, `value`, 'RECORDED', `type`, `createdAt`, `modifiedAt`
            FROM `readings`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `readings`")
        database.execSQL("ALTER TABLE `readings_new` RENAME TO `readings`")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_readings_measurementDate_type` ON `readings` (`measurementDate`, `type`)")
    }
}
