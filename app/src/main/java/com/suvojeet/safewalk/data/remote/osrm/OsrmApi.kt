package com.suvojeet.safewalk.data.remote.osrm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApi {

    @GET("route/v1/walking/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("alternatives") alternatives: Boolean = true,
        @Query("steps") steps: Boolean = true,
    ): OsrmRouteResponse
}

@Serializable
data class OsrmRouteResponse(
    val code: String = "",
    val routes: List<OsrmRoute> = emptyList(),
)

@Serializable
data class OsrmRoute(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val geometry: OsrmGeometry? = null,
    val legs: List<OsrmLeg> = emptyList(),
)

@Serializable
data class OsrmGeometry(
    val type: String = "",
    val coordinates: List<List<Double>> = emptyList(),
)

@Serializable
data class OsrmLeg(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val summary: String = "",
    val steps: List<OsrmStep> = emptyList(),
)

@Serializable
data class OsrmStep(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val name: String = "",
    val maneuver: OsrmManeuver? = null,
)

@Serializable
data class OsrmManeuver(
    val type: String = "",
    val modifier: String = "",
    val location: List<Double> = emptyList(),
    @SerialName("bearing_before")
    val bearingBefore: Double = 0.0,
    @SerialName("bearing_after")
    val bearingAfter: Double = 0.0,
)
