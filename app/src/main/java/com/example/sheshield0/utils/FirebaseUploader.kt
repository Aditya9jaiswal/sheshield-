package com.example.sheshield0.utils

import android.util.Log
import com.example.sheshield0.model.Emergency
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object FirebaseUploader {

    fun uploadEmergencyData(
        videoUrl: String,
        latitude: Double,
        longitude: Double,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onComplete(false)
            return
        }

        val db = FirebaseDatabase.getInstance()
        val emergenciesRef = db.getReference("emergencies").child(uid)


        val pushId = emergenciesRef.push().key ?: System.currentTimeMillis().toString()

        // Create Emergency object
        val emergencyData = Emergency(
            userId = uid,
            videoUrl = videoUrl,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            status = "pending",
            policeNotified = false
        )

        // Save emergency first
        emergenciesRef.child(pushId).setValue(emergencyData)
            .addOnSuccessListener {
                // ✅ Step 1: Save alert
                val alertsRef = db.getReference("alerts").child(uid).child(pushId)
                val alertData = mapOf(
                    "pushStatus" to true,
                    "familyNotified" to false,
                    "policeAlerted" to false
                )

                alertsRef.setValue(alertData).addOnSuccessListener {
                    // ✅ Step 2: Notify family members
                    notifyFamilyMembers(uid, videoUrl, latitude, longitude)
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Fetches family members from Firebase and notifies them.
     */
    private fun notifyFamilyMembers(uid: String, videoUrl: String, lat: Double, lon: Double) {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference("users").child(uid)

        userRef.child("family").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (member in snapshot.children) {
                    val mobile = member.child("mobile").value?.toString()
                    val name = member.child("name").value?.toString()

                    if (!mobile.isNullOrEmpty()) {
                        val message =
                            "🚨 Emergency Alert!\n$name needs help.\n" +
                                    "📍 Location: https://maps.google.com/?q=$lat,$lon\n" +
                                    "📹 Video: $videoUrl"

                        // For now we just log (later integrate SMS / FCM push)
                        Log.d("FamilyNotify", "Send to $mobile: $message")

                        // TODO: You can use:
                        // 1️⃣ Firebase Cloud Messaging (FCM) → push notification
                        // 2️⃣ SMS Manager → send SMS
                        // 3️⃣ WhatsApp API → send WhatsApp msg
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FamilyNotify", "Error: ${error.message}")
            }
        })
    }
}
