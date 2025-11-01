package com.example.meditrack.data

import androidx.room.*
import java.time.*

@Entity
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,            // e.g., "500 mg"
    val form: String,              // "pill" | "liquid" | "injection"
    val notes: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate?,       // null = indefinite
    val archived: Boolean = false
)

@Entity
data class TriggerLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val scheduledAt: String,
    val firedAt: String,
    val source: String
)


@Entity(
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medId")]
)
data class DoseTime(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val timeOfDay: LocalTime        // e.g., 08:00
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medId")]
)
data class ScheduleRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val type: String,               // "DAILY", "EVERY_N_DAYS", "DOW"
    val intervalDays: Int? = null,  // for EVERY_N_DAYS
    val daysOfWeek: String? = null  // e.g., "MON,WED,FRI"
)



@Entity(indices = [Index("medId"), Index("scheduledAt")])
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val scheduledAt: LocalDateTime, // when it was due
    val takenAt: LocalDateTime?,    // null = not taken yet
    val status: String              // "TAKEN", "MISSED", "SKIPPED"
)
