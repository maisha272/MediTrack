
package com.example.meditrack.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.meditrack.R
import com.example.meditrack.ui.MedListActivity
import com.example.meditrack.utils.NotifIds
import com.example.meditrack.utils.Notify
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", -1L)
        val dueAtStr = intent.getStringExtra("DUE_AT") ?: return
        if (medId == -1L) return
        LocalDateTime.parse(dueAtStr) // parse once if you need it later

        // Stable ID so we can cancel this exact notification later
        val notifId = NotifIds.build(medId, dueAtStr)

        // --- Intents for actions ---
        val takenIntent = Intent(ctx, TakenActionReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAtStr)
            putExtra("NOTIF_ID", notifId)
        }
        val takenPi = PendingIntent.getBroadcast(
            ctx, notifId + 1, takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(ctx, SnoozeActionReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAtStr)
            putExtra("NOTIF_ID", notifId)
        }
        val snoozePi = PendingIntent.getBroadcast(
            ctx, notifId + 2, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentPi = PendingIntent.getActivity(
            ctx, notifId + 3,
            Intent(ctx, MedListActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- Build Notification (channel MUST match the one you created) ---
        val notif = NotificationCompat.Builder(ctx, "MED_REMINDERS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take your medicine 💊")
            .setContentText("Dose scheduled for $dueAtStr")
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_done, "Taken", takenPi)
            .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozePi)
            .build()

        // OLD
// NotificationManagerCompat.from(ctx).notify(notifId, notif)

// NEW — pass the *builder* (or the built notif; both ok if you overload)
        Notify.safeNotify(ctx, notifId, NotificationCompat.Builder(ctx, "MED_REMINDERS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take your medicine 💊")
            .setContentText("Dose scheduled for $dueAtStr")
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_done, "Taken", takenPi)
            .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozePi))

    }
}
