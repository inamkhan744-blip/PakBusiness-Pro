package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Patient in the Hospital & Clinic module.
 * Fully isolated by businessId for multi-tenant clinic operations.
 */
@Entity(
    tableName = "patients",
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
        Index(value = ["phone"]),
        Index(value = ["name"])
    ]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val age: Int,
    val gender: String, // Male, Female, Other
    val bloodGroup: String = "", // A+, B+, O+, AB+, etc.
    val address: String = "",
    val medicalHistory: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val mrNumber: String get() = "MR-$id"
}

typealias Patient = PatientEntity
