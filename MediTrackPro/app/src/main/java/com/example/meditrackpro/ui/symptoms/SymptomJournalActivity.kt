package com.example.meditrackpro.ui.symptoms

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivitySymptomJournalBinding

class SymptomJournalActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySymptomJournalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddSymptom.setOnClickListener {
            startActivity(Intent(this, AddEditSymptomActivity::class.java))
        }
    }
}
