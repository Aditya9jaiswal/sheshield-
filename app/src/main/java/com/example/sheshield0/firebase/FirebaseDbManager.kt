package com.example.sheshield0.firebase

import android.util.Log
import com.example.sheshield0.model.FamilyMember
import com.example.sheshield0.model.UserModel
import com.google.firebase.database.FirebaseDatabase

class FirebaseDbManager {

    private val database = FirebaseDatabase.getInstance().reference

    // Save basic user data
    fun saveUser(userId: String, user: UserModel, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) Log.e("FirebaseDbManager", "Failed to save user: ${task.exception}")
                onResult(task.isSuccessful)
            }
    }

    // Update family members for a user
    fun saveFamily(userId: String, familyMembers: List<FamilyMember>, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).child("family").setValue(familyMembers)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) Log.e("FirebaseDbManager", "Failed to save family members: ${task.exception}")
                onResult(task.isSuccessful)
            }
    }
}
