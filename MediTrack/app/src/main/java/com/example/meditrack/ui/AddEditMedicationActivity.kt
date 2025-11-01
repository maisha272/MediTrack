package com.example.meditrack.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.meditrack.R
import com.example.meditrack.alarm.DoseScheduler
import com.example.meditrack.data.*
import com.example.meditrack.databinding.ActivityAddEditMedicationBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AddEditMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMedicationBinding
    private val times = mutableListOf<LocalTime>()

    private val db by lazy { MedDatabase.get(application) }
    private val scheduler by lazy { DoseScheduler(application, db) }
    private val repo by lazy { MedRepository(db, scheduler) }

    private var startDate: LocalDate = LocalDate.now()
    private var endDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // After binding setup
        val medId = intent.getLongExtra("MED_ID", -1L)
        if (medId != -1L) {
            lifecycleScope.launch {
                val med = db.medicationDao().get(medId)
                val rule = db.scheduleRuleDao().getForMed(medId)
                val times = db.doseTimeDao().listForMed(medId)

                if (med != null) {
                    binding.edtName.setText(med.name)
                    binding.edtDosage.setText(med.dosage)
                    binding.edtForm.setText(med.form, false)
                    binding.edtNotes.setText(med.notes)
                    binding.edtStart.setText(med.startDate.toString())
                    med.endDate?.let { binding.edtEnd.setText(it.toString()) }
                    if (med.endDate == null) binding.switchIndefinite.isChecked = true

                    // Set repeat type
                    when (rule?.type) {
                        "DAILY" -> binding.rbDaily.isChecked = true
                        "EVERY_N_DAYS" -> {
                            binding.rbEveryN.isChecked = true
                            binding.edtEveryN.setText(rule.intervalDays?.toString() ?: "1")
                        }
                        "DOW" -> {
                            binding.rbDow.isChecked = true
                            val map = mapOf(
                                "MON" to binding.chMon,
                                "TUE" to binding.chTue,
                                "WED" to binding.chWed,
                                "THU" to binding.chThu,
                                "FRI" to binding.chFri,
                                "SAT" to binding.chSat,
                                "SUN" to binding.chSun,
                            )
                            rule.daysOfWeek?.split(",")?.forEach { code ->
                                map[code.trim()]?.isChecked = true
                            }
                        }
                    }

                    // Time chips
                    times.forEach { addTimeChip(it.timeOfDay) }

                    // Keep ID for update
                    binding.btnSave.text = getString(R.string.update)
                    binding.btnArchive.visibility = android.view.View.VISIBLE
                    binding.btnArchive.setOnClickListener {
                        archiveMedication(med.id)
                    }
                }
            }
        }


        // Form dropdown
        val forms = listOf("pill", "liquid", "injection")
        binding.edtForm.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, forms))
        binding.edtForm.setText(forms.first(), false)

        // Dates
        binding.edtStart.setText(startDate.toString())
        binding.edtStart.setOnClickListener { pickDate(true) }

        binding.edtEnd.setOnClickListener { pickDate(false) }
        binding.switchIndefinite.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                endDate = null
                binding.edtEnd.setText("")
                binding.edtEnd.isEnabled = false
            } else {
                binding.edtEnd.isEnabled = true
            }
        }

        // Repeat visibility toggles
        binding.radioRepeatGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbDaily -> {
                    binding.tilEveryN.visibility = android.view.View.GONE
                    binding.chipsDow.visibility = android.view.View.GONE
                }
                R.id.rbEveryN -> {
                    binding.tilEveryN.visibility = android.view.View.VISIBLE
                    binding.chipsDow.visibility = android.view.View.GONE
                }
                R.id.rbDow -> {
                    binding.tilEveryN.visibility = android.view.View.GONE
                    binding.chipsDow.visibility = android.view.View.VISIBLE
                }
            }
        }

        // Time add
        binding.btnAddTime.setOnClickListener { pickTime() }

        // Save
        binding.btnSave.setOnClickListener { save()


        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun pickDate(isStart: Boolean) {
        val cur = if (isStart) startDate else endDate ?: LocalDate.now()
        val dlg = DatePickerDialog(
            this,
            { _, y, m, d ->
                val picked = LocalDate.of(y, m + 1, d)
                if (isStart) {
                    startDate = picked
                    binding.edtStart.setText(picked.toString())
                } else {
                    endDate = picked
                    binding.edtEnd.setText(picked.toString())
                }
            },
            cur.year, cur.monthValue - 1, cur.dayOfMonth
        )
        dlg.show()
    }

    private fun pickTime() {
        val now = LocalTime.now()
        TimePickerDialog(this, { _, h, m ->
            val t = LocalTime.of(h, m).withSecond(0).withNano(0)
            if (!times.contains(t)) {
                times += t
                addTimeChip(t)
            }
        }, now.hour, now.minute, true).show()
    }

    private fun addTimeChip(t: LocalTime) {
        val chip = Chip(this).apply {
            text = t.toString()
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipsTimes.removeView(this)
                times.remove(t)
            }
        }
        binding.chipsTimes.addView(chip)
    }

    private fun validate(): Boolean {
        if (binding.edtName.text.isNullOrBlank()) {
            binding.edtName.error = getString(R.string.invalid_name); return false
        }
        if (times.isEmpty()) {
            Toast.makeText(this, R.string.invalid_time, Toast.LENGTH_SHORT).show(); return false
        }
        if (binding.rbEveryN.isChecked) {
            val n = binding.edtEveryN.text?.toString()?.toIntOrNull() ?: 0
            if (n < 1) {
                binding.edtEveryN.error = getString(R.string.invalid_every_n); return false
            }
        }
        if (binding.rbDow.isChecked) {
            val selected = selectedDow()
            if (selected.isEmpty()) {
                Toast.makeText(this, R.string.invalid_dow, Toast.LENGTH_SHORT).show(); return false
            }
        }
        return true
    }

    private fun selectedDow(): List<String> {
        val map = mapOf(
            R.id.chMon to "MON",
            R.id.chTue to "TUE",
            R.id.chWed to "WED",
            R.id.chThu to "THU",
            R.id.chFri to "FRI",
            R.id.chSat to "SAT",
            R.id.chSun to "SUN",
        )
        val list = mutableListOf<String>()
        binding.chipsDow.children.forEach { v ->
            val chip = v as Chip
            if (chip.isChecked) list += map[chip.id]!!
        }
        return list
    }

    private fun buildRule(medId: Long = 0L): ScheduleRule {
        return when {
            binding.rbDaily.isChecked -> ScheduleRule(medId = medId, type = "DAILY")
            binding.rbEveryN.isChecked -> {
                val n = binding.edtEveryN.text?.toString()?.toIntOrNull() ?: 1
                ScheduleRule(medId = medId, type = "EVERY_N_DAYS", intervalDays = n)
            }
            else -> {
                val dow = selectedDow().joinToString(",")
                ScheduleRule(medId = medId, type = "DOW", daysOfWeek = dow)
            }
        }
    }

    private fun save() {
        if (!validate()) return

        val med = Medication(
            name = binding.edtName.text.toString().trim(),
            dosage = binding.edtDosage.text.toString().trim(),
            form = binding.edtForm.text?.toString()?.trim().orEmpty(),
            notes = binding.edtNotes.text?.toString(),
            startDate = startDate,
            endDate = if (binding.switchIndefinite.isChecked) null else endDate
        )
        val rule = buildRule()

        lifecycleScope.launch {
            // Save the medication and receive its ID
            val id = repo.upsertMedicationWithSchedule(med, times.sorted(), rule)

            //  Schedule reminders immediately
            scheduler.rescheduleForMedication(id)

            Toast.makeText(
                this@AddEditMedicationActivity,
                "Medication saved & reminders scheduled",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
    private fun archiveMedication(id: Long) {
        lifecycleScope.launch {
            db.medicationDao().archive(id)
            scheduler.cancelForMedication(id)
            Toast.makeText(this@AddEditMedicationActivity, "Medication archived", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


}
