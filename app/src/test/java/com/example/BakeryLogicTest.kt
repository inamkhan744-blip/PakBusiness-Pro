package com.example

import com.example.data.BakeryCakeOrderEntity
import com.example.data.BakeryItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BakeryLogicTest {

    @Test
    fun `test custom cake order financial calculation and status`() {
        val total = 5500.0
        val advance = 2000.0
        val balance = total - advance

        val cakeOrder = BakeryCakeOrderEntity(
            id = 1L,
            businessId = 1L,
            orderNumber = "CAKE-777",
            customerName = "Hassan Ali",
            customerPhone = "+92 300 9876543",
            occasion = "Wedding",
            flavor = "Red Velvet & Cream Cheese",
            weightLbs = 4.5,
            spongeType = "Eggless",
            messageOnCake = "Congratulations Ali & Fatima",
            customDesignNotes = "3-Tier with floral sugar roses",
            deliveryDate = "2026-09-28",
            deliveryTime = "07:30 PM",
            status = "RECEIVED",
            totalPrice = total,
            advancePaid = advance,
            remainingBalance = balance
        )

        assertEquals(3500.0, cakeOrder.remainingBalance, 0.001)
        assertEquals("Wedding", cakeOrder.occasion)
        assertEquals("Eggless", cakeOrder.spongeType)
        assertEquals("RECEIVED", cakeOrder.status)
        assertEquals(4.5, cakeOrder.weightLbs, 0.001)
    }

    @Test
    fun `test cake order status workflow progression`() {
        val pipeline = listOf("RECEIVED", "BAKING", "DECORATING", "READY", "DELIVERED")

        fun getNextStatus(current: String): String {
            val idx = pipeline.indexOf(current)
            return if (idx in 0 until pipeline.size - 1) pipeline[idx + 1] else current
        }

        assertEquals("BAKING", getNextStatus("RECEIVED"))
        assertEquals("DECORATING", getNextStatus("BAKING"))
        assertEquals("READY", getNextStatus("DECORATING"))
        assertEquals("DELIVERED", getNextStatus("READY"))
        assertEquals("DELIVERED", getNextStatus("DELIVERED"))
    }

    @Test
    fun `test bakery product production and expiry dates`() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        cal.add(Calendar.DAY_OF_YEAR, -5)
        val pastDate = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 15)
        val futureDate = sdf.format(cal.time)

        val freshItem = BakeryItemEntity(
            id = 1L,
            businessId = 1L,
            name = "Motichoor Ladoo",
            category = "Mithai / Sweets",
            unit = "kg",
            pricePerUnit = 1100.0,
            currentStock = 25.0,
            productionDate = sdf.format(Calendar.getInstance().time),
            expiryDate = futureDate,
            batchNumber = "BATCH-001"
        )

        val expiredItem = BakeryItemEntity(
            id = 2L,
            businessId = 1L,
            name = "Fresh Cream Pastry",
            category = "Pastries & Cakes",
            unit = "piece",
            pricePerUnit = 180.0,
            currentStock = 8.0,
            productionDate = pastDate,
            expiryDate = pastDate,
            batchNumber = "BATCH-OLD"
        )

        assertFalse(freshItem.isExpired)
        assertTrue(expiredItem.isExpired)
        assertFalse(freshItem.isLowStock)
    }
}
