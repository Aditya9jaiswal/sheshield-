package com.example.sheshield0.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sheshield0.R

object NotificationHelper {

    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.description = "Notifications for SheShield emergencies"
            manager.createNotificationChannel(channel)
        }

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_emergency) // Use your emergency icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Tap to dismiss
            .build()

        // Show notification
        manager.notify(notificationId, notification)
    }
}
