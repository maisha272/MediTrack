package com.example.meditrackpro.ui.appts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityAddEditAppointmentBinding

class AddEditAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
