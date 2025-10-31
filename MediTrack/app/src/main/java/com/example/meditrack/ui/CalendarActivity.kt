package com.example.meditrack.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.databinding.ActivityCalendarBinding
import java.time.LocalDate
import java.time.ZoneId

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val vm: CalendarViewModel by viewModels()
    private lateinit var adapter: DoseLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = DoseLogAdapter(emptyList())
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        // today
        val today = LocalDate.now()
        vm.loadForDate(today)        // <- pass LocalDate

        // when user taps another day
        binding.calendar.setOnDateChangeListener { _, y, m, d ->
            val picked = LocalDate.of(y, m + 1, d)
            vm.loadForDate(picked)
        }

        vm.logs.observe(this) { list ->
            // here list is already filtered to that day
            adapter.submit(list)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
