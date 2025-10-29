package com.example.meditrackpro.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityMainBinding
import com.example.meditrackpro.ui.meds.MedsListActivity
import com.example.meditrackpro.ui.symptoms.SymptomJournalActivity
import com.example.meditrackpro.ui.appts.AppointmentsActivity
import com.example.meditrackpro.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnManageMeds.setOnClickListener {
            startActivity(Intent(this, MedsListActivity::class.java))
        }

        binding.btnLogSymptom.setOnClickListener {
            startActivity(Intent(this, SymptomJournalActivity::class.java))
        }

        binding.btnAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
