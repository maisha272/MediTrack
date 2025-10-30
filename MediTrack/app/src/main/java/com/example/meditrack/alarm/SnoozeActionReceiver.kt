package com.example.meditrack.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.MedDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class SnoozeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", -1L)
        val dueAtIso = intent.getStringExtra("DUE_AT") ?: return
        val notifId = intent.getIntExtra("NOTIF_ID", 0)
        if (medId == -1L || notifId == 0) return

        // 1) Dismiss current notification
        NotificationManagerCompat.from(context).cancel(notifId)

        // 2) Re-schedule SAME med +10 minutes
        val app = context.applicationContext as android.app.Application
        val db = MedDatabase.get(app)

        CoroutineScope(Dispatchers.IO).launch {
            // schedule in 10 minutes using your existing helper
            DoseScheduler(app, db).scheduleInMinutes(medId, 10)

            // 3) (optional) log it
            val scheduledAt = LocalDateTime.parse(dueAtIso)
            val snoozedAt = LocalDateTime.now(ZoneId.systemDefault())

            // if you want to keep history:
            db.doseLogDao().insert(
                DoseLog(
                    medId = medId,
                    scheduledAt = scheduledAt,   // the original due time
                    takenAt = snoozedAt,         // or null if you prefer
                    status = "SNOOZED"
                )
            )
        }
    }
}

