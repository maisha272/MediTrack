package com.example.meditrack.data

import androidx.room.TypeConverter
import java.time.*

class Converters {
    @TypeConverter fun fromLocalDate(v: LocalDate?) = v?.toString()
    @TypeConverter fun toLocalDate(v: String?) = v?.let { LocalDate.parse(it) }

    @TypeConverter fun fromLocalTime(v: LocalTime?) = v?.toString()
    @TypeConverter fun toLocalTime(v: String?) = v?.let { LocalTime.parse(it) }

    @TypeConverter fun fromLocalDateTime(v: LocalDateTime?) = v?.toString()
    @TypeConverter fun toLocalDateTime(v: String?) = v?.let { LocalDateTime.parse(it) }
}
