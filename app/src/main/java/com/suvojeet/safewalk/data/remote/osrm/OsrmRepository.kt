package com.suvojeet.safewalk.data.remote.osrm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsrmRepository @Inject constructor(
    private val osrmApi: OsrmApi,
) {
    /**
     * Get walking route between two coordinates.
     * @param startLon start longitude
     * @param startLat start latitude
     * @param endLon end longitude
     * @param endLat end latitude
     * @return list of routes or empty on failure
     */
    suspend fun getWalkingRoute(
        startLon: Double,
        startLat: Double,
        endLon: Double,
        endLat: Double,
    ): Result<List<OsrmRoute>> {
        return try {
            val coordinates = "$startLon,$startLat;$endLon,$endLat"
            val response = osrmApi.getRoute(coordinates)
            if (response.code == "Ok") {
                Result.success(response.routes)
            } else {
                Result.failure(Exception("OSRM error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract coordinate pairs from an OSRM route geometry.
     */
    fun extractCoordinates(route: OsrmRoute): List<Pair<Double, Double>> {
        return route.geometry?.coordinates?.map { coord ->
            // OSRM returns [longitude, latitude], we convert to [latitude, longitude]
            Pair(coord[1], coord[0])
        } ?: emptyList()
    }
}
