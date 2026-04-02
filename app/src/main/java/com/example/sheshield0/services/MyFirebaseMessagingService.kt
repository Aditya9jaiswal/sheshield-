package com.example.sheshield0.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sheshield0.MainActivity
import com.example.sheshield0.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Emergency Alert"
        val body = message.notification?.body ?: "Check the SheShield app"

        // PendingIntent to open MainActivity when notification is tapped
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show notification using helper
        NotificationHelper.showNotification(
            context = this,
            channelId = "SheShield_Channel",
            title = title,
            message = body,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Save this FCM token to Firebase DB under /users/{uid}/fcmToken
    }
}
