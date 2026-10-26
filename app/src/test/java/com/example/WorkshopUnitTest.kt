package com.example

import com.example.data.WorkshopJobCardEntity
import com.example.data.WorkshopMechanicEntity
import com.example.data.WorkshopVehicleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkshopUnitTest {

    @Test
    fun testVehicleCreationWithChassisNumber() {
        val vehicle = WorkshopVehicleEntity(
            id = 1L,
            businessId = 1L,
            customerName = "Mian Tariq",
            customerPhone = "+92 300 1234567",
            plateNumber = "LEA-21-4589",
            makeAndModel = "Toyota Corolla Altis 1.6",
            modelYear = "2021",
            chassisNumber = "NZE140-9082341",
            engineNumber = "1ZR-FE-56901",
            vehicleType = "Car / Sedan",
            color = "Super White",
            currentMileageKm = 45200,
            fuelType = "Petrol",
            registeredCity = "Lahore"
        )

        assertEquals("LEA-21-4589", vehicle.plateNumber)
        assertEquals("NZE140-9082341", vehicle.chassisNumber)
        assertEquals("Toyota Corolla Altis 1.6", vehicle.makeAndModel)
        assertEquals(45200, vehicle.currentMileageKm)
    }

    @Test
    fun testJobCardCreationAndStatusProgression() {
        var jobCard = WorkshopJobCardEntity(
            id = 1L,
            businessId = 1L,
            jobCardNumber = "JC-1001",
            vehicleId = 1L,
            customerName = "Mian Tariq",
            customerPhone = "+92 300 1234567",
            vehiclePlateNumber = "LEA-21-4589",
            vehicleModel = "Toyota Corolla Altis 1.6",
            chassisNumber = "NZE140-9082341",
            reportedCustomerComplaints = "Engine check light glowing, front brake pads making grinding noise",
            diagnosticNotes = "Brake pads worn to metal, O2 sensor fouled",
            assignedMechanicId = 1L,
            assignedMechanicName = "Ustad Tariq Mehmood",
            status = "PENDING",
            laborCharges = 3500.0,
            partsEstimatedCost = 8500.0,
            totalEstimatedCost = 12000.0,
            advanceDeposit = 5000.0,
            remainingBalance = 7000.0,
            receivedDate = "2026-09-21",
            promisedDeliveryDate = "2026-09-23"
        )

        assertEquals("PENDING", jobCard.status)
        assertEquals(12000.0, jobCard.totalEstimatedCost, 0.01)
        assertEquals(7000.0, jobCard.remainingBalance, 0.01)

        // Advance to IN_PROGRESS
        jobCard = jobCard.copy(status = "IN_PROGRESS")
        assertEquals("IN_PROGRESS", jobCard.status)

        // Advance to COMPLETED and update balance on payment
        val newAdvance = jobCard.advanceDeposit + 7000.0
        val newBalance = (jobCard.totalEstimatedCost - newAdvance).coerceAtLeast(0.0)
        jobCard = jobCard.copy(status = "COMPLETED", advanceDeposit = newAdvance, remainingBalance = newBalance)

        assertEquals("COMPLETED", jobCard.status)
        assertEquals(0.0, jobCard.remainingBalance, 0.01)
    }

    @Test
    fun testMechanicAssignment() {
        val mechanic = WorkshopMechanicEntity(
            id = 2L,
            businessId = 1L,
            name = "Rana Shakeel",
            specialization = "Auto Electrician / EFI Scanner Specialist",
            phone = "+92 321 9876543",
            isAvailable = true
        )

        assertNotNull(mechanic)
        assertTrue(mechanic.isAvailable)
        assertEquals("Auto Electrician / EFI Scanner Specialist", mechanic.specialization)

        val jobCard = WorkshopJobCardEntity(
            id = 2L,
            businessId = 1L,
            jobCardNumber = "JC-1002",
            vehicleId = 2L,
            customerName = "Malik Rizwan",
            customerPhone = "+92 321 4455667",
            vehiclePlateNumber = "ICT-B-9988",
            vehicleModel = "Honda Civic Oriel 1.8",
            chassisNumber = "FC1-2009841",
            reportedCustomerComplaints = "AC cooling weak, blower speed stuck",
            assignedMechanicId = mechanic.id,
            assignedMechanicName = mechanic.name,
            status = "IN_PROGRESS",
            receivedDate = "2026-09-21",
            promisedDeliveryDate = "2026-09-22"
        )

        assertEquals(mechanic.id, jobCard.assignedMechanicId)
        assertEquals(mechanic.name, jobCard.assignedMechanicName)
    }
}
