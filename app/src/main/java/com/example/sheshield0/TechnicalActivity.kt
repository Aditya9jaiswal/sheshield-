package com.example.sheshield0

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sheshield0.adapter.TechnicalAdapter
import com.example.sheshield0.model.TechnicalIssue
import com.example.sheshield0.utils.JsonUtils
import com.example.sheshield0.utils.similarity
import java.util.*

class TechnicalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TechnicalAdapter
    private lateinit var issuesList: List<TechnicalIssue>

    private lateinit var etProblem: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnMic: ImageButton

    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technical)

        recyclerView = findViewById(R.id.rvTechnicalIssues)
        etProblem = findViewById(R.id.etProblem)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnMic = findViewById(R.id.btnMic)

        issuesList = JsonUtils.loadTechnicalIssuesFromJson(this)
        adapter = TechnicalAdapter(issuesList) { openSolutionActivity(it) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSubmit.setOnClickListener {
            handleProblem(etProblem.text.toString())
        }

        btnMic.setOnClickListener {
            startVoiceInput()
        }

        checkMicPermission()
    }

    // 🎤 MIC LISTEN
    private fun startVoiceInput() {
        Toast.makeText(this, "Listening… Speak your problem", Toast.LENGTH_SHORT).show()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        }

        speechRecognizer.setRecognitionListener(object :
            android.speech.RecognitionListener {

            override fun onResults(results: Bundle?) {
                val text =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                if (!text.isNullOrEmpty()) {
                    etProblem.setText(text)
                    handleProblem(text)
                }
            }

            override fun onError(error: Int) {
                Toast.makeText(this@TechnicalActivity, "Try again", Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    // 🧠 ASSISTANT LOGIC
    private fun handleProblem(input: String) {
        if (input.isBlank()) {
            Toast.makeText(this, "Please describe your problem", Toast.LENGTH_SHORT).show()
            return
        }

        val matchedIssue = findBestMatch(input)
        if (matchedIssue != null) {
            openSolutionActivity(matchedIssue)
        } else {
            Toast.makeText(
                this,
                "Sorry, I couldn't find an exact solution. Try different words.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun findBestMatch(userInput: String): TechnicalIssue? {
        val input = userInput.lowercase(Locale.getDefault())

        issuesList.find { issue ->
            issue.keywords.any { input.contains(it.lowercase()) } ||
                    issue.name_en.lowercase().contains(input) ||
                    issue.name_hi.contains(input)
        }?.let { return it }

        issuesList.find { issue ->
            issue.keywords.any { input.similarity(it.lowercase()) > 0.7 }
        }?.let { return it }

        return findCyberSecurityMatch(input)
    }

    private fun findCyberSecurityMatch(input: String): TechnicalIssue? {
        val map = mapOf(
            listOf("upi", "payment", "bank", "money") to 101,
            listOf("otp", "password", "login", "hack") to 102,
            listOf("unauthorized", "breach", "access") to 103
        )

        map.forEach { (keys, id) ->
            if (keys.any { input.contains(it) }) {
                return issuesList.find { it.id == id }
            }
        }
        return null
    }

    private fun openSolutionActivity(issue: TechnicalIssue) {
        startActivity(Intent(this, SolutionActivity::class.java).apply {
            putExtra("issue", issue)
        })
    }

    // 🔐 MIC PERMISSION
    private fun checkMicPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}
