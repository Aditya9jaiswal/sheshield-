package com.example.sheshield0.services

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.speech.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.sheshield0.R
import com.example.sheshield0.utils.CameraUtils
import com.example.sheshield0.utils.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class EmergencyService : Service() {

    private val TAG = "EmergencyService"
    private val CHANNEL_ID = "emergency_channel"

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var locationHelper: LocationHelper

    private var latitude = 0.0
    private var longitude = 0.0
    private var videoFile: File? = null

    private val keywords = listOf("help", "help me", "bachao", "bacha lo")

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, notification("🎤 Listening…"))
        startVoiceListening()
    }

    // 🎤 MIC ON
    private fun startVoiceListening() {
        if (!hasMicPermission()) {
            startEmergencyFlow()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {
                val list =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                list?.forEach {
                    val text = it.lowercase(Locale.getDefault())
                    if (keywords.any { key -> text.contains(key) }) {
                        Log.d(TAG, "Keyword matched: $text")
                        speechRecognizer.destroy()
                        startEmergencyFlow()
                        return
                    }
                }
            }

            override fun onError(error: Int) {
                startEmergencyFlow()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }

        speechRecognizer.startListening(intent)
    }

    // 🔥 AFTER KEYWORD
    private fun startEmergencyFlow() {
        updateNotification("🎥 Recording video…")

        // 1️⃣ Start live location (SAME AS marge)
        startLiveLocation()

        // 2️⃣ Record video
        CameraUtils.startCameraRecording(this, 7000) { file ->
            videoFile = file
            uploadToFirebase()
        }
    }

    // 📍 LIVE LOCATION (SAME LOGIC AS marge)
    private fun startLiveLocation() {
        locationHelper = LocationHelper(this)
        locationHelper.startLocationUpdates(object :
            LocationHelper.LocationListener {

            override fun onLocationChanged(location: Location) {
                latitude = location.latitude
                longitude = location.longitude
                Log.d(TAG, "Live location: $latitude , $longitude")
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "Location error: ${error.message}")
            }
        })
    }

    // ☁️ FIREBASE UPLOAD
    private fun uploadToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val file = videoFile ?: return

        updateNotification("☁️ Uploading data…")

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("emergency_videos/$userId/${System.currentTimeMillis()}.mp4")

        storageRef.putFile(android.net.Uri.fromFile(file))
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    saveToDatabase(userId, url.toString())
                }
            }
    }

    // 💾 SAVE TO DATABASE
    private fun saveToDatabase(userId: String, videoUrl: String) {
        val data = mapOf(
            "videoUrl" to videoUrl,
            "latitude" to latitude,
            "longitude" to longitude,
            "time" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance()
            .getReference("emergencies")
            .child(userId)
            .push()
            .setValue(data)

        updateNotification("✅ Emergency uploaded")
        stopSelf()
    }

    // 🔔 HELPERS
    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun notification(msg: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SheShield Emergency")
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_emergency)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

    private fun updateNotification(msg: String) {
        getSystemService(NotificationManager::class.java)
            .notify(1, notification(msg))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Emergency",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        locationHelper.cleanup()
        speechRecognizer.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
