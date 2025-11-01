package com.example.meditrack.data

import com.example.meditrack.alarm.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class MedWithNext(
    val med: Medication,
    val nextDue: LocalDateTime?
)

class MedRepository(
    private val db: MedDatabase,
//    private val scheduler: DoseScheduler
    private val scheduler: ReminderScheduler
) {
    fun activeMedsFlow() = db.medicationDao().observeActive()

    // --- new: enrich meds with next scheduled time ---
    fun activeMedsWithNextFlow(): Flow<List<MedWithNext>> =
        db.medicationDao().observeActive().map { meds ->
            meds.map { med ->
                val next = nextDueFor(med.id)
                MedWithNext(med, next)
            }
        }

    // --- new: helper for next scheduled dose ---
    suspend fun nextDueFor(medId: Long): LocalDateTime? = withContext(Dispatchers.IO) {
        val med = db.medicationDao().get(medId) ?: return@withContext null
        val rule = db.scheduleRuleDao().getForMed(medId) ?: return@withContext null
        val times = db.doseTimeDao().listForMed(medId)
        if (times.isEmpty()) return@withContext null

        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val end = med.endDate ?: today.plusDays(7)

        var d = today
        while (!d.isAfter(end)) {
            val active = when (rule.type) {
                "DAILY" -> !d.isBefore(med.startDate)
                "EVERY_N_DAYS" -> {
                    val n = rule.intervalDays ?: 1
                    val diff = ChronoUnit.DAYS.between(med.startDate, d).toInt()
                    diff >= 0 && diff % n == 0
                }
                "DOW" -> {
                    val set = (rule.daysOfWeek ?: "").split(",").map { it.trim() }.toSet()
                    val dow = d.dayOfWeek.name.substring(0, 3)
                    set.contains(dow)
                }
                else -> false
            }

            if (active) {
                for (t in times) {
                    val dt = LocalDateTime.of(d, t.timeOfDay)
                    if (dt.isAfter(now)) return@withContext dt
                }
            }
            d = d.plusDays(1)
        }
        return@withContext null
    }

    suspend fun upsertMedicationWithSchedule(
        med: Medication,
        times: List<java.time.LocalTime>,
        rule: ScheduleRule
    ): Long = withContext(Dispatchers.IO) {
        val id = db.medicationDao().upsert(med)
        val doseTimes = times.map { t -> DoseTime(medId = id, timeOfDay = t) }
        db.doseTimeDao().replaceForMed(id, doseTimes)

        db.scheduleRuleDao().replaceForMed(rule.copy(medId = id))
        // this now goes through whichever scheduler we chose
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

