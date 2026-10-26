package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Core multi-tenant entity representing business contacts, customers, suppliers, and ledger accounts.
 */
@Entity(
    tableName = "parties",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["partyType"]),
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val partyType: String = "CUSTOMER", // "CUSTOMER", "SUPPLIER", "VENDOR", "PARTNER"
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val currentBalance: Double = 0.0, // Khata balance: +ve = Receivable, -ve = Payable
    val creditLimit: Double = 0.0,
    val ntnOrTaxId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Core multi-tenant entity representing inventory stock and product catalog.
 */
@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["sku"]),
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val unit: String = "Pcs", // Pcs, Kg, Litre, Meter, Box, Pack, Dozen
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val minThreshold: Double = 5.0,
    val location: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Core multi-tenant entity representing sales, purchases, and invoices.
 */
@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["orderNumber"]),
        Index(value = ["partyId"]),
        Index(value = ["orderType"]),
        Index(value = ["status"]),
        Index(value = ["orderDate"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val orderNumber: String,
    val partyId: Long? = null,
    val partyName: String = "Walk-in Customer",
    val partyPhone: String = "",
    val orderType: String = "SALE", // "SALE", "PURCHASE", "RETURN", "ESTIMATE"
    val status: String = "COMPLETED", // "PENDING", "PROCESSING", "COMPLETED", "CANCELLED"
    val orderDate: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val paymentMode: String = "CASH", // "CASH", "BANK_TRANSFER", "JAZZCASH", "EASYPAISA", "CREDIT"
    val paymentStatus: String = "PAID", // "PAID", "PARTIAL", "UNPAID"
    val itemsCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Order item detail record linking an order to specific products/quantities.
 */
@Entity(
    tableName = "order_items",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["inventoryItemId"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val inventoryItemId: Long? = null,
    val itemName: String,
    val sku: String = "",
    val unit: String = "Pcs",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val discount: Double = 0.0,
    val totalPrice: Double = 0.0,
    val notes: String = ""
)

/**
 * Composite relation holding an order together with all its line items.
 */
data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)
