package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Gym Membership plan / package in the persistence layer.
 * Multi-tenant partitioned by businessId.
 */
@Entity(
    tableName = "memberships",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["name"])
    ]
)
data class MembershipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val name: String, // e.g. "Monthly Standard", "Quarterly", "Annual VIP", "CrossFit & Cardio"
    val durationDays: Int = 30, // 30, 90, 180, 365 days
    val pricePkr: Double = 3000.0,
    val admissionFeePkr: Double = 0.0,
    val description: String = "",
    val facilities: String = "", // e.g. "Weights, Cardio, Locker Room, Steam/Sauna"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

typealias Membership = MembershipEntity
typealias Memberships = MembershipEntity
