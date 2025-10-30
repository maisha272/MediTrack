package com.example.meditrackpro.ui.meds

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrackpro.databinding.ActivityMedsListBinding

class MedsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedsListBinding
    private val medsViewModel: MedsViewModel by viewModels()
    private lateinit var medsAdapter: MedsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        medsViewModel.allMedications.observe(this) { medications ->
            medications?.let { medsAdapter.submitList(it) }
        }

        binding.fabAddMed.setOnClickListener {
            startActivity(Intent(this, AddEditMedicationActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        medsAdapter = MedsAdapter { medication ->
            // TODO: Handle medication edit
        }
        binding.medsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MedsListActivity)
            adapter = medsAdapter
        }
    }
}