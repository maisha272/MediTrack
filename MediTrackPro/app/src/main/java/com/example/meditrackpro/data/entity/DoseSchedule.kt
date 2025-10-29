package com.example.meditrackpro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_schedules",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["medId"],
        childColumns = ["medId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["medId"])]
)
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true)
    val scheduleId: Long = 0,
    val medId: Long,
    val timeOfDay: String, // "HH:mm"
    val daysMask: Int,     // 7-bit, Sun..Sat
    val withFood: Boolean
)