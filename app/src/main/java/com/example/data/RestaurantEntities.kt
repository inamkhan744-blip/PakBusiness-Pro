package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a dining table in the restaurant or cafe.
 * Status values: "AVAILABLE", "OCCUPIED", "RESERVED"
 */
@Entity(tableName = "restaurant_tables")
data class RestaurantTableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,             // e.g. "Table 1", "VIP Booth", "Terrace T2"
    val capacity: Int = 4,        // Number of seats: 2, 4, 6, 8, etc.
    val section: String = "Main Dining", // "Main Dining", "Family Hall", "Outdoor Terrace", "Rooftop"
    val status: String = "AVAILABLE", // "AVAILABLE", "OCCUPIED", "RESERVED"
    val currentOrderId: Long? = null,
    val reservedFor: String? = null,
    val reservedPhone: String? = null,
    val reservedTime: Long? = null,
    val occupiedSince: Long? = null
)

/**
 * Represents an item on the restaurant/cafe food & beverage menu.
 */
@Entity(tableName = "restaurant_menu_items")
data class RestaurantMenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,             // e.g. "Chicken Karahi Special", "Mutton Dum Biryani"
    val category: String,         // "Desi Special", "Karahi & Handi", "BBQ & Tandoor", "Fast Food", "Beverages", "Breads", "Desserts"
    val pricePkr: Double,         // Price in Pakistani Rupees
    val description: String = "",
    val isAvailable: Boolean = true, // In stock vs sold out toggle
    val prepTimeMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents an active or completed table order in the restaurant.
 * Status values: "ACTIVE", "KITCHEN", "SERVED", "COMPLETED", "CANCELLED"
 */
@Entity(tableName = "restaurant_orders")
data class RestaurantOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val tableId: Long,
    val tableName: String,
    val orderNumber: Int,
    val status: String = "ACTIVE",   // "ACTIVE", "KITCHEN", "SERVED", "COMPLETED", "CANCELLED"
    val customerName: String = "",
    val customerPhone: String = "",
    val guestCount: Int = 2,
    val subtotalPkr: Double = 0.0,
    val taxPercent: Double = 5.0,    // Standard GST / Provincial Sales Tax (e.g. 5%)
    val taxPkr: Double = 0.0,
    val discountPkr: Double = 0.0,
    val totalPkr: Double = 0.0,
    val paymentMethod: String = "UNPAID", // "CASH", "CARD", "JAZZCASH", "EASYPAISA", "UNPAID"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * Represents a single line item in an order, sent to the Kitchen Order Ticket (KOT).
 * kotStatus values: "PENDING", "PREPARING", "READY", "SERVED"
 */
@Entity(tableName = "restaurant_order_items")
data class RestaurantOrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val businessId: Long,
    val tableId: Long,
    val tableName: String,
    val menuItemId: Long,
    val itemName: String,
    val itemCategory: String,
    val unitPricePkr: Double,
    val quantity: Int = 1,
    val instructions: String = "",    // e.g. "Extra spicy, no raita", "Less sweet", "Hot & crispy"
    val kotStatus: String = "PENDING", // "PENDING", "PREPARING", "READY", "SERVED"
    val kotSentTime: Long = System.currentTimeMillis(),
    val kotCompletedTime: Long? = null
) {
    val lineTotalPkr: Double
        get() = unitPricePkr * quantity
}
