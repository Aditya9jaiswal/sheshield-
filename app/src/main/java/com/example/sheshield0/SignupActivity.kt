package com.example.sheshield0

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sheshield0.model.FamilyMember
import com.example.sheshield0.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignupActivity : AppCompatActivity() {

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var mobileEt: EditText
    private lateinit var ageEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var genderSpinner: Spinner

    private lateinit var etFamily1Name: EditText
    private lateinit var spFamily1Relation: Spinner
    private lateinit var etFamily1Mobile: EditText
    private lateinit var etFamily1Email: EditText

    private lateinit var etFamily2Name: EditText
    private lateinit var spFamily2Relation: Spinner
    private lateinit var etFamily2Mobile: EditText
    private lateinit var etFamily2Email: EditText

    private lateinit var signupBtn: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var selectedGender: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // INIT
        nameEt = findViewById(R.id.etName)
        emailEt = findViewById(R.id.etEmail)
        mobileEt = findViewById(R.id.etMobile)
        ageEt = findViewById(R.id.etAge)
        passwordEt = findViewById(R.id.etPassword)
        genderSpinner = findViewById(R.id.spGender)
        signupBtn = findViewById(R.id.btnSignup)
        progressBar = findViewById(R.id.progressBar)

        etFamily1Name = findViewById(R.id.etFamily1Name)
        spFamily1Relation = findViewById(R.id.spFamily1Relation)
        etFamily1Mobile = findViewById(R.id.etFamily1Mobile)
        etFamily1Email = findViewById(R.id.etFamily1Email)

        etFamily2Name = findViewById(R.id.etFamily2Name)
        spFamily2Relation = findViewById(R.id.spFamily2Relation)
        etFamily2Mobile = findViewById(R.id.etFamily2Mobile)
        etFamily2Email = findViewById(R.id.etFamily2Email)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Gender Spinner
        val genderOptions = arrayOf("Select Gender", "Female", "Male", "Other")
        genderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGender = if (position > 0) genderOptions[position] else ""
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Relation Spinner
        val relations = arrayOf("Select Relation", "Mother", "Father", "Brother", "Sister", "Other")
        spFamily1Relation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, relations)
        spFamily2Relation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, relations)

        signupBtn.setOnClickListener {
            performSignup()
        }
    }

    private fun performSignup() {

        val name = nameEt.text.toString().trim()
        val email = emailEt.text.toString().trim().lowercase()
        val mobileInput = mobileEt.text.toString().trim()
        val age = ageEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()

        val f1Name = etFamily1Name.text.toString().trim()
        val f1Relation = spFamily1Relation.selectedItem.toString()
        val f1Mobile = etFamily1Mobile.text.toString().trim()
        val f1Email = etFamily1Email.text.toString().trim()

        val f2Name = etFamily2Name.text.toString().trim()
        val f2Relation = spFamily2Relation.selectedItem.toString()
        val f2Mobile = etFamily2Mobile.text.toString().trim()
        val f2Email = etFamily2Email.text.toString().trim()

        // VALIDATION
        if (name.isEmpty() || email.isEmpty() || mobileInput.isEmpty() || age.isEmpty() || password.isEmpty() || selectedGender.isEmpty()) {
            showToast("Fill all personal fields")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email")
            return
        }

        val cleanMobile = mobileInput.replace("[^0-9]".toRegex(), "")
        if (!cleanMobile.matches(Regex("^[6-9]\\d{9}$"))) {
            showToast("Enter valid mobile")
            return
        }

        if (password.length < 6) {
            showToast("Password must be 6+ chars")
            return
        }

        if (f1Name.isEmpty() || f1Relation == "Select Relation" || f1Mobile.isEmpty()) {
            showToast("Fill Family Member 1")
            return
        }

        if (f2Name.isEmpty() || f2Relation == "Select Relation" || f2Mobile.isEmpty()) {
            showToast("Fill Family Member 2")
            return
        }

        progressBar.visibility = View.VISIBLE
        signupBtn.isEnabled = false

        // AUTH
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->

                if (authTask.isSuccessful) {

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->

                        if (!tokenTask.isSuccessful) {
                            showToast("FCM token failed")
                            progressBar.visibility = View.GONE
                            signupBtn.isEnabled = true
                            return@addOnCompleteListener
                        }

                        val fcmToken = tokenTask.result

                        // ✅ Correct UserModel with named parameters
                        val user = UserModel(
                            uid = cleanMobile,
                            name = name,
                            email = email,
                            mobile = cleanMobile,
                            age = age,
                            gender = selectedGender,
                            password = password,
                            fcmToken = fcmToken,
                            familyMembers = listOf(
                                FamilyMember(f1Name, f1Relation, f1Mobile, f1Email),
                                FamilyMember(f2Name, f2Relation, f2Mobile, f2Email)
                            )
                        )

                        val userRef = database.getReference("users").child(cleanMobile)

                        userRef.setValue(user).addOnSuccessListener {

                            val prefs = getSharedPreferences("SheShieldPrefs", MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("current_user_name", name)
                                putString("current_user_email", email)
                                putString("current_user_mobile", cleanMobile)
                                putString("current_user_uid", cleanMobile)
                                putString("fcm_token", fcmToken)
                                apply()
                            }

                            showToast("Signup Successful!")

                            val intent = Intent(this, OtpVerificationActivity::class.java)
                            intent.putExtra("userId", cleanMobile)
                            intent.putExtra("userMobile", cleanMobile)
                            startActivity(intent)
                            finish()

                        }.addOnFailureListener {
                            showToast("Database Error: ${it.message}")
                            progressBar.visibility = View.GONE
                            signupBtn.isEnabled = true
                        }
                    }

                } else {
                    progressBar.visibility = View.GONE
                    signupBtn.isEnabled = true
                    showToast(authTask.exception?.message ?: "Signup failed")
                }
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}