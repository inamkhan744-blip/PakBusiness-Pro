package com.example.data

import kotlinx.coroutines.flow.Flow

class RestaurantRepository(private val restaurantDao: RestaurantDao) {

    // --- Tables ---

    fun getTables(businessId: Long): Flow<List<RestaurantTableEntity>> =
        restaurantDao.getTables(businessId)

    fun getTableById(id: Long): Flow<RestaurantTableEntity?> =
        restaurantDao.getTableById(id)

    suspend fun getTableByIdDirect(id: Long): RestaurantTableEntity? =
        restaurantDao.getTableByIdDirect(id)

    suspend fun saveTable(table: RestaurantTableEntity): Long {
        return if (table.id == 0L) {
            restaurantDao.insertTable(table)
        } else {
            restaurantDao.updateTable(table)
            table.id
        }
    }

    suspend fun deleteTable(id: Long) {
        restaurantDao.deleteTableById(id)
    }

    suspend fun reserveTable(tableId: Long, customerName: String, phone: String) {
        restaurantDao.updateTableOccupancy(
            tableId = tableId,
            status = "RESERVED",
            currentOrderId = null,
            occupiedSince = null,
            reservedFor = customerName,
            reservedPhone = phone,
            reservedTime = System.currentTimeMillis()
        )
    }

    suspend fun clearTableReservation(tableId: Long) {
        restaurantDao.updateTableOccupancy(
            tableId = tableId,
            status = "AVAILABLE",
            currentOrderId = null,
            occupiedSince = null,
            reservedFor = null,
            reservedPhone = null,
            reservedTime = null
        )
    }

    suspend fun seatTable(tableId: Long, orderId: Long) {
        restaurantDao.updateTableOccupancy(
            tableId = tableId,
            status = "OCCUPIED",
            currentOrderId = orderId,
            occupiedSince = System.currentTimeMillis(),
            reservedFor = null,
            reservedPhone = null,
            reservedTime = null
        )
    }

    suspend fun freeTable(tableId: Long) {
        restaurantDao.updateTableOccupancy(
            tableId = tableId,
            status = "AVAILABLE",
            currentOrderId = null,
            occupiedSince = null,
            reservedFor = null,
            reservedPhone = null,
            reservedTime = null
        )
    }

    // --- Menu Items ---

    fun getMenuItems(businessId: Long): Flow<List<RestaurantMenuItemEntity>> =
        restaurantDao.getMenuItems(businessId)

    fun getMenuItemById(id: Long): Flow<RestaurantMenuItemEntity?> =
        restaurantDao.getMenuItemById(id)

    suspend fun saveMenuItem(item: RestaurantMenuItemEntity): Long {
        return if (item.id == 0L) {
            restaurantDao.insertMenuItem(item)
        } else {
            restaurantDao.updateMenuItem(item)
            item.id
        }
    }

    suspend fun deleteMenuItem(id: Long) {
        restaurantDao.deleteMenuItemById(id)
    }

    suspend fun toggleMenuItemAvailability(id: Long, isAvailable: Boolean) {
        restaurantDao.setMenuItemAvailability(id, isAvailable)
    }

    // --- Orders & POS ---

    fun getActiveOrderByTable(tableId: Long): Flow<RestaurantOrderEntity?> =
        restaurantDao.getActiveOrderByTable(tableId)

    suspend fun getActiveOrderByTableDirect(tableId: Long): RestaurantOrderEntity? =
        restaurantDao.getActiveOrderByTableDirect(tableId)

    fun getOrderById(orderId: Long): Flow<RestaurantOrderEntity?> =
        restaurantDao.getOrderById(orderId)

    fun getActiveOrders(businessId: Long): Flow<List<RestaurantOrderEntity>> =
        restaurantDao.getActiveOrders(businessId)

    fun getRecentOrders(businessId: Long): Flow<List<RestaurantOrderEntity>> =
        restaurantDao.getRecentOrders(businessId)

    fun getOrderItems(orderId: Long): Flow<List<RestaurantOrderItemEntity>> =
        restaurantDao.getOrderItems(orderId)

