package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salon_services")
data class SalonServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val category: String, // "HAIR", "SKIN", "SPA"
    val price: Double, // in PKR
    val durationMinutes: Int = 45,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "salon_stylists")
data class StylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val specialty: String, // e.g. "Hair Specialist", "Skin & Facial Expert", "Spa Therapist", "Master Stylist"
    val commissionPercentage: Double = 20.0, // e.g. 20.0 for 20%
    val totalEarnedCommission: Double = 0.0, // in PKR
    val totalServicesCompleted: Int = 0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "salon_appointments")
data class SalonAppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val clientName: String,
    val clientPhone: String,
    val serviceId: Long,
    val serviceName: String,
    val serviceCategory: String = "HAIR", // "HAIR", "SKIN", "SPA"
    val stylistId: Long,
    val stylistName: String,
    val appointmentDate: String, // "YYYY-MM-DD" e.g. "2026-09-20"
    val appointmentTime: String, // e.g. "02:30 PM"
    val status: String = "SCHEDULED", // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val servicePrice: Double, // in PKR
    val discount: Double = 0.0, // in PKR
    val finalPrice: Double, // in PKR
    val commissionPercentage: Double = 20.0,
    val commissionAmount: Double = 0.0, // in PKR
    val paymentStatus: String = "PENDING", // "PENDING", "PAID"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
