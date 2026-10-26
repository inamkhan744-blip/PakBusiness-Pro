package com.example.ui.screens.hotel

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

fun formatHotelPkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    formatter.maximumFractionDigits = 0
    return "PKR ${formatter.format(amount)}"
}

object HotelColors {
    val Available = Color(0xFF10B981) // Emerald Green
    val AvailableContainer = Color(0xFFE8F5E9)
    val AvailableOnContainer = Color(0xFF1B5E20)

    val Booked = Color(0xFFE11D48) // Rose Red
    val BookedContainer = Color(0xFFFFEBEE)
    val BookedOnContainer = Color(0xFFB71C1C)

    val Cleaning = Color(0xFFF59E0B) // Amber
    val CleaningContainer = Color(0xFFFFF8E1)
    val CleaningOnContainer = Color(0xFFB45309)

    val GoldAccent = Color(0xFFD97706)
}

fun getRoomStatusColors(status: String): Triple<Color, Color, Color> {
    return when (status.uppercase()) {
        "AVAILABLE" -> Triple(HotelColors.Available, HotelColors.AvailableContainer, HotelColors.AvailableOnContainer)
        "BOOKED" -> Triple(HotelColors.Booked, HotelColors.BookedContainer, HotelColors.BookedOnContainer)
        "CLEANING" -> Triple(HotelColors.Cleaning, HotelColors.CleaningContainer, HotelColors.CleaningOnContainer)
        else -> Triple(Color(0xFF64748B), Color(0xFFF1F5F9), Color(0xFF334155))
    }
}

fun getBookingStatusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "CHECKED_IN" -> Pair(Color(0xFF059669), Color(0xFFD1FAE5))
        "CONFIRMED" -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))
        "CHECKED_OUT" -> Pair(Color(0xFF475569), Color(0xFFF1F5F9))
        "CANCELLED" -> Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))
        else -> Pair(Color(0xFF64748B), Color(0xFFF1F5F9))
    }
}
