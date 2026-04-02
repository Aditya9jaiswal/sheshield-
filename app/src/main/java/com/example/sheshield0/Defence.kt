package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Defence : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge content
        enableEdgeToEdge()
        setContentView(R.layout.activity_defence)


         val cardLegal = findViewById<CardView>(R.id.cardLegal)

        val heartbeatAnim = AnimationUtils.loadAnimation(this, R.anim.button_heartbeat)

        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cardLegal.setOnClickListener {
            val intent = Intent(this, TechnicalActivity::class.java)
            startActivity(intent)
        }




        // Initialize CardViews
        val cardSelfDefence = findViewById<CardView>(R.id.cardSelfDefence)
        val cardYoga = findViewById<CardView>(R.id.cardYoga)

        cardSelfDefence.startAnimation(heartbeatAnim)
        cardYoga.startAnimation(heartbeatAnim)
        cardLegal.startAnimation(heartbeatAnim)
        cardSelfDefence
        // Set click listeners
        cardSelfDefence.setOnClickListener {
            startActivity(Intent(this, SelfDefenceActivity::class.java))
        }

        cardYoga.setOnClickListener {
            startActivity(Intent(this, YogaActivity::class.java))
        }

    }
}
