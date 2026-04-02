package com.example.sheshield0

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sheshield0.fragment.AboutFragment
import com.example.sheshield0.fragment.HelpFragment
import com.example.sheshield0.fragment.ProfileFragment
import com.example.sheshield0.services.ShakeDetectionService
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var btnSOS: ImageButton
    private lateinit var btnWomenHelpline: ImageButton
    private lateinit var btnAmbulance: ImageButton
    private lateinit var btnChildHelp: ImageButton
    private lateinit var btnVoiceActivation: Button
    private lateinit var btnStartEmergency: Button
    private lateinit var im1: ImageView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var scrollView: View
    private lateinit var fragmentContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        setupBottomNavigation()

        // Initially show home screen
        showHomeScreen()
    }


    private fun initializeViews() {
        btnSOS = findViewById(R.id.btnSOS)
        btnWomenHelpline = findViewById(R.id.btnWomenHelpline)
        btnAmbulance = findViewById(R.id.btnAmbulance)
        btnChildHelp = findViewById(R.id.btnChildHelp)
        btnVoiceActivation = findViewById(R.id.btnVoiceActivation)
        btnStartEmergency = findViewById(R.id.btnStartEmergency)
        im1 = findViewById(R.id.im1)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        scrollView = findViewById(R.id.scrollView)
        fragmentContainer = findViewById(R.id.fragment_container)
    }

    private fun setupClickListeners() {
        // Load scale animation
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.button_scale)
        val enter = AnimationUtils.loadAnimation(this, R.anim.button_enter)
        val heartbeatAnim = AnimationUtils.loadAnimation(this, R.anim.button_heartbeat)


        val intent = Intent(this, ShakeDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }


        btnSOS.startAnimation(heartbeatAnim)
        btnWomenHelpline.startAnimation(heartbeatAnim)
        btnAmbulance.startAnimation(heartbeatAnim)
        btnChildHelp.startAnimation(heartbeatAnim)
        im1.startAnimation(heartbeatAnim)

        btnSOS.isSoundEffectsEnabled  = true
        btnWomenHelpline.isSoundEffectsEnabled  = true
        btnAmbulance.isSoundEffectsEnabled  = true
        btnChildHelp.isSoundEffectsEnabled  = true
        im1.isSoundEffectsEnabled  = true

        btnSOS.setOnClickListener {
            it.startAnimation(enter)
            dialNumber("112")
        }
        btnWomenHelpline.setOnClickListener {
            it.startAnimation(scaleAnim)
            dialNumber("1091")
        }
        btnAmbulance.setOnClickListener {
            it.startAnimation(scaleAnim)
            dialNumber("102")
        }
        btnChildHelp.setOnClickListener {
            it.startAnimation(scaleAnim)
            dialNumber("1098")
        }

        btnVoiceActivation.setOnClickListener {
            it.startAnimation(scaleAnim)
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        im1.setOnClickListener {
            it.startAnimation(scaleAnim)
            startActivity(Intent(this, Defence::class.java))
        }

        btnStartEmergency.setOnClickListener {
            it.startAnimation(scaleAnim)
            startActivity(Intent(this, marge::class.java))
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeScreen()
                    true
                }
                R.id.nav_profile -> {
                    openFragment(ProfileFragment())
                    true
                }
                R.id.nav_help -> {
                    openFragment(HelpFragment())
                    true
                }
                R.id.nav_about -> {
                    openFragment(AboutFragment())
                    true
                }
                else -> false
            }
        }

        // Set home as default
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun openFragment(fragment: Fragment) {
        scrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showHomeScreen() {
        scrollView.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE

        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun dialNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }



    override fun onBackPressed() {
        if (fragmentContainer.visibility == View.VISIBLE) {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else {
                showHomeScreen()
                bottomNavigation.selectedItemId = R.id.nav_home
            }
        } else {
            super.onBackPressed()
        }
    }
}
