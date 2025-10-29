package com.example.meditrackpro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.meditrackpro.data.entity.SymptomEntry

@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptomEntry: SymptomEntry)

    @Update
    suspend fun update(symptomEntry: SymptomEntry)

    @Delete
    suspend fun delete(symptomEntry: SymptomEntry)

    @Query("SELECT * FROM symptom_entries ORDER BY dateEpoch DESC")
    fun getAllSymptomEntries(): LiveData<List<SymptomEntry>>

    @Query("SELECT * FROM symptom_entries WHERE symptomId = :id")
    suspend fun getSymptomEntryById(id: Long): SymptomEntry?

    // TODO: Implement weekly aggregate query
    @Query("SELECT AVG(severity) FROM symptom_entries WHERE dateEpoch >= :startOfWeekEpoch")
    fun getWeeklyAverageSeverity(startOfWeekEpoch: Long): LiveData<Double>
}
