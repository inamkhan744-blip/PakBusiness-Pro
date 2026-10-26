package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing sales/purchase orders, line items, and invoice tracking.
 * Supports multi-tenant isolation by businessId.
 */
@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE businessId = :businessId ORDER BY orderDate DESC, id DESC")
    fun getOrders(businessId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE businessId = :businessId AND orderType = :orderType ORDER BY orderDate DESC, id DESC")
    fun getOrdersByType(businessId: Long, orderType: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE businessId = :businessId AND partyId = :partyId ORDER BY orderDate DESC, id DESC")
    fun getOrdersByParty(businessId: Long, partyId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE businessId = :businessId AND status = :status ORDER BY orderDate DESC, id DESC")
    fun getOrdersByStatus(businessId: Long, status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: Long): OrderEntity?

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderWithItemsDirect(orderId: Long): OrderWithItems?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsDirect(orderId: Long): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET paidAmount = :paidAmount, balanceAmount = :balanceAmount, paymentStatus = :paymentStatus WHERE id = :orderId")
    suspend fun updateOrderPayment(orderId: Long, paidAmount: Double, balanceAmount: Double, paymentStatus: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)

    @Query("DELETE FROM orders WHERE id = :id AND businessId = :businessId")
    suspend fun deleteOrderByIdAndBusiness(id: Long, businessId: Long)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItems(orderId: Long)

    @Query("SELECT COUNT(*) FROM orders WHERE businessId = :businessId")
    suspend fun getOrderCount(businessId: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE businessId = :businessId AND orderType = 'SALE' AND status != 'CANCELLED'")
    fun getTotalSalesCount(businessId: Long): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE businessId = :businessId AND orderType = 'SALE' AND status != 'CANCELLED'")
    fun getTotalSalesAmount(businessId: Long): Flow<Double?>

    @Query("SELECT MAX(id) FROM orders WHERE businessId = :businessId")
    suspend fun getMaxOrderId(businessId: Long): Long?
}
