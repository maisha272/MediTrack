package com.example.meditrack.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.example.meditrack.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.*
import kotlin.math.min

class DoseScheduler(
    private val app: Application,
    private val db: MedDatabase
) : ReminderScheduler {
    private val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val nm = NotificationManagerCompat.from(app)
        val ch = NotificationChannelCompat.Builder("MED_REMINDERS",
            android.app.NotificationManager.IMPORTANCE_HIGH)
            .setName("Medicine reminders")
            .setDescription("Alerts for scheduled doses")
            .build()
        nm.createNotificationChannel(ch)
    }

    override fun rescheduleForMedication(medId: Long) {
        scope.launch {
            val med = db.medicationDao().get(medId) ?: return@launch
            val rule = db.scheduleRuleDao().getForMed(medId) ?: return@launch
            val times = db.doseTimeDao().listForMed(medId)

            val today = LocalDate.now()
            val end = listOfNotNull(med.endDate, today.plusDays(7)).minBy { it.toEpochDay() }
            var d = today
            while (!d.isAfter(end)) {
                if (isActiveOn(rule, med.startDate, d)) {
                    for (t in times) {
                        val dt = LocalDateTime.of(d, t.timeOfDay)
                        if (dt.isAfter(LocalDateTime.now().minusSeconds(5))) {
                            scheduleDose(medId, dt)
                        }
                        android.util.Log.d("DoseScheduler", "Scheduling $medId at $dt")

                    }
                }
                d = d.plusDays(1)
            }
        }
    }

    private fun isActiveOn(rule: ScheduleRule, start: LocalDate, day: LocalDate): Boolean = when (rule.type) {
        "DAILY" -> !day.isBefore(start)
        "EVERY_N_DAYS" -> {
            val n = rule.intervalDays ?: 1
            val diff = java.time.temporal.ChronoUnit.DAYS.between(start, day).toInt()
            diff >= 0 && diff % n == 0
        }
        "DOW" -> {
            val set = (rule.daysOfWeek ?: "").split(",").map { it.trim() }.toSet()
            val dow = day.dayOfWeek.name.substring(0,3) // MON,TUE,...
            set.contains(dow)
        }
        else -> false
    }

    override fun scheduleDose(medId: Long, dueAt: LocalDateTime) {
        val epochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val req = ((medId and 0x7FFF_FFFFL).toInt() * 31) xor (epochMillis and 0x7FFF_FFFFL).toInt()
        // alternatively:
        // val epochSec = dueAt.atZone(ZoneId.systemDefault()).toEpochSecond()
        // val req = ((medId and 0x7FFF_FFFFL).toInt() * 31) xor (epochSec and 0x7FFF_FFFFL).toInt()

        val intent = Intent(app, NotificationReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAt.toString())
        }
        val pi = PendingIntent.getBroadcast(
            app, req, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerAt = epochMillis
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    android.util.Log.w("DoseScheduler", "Exact alarm not permitted — skipping")
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    override fun cancelForMedication(medId: Long) {
        scope.launch {
            val med = db.medicationDao().get(medId) ?: return@launch
            val rule = db.scheduleRuleDao().getForMed(medId) ?: return@launch
            val times = db.doseTimeDao().listForMed(medId)

            val today = LocalDate.now()
            val end = listOfNotNull(med.endDate, today.plusDays(7)).minBy { it.toEpochDay() }
            var d = today
            while (!d.isAfter(end)) {
                if (isActiveOn(rule, med.startDate, d)) {
                    for (t in times) {
                        val dt = LocalDateTime.of(d, t.timeOfDay)
                        val epochMillis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val req = ((medId and 0x7FFF_FFFFL).toInt() * 31) xor (epochMillis and 0x7FFF_FFFFL).toInt()
                        val intent = Intent(app, NotificationReceiver::class.java)
                        val pi = PendingIntent.getBroadcast(
                            app, req, intent,
                            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                        )
                        if (pi != null) {
                            am.cancel(pi)
                        }
                    }
                }
                d = d.plusDays(1)
            }
        }
    }

    // used by SnoozeActionReceiver
    fun scheduleInMinutes(medId: Long, minutes: Long) {
        val dueAt = LocalDateTime.now().plusMinutes(minutes)
        scheduleDose(medId, dueAt)
    }
}
