package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_members")
data class GymMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val name: String,
    val phone: String,
    val gender: String, // "Male", "Female", "Other"
    val plan: String,   // "Monthly Standard", "Quarterly (3 Mos)", "Semi-Annual (6 Mos)", "Annual VIP", "CrossFit & Cardio"
    val startDate: Long = System.currentTimeMillis(),
    val durationDays: Int = 30,
    val amountPkr: Double = 3000.0,
    val admissionFeePkr: Double = 0.0,
    val pendingDuePkr: Double = 0.0,
    val fitnessGoal: String = "",
    val workoutPlan: String = "",
    val dietPlan: String = "",
    val lockerNumber: String = "",
    val assignedTrainer: String = "",
    val isCheckedIn: Boolean = false,
    val lastCheckInTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val expiryDate: Long
        get() = startDate + (durationDays.toLong() * 24L * 60L * 60L * 1000L)

    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiryDate

    val isExpiringSoon: Boolean
        get() {
            val now = System.currentTimeMillis()
            val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L
            return !isExpired && (expiryDate - now) <= sevenDaysMillis
        }

    val remainingDays: Long
        get() {
            val diff = expiryDate - System.currentTimeMillis()
            val dayMillis = 24L * 60L * 60L * 1000L
            return if (diff > 0) (diff + dayMillis - 1L) / dayMillis else 0L
        }
}
