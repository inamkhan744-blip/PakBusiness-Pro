package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BakeryDao {

    // --- Inventory & Products ---
    @Query("SELECT * FROM bakery_items WHERE businessId = :businessId ORDER BY name ASC")
    fun getItems(businessId: Long): Flow<List<BakeryItemEntity>>

    @Query("SELECT * FROM bakery_items WHERE businessId = :businessId AND category = :category ORDER BY name ASC")
    fun getItemsByCategory(businessId: Long, category: String): Flow<List<BakeryItemEntity>>

    @Query("SELECT * FROM bakery_items WHERE id = :id")
    suspend fun getItemById(id: Long): BakeryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BakeryItemEntity): Long

    @Update
    suspend fun updateItem(item: BakeryItemEntity)

    @Query("DELETE FROM bakery_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("SELECT COUNT(*) FROM bakery_items WHERE businessId = :businessId")
    suspend fun getItemCount(businessId: Long): Int

    @Query("UPDATE bakery_items SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)

    // --- Custom Cake Orders ---
    @Query("SELECT * FROM bakery_cake_orders WHERE businessId = :businessId ORDER BY deliveryDate ASC, createdAt DESC")
    fun getAllCakeOrders(businessId: Long): Flow<List<BakeryCakeOrderEntity>>

    @Query("SELECT * FROM bakery_cake_orders WHERE businessId = :businessId AND status = :status ORDER BY deliveryDate ASC")
    fun getCakeOrdersByStatus(businessId: Long, status: String): Flow<List<BakeryCakeOrderEntity>>

    @Query("SELECT * FROM bakery_cake_orders WHERE id = :id")
    suspend fun getCakeOrderById(id: Long): BakeryCakeOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCakeOrder(order: BakeryCakeOrderEntity): Long

    @Update
    suspend fun updateCakeOrder(order: BakeryCakeOrderEntity)

    @Query("DELETE FROM bakery_cake_orders WHERE id = :id")
    suspend fun deleteCakeOrder(id: Long)

    @Query("UPDATE bakery_cake_orders SET status = :status WHERE id = :orderId")
    suspend fun updateCakeOrderStatus(orderId: Long, status: String)

    @Query("UPDATE bakery_cake_orders SET advancePaid = :advance, remainingBalance = :balance WHERE id = :orderId")
    suspend fun updateCakeOrderPayment(orderId: Long, advance: Double, balance: Double)

    @Query("SELECT COUNT(*) FROM bakery_cake_orders WHERE businessId = :businessId")
    suspend fun getCakeOrderCount(businessId: Long): Int
}
