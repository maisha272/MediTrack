package com.example.meditrack.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.meditrack.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // (optional) small fade animation
        binding.imgLogo.alpha = 0f
        binding.imgLogo.animate().alpha(1f).setDuration(700).start()

        // go to main list after 1.3s
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MedListActivity::class.java))
            finish() // so user can’t go back to splash
        }, 1300)
    }
}
