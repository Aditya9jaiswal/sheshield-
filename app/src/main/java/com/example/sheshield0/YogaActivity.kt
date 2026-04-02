package com.example.sheshield0

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class YogaActivity : AppCompatActivity() {

    private lateinit var imgStep: ImageView
    private lateinit var tvStepName: TextView
    private lateinit var tvStepDesc: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnStart: Button
    private lateinit var btnEnd: Button

    private var stepIndex = 0
    private lateinit var timer: CountDownTimer
    private lateinit var mediaPlayer: MediaPlayer
    private val stepTime = 15_000L // 15 seconds per step

    data class Step(val image: Int, val name: String, val desc: String)

    private val steps = listOf(
        Step(R.drawable.sr1, "Pranamasana", "Stand straight with hands in prayer position. / सीधा खड़े होकर हाथ जोड़ें।"),
        Step(R.drawable.sr2, "Hastauttanasana", "Stretch arms up and back. / हाथ ऊपर उठाकर पीछे झुकें।"),
        Step(R.drawable.sr3, "Hastapadasana", "Bend forward touching your feet. / कमर से झुककर हाथ पैरों को छूएँ।"),
        Step(R.drawable.sr4, "Ashwa Sanchalanasana Right", "Right leg back, left leg forward. / दायां पैर पीछे, बायां पैर आगे।"),
        Step(R.drawable.sr5, "Dandasana", "Plank position. / प्लैंक पोज़।"),
        Step(R.drawable.sr6, "Ashtanga Namaskara", "Knees, chest and chin touch the floor. / घुटने, छाती और ठोड़ी जमीन को छूए।"),
        Step(R.drawable.sr7, "Bhujangasana", "Raise chest in cobra pose. / कोबरा पोज़ में छाती ऊपर उठाएँ।"),
        Step(R.drawable.sr8, "Adhomukh Shwanasana", "Downward dog pose. / उल्टा 'V' आकार में शरीर रखें।"),
        Step(R.drawable.sr4, "Ashwa Sanchalanasana Left", "Left leg back, right leg forward. / बायां पैर पीछे, दायां पैर आगे।"),
        Step(R.drawable.sr3, "Hastapadasana", "Bend forward touching your feet. / कमर से झुकें।"),
        Step(R.drawable.sr2, "Hastauttanasana", "Stretch arms up and back. / हाथ ऊपर उठाएँ।"),
        Step(R.drawable.sr1, "Pranamasana", "Return to prayer position. / हाथ जोड़कर प्रारंभिक मुद्रा में आएँ।")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)

        // Bind views
        imgStep = findViewById(R.id.imgStep)
        tvStepName = findViewById(R.id.tvStepName)
        tvStepDesc = findViewById(R.id.tvStepDesc)
        tvTimer = findViewById(R.id.tvTimer)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnStart = findViewById(R.id.btnStart)
        btnEnd = findViewById(R.id.btnEnd)

        showStep()

        btnStart.setOnClickListener {
            // Start OM sound
            mediaPlayer = MediaPlayer.create(this, R.raw.om_sound)
            mediaPlayer.isLooping = true // loop until session ends
            mediaPlayer.start()

            startTimer()
        }

        btnNext.setOnClickListener { nextStep() }
        btnPrev.setOnClickListener { prevStep() }
        btnEnd.setOnClickListener { finish() }
    }

    private fun showStep() {
        val step = steps[stepIndex]
        imgStep.setImageResource(step.image)
        tvStepName.text = step.name
        tvStepDesc.text = step.desc
        tvTimer.text = "00:00"
    }

    private fun startTimer() {
        if (this::timer.isInitialized) timer.cancel() // Cancel previous timer if running
        timer = object : CountDownTimer(stepTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = String.format("00:%02d", seconds)
            }

            override fun onFinish() {
                nextStep() // Automatic move to next step
            }
        }.start()
    }

    private fun nextStep() {
        if (this::timer.isInitialized) timer.cancel()
        if (stepIndex < steps.size - 1) {
            stepIndex++
            showStep()
            startTimer()
        } else {
            tvTimer.text = "Completed!"
            stopOmSound()
        }
    }

    private fun prevStep() {
        if (this::timer.isInitialized) timer.cancel()
        if (stepIndex > 0) {
            stepIndex--
            showStep()
            startTimer()
        }
    }

    private fun stopOmSound() {
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::timer.isInitialized) timer.cancel()
        stopOmSound()
    }
}
