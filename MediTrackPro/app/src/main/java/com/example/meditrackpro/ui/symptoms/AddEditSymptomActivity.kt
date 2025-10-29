package com.example.meditrackpro.ui.symptoms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrackpro.databinding.ActivityAddEditSymptomBinding

class AddEditSymptomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditSymptomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditSymptomBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
