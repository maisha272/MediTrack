package com.example.meditrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.meditrack.alarm.DoseScheduler
import com.example.meditrack.data.MedDatabase
import com.example.meditrack.data.MedRepository

class MedListViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MedDatabase.get(app)
    private val repo = MedRepository(db, DoseScheduler(app, db))

    // Observe meds enriched with next scheduled time
    val meds = repo.activeMedsWithNextFlow().asLiveData()
}

