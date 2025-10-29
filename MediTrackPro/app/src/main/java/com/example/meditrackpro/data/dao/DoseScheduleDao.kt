package com.example.meditrackpro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.meditrackpro.data.entity.DoseSchedule

@Dao
interface DoseScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doseSchedule: DoseSchedule)

    @Update
    suspend fun update(doseSchedule: DoseSchedule)

    @Query("SELECT * FROM dose_schedules WHERE medId = :medId")
    fun getSchedulesForMedication(medId: Long): LiveData<List<DoseSchedule>>

    @Query("DELETE FROM dose_schedules WHERE medId = :medId")
    suspend fun deleteSchedulesForMedication(medId: Long)
}
