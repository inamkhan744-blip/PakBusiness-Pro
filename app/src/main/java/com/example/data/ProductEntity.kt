package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Product in the core business catalog and POS inventory module.
 * Fully multi-tenant and isolated by businessId.
 */
@Entity(
    tableName = "products",
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
        Index(value = ["sku"]),
        Index(value = ["barcode"]),
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val description: String = "",
    val costPricePkr: Double = 0.0,
    val salePricePkr: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val minStockThreshold: Double = 5.0,
    val unit: String = "Pcs", // Pcs, Kg, Litre, Meter, Box, Pack, Dozen
    val location: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

typealias Product = ProductEntity
