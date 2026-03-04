package com.suvojeet.safewalk.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SafeWalkMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Handle incoming FCM messages (e.g., alerts from contacts)
        message.notification?.let {
            // TODO: Show notification to user
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to Firebase for this user
    }
}
