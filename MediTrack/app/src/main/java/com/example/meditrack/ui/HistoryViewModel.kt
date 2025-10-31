package com.example.meditrack.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.meditrack.data.MedDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MedDatabase.get(app)

    private val _weeklyStats = MutableLiveData<List<Int>>() // count per day
    val weeklyStats: LiveData<List<Int>> = _weeklyStats

    init {
        loadWeeklyData()
    }

    fun loadWeeklyData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val logs = db.doseLogDao().recentForMed(0).value.orEmpty()
            val data = (0..6).map { offset ->
                val date = today.minusDays(offset.toLong())
                logs.count { it.scheduledAt.toLocalDate() == date && it.status == "TAKEN" }
            }.reversed()
            _weeklyStats.postValue(data)
        }
    }
}
