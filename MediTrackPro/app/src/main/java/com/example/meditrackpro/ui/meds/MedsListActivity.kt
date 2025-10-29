package com.example.meditrackpro.ui.meds

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityMedsListBinding

class MedsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddMed.setOnClickListener {
            startActivity(Intent(this, AddEditMedicationActivity::class.java))
        }
    }
}
