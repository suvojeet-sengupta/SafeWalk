package com.suvojeet.safewalk.util

import android.location.Location

object LocationUtils {

    /**
     * Calculate distance between two coordinates in meters.
     */
    fun distanceBetween(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Determine if user is moving based on speed threshold.
     * Speed > 0.5 m/s (~1.8 km/h) = walking
     */
    fun isMoving(speed: Float): Boolean = speed > 0.5f

    /**
     * Generate a Google Maps link from coordinates.
     */
    fun generateMapsLink(latitude: Double, longitude: Double): String {
        return "https://maps.google.com/maps?q=$latitude,$longitude"
    }

    /**
     * Format coordinates for display.
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }
}
