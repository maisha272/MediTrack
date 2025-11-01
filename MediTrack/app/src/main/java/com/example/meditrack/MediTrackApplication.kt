package com.example.meditrack

import android.app.Application
import com.example.meditrack.alarm.AlarmReminderScheduler
import com.example.meditrack.alarm.ReminderScheduler
import com.example.meditrack.alarm.WorkManagerReminderScheduler
import com.example.meditrack.data.MedDatabase
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MediTrackApplication : Application() {

    // flip this to false to run the WorkManager variant for your investigation
    val useExactAlarms: Boolean = true

    lateinit var db: MedDatabase
    lateinit var scheduler: ReminderScheduler



    override fun onCreate() {
        super.onCreate()

        db = MedDatabase.get(this)

        scheduler = if (useExactAlarms) {
            AlarmReminderScheduler(this, db)
        } else {
            WorkManagerReminderScheduler(this, db)
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Reminders"
            val descriptionText = "Notifications to remind you to take your medication"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("medication_reminders", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
