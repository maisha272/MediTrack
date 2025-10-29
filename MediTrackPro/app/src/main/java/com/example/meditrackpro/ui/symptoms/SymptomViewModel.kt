package com.example.meditrackpro.ui.symptoms

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.meditrackpro.data.entity.SymptomEntry
import com.example.meditrackpro.data.repo.MediRepository

class SymptomViewModel(private val repository: MediRepository) : ViewModel() {
    val allSymptomEntries: LiveData<List<SymptomEntry>> = repository.allSymptomEntries

    // TODO: Implement weekly average severity
}
