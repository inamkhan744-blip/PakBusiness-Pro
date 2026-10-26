package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "electronics_products")
data class ElectronicsProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val condition: String = "",
    val imei1: String = "",
    val imei2: String = "",
    val serialNumber: String = "",
    val ptaStatus: String = "",
    val storageSpecs: String = "",
    val color: String = "",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val stockQuantity: Int = 0,
    val warrantyType: String = "",
    val warrantyExpiryDate: String = "",
    val supplierName: String = "",
    val purchaseDate: String = "",
    val notes: String = ""
) {
    val isWarrantyActive: Boolean
        get() {
            if (warrantyExpiryDate.isBlank()) return false
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val exp = sdf.parse(warrantyExpiryDate)
                exp != null && exp.after(java.util.Date())
            } catch (e: Exception) {
                false
            }
        }
}

@Entity(tableName = "electronics_repair_tickets")
data class RepairTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val ticketNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deviceModel: String = "",
    val deviceColor: String = "",
    val imeiOrSerial: String = "",
    val devicePasscode: String = "",
    val problemDescription: String = "",
    val conditionNotes: String = "",
    val assignedTechnician: String = "",
    val status: String = "DIAGNOSING", // DIAGNOSING, IN_REPAIR, READY, DELIVERED, CANCELLED
    val estimatedCost: Double = 0.0,
    val advancePaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val receivedDate: String = "",
    val expectedDeliveryDate: String = "",
    val completedDate: String = "",
    val technicianRemarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// Legacy alias / backward-compatibility entity if referenced elsewhere
@Entity(tableName = "repair_jobs")
data class RepairJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val tokenNumber: String = "REP-${System.currentTimeMillis() % 100000}",
    val customerName: String = "",
    val customerPhone: String = "",
    val deviceBrand: String = "Samsung",
    val deviceModel: String = "Galaxy A32",
    val imeiOrSerial: String = "",
    val problemDescription: String = "Screen Broken",
    val status: String = "RECEIVED",
    val estimatedCostPkr: Double = 3500.0,
    val advancePaidPkr: Double = 1000.0,
    val deliveryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
