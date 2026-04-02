package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class ForgotPasswordActivity: AppCompatActivity() {

    private lateinit var mobileInput: EditText
    private lateinit var sendOtpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mobileInput = findViewById(R.id.mobileInput)
        sendOtpBtn = findViewById(R.id.sendOtpBtn)

        sendOtpBtn.setOnClickListener {
            var mobile = mobileInput.text.toString().trim()
            if (mobile.isEmpty()) {
                Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!mobile.startsWith("+91")) mobile = "+91$mobile"
            val uid = mobile.replace("+","")

            // Check user exists in Realtime Database
            FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // User exists → open OTP Verification for password reset
                        val intent = Intent(this, OtpVerificationActivity::class.java)
                        intent.putExtra("mobile", mobile)
                        intent.putExtra("isForgotPassword", true) // flag to reset password
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
