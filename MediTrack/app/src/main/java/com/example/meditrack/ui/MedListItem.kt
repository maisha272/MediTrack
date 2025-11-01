package com.example.meditrack.ui

import java.time.LocalDateTime

data class MedListItem(
    val id: Long,
    val name: String,
    val dosage: String,
    val nextDue: LocalDateTime?   // null = no future dose
)
