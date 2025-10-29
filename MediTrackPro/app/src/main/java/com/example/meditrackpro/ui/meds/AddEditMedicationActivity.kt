package com.example.meditrackpro.ui.meds

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityAddEditMedicationBinding

class AddEditMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddSchedule.setOnClickListener {
            // TODO: Dynamically add schedule view
        }
    }
}
