package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PartyDao
import com.example.data.PartyEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PartiesViewModel(
    application: Application,
    private val partyDao: PartyDao = AppDatabase.getDatabase(application).partyDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _partyTypeFilter = MutableStateFlow("ALL") // ALL, CUSTOMER, SUPPLIER
    val partyTypeFilter: StateFlow<String> = _partyTypeFilter.asStateFlow()

    val parties: StateFlow<List<PartyEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) partyDao.getParties(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredParties: StateFlow<List<PartyEntity>> = combine(
        parties,
        searchQuery,
        partyTypeFilter
    ) { list, q, type ->
        list.filter {
            val matchesQuery = q.isBlank() || it.name.contains(q, ignoreCase = true) || it.phone.contains(q, ignoreCase = true)
            val matchesType = type == "ALL" || it.partyType == type
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPartyTypeFilter(type: String) {
        _partyTypeFilter.value = type
    }

    fun saveParty(
        id: Long = 0L,
        name: String,
        phone: String,
        type: String,
        city: String = "",
        initialBalance: Double = 0.0
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val entity = PartyEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                phone = phone.trim(),
                partyType = type,
                city = city.trim(),
                currentBalance = initialBalance
            )
            if (id == 0L) partyDao.insertParty(entity) else partyDao.updateParty(entity)
        }
    }

    fun updateBalance(id: Long, delta: Double) {
        launchWithLoading {
            partyDao.updatePartyBalance(id, delta)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PartiesViewModel(application) as T
                }
            }
    }
}
