package com.example.meditrackpro.ui.appts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.meditrackpro.data.entity.Appointment
import com.example.meditrackpro.data.repo.MediRepository

class AppointmentsViewModel(private val repository: MediRepository) : ViewModel() {
    val allAppointments: LiveData<List<Appointment>> = repository.allAppointments

    // TODO: Implement next upcoming appointment
}
