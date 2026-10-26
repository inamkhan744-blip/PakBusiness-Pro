package com.example.data

import kotlinx.coroutines.flow.Flow

class TailorRepository(private val tailorDao: TailorDao) {

    fun getCustomers(businessId: Long): Flow<List<TailorCustomerEntity>> =
        tailorDao.getCustomers(businessId)

    suspend fun insertCustomer(customer: TailorCustomerEntity): Long =
        tailorDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: TailorCustomerEntity) =
        tailorDao.updateCustomer(customer)

    suspend fun deleteCustomer(id: Long) =
        tailorDao.deleteCustomer(id)

    fun getAllMeasurements(businessId: Long): Flow<List<TailorMeasurementEntity>> =
        tailorDao.getAllMeasurements(businessId)

    fun getMeasurementsForCustomer(businessId: Long, customerId: Long): Flow<List<TailorMeasurementEntity>> =
        tailorDao.getMeasurementsForCustomer(businessId, customerId)

    suspend fun getMeasurementById(id: Long): TailorMeasurementEntity? =
        tailorDao.getMeasurementById(id)

    suspend fun insertMeasurement(measurement: TailorMeasurementEntity): Long =
        tailorDao.insertMeasurement(measurement)

    suspend fun updateMeasurement(measurement: TailorMeasurementEntity) =
        tailorDao.updateMeasurement(measurement)

    suspend fun deleteMeasurement(id: Long) =
        tailorDao.deleteMeasurement(id)

    fun getAllOrders(businessId: Long): Flow<List<TailorOrderEntity>> =
        tailorDao.getAllOrders(businessId)

    fun getOrdersByStatus(businessId: Long, status: String): Flow<List<TailorOrderEntity>> =
        tailorDao.getOrdersByStatus(businessId, status)

    suspend fun getOrderById(id: Long): TailorOrderEntity? =
        tailorDao.getOrderById(id)

    suspend fun insertOrder(order: TailorOrderEntity): Long =
        tailorDao.insertOrder(order)

    suspend fun updateOrder(order: TailorOrderEntity) =
        tailorDao.updateOrder(order)

    suspend fun deleteOrder(id: Long) =
        tailorDao.deleteOrder(id)

    suspend fun updateOrderStatus(orderId: Long, status: String) =
        tailorDao.updateOrderStatus(orderId, status)

    suspend fun updateOrderPayment(orderId: Long, advance: Double, balance: Double) =
        tailorDao.updateOrderPayment(orderId, advance, balance)

    suspend fun seedSampleTailorDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake tailor entries
        val existingOrdersCount = tailorDao.getOrderCount(businessId)
        if (existingOrdersCount > 0) return

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val cal = java.util.Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, -2)
        val deliveredDateStr = sdf.format(cal.time)

        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val tomorrowDateStr = sdf.format(cal.time)

        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 2)
        val twoDaysDateStr = sdf.format(cal.time)

        cal.time = java.util.Date()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 4)
        val fourDaysDateStr = sdf.format(cal.time)

        // Seed Customers
        val cust1Id = tailorDao.insertCustomer(
            TailorCustomerEntity(
                businessId = businessId,
                name = "Chaudhry Bilal Hussain",
                phone = "+92 300 7861122",
                gender = "Gents / Men's",
                city = "Lahore",
                notes = "Prefers hard Sherwani collar and double cuff",
                totalOrders = 3
            )
        )

        val cust2Id = tailorDao.insertCustomer(
            TailorCustomerEntity(
                businessId = businessId,
                name = "Hamza Tariq Butt",
                phone = "+92 321 4455667",
                gender = "Gents / Men's",
                city = "Gujranwala",
                notes = "Wash & wear fabrics only, regular fit",
                totalOrders = 2
            )
        )

        val cust3Id = tailorDao.insertCustomer(
            TailorCustomerEntity(
                businessId = businessId,
                name = "Zainab Fatima",
                phone = "+92 333 9988771",
                gender = "Ladies / Boutique",
                city = "Islamabad",
                notes = "Boutique Party Wear, contrast piping on daman",
                totalOrders = 1
            )
        )

        val cust4Id = tailorDao.insertCustomer(
            TailorCustomerEntity(
                businessId = businessId,
                name = "Shahid Khan Afridi",
                phone = "+92 314 2233445",
                gender = "Gents / Men's",
                city = "Peshawar",
                notes = "Traditional Kurta with Ban collar and single cuff",
                totalOrders = 1
            )
        )

        // Seed Measurements
        tailorDao.insertMeasurement(
            TailorMeasurementEntity(
                businessId = businessId,
                customerId = cust1Id,
                customerName = "Chaudhry Bilal Hussain",
                profileTitle = "Standard Shalwar Kameez",
                gender = "Gents",
                length = 41.0,
                chest = 40.0,
                waist = 36.0,
                hip = 42.0,
                shoulder = 18.5,
                sleeves = 24.5,
                collar = 16.0,
                daman = 23.5,
                cuff = 9.5,
                armhole = 10.0,
                shalwarLength = 39.0,
                paincha = 8.0,
                asan = 17.0,
                trouserWaist = 36.0,
                collarType = "Sherwani Ban",
                pocketStyle = "1 Front + 2 Side",
                cuffStyle = "Double Kaf",
                damanStyle = "Chauras (Square)",
                notes = "Add secret mobile pocket inside right side pocket"
            )
        )

        tailorDao.insertMeasurement(
            TailorMeasurementEntity(
                businessId = businessId,
                customerId = cust2Id,
                customerName = "Hamza Tariq Butt",
                profileTitle = "Executive Kurta Pajama",
                gender = "Gents",
                length = 39.5,
                chest = 38.0,
                waist = 33.0,
                hip = 39.0,
                shoulder = 17.5,
                sleeves = 23.0,
                collar = 15.5,
                daman = 22.0,
                cuff = 9.0,
                armhole = 9.5,
                shalwarLength = 37.5,
                paincha = 7.5,
                asan = 15.5,
                trouserWaist = 33.0,
                collarType = "Cut Ban",
                pocketStyle = "Side Pockets Only",
                cuffStyle = "Open Bazu (Gol)",
                damanStyle = "Gol Daman (Round)",
                notes = "Loose fitting for daily wear"
            )
        )

        tailorDao.insertMeasurement(
            TailorMeasurementEntity(
                businessId = businessId,
                customerId = cust3Id,
                customerName = "Zainab Fatima",
                profileTitle = "Designer Boutique Suit",
                gender = "Ladies",
                length = 42.0,
                chest = 36.0,
                waist = 30.0,
                hip = 38.0,
                shoulder = 15.5,
                sleeves = 21.5,
                collar = 14.0,
                daman = 24.0,
                cuff = 7.5,
                armhole = 8.5,
                shalwarLength = 38.0,
                paincha = 6.5,
                asan = 14.5,
                trouserWaist = 30.0,
                collarType = "V-Shape",
                pocketStyle = "None",
                cuffStyle = "Open Bazu (Gol)",
                damanStyle = "Chauras (Square)",
                notes = "Fine stitching with organza lace borders"
            )
        )

        // Seed Orders spanning the complete workflow: CUTTING -> STITCHING -> READY -> DELIVERED
        tailorDao.insertOrder(
            TailorOrderEntity(
                businessId = businessId,
                orderNumber = "#TK-201",
                customerId = cust4Id,
                customerName = "Shahid Khan Afridi",
                customerPhone = "+92 314 2233445",
                suitType = "Designer Kurta Pajama",
                quantity = 1,
                fabricDetails = "Charcoal Grey Boski (Customer Provided)",
                orderDate = todayStr,
                deliveryDate = fourDaysDateStr,
                status = "CUTTING",
                stitchingRate = 2200.0,
                totalAmount = 2200.0,
                advancePaid = 1000.0,
                remainingBalance = 1200.0,
                specialInstructions = "Urgent for family dinner. Add contrast charcoal buttons.",
                assignedMaster = "Master Ustad Rafique",
                measurementSummary = "L:40, C:39, Sh:18, Sl:24, P:7.5"
            )
        )

        tailorDao.insertOrder(
            TailorOrderEntity(
                businessId = businessId,
                orderNumber = "#TK-202",
                customerId = cust2Id,
                customerName = "Hamza Tariq Butt",
                customerPhone = "+92 321 4455667",
                suitType = "Men's Shalwar Kameez",
                quantity = 1,
                fabricDetails = "Royal Blue Wash & Wear Cotton",
                orderDate = todayStr,
                deliveryDate = twoDaysDateStr,
                status = "STITCHING",
                stitchingRate = 1800.0,
                totalAmount = 1800.0,
                advancePaid = 1000.0,
                remainingBalance = 800.0,
                specialInstructions = "Double thread stitching along front patti.",
                assignedMaster = "Master Aslam",
                measurementSummary = "L:39.5, C:38, W:33, Sh:17.5, P:7.5"
            )
        )

        tailorDao.insertOrder(
            TailorOrderEntity(
                businessId = businessId,
                orderNumber = "#TK-203",
                customerId = cust1Id,
                customerName = "Chaudhry Bilal Hussain",
                customerPhone = "+92 300 7861122",
                suitType = "Suit with Waistcoat / Wasket",
                quantity = 1,
                fabricDetails = "Egyptian White Cotton + Raw Silk Maroon Wasket",
                orderDate = deliveredDateStr,
                deliveryDate = tomorrowDateStr,
                status = "READY",
                stitchingRate = 4500.0,
                totalAmount = 4500.0,
                advancePaid = 2000.0,
                remainingBalance = 2500.0,
                specialInstructions = "Ready for trial. Garment pressed and placed on hanger.",
                assignedMaster = "Master Ustad Rafique",
                measurementSummary = "L:41, C:40, Sh:18.5, Sl:24.5, P:8"
            )
        )

        tailorDao.insertOrder(
            TailorOrderEntity(
                businessId = businessId,
                orderNumber = "#TK-204",
                customerId = cust3Id,
                customerName = "Zainab Fatima",
                customerPhone = "+92 333 9988771",
                suitType = "Ladies 3-Piece Boutique Suit",
                quantity = 1,
                fabricDetails = "Lawn Embroidered with Chiffon Dupatta",
                orderDate = deliveredDateStr,
                deliveryDate = deliveredDateStr,
                status = "DELIVERED",
                stitchingRate = 3500.0,
                totalAmount = 3500.0,
                advancePaid = 3500.0,
                remainingBalance = 0.0,
                specialInstructions = "Delivered to customer. Paid in full.",
                assignedMaster = "Boutique Artisan Sana",
                measurementSummary = "L:42, C:36, W:30, Sl:21.5, P:6.5"
            )
        )
    }
}
