package com.example.glucoserecordbook.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ReadingRepository(private val database: RecordDatabase) {
    private val dao = database.readingDao()

    fun observeForDate(date: LocalDate): Flow<List<Reading>> = dao.observeForDate(date.toEpochDay())
    fun observeAll(): Flow<List<Reading>> = dao.observeAll()

    suspend fun save(date: LocalDate, type: ReadingType, value: Int): Reading =
        save(date, type, value, ReadingStatus.RECORDED)

    suspend fun saveSkipped(date: LocalDate, type: ReadingType): Reading =
        save(date, type, null, ReadingStatus.SKIPPED)

    private suspend fun save(
        date: LocalDate,
        type: ReadingType,
        value: Int?,
        status: ReadingStatus
    ): Reading = database.withTransaction {
        val now = System.currentTimeMillis()
        val existing = dao.findByDateAndType(date.toEpochDay(), type)
        val record = if (existing == null) {
            Reading(
                measurementDate = date.toEpochDay(), recordedAt = now, value = value, status = status, type = type,
                createdAt = now, modifiedAt = now
            )
        } else {
            existing.copy(value = value, status = status, recordedAt = now, modifiedAt = now)
        }
        if (existing == null) {
            val id = dao.insert(record)
            if (id == -1L) dao.findByDateAndType(date.toEpochDay(), type)!! else record.copy(id = id)
        } else {
            dao.update(record)
            record
        }
    }

    suspend fun delete(reading: Reading) = dao.delete(reading)
}
