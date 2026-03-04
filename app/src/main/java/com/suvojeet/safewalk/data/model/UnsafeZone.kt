package com.suvojeet.safewalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnsafeZone(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 200.0,
    val reportCount: Int = 1,
    val description: String = "",
    val lastReportedMs: Long = System.currentTimeMillis(),
    val reportedBy: String = "",
)
