package com.example.ui.screens.realestate

import java.text.NumberFormat
import java.util.Locale

object RealEstateFormatters {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun formatPkrFull(amount: Double): String {
        return "PKR ${numberFormat.format(amount)}"
    }

    fun formatPkrShort(amount: Double): String {
        return when {
            amount >= 10_000_000 -> {
                val crore = amount / 10_000_000.0
                if (crore == crore.toLong().toDouble()) {
                    "${crore.toLong()} Crore"
                } else {
                    String.format(Locale.US, "%.2f Crore", crore)
                }
            }
            amount >= 100_000 -> {
                val lakh = amount / 100_000.0
                if (lakh == lakh.toLong().toDouble()) {
                    "${lakh.toLong()} Lakh"
                } else {
                    String.format(Locale.US, "%.2f Lakh", lakh)
                }
            }
            amount >= 1_000 -> {
                val thousand = amount / 1_000.0
                String.format(Locale.US, "%.1fk", thousand)
            }
            else -> numberFormat.format(amount)
        }
    }

    fun formatPkrCombined(amount: Double, purpose: String = "SALE"): String {
        val shortStr = formatPkrShort(amount)
        val suffix = if (purpose.equals("RENT", ignoreCase = true)) "/mo" else ""
        return if (amount >= 100_000) {
            "PKR ${numberFormat.format(amount)} ($shortStr$suffix)"
        } else {
            "PKR ${numberFormat.format(amount)}$suffix"
        }
    }

    fun formatBudgetRange(min: Double, max: Double): String {
        return when {
            min > 0 && max > 0 -> {
                "${formatPkrShort(min)} - ${formatPkrShort(max)}"
            }
            max > 0 -> {
                "Up to ${formatPkrShort(max)}"
            }
            min > 0 -> {
                "From ${formatPkrShort(min)}"
            }
            else -> "Budget Flexible"
        }
    }
}
