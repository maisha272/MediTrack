// utils/Notify.kt
package com.example.meditrack.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notify {
    fun safeNotify(ctx: Context, id: Int, builder: NotificationCompat.Builder) {
        val hasPerm = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPerm) return

        val nm = NotificationManagerCompat.from(ctx)
        if (!nm.areNotificationsEnabled()) return

        try {
            nm.notify(id, builder.build())
        } catch (se: SecurityException) {
            Log.w("Notify", "notify() blocked by permission policy", se)
        }
    }
}

