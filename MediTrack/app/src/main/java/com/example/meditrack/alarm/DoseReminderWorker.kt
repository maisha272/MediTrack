package com.example.meditrack.alarm

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DoseReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val medId = inputData.getLong("MED_ID", -1)
        val dueAtStr = inputData.getString("DUE_AT") ?: return Result.failure()

        // fire notification broadcast
        val i = Intent(applicationContext, NotificationReceiver::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("DUE_AT", dueAtStr)
        }
        applicationContext.sendBroadcast(i)

        return Result.success()
    }
}
