package com.example.meditrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.meditrack.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * We don't use the real MedRepository (because it creates a real DoseScheduler).
 * Instead we make a test-version of the repository that accepts a fake scheduler.
 */
@RunWith(AndroidJUnit4::class)
class MedRepositoryTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var db: MedDatabase
    private lateinit var repo: TestMedRepository
    private lateinit var fakeScheduler: FakeScheduler

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MedDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        fakeScheduler = FakeScheduler()
        repo = TestMedRepository(db, fakeScheduler)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertMedicationWithSchedule_savesMedicationTimesRule_andCallsScheduler() = runBlocking {
        // arrange
        val med = Medication(
            name = "Vitamin C",
            dosage = "500 mg",
            form = "pill",
            notes = "after food",
            startDate = LocalDate.now(),
            endDate = null
        )
        val times = listOf(
            LocalTime.of(8, 0),
            LocalTime.of(20, 0)
        )
        val rule = ScheduleRule(
            medId = 0,
            type = "DAILY"
        )

        // act
        val newId = repo.upsertMedicationWithSchedule(med, times, rule)

        // assert DB
        val savedMed = db.medicationDao().get(newId)
        Assert.assertNotNull(savedMed)
        Assert.assertEquals("Vitamin C", savedMed?.name)

        val savedTimes = db.doseTimeDao().listForMed(newId)
        Assert.assertEquals(2, savedTimes.size)
        Assert.assertEquals(LocalTime.of(8, 0), savedTimes[0].timeOfDay)

        val savedRule = db.scheduleRuleDao().getForMed(newId)
        Assert.assertEquals("DAILY", savedRule?.type)

        // assert scheduler
        Assert.assertTrue(fakeScheduler.called)
        Assert.assertEquals(newId, fakeScheduler.lastMedId)
    }

    @Test
    fun markTaken_insertsDoseLog() = runBlocking {
        val medId = db.medicationDao().upsert(
            Medication(
                name = "Panadol",
                dosage = "500 mg",
                form = "pill",
                startDate = LocalDate.now(),
                endDate = null
            )
        )

        val due = LocalDateTime.now().withSecond(0).withNano(0)

        repo.markTaken(medId, due)

        val logs = withContext(Dispatchers.IO) { db.doseLogDao().allLogs() }
        Assert.assertEquals(1, logs.size)
        Assert.assertEquals("TAKEN", logs[0].status)
        Assert.assertEquals(medId, logs[0].medId)
        Assert.assertEquals(due, logs[0].scheduledAt)
    }

    // ---------------------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------------------

    /** minimal fake instead of real DoseScheduler */
    private class FakeScheduler {
        var called = false
        var lastMedId: Long? = null

        fun rescheduleForMedication(medId: Long) {
            called = true
            lastMedId = medId
        }
    }


    private class TestMedRepository(
        private val db: MedDatabase,
        private val scheduler: FakeScheduler
    ) {
        suspend fun upsertMedicationWithSchedule(
            med: Medication,
            times: List<LocalTime>,
            rule: ScheduleRule
        ): Long = withContext(Dispatchers.IO) {
            val id = db.medicationDao().upsert(med)
            val doseTimes = times.map { t -> DoseTime(medId = id, timeOfDay = t) }
            db.doseTimeDao().replaceForMed(id, doseTimes)
            db.scheduleRuleDao().replaceForMed(rule.copy(medId = id))

            // call fake
            scheduler.rescheduleForMedication(id)
            id
        }

        suspend fun markTaken(medId: Long, scheduledAt: LocalDateTime) =
            withContext(Dispatchers.IO) {
                db.doseLogDao().insert(
                    DoseLog(
                        medId = medId,
                        scheduledAt = scheduledAt,
                        takenAt = LocalDateTime.now(),
                        status = "TAKEN"
                    )
                )
            }
    }
}
