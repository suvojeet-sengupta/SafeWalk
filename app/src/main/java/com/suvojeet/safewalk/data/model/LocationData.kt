package com.suvojeet.safewalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
)
