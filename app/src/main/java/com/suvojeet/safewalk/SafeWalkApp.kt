package com.suvojeet.safewalk

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SafeWalkApp : Application() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        // Sign in anonymously so Firebase RTDB operations have a uid
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d(TAG, "Anonymous auth success: uid=${it.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Anonymous auth failed", e)
                }
        } else {
            Log.d(TAG, "Already signed in: uid=${firebaseAuth.currentUser?.uid}")
        }
    }

    companion object {
        private const val TAG = "SafeWalkApp"
    }
}
