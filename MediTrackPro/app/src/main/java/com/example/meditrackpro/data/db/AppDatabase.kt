package com.example.meditrackpro.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meditrackpro.data.dao.*
import com.example.meditrackpro.data.entity.*

@Database(entities = [Medication::class, DoseSchedule::class, DoseLog::class, SymptomEntry::class, Appointment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseScheduleDao(): DoseScheduleDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun symptomDao(): SymptomDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meditrack.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
