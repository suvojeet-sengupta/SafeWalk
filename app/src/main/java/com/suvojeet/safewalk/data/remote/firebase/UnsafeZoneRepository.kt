package com.suvojeet.safewalk.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.suvojeet.safewalk.data.model.UnsafeZone
import com.suvojeet.safewalk.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnsafeZoneRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
) {
    private val zonesRef = firebaseDatabase.reference.child(Constants.FIREBASE_UNSAFE_ZONES_PATH)

    suspend fun reportUnsafeZone(zone: UnsafeZone) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val zoneWithReporter = zone.copy(
            reportedBy = userId,
            lastReportedMs = System.currentTimeMillis(),
        )
        val key = zone.id.ifEmpty { zonesRef.push().key ?: return }
        zonesRef.child(key).setValue(zoneWithReporter).await()
    }

    fun observeUnsafeZones(): Flow<List<UnsafeZone>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zones = snapshot.children.mapNotNull { child ->
                    child.getValue(UnsafeZone::class.java)?.copy(id = child.key ?: "")
                }
                trySend(zones)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        zonesRef.addValueEventListener(listener)
        awaitClose { zonesRef.removeEventListener(listener) }
    }

    suspend fun incrementReportCount(zoneId: String) {
        val snapshot = zonesRef.child(zoneId).get().await()
        val currentCount = snapshot.child("reportCount").getValue(Int::class.java) ?: 0
        zonesRef.child(zoneId).child("reportCount").setValue(currentCount + 1).await()
        zonesRef.child(zoneId).child("lastReportedMs").setValue(System.currentTimeMillis()).await()
    }
}
