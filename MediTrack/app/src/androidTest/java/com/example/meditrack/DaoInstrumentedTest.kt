package com.example.meditrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.DoseLogDao
import com.example.meditrack.data.DoseTimeDao
import com.example.meditrack.data.MedDatabase
import com.example.meditrack.data.Medication
import com.example.meditrack.data.MedicationDao
import com.example.meditrack.data.ScheduleRuleDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

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
}
