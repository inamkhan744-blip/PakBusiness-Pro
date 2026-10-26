package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wholesale_parties",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["partyType"]),
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class WholesalePartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String, // e.g. "Madina Traders Lahore", "Bismillah Whole-Sellers", "Al-Rehman Mills"
    val contactPerson: String = "",
    val phone: String, // e.g. "+92 300 1234567"
    val address: String = "Circular Road, Urdu Bazar, Lahore",
    val city: String = "Lahore",
    val partyType: String = "CUSTOMER", // "CUSTOMER", "SUPPLIER", "BOTH"
    val creditLimit: Double = 500000.0, // Credit limit in PKR
    val currentBalance: Double = 0.0, // Positive = Receivable (They owe us), Negative = Payable (We owe them)
    val taxNtnNumber: String = "", // NTN / STRN tax registration
    val paymentTermsDays: Int = 15, // 7 days, 15 days, 30 days credit
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "wholesale_bulk_orders",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["orderNumber"]),
        Index(value = ["partyId"]),
        Index(value = ["orderType"]),
        Index(value = ["status"])
    ]
)
data class WholesaleBulkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val orderNumber: String, // e.g. "WO-2026-001"
    val partyId: Long,
    val partyName: String,
    val partyPhone: String,
    val orderType: String = "SALE", // "SALE" (Wholesale Sale to Customer), "PURCHASE" (Bulk Purchase from Supplier)
    val status: String = "CONFIRMED", // "DRAFT", "CONFIRMED", "DISPATCHED", "DELIVERED", "CANCELLED"
    val orderDate: String, // "YYYY-MM-DD"
    val deliveryDate: String = "",
    val vehicleNumberOrBilty: String = "", // Bilty / Goods transport # e.g. "Faisal Movers Cargo #4512"
    val itemsSummary: String = "", // e.g. "Basmati Rice 50kg x 40 Bags, Dal Chana 25kg x 20 Sacks"
    val totalUnitsCount: Int = 0, // Total cartoons / sacks / crates
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxGst: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val creditBalanceAdded: Double = grandTotal - paidAmount,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "wholesale_payments",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["partyId"]),
        Index(value = ["paymentDate"]),
        Index(value = ["voucherNumber"])
    ]
)
data class WholesalePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val voucherNumber: String, // e.g. "RV-891" or "PV-452"
    val partyId: Long,
    val partyName: String,
    val paymentType: String, // "RECEIPT" (Cash In from Customer), "PAYMENT" (Cash Out to Supplier)
    val amount: Double,
    val paymentMethod: String = "Cash", // "Cash", "Online Bank Transfer (Meezan/HBL)", "Cheque", "EasyPaisa / JazzCash"
    val bankAccountOrChequeNo: String = "", // e.g. "Meezan Bank Chq #901248"
    val paymentDate: String, // "YYYY-MM-DD"
    val notes: String = "",
    val balanceAfterPayment: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
