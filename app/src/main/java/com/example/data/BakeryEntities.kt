package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "bakery_items",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["category"]),
        Index(value = ["expiryDate"])
    ]
)
data class BakeryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String,
    val category: String, // "Mithai / Sweets", "Bakery & Breads", "Pastries & Cakes", "Snacks & Savories", "Biscuits & Cookies"
    val unit: String, // "kg", "piece", "pack", "box"
    val pricePerUnit: Double,
    val currentStock: Double,
    val productionDate: String, // "YYYY-MM-DD"
    val expiryDate: String, // "YYYY-MM-DD"
    val batchNumber: String = "",
    val lowStockThreshold: Double = 5.0
) {
    val isExpired: Boolean
        get() {
            if (expiryDate.isBlank()) return false
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val exp = sdf.parse(expiryDate)
                val today = sdf.parse(sdf.format(Date()))
                exp != null && exp.before(today)
            } catch (e: Exception) {
                false
            }
        }

    val isExpiringSoon: Boolean
        get() {
            if (expiryDate.isBlank()) return false
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val exp = sdf.parse(expiryDate) ?: return false
                val today = sdf.parse(sdf.format(Date())) ?: return false
                val diffDays = (exp.time - today.time) / (1000 * 60 * 60 * 24)
                diffDays in 0..2
            } catch (e: Exception) {
                false
            }
        }

    val isLowStock: Boolean
        get() = currentStock <= lowStockThreshold
}

@Entity(
    tableName = "bakery_cake_orders",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["status"]),
        Index(value = ["deliveryDate"])
    ]
)
data class BakeryCakeOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val occasion: String, // "Birthday", "Wedding", "Anniversary", "Custom Event"
    val flavor: String, // "Chocolate Fudge", "Red Velvet", "Lotus Biscoff", "Vanilla Buttercream", "Pistachio Kulfi", "Fresh Mango"
    val weightLbs: Double, // e.g. 2.0, 3.5, 5.0
    val spongeType: String = "Regular (Egg)", // "Regular (Egg)", "Eggless"
    val messageOnCake: String = "",
    val customDesignNotes: String = "",
    val deliveryDate: String, // "YYYY-MM-DD"
    val deliveryTime: String = "05:00 PM",
    val status: String = "RECEIVED", // "RECEIVED", "BAKING", "DECORATING", "READY", "DELIVERED", "CANCELLED"
    val totalPrice: Double,
    val advancePaid: Double = 0.0,
    val remainingBalance: Double = totalPrice - advancePaid,
    val createdAt: Long = System.currentTimeMillis()
)
