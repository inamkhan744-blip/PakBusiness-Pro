package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkshopRepository(private val workshopDao: WorkshopDao) {

    // Customer Vehicles
    fun getVehicles(businessId: Long): Flow<List<WorkshopVehicleEntity>> =
        workshopDao.getVehicles(businessId)

    fun searchVehicles(businessId: Long, query: String): Flow<List<WorkshopVehicleEntity>> =
        workshopDao.searchVehicles(businessId, query)

    suspend fun getVehicleById(id: Long): WorkshopVehicleEntity? =
        workshopDao.getVehicleById(id)

    suspend fun insertVehicle(vehicle: WorkshopVehicleEntity): Long =
        workshopDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: WorkshopVehicleEntity) =
        workshopDao.updateVehicle(vehicle)

    suspend fun deleteVehicle(id: Long) =
        workshopDao.deleteVehicle(id)

    // Mechanics
    fun getMechanics(businessId: Long): Flow<List<WorkshopMechanicEntity>> =
        workshopDao.getMechanics(businessId)

    suspend fun getMechanicById(id: Long): WorkshopMechanicEntity? =
        workshopDao.getMechanicById(id)

    suspend fun insertMechanic(mechanic: WorkshopMechanicEntity): Long =
        workshopDao.insertMechanic(mechanic)

    suspend fun updateMechanic(mechanic: WorkshopMechanicEntity) =
        workshopDao.updateMechanic(mechanic)

    suspend fun deleteMechanic(id: Long) =
        workshopDao.deleteMechanic(id)

    // Job Cards
    fun getJobCards(businessId: Long): Flow<List<WorkshopJobCardEntity>> =
        workshopDao.getJobCards(businessId)

    fun getJobCardsByStatus(businessId: Long, status: String): Flow<List<WorkshopJobCardEntity>> =
        workshopDao.getJobCardsByStatus(businessId, status)

    fun getJobCardsForVehicle(vehicleId: Long): Flow<List<WorkshopJobCardEntity>> =
        workshopDao.getJobCardsForVehicle(vehicleId)

    suspend fun getJobCardById(id: Long): WorkshopJobCardEntity? =
        workshopDao.getJobCardById(id)

    suspend fun insertJobCard(jobCard: WorkshopJobCardEntity): Long =
        workshopDao.insertJobCard(jobCard)

    suspend fun updateJobCard(jobCard: WorkshopJobCardEntity) =
        workshopDao.updateJobCard(jobCard)

    suspend fun deleteJobCard(id: Long) =
        workshopDao.deleteJobCard(id)

    suspend fun updateJobCardStatus(jobCardId: Long, status: String) =
        workshopDao.updateJobCardStatus(jobCardId, status)

    suspend fun assignMechanic(jobCardId: Long, mechanicId: Long, mechanicName: String) =
        workshopDao.assignMechanic(jobCardId, mechanicId, mechanicName)

    suspend fun updateJobCardPayment(jobCardId: Long, deposit: Double, balance: Double) =
        workshopDao.updateJobCardPayment(jobCardId, deposit, balance)

    suspend fun seedSampleWorkshopDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake workshop entries
        val vCount = workshopDao.getVehicleCount(businessId)
        val mCount = workshopDao.getMechanicCount(businessId)
        val jCount = workshopDao.getJobCardCount(businessId)
        if (vCount > 0 && mCount > 0 && jCount > 0) return

        // 1. Pre-seed Mechanics
        val m1 = WorkshopMechanicEntity(
            businessId = businessId,
            name = "Ustad Tariq Mehmood",
            specialization = "Master Engine Specialist & Tuning",
            phone = "+92 300 4561234",
            activeJobCount = 2,
            isAvailable = true
        )
        val m2 = WorkshopMechanicEntity(
            businessId = businessId,
            name = "Shafiq Auto Electrician",
            specialization = "Auto Electrician / EFI & Scanner",
            phone = "+92 321 8765432",
            activeJobCount = 1,
            isAvailable = true
        )
        val m3 = WorkshopMechanicEntity(
            businessId = businessId,
            name = "Ustad Asghar Denter",
            specialization = "Denting & Painting & Body Alignment",
            phone = "+92 333 1122334",
            activeJobCount = 1,
            isAvailable = true
        )
        val m4 = WorkshopMechanicEntity(
            businessId = businessId,
            name = "Babar Ali",
            specialization = "Suspension & Brakes & Steering",
            phone = "+92 304 9988776",
            activeJobCount = 0,
            isAvailable = true
        )

        val m1Id = workshopDao.insertMechanic(m1)
        val m2Id = workshopDao.insertMechanic(m2)
        val m3Id = workshopDao.insertMechanic(m3)
        workshopDao.insertMechanic(m4)

        // 2. Pre-seed Customer Vehicles
        val v1 = WorkshopVehicleEntity(
            businessId = businessId,
            customerName = "Chaudhry Kamran",
            customerPhone = "+92 300 8456789",
            plateNumber = "LEA-21-4589",
            makeAndModel = "Toyota Corolla Altis 1.6",
            modelYear = "2021",
            chassisNumber = "NZE140-9082341",
            engineNumber = "1ZR-FE-56901",
            vehicleType = "Car / Sedan",
            color = "Super White",
            currentMileageKm = 52000,
            fuelType = "Petrol",
            registeredCity = "Lahore",
            notes = "Regular maintenance client"
        )
        val v2 = WorkshopVehicleEntity(
            businessId = businessId,
            customerName = "Dr. Asim Farooq",
            customerPhone = "+92 321 4455667",
            plateNumber = "ICT-BGY-780",
            makeAndModel = "Honda Civic Oriel 1.8",
            modelYear = "2020",
            chassisNumber = "FC1-2098456",
            engineNumber = "R18Z1-33421",
            vehicleType = "Car / Sedan",
            color = "Urban Titanium",
            currentMileageKm = 68500,
            fuelType = "Petrol",
            registeredCity = "Islamabad",
            notes = "Check engine light comes on randomly"
        )
        val v3 = WorkshopVehicleEntity(
            businessId = businessId,
            customerName = "Malik Rizwan",
            customerPhone = "+92 333 5566778",
            plateNumber = "KHI-AYZ-3412",
            makeAndModel = "Suzuki Alto VXR",
            modelYear = "2022",
            chassisNumber = "HA36S-118902",
            engineNumber = "R06A-89021",
            vehicleType = "Hatchback",
            color = "Silky Silver",
            currentMileageKm = 34000,
            fuelType = "Petrol",
            registeredCity = "Karachi",
            notes = "AC cooling low, oil change overdue"
        )
        val v4 = WorkshopVehicleEntity(
            businessId = businessId,
            customerName = "Haji Saeed Ur Rehman",
            customerPhone = "+92 301 7788990",
            plateNumber = "MN-19-9900",
            makeAndModel = "Toyota Hilux Revo Rocco",
            modelYear = "2022",
            chassisNumber = "GUN126-781203",
            engineNumber = "1GD-FTV-90112",
            vehicleType = "Commercial / Pickup",
            color = "Attitude Black",
            currentMileageKm = 76000,
            fuelType = "Diesel",
            registeredCity = "Multan",
            notes = "Front suspension noise on rough roads"
        )

        val v1Id = workshopDao.insertVehicle(v1)
        val v2Id = workshopDao.insertVehicle(v2)
        val v3Id = workshopDao.insertVehicle(v3)
        val v4Id = workshopDao.insertVehicle(v4)

        // 3. Pre-seed Job Cards
        val jc1 = WorkshopJobCardEntity(
            businessId = businessId,
            jobCardNumber = "JC-2026-101",
            vehicleId = v1Id,
            customerName = "Chaudhry Kamran",
            customerPhone = "+92 300 8456789",
            vehiclePlateNumber = "LEA-21-4589",
            vehicleModel = "Toyota Corolla Altis 1.6",
            chassisNumber = "NZE140-9082341",
            currentMileageKm = 52000,
            fuelGaugeLevel = "1/2 Tank",
            reportedCustomerComplaints = "Periodic 50K tuning, engine oil change (5W-30 Synthetic), front brake pads inspection",
            diagnosticNotes = "Brake pads 40% left, throttle body cleaned, spark plugs good, engine oil & filter replaced",
            assignedMechanicId = m1Id,
            assignedMechanicName = "Ustad Tariq Mehmood",
            bayOrRackNumber = "Bay 1 (Lift)",
            status = "IN_PROGRESS",
            laborCharges = 3500.0,
            partsEstimatedCost = 14500.0,
            totalEstimatedCost = 18000.0,
            advanceDeposit = 10000.0,
            remainingBalance = 8000.0,
            receivedDate = "2026-09-20",
            promisedDeliveryDate = "2026-09-22",
            itemsInventoryChecklist = "Jack, Spare tire, Wheel spanner, Rubber mats"
        )

        val jc2 = WorkshopJobCardEntity(
            businessId = businessId,
            jobCardNumber = "JC-2026-102",
            vehicleId = v2Id,
            customerName = "Dr. Asim Farooq",
            customerPhone = "+92 321 4455667",
            vehiclePlateNumber = "ICT-BGY-780",
            vehicleModel = "Honda Civic Oriel 1.8",
            chassisNumber = "FC1-2098456",
            currentMileageKm = 68500,
            fuelGaugeLevel = "3/4 Tank",
            reportedCustomerComplaints = "OBD Check Engine light glowing, VSA light warning, poor acceleration on incline",
            diagnosticNotes = "Scanner diagnosis code P0171 (System Too Lean Bank 1). O2 sensor tested, found vacuum leak at PCV hose",
            assignedMechanicId = m2Id,
            assignedMechanicName = "Shafiq Auto Electrician",
            bayOrRackNumber = "Bay 3 (Diagnostic Lab)",
            status = "WAITING_PARTS",
            laborCharges = 4500.0,
            partsEstimatedCost = 8200.0,
            totalEstimatedCost = 12700.0,
            advanceDeposit = 5000.0,
            remainingBalance = 7700.0,
            receivedDate = "2026-09-21",
            promisedDeliveryDate = "2026-09-23",
            itemsInventoryChecklist = "Android panel, Tool kit, Jack, Dashcam"
        )

        val jc3 = WorkshopJobCardEntity(
            businessId = businessId,
            jobCardNumber = "JC-2026-103",
            vehicleId = v3Id,
            customerName = "Malik Rizwan",
            customerPhone = "+92 333 5566778",
            vehiclePlateNumber = "KHI-AYZ-3412",
            vehicleModel = "Suzuki Alto VXR",
            chassisNumber = "HA36S-118902",
            currentMileageKm = 34000,
            fuelGaugeLevel = "1/4 Tank",
            reportedCustomerComplaints = "AC tripping frequently in afternoon heat, cabin air warm, rattling sound near compressor",
            diagnosticNotes = "AC compressor clutch slipping, refrigerant pressure low (30 PSI). Leak test passed on condenser. Topped R134a gas & adjusted clutch gap",
            assignedMechanicId = m1Id,
            assignedMechanicName = "Ustad Tariq Mehmood",
            bayOrRackNumber = "Bay 2 (AC Station)",
            status = "COMPLETED",
            laborCharges = 3000.0,
            partsEstimatedCost = 4500.0,
            totalEstimatedCost = 7500.0,
            advanceDeposit = 7500.0,
            remainingBalance = 0.0,
            receivedDate = "2026-09-19",
            promisedDeliveryDate = "2026-09-20",
            completedDate = "2026-09-20",
            itemsInventoryChecklist = "Spare wheel, Jack, Trunk carpet"
        )

        val jc4 = WorkshopJobCardEntity(
            businessId = businessId,
            jobCardNumber = "JC-2026-104",
            vehicleId = v4Id,
            customerName = "Haji Saeed Ur Rehman",
            customerPhone = "+92 301 7788990",
            vehiclePlateNumber = "MN-19-9900",
            vehicleModel = "Toyota Hilux Revo Rocco",
            chassisNumber = "GUN126-781203",
            currentMileageKm = 76000,
            fuelGaugeLevel = "Full Tank",
            reportedCustomerComplaints = "Rear bumper corner scratch denting & touch up, front right fender alignment",
            diagnosticNotes = "Dent pulled without paint damage, minor compounding and clear coat finish required on bumper corner",
            assignedMechanicId = m3Id,
            assignedMechanicName = "Ustad Asghar Denter",
            bayOrRackNumber = "Bay 4 (Body Shop)",
            status = "QUALITY_CHECK",
            laborCharges = 8000.0,
            partsEstimatedCost = 2000.0,
            totalEstimatedCost = 10000.0,
            advanceDeposit = 5000.0,
            remainingBalance = 5000.0,
            receivedDate = "2026-09-18",
            promisedDeliveryDate = "2026-09-21",
            itemsInventoryChecklist = "Tool kit, Bed liner, Spare tire, Tow rope"
        )

        workshopDao.insertJobCard(jc1)
        workshopDao.insertJobCard(jc2)
        workshopDao.insertJobCard(jc3)
        workshopDao.insertJobCard(jc4)
    }
}
