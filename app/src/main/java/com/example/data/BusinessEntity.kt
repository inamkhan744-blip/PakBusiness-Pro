package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "My Business",
    val type: String = "Retail & General Store",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val currency: String = "PKR",
    val logoUri: String = "",
    val tagline: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val category: String get() = type
    val currencySymbol: String get() = currency
}
