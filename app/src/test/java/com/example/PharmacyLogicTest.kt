package com.example

import com.example.data.MedicineEntity
import com.example.data.PharmacyCartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PharmacyLogicTest {

    @Test
    fun `test medicine expiry logic`() {
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000

        // 1. Expired medicine (expired 5 days ago)
        val expiredMed = MedicineEntity(
            businessId = 1L,
            name = "Panadol 500mg",
            genericName = "Paracetamol",
            category = "Tablet",
            batchNumber = "BATCH-001",
            rackNumber = "Rack A-1",
            purchasePrice = 10.0,
            salePrice = 15.0,
            quantity = 50,
            minStockAlert = 10,
            expiryDate = now - (5 * dayMillis)
        )
        assertTrue(expiredMed.isExpired(now))
        assertEquals(-5, expiredMed.daysUntilExpiry(now))

        // 2. Expiring soon medicine (expiring in 10 days)
        val expiringMed = MedicineEntity(
            businessId = 1L,
            name = "Augmentin 625mg",
            genericName = "Amoxicillin / Clavulanate",
            category = "Tablet",
            batchNumber = "BATCH-002",
            rackNumber = "Rack B-2",
            purchasePrice = 180.0,
            salePrice = 240.0,
            quantity = 20,
            minStockAlert = 5,
            expiryDate = now + (10 * dayMillis)
        )
        assertFalse(expiringMed.isExpired(now))
        assertTrue(expiringMed.isExpiringSoon(days = 30, now = now))
        assertEquals(10, expiringMed.daysUntilExpiry(now))

        // 3. Healthy valid medicine (expiring in 365 days)
        val healthyMed = MedicineEntity(
            businessId = 1L,
            name = "Disprin 300mg",
            genericName = "Aspirin",
            category = "Tablet",
            batchNumber = "BATCH-003",
            rackNumber = "Rack A-3",
            purchasePrice = 5.0,
            salePrice = 8.0,
            quantity = 100,
            minStockAlert = 10,
            expiryDate = now + (365 * dayMillis)
        )
        assertFalse(healthyMed.isExpired(now))
        assertFalse(healthyMed.isExpiringSoon(days = 30, now = now))
    }

    @Test
    fun `test low stock alert check`() {
        val lowStockMed = MedicineEntity(
            businessId = 1L,
            name = "Brufen 100mg/5ml",
            genericName = "Ibuprofen",
            category = "Syrup",
            batchNumber = "BATCH-004",
            rackNumber = "Shelf 1",
            purchasePrice = 60.0,
            salePrice = 85.0,
            quantity = 3,
            minStockAlert = 10,
            expiryDate = System.currentTimeMillis() + 100000000L
        )
        assertTrue(lowStockMed.isLowStock())

        val sufficientStockMed = lowStockMed.copy(quantity = 25)
        assertFalse(sufficientStockMed.isLowStock())
    }

    @Test
    fun `test cart item calculation`() {
        val med = MedicineEntity(
            id = 10L,
            businessId = 1L,
            name = "Flagyl 400mg",
            genericName = "Metronidazole",
            category = "Tablet",
            batchNumber = "FL-991",
            rackNumber = "Rack C-1",
            purchasePrice = 12.0,
            salePrice = 20.0,
            quantity = 100,
            minStockAlert = 10,
            expiryDate = System.currentTimeMillis() + 100000000L
        )

        val cartItem = PharmacyCartItem(
            medicine = med,
            quantity = 5
        )

        assertEquals(100.0, cartItem.subtotal, 0.001)
    }
}