    suspend fun getOrderItemsDirect(orderId: Long): List<RestaurantOrderItemEntity> =
        restaurantDao.getOrderItemsDirect(orderId)

    suspend fun getNextOrderNumber(businessId: Long): Int {
        val max = restaurantDao.getMaxOrderNumber(businessId) ?: 100
        return max + 1
    }

    suspend fun createOrUpdateOrderWithItems(
        order: RestaurantOrderEntity,
        items: List<RestaurantOrderItemEntity>,
        sendToKitchen: Boolean = true
    ): Long {
        val orderId = if (order.id == 0L) {
            val orderNum = if (order.orderNumber <= 0) getNextOrderNumber(order.businessId) else order.orderNumber
            val newOrder = order.copy(orderNumber = orderNum)
            restaurantDao.insertOrder(newOrder)
        } else {
            restaurantDao.updateOrder(order)
            order.id
        }

        // Save items
        // First delete previous uncommitted or replace items
        restaurantDao.deleteOrderItemsByOrderId(orderId)
        val itemsWithOrderId = items.map {
            it.copy(
                orderId = orderId,
                businessId = order.businessId,
                tableId = order.tableId,
                tableName = order.tableName,
                kotStatus = if (sendToKitchen) (if (it.kotStatus == "SERVED") "SERVED" else it.kotStatus) else "PENDING"
            )
        }
        restaurantDao.insertOrderItems(itemsWithOrderId)

        // Mark table as OCCUPIED
        seatTable(order.tableId, orderId)

        return orderId
    }

    suspend fun settleAndCompleteOrder(
        orderId: Long,
        paymentMethod: String,
        tableId: Long
    ) {
        val existingOrder = restaurantDao.getOrderByIdDirect(orderId)
        if (existingOrder != null) {
            val completed = existingOrder.copy(
                status = "COMPLETED",
                paymentMethod = paymentMethod,
                completedAt = System.currentTimeMillis()
            )
            restaurantDao.updateOrder(completed)
        }
        // Also update all KOT items for this order to SERVED
        restaurantDao.updateAllKotStatusForOrder(orderId, "SERVED")
        // Free the table
        freeTable(tableId)
    }

    suspend fun cancelOrder(orderId: Long, tableId: Long) {
        val existingOrder = restaurantDao.getOrderByIdDirect(orderId)
        if (existingOrder != null) {
            val cancelled = existingOrder.copy(
                status = "CANCELLED",
                completedAt = System.currentTimeMillis()
            )
            restaurantDao.updateOrder(cancelled)
        }
        restaurantDao.deleteOrderItemsByOrderId(orderId)
        freeTable(tableId)
    }

    // --- KOT Operations ---

    fun getActiveKotItems(businessId: Long): Flow<List<RestaurantOrderItemEntity>> =
        restaurantDao.getActiveKotItems(businessId)

    fun getAllKotItems(businessId: Long): Flow<List<RestaurantOrderItemEntity>> =
        restaurantDao.getAllKotItems(businessId)

    suspend fun updateKotItemStatus(itemId: Long, status: String) {
        val completedTime = if (status == "READY" || status == "SERVED") System.currentTimeMillis() else null
        restaurantDao.updateKotStatus(itemId, status, completedTime)
    }

    // --- Seed Data Generator ---

