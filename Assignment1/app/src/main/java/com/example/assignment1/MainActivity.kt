package com.example.assignment1

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GymApp"
        private const val KEY_SCORE = "score"
        private const val KEY_ELEMENT = "element"
        private const val KEY_DEDUCT = "deductTaken"
        private const val KEY_COMPLETE = "complete"
        private const val MAX_SCORE = 20
        private const val TOTAL_ELEMENTS = 10
    }

    private lateinit var tvTitle: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvElement: TextView
    private lateinit var btnPerform: Button
    private lateinit var btnDeduct: Button
    private lateinit var btnReset: Button

    private var score = 0                 // 0..20
    private var elementsDone = 0          // 0..10
    private var deductionTaken = false    // once true, performing is locked
    private var routineComplete = false   // becomes true at 10 elements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTitle = findViewById(R.id.tvTitle)
        tvScore = findViewById(R.id.tvScore)
        tvElement = findViewById(R.id.tvElement)
        btnPerform = findViewById(R.id.btnPerform)
        btnDeduct = findViewById(R.id.btnDeduct)
        btnReset = findViewById(R.id.btnReset)

        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(KEY_SCORE, 0)
            elementsDone = savedInstanceState.getInt(KEY_ELEMENT, 0)
            deductionTaken = savedInstanceState.getBoolean(KEY_DEDUCT, false)
            routineComplete = savedInstanceState.getBoolean(KEY_COMPLETE, false)
            Log.d(TAG, "restore: score=$score elements=$elementsDone " +
                    "deduct=$deductionTaken complete=$routineComplete")
        }

        btnPerform.setOnClickListener { onPerform() }
        btnDeduct.setOnClickListener { onDeduct() }
        btnReset.setOnClickListener { onReset() }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_ELEMENT, elementsDone)
        outState.putBoolean(KEY_DEDUCT, deductionTaken)
        outState.putBoolean(KEY_COMPLETE, routineComplete)
        Log.d(TAG, "save")
    }

    private fun onPerform() {
        Log.d(TAG, "Perform clicked")
        if (routineComplete) {
            Toast.makeText(this, getString(R.string.msg_after_complete), Toast.LENGTH_SHORT).show()
            return
        }
        if (deductionTaken) {
            Toast.makeText(this, getString(R.string.msg_after_deduction), Toast.LENGTH_SHORT).show()
            return
        }
        if (elementsDone >= TOTAL_ELEMENTS) {
            routineComplete = true
            updateUI()
            return
        }

        val nextElement = elementsDone + 1
        val pts = pointsForElement(nextElement)
        score = (score + pts).coerceAtMost(MAX_SCORE)
        elementsDone++

        Log.d(TAG, "performed element=$nextElement (+$pts) -> score=$score")

        if (elementsDone == TOTAL_ELEMENTS) {
            routineComplete = true
            Toast.makeText(this, getString(R.string.msg_completed_toast), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Routine complete")
        }

        updateUI()
    }

    private fun onDeduct() {
        Log.d(TAG, "Deduct clicked")
        if (elementsDone == 0) {
            Toast.makeText(this, getString(R.string.msg_not_started), Toast.LENGTH_SHORT).show()
            return
        }
        if (routineComplete) {
            Toast.makeText(this, getString(R.string.msg_after_complete), Toast.LENGTH_SHORT).show()
            return
        }

        if (!deductionTaken) {
            deductionTaken = true       // lock further performing
            Log.d(TAG, "first deduction -> performing locked")
        }

        score = (score - 2).coerceAtLeast(0)
        Log.d(TAG, "score after deduction=$score")
        updateUI()
    }

    private fun onReset() {
        Log.d(TAG, "Reset clicked")
        score = 0
        elementsDone = 0
        deductionTaken = false
        routineComplete = false
        updateUI()
    }

    private fun updateUI() {
        tvScore.text = getString(R.string.score_label, score)
        tvElement.text = getString(R.string.element_label, elementsDone, TOTAL_ELEMENTS)

        // Colour by zone determined from the next element
        val next = (elementsDone + 1).coerceAtMost(TOTAL_ELEMENTS)
        val colorRes = when (zoneFor(next)) {
            Zone.BASIC -> R.color.zone_basic
            Zone.INTERMEDIATE -> R.color.zone_intermediate
            Zone.ADVANCED -> R.color.zone_advanced
        }
        tvScore.setTextColor(ContextCompat.getColor(this, colorRes))

        btnPerform.isEnabled = !routineComplete && !deductionTaken && elementsDone < TOTAL_ELEMENTS
        btnDeduct.isEnabled = !routineComplete && elementsDone >= 1
        btnReset.isEnabled = true
    }

    private fun pointsForElement(oneBased: Int): Int = when (oneBased) {
        in 1..3 -> 1
        in 4..7 -> 2
        in 8..10 -> 3
        else -> 0
    }

    private fun zoneFor(oneBased: Int): Zone = when (oneBased) {
        in 1..3 -> Zone.BASIC
        in 4..7 -> Zone.INTERMEDIATE
        else -> Zone.ADVANCED
    }

    private enum class Zone { BASIC, INTERMEDIATE, ADVANCED }
}
