package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- Tables ---

    @Query("SELECT * FROM restaurant_tables WHERE businessId = :businessId ORDER BY id ASC")
    fun getTables(businessId: Long): Flow<List<RestaurantTableEntity>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id LIMIT 1")
    fun getTableById(id: Long): Flow<RestaurantTableEntity?>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id LIMIT 1")
    suspend fun getTableByIdDirect(id: Long): RestaurantTableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<RestaurantTableEntity>)

    @Update
    suspend fun updateTable(table: RestaurantTableEntity)

    @Query("DELETE FROM restaurant_tables WHERE id = :id")
    suspend fun deleteTableById(id: Long)

    @Query("UPDATE restaurant_tables SET status = :status, currentOrderId = :currentOrderId, occupiedSince = :occupiedSince, reservedFor = :reservedFor, reservedPhone = :reservedPhone, reservedTime = :reservedTime WHERE id = :tableId")
    suspend fun updateTableOccupancy(
        tableId: Long,
        status: String,
        currentOrderId: Long?,
        occupiedSince: Long?,
        reservedFor: String?,
        reservedPhone: String?,
        reservedTime: Long?
    )

    @Query("SELECT COUNT(*) FROM restaurant_tables WHERE businessId = :businessId")
    suspend fun getTableCount(businessId: Long): Int

    // --- Menu Items ---

    @Query("SELECT * FROM restaurant_menu_items WHERE businessId = :businessId ORDER BY category ASC, name ASC")
    fun getMenuItems(businessId: Long): Flow<List<RestaurantMenuItemEntity>>

    @Query("SELECT * FROM restaurant_menu_items WHERE id = :id LIMIT 1")
    fun getMenuItemById(id: Long): Flow<RestaurantMenuItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: RestaurantMenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<RestaurantMenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: RestaurantMenuItemEntity)

    @Query("DELETE FROM restaurant_menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Long)

    @Query("UPDATE restaurant_menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setMenuItemAvailability(id: Long, isAvailable: Boolean)

    @Query("SELECT COUNT(*) FROM restaurant_menu_items WHERE businessId = :businessId")
    suspend fun getMenuItemCount(businessId: Long): Int

    // --- Orders ---

    @Query("SELECT * FROM restaurant_orders WHERE tableId = :tableId AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveOrderByTable(tableId: Long): Flow<RestaurantOrderEntity?>

    @Query("SELECT * FROM restaurant_orders WHERE tableId = :tableId AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveOrderByTableDirect(tableId: Long): RestaurantOrderEntity?

    @Query("SELECT * FROM restaurant_orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<RestaurantOrderEntity?>

    @Query("SELECT * FROM restaurant_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: Long): RestaurantOrderEntity?

    @Query("SELECT * FROM restaurant_orders WHERE businessId = :businessId AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getActiveOrders(businessId: Long): Flow<List<RestaurantOrderEntity>>

    @Query("SELECT * FROM restaurant_orders WHERE businessId = :businessId ORDER BY createdAt DESC LIMIT 50")
    fun getRecentOrders(businessId: Long): Flow<List<RestaurantOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: RestaurantOrderEntity): Long

    @Update
    suspend fun updateOrder(order: RestaurantOrderEntity)

    @Query("DELETE FROM restaurant_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Query("SELECT MAX(orderNumber) FROM restaurant_orders WHERE businessId = :businessId")
    suspend fun getMaxOrderNumber(businessId: Long): Int?

    // --- Order Items / KOT ---

    @Query("SELECT * FROM restaurant_order_items WHERE orderId = :orderId ORDER BY id ASC")
    fun getOrderItems(orderId: Long): Flow<List<RestaurantOrderItemEntity>>

    @Query("SELECT * FROM restaurant_order_items WHERE orderId = :orderId ORDER BY id ASC")
    suspend fun getOrderItemsDirect(orderId: Long): List<RestaurantOrderItemEntity>

    @Query("SELECT * FROM restaurant_order_items WHERE businessId = :businessId AND kotStatus NOT IN ('SERVED') ORDER BY kotSentTime ASC")
    fun getActiveKotItems(businessId: Long): Flow<List<RestaurantOrderItemEntity>>

    @Query("SELECT * FROM restaurant_order_items WHERE businessId = :businessId ORDER BY kotSentTime DESC LIMIT 100")
    fun getAllKotItems(businessId: Long): Flow<List<RestaurantOrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: RestaurantOrderItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<RestaurantOrderItemEntity>)

    @Update
    suspend fun updateOrderItem(item: RestaurantOrderItemEntity)

    @Query("DELETE FROM restaurant_order_items WHERE id = :id")
    suspend fun deleteOrderItemById(id: Long)

    @Query("DELETE FROM restaurant_order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)

    @Query("UPDATE restaurant_order_items SET kotStatus = :kotStatus, kotCompletedTime = :completedTime WHERE id = :id")
    suspend fun updateKotStatus(id: Long, kotStatus: String, completedTime: Long?)

    @Query("UPDATE restaurant_order_items SET kotStatus = :kotStatus WHERE orderId = :orderId")
    suspend fun updateAllKotStatusForOrder(orderId: Long, kotStatus: String)
}
