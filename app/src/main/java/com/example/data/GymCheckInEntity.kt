package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_checkins")
data class GymCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val businessId: Long = 0,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val dateString: String // "2026-09-19"
)
