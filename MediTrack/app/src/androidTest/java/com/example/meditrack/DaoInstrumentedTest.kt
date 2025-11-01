package com.example.meditrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.DoseLogDao
import com.example.meditrack.data.DoseTime
import com.example.meditrack.data.DoseTimeDao
import com.example.meditrack.data.MedDatabase
import com.example.meditrack.data.Medication
import com.example.meditrack.data.MedicationDao
import com.example.meditrack.data.ScheduleRule
import com.example.meditrack.data.ScheduleRuleDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Instrumented tests for the Room DAOs.
 * Uses an in-memory DB so it won’t touch your real app DB.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest {

    // make LiveData post instantly
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: MedDatabase
    private lateinit var medDao: MedicationDao
    private lateinit var doseTimeDao: DoseTimeDao
    private lateinit var scheduleRuleDao: ScheduleRuleDao
    private lateinit var doseLogDao: DoseLogDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            MedDatabase::class.java
        )
            .allowMainThreadQueries()   // OK for tests
            .build()

        medDao = db.medicationDao()
        doseTimeDao = db.doseTimeDao()
        scheduleRuleDao = db.scheduleRuleDao()
        doseLogDao = db.doseLogDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertMedication_andReadBack() = runBlocking {
        val med = Medication(
            name = "Panadol",
            dosage = "500mg",
            form = "pill",
            notes = null,
            startDate = LocalDate.now(),
            endDate = null
        )

        val id = medDao.upsert(med)
        val fetched = medDao.get(id)

        Assert.assertNotNull(fetched)
        Assert.assertEquals("Panadol", fetched?.name)
        Assert.assertEquals("500mg", fetched?.dosage)
    }

    @Test
    fun insertDoseLog_andVerifyTakenStatus() = runBlocking {
        // 1) create a med
        val medId = medDao.upsert(
            Medication(
                name = "TestMed",
                dosage = "10mg",
                form = "pill",
                notes = null,
                startDate = LocalDate.now(),
                endDate = null
            )
        )

        // 2) insert a TAKEN log
        val scheduledTime = LocalDateTime.now().withSecond(0).withNano(0)
        doseLogDao.insert(
            DoseLog(
                medId = medId,
                scheduledAt = scheduledTime,
                takenAt = LocalDateTime.now(),
                status = "TAKEN"
            )
        )

        // 3) read it back from LiveData
        val logs = doseLogDao.recentForMed(medId).getOrAwaitValue()
        Assert.assertTrue(logs.any { it.status == "TAKEN" })
    }

    @Test
    fun updateMedication_andReadBack() = runBlocking {
        val med = Medication(
            name = "Panadol",
            dosage = "500mg",
            form = "pill",
            notes = null,
            startDate = LocalDate.now(),
            endDate = null
        )
        val id = medDao.upsert(med)

        val fetched = medDao.get(id)
        Assert.assertNotNull(fetched)

        val updatedMed = fetched!!.copy(name = "Panadol Extra")
        medDao.upsert(updatedMed)

        val fetchedAgain = medDao.get(id)
        Assert.assertEquals("Panadol Extra", fetchedAgain?.name)
    }

    @Test
    fun deleteMedication_andVerify() = runBlocking {
        val med = Medication(
            name = "Panadol",
            dosage = "500mg",
            form = "pill",
            notes = null,
            startDate = LocalDate.now(),
            endDate = null
        )
        val id = medDao.upsert(med)
        Assert.assertNotNull(medDao.get(id))

        medDao.delete(med.copy(id = id))
        Assert.assertNull(medDao.get(id))
    }

    @Test
    fun deleteMedication_cascadeDeletesDoseTimesAndScheduleRules() = runBlocking {
        // 1. Insert a medication
        val med = Medication(
            name = "Test Med",
            dosage = "100mg",
            form = "pill",
            notes = null,
            startDate = LocalDate.now(),
            endDate = null
        )
        val medId = medDao.upsert(med)

        // 2. Insert associated DoseTime and ScheduleRule
        val doseTime = DoseTime(medId = medId, timeOfDay = LocalTime.of(8, 0))
        doseTimeDao.insertAll(listOf(doseTime))

        val scheduleRule = ScheduleRule(medId = medId, type = "EVERY_DAY", intervalDays = 1)
        scheduleRuleDao.upsert(scheduleRule)

        // 3. Verify insertion
        Assert.assertNotNull(medDao.get(medId))
        Assert.assertFalse(doseTimeDao.listForMed(medId).isEmpty())
        Assert.assertNotNull(scheduleRuleDao.getForMed(medId))

        // 4. Delete the medication
        medDao.delete(med.copy(id = medId))

        // 5. Verify cascade delete
        Assert.assertNull(medDao.get(medId))
        Assert.assertTrue(doseTimeDao.listForMed(medId).isEmpty())
        Assert.assertNull(scheduleRuleDao.getForMed(medId))
    }
}