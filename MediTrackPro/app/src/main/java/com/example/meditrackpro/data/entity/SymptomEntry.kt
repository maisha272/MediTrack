package com.example.meditrackpro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptom_entries",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["medId"],
        childColumns = ["medId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["medId"])]
)
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val symptomId: Long = 0,
    val dateEpoch: Long,
    val severity: Int,
    val tags: String,
    val notes: String,
    val medId: Long?
)