package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository pattern implementation for Products catalog and inventory operations.
 */
class ProductRepository(
    private val productDao: ProductDao
) {
    fun getProducts(businessId: Long): Flow<List<ProductEntity>> =
        productDao.getProducts(businessId)

    fun getActiveProducts(businessId: Long): Flow<List<ProductEntity>> =
        productDao.getActiveProducts(businessId)

    fun getProductsByCategory(businessId: Long, category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(businessId, category)

    fun getLowStockProducts(businessId: Long): Flow<List<ProductEntity>> =
        productDao.getLowStockProducts(businessId)

    fun getProductById(id: Long): Flow<ProductEntity?> =
        productDao.getProductById(id)

    suspend fun getProductByIdDirect(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByIdDirect(id)
    }

    suspend fun getProductByBarcodeOrSku(businessId: Long, barcodeOrSku: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByBarcodeOrSku(businessId, barcodeOrSku)
    }

    fun searchProducts(businessId: Long, query: String): Flow<List<ProductEntity>> =
        productDao.searchProducts(businessId, query)

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun insertProducts(products: List<ProductEntity>) = withContext(Dispatchers.IO) {
        productDao.insertProducts(products)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun adjustStock(id: Long, deltaQuantity: Double) = withContext(Dispatchers.IO) {
        productDao.adjustStock(id, deltaQuantity)
    }

    suspend fun getProductCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        productDao.getProductCount(businessId)
    }

    fun getProductCategories(businessId: Long): Flow<List<String>> =
        productDao.getProductCategories(businessId)
}
