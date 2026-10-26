package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LaundryRepository(private val laundryDao: LaundryDao) {

    fun getCustomers(businessId: Long): Flow<List<LaundryCustomerEntity>> =
        laundryDao.getCustomers(businessId)

    suspend fun getCustomerById(id: Long): LaundryCustomerEntity? =
        laundryDao.getCustomerById(id)

    suspend fun insertCustomer(customer: LaundryCustomerEntity): Long =
        laundryDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: LaundryCustomerEntity) =
        laundryDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: LaundryCustomerEntity) =
        laundryDao.deleteCustomer(customer)

    fun getOrders(businessId: Long): Flow<List<LaundryOrderEntity>> =
        laundryDao.getOrders(businessId)

    fun getOrdersByStatus(businessId: Long, status: String): Flow<List<LaundryOrderEntity>> =
        laundryDao.getOrdersByStatus(businessId, status)

    fun getOrdersForCustomer(businessId: Long, customerId: Long): Flow<List<LaundryOrderEntity>> =
        laundryDao.getOrdersForCustomer(businessId, customerId)

    suspend fun insertOrder(order: LaundryOrderEntity): Long =
        laundryDao.insertOrder(order)

    suspend fun updateOrder(order: LaundryOrderEntity) =
        laundryDao.updateOrder(order)

    suspend fun deleteOrder(order: LaundryOrderEntity) =
        laundryDao.deleteOrder(order)

    suspend fun updateOrderStatus(orderId: Long, status: String) =
        laundryDao.updateOrderStatus(orderId, status)

    suspend fun markOrderDelivered(orderId: Long) =
        laundryDao.markOrderDelivered(
            orderId = orderId,
            status = "DELIVERED",
            deliveredAt = System.currentTimeMillis(),
            paymentStatus = "PAID"
        )

    suspend fun updateOrderPayment(orderId: Long, additionalPayment: Double, newBalance: Double, paymentStatus: String) =
        laundryDao.updateOrderPayment(orderId, additionalPayment, newBalance, paymentStatus)

    suspend fun seedSampleLaundryDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake laundry entries
        val count = laundryDao.getCustomerCount(businessId)
        if (count == 0) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            val tomorrowStr = dateFormat.format(Date(System.currentTimeMillis() + 86400000L))
            val next2DaysStr = dateFormat.format(Date(System.currentTimeMillis() + 86400000L * 2))

            val c1Id = laundryDao.insertCustomer(
                LaundryCustomerEntity(
                    businessId = businessId,
                    name = "Malik Arshad",
                    phone = "+92 300 4567890",
                    address = "House 14-B, Model Town, Lahore",
                    notes = "Always prefers light fragrance, gentle iron",
                    totalOrdersCount = 4,
                    totalSpent = 4800.0
                )
            )

            val c2Id = laundryDao.insertCustomer(
                LaundryCustomerEntity(
                    businessId = businessId,
                    name = "Dr. Saira Jamil",
                    phone = "+92 321 8765432",
                    address = "Apartment 402, Askari 10, Lahore",
                    notes = "Hospital lab coats & formal suits only",
                    totalOrdersCount = 2,
                    totalSpent = 3200.0
                )
            )

            val c3Id = laundryDao.insertCustomer(
                LaundryCustomerEntity(
                    businessId = businessId,
                    name = "Chaudhry Waqas",
                    phone = "+92 333 1122334",
                    address = "Street 7, DHA Phase 5, Lahore",
                    notes = "Urgent service usually required",
                    totalOrdersCount = 3,
                    totalSpent = 5500.0
                )
            )

            // Seed sample orders across workflow stages
            // 1. In WASHING
            laundryDao.insertOrder(
                LaundryOrderEntity(
                    businessId = businessId,
                    orderNumber = "LD-1001",
                    customerId = c1Id,
                    customerName = "Malik Arshad",
                    customerPhone = "+92 300 4567890",
                    serviceType = "Wash & Iron",
                    urgentDelivery = false,
                    shirtCount = 5,
                    pantsCount = 3,
                    bedsheetCount = 2,
                    shalwarKameezCount = 2,
                    suitCount = 0,
                    totalPieces = 12,
                    status = "WASHING",
                    totalAmount = 1450.0,
                    advancePaid = 500.0,
                    balanceDue = 950.0,
                    paymentStatus = "PARTIAL",
                    orderDate = todayStr,
                    deliveryDate = next2DaysStr,
                    specialInstructions = "Collar starch normal, separate white shirts",
                    rackLocation = "Washer Bay 2"
                )
            )

            // 2. In IRONING
            laundryDao.insertOrder(
                LaundryOrderEntity(
                    businessId = businessId,
                    orderNumber = "LD-1002",
                    customerId = c2Id,
                    customerName = "Dr. Saira Jamil",
                    customerPhone = "+92 321 8765432",
                    serviceType = "Dry Clean",
                    urgentDelivery = true,
                    shirtCount = 2,
                    pantsCount = 2,
                    bedsheetCount = 0,
                    shalwarKameezCount = 0,
                    suitCount = 2,
                    totalPieces = 6,
                    status = "IRONING",
                    totalAmount = 2200.0,
                    advancePaid = 2200.0,
                    balanceDue = 0.0,
                    paymentStatus = "PAID",
                    orderDate = todayStr,
                    deliveryDate = tomorrowStr,
                    specialInstructions = "Dry clean suit jackets with hanger & plastic cover",
                    rackLocation = "Steam Press Station 1"
                )
            )

            // 3. READY for pickup
            laundryDao.insertOrder(
                LaundryOrderEntity(
                    businessId = businessId,
                    orderNumber = "LD-1003",
                    customerId = c3Id,
                    customerName = "Chaudhry Waqas",
                    customerPhone = "+92 333 1122334",
                    serviceType = "Wash & Iron",
                    urgentDelivery = false,
                    shirtCount = 4,
                    pantsCount = 4,
                    bedsheetCount = 3,
                    shalwarKameezCount = 4,
                    suitCount = 0,
                    totalPieces = 15,
                    status = "READY",
                    totalAmount = 2100.0,
                    advancePaid = 1000.0,
                    balanceDue = 1100.0,
                    paymentStatus = "PARTIAL",
                    orderDate = todayStr,
                    deliveryDate = todayStr,
                    specialInstructions = "Bedsheets folded in plastic pack",
                    rackLocation = "Rack Ready-A4"
                )
            )

            // 4. DELIVERED
            laundryDao.insertOrder(
                LaundryOrderEntity(
                    businessId = businessId,
                    orderNumber = "LD-1004",
                    customerId = c1Id,
                    customerName = "Malik Arshad",
                    customerPhone = "+92 300 4567890",
                    serviceType = "Wash & Iron",
                    urgentDelivery = false,
                    shirtCount = 3,
                    pantsCount = 2,
                    bedsheetCount = 1,
                    shalwarKameezCount = 1,
                    suitCount = 0,
                    totalPieces = 7,
                    status = "DELIVERED",
                    totalAmount = 850.0,
                    advancePaid = 850.0,
                    balanceDue = 0.0,
                    paymentStatus = "PAID",
                    orderDate = todayStr,
                    deliveryDate = todayStr,
                    deliveredAt = System.currentTimeMillis() - 7200000L,
                    specialInstructions = "Delivered to customer doorstep",
                    rackLocation = "Delivered"
                )
            )
        }
    }
}
