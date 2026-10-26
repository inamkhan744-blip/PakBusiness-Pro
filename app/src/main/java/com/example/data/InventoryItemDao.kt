package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing inventory items, product catalog, and stock counts.
 * Supports multi-tenant isolation by businessId.
 */
@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items WHERE businessId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun getItems(businessId: Long): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE businessId = :businessId AND isActive = 1 AND stockQuantity <= minThreshold ORDER BY stockQuantity ASC")
    fun getLowStockItems(businessId: Long): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE businessId = :businessId AND category = :category AND isActive = 1 ORDER BY name ASC")
    fun getItemsByCategory(businessId: Long, category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT DISTINCT category FROM inventory_items WHERE businessId = :businessId AND isActive = 1 ORDER BY category ASC")
    fun getAllCategories(businessId: Long): Flow<List<String>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    fun getItemById(id: Long): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdDirect(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE businessId = :businessId AND (sku = :skuOrBarcode OR barcode = :skuOrBarcode) LIMIT 1")
    suspend fun getItemBySkuOrBarcode(businessId: Long, skuOrBarcode: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE businessId = :businessId AND isActive = 1 AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchItems(businessId: Long, query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM inventory_items WHERE id = :id AND businessId = :businessId")
    suspend fun deleteItemByIdAndBusiness(id: Long, businessId: Long)

    @Query("UPDATE inventory_items SET stockQuantity = stockQuantity + :quantityDelta, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateStock(itemId: Long, quantityDelta: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET stockQuantity = :newQuantity, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun setStock(itemId: Long, newQuantity: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM inventory_items WHERE businessId = :businessId AND isActive = 1")
    suspend fun getItemCount(businessId: Long): Int

    @Query("SELECT SUM(stockQuantity * costPrice) FROM inventory_items WHERE businessId = :businessId AND isActive = 1")
    fun getTotalInventoryCostValue(businessId: Long): Flow<Double?>

    @Query("SELECT SUM(stockQuantity * salePrice) FROM inventory_items WHERE businessId = :businessId AND isActive = 1")
    fun getTotalInventoryRetailValue(businessId: Long): Flow<Double?>
}
