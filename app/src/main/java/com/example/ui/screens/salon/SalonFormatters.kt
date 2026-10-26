package com.example.ui.screens.salon

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

fun formatSalonPkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    formatter.maximumFractionDigits = 0
    return "PKR ${formatter.format(amount)}"
}

object SalonColors {
    val HairColor = Color(0xFF673AB7) // Purple
    val HairContainer = Color(0xFFEDE7F6)
    val HairOnContainer = Color(0xFF311B92)

    val SkinColor = Color(0xFFD81B60) // Rose / Pink
    val SkinContainer = Color(0xFFFCE4EC)
    val SkinOnContainer = Color(0xFF880E4F)

    val SpaColor = Color(0xFF00897B) // Teal
    val SpaContainer = Color(0xFFE0F2F1)
    val SpaOnContainer = Color(0xFF004D40)

    val GoldAccent = Color(0xFFF59E0B)
    val EmeraldAccent = Color(0xFF10B981)
}

fun getServiceCategoryColors(category: String): Triple<Color, Color, Color> {
    return when (category.uppercase()) {
        "HAIR" -> Triple(SalonColors.HairColor, SalonColors.HairContainer, SalonColors.HairOnContainer)
        "SKIN" -> Triple(SalonColors.SkinColor, SalonColors.SkinContainer, SalonColors.SkinOnContainer)
        "SPA" -> Triple(SalonColors.SpaColor, SalonColors.SpaContainer, SalonColors.SpaOnContainer)
        else -> Triple(Color(0xFF455A64), Color(0xFFECEFF1), Color(0xFF263238))
    }
}

fun getAppointmentStatusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "SCHEDULED" -> Pair(Color(0xFF1976D2), Color(0xFFE3F2FD))
        "IN_PROGRESS" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0))
        "COMPLETED" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
        "CANCELLED" -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE))
        else -> Pair(Color(0xFF546E7A), Color(0xFFECEFF1))
    }
}
