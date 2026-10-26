package com.example.ui.screens.gym

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GymDateUtils {
    fun formatDate(millis: Long): String {
        if (millis <= 0) return "N/A"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        if (millis <= 0) return "N/A"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        if (millis <= 0) return "N/A"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatCurrency(amount: Double): String {
        return "₨ ${"%,.0f".format(amount)} PKR"
    }
}
