package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auto_workshop_vehicles",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["plateNumber"]),
        Index(value = ["chassisNumber"]),
        Index(value = ["customerPhone"])
    ]
)
data class WorkshopVehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val customerName: String,
    val customerPhone: String,
    val plateNumber: String, // e.g. "LEA-1234", "ICT-567", "KHI-8901"
    val makeAndModel: String, // e.g. "Toyota Corolla Altis 1.6", "Honda Civic Oriel", "Suzuki Alto VXR"
    val modelYear: String = "2021",
    val chassisNumber: String = "", // VIN / Chassis e.g. "NZE140-9012345"
    val engineNumber: String = "", // e.g. "1ZR-FE-45892"
    val vehicleType: String = "Car / Sedan", // "Car / Sedan", "SUV / 4x4", "Hatchback", "Commercial / Pickup", "Motorcycle"
    val color: String = "White",
    val currentMileageKm: Int = 45000,
    val fuelType: String = "Petrol", // "Petrol", "Diesel", "Hybrid", "Electric", "CNG"
    val registeredCity: String = "Lahore",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "auto_workshop_mechanics",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["name"])
    ]
)
data class WorkshopMechanicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val name: String, // e.g. "Ustad Tariq Mehmood", "Shafiq Auto Electrician", "Asghar Denter/Painter"
    val specialization: String = "Master Engine Specialist", // "Master Engine Specialist", "Auto Electrician / EFI", "Suspension & Brakes", "Denting & Painting", "AC & Radiator Specialist", "General Maintenance"
    val phone: String = "+92 300 9876543",
    val dailyCommissionRate: Double = 0.0, // Commission or daily rate
    val activeJobCount: Int = 0,
    val isAvailable: Boolean = true
)

@Entity(
    tableName = "auto_workshop_job_cards",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["jobCardNumber"]),
        Index(value = ["vehicleId"]),
        Index(value = ["status"]),
        Index(value = ["assignedMechanicId"])
    ]
)
data class WorkshopJobCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val jobCardNumber: String, // e.g. "JC-2026-001"
    val vehicleId: Long,
    val customerName: String,
    val customerPhone: String,
    val vehiclePlateNumber: String,
    val vehicleModel: String,
    val chassisNumber: String = "",
    val currentMileageKm: Int = 0,
    val fuelGaugeLevel: String = "1/2 Tank", // "Empty", "1/4 Tank", "1/2 Tank", "3/4 Tank", "Full Tank"
    
    // Reported issues & diagnostic
    val reportedCustomerComplaints: String, // e.g. "Engine missing on idle, brake squeaking, AC cooling weak"
    val diagnosticNotes: String = "", // e.g. "Throttle body cleaning needed, front brake pads worn, cabin filter choked"
    
    // Assignment
    val assignedMechanicId: Long = 0,
    val assignedMechanicName: String = "Ustad Tariq",
    val bayOrRackNumber: String = "Bay 2 (Lift)",
    
    // Workflow Status
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "WAITING_PARTS", "QUALITY_CHECK", "COMPLETED", "DELIVERED", "CANCELLED"
    
    // Financials in PKR
    val laborCharges: Double = 3500.0,
    val partsEstimatedCost: Double = 6500.0,
    val totalEstimatedCost: Double = 10000.0,
    val advanceDeposit: Double = 2000.0,
    val remainingBalance: Double = 8000.0,
    
    // Dates & timestamps
    val receivedDate: String, // "YYYY-MM-DD"
    val promisedDeliveryDate: String, // "YYYY-MM-DD"
    val completedDate: String = "",
    val itemsInventoryChecklist: String = "Spare wheel, Jack, Tool kit, Floor mats, Music system", // items left inside vehicle
    val createdAt: Long = System.currentTimeMillis()
)
