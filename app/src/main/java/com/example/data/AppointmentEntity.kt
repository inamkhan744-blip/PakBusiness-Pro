package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Patient Appointment / Token booking in the Hospital & Clinic module.
 * Fully isolated by businessId for multi-tenant clinic operations.
 */
@Entity(
    tableName = "appointments",
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
        Index(value = ["doctorId"]),
        Index(value = ["patientId"]),
        Index(value = ["appointmentDate"])
    ]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val patientId: Long,
    val patientName: String,
    val patientPhone: String,
    val doctorId: Long,
    val doctorName: String,
    val doctorSpecialization: String,
    val appointmentDate: Long, // timestamp (start of day)
    val timeSlot: String, // e.g. "06:30 PM"
    val tokenNumber: Int,
    val consultationFeePkr: Double,
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    val symptoms: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

typealias Appointment = AppointmentEntity
