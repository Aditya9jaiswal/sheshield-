package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var otpInput: EditText
    private lateinit var verifyBtn: Button
    private lateinit var resendBtn: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var mobile: String
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        otpInput = findViewById(R.id.otpInput)
        verifyBtn = findViewById(R.id.verifyBtn)
        resendBtn = findViewById(R.id.resendOtpText)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        mobile = intent.getStringExtra("mobile") ?: ""
        if (mobile.isEmpty()) {
            Toast.makeText(this, "Mobile number not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sendOtpToUser(mobile)

        verifyBtn.setOnClickListener {
            val code = otpInput.text.toString().trim()
            if (code.length == 6 && this::verificationId.isInitialized) {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                verifyOtpAndLogin(credential)
            } else {
                Toast.makeText(this, "Enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendBtn.setOnClickListener { sendOtpToUser(mobile) }
    }

    private fun sendOtpToUser(mobile: String) {
        progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobile)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressBar.visibility = View.GONE
                    verifyOtpAndLogin(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@OtpVerificationActivity, "OTP failed: ${e.message}", Toast.LENGTH_LONG).show()
                    deleteUserDataOnFailure()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    progressBar.visibility = View.GONE
                    verificationId = id
                    Toast.makeText(this@OtpVerificationActivity, "OTP sent to $mobile", Toast.LENGTH_SHORT).show()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtpAndLogin(credential: PhoneAuthCredential) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "OTP Verification Failed", Toast.LENGTH_LONG).show()
                deleteUserDataOnFailure()
            }
        }
    }

    private fun deleteUserDataOnFailure() {
        FirebaseDatabase.getInstance().getReference("users")
            .child(mobile.replace("+91", ""))
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Invalid OTP. User data deleted.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SignupActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting user data", Toast.LENGTH_LONG).show()
            }
    }
}
