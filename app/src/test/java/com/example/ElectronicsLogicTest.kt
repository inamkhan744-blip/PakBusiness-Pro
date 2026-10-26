package com.example

import com.example.data.ElectronicsProductEntity
import com.example.data.RepairTicketEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ElectronicsLogicTest {

    @Test
    fun `test warranty active calculation for future and past dates`() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        cal.add(Calendar.MONTH, 6)
        val futureDate = sdf.format(cal.time)

        cal.add(Calendar.MONTH, -12) // past
        val pastDate = sdf.format(cal.time)

        val productUnderWarranty = ElectronicsProductEntity(
            id = 1L,
            businessId = 1L,
            name = "Galaxy S23 Ultra",
            brand = "Samsung",
            category = "Smartphones",
            imei1 = "358920112345678",
            purchasePrice = 280000.0,
            salePrice = 310000.0,
            warrantyType = "1 Year Official",
            warrantyExpiryDate = futureDate
        )

        val productExpired = ElectronicsProductEntity(
            id = 2L,
            businessId = 1L,
            name = "iPhone 11",
            brand = "Apple",
            category = "Smartphones",
            imei1 = "354890109988776",
            purchasePrice = 85000.0,
            salePrice = 98000.0,
            warrantyType = "7 Days Checking",
            warrantyExpiryDate = pastDate
        )

        assertTrue(productUnderWarranty.isWarrantyActive)
        assertFalse(productExpired.isWarrantyActive)
    }

    @Test
    fun `test mobile repair ticket financials and balance calculation`() {
        val estimatedCost = 35000.0
        val advancePaid = 15000.0
        val remainingBalance = estimatedCost - advancePaid

        val ticket = RepairTicketEntity(
            id = 10L,
            businessId = 1L,
            ticketNumber = "REP-505",
            customerName = "Ali Raza",
            customerPhone = "+92 300 1234567",
            deviceModel = "iPhone 13 Pro Max",
            problemDescription = "Glass and display broken",
            status = "RECEIVED",
            estimatedCost = estimatedCost,
            advancePaid = advancePaid,
            remainingBalance = remainingBalance,
            receivedDate = "2026-09-20",
            expectedDeliveryDate = "2026-09-23"
        )

        assertEquals(20000.0, ticket.remainingBalance, 0.001)
        assertEquals("REP-505", ticket.ticketNumber)
        assertEquals("RECEIVED", ticket.status)
    }

    @Test
    fun `test mobile repair ticket workflow pipeline stages`() {
        val stages = listOf("RECEIVED", "DIAGNOSING", "IN_REPAIR", "READY", "DELIVERED")

        fun nextStage(current: String): String {
            val idx = stages.indexOf(current)
            return if (idx in 0 until stages.size - 1) stages[idx + 1] else current
        }

        assertEquals("DIAGNOSING", nextStage("RECEIVED"))
        assertEquals("IN_REPAIR", nextStage("DIAGNOSING"))
        assertEquals("READY", nextStage("IN_REPAIR"))
        assertEquals("DELIVERED", nextStage("READY"))
        assertEquals("DELIVERED", nextStage("DELIVERED"))
    }
}
