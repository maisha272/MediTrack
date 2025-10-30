package com.example.meditrack.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.databinding.ActivityMedListBinding

class MedListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedListBinding
    private val vm: MedListViewModel by viewModels()
    private lateinit var adapter: MedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // 1) set up recycler
        adapter = MedAdapter(emptyList()) { item ->
            // TODO: later open a detail screen for item.med.id
            // startActivity(...)
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        // 2) FAB → add med
        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddEditMedicationActivity::class.java))
        }

        // 3) observe meds with next time
        vm.meds.observe(this) { list ->
            adapter.submit(list)
        }

        // 4) runtime permissions
        // Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        // Exact alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(android.app.AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }
}

