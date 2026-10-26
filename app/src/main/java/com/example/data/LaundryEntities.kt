package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Laundry customer entity with name, phone, address, and special notes.
 */
@Entity(
    tableName = "laundry_customers",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["phone"])
    ]
)
data class LaundryCustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val address: String = "",
    val notes: String = "",
    val totalOrdersCount: Int = 0,
    val totalSpent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Laundry order tracking entity.
 * Status workflow: WASHING -> IRONING -> READY -> DELIVERED (also supports CANCELLED)
 */
@Entity(
    tableName = "laundry_orders",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["orderNumber"]),
        Index(value = ["customerId"]),
        Index(value = ["status"])
    ]
)
data class LaundryOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val orderNumber: String, // e.g. "LD-2026-001"
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    
    // Service details
    val serviceType: String = "Wash & Iron", // "Wash & Iron", "Dry Clean", "Iron Only", "Wash & Fold"
    val urgentDelivery: Boolean = false,
    
    // Clothes item quantities
    val shirtCount: Int = 0,
    val pantsCount: Int = 0,
    val bedsheetCount: Int = 0,
    val shalwarKameezCount: Int = 0,
    val suitCount: Int = 0,
    val curtainCount: Int = 0,
    val otherItemsCount: Int = 0,
    val otherItemsDescription: String = "",
    
    // Total pieces
    val totalPieces: Int = 0,
    
    // Tracking status: WASHING -> IRONING -> READY -> DELIVERED
    val status: String = "WASHING", // "WASHING", "IRONING", "READY", "DELIVERED", "CANCELLED"
    
    // Financials in PKR
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val paymentStatus: String = "PENDING", // "PAID", "PARTIAL", "PENDING"
    
    // Dates
    val orderDate: String, // "YYYY-MM-DD"
    val deliveryDate: String, // "YYYY-MM-DD"
    val deliveredAt: Long = 0,
    
    // Starch & fragrance preferences / tags
    val specialInstructions: String = "", // e.g. "Double starch on collars, separate whites"
    val rackLocation: String = "Rack A-3",
    
    val createdAt: Long = System.currentTimeMillis()
)
