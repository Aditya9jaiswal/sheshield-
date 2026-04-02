package com.example.sheshield0

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPassword: EditText
    private lateinit var resetBtn: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        val mobile = intent.getStringExtra("mobile") ?: return finish()
        val uid = mobile.replace("+","")
        val authEmail = "$uid@sheshield.com"

        newPassword = findViewById(R.id.newPassword)
        resetBtn = findViewById(R.id.resetBtn)

        resetBtn.setOnClickListener {
            val pass = newPassword.text.toString().trim()
            if (pass.isEmpty()) {
                Toast.makeText(this, "Enter new password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update password in Firebase Auth
            auth.signInWithEmailAndPassword(authEmail, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser?.updatePassword(pass)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Also update password in Realtime DB
                            FirebaseDatabase.getInstance().getReference("users").child(uid)
                                .child("password").setValue(pass)

                            Toast.makeText(this, "Password Reset Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
