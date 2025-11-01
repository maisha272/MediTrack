
package com.example.meditrack.alarm

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.meditrack.MediTrackApplication
import com.example.meditrack.R
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.MedDatabase
import com.example.meditrack.data.TriggerLog
import com.example.meditrack.ui.MedListActivity
import com.example.meditrack.utils.Notify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", -1)
        if (medId == -1L) return

        val dueAtStr = intent.getStringExtra("DUE_AT") ?: return
        val dueAt = LocalDateTime.parse(dueAtStr)

        val app = ctx.applicationContext as MediTrackApplication

        // log in background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            app.db.triggerLogDao().insert(
                TriggerLog(
                    medId = medId,
                    scheduledAt = dueAtStr,
                    firedAt = LocalDateTime.now().toString(),
                    source = if (app.useExactAlarms) "ALARM" else "WORK"
                )
            )
        }

        // 1) we tell Android “I’m going to finish later”
        val pendingResult = goAsync()

        // 2) do DB work off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val app = ctx.applicationContext as Application
            val db = MedDatabase.get(app)

            // check if we already have a log for this med/time
            val existing = db.doseLogDao()
                .forRange(dueAt.minusMinutes(1), dueAt.plusMinutes(1))
                .firstOrNull { it.medId == medId && it.scheduledAt == dueAt }

            if (existing == null) {
                // create MISSED by default – user can override by tapping “Taken”
                db.doseLogDao().insert(
                    DoseLog(
                        medId = medId,
                        scheduledAt = dueAt,
                        takenAt = LocalDateTime.now(),
                        status = "MISSED"
                    )
                )
            }

            // very important: tell Android we are done
            pendingResult.finish()
        }

        // 3) build + show notification (this is light, OK to do now)

        // stable-ish ID
        val notifId = (medId.toInt() shl 8) or dueAt.minute

        val takenIntent = Intent(ctx, TakenActionReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAtStr)
            putExtra("NOTIF_ID", notifId)
        }
        val takenPi = PendingIntent.getBroadcast(
            ctx, (medId + 1).toInt(), takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(ctx, SnoozeActionReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAtStr)
            putExtra("NOTIF_ID", notifId)
        }
        val snoozePi = PendingIntent.getBroadcast(
            ctx, (medId + 2).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentPi = PendingIntent.getActivity(
            ctx, (medId + 3).toInt(),
            Intent(ctx, MedListActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(ctx, "medication_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take your medicine 💊")
            .setContentText("Dose scheduled for $dueAtStr")
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_done, "Taken", takenPi)
            .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozePi)

        Notify.safeNotify(ctx, notifId, notif)
    }
}
