package com.example

import com.example.data.GymMemberEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GymLogicTest {

    @Test
    fun `test member active and expiry calculation`() {
        val now = System.currentTimeMillis()
        val activeMember = GymMemberEntity(
            id = 1L,
            businessId = 1L,
            name = "Hamza Ali",
            phone = "+92 300 1234567",
            gender = "Male",
            plan = "Monthly Standard",
            startDate = now,
            durationDays = 30,
            amountPkr = 3500.0,
            isCheckedIn = false
        )

        assertFalse(activeMember.isExpired)
        assertTrue(activeMember.remainingDays in 29..31)
    }

    @Test
    fun `test expired member calculation`() {
        val pastDate = System.currentTimeMillis() - (40L * 24 * 60 * 60 * 1000)
        val expiredMember = GymMemberEntity(
            id = 2L,
            businessId = 1L,
            name = "Bilal Khan",
            phone = "+92 321 7654321",
            gender = "Male",
            plan = "Monthly Standard",
            startDate = pastDate,
            durationDays = 30,
            amountPkr = 3500.0,
            isCheckedIn = false
        )

        assertTrue(expiredMember.isExpired)
        assertEquals(0, expiredMember.remainingDays)
    }

    @Test
    fun `test expiring soon calculation`() {
        // Started 26 days ago for a 30-day plan -> 4 days remaining (expiring soon)
        val pastDate = System.currentTimeMillis() - (26L * 24 * 60 * 60 * 1000)
        val expiringMember = GymMemberEntity(
            id = 3L,
            businessId = 1L,
            name = "Sara Tariq",
            phone = "+92 333 9988776",
            gender = "Female",
            plan = "Monthly Standard",
            startDate = pastDate,
            durationDays = 30,
            amountPkr = 3500.0,
            isCheckedIn = false
        )

        assertFalse(expiringMember.isExpired)
        assertTrue(expiringMember.isExpiringSoon)
        assertTrue(expiringMember.remainingDays in 3..4)
    }
}
