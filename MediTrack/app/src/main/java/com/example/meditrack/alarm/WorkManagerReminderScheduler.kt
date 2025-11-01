package com.example.meditrack.alarm

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meditrack.data.MedDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WorkManagerReminderScheduler(
    private val ctx: Context,
    private val db: MedDatabase
) : ReminderScheduler {

    override fun scheduleDose(medId: Long, dueAt: LocalDateTime) {
        val now = System.currentTimeMillis()
        val dueMillis = dueAt.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val delay = (dueMillis - now).coerceAtLeast(0)

        val data = Data.Builder()
            .putLong("MED_ID", medId)
            .putString("DUE_AT", dueAt.toString())
            .build()

        val req = OneTimeWorkRequestBuilder<DoseReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(ctx).enqueue(req)
    }

    override fun rescheduleForMedication(medId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // we can reuse your DoseScheduler logic again
            val inner = DoseScheduler(ctx.applicationContext as android.app.Application, db)
            inner.rescheduleForMedication(medId)
        }
    }

    override fun cancelForMedication(medId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // we can reuse your DoseScheduler logic again
            val inner = DoseScheduler(ctx.applicationContext as android.app.Application, db)
            inner.cancelForMedication(medId)
        }
    }
}
