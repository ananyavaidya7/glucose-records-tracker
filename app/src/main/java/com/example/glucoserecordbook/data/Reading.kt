package com.example.glucoserecordbook.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class ReadingType(val label: String, val shortLabel: String) {
    FASTING("Fasting", "Fasting"),
    POST_BREAKFAST("2 Hours After Breakfast", "After Breakfast"),
    POST_LUNCH("2 Hours After Lunch", "After Lunch"),
    POST_DINNER("2 Hours After Dinner", "After Dinner")
}

enum class ReadingStatus {
    RECORDED,
    SKIPPED
}

/** One paper-log equivalent: there can be only one value of each type on a date. */
@Entity(
    tableName = "readings",
    indices = [Index(value = ["measurementDate", "type"], unique = true)]
)
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measurementDate: Long,
    val recordedAt: Long,
    val value: Int?,
    val status: ReadingStatus,
    val type: ReadingType,
    val createdAt: Long,
    val modifiedAt: Long
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(measurementDate)
    val isSkipped: Boolean get() = status == ReadingStatus.SKIPPED
}

fun nextUnresolved(readings: Map<ReadingType, Reading>): ReadingType? =
    ReadingType.entries.firstOrNull { it !in readings }
