package com.example.meditrackpro.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.meditrackpro.data.entity.Appointment
import com.example.meditrackpro.data.repo.MediRepository

class MainViewModel(private val repository: MediRepository) : ViewModel() {
    // TODO: Implement LiveData for today's doses, adherence, and next appointment
    val nextAppointment: LiveData<Appointment?> = repository.nextAppointment
}
