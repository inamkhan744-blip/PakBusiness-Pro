package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_payments")
data class GymPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val memberId: Long = 0,
    val memberName: String = "",
    val amountPkr: Double = 0.0,
    val paymentType: String = "Monthly Fee", // Monthly Fee, Admission Fee, Locker Fee, PT Fee
    val paymentMethod: String = "Cash",     // Cash, EasyPaisa, JazzCash, Bank Transfer
    val paymentDate: Long = System.currentTimeMillis(),
    val receiptNumber: String = "GYM-REC-${System.currentTimeMillis() % 100000}",
    val notes: String = ""
)

@Entity(tableName = "gym_lockers")
data class GymLockerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val lockerNumber: String,
    val assignedMemberId: Long? = null,
    val assignedMemberName: String = "",
    val monthlyFeePkr: Double = 500.0,
    val isOccupied: Boolean = false,
    val expiryDate: Long? = null
)
