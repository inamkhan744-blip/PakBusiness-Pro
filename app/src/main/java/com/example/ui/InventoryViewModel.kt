package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.InventoryItemDao
import com.example.data.InventoryItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing 'InventoryItems' (Products, Catalog, Stock, Low Stock Alerts).
 * Provides reactive StateFlow streams for UI consumption.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    application: Application,
    private val inventoryDao: InventoryItemDao = AppDatabase.getDatabase(application).inventoryItemDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("ALL")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedItem = MutableStateFlow<InventoryItemEntity?>(null)
    val selectedItem: StateFlow<InventoryItemEntity?> = _selectedItem.asStateFlow()

    // Raw stream of items for active business
    val rawInventoryItems: StateFlow<List<InventoryItemEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) inventoryDao.getItems(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered items by query and category
    val inventoryItems: StateFlow<List<InventoryItemEntity>> = combine(
        rawInventoryItems,
        searchQuery,
        selectedCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.sku.contains(query, ignoreCase = true) ||
                item.barcode.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || category == "ALL" ||
                item.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for inventory screen
    val inventoryUiState: StateFlow<EntityUiState<List<InventoryItemEntity>>> = combine(
        inventoryItems,
        isLoading
    ) { list, loading ->
        when {
            loading && list.isEmpty() -> EntityUiState.Loading
            list.isEmpty() -> EntityUiState.Empty
            else -> EntityUiState.Success(list)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntityUiState.Loading)

    // Low stock items (stock <= minThreshold)
    val lowStockItems: StateFlow<List<InventoryItemEntity>> = rawInventoryItems.map { list ->
        list.filter { it.stockQuantity <= it.minThreshold }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories available in current inventory
    val categories: StateFlow<List<String>> = rawInventoryItems.map { list ->
        list.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total stock valuation
    val totalCostValue: StateFlow<Double> = rawInventoryItems.map { list ->
        list.sumOf { it.stockQuantity * it.costPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalRetailValue: StateFlow<Double> = rawInventoryItems.map { list ->
        list.sumOf { it.stockQuantity * it.salePrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectItem(item: InventoryItemEntity?) {
        _selectedItem.value = item
    }

    fun saveItem(
        id: Long = 0L,
        name: String,
        sku: String = "",
        barcode: String = "",
        category: String = "General",
        unit: String = "Pcs",
        costPrice: Double = 0.0,
        salePrice: Double = 0.0,
        stockQuantity: Double = 0.0,
        minThreshold: Double = 5.0,
        location: String = "",
        onComplete: (Long) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val item = InventoryItemEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                sku = sku.trim(),
                barcode = barcode.trim(),
                category = category.trim(),
                unit = unit.trim(),
                costPrice = costPrice,
                salePrice = salePrice,
                stockQuantity = stockQuantity,
                minThreshold = minThreshold,
                location = location.trim(),
                updatedAt = System.currentTimeMillis()
            )
            val generatedId = if (id == 0L) {
                inventoryDao.insertItem(item)
            } else {
                inventoryDao.updateItem(item)
                id
            }
            onComplete(generatedId)
        }
    }

    fun adjustStock(itemId: Long, quantityDelta: Double) {
        launchWithLoading {
            inventoryDao.updateStock(itemId, quantityDelta)
        }
    }

    fun setStock(itemId: Long, newQuantity: Double) {
        launchWithLoading {
            inventoryDao.setStock(itemId, newQuantity)
        }
    }

    fun deleteItem(itemId: Long) {
        launchWithLoading {
            inventoryDao.deleteItem(itemId)
            if (_selectedItem.value?.id == itemId) {
                _selectedItem.value = null
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InventoryViewModel(application) as T
                }
            }
    }
}

typealias InventoryItemsViewModel = InventoryViewModel
typealias InventoryItemViewModel = InventoryViewModel
