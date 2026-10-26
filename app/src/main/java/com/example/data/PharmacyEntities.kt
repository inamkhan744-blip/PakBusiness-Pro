package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(
    tableName = "pharmacy_medicines",
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
        Index(value = ["name"]),
        Index(value = ["batchNumber"]),
        Index(value = ["expiryDate"])
    ]
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,                  // e.g. "Panadol 500mg"
    val genericName: String = "",       // e.g. "Paracetamol"
    val category: String = "Tablet",    // Tablet, Capsule, Syrup, Suspension, Injection, Ointment, Drops, Sachet, Inhaler
    val batchNumber: String,           // e.g. "BT-2026-08"
    val expiryDate: Long,              // Milliseconds timestamp
    val rackNumber: String = "Rack A-1",// e.g. "Rack A-1", "Shelf 2", "Fridge"
    val purchasePrice: Double = 0.0,   // Cost price in PKR
    val salePrice: Double,             // Retail sale price in PKR
    val quantity: Int = 0,             // Current in-stock quantity
    val minStockAlert: Int = 10,       // Threshold for low stock warning
    val supplierName: String = "",     // e.g. "Searle Pakistan Ltd."
    val supplierPhone: String = "",    // e.g. "0300-1234567"
    val supplierInvoiceRef: String = "",// e.g. "INV-9921"
    val dosageInstructions: String = "",// e.g. "Take after meal"
    val notes: String = "",            // Extra details / storage warnings
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return expiryDate <= now
    }

    fun isExpiringSoon(days: Int = 30, now: Long = System.currentTimeMillis()): Boolean {
        return !isExpired(now) && expiryDate <= (now + days * 24L * 60 * 60 * 1000L)
    }

    fun isLowStock(): Boolean {
        return quantity <= minStockAlert
    }

    fun daysUntilExpiry(now: Long = System.currentTimeMillis()): Long {
        val diff = expiryDate - now
        return diff / (24L * 60 * 60 * 1000L)
    }
}

@Entity(
    tableName = "pharmacy_sales",
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
        Index(value = ["saleDate"])
    ]
)
data class PharmacySaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val invoiceNumber: String,          // e.g. "PH-20260919-001"
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val doctorPrescriber: String = "",  // Prescribing doctor name if applicable
    val totalAmount: Double,            // Gross sum in PKR
    val discount: Double = 0.0,         // Discount in PKR
    val netAmount: Double,              // Net paid in PKR
    val paymentMethod: String = "Cash", // Cash, EasyPaisa, JazzCash, Card
    val saleDate: Long = System.currentTimeMillis(),
    val itemsJson: String = "[]",       // JSON serialized list of sold items
    val notes: String = ""
) {
    fun parseItems(): List<PharmacySoldItem> {
        return try {
            val array = JSONArray(itemsJson)
            val list = mutableListOf<PharmacySoldItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PharmacySoldItem(
                        medicineId = obj.optLong("medicineId", 0L),
                        name = obj.optString("name", ""),
                        batchNumber = obj.optString("batchNumber", ""),
                        rackNumber = obj.optString("rackNumber", ""),
                        quantity = obj.optInt("quantity", 1),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        subtotal = obj.optDouble("subtotal", 0.0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun encodeItems(items: List<PharmacySoldItem>): String {
            return try {
                val array = JSONArray()
                for (item in items) {
                    val obj = JSONObject().apply {
                        put("medicineId", item.medicineId)
                        put("name", item.name)
                        put("batchNumber", item.batchNumber)
                        put("rackNumber", item.rackNumber)
                        put("quantity", item.quantity)
                        put("unitPrice", item.unitPrice)
                        put("subtotal", item.subtotal)
                    }
                    array.put(obj)
                }
                array.toString()
            } catch (e: Exception) {
                "[]"
            }
        }
    }
}

data class PharmacySoldItem(
    val medicineId: Long,
    val name: String,
    val batchNumber: String,
    val rackNumber: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

data class PharmacyCartItem(
    val medicine: MedicineEntity,
    var quantity: Int = 1
) {
    val subtotal: Double
        get() = quantity * medicine.salePrice
}
