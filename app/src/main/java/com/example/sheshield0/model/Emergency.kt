package com.example.sheshield0.model
data class Emergency(
    val userId: String = "",
    val videoUrl: String? = null,
    val audioUrl: String? = null, // NEW FIELD
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val mapUrl: String = "",
    val timestamp: Long = 0L,
    val status: String = "active",
    val policeNotified: Boolean = false,
    val triggeredBy: String? = null // voice_detection or manual
)