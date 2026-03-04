package com.suvojeet.safewalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckInTimer(
    val id: String = "",
    val durationMinutes: Int = 15,
    val message: String = "",
    val contactIds: List<String> = emptyList(),
    val startTimeMs: Long = 0L,
    val status: TimerStatus = TimerStatus.IDLE,
)

@Serializable
enum class TimerStatus {
    IDLE,
    ACTIVE,
    CHECKED_IN,
    ESCALATION_FIRST,
    ESCALATION_SECOND,
    EXPIRED,
}
