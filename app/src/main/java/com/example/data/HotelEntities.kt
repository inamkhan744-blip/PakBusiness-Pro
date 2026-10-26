package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hotel_rooms",
    indices = [Index(value = ["businessId", "roomNumber"], unique = true)]
)
data class HotelRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val roomNumber: String,
    val roomType: String, // "Single", "Double", "Deluxe", "Executive Suite", "Family Suite"
    val floor: String, // "Ground Floor", "1st Floor", "2nd Floor", "3rd Floor"
    val pricePerNight: Double,
    val status: String = "AVAILABLE", // "AVAILABLE", "BOOKED", "CLEANING"
    val amenities: String = "AC, Wi-Fi, Smart TV, Hot Water",
    val currentBookingId: Long? = null
)

@Entity(
    tableName = "hotel_bookings",
    indices = [Index(value = ["businessId"]), Index(value = ["roomId"])]
)
data class HotelBookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val roomId: Long,
    val roomNumber: String,
    val roomType: String,
    val guestName: String,
    val guestPhone: String,
    val guestCnic: String, // Pakistani National Identity Card (e.g. 35201-1234567-1)
    val guestCity: String = "Lahore",
    val checkInDate: String, // "YYYY-MM-DD"
    val checkOutDate: String, // "YYYY-MM-DD"
    val numberOfNights: Int = 1,
    val pricePerNight: Double,
    val totalAmount: Double,
    val advancePaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val status: String = "CONFIRMED", // "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"
    val specialRequests: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "hotel_guests",
    indices = [Index(value = ["businessId", "cnic"], unique = true)]
)
data class HotelGuestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val phone: String,
    val cnic: String,
    val city: String = "Islamabad",
    val totalStays: Int = 1,
    val totalSpent: Double = 0.0,
    val lastStayDate: String = ""
)
