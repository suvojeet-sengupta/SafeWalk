package com.suvojeet.safewalk.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int,
    val isActive: Boolean = true,
)
