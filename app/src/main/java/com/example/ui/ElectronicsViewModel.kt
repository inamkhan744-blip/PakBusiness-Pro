package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ElectronicsDao
import com.example.data.RepairJobEntity
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
class ElectronicsViewModel(
    application: Application,
    private val electronicsDao: ElectronicsDao = AppDatabase.getDatabase(application).electronicsDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    val repairJobs: StateFlow<List<RepairJobEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) electronicsDao.getRepairJobs(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredJobs: StateFlow<List<RepairJobEntity>> = combine(
        repairJobs,
        searchQuery,
        statusFilter
    ) { list, q, filter ->
        list.filter {
            val matchesQuery = q.isBlank() ||
                it.customerName.contains(q, ignoreCase = true) ||
                it.deviceModel.contains(q, ignoreCase = true) ||
                it.imeiOrSerial.contains(q, ignoreCase = true) ||
                it.tokenNumber.contains(q, ignoreCase = true)

            val matchesFilter = filter == "ALL" || it.status == filter
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun createRepairJob(
        customerName: String,
        phone: String,
        deviceBrand: String,
        deviceModel: String,
        imei: String,
        problem: String,
        estimatedCost: Double,
        advancePaid: Double
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            electronicsDao.insertRepairJob(
                RepairJobEntity(
                    businessId = bizId,
                    customerName = customerName.trim(),
                    customerPhone = phone.trim(),
                    deviceBrand = deviceBrand.trim(),
                    deviceModel = deviceModel.trim(),
                    imeiOrSerial = imei.trim(),
                    problemDescription = problem.trim(),
                    estimatedCostPkr = estimatedCost,
                    advancePaidPkr = advancePaid
                )
            )
        }
    }

    fun updateJobStatus(id: Long, status: String) {
        launchWithLoading {
            electronicsDao.updateStatus(id, status)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ElectronicsViewModel(application) as T
                }
            }
    }
}
