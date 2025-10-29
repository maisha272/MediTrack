package com.example.meditrackpro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.meditrackpro.data.entity.Appointment

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment)

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("SELECT * FROM appointments ORDER BY dateTimeEpoch DESC")
    fun getAllAppointments(): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE apptId = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Query("SELECT * FROM appointments WHERE dateTimeEpoch >= :nowEpoch ORDER BY dateTimeEpoch ASC LIMIT 1")
    fun getNextUpcomingAppointment(nowEpoch: Long): LiveData<Appointment?>
}
