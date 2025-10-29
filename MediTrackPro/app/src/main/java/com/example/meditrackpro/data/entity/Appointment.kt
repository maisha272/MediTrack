package com.example.meditrackpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val apptId: Long = 0,
    val dateTimeEpoch: Long,
    val doctor: String,
    val location: String,
    val purpose: String,
    val notes: String? = null
)
