package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MembershipDao
import com.example.data.MembershipEntity
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
 * Specialized ViewModel for managing Gym Membership plans and packages.
 * Uses StateFlow to deliver reactive, real-time updates to the UI layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MembershipsViewModel(
    application: Application,
    private val membershipDao: MembershipDao = AppDatabase.getDatabase(application).membershipDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showActiveOnly = MutableStateFlow(true)
    val showActiveOnly: StateFlow<Boolean> = _showActiveOnly.asStateFlow()

    private val _selectedMembership = MutableStateFlow<MembershipEntity?>(null)
    val selectedMembership: StateFlow<MembershipEntity?> = _selectedMembership.asStateFlow()

    // Raw stream of membership plans for the active business tenant
    val rawMemberships: StateFlow<List<MembershipEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) membershipDao.getMemberships(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered memberships by search query and active toggle
    val memberships: StateFlow<List<MembershipEntity>> = combine(
        rawMemberships,
        searchQuery,
        showActiveOnly
    ) { list, query, activeOnly ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.facilities.contains(query, ignoreCase = true)

            val matchesActive = !activeOnly || item.isActive

            matchesQuery && matchesActive
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for memberships screen
    val membershipsUiState: StateFlow<EntityUiState<List<MembershipEntity>>> = combine(
        memberships,
        isLoading
    ) { list, loading ->
        when {
            loading && list.isEmpty() -> EntityUiState.Loading
            list.isEmpty() -> EntityUiState.Empty
            else -> EntityUiState.Success(list)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntityUiState.Loading)

    // Plan count metrics
    val totalPlansCount: StateFlow<Int> = rawMemberships.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activePlansCount: StateFlow<Int> = rawMemberships.map { list -> list.count { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowActiveOnly(activeOnly: Boolean) {
        _showActiveOnly.value = activeOnly
    }

    fun selectMembership(membership: MembershipEntity?) {
        _selectedMembership.value = membership
    }

    fun saveMembership(
        id: Long = 0L,
        name: String,
        durationDays: Int = 30,
        pricePkr: Double = 3000.0,
        admissionFeePkr: Double = 0.0,
        description: String = "",
        facilities: String = "",
        isActive: Boolean = true,
        onComplete: (Long) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val membership = MembershipEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                durationDays = durationDays,
                pricePkr = pricePkr,
                admissionFeePkr = admissionFeePkr,
                description = description.trim(),
                facilities = facilities.trim(),
                isActive = isActive,
                createdAt = System.currentTimeMillis()
            )

            val generatedId = if (id == 0L) {
                membershipDao.insertMembership(membership)
            } else {
                membershipDao.updateMembership(membership)
                id
            }
            onComplete(generatedId)
        }
    }

    fun toggleMembershipActive(id: Long, currentStatus: Boolean) {
        launchWithLoading {
            val existing = membershipDao.getMembershipByIdDirect(id) ?: return@launchWithLoading
            membershipDao.updateMembership(existing.copy(isActive = !currentStatus))
        }
    }

    fun deleteMembership(id: Long) {
        launchWithLoading {
            membershipDao.deleteMembershipById(id)
            if (_selectedMembership.value?.id == id) {
                _selectedMembership.value = null
            }
        }
    }

    fun seedDefaultMemberships() {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            if (rawMemberships.value.isEmpty()) {
                val defaults = listOf(
                    MembershipEntity(
                        businessId = bizId,
                        name = "Monthly Standard",
                        durationDays = 30,
                        pricePkr = 3000.0,
                        admissionFeePkr = 1000.0,
                        description = "Standard 1-month fitness access",
                        facilities = "Gym floor, Weights, Cardio"
                    ),
                    MembershipEntity(
                        businessId = bizId,
                        name = "Quarterly Plan",
                        durationDays = 90,
                        pricePkr = 8000.0,
                        admissionFeePkr = 500.0,
                        description = "3-month discounted fitness package",
                        facilities = "Gym floor, Cardio, Locker Room"
                    ),
                    MembershipEntity(
                        businessId = bizId,
                        name = "Annual VIP",
                        durationDays = 365,
                        pricePkr = 28000.0,
                        admissionFeePkr = 0.0,
                        description = "Full 1-year all-inclusive VIP pass",
                        facilities = "Gym, Cardio, Steam/Sauna, Personal Trainer Consultation, Locker"
                    ),
                    MembershipEntity(
                        businessId = bizId,
                        name = "CrossFit & Cardio",
                        durationDays = 30,
                        pricePkr = 4500.0,
                        admissionFeePkr = 1000.0,
                        description = "High-intensity functional training & cardio",
                        facilities = "CrossFit Rig, Cardio, Shower, Locker"
                    )
                )
                membershipDao.insertMemberships(defaults)
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MembershipsViewModel(application) as T
                }
            }
    }
}

typealias MembershipViewModel = MembershipsViewModel
