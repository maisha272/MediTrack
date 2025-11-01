package com.example.meditrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.meditrack.MediTrackApplication
import com.example.meditrack.data.MedRepository

class MedListViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx = app as MediTrackApplication
    private val db = appCtx.db
    private val repo = MedRepository(db, appCtx.scheduler)

    val meds = repo.activeMedsWithNextFlow().asLiveData()
}
