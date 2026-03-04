package com.suvojeet.safewalk.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.suvojeet.safewalk.MainActivity
import com.suvojeet.safewalk.R
import com.suvojeet.safewalk.util.Constants

class SafeWalkMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Use data payload or notification payload
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "SafeWalk Alert"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new safety alert."

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // Save token to Firebase RTDB under this user's node
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child(Constants.FIREBASE_USERS_PATH)
            .child(userId)
            .child("fcmToken")
            .setValue(token)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = Constants.CHANNEL_GENERAL

        // Create channel
        val channel = NotificationChannel(
            channelId,
            "General Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Safety alerts from contacts"
            enableVibration(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "SafeWalkFCM"
        private const val NOTIFICATION_ID = 3001
    }
}
