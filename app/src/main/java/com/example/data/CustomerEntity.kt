package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Customer / Khata Account in the core business module.
 * Fully multi-tenant and isolated by businessId.
 */
@Entity(
    tableName = "customers",
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
        Index(value = ["name"]),
        Index(value = ["city"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val openingBalancePkr: Double = 0.0,
    val currentBalancePkr: Double = 0.0, // Khata balance: positive = receivable from customer
    val creditLimitPkr: Double = 0.0,
    val ntnOrCnic: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

typealias Customer = CustomerEntity
