package com.example.meditrackpro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.meditrackpro.data.entity.DoseLog

@Dao
interface DoseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doseLog: DoseLog)

    @Update
    suspend fun update(doseLog: DoseLog)

    @Delete
    suspend fun delete(doseLog: DoseLog)

    @Query("SELECT * FROM dose_logs WHERE scheduledEpoch >= :startOfDayEpoch AND scheduledEpoch < :endOfDayEpoch ORDER BY scheduledEpoch ASC")
    fun getDoseLogsForToday(startOfDayEpoch: Long, endOfDayEpoch: Long): LiveData<List<DoseLog>>

    // TODO: Implement weekly adherence query
    @Query("SELECT COUNT(*) FROM dose_logs WHERE status = 'TAKEN' AND scheduledEpoch >= :startOfWeekEpoch")
    fun getTakenDosesCount(startOfWeekEpoch: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM dose_logs WHERE scheduledEpoch >= :startOfWeekEpoch")
    fun getTotalDosesCount(startOfWeekEpoch: Long): LiveData<Int>
}
