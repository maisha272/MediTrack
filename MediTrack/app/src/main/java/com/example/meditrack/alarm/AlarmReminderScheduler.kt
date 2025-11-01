package com.example.meditrack.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.meditrack.data.MedDatabase
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReminderScheduler(
    private val ctx: Context,
    private val db: MedDatabase
) : ReminderScheduler {

    private val am: AlarmManager =
        ctx.getSystemService(AlarmManager::class.java)

    override fun scheduleDose(medId: Long, dueAt: LocalDateTime) {
        val i = Intent(ctx, NotificationReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAt.toString())
        }

        val reqCode = (medId.toInt() shl 8) or dueAt.toLocalTime().toSecondOfDay()
        val pi = PendingIntent.getBroadcast(
            ctx,
            reqCode,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = dueAt.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pi
                )
            }
        } else {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
            )
        }
    }

    override fun rescheduleForMedication(medId: Long) {
        // reuse your existing logic that walks days + times
        val inner = DoseScheduler(ctx.applicationContext as android.app.Application, db)
        // this will call schedule(...) many times
        // so keep it for back-compat
        CoroutineScope(Dispatchers.IO).launch {
            inner.rescheduleForMedication(medId)
        }
    }

    override fun cancelForMedication(medId: Long) {
        val inner = DoseScheduler(ctx.applicationContext as android.app.Application, db)
        CoroutineScope(Dispatchers.IO).launch {
            inner.cancelForMedication(medId)
        }
    }
}
