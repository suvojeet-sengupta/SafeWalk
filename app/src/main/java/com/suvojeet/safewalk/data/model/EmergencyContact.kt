package com.suvojeet.safewalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val priority: Int = 0,
    val isActive: Boolean = true,
)