    suspend fun ensureInitialRestaurantData(businessId: Long) {
        return // Disabled: do not seed fake restaurant tables or menu items
        val tableCount = restaurantDao.getTableCount(businessId)
        if (tableCount == 0) {
            val initialTables = listOf(
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Table 1",
                    capacity = 2,
                    section = "Main Dining",
                    status = "AVAILABLE"
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Table 2",
                    capacity = 4,
                    section = "Main Dining",
                    status = "AVAILABLE"
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Table 3",
                    capacity = 4,
                    section = "Main Dining",
                    status = "OCCUPIED",
                    occupiedSince = System.currentTimeMillis() - (25L * 60L * 1000L) // 25 mins ago
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Table 4",
                    capacity = 6,
                    section = "Main Dining",
                    status = "AVAILABLE"
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Family Hall 1",
                    capacity = 8,
                    section = "Family Hall",
                    status = "RESERVED",
                    reservedFor = "Tariq Mahmood",
                    reservedPhone = "+92 300 5544332",
                    reservedTime = System.currentTimeMillis() + (45L * 60L * 1000L)
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Family Hall 2",
                    capacity = 6,
                    section = "Family Hall",
                    status = "AVAILABLE"
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "Terrace T1",
                    capacity = 4,
                    section = "Outdoor Terrace",
                    status = "AVAILABLE"
                ),
                RestaurantTableEntity(
                    businessId = businessId,
                    name = "VIP Lounge",
                    capacity = 8,
                    section = "VIP Room",
                    status = "AVAILABLE"
                )
            )
            restaurantDao.insertTables(initialTables)
        }

