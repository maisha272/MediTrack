package com.example.meditrack.alarm

import android.app.Application
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

class TakenActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", -1)
        val dueAtStr = intent.getStringExtra("DUE_AT") ?: return
        val notifId = intent.getIntExtra("NOTIF_ID", 0)
        if (medId == -1L) return

        // 1) dismiss notif
        NotificationManagerCompat.from(context).cancel(notifId)

        val dueAt = LocalDateTime.parse(dueAtStr)

        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as Application
            val db = MedDatabase.get(app)

            // try to update existing MISSED row first
            val updated = db.doseLogDao().updateMark(
                medId = medId,
                scheduledAt = dueAt,
                takenAt = LocalDateTime.now(),
                status = "TAKEN"
            )

            if (updated == 0) {
                // there was no MISSED row (maybe user pressed Taken very fast) → insert
                db.doseLogDao().insert(
                    DoseLog(
                        medId = medId,
                        scheduledAt = dueAt,
                        takenAt = LocalDateTime.now(),
                        status = "TAKEN"
                    )
                )
            }

            pending.finish()
        }
    }
}


