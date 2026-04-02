package com.example.sheshield0

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.sheshield0.services.ShakeDetectionService
import androidx.core.content.edit

class EmergencyActivity : AppCompatActivity() {

    private lateinit var switchEmergency: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        switchEmergency = findViewById(R.id.switchEmergency)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isOn = prefs.getBoolean("emergency_mode", true)  // ✅ Default ON
        switchEmergency.isChecked = isOn

        // Start/Stop shake detection according to preference
        if (isOn) {
            startShakeDetection()
        }

        // Switch listener
        switchEmergency.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("emergency_mode", isChecked) }

            if (isChecked) {
                startShakeDetection()
                Toast.makeText(this, "Emergency Mode ON: Shake detection active", Toast.LENGTH_SHORT).show()
            } else {
                stopShakeDetection()
                Toast.makeText(this, "Emergency Mode OFF: Shake detection disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startShakeDetection() {
        val intent = Intent(this, ShakeDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopShakeDetection() {
        val intent = Intent(this, ShakeDetectionService::class.java)
        stopService(intent)
    }
}
