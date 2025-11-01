package com.example.meditrack

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.meditrack.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class ConcurrencyTest {

    private lateinit var db: MedDatabase
    private val testDispatcher = Dispatchers.IO

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MedDatabase::class.java
        ).build()
        // Clear all tables before each test
        runBlocking {
            db.medicationDao().deleteAll()
            db.doseLogDao().deleteAll()
        }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun concurrentDoseLogInserts_areAllSaved() = runBlocking {
        // 1. Setup: Insert a medication to log doses against
        val medication = Medication(id = 1, name = "TestMed", dosage = "10mg", form = "TABLET", startDate = LocalDate.now(), endDate = null)
        db.medicationDao().upsert(medication)

        val numberOfThreads = 10
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        // 2. Action: Launch concurrent coroutines to insert dose logs
        for (i in 1..numberOfThreads) {
            launch(testDispatcher) {
                try {
                    val doseLog = DoseLog(
                        medId = medication.id,
                        scheduledAt = LocalDateTime.now(),
                        takenAt = LocalDateTime.now(),
                        status = "TAKEN"
                    )
                    db.doseLogDao().insert(doseLog)
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all threads to complete
        latch.await()
        executor.shutdown()

        // 3. Assert: Verify that all dose logs were inserted correctly
        val doseLogs = db.doseLogDao().getLogsForMedication(medication.id)
        assertEquals("All dose logs should be saved during concurrent access", numberOfThreads, doseLogs.size)
    }
}