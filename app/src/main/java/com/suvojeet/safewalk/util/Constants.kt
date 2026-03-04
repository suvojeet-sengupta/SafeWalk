package com.suvojeet.safewalk.util

object Constants {

    // Firebase Realtime DB paths
    const val FIREBASE_USERS_PATH = "users"
    const val FIREBASE_LOCATION_PATH = "location"
    const val FIREBASE_SHARING_PATH = "sharing"
    const val FIREBASE_UNSAFE_ZONES_PATH = "unsafe_zones"
    const val FIREBASE_ALERTS_PATH = "alerts"

    // Location update intervals (milliseconds)
    const val LOCATION_UPDATE_MOVING_MS = 30_000L
    const val LOCATION_UPDATE_STATIONARY_MS = 120_000L
    const val LOCATION_UPDATE_PANIC_MS = 5_000L
    const val LOCATION_DISPLACEMENT_THRESHOLD_M = 50f

    // Timer defaults
    const val DEFAULT_CHECK_IN_DURATION_MIN = 15
    const val ESCALATION_FIRST_DELAY_MIN = 5
    const val ESCALATION_SECOND_DELAY_MIN = 15

    // Shake detection
    const val SHAKE_THRESHOLD_DEFAULT = 12.0f
    const val SHAKE_SLOP_TIME_MS = 500
    const val SHAKE_COUNT_RESET_TIME_MS = 3000
    const val SHAKE_COUNT_TRIGGER = 3

    // DataStore keys
    const val PREFS_NAME = "safewalk_prefs"

    // OSRM
    const val OSRM_BASE_URL = "https://router.project-osrm.org/"

    // Notification channels
    const val CHANNEL_LOCATION = "location_tracking"
    const val CHANNEL_PANIC = "panic_alert"
    const val CHANNEL_TIMER = "check_in_timer"
    const val CHANNEL_GENERAL = "general"

    // Work tags
    const val WORK_TAG_CHECK_IN = "check_in_timer"
}
