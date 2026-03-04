package com.suvojeet.safewalk.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.local.db.entity.ContactEntity
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import com.suvojeet.safewalk.service.LocationTrackingService
import com.suvojeet.safewalk.util.Constants
import com.suvojeet.safewalk.worker.CheckInWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val appContext: Context,
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

    private var timerJob: Job? = null

    val defaultTimerDuration: StateFlow<Int> = preferencesManager.defaultTimerDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Constants.DEFAULT_CHECK_IN_DURATION_MIN)

    private val _timerTotalSeconds = MutableStateFlow(0L)
    val timerTotalSeconds: StateFlow<Long> = _timerTotalSeconds.asStateFlow()

    fun onPanicTriggered() {
        _isPanicActive.value = true
    }

    fun onPanicDeactivated() {
        _isPanicActive.value = false
    }

    /**
     * Toggle location sharing: starts/stops the LocationTrackingService
     * and persists the preference.
     */
    fun toggleLocationSharing(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLocationSharingActive(enabled)
            if (enabled) {
                LocationTrackingService.start(context, panicMode = false)
            } else {
                LocationTrackingService.stop(context)
            }
        }
    }

    /**
     * Start the check-in timer with custom or default duration.
     * Saves the selected duration as the new default.
     */
    fun startTimer(durationMinutes: Int? = null) {
        viewModelScope.launch {
            val duration = durationMinutes ?: preferencesManager.defaultTimerDuration.first()
            preferencesManager.setDefaultTimerDuration(duration)

            timerJob?.cancel()
            val totalSeconds = duration * 60L
            _timerTotalSeconds.value = totalSeconds
            _timerRemainingSeconds.value = totalSeconds
            _timerActive.value = true

            timerJob = viewModelScope.launch {
                while (_timerRemainingSeconds.value > 0 && _timerActive.value) {
                    delay(1000L)
                    _timerRemainingSeconds.value -= 1
                }
                if (_timerActive.value && _timerRemainingSeconds.value <= 0) {
                    _timerActive.value = false
                    scheduleEscalation()
                }
            }
        }
    }

    fun setTimerActive(active: Boolean, remainingSeconds: Long = 0L) {
        if (!active) {
            timerJob?.cancel()
            timerJob = null
            // Cancel any pending escalation workers
            WorkManager.getInstance(appContext)
                .cancelAllWorkByTag(Constants.WORK_TAG_CHECK_IN)
        }
        _timerActive.value = active
        _timerRemainingSeconds.value = remainingSeconds
    }

    /**
     * Schedule escalation workers when user misses check-in.
     * Level 1: Send SMS immediately.
     * Level 2: Show urgent notification after ESCALATION_SECOND_DELAY_MIN.
     */
    private fun scheduleEscalation() {
        val workManager = WorkManager.getInstance(appContext)

        // Level 1 — immediate SMS to contacts
        val firstEscalation = OneTimeWorkRequestBuilder<CheckInWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(CheckInWorker.KEY_ESCALATION_LEVEL, 1)
                    .build()
            )
            .addTag(Constants.WORK_TAG_CHECK_IN)
            .build()

        // Level 2 — urgent notification after delay
        val secondEscalation = OneTimeWorkRequestBuilder<CheckInWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(CheckInWorker.KEY_ESCALATION_LEVEL, 2)
                    .build()
            )
            .setInitialDelay(
                Constants.ESCALATION_SECOND_DELAY_MIN.toLong(),
                TimeUnit.MINUTES,
            )
            .addTag(Constants.WORK_TAG_CHECK_IN)
            .build()

        workManager.enqueue(listOf(firstEscalation, secondEscalation))
    }
}
