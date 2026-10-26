package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BakeryRepository(private val bakeryDao: BakeryDao) {

    // Inventory
    fun getItems(businessId: Long): Flow<List<BakeryItemEntity>> =
        bakeryDao.getItems(businessId)

    fun getItemsByCategory(businessId: Long, category: String): Flow<List<BakeryItemEntity>> =
        bakeryDao.getItemsByCategory(businessId, category)

    suspend fun getItemById(id: Long): BakeryItemEntity? =
        bakeryDao.getItemById(id)

    suspend fun insertItem(item: BakeryItemEntity): Long =
        bakeryDao.insertItem(item)

    suspend fun updateItem(item: BakeryItemEntity) =
        bakeryDao.updateItem(item)

    suspend fun deleteItem(id: Long) =
        bakeryDao.deleteItem(id)

    suspend fun updateStock(id: Long, newStock: Double) =
        bakeryDao.updateStock(id, newStock)

    // Cake Orders
    fun getAllCakeOrders(businessId: Long): Flow<List<BakeryCakeOrderEntity>> =
        bakeryDao.getAllCakeOrders(businessId)

    fun getCakeOrdersByStatus(businessId: Long, status: String): Flow<List<BakeryCakeOrderEntity>> =
        bakeryDao.getCakeOrdersByStatus(businessId, status)

    suspend fun getCakeOrderById(id: Long): BakeryCakeOrderEntity? =
        bakeryDao.getCakeOrderById(id)

    suspend fun insertCakeOrder(order: BakeryCakeOrderEntity): Long =
        bakeryDao.insertCakeOrder(order)

    suspend fun updateCakeOrder(order: BakeryCakeOrderEntity) =
        bakeryDao.updateCakeOrder(order)

    suspend fun deleteCakeOrder(id: Long) =
        bakeryDao.deleteCakeOrder(id)

    suspend fun updateCakeOrderStatus(orderId: Long, status: String) =
        bakeryDao.updateCakeOrderStatus(orderId, status)

    suspend fun updateCakeOrderPayment(orderId: Long, advance: Double, balance: Double) =
        bakeryDao.updateCakeOrderPayment(orderId, advance, balance)

    suspend fun seedSampleBakeryDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake bakery entries
        val existingOrdersCount = bakeryDao.getCakeOrderCount(businessId)
        val existingItemCount = bakeryDao.getItemCount(businessId)
        if (existingOrdersCount > 0 && existingItemCount > 0) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val inTwoDaysStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 4)
        val inFourDaysStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val inAWeekStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val expiredYesterdayStr = sdf.format(cal.time)

        if (existingItemCount == 0) {
            val sampleItems = listOf(
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Special Gulab Jamun",
                    category = "Mithai / Sweets",
                    unit = "kg",
                    pricePerUnit = 1200.0,
                    currentStock = 18.5,
                    productionDate = todayStr,
                    expiryDate = inFourDaysStr,
                    batchNumber = "MTH-0921A"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Fresh Khoya Barfi",
                    category = "Mithai / Sweets",
                    unit = "kg",
                    pricePerUnit = 1450.0,
                    currentStock = 12.0,
                    productionDate = yesterdayStr,
                    expiryDate = inTwoDaysStr,
                    batchNumber = "MTH-0920B"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Motichoor Ladoo",
                    category = "Mithai / Sweets",
                    unit = "kg",
                    pricePerUnit = 1100.0,
                    currentStock = 22.0,
                    productionDate = todayStr,
                    expiryDate = inAWeekStr,
                    batchNumber = "MTH-0921C"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Chicken Bread Jumbo",
                    category = "Bakery & Breads",
                    unit = "piece",
                    pricePerUnit = 380.0,
                    currentStock = 14.0,
                    productionDate = todayStr,
                    expiryDate = inTwoDaysStr,
                    batchNumber = "BKY-0921J"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Crispy Chicken Patties",
                    category = "Snacks & Savories",
                    unit = "piece",
                    pricePerUnit = 120.0,
                    currentStock = 45.0,
                    productionDate = todayStr,
                    expiryDate = inTwoDaysStr,
                    batchNumber = "SNK-0921P"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Black Forest Pastry",
                    category = "Pastries & Cakes",
                    unit = "piece",
                    pricePerUnit = 220.0,
                    currentStock = 16.0,
                    productionDate = todayStr,
                    expiryDate = inTwoDaysStr,
                    batchNumber = "PST-0921F"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Almond Tea Rusks",
                    category = "Biscuits & Cookies",
                    unit = "pack",
                    pricePerUnit = 320.0,
                    currentStock = 35.0,
                    productionDate = yesterdayStr,
                    expiryDate = inAWeekStr,
                    batchNumber = "BSC-0920R"
                ),
                BakeryItemEntity(
                    businessId = businessId,
                    name = "Fresh Milk Sandwich Bread",
                    category = "Bakery & Breads",
                    unit = "pack",
                    pricePerUnit = 180.0,
                    currentStock = 3.0, // low stock test
                    productionDate = yesterdayStr,
                    expiryDate = todayStr, // expiring today test
                    batchNumber = "BKY-0920M"
                )
            )
            sampleItems.forEach { bakeryDao.insertItem(it) }
        }

        if (existingOrdersCount == 0) {
            val sampleCakeOrders = listOf(
                BakeryCakeOrderEntity(
                    businessId = businessId,
                    orderNumber = "CAKE-501",
                    customerName = "Zainab Malik",
                    customerPhone = "+92 301 8877665",
                    occasion = "Birthday",
                    flavor = "Belgian Chocolate Fudge",
                    weightLbs = 3.0,
                    spongeType = "Regular (Egg)",
                    messageOnCake = "Happy 7th Birthday Ayaan!",
                    customDesignNotes = "Spider-man blue & red fondant web topper with candles",
                    deliveryDate = inTwoDaysStr,
                    deliveryTime = "06:30 PM",
                    status = "BAKING",
                    totalPrice = 4500.0,
                    advancePaid = 2000.0,
                    remainingBalance = 2500.0
                ),
                BakeryCakeOrderEntity(
                    businessId = businessId,
                    orderNumber = "CAKE-502",
                    customerName = "Farhan Siddiqui",
                    customerPhone = "+92 321 4455667",
                    occasion = "Wedding",
                    flavor = "Red Velvet & Cream Cheese",
                    weightLbs = 6.0,
                    spongeType = "Eggless",
                    messageOnCake = "Hassan & Ayesha - Two Hearts One Soul",
                    customDesignNotes = "3-Tier ivory pearl finish with fresh pastel roses and gold leafing",
                    deliveryDate = inFourDaysStr,
                    deliveryTime = "08:00 PM",
                    status = "RECEIVED",
                    totalPrice = 12500.0,
                    advancePaid = 6000.0,
                    remainingBalance = 6500.0
                ),
                BakeryCakeOrderEntity(
                    businessId = businessId,
                    orderNumber = "CAKE-503",
                    customerName = "Dr. Marium Tariq",
                    customerPhone = "+92 333 1122334",
                    occasion = "Anniversary",
                    flavor = "Lotus Biscoff Dream",
                    weightLbs = 2.5,
                    spongeType = "Regular (Egg)",
                    messageOnCake = "Happy 10th Anniversary!",
                    customDesignNotes = "Caramel drip with crushed lotus biscuits & heart chocolate plaque",
                    deliveryDate = todayStr,
                    deliveryTime = "04:00 PM",
                    status = "READY",
                    totalPrice = 3800.0,
                    advancePaid = 3800.0,
                    remainingBalance = 0.0
                ),
                BakeryCakeOrderEntity(
                    businessId = businessId,
                    orderNumber = "CAKE-504",
                    customerName = "Bilal Sheikh",
                    customerPhone = "+92 345 9988112",
                    occasion = "Birthday",
                    flavor = "Fresh Pineapple Cream",
                    weightLbs = 2.0,
                    spongeType = "Regular (Egg)",
                    messageOnCake = "Best Baba in the World",
                    customDesignNotes = "Classic white rosette piping with cherries",
                    deliveryDate = yesterdayStr,
                    deliveryTime = "07:00 PM",
                    status = "DELIVERED",
                    totalPrice = 2800.0,
                    advancePaid = 2800.0,
                    remainingBalance = 0.0
                )
            )
            sampleCakeOrders.forEach { bakeryDao.insertCakeOrder(it) }
        }
    }
}
