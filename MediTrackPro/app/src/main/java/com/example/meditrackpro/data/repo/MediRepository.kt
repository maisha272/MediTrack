package com.example.meditrackpro.data.repo

import androidx.lifecycle.LiveData
import com.example.meditrackpro.data.dao.*
import com.example.meditrackpro.data.entity.*

class MediRepository(
    private val medicationDao: MedicationDao,
    private val doseScheduleDao: DoseScheduleDao,
    private val doseLogDao: DoseLogDao,
    private val symptomDao: SymptomDao,
    private val appointmentDao: AppointmentDao
) {
    val allMedications: LiveData<List<Medication>> = medicationDao.getAllMedications()
    val allSymptomEntries: LiveData<List<SymptomEntry>> = symptomDao.getAllSymptomEntries()
    val allAppointments: LiveData<List<Appointment>> = appointmentDao.getAllAppointments()
    val nextAppointment: LiveData<Appointment?> = appointmentDao.getNextUpcomingAppointment()

    // TODO: Implement other repository methods
}
