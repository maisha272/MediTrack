package com.example.meditrack.alarm

import java.time.LocalDateTime

interface ReminderScheduler {
    /**
     * Schedule ONE dose for a medication at a specific time.
     */
    fun scheduleDose(medId: Long, dueAt: LocalDateTime)

    /**
     * Recreate / refresh all future doses for this medication.
     * (Called after user edits the med)
     */
    fun rescheduleForMedication(medId: Long)

    /**
     * Cancel all future doses for this medication.
     * (Called after user archives the med)
     */
    fun cancelForMedication(medId: Long)
}
