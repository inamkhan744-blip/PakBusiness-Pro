package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Doctor / Consultant in the Hospital & Clinic module.
 * Fully isolated by businessId for multi-tenant clinic operations.
 */
@Entity(
    tableName = "doctors",
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
        Index(value = ["name"]),
        Index(value = ["specialization"])
    ]
)
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val specialization: String,
    val qualification: String,
    val consultationFeePkr: Double,
    val availableDays: String, // e.g. Mon, Tue, Wed, Thu, Fri
    val availableHours: String, // e.g. 05:00 PM - 09:00 PM
    val phone: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

typealias Doctor = DoctorEntity
