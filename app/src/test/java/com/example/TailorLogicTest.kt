package com.example

import com.example.data.TailorCustomerEntity
import com.example.data.TailorMeasurementEntity
import com.example.data.TailorOrderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TailorLogicTest {

    @Test
    fun `test tailor order remaining balance calculation`() {
        val totalAmount = 4500.0
        val advancePaid = 2000.0
        val remainingBalance = totalAmount - advancePaid

        val order = TailorOrderEntity(
            id = 1L,
            businessId = 1L,
            orderNumber = "ORD-101",
            customerId = 1L,
            customerName = "Mohammad Usman",
            customerPhone = "+92 300 1234567",
            suitType = "Shalwar Kameez",
            status = "CUTTING",
            totalAmount = totalAmount,
            advancePaid = advancePaid,
            remainingBalance = remainingBalance,
            orderDate = "2026-09-20",
            deliveryDate = "2026-09-25",
            specialInstructions = "Ban collar, double pocket"
        )

        assertEquals(2500.0, order.remainingBalance, 0.001)
        assertEquals("CUTTING", order.status)
        assertEquals("Shalwar Kameez", order.suitType)
    }

    @Test
    fun `test status progression cutting to stitching to ready to delivered`() {
        val statuses = listOf("CUTTING", "STITCHING", "READY", "DELIVERED")

        assertEquals(0, statuses.indexOf("CUTTING"))
        assertEquals(1, statuses.indexOf("STITCHING"))
        assertEquals(2, statuses.indexOf("READY"))
        assertEquals(3, statuses.indexOf("DELIVERED"))

        fun getNextStatus(current: String): String {
            val idx = statuses.indexOf(current)
            return if (idx in 0 until statuses.size - 1) statuses[idx + 1] else current
        }

        assertEquals("STITCHING", getNextStatus("CUTTING"))
        assertEquals("READY", getNextStatus("STITCHING"))
        assertEquals("DELIVERED", getNextStatus("READY"))
        assertEquals("DELIVERED", getNextStatus("DELIVERED"))
    }

    @Test
    fun `test customer measurements capture key parameters`() {
        val measurement = TailorMeasurementEntity(
            id = 1L,
            businessId = 1L,
            customerId = 1L,
            customerName = "Ali Raza",
            profileTitle = "Standard Shalwar Kameez",
            length = 41.5,
            chest = 39.0,
            waist = 36.0,
            shoulder = 18.5,
            sleeves = 24.0,
            collar = 16.0,
            shalwarLength = 38.5,
            paincha = 8.5,
            notes = "Round Daman, double stitches"
        )

        assertEquals(41.5, measurement.length, 0.001)
        assertEquals(39.0, measurement.chest, 0.001)
        assertEquals(36.0, measurement.waist, 0.001)
        assertEquals(18.5, measurement.shoulder, 0.001)
        assertEquals(24.0, measurement.sleeves, 0.001)
        assertEquals(16.0, measurement.collar, 0.001)
        assertEquals(38.5, measurement.shalwarLength, 0.001)
        assertEquals(8.5, measurement.paincha, 0.001)
    }
}
