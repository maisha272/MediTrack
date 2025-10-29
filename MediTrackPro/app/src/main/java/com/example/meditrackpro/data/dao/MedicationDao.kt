package com.example.meditrackpro.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.meditrackpro.data.entity.Medication

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications")
    fun getAllMedications(): LiveData<List<Medication>>

    @Query("SELECT * FROM medications WHERE medId = :id")
    suspend fun getMedicationById(id: Long): Medication?
}
