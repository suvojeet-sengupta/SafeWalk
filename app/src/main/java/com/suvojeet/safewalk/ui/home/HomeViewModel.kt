package com.suvojeet.safewalk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.local.db.entity.ContactEntity
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val contacts: StateFlow<List<ContactEntity>> = contactDao.getActiveContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLocationSharing: StateFlow<Boolean> = preferencesManager.isLocationSharingActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isPanicActive = MutableStateFlow(false)
    val isPanicActive: StateFlow<Boolean> = _isPanicActive.asStateFlow()

    private val _timerActive = MutableStateFlow(false)
    val timerActive: StateFlow<Boolean> = _timerActive.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(0L)
    val timerRemainingSeconds: StateFlow<Long> = _timerRemainingSeconds.asStateFlow()

    fun onPanicTriggered() {
        _isPanicActive.value = true
    }

    fun onPanicDeactivated() {
        _isPanicActive.value = false
    }

    fun setTimerActive(active: Boolean, remainingSeconds: Long = 0L) {
        _timerActive.value = active
        _timerRemainingSeconds.value = remainingSeconds
    }
}
