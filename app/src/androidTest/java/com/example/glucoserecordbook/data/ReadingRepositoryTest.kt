package com.example.glucoserecordbook.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ReadingRepositoryTest {
    private lateinit var database: RecordDatabase
    private lateinit var repository: ReadingRepository
    private val date = LocalDate.of(2026, 7, 11)

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RecordDatabase::class.java).allowMainThreadQueries().build()
        repository = ReadingRepository(database)
    }
    @After fun tearDown() = database.close()

    @Test fun saveThenRetrieveForDate() = runBlocking {
        repository.save(date, ReadingType.POST_LUNCH, 149)
        assertEquals(149, repository.observeForDate(date).first().single().value)
    }

    @Test fun savingSameDateAndTypeUpdatesRatherThanDuplicating() = runBlocking {
        repository.save(date, ReadingType.FASTING, 116)
        repository.save(date, ReadingType.FASTING, 118)
        val values = repository.observeForDate(date).first()
        assertEquals(1, values.size)
        assertEquals(118, values.single().value)
    }

    @Test fun deleteOnlyRemovesSelectedReading() = runBlocking {
        val first = repository.save(date, ReadingType.FASTING, 100)
        repository.save(date, ReadingType.POST_DINNER, 200)
        repository.delete(first)
        assertEquals(listOf(ReadingType.POST_DINNER), repository.observeForDate(date).first().map { it.type })
    }

    @Test fun allRecordsAreOrderedWithNewestDateFirst() = runBlocking {
        repository.save(date.minusDays(1), ReadingType.FASTING, 90)
        repository.save(date, ReadingType.FASTING, 100)
        assertEquals(listOf(date, date.minusDays(1)), repository.observeAll().first().map { it.date })
    }
}
