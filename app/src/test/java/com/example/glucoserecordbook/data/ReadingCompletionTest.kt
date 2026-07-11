package com.example.glucoserecordbook.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReadingCompletionTest {
    private val date = LocalDate.of(2026, 7, 12)

    @Test fun `recorded and skipped rows both resolve a slot`() {
        val recorded = reading(ReadingType.FASTING, ReadingStatus.RECORDED, 116)
        val skipped = reading(ReadingType.POST_BREAKFAST, ReadingStatus.SKIPPED, null)

        assertNull(nextUnresolved(mapOf(recorded.type to recorded, skipped.type to skipped)))
            .also { /* This map contains only two categories; checked below with all categories. */ }
    }

    @Test fun `next unresolved ignores skipped breakfast and selects lunch`() {
        val readings = listOf(
            reading(ReadingType.FASTING, ReadingStatus.RECORDED, 116),
            reading(ReadingType.POST_BREAKFAST, ReadingStatus.SKIPPED, null),
            reading(ReadingType.POST_DINNER, ReadingStatus.RECORDED, 225)
        ).associateBy { it.type }

        assertEquals(ReadingType.POST_LUNCH, nextUnresolved(readings))
    }

    @Test fun `no next slot remains when numeric and dash states complete the day`() {
        val readings = listOf(
            reading(ReadingType.FASTING, ReadingStatus.RECORDED, 116),
            reading(ReadingType.POST_BREAKFAST, ReadingStatus.SKIPPED, null),
            reading(ReadingType.POST_LUNCH, ReadingStatus.RECORDED, 149),
            reading(ReadingType.POST_DINNER, ReadingStatus.SKIPPED, null)
        ).associateBy { it.type }

        assertNull(nextUnresolved(readings))
    }

    @Test fun `empty slot is unresolved`() {
        assertEquals(ReadingType.FASTING, nextUnresolved(emptyMap()))
    }

    private fun reading(type: ReadingType, status: ReadingStatus, value: Int?): Reading = Reading(
        id = type.ordinal.toLong(),
        measurementDate = date.toEpochDay(),
        recordedAt = 1,
        value = value,
        status = status,
        type = type,
        createdAt = 1,
        modifiedAt = 1
    )
}
