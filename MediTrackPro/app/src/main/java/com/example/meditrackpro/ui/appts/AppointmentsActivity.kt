package com.example.meditrackpro.ui.appts

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityAppointmentsBinding

class AppointmentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddAppointment.setOnClickListener {
            startActivity(Intent(this, AddEditAppointmentActivity::class.java))
        }
    }
}
