package com.doha.emsiquizapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash)

            // Délai de 2 secondes puis transition
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Log.e("SplashError", "Erreur lors du passage à MainActivity", e)
                }
            }, 2000)
        } catch (e: Exception) {
            Log.e("SplashError", "Erreur lors du setContentView", e)
        }
    }
}
