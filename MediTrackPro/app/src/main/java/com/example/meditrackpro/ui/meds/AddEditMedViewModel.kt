package com.example.meditrackpro.ui.meds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrackpro.data.entity.Medication
import com.example.meditrackpro.data.repo.MediRepository
import kotlinx.coroutines.launch

class AddEditMedViewModel(private val repository: MediRepository) : ViewModel() {
    fun saveMedication(medication: Medication) = viewModelScope.launch {
        // TODO: repository.insertMedication(medication)
    }
}
