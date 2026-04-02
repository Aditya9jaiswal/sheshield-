package com.example.sheshield0

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelfDefenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_defence)

        val heartbeatAnim = AnimationUtils.loadAnimation(this, R.anim.button_heartbeat)


        // Updated video links (replacements for deleted videos)
        val delhiPolicePart1 = "https://www.youtube.com/watch?v=9m-x64bKfR4"        // original Part 1 (kept)
        val delhiPoliceAltPart2 = "https://www.youtube.com/watch?v=l2IqEATNybg"     // alternate Part 2 (Delhi Police upload). :contentReference[oaicite:3]{index=3}
        val drSeemaRaoTips = "https://www.youtube.com/watch?v=RMznVg4qo5E"         // Dr. Seema Rao practical tips. :contentReference[oaicite:4]{index=4}

        val btnPart1: Button = findViewById(R.id.btnDelhiPolicePart1)
        val btnPart2: Button = findViewById(R.id.btnDelhiPolicePart2)
        val btnWcd: Button = findViewById(R.id.btnWCDTraining)


        btnPart1.startAnimation(heartbeatAnim)
        btnPart2.startAnimation(heartbeatAnim)
        btnWcd.startAnimation(heartbeatAnim)

        btnPart1.setOnClickListener { openYoutubeVideo(delhiPolicePart1) }
        btnPart2.setOnClickListener { openYoutubeVideo(delhiPoliceAltPart2) }
        btnWcd.setOnClickListener { openYoutubeVideo(drSeemaRaoTips) }
    }

    private fun openYoutubeVideo(url: String) {
        // Try open with YouTube app first (intent will be handled by YouTube if available),
        // fallback to browser if not installed.
        val videoId = try {
            Uri.parse(url).getQueryParameter("v") ?: ""
        } catch (e: Exception) {
            ""
        }

        // Prefer vnd.youtube: scheme if possible for better UX
        if (videoId.isNotEmpty()) {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(appIntent)
            } catch (ex: ActivityNotFoundException) {
                startActivity(webIntent)
            }
        } else {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (ex: ActivityNotFoundException) {
                // nothing we can do — ignore or show toast in real app
            }
        }
    }
}
