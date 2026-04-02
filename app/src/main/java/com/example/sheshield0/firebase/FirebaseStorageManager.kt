package com.example.sheshield0.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class FirebaseStorageManager {

    private val storage = FirebaseStorage.getInstance().reference

    // Upload video
    fun uploadVideo(userId: String, fileUri: Uri, onResult: (Boolean, String?) -> Unit) {
        val fileRef = storage.child("videos/$userId/${System.currentTimeMillis()}.mp4")

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onResult(true, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    // Upload audio
    fun uploadAudio(userId: String, fileUri: Uri, onResult: (Boolean, String?) -> Unit) {
        val fileRef = storage.child("audios/$userId/${System.currentTimeMillis()}.mp3")

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onResult(true, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}
