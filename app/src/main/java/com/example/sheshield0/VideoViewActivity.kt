package com.example.sheshield0

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        val videoView = findViewById<VideoView>(R.id.videoView)

        val videoUriString = intent.getStringExtra("videoUri")
        val videoUri = Uri.parse(videoUriString)

        videoView.setVideoURI(videoUri)
        videoView.setMediaController(MediaController(this))
        videoView.requestFocus()
        videoView.start()
    }
}
