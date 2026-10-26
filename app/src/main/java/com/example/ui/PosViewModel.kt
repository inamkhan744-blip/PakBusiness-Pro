package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.InventoryItemDao
import com.example.data.InventoryItemEntity
import com.example.data.OrderDao
import com.example.data.OrderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val item: InventoryItemEntity,
    val quantity: Double = 1.0,
    val unitPrice: Double = item.salePrice
) {
    val total: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalCoroutinesApi::class)
class PosViewModel(
    application: Application,
    private val inventoryDao: InventoryItemDao = AppDatabase.getDatabase(application).inventoryItemDao(),
    private val orderDao: OrderDao = AppDatabase.getDatabase(application).orderDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    val rawInventory: StateFlow<List<InventoryItemEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) inventoryDao.getItems(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredInventory: StateFlow<List<InventoryItemEntity>> = combine(
        rawInventory,
        searchQuery
    ) { items, query ->
        if (query.isBlank()) items else {
            items.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.barcode.contains(query, ignoreCase = true) ||
                it.sku.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentOrders: StateFlow<List<OrderEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) orderDao.getOrders(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(item: InventoryItemEntity, qty: Double = 1.0) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == item.id }
        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + qty)
        } else {
            current.add(CartItem(item = item, quantity = qty))
        }
        _cart.value = current
    }

    fun updateCartQuantity(itemId: Long, qty: Double) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == itemId }
        if (index != -1) {
            if (qty <= 0.0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = qty)
            }
            _cart.value = current
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discount.value = 0.0
    }

    fun setDiscount(amount: Double) {
        _discount.value = amount
    }

    fun addByBarcode(barcode: String, onResult: (String) -> Unit) {
        val item = rawInventory.value.find { it.barcode.equals(barcode.trim(), ignoreCase = true) }
        if (item != null) {
            addToCart(item)
            onResult("Added to Cart: ${item.name}")
        } else {
            onResult("Item not found with barcode: '$barcode'")
        }
    }

    fun checkout(
        customerName: String = "Walk-in Customer",
        customerPhone: String = "",
        paymentMethod: String = "CASH",
        onSuccess: (Long) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        launchWithLoading {
            val subtotal = currentCart.sumOf { it.total }
            val finalTotal = (subtotal - _discount.value).coerceAtLeast(0.0)

            val order = OrderEntity(
                businessId = bizId,
                orderNumber = "ORD-${System.currentTimeMillis() % 1000000}",
                partyName = customerName.ifBlank { "Walk-in Customer" },
                partyPhone = customerPhone,
                totalAmount = finalTotal,
                discountAmount = _discount.value,
                paymentStatus = "PAID",
                paymentMode = paymentMethod
            )
            val orderId = orderDao.insertOrder(order)

            // Deduct stock for sold items
            currentCart.forEach { cartItem ->
                inventoryDao.updateStock(cartItem.item.id, -cartItem.quantity)
            }

            clearCart()
            onSuccess(orderId)
        }
    }

    fun saveProduct(
        id: Long = 0L,
        name: String,
        category: String = "General",
        costPrice: Double = 0.0,
        salePrice: Double = 0.0,
        stock: Double = 10.0,
        barcode: String = "",
        sku: String = ""
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val entity = InventoryItemEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                category = category.trim(),
                costPrice = costPrice,
                salePrice = salePrice,
                stockQuantity = stock,
                barcode = barcode.trim(),
                sku = sku.trim()
            )
            if (id == 0L) {
                inventoryDao.insertItem(entity)
            } else {
                inventoryDao.updateItem(entity)
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PosViewModel(application) as T
                }
            }
    }
}
