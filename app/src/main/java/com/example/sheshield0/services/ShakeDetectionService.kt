package com.example.sheshield0.services

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.sheshield0.CameraActivity
import com.example.sheshield0.EmergencyActivity
import com.example.sheshield0.marge
import kotlin.math.sqrt
import com.example.sheshield0.utils.VoiceTriggerHelper

class ShakeDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastShakeTime: Long = 0
    private var shakeStartTime: Long = 0
    private var isShaking = false
    private var shakeTriggered = false

    private val shakeThreshold = 15f
    private val shakeDuration = 1000L
    private val voiceListenDuration = 5000L // 5 seconds

    private var voiceHelper: VoiceTriggerHelper? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isListening = false

    private val keywords = listOf(
        "help me",
        "dar lag raha hai",
        "mummy papa dar lag raha hai"
    )

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        showToast("Shake detection started")
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        stopVoiceListening()
        showToast("Shake detection stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val emergencyMode = prefs.getBoolean("emergency_mode", true)
        if (!emergencyMode) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = sqrt((x*x + y*y + z*z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH
        val currentTime = System.currentTimeMillis()

        if (acceleration > shakeThreshold) {
            if (!isShaking) shakeStartTime = currentTime
            isShaking = true
            lastShakeTime = currentTime

            if (!shakeTriggered && currentTime - shakeStartTime >= shakeDuration) {
                shakeTriggered = true
                startVoiceKeywordListener()
            }

        } else if (isShaking && currentTime - lastShakeTime > 500) {
            isShaking = false
        }
    }


    private fun startVoiceKeywordListener() {
        showToast("Shake detected! Listening for keyword...")

        voiceHelper = VoiceTriggerHelper(this)
        isListening = true

        voiceHelper?.startListening { detectedText ->
            if (detectedText != null) {
                val textLowercase = detectedText.lowercase()
                val keywordDetected = keywords.any { keyword ->
                    textLowercase.contains(keyword.lowercase())
                }

                if (keywordDetected) {
                    showToast("Keyword detected! Starting emergency...")
                    startService(Intent(this, marge::class.java))
                    stopVoiceListening()
                }
            }
        }

        // Stop listening automatically after 5 seconds
        handler.postDelayed({
            if (isListening) {
                stopVoiceListening()
                showToast("No keyword detected, stopping listening")
            }
        }, voiceListenDuration)

        // Reset shake detection after 10 seconds
        handler.postDelayed({ shakeTriggered = false }, 10_000)
    }

    private fun stopVoiceListening() {
        isListening = false
        voiceHelper = null
        // Your VoiceTriggerHelper may need a stopListening() method if it has one
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
