package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sheshield0.firebase.FirebaseDbManager
import com.example.sheshield0.model.FamilyMember

class FamilyDetailsActivity : AppCompatActivity() {

    private lateinit var dbManager: FirebaseDbManager
    private lateinit var uid: String
    private lateinit var userMobile: String
    private lateinit var progressBar: ProgressBar
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_details)

        dbManager = FirebaseDbManager()

        // Get intent data safely
        uid = intent.getStringExtra("userId") ?: ""
        userMobile = intent.getStringExtra("userMobile") ?: ""

        Log.d("DEBUG", "UID: $uid , Mobile: $userMobile")

        if (uid.isEmpty() || userMobile.isEmpty()) {
            Toast.makeText(this, "Invalid user data!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Views
        val name1Et = findViewById<EditText>(R.id.etFamily1Name)
        val email1Et = findViewById<EditText>(R.id.etFamily1Email)
        val gender1Sp = findViewById<Spinner>(R.id.spFamily1Gender)
        val mobile1Et = findViewById<EditText>(R.id.etFamily1Mobile)

        val name2Et = findViewById<EditText>(R.id.etFamily2Name)
        val email2Et = findViewById<EditText>(R.id.etFamily2Email)
        val gender2Sp = findViewById<Spinner>(R.id.spFamily2Gender)
        val mobile2Et = findViewById<EditText>(R.id.etFamily2Mobile)

        submitBtn = findViewById(R.id.btnSubmitFamily)
        progressBar = findViewById(R.id.progressBar)

        // Spinner setup
        val genderOptions = arrayOf("Select Gender", "Female", "Male", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        gender1Sp.adapter = adapter
        gender2Sp.adapter = adapter

        submitBtn.setOnClickListener {

            val family1 = FamilyMember(
                name = name1Et.text.toString().trim(),
                email = email1Et.text.toString().trim(),
                relation = gender1Sp.selectedItem.toString().takeIf { it != "Select Gender" } ?: "",
                mobile = formatMobile(mobile1Et.text.toString().trim())
            )

            val family2 = FamilyMember(
                name = name2Et.text.toString().trim(),
                email = email2Et.text.toString().trim(),
                relation = gender2Sp.selectedItem.toString().takeIf { it != "Select Gender" } ?: "",
                mobile = formatMobile(mobile2Et.text.toString().trim())
            )

            if (!validateFamily(family1, family2)) return@setOnClickListener

            progressBar.visibility = View.VISIBLE
            submitBtn.isEnabled = false

            dbManager.saveFamily(uid, listOf(family1, family2)) { success ->

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    submitBtn.isEnabled = true

                    if (success) {
                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, OtpVerificationActivity::class.java)
                        intent.putExtra("userId", uid)
                        intent.putExtra("userMobile", userMobile)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateFamily(f1: FamilyMember, f2: FamilyMember): Boolean {

        if (f1.name.isEmpty() || f1.email.isEmpty() || f1.mobile.isEmpty() || f1.relation.isEmpty() ||
            f2.name.isEmpty() || f2.email.isEmpty() || f2.mobile.isEmpty() || f2.relation.isEmpty()
        ) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(f1.email).matches() ||
            !Patterns.EMAIL_ADDRESS.matcher(f2.email).matches()
        ) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidMobile(f1.mobile) || !isValidMobile(f2.mobile)) {
            Toast.makeText(this, "Invalid Mobile Number", Toast.LENGTH_SHORT).show()
            return false
        }

        val f1Num = f1.mobile.removePrefix("+91")
        val f2Num = f2.mobile.removePrefix("+91")

        if (f1Num == userMobile || f2Num == userMobile) {
            Toast.makeText(this, "Cannot use your own number", Toast.LENGTH_SHORT).show()
            return false
        }

        if (f1.mobile == f2.mobile) {
            Toast.makeText(this, "Both numbers cannot be same", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isValidMobile(mobile: String): Boolean {
        val clean = mobile.removePrefix("+91")
        return clean.matches(Regex("^[6-9]\\d{9}$"))
    }

    private fun formatMobile(mobile: String): String {
        return if (mobile.startsWith("+91")) mobile else "+91$mobile"
    }
}