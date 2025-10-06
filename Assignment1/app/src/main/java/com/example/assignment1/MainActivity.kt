package com.example.assignment1

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

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

    private lateinit var tvScore: TextView
    private lateinit var tvElement: TextView
    private lateinit var btnPerform: Button
    private lateinit var btnDeduct: Button
    private lateinit var btnReset: Button
    private lateinit var progress: ProgressBar
    private lateinit var celebrateView: TextView
    private lateinit var root: View

    private var score = 0
    private var elementsDone = 0
    private var deductionTaken = false
    private var routineComplete = false

    private var lastState: Triple<Int, Int, Boolean>? = null // for UNDO
    private var toneGen: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar for menu (theme is NoActionBar)
        findViewById<MaterialToolbar>(R.id.topAppBar)?.let { setSupportActionBar(it) }

        root = findViewById(android.R.id.content)
        tvScore = findViewById(R.id.tvScore)
        tvElement = findViewById(R.id.tvElement)
        btnPerform = findViewById(R.id.btnPerform)
        btnDeduct = findViewById(R.id.btnDeduct)
        btnReset = findViewById(R.id.btnReset)
        progress = findViewById(R.id.progress)
        celebrateView = findViewById(R.id.celebrateView)

        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(KEY_SCORE, 0)
            elementsDone = savedInstanceState.getInt(KEY_ELEMENT, 0)
            deductionTaken = savedInstanceState.getBoolean(KEY_DEDUCT, false)
            routineComplete = savedInstanceState.getBoolean(KEY_COMPLETE, false)
            Log.d(TAG, "restore s=$score e=$elementsDone d=$deductionTaken c=$routineComplete")
        }

        btnPerform.setOnClickListener { onPerform() }
        btnDeduct.setOnClickListener { onDeduct() }
        btnReset.setOnClickListener { confirmReset() }

        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGen?.release()
        toneGen = null
    }

    // ---------------- Menu (Share + Language Switch) ----------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_howto -> { showHowToDialog(); true }
            R.id.action_share -> { shareSummary(); true }
            R.id.lang_en -> { setAppLanguage("en"); true }
            R.id.lang_bn -> { setAppLanguage("bn"); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showHowToDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.howto_title)
            .setMessage(R.string.howto_body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }


    private fun setAppLanguage(tag: String) {
        val appLocale = LocaleListCompat.forLanguageTags(tag)

        Log.i(TAG, "Language switched to $tag")

        AppCompatDelegate.setApplicationLocales(appLocale)
        Snackbar.make(root, getString(R.string.lang_switched), Snackbar.LENGTH_SHORT).show()
    }

    // ---------------- Core Actions ----------------
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
        val nextElement = elementsDone + 1
        if (nextElement > TOTAL_ELEMENTS) return

        val pts = pointsForElement(nextElement)
        score = (score + pts).coerceAtMost(MAX_SCORE)
        elementsDone++

        // Feedback: tone + haptic + score animation
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        tvScore.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        animateScoreChange()

        Log.d(TAG, "performed element=$nextElement (+$pts) -> score=$score")

        if (elementsDone == TOTAL_ELEMENTS) {
            routineComplete = true
            Log.i(TAG, "Routine complete (score=$score, elements=$elementsDone)")
            Toast.makeText(this, getString(R.string.msg_completed_toast), Toast.LENGTH_SHORT).show()
            if (score == MAX_SCORE) showCelebration()
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
            deductionTaken = true // first deduction locks performing
        }
        score = (score - 2).coerceAtLeast(0)

        tvScore.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        animateScoreChange()

        Log.d(TAG, "deducted -> score=$score")
        updateUI()
    }

    private fun confirmReset() {
        if (elementsDone == 0 && score == 0) { onReset(); return }
        AlertDialog.Builder(this)
            .setTitle(R.string.reset)
            .setMessage(R.string.confirm_reset)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Log.i(TAG, "Reset confirmed by user")
                lastState = Triple(score, elementsDone, deductionTaken)
                onReset()
                Snackbar.make(root, R.string.reset_done, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        lastState?.let { (s, e, d) ->
                            score = s; elementsDone = e; deductionTaken = d
                            routineComplete = (e == TOTAL_ELEMENTS)
                            updateUI()
                        }
                    }.show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun onReset() {
        score = 0
        elementsDone = 0
        deductionTaken = false
        routineComplete = false
        updateUI()
    }

    // ---------------- UI helpers ----------------
    private fun updateUI() {
        tvScore.text = getString(R.string.score_label, score)
        tvElement.text = getString(R.string.element_label, elementsDone, TOTAL_ELEMENTS)

        // Accessibility: up-to-date descriptions
        tvScore.contentDescription = getString(R.string.cd_score_full, score, MAX_SCORE)
        tvElement.contentDescription = getString(R.string.cd_element_full, elementsDone, TOTAL_ELEMENTS)

        // Zone colour & progress tint
        val next = (elementsDone + 1).coerceAtMost(TOTAL_ELEMENTS)
        val colorRes = when (zoneFor(next)) {
            Zone.BASIC -> R.color.zone_basic
            Zone.INTERMEDIATE -> R.color.zone_intermediate
            Zone.ADVANCED -> R.color.zone_advanced
        }
        tvScore.setTextColor(ContextCompat.getColor(this, colorRes))
        progress.progress = elementsDone
        progress.progressTintList = ContextCompat.getColorStateList(this, colorRes)

        // Buttons
        btnPerform.isEnabled = !routineComplete && !deductionTaken && elementsDone < TOTAL_ELEMENTS
        btnDeduct.isEnabled = !routineComplete && elementsDone >= 1
        btnReset.isEnabled = true

        // Hide celebration unless perfect score
        if (score < MAX_SCORE) celebrateView.visibility = View.GONE
    }

    private fun animateScoreChange() {
        tvScore.animate().cancel()
        tvScore.scaleX = 1f; tvScore.scaleY = 1f
        tvScore.animate()
            .scaleX(1.12f).scaleY(1.12f).setDuration(120)
            .withEndAction {
                tvScore.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
    }

    private fun showCelebration() {
        celebrateView.text = getString(R.string.celebrate_text)
        celebrateView.alpha = 0f
        celebrateView.scaleX = 0.8f
        celebrateView.scaleY = 0.8f
        celebrateView.visibility = View.VISIBLE
        celebrateView.animate()
            .alpha(1f).scaleX(1.2f).scaleY(1.2f).setDuration(300)
            .withEndAction {
                toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                tvScore.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                celebrateView.animate()
                    .alpha(0f).setStartDelay(1200).setDuration(450)
                    .withEndAction { celebrateView.visibility = View.GONE }
                    .start()
            }.start()
    }

    private fun shareSummary() {
        Log.i(TAG, "Share invoked (score=$score, elements=$elementsDone)")
        val msg = getString(R.string.share_text, score, elementsDone, TOTAL_ELEMENTS)
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }
        startActivity(Intent.createChooser(i, getString(R.string.share_via)))
    }

    // ---------------- Helpers ----------------
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_ELEMENT, elementsDone)
        outState.putBoolean(KEY_DEDUCT, deductionTaken)
        outState.putBoolean(KEY_COMPLETE, routineComplete)
    }
}



