package com.example.sheshield0

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sheshield0.model.TechnicalIssue

class SolutionActivity : AppCompatActivity() {

    private lateinit var tvNameEn: TextView
    private lateinit var tvNameHi: TextView
    private lateinit var tvDescEn: TextView
    private lateinit var tvDescHi: TextView
    private lateinit var tvSection: TextView
    private lateinit var tvSolutionEn: TextView
    private lateinit var tvSolutionHi: TextView
    private lateinit var btnCallHelpline: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solution)

        initializeViews()

        val heartbeatAnim = AnimationUtils.loadAnimation(this, R.anim.button_heartbeat)
        btnCallHelpline.startAnimation(heartbeatAnim)

        @Suppress("DEPRECATION")
        val issue = intent.getSerializableExtra("issue") as? TechnicalIssue
        issue?.let { displayIssueDetails(it) }
    }

    private fun initializeViews() {
        tvNameEn = findViewById(R.id.tvNameEn)
        tvNameHi = findViewById(R.id.tvNameHi)
        tvDescEn = findViewById(R.id.tvDescEn)
        tvDescHi = findViewById(R.id.tvDescHi)
        tvSection = findViewById(R.id.tvSection)
        tvSolutionEn = findViewById(R.id.tvSolutionEn)
        tvSolutionHi = findViewById(R.id.tvSolutionHi)
        btnCallHelpline = findViewById(R.id.btnCallHelpline)
    }

    private fun displayIssueDetails(issue: TechnicalIssue) {
        tvNameEn.text = issue.name_en
        tvNameHi.text = issue.name_hi
        tvDescEn.text = issue.desc_en
        tvDescHi.text = issue.desc_hi
        tvSection.text = issue.section
        tvSolutionEn.text = issue.solution_en
        tvSolutionHi.text = issue.solution_hi

        // Helpline Button (only if available)
        issue.helpline?.takeIf { it.isNotEmpty() }?.let { number ->
            btnCallHelpline.visibility = View.VISIBLE
            btnCallHelpline.text = "Call Helpline: $number"
            btnCallHelpline.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                startActivity(intent)
            }
        } ?: run {
            btnCallHelpline.visibility = View.GONE
        }
    }
}
