package com.example.meditrack.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.meditrack.data.MedDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class DoseDisplay(
    val medName: String,
    val time: String,
    val status: String
)

class CalendarViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MedDatabase.get(app)

    private val _logs = MutableLiveData<List<DoseDisplay>>()
    val logs: LiveData<List<DoseDisplay>> = _logs

    fun loadForDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1) All active meds
            val meds = db.medicationDao().observeActive().first()

            // 2) All taken/missed logs for that day (once)
            val dayStart = date.atStartOfDay()
            val dayEnd = date.atTime(23, 59, 59)
            val dayLogs = db.doseLogDao().forRange(dayStart, dayEnd)

            val displays = mutableListOf<DoseDisplay>()

            for (med in meds) {
                val times = db.doseTimeDao().listForMed(med.id)

                for (t in times) {
                    // scheduled datetime for that med on this date
                    val scheduled = LocalDateTime.of(date, t.timeOfDay)
                        .withSecond(0).withNano(0)

                    // does DB have a log for EXACTLY this med + time?
                    val match = dayLogs.firstOrNull { log ->
                        log.medId == med.id &&
                                log.scheduledAt
                                    .withSecond(0).withNano(0) == scheduled
                    }

                    val status = when {
                        match != null && match.status == "TAKEN" -> "✅ Taken"
                        scheduled.isBefore(LocalDateTime.now()) -> "🔴 Missed"
                        else -> "🕓 Due"
                    }

                    displays += DoseDisplay(
                        medName = med.name,
                        time = t.timeOfDay.toString(),
                        status = status
                    )
                }
            }

            // sort by time for nicer list
            _logs.postValue(displays.sortedBy { it.time })
        }
    }
}

