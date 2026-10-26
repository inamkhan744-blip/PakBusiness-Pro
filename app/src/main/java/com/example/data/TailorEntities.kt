package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tailor_customers",
    indices = [Index(value = ["businessId", "phone"])]
)
data class TailorCustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val gender: String = "Gents / Men's", // "Gents / Men's", "Ladies / Boutique", "Kids"
    val city: String = "Lahore",
    val notes: String = "",
    val totalOrders: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tailor_measurements",
    indices = [Index(value = ["businessId", "customerId"])]
)
data class TailorMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val customerId: Long,
    val customerName: String,
    val profileTitle: String = "Standard Shalwar Kameez", // e.g. "Kurta Pajama", "Waistcoat", "Ladies 3-Piece"
    val gender: String = "Gents", // "Gents", "Ladies"
    
    // Kameez / Shirt / Top Measurements (Inches)
    val length: Double = 40.0, // Kameez / Shirt Length (Lambai)
    val chest: Double = 38.0, // Chest (Chhati)
    val waist: Double = 34.0, // Waist (Kamar)
    val hip: Double = 40.0, // Hip / Ghera
    val shoulder: Double = 17.5, // Shoulder (Teera)
    val sleeves: Double = 23.5, // Sleeves length (Bazu)
    val collar: Double = 15.5, // Neck / Collar (Gala)
    val daman: Double = 22.0, // Daman width
    val cuff: Double = 9.0, // Cuff (Kaf)
    val armhole: Double = 9.5, // Armhole (Mora)
    
    // Shalwar / Trouser / Bottom Measurements (Inches)
    val shalwarLength: Double = 38.0, // Shalwar / Trouser Length
    val paincha: Double = 7.5, // Paincha / Bottom width
    val asan: Double = 16.0, // Crotch (Asan)
    val trouserWaist: Double = 34.0, // Trouser Waist
    
    // Style Options
    val collarType: String = "Sherwani Ban", // "Sherwani Ban", "Shirt Collar", "V-Neck", "Gol Gala (Round)", "Cut Ban"
    val pocketStyle: String = "1 Front + 2 Side", // "1 Front + 2 Side", "Front Pocket Only", "Side Pockets Only", "Hidden Pocket", "None"
    val cuffStyle: String = "Single Kaf", // "Single Kaf", "Double Kaf", "Open Bazu (Gol)", "Button Kaf"
    val damanStyle: String = "Chauras (Square)", // "Chauras (Square)", "Gol Daman (Round)"
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tailor_orders",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["customerId"]),
        Index(value = ["status"])
    ]
)
data class TailorOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val orderNumber: String, // e.g. "#TK-101"
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val suitType: String = "Men's Shalwar Kameez", // "Men's Shalwar Kameez", "Designer Kurta", "Waistcoat / Wasket", "2-Piece Suit", "Sherwani", "Ladies 3-Piece Suit", "Party Wear Maxi", "Kids Kurta"
    val quantity: Int = 1,
    val fabricDetails: String = "", // e.g. "Customer Egyptian Cotton Navy Blue", "Charcoal Grey Boski"
    val orderDate: String, // "YYYY-MM-DD"
    val deliveryDate: String, // "YYYY-MM-DD"
    val status: String = "CUTTING", // "CUTTING", "STITCHING", "READY", "DELIVERED", "CANCELLED"
    val stitchingRate: Double = 1500.0,
    val totalAmount: Double = 1500.0,
    val advancePaid: Double = 500.0,
    val remainingBalance: Double = 1000.0,
    val specialInstructions: String = "",
    val assignedMaster: String = "Master Rafique",
    val measurementSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
