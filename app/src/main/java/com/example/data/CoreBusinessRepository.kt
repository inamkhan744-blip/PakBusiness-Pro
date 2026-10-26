package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository providing access to core multi-tenant business records:
 * Parties (customers/suppliers), Inventory Items (products/stock), and Orders (sales/purchases).
 */
class CoreBusinessRepository(
    private val partyDao: PartyDao,
    private val inventoryItemDao: InventoryItemDao,
    private val orderDao: OrderDao
) {
    // ==========================================
    // PARTY OPERATIONS
    // ==========================================

    fun getParties(businessId: Long): Flow<List<PartyEntity>> =
        partyDao.getParties(businessId)

    fun getPartiesByType(businessId: Long, partyType: String): Flow<List<PartyEntity>> =
        partyDao.getPartiesByType(businessId, partyType)

    fun getPartyById(id: Long): Flow<PartyEntity?> =
        partyDao.getPartyById(id)

    suspend fun getPartyByIdDirect(id: Long): PartyEntity? = withContext(Dispatchers.IO) {
        partyDao.getPartyByIdDirect(id)
    }

    fun searchParties(businessId: Long, query: String): Flow<List<PartyEntity>> =
        partyDao.searchParties(businessId, query)

    suspend fun insertParty(party: PartyEntity): Long = withContext(Dispatchers.IO) {
        partyDao.insertParty(party)
    }

    suspend fun insertParties(parties: List<PartyEntity>) = withContext(Dispatchers.IO) {
        partyDao.insertParties(parties)
    }

    suspend fun updateParty(party: PartyEntity) = withContext(Dispatchers.IO) {
        partyDao.updateParty(party)
    }

    suspend fun deleteParty(id: Long) = withContext(Dispatchers.IO) {
        partyDao.deleteParty(id)
    }

    suspend fun updatePartyBalance(partyId: Long, balanceDelta: Double) = withContext(Dispatchers.IO) {
        partyDao.updatePartyBalance(partyId, balanceDelta)
    }

    suspend fun getPartyCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        partyDao.getPartyCount(businessId)
    }

    // ==========================================
    // INVENTORY ITEM OPERATIONS
    // ==========================================

    fun getInventoryItems(businessId: Long): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItems(businessId)

    fun getLowStockItems(businessId: Long): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getLowStockItems(businessId)

    fun getInventoryItemsByCategory(businessId: Long, category: String): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsByCategory(businessId, category)

    fun getInventoryItemById(id: Long): Flow<InventoryItemEntity?> =
        inventoryItemDao.getItemById(id)

    suspend fun getInventoryItemByIdDirect(id: Long): InventoryItemEntity? = withContext(Dispatchers.IO) {
        inventoryItemDao.getItemByIdDirect(id)
    }

    suspend fun getInventoryItemBySkuOrBarcode(businessId: Long, skuOrBarcode: String): InventoryItemEntity? = withContext(Dispatchers.IO) {
        inventoryItemDao.getItemBySkuOrBarcode(businessId, skuOrBarcode)
    }

    fun searchInventoryItems(businessId: Long, query: String): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.searchItems(businessId, query)

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        inventoryItemDao.insertItem(item)
    }

    suspend fun insertInventoryItems(items: List<InventoryItemEntity>) = withContext(Dispatchers.IO) {
        inventoryItemDao.insertItems(items)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryItemDao.updateItem(item)
    }

    suspend fun deleteInventoryItem(id: Long) = withContext(Dispatchers.IO) {
        inventoryItemDao.deleteItem(id)
    }

    suspend fun updateInventoryStock(itemId: Long, quantityDelta: Double) = withContext(Dispatchers.IO) {
        inventoryItemDao.updateStock(itemId, quantityDelta)
    }

    suspend fun getInventoryItemCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        inventoryItemDao.getItemCount(businessId)
    }

    fun getTotalInventoryCostValue(businessId: Long): Flow<Double?> =
        inventoryItemDao.getTotalInventoryCostValue(businessId)

    fun getTotalInventoryRetailValue(businessId: Long): Flow<Double?> =
        inventoryItemDao.getTotalInventoryRetailValue(businessId)

    // ==========================================
    // ORDER OPERATIONS
    // ==========================================

    fun getOrders(businessId: Long): Flow<List<OrderEntity>> =
        orderDao.getOrders(businessId)

    fun getOrdersByType(businessId: Long, orderType: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByType(businessId, orderType)

    fun getOrdersByParty(businessId: Long, partyId: Long): Flow<List<OrderEntity>> =
        orderDao.getOrdersByParty(businessId, partyId)

    fun getOrderById(id: Long): Flow<OrderEntity?> =
        orderDao.getOrderById(id)

    suspend fun getOrderByIdDirect(id: Long): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderByIdDirect(id)
    }

    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?> =
        orderDao.getOrderWithItems(orderId)

    suspend fun getOrderWithItemsDirect(orderId: Long): OrderWithItems? = withContext(Dispatchers.IO) {
        orderDao.getOrderWithItemsDirect(orderId)
    }

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> =
        orderDao.getOrderItems(orderId)

    suspend fun insertOrder(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(id: Long) = withContext(Dispatchers.IO) {
        orderDao.deleteOrderItems(id)
        orderDao.deleteOrder(id)
    }

    suspend fun getOrderCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        orderDao.getOrderCount(businessId)
    }

    fun getTotalSalesAmount(businessId: Long): Flow<Double?> =
        orderDao.getTotalSalesAmount(businessId)

    /**
     * Complete transaction to create an order, save its line items, adjust inventory stock,
     * and update the party's ledger balance if applicable.
     */
    suspend fun createOrderWithItems(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        updateStock: Boolean = true,
        updatePartyLedger: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        orderDao.insertOrderItems(itemsWithOrderId)

        if (updateStock) {
            for (item in items) {
                val inventoryId = item.inventoryItemId
                if (inventoryId != null && inventoryId > 0) {
                    val delta = if (order.orderType == "PURCHASE" || order.orderType == "RETURN") {
                        item.quantity
                    } else {
                        -item.quantity
                    }
                    inventoryItemDao.updateStock(inventoryId, delta)
                }
            }
        }

        if (updatePartyLedger && order.partyId != null && order.balanceAmount > 0) {
            // If credit balance remaining on a sale, party owes us money (+ve balance)
            val balanceDelta = if (order.orderType == "SALE") order.balanceAmount else -order.balanceAmount
            partyDao.updatePartyBalance(order.partyId, balanceDelta)
        }

        orderId
    }

    suspend fun generateNextOrderNumber(businessId: Long, prefix: String = "ORD"): String = withContext(Dispatchers.IO) {
        val maxId = orderDao.getMaxOrderId(businessId) ?: 0L
        val nextNum = maxId + 1
        "$prefix-${String.format("%04d", nextNum)}"
    }
}
