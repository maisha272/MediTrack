package com.example.meditrackpro.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }
}
