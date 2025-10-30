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

class TakenActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", -1L)
        val dueAtIso = intent.getStringExtra("DUE_AT") ?: return
        val notifId = intent.getIntExtra("NOTIF_ID", 0)
        if (medId == -1L || notifId == 0) return

        // 1) Dismiss the shown notification
        NotificationManagerCompat.from(context).cancel(notifId)

        // 2) Mark as TAKEN in DB
        val db = MedDatabase.get(context.applicationContext as android.app.Application)

        CoroutineScope(Dispatchers.IO).launch {
            val scheduledAt = LocalDateTime.parse(dueAtIso)
            val takenAt = LocalDateTime.now(ZoneId.systemDefault())

            val updated = db.doseLogDao().updateMark(medId, scheduledAt, takenAt, "TAKEN")
            if (updated == 0) {
                db.doseLogDao().insert(
                    DoseLog(
                        medId = medId,
                        scheduledAt = scheduledAt,
                        takenAt = takenAt,
                        status = "TAKEN"
                    )
                )
            }

            // (Optional) Refresh future alarms for this med
            DoseScheduler(context.applicationContext as android.app.Application, db)
                .rescheduleForMedication(medId)
        }
    }
}


