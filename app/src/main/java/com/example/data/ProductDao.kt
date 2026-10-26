package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Products in the core business catalog & POS.
 * Adheres to Room best practices: reactive Flow queries and suspend mutations.
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY name ASC")
    fun getProducts(businessId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(businessId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(businessId: Long, category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND stockQuantity <= minStockThreshold AND isActive = 1 ORDER BY stockQuantity ASC")
    fun getLowStockProducts(businessId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId AND (barcode = :barcodeOrSku OR sku = :barcodeOrSku) LIMIT 1")
    suspend fun getProductByBarcodeOrSku(businessId: Long, barcodeOrSku: String): ProductEntity?

    @Query("""
        SELECT * FROM products 
        WHERE businessId = :businessId 
        AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(businessId: Long, query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("DELETE FROM products WHERE id = :id AND businessId = :businessId")
    suspend fun deleteProductByIdAndBusiness(id: Long, businessId: Long)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :deltaQuantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustStock(id: Long, deltaQuantity: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products WHERE businessId = :businessId")
    suspend fun getProductCount(businessId: Long): Int

    @Query("SELECT DISTINCT category FROM products WHERE businessId = :businessId ORDER BY category ASC")
    fun getProductCategories(businessId: Long): Flow<List<String>>
}
