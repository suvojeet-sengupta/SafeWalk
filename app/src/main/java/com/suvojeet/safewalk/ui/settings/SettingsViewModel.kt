package com.suvojeet.safewalk.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
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
}
