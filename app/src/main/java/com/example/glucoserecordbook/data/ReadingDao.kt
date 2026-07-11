package com.example.glucoserecordbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE measurementDate = :epochDay ORDER BY type")
    fun observeForDate(epochDay: Long): Flow<List<Reading>>

    @Query("SELECT * FROM readings ORDER BY measurementDate DESC, type ASC")
    fun observeAll(): Flow<List<Reading>>

    @Query("SELECT * FROM readings WHERE measurementDate = :epochDay AND type = :type LIMIT 1")
    suspend fun findByDateAndType(epochDay: Long, type: ReadingType): Reading?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reading: Reading): Long

    @Update
    suspend fun update(reading: Reading)

    @Delete
    suspend fun delete(reading: Reading)
}
