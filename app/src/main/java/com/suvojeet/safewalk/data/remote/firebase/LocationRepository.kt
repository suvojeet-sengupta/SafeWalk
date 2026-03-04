package com.suvojeet.safewalk.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.suvojeet.safewalk.data.model.LocationData
import com.suvojeet.safewalk.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
) {
    private val usersRef = firebaseDatabase.reference.child(Constants.FIREBASE_USERS_PATH)

    suspend fun pushLocation(locationData: LocationData) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        usersRef.child(userId)
            .child(Constants.FIREBASE_LOCATION_PATH)
            .setValue(locationData)
            .await()
    }

    suspend fun setLocationSharingActive(active: Boolean, shareId: String = "") {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val sharingData = mapOf(
            "active" to active,
            "shareId" to shareId,
            "timestamp" to System.currentTimeMillis(),
        )
        usersRef.child(userId)
            .child(Constants.FIREBASE_SHARING_PATH)
            .setValue(sharingData)
            .await()
    }

    fun observeLocation(userId: String): Flow<LocationData?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationData::class.java)
                trySend(location)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = usersRef.child(userId).child(Constants.FIREBASE_LOCATION_PATH)
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    fun generateShareId(): String {
        return usersRef.push().key ?: System.currentTimeMillis().toString()
    }
}
