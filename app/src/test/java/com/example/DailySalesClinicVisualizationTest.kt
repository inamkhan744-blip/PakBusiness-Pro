package com.example

import com.example.data.AppointmentEntity
import com.example.data.OrderEntity
import com.example.ui.components.DailySalesClinicDataPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailySalesClinicVisualizationTest {

    @Test
    fun testDataPointCreationAndMetrics() {
        val point = DailySalesClinicDataPoint(
            dayLabel = "Mon 10",
            dateIso = "2026-10-10",
            timestamp = 1791638400000L,
            salesPkr = 45000.0,
            appointmentsCount = 12,
            appointmentsFeePkr = 18000.0
        )

        assertEquals("Mon 10", point.dayLabel)
        assertEquals("2026-10-10", point.dateIso)
        assertEquals(45000.0, point.salesPkr, 0.01)
        assertEquals(12, point.appointmentsCount)
        assertEquals(18000.0, point.appointmentsFeePkr, 0.01)
        assertEquals(63000.0, point.totalRevenuePkr, 0.01)
    }

    @Test
    fun testOrderAndAppointmentAggregation() {
        val fakeOrders = listOf(
            OrderEntity(
                businessId = 1L,
                orderNumber = "ORD-001",
                orderType = "SALE",
                totalAmount = 25000.0,
                orderDate = System.currentTimeMillis()
            ),
            OrderEntity(
                businessId = 1L,
                orderNumber = "ORD-002",
                orderType = "SALE",
                totalAmount = 15000.0,
                orderDate = System.currentTimeMillis()
            )
        )

        val fakeAppointments = listOf(
            AppointmentEntity(
                businessId = 1L,
                patientId = 101L,
                patientName = "Muhammad Ali",
                patientPhone = "03001234567",
                doctorId = 201L,
                doctorName = "Dr. Farooq",
                doctorSpecialization = "Cardiology",
                appointmentDate = System.currentTimeMillis(),
                timeSlot = "05:00 PM",
                tokenNumber = 1,
                consultationFeePkr = 2000.0,
                status = "SCHEDULED"
            ),
            AppointmentEntity(
                businessId = 1L,
                patientId = 102L,
                patientName = "Ayesha Khan",
                patientPhone = "03019876543",
                doctorId = 201L,
                doctorName = "Dr. Farooq",
                doctorSpecialization = "Cardiology",
                appointmentDate = System.currentTimeMillis(),
                timeSlot = "05:30 PM",
                tokenNumber = 2,
                consultationFeePkr = 2000.0,
                status = "SCHEDULED"
            )
        )

        val totalSales = fakeOrders.sumOf { it.totalAmount }
        val totalConsultationFee = fakeAppointments.sumOf { it.consultationFeePkr }
        val grandTotal = totalSales + totalConsultationFee

        assertEquals(40000.0, totalSales, 0.01)
        assertEquals(4000.0, totalConsultationFee, 0.01)
        assertEquals(44000.0, grandTotal, 0.01)
        assertEquals(2, fakeAppointments.size)
    }
}
