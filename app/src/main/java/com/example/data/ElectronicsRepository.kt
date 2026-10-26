package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ElectronicsRepository(private val electronicsDao: ElectronicsDao) {

    // Products
    fun getProducts(businessId: Long): Flow<List<ElectronicsProductEntity>> =
        electronicsDao.getProducts(businessId)

    fun getProductsByCategory(businessId: Long, category: String): Flow<List<ElectronicsProductEntity>> =
        electronicsDao.getProductsByCategory(businessId, category)

    fun searchProducts(businessId: Long, query: String): Flow<List<ElectronicsProductEntity>> =
        electronicsDao.searchProducts(businessId, query)

    suspend fun getProductById(id: Long): ElectronicsProductEntity? =
        electronicsDao.getProductById(id)

    suspend fun insertProduct(product: ElectronicsProductEntity): Long =
        electronicsDao.insertProduct(product)

    suspend fun updateProduct(product: ElectronicsProductEntity) =
        electronicsDao.updateProduct(product)

    suspend fun deleteProduct(id: Long) =
        electronicsDao.deleteProduct(id)

    suspend fun updateProductStock(id: Long, newStock: Int) =
        electronicsDao.updateProductStock(id, newStock)

    // Repair Tickets
    fun getRepairTickets(businessId: Long): Flow<List<RepairTicketEntity>> =
        electronicsDao.getRepairTickets(businessId)

    fun getRepairTicketsByStatus(businessId: Long, status: String): Flow<List<RepairTicketEntity>> =
        electronicsDao.getRepairTicketsByStatus(businessId, status)

    suspend fun getRepairTicketById(id: Long): RepairTicketEntity? =
        electronicsDao.getRepairTicketById(id)

    suspend fun insertRepairTicket(ticket: RepairTicketEntity): Long =
        electronicsDao.insertRepairTicket(ticket)

    suspend fun updateRepairTicket(ticket: RepairTicketEntity) =
        electronicsDao.updateRepairTicket(ticket)

    suspend fun deleteRepairTicket(id: Long) =
        electronicsDao.deleteRepairTicket(id)

    suspend fun updateRepairTicketStatus(ticketId: Long, status: String) =
        electronicsDao.updateRepairTicketStatus(ticketId, status)

    suspend fun updateRepairTicketPayment(ticketId: Long, advance: Double, balance: Double) =
        electronicsDao.updateRepairTicketPayment(ticketId, advance, balance)

    suspend fun seedSampleElectronicsDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake electronics entries
        val existingProductsCount = electronicsDao.getProductCount(businessId)
        val existingTicketsCount = electronicsDao.getRepairTicketCount(businessId)
        if (existingProductsCount > 0 && existingTicketsCount > 0) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -10)
        val purchaseDatePastStr = sdf.format(cal.time)

        // Warranty dates
        cal.time = Date()
        cal.add(Calendar.MONTH, 11)
        val warrantyFutureStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val inTwoDaysStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 4)
        val inFourDaysStr = sdf.format(cal.time)

        if (existingProductsCount == 0) {
            val sampleProducts = listOf(
                ElectronicsProductEntity(
                    businessId = businessId,
                    name = "Samsung Galaxy S23 Ultra 5G",
                    brand = "Samsung",
                    category = "Smartphones",
                    condition = "Brand New (Box Pack)",
                    imei1 = "358920112345678",
                    imei2 = "358920112345679",
                    serialNumber = "R5CR10ABC99",
                    ptaStatus = "PTA Approved",
                    storageSpecs = "12GB RAM / 256GB ROM",
                    color = "Phantom Black",
                    purchasePrice = 285000.0,
                    salePrice = 310000.0,
                    stockQuantity = 2,
                    warrantyType = "1 Year Official Airlink",
                    warrantyExpiryDate = warrantyFutureStr,
                    supplierName = "Airlink Communications",
                    purchaseDate = purchaseDatePastStr,
                    notes = "Official brand warranty with receipt"
                ),
                ElectronicsProductEntity(
                    businessId = businessId,
                    name = "iPhone 14 Pro Max",
                    brand = "Apple",
                    category = "Smartphones",
                    condition = "Used (Kit Only)",
                    imei1 = "354890109988776",
                    imei2 = "354890109988777",
                    serialNumber = "F2LFD88K0N",
                    ptaStatus = "CPID / Patch",
                    storageSpecs = "128GB ROM",
                    color = "Deep Purple",
                    purchasePrice = 230000.0,
                    salePrice = 255000.0,
                    stockQuantity = 1,
                    warrantyType = "7 Days Checking",
                    warrantyExpiryDate = inTwoDaysStr,
                    supplierName = "Dubai Mobile Import",
                    purchaseDate = purchaseDatePastStr,
                    notes = "Battery health 91%, scratchless 10/10 condition"
                ),
                ElectronicsProductEntity(
                    businessId = businessId,
                    name = "Infinix Note 30 VIP",
                    brand = "Infinix",
                    category = "Smartphones",
                    condition = "Brand New (Box Pack)",
                    imei1 = "867722055667788",
                    imei2 = "867722055667789",
                    serialNumber = "INF-N30-9912",
                    ptaStatus = "PTA Approved",
                    storageSpecs = "12GB (+9GB) / 256GB",
                    color = "Glacier White",
                    purchasePrice = 64000.0,
                    salePrice = 71500.0,
                    stockQuantity = 4,
                    warrantyType = "1 Year Carlcare",
                    warrantyExpiryDate = warrantyFutureStr,
                    supplierName = "Hafeez Center Wholesaler",
                    purchaseDate = purchaseDatePastStr,
                    notes = "68W wired + 50W wireless charging"
                ),
                ElectronicsProductEntity(
                    businessId = businessId,
                    name = "Dell Latitude 7420 Core i7 11th Gen",
                    brand = "Dell",
                    category = "Laptops",
                    condition = "Used (Imported)",
                    imei1 = "",
                    imei2 = "",
                    serialNumber = "8HG7TX2",
                    ptaStatus = "Not Applicable",
                    storageSpecs = "16GB DDR4 / 512GB NVMe SSD",
                    color = "Carbon Black",
                    purchasePrice = 115000.0,
                    salePrice = 135000.0,
                    stockQuantity = 3,
                    warrantyType = "1 Month Local Warranty",
                    warrantyExpiryDate = inFourDaysStr,
                    supplierName = "Karachi IT Hub",
                    purchaseDate = purchaseDatePastStr,
                    notes = "Backlit keyboard, FHD IPS display"
                ),
                ElectronicsProductEntity(
                    businessId = businessId,
                    name = "Apple AirPods Pro 2 (USB-C)",
                    brand = "Apple",
                    category = "Audio & Accessories",
                    condition = "Brand New (Box Pack)",
                    imei1 = "",
                    imei2 = "",
                    serialNumber = "GX4L990P32",
                    ptaStatus = "Not Applicable",
                    storageSpecs = "ANC + Transparency",
                    color = "White",
                    purchasePrice = 62000.0,
                    salePrice = 68000.0,
                    stockQuantity = 5,
                    warrantyType = "1 Year International",
                    warrantyExpiryDate = warrantyFutureStr,
                    supplierName = "Mega Electronics",
                    purchaseDate = purchaseDatePastStr,
                    notes = "MagSafe Charging Case (USB-C)"
                )
            )
            sampleProducts.forEach { electronicsDao.insertProduct(it) }
        }

        if (existingTicketsCount == 0) {
            val sampleTickets = listOf(
                RepairTicketEntity(
                    businessId = businessId,
                    ticketNumber = "REP-301",
                    customerName = "Muhammad Hamza",
                    customerPhone = "+92 301 5544332",
                    deviceModel = "iPhone 13 Pro",
                    deviceColor = "Sierra Blue",
                    imeiOrSerial = "353890102345112",
                    devicePasscode = "Pin: 1994",
                    problemDescription = "Original OLED screen cracked, touch ghosting on upper area",
                    conditionNotes = "Camera lens scratchless, minor side scuffs",
                    assignedTechnician = "Master Farooq",
                    status = "IN_REPAIR",
                    estimatedCost = 38000.0,
                    advancePaid = 15000.0,
                    remainingBalance = 23000.0,
                    receivedDate = purchaseDatePastStr,
                    expectedDeliveryDate = inTwoDaysStr,
                    technicianRemarks = "TrueTone programmer ready, installing original pull-out display"
                ),
                RepairTicketEntity(
                    businessId = businessId,
                    ticketNumber = "REP-302",
                    customerName = "Rashid Minhas",
                    customerPhone = "+92 321 8899001",
                    deviceModel = "Samsung Galaxy A52s 5G",
                    deviceColor = "Awesome Violet",
                    imeiOrSerial = "359120109988123",
                    devicePasscode = "Pattern: Z-Shape",
                    problemDescription = "Phone not charging, moisture detected error on charging port",
                    conditionNotes = "Back cover replaced previously",
                    assignedTechnician = "Ustad Jameel",
                    status = "DIAGNOSING",
                    estimatedCost = 4500.0,
                    advancePaid = 2000.0,
                    remainingBalance = 2500.0,
                    receivedDate = todayStr,
                    expectedDeliveryDate = inTwoDaysStr,
                    technicianRemarks = "Testing sub-board ribbon and charging flex"
                ),
                RepairTicketEntity(
                    businessId = businessId,
                    ticketNumber = "REP-303",
                    customerName = "Dr. Naveed Akhtar",
                    customerPhone = "+92 333 4455667",
                    deviceModel = "Xiaomi Redmi Note 11",
                    deviceColor = "Star Blue",
                    imeiOrSerial = "869911002233445",
                    devicePasscode = "0000",
                    problemDescription = "Battery draining fast, phone shuts down at 30%",
                    conditionNotes = "Very clean condition with glass protector",
                    assignedTechnician = "Master Farooq",
                    status = "READY",
                    estimatedCost = 3200.0,
                    advancePaid = 3200.0,
                    remainingBalance = 0.0,
                    receivedDate = purchaseDatePastStr,
                    expectedDeliveryDate = todayStr,
                    technicianRemarks = "Original BN5D battery replaced, 100% capacity verified"
                ),
                RepairTicketEntity(
                    businessId = businessId,
                    ticketNumber = "REP-304",
                    customerName = "Zubair Shah",
                    customerPhone = "+92 345 7766554",
                    deviceModel = "Vivo Y20",
                    deviceColor = "Nebula Blue",
                    imeiOrSerial = "864455667788990",
                    devicePasscode = "None",
                    problemDescription = "Speaker and ringer mic dead, water splashed",
                    conditionNotes = "Back scratched",
                    assignedTechnician = "Ustad Jameel",
                    status = "DELIVERED",
                    estimatedCost = 2500.0,
                    advancePaid = 2500.0,
                    remainingBalance = 0.0,
                    receivedDate = purchaseDatePastStr,
                    expectedDeliveryDate = todayStr,
                    completedDate = todayStr,
                    technicianRemarks = "Loudspeaker buzzer replaced & PCB ultrasonic cleaned"
                )
            )
            sampleTickets.forEach { electronicsDao.insertRepairTicket(it) }
        }
    }
}
