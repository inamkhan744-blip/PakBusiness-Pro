package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GymCheckInEntity
import com.example.data.GymDao
import com.example.data.GymMemberEntity
import com.example.data.MembershipDao
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Specialized ViewModel for managing Gym Members and Attendance Check-Ins.
 * Uses StateFlow to deliver reactive, real-time updates to the UI layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GymMembersViewModel(
    application: Application,
    private val gymDao: GymDao = AppDatabase.getDatabase(application).gymDao(),
    private val membershipDao: MembershipDao = AppDatabase.getDatabase(application).membershipDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter options: ALL, ACTIVE, EXPIRING_SOON, EXPIRED, CHECKED_IN
    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId.asStateFlow()

    // Today's date string format: yyyy-MM-dd
    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Raw stream of members for the current active business tenant
    val rawMembers: StateFlow<List<GymMemberEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getMembersByBusiness(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's check-ins for the gym
    val todayCheckIns: StateFlow<List<GymCheckInEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getTodayCheckIns(biz.id, todayDateString) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected member reactive stream
    val selectedMember: StateFlow<GymMemberEntity?> = _selectedMemberId.flatMapLatest { id ->
        if (id != null) gymDao.getMemberById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered list of members by query and membership status
    val members: StateFlow<List<GymMemberEntity>> = combine(
        rawMembers,
        searchQuery,
        statusFilter
    ) { list, query, filter ->
        list.filter { member ->
            val matchesQuery = query.isBlank() ||
                member.name.contains(query, ignoreCase = true) ||
                member.phone.contains(query, ignoreCase = true) ||
                member.plan.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "ACTIVE" -> !member.isExpired
                "EXPIRING_SOON" -> member.isExpiringSoon
                "EXPIRED" -> member.isExpired
                "CHECKED_IN" -> member.isCheckedIn
                else -> true // "ALL"
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for members screen
    val membersUiState: StateFlow<EntityUiState<List<GymMemberEntity>>> = combine(
        members,
        isLoading
    ) { list, loading ->
        when {
            loading && list.isEmpty() -> EntityUiState.Loading
            list.isEmpty() -> EntityUiState.Empty
            else -> EntityUiState.Success(list)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntityUiState.Loading)

    // Dashboard metrics
    val totalMembersCount: StateFlow<Int> = rawMembers.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeMembersCount: StateFlow<Int> = rawMembers.map { list -> list.count { !it.isExpired } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringSoonCount: StateFlow<Int> = rawMembers.map { list -> list.count { it.isExpiringSoon } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiredCount: StateFlow<Int> = rawMembers.map { list -> list.count { it.isExpired } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentlyCheckedInCount: StateFlow<Int> = rawMembers.map { list -> list.count { it.isCheckedIn } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun selectMember(memberId: Long?) {
        _selectedMemberId.value = memberId
    }

    fun saveMember(
        id: Long = 0L,
        name: String,
        phone: String,
        gender: String = "Male",
        plan: String = "Monthly Standard",
        startDate: Long = System.currentTimeMillis(),
        durationDays: Int = 30,
        amountPkr: Double = 3000.0,
        onComplete: (Long) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val existing = if (id != 0L) gymDao.getMemberByIdDirect(id) else null
            val member = GymMemberEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                phone = phone.trim(),
                gender = gender,
                plan = plan.trim(),
                startDate = startDate,
                durationDays = durationDays,
                amountPkr = amountPkr,
                isCheckedIn = existing?.isCheckedIn ?: false,
                lastCheckInTime = existing?.lastCheckInTime,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )

            val generatedId = if (id == 0L) {
                gymDao.insertMember(member)
            } else {
                gymDao.updateMember(member)
                id
            }
            selectMember(generatedId)
            onComplete(generatedId)
        }
    }

    fun toggleCheckIn(member: GymMemberEntity) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val now = System.currentTimeMillis()
            val dateStr = todayDateString

            if (!member.isCheckedIn) {
                // Check IN
                gymDao.updateCheckInStatus(member.id, true, now)
                val checkIn = GymCheckInEntity(
                    businessId = bizId,
                    memberId = member.id,
                    memberName = member.name,
                    checkInTime = now,
                    dateString = dateStr
                )
                gymDao.insertCheckIn(checkIn)
            } else {
                // Check OUT
                gymDao.updateCheckInStatus(member.id, false, member.lastCheckInTime)
                val openCheckIn = gymDao.getOpenCheckIn(member.id, dateStr)
                if (openCheckIn != null) {
                    gymDao.updateCheckIn(openCheckIn.copy(checkOutTime = now))
                }
            }
        }
    }

    fun renewMembership(
        memberId: Long,
        additionalDays: Int = 30,
        amountPkr: Double = 3000.0,
        plan: String? = null
    ) {
        launchWithLoading {
            val existing = gymDao.getMemberByIdDirect(memberId) ?: return@launchWithLoading
            val now = System.currentTimeMillis()
            // If expired, start from now; if still active, extend from previous expiry
            val newStartDate = if (existing.isExpired) now else existing.startDate
            val newDuration = if (existing.isExpired) additionalDays else existing.durationDays + additionalDays

            val updated = existing.copy(
                startDate = newStartDate,
                durationDays = newDuration,
                amountPkr = amountPkr,
                plan = plan ?: existing.plan
            )
            gymDao.updateMember(updated)
        }
    }

    fun deleteMember(memberId: Long) {
        launchWithLoading {
            gymDao.deleteMemberById(memberId)
            if (_selectedMemberId.value == memberId) {
                _selectedMemberId.value = null
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GymMembersViewModel(application) as T
                }
            }
    }
}

typealias GymMemberViewModel = GymMembersViewModel
