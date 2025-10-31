
package com.example.meditrack.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.meditrack.R
import com.example.meditrack.data.MedDatabase
import com.example.meditrack.databinding.ActivityHistoryBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadData() {
        val app = application
        val db = MedDatabase.get(app)

        lifecycleScope.launch {
            // get all dose logs (we limited to 100 per med before; here we just ask DB for all)
            val today = LocalDate.now()

            // boundaries
            val startOfToday = today.atStartOfDay()
            val startOfWeek = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay()
            val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
            val endNow = LocalDateTime.now()

            val logs = withContext(Dispatchers.IO) {
                db.doseLogDao().allLogs()   // we'll add this in Dao
            }

            // ---- group + count ----
            val todayStats = statsForRange(logs, startOfToday, endNow)
            val weekStats = statsForRange(logs, startOfWeek, endNow)
            val monthStats = statsForRange(logs, startOfMonth, endNow)

            // ---- update charts ----
            renderPie(binding.chartToday, todayStats, "Today")
            renderPie(binding.chartWeek, weekStats, "This week")
            renderPie(binding.chartMonth, monthStats, "This month")

            // ---- update status panel ----
            val percent = monthStats.percentTaken()   // we’ll score on month
            when {
                percent >= 90 -> {
                    binding.panelStatus.setBackgroundColor(Color.parseColor("#C8E6C9"))
                    binding.tvStatusTitle.text = "On track ✅"
                    binding.tvStatusMessage.text = "Amazing consistency! 💪 You’ve taken ${monthStats.taken}/${monthStats.total} doses this month. Keep up the great work! 💚"
                }
                percent >= 70 -> {
                    binding.panelStatus.setBackgroundColor(Color.parseColor("#FFF9C4"))
                    binding.tvStatusTitle.text = "Getting there ⚠️"
                    binding.tvStatusMessage.text = "You're improving — ${monthStats.taken}/${monthStats.total} doses taken. A little more focus and you'll hit your goal! 🌼"
                }
                else -> {
                    binding.panelStatus.setBackgroundColor(Color.parseColor("#FFCDD2"))
                    binding.tvStatusTitle.text = "Let’s get back on track ❤️"
                    binding.tvStatusMessage.text = "You’ve missed a few doses (${monthStats.missed}). Don’t worry — every day is a new chance to restart 🌅"
                }
            }

        }
    }

    // ---- helpers ----

    data class RangeStats(val taken: Int, val missed: Int) {
        val total: Int get() = taken + missed
        fun percentTaken(): Int = if (total == 0) 0 else ((taken * 100f) / total).toInt()
    }

    private fun statsForRange(
        logs: List<com.example.meditrack.data.DoseLog>,
        start: LocalDateTime,
        end: LocalDateTime
    ): RangeStats {
        var taken = 0
        var missed = 0

        logs.forEach { log ->
            if (!log.scheduledAt.isBefore(start) && !log.scheduledAt.isAfter(end)) {
                when (log.status) {
                    "TAKEN" -> taken++
                    // everything else we count as missed/skipped
                    else -> missed++
                }
            }
        }

        return RangeStats(taken, missed)
    }

    private fun renderPie(
        chart: com.github.mikephil.charting.charts.PieChart,
        stats: RangeStats,
        center: String
    ) {
        val entries = ArrayList<PieEntry>()
        if (stats.taken > 0) entries.add(PieEntry(stats.taken.toFloat(), "Taken"))
        if (stats.missed > 0) entries.add(PieEntry(stats.missed.toFloat(), "Missed"))
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No data"))
        }

        val set = PieDataSet(entries, "").apply {
            // colours: green, red, grey
            colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#F44336"),
                Color.parseColor("#BDBDBD")
            )
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        chart.setHoleColor(Color.WHITE)
        chart.setTransparentCircleAlpha(110)
        chart.setCenterTextColor(Color.DKGRAY)
        chart.setEntryLabelTextSize(11f)
        chart.animateY(800)   // smooth entry animation


        chart.data = PieData(set)
        chart.description.isEnabled = false
        chart.setUsePercentValues(true)
        chart.centerText = center
        chart.setEntryLabelColor(Color.BLACK)
        chart.legend.isEnabled = true
        chart.invalidate()
    }
}

