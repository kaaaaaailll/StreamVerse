package com.example.streamverse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.IV_SplashLogo)
        val title = findViewById<TextView>(R.id.TV_SplashTitle)
        val subtitle = findViewById<TextView>(R.id.TV_SplashSubtitle)

        // Fade in logo
        logo.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        // Fade in title after logo
        title.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .start()

        // Fade in subtitle then go to MainActivity
        subtitle.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(700)
            .withEndAction {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .start()
    }
}