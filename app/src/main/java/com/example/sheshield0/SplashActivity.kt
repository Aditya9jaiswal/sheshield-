package com.example.sheshield0

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            val denied = permissionsMap.filter { !it.value }.keys
            if (denied.isNotEmpty()) {
                // If user denied, you can show a toast or re-request if necessary
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logoImageView = findViewById<ImageView>(R.id.logo)
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim)
        logoImageView.startAnimation(animation)

        checkAndRequestPermissions()

        lifecycleScope.launch {
            delay(2000)

            val currentUser = FirebaseAuth.getInstance().currentUser
            val prefs = getSharedPreferences("SheShieldPrefs", MODE_PRIVATE)

            if (currentUser != null) {
                val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users").child(currentUser.uid)
                userRef.child("mobile").get().addOnSuccessListener { mobileSnapshot ->
                    val mobile = mobileSnapshot.getValue(String::class.java) ?: ""
                    prefs.edit().apply {
                        putString("current_user_uid", currentUser.uid)
                        putString("current_user_email", currentUser.email)
                        putString("user_mobile", mobile)
                        apply()
                    }
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            } else {
                prefs.edit().apply {
                    clear()
                    apply()
                }
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (deniedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }
}
