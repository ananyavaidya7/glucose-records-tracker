package com.example.glucoserecordbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Reading::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao

    companion object {
        fun create(context: Context): RecordDatabase = Room.databaseBuilder(
            context.applicationContext,
            RecordDatabase::class.java,
            "glucose-record-book.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
}
