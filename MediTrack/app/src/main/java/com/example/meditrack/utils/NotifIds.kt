package com.example.meditrack.utils

import java.time.LocalDateTime
import java.time.ZoneId

object NotifIds {
    fun build(medId: Long, dueAtIso: String): Int {
        val epochSec = LocalDateTime.parse(dueAtIso)
            .atZone(ZoneId.systemDefault()).toEpochSecond()
        // Stable Int based on med + due time
        return ((medId and 0x7FFF_FFFFL).toInt() * 31) xor (epochSec and 0x7FFF_FFFFL).toInt()
    }
}

