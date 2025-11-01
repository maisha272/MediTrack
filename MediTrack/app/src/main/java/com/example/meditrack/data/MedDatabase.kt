package com.example.meditrack.data

import android.app.Application
import androidx.room.*

@Database(
    entities = [Medication::class, DoseTime::class, ScheduleRule::class, DoseLog::class, TriggerLog::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class MedDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseTimeDao(): DoseTimeDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun doseLogDao(): DoseLogDao

    abstract fun triggerLogDao(): TriggerLogDao

    companion object {
        @Volatile private var INSTANCE: MedDatabase? = null
        fun get(app: Application): MedDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(app, MedDatabase::class.java, "med.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
