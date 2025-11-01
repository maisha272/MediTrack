package com.example.meditrack.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicationDao {
    @Upsert
    suspend fun upsert(med: Medication): Long

    @Delete
    suspend fun delete(med: Medication)

    @Query("SELECT * FROM Medication WHERE archived = 0 ORDER BY name")
    fun observeActive(): Flow<List<Medication>>

    @Query("SELECT * FROM Medication WHERE id = :id")
    suspend fun get(id: Long): Medication?

    @Query("UPDATE Medication SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM Medication")
    suspend fun deleteAll()
}

@Dao
interface TriggerLogDao {
    @Insert
    suspend fun insert(log: TriggerLog)

    @Query("SELECT * FROM TriggerLog ORDER BY id DESC LIMIT 200")
    suspend fun recent(): List<TriggerLog>
}


@Dao
interface DoseTimeDao {
    @Insert
    suspend fun insertAll(items: List<DoseTime>)

    @Query("DELETE FROM DoseTime WHERE medId = :medId")
    suspend fun deleteForMed(medId: Long)

    @Transaction
    suspend fun replaceForMed(medId: Long, times: List<DoseTime>) {
        deleteForMed(medId)
        insertAll(times)
    }

    @Query("SELECT * FROM DoseTime WHERE medId = :medId ORDER BY timeOfDay")
    suspend fun listForMed(medId: Long): List<DoseTime>
}

@Dao
interface ScheduleRuleDao {
    @Upsert
    suspend fun upsert(rule: ScheduleRule)

    @Query("DELETE FROM ScheduleRule WHERE medId = :medId")
    suspend fun deleteForMed(medId: Long)

    @Transaction
    suspend fun replaceForMed(rule: ScheduleRule) {
        deleteForMed(rule.medId)
        upsert(rule)
    }

    @Query("SELECT * FROM ScheduleRule WHERE medId = :medId")
    suspend fun getForMed(medId: Long): ScheduleRule?
}

@Dao
interface DoseLogDao {
    @Insert
    suspend fun insert(log: DoseLog)

    @Query("SELECT * FROM DoseLog WHERE medId = :medId ORDER BY scheduledAt DESC LIMIT 100")
    fun recentForMed(medId: Long): LiveData<List<DoseLog>>


    @Query("""
        UPDATE DoseLog
        SET takenAt = :takenAt, status = :status
        WHERE medId = :medId AND scheduledAt = :scheduledAt
    """)
    suspend fun updateMark(
        medId: Long,
        scheduledAt: java.time.LocalDateTime,
        takenAt: java.time.LocalDateTime,
        status: String
    ): Int

    // ✅ new: fetch all dose logs for a given day/range
    @Query("""
        SELECT * FROM DoseLog
        WHERE scheduledAt BETWEEN :from AND :to
    """)
    suspend fun forRange(
        from: java.time.LocalDateTime,
        to: java.time.LocalDateTime
    ): List<DoseLog>

    @Query("SELECT * FROM DoseLog ORDER BY scheduledAt DESC")
    suspend fun allLogs(): List<DoseLog>

    @Query("DELETE FROM DoseLog")
    suspend fun deleteAll()

    @Query("SELECT * FROM DoseLog WHERE medId = :medId")
    suspend fun getLogsForMedication(medId: Long): List<DoseLog>
}
