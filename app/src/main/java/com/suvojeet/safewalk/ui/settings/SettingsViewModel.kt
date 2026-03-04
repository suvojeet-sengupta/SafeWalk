package com.suvojeet.safewalk.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import com.suvojeet.safewalk.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val shakeEnabled: StateFlow<Boolean> = preferencesManager.isShakeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shakeSensitivity: StateFlow<Float> = preferencesManager.shakeSensitivity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12f)

    val panicVibrate: StateFlow<Boolean> = preferencesManager.isPanicVibrateEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoCall: StateFlow<Boolean> = preferencesManager.isAutoCallEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkTheme: StateFlow<Boolean> = preferencesManager.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val userName: StateFlow<String> = preferencesManager.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SafeWalk User")

    val userPhone: StateFlow<String> = preferencesManager.userPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val timerDuration: StateFlow<Int> = preferencesManager.defaultTimerDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Constants.DEFAULT_CHECK_IN_DURATION_MIN)

    val sirenEnabled: StateFlow<Boolean> = preferencesManager.isSirenEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val strobeEnabled: StateFlow<Boolean> = preferencesManager.isStrobeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lowBatteryAlert: StateFlow<Boolean> = preferencesManager.isLowBatteryAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val networkLossAlert: StateFlow<Boolean> = preferencesManager.isNetworkLossAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setShakeEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setShakeEnabled(enabled) }
    }

    fun setShakeSensitivity(sensitivity: Float) {
        viewModelScope.launch { preferencesManager.setShakeSensitivity(sensitivity) }
    }

    fun setPanicVibrate(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setPanicVibrate(enabled) }
    }

    fun setAutoCall(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoCallEnabled(enabled) }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { preferencesManager.setDarkTheme(dark) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { preferencesManager.setUserName(name) }
    }

    fun setUserPhone(phone: String) {
        viewModelScope.launch { preferencesManager.setUserPhone(phone) }
    }

    fun setTimerDuration(minutes: Int) {
        viewModelScope.launch { preferencesManager.setDefaultTimerDuration(minutes) }
    }

    fun setSirenEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setSirenEnabled(enabled) }
    }

    fun setStrobeEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setStrobeEnabled(enabled) }
    }

    fun setLowBatteryAlert(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setLowBatteryAlert(enabled) }
    }

    fun setNetworkLossAlert(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNetworkLossAlert(enabled) }
    }
}