        val menuCount = restaurantDao.getMenuItemCount(businessId)
        if (menuCount == 0) {
            val initialMenu = listOf(
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Chicken Biryani (Special)",
                    category = "Desi Special",
                    pricePkr = 450.0,
                    description = "Fragrant basmati rice cooked with succulent chicken & aromatic Pakistani spices",
                    prepTimeMinutes = 10
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Mutton Dum Biryani",
                    category = "Desi Special",
                    pricePkr = 850.0,
                    description = "Slow-cooked mutton with saffron rice, fried onions & whole spices",
                    prepTimeMinutes = 15
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Chicken Karahi (Half)",
                    category = "Karahi & Handi",
                    pricePkr = 1400.0,
                    description = "Wok-cooked fresh desi chicken with ginger, tomatoes & crushed black pepper",
                    prepTimeMinutes = 25
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Mutton Handi Special",
                    category = "Karahi & Handi",
                    pricePkr = 2200.0,
                    description = "Tender mutton cooked in rich creamy yogurt gravy in a clay handi",
                    prepTimeMinutes = 30
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Chicken Seekh Kabab (4 Pcs)",
                    category = "BBQ & Tandoor",
                    pricePkr = 550.0,
                    description = "Minced chicken skewers chargrilled to perfection over coal",
                    prepTimeMinutes = 18
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Chicken Malai Boti Platter",
                    category = "BBQ & Tandoor",
                    pricePkr = 680.0,
                    description = "Melt-in-mouth boneless chicken cubes marinated in fresh cream & green chilies",
                    prepTimeMinutes = 20
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Crispy Zinger Burger & Fries",
                    category = "Fast Food",
                    pricePkr = 480.0,
                    description = "Golden crispy fried chicken thigh with secret mayo sauce & salted fries",
                    prepTimeMinutes = 12
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Club Sandwich (Special)",
                    category = "Fast Food",
                    pricePkr = 420.0,
                    description = "Triple decker toasted bread with grilled chicken, egg, cheese & coleslaw",
                    prepTimeMinutes = 12
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Garlic Roghani Naan",
                    category = "Breads",
                    pricePkr = 90.0,
                    description = "Fresh clay oven baked naan brushed with butter and roasted minced garlic",
                    prepTimeMinutes = 5
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Karak Doodh Patti Chai",
                    category = "Beverages",
                    pricePkr = 120.0,
                    description = "Authentic Pakistani highway-style rich brewed milk tea with cardamom",
                    prepTimeMinutes = 8
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Kashmiri Pink Tea",
                    category = "Beverages",
                    pricePkr = 180.0,
                    description = "Traditional slow-brewed pink tea garnished with crushed pistachios & almonds",
                    prepTimeMinutes = 8
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Fresh Mint Margarita",
                    category = "Beverages",
                    pricePkr = 250.0,
                    description = "Blended fresh garden mint leaves, lemon juice, soda & black salt",
                    prepTimeMinutes = 5
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Gulab Jamun (2 Pcs with Rabri)",
                    category = "Desserts",
                    pricePkr = 220.0,
                    description = "Warm sweet milk dumplings soaked in cardamom rose syrup topped with chilled rabri",
                    prepTimeMinutes = 5
                ),
                RestaurantMenuItemEntity(
                    businessId = businessId,
                    name = "Matka Kulfi Special",
                    category = "Desserts",
                    pricePkr = 190.0,
                    description = "Traditional slow-frozen clotted cream ice cream in an earthen pot",
                    prepTimeMinutes = 3
                )
            )
            restaurantDao.insertMenuItems(initialMenu)
        }

        // If Table 3 was seeded as OCCUPIED, seed its active order and KOT items so KOT screen has live data!
        val table3 = restaurantDao.getTableByIdDirect(3L)
        if (table3 != null && table3.status == "OCCUPIED" && table3.currentOrderId == null) {
            val orderNum = 101
            val activeOrder = RestaurantOrderEntity(
                businessId = businessId,
                tableId = table3.id,
                tableName = table3.name,
                orderNumber = orderNum,
                status = "KITCHEN",
                customerName = "Hamza & Family",
                customerPhone = "+92 321 9876543",
                guestCount = 4,
                subtotalPkr = 2870.0,
                taxPercent = 5.0,
                taxPkr = 143.5,
                discountPkr = 0.0,
                totalPkr = 3013.5,
                paymentMethod = "UNPAID",
                notes = "Family dinner, please serve biryani first",
                createdAt = System.currentTimeMillis() - (20L * 60L * 1000L)
            )
            val orderId = restaurantDao.insertOrder(activeOrder)
            restaurantDao.updateTableOccupancy(
                tableId = table3.id,
                status = "OCCUPIED",
                currentOrderId = orderId,
                occupiedSince = activeOrder.createdAt,
                reservedFor = null,
                reservedPhone = null,
                reservedTime = null
            )

            val kotItems = listOf(
                RestaurantOrderItemEntity(
                    orderId = orderId,
                    businessId = businessId,
                    tableId = table3.id,
                    tableName = table3.name,
                    menuItemId = 1L,
                    itemName = "Chicken Biryani (Special)",
                    itemCategory = "Desi Special",
                    unitPricePkr = 450.0,
                    quantity = 2,
                    instructions = "Extra spicy, raita on side",
                    kotStatus = "PREPARING",
                    kotSentTime = System.currentTimeMillis() - (18L * 60L * 1000L)
                ),
                RestaurantOrderItemEntity(
                    orderId = orderId,
                    businessId = businessId,
                    tableId = table3.id,
                    tableName = table3.name,
                    menuItemId = 3L,
                    itemName = "Chicken Karahi (Half)",
                    itemCategory = "Karahi & Handi",
                    unitPricePkr = 1400.0,
                    quantity = 1,
                    instructions = "Less oil, more ginger julienne",
                    kotStatus = "PENDING",
                    kotSentTime = System.currentTimeMillis() - (15L * 60L * 1000L)
                ),
                RestaurantOrderItemEntity(
                    orderId = orderId,
                    businessId = businessId,
                    tableId = table3.id,
                    tableName = table3.name,
                    menuItemId = 9L,
                    itemName = "Garlic Roghani Naan",
                    itemCategory = "Breads",
                    unitPricePkr = 90.0,
                    quantity = 4,
                    instructions = "Serve hot with Karahi",
                    kotStatus = "PENDING",
                    kotSentTime = System.currentTimeMillis() - (15L * 60L * 1000L)
                ),
                RestaurantOrderItemEntity(
                    orderId = orderId,
                    businessId = businessId,
                    tableId = table3.id,
                    tableName = table3.name,
                    menuItemId = 12L,
                    itemName = "Fresh Mint Margarita",
                    itemCategory = "Beverages",
                    unitPricePkr = 250.0,
                    quantity = 2,
                    instructions = "Extra crushed ice",
                    kotStatus = "READY",
                    kotSentTime = System.currentTimeMillis() - (20L * 60L * 1000L),
                    kotCompletedTime = System.currentTimeMillis() - (5L * 60L * 1000L)
                )
            )
            restaurantDao.insertOrderItems(kotItems)
        }
    }
}
