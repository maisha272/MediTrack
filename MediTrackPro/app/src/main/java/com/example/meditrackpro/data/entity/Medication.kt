package com.example.meditrackpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medId: Long = 0,
    val name: String,
    val dosageText: String,
    val color: Int = 0xFF4285F4.toInt(), // Default to Google Blue
    val notes: String? = null
)