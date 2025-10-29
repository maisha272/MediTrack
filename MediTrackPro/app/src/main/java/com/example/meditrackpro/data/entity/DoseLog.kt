package com.example.meditrackpro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["medId"],
        childColumns = ["medId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["medId"])]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val medId: Long,
    val scheduledEpoch: Long,
    var takenEpoch: Long? = null,
    var status: String // TAKEN, MISSED, SKIPPED
) {
    companion object {
        const val TAKEN = "TAKEN"
        const val MISSED = "MISSED"
        const val SKIPPED = "SKIPPED"
        const val UPCOMING = "UPCOMING"
    }
}