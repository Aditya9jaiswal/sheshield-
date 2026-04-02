package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var etUser: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button
    private lateinit var tvForgot: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUser = findViewById(R.id.etUser)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignup = findViewById(R.id.btnSignup)
        tvForgot = findViewById(R.id.tvForgot)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Auto-login if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val userInput = etUser.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (userInput.isEmpty() || password.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showToast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            progressBar.visibility = ProgressBar.VISIBLE
            btnLogin.isEnabled = false
            loginUser(userInput, password)
        }

        tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            // Prefill if user entered email/mobile
            intent.putExtra("email_prefill", etUser.text.toString().trim())
            intent.putExtra("mobile_prefill", etUser.text.toString().trim())
            startActivity(intent)
        }
    }

    private fun loginUser(userInput: String, password: String) {
        if (Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
            loginWithEmail(userInput, password)
        } else {
            loginWithMobile(userInput, password)
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Find mobile number from database using email
                    findUserByEmail(email, currentUser.uid)
                }
            } else {
                progressBar.visibility = ProgressBar.GONE
                btnLogin.isEnabled = true
                showToast(task.exception?.message ?: "Login failed")
            }
        }
    }

    private fun loginWithMobile(mobileInput: String, password: String) {
        // Remove any non-digit characters and ensure it's 10 digits
        val cleanMobile = mobileInput.replace("[^0-9]".toRegex(), "")

        if (cleanMobile.length != 10) {
            progressBar.visibility = ProgressBar.GONE
            btnLogin.isEnabled = true
            showToast("Please enter a valid 10-digit mobile number")
            return
        }

        val usersRef = database.getReference("users").child(cleanMobile)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userEmail = snapshot.child("email").getValue(String::class.java)

                    if (userEmail != null) {
                        // Sign in with email (Firebase Auth requires email)
                        auth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener { task ->
                            progressBar.visibility = ProgressBar.GONE
                            btnLogin.isEnabled = true

                            if (task.isSuccessful) {
                                val currentUser = auth.currentUser
                                if (currentUser != null) {
                                    storeCurrentUser(userEmail, currentUser.uid, cleanMobile)
                                    val userName = snapshot.child("name").getValue(String::class.java) ?: "User"
                                    showToast("Welcome $userName")
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                showToast(task.exception?.message ?: "Login failed")
                            }
                        }
                    } else {
                        progressBar.visibility = ProgressBar.GONE
                        btnLogin.isEnabled = true
                        showToast("User email not found")
                    }
                } else {
                    progressBar.visibility = ProgressBar.GONE
                    btnLogin.isEnabled = true
                    showToast("User not found. Please sign up first.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                btnLogin.isEnabled = true
                showToast("Database error: ${error.message}")
            }
        })
    }

    private fun findUserByEmail(email: String, uid: String) {
        val usersRef = database.getReference("users")

        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = ProgressBar.GONE
                    btnLogin.isEnabled = true

                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val mobile = userSnapshot.key
                            val userName = userSnapshot.child("name").getValue(String::class.java) ?: "User"

                            if (!mobile.isNullOrEmpty()) {
                                storeCurrentUser(email, uid, mobile)
                                showToast("Welcome $userName")
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                                return
                            }
                        }
                    }
                    // If mobile not found, still proceed but show warning
                    storeCurrentUser(email, uid, "")
                    showToast("Welcome back!")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = ProgressBar.GONE
                    btnLogin.isEnabled = true
                    showToast("Database error: ${error.message}")
                }
            })
    }

    private fun storeCurrentUser(email: String, uid: String, mobile: String) {
        val prefs = getSharedPreferences("SheShieldPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("current_user_email", email)
            putString("current_user_uid", uid)
            putString("current_user_mobile", mobile)
            apply()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}