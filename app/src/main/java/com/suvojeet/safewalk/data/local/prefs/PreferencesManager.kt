package com.suvojeet.safewalk.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safewalk_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    // Keys
    private object Keys {
        val SHAKE_ENABLED = booleanPreferencesKey("shake_enabled")
        val SHAKE_SENSITIVITY = floatPreferencesKey("shake_sensitivity")
        val DEFAULT_TIMER_DURATION = intPreferencesKey("default_timer_duration")
        val LOCATION_SHARING_ACTIVE = booleanPreferencesKey("location_sharing_active")
        val LOCATION_UPDATE_INTERVAL = longPreferencesKey("location_update_interval")
        val PANIC_VIBRATE = booleanPreferencesKey("panic_vibrate")
        val AUTO_CALL_ENABLED = booleanPreferencesKey("auto_call_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val SIREN_ENABLED = booleanPreferencesKey("siren_enabled")
        val STROBE_ENABLED = booleanPreferencesKey("strobe_enabled")
        val LOW_BATTERY_ALERT = booleanPreferencesKey("low_battery_alert")
        val NETWORK_LOSS_ALERT = booleanPreferencesKey("network_loss_alert")
    }

    // Shake detection
    val isShakeEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.SHAKE_ENABLED] ?: false }
    val shakeSensitivity: Flow<Float> = dataStore.data.map { it[Keys.SHAKE_SENSITIVITY] ?: 12.0f }

    suspend fun setShakeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SHAKE_ENABLED] = enabled }
    }

    suspend fun setShakeSensitivity(sensitivity: Float) {
        dataStore.edit { it[Keys.SHAKE_SENSITIVITY] = sensitivity }
    }

    // Timer
    val defaultTimerDuration: Flow<Int> = dataStore.data.map { it[Keys.DEFAULT_TIMER_DURATION] ?: 15 }

    suspend fun setDefaultTimerDuration(minutes: Int) {
        dataStore.edit { it[Keys.DEFAULT_TIMER_DURATION] = minutes }
    }

    // Location
    val isLocationSharingActive: Flow<Boolean> = dataStore.data.map {
        it[Keys.LOCATION_SHARING_ACTIVE] ?: false
    }

    suspend fun setLocationSharingActive(active: Boolean) {
        dataStore.edit { it[Keys.LOCATION_SHARING_ACTIVE] = active }
    }

    // Panic settings
    val isPanicVibrateEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.PANIC_VIBRATE] ?: true }
    val isAutoCallEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_CALL_ENABLED] ?: false }

    suspend fun setPanicVibrate(enabled: Boolean) {
        dataStore.edit { it[Keys.PANIC_VIBRATE] = enabled }
    }

    suspend fun setAutoCallEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_CALL_ENABLED] = enabled }
    }

    // Onboarding
    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map {
        it[Keys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    // Theme
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[Keys.DARK_THEME] ?: true }

    suspend fun setDarkTheme(dark: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = dark }
    }

    // User name
    val userName: Flow<String> = dataStore.data.map { it[Keys.USER_NAME] ?: "SafeWalk User" }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    // User phone number (included in SOS messages so contacts can call back)
    val userPhone: Flow<String> = dataStore.data.map { it[Keys.USER_PHONE] ?: "" }

    suspend fun setUserPhone(phone: String) {
        dataStore.edit { it[Keys.USER_PHONE] = phone }
    }

    // Deterrent Mode
    val isSirenEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.SIREN_ENABLED] ?: true }
    val isStrobeEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.STROBE_ENABLED] ?: true }

    suspend fun setSirenEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SIREN_ENABLED] = enabled }
    }

    suspend fun setStrobeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.STROBE_ENABLED] = enabled }
    }

    // System Monitoring
    val isLowBatteryAlertEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.LOW_BATTERY_ALERT] ?: true }
    val isNetworkLossAlertEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.NETWORK_LOSS_ALERT] ?: true }

    suspend fun setLowBatteryAlert(enabled: Boolean) {
        dataStore.edit { it[Keys.LOW_BATTERY_ALERT] = enabled }
    }

    suspend fun setNetworkLossAlert(enabled: Boolean) {
        dataStore.edit { it[Keys.NETWORK_LOSS_ALERT] = enabled }
    }
}
