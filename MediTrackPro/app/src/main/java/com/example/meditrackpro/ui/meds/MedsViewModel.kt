package com.example.meditrackpro.ui.meds

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrackpro.data.entity.Medication
import com.example.meditrackpro.data.repo.MediRepository
import kotlinx.coroutines.launch

class MedsViewModel(private val repository: MediRepository) : ViewModel() {
    val allMedications: LiveData<List<Medication>> = repository.allMedications

    fun deleteMedication(medication: Medication) = viewModelScope.launch {
        // TODO: repository.deleteMedication(medication)
    }
}
