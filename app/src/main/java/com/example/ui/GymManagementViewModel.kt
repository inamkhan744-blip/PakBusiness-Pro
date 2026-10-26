package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GymCheckInEntity
import com.example.data.GymDao
import com.example.data.GymLockerEntity
import com.example.data.GymMemberEntity
import com.example.data.GymPaymentEntity
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
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GymKpis(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val expiringSoon: Int = 0,
    val expiredOrDefaulters: Int = 0,
    val todayCheckIns: Int = 0,
    val currentlyInGym: Int = 0,
    val monthlyRevenuePkr: Double = 0.0,
    val occupiedLockers: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymManagementViewModel(
    application: Application,
    private val gymDao: GymDao = AppDatabase.getDatabase(application).gymDao(),
    private val membershipDao: MembershipDao = AppDatabase.getDatabase(application).membershipDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter: ALL, ACTIVE, EXPIRING_SOON, EXPIRED, DEFAULTER, CHECKED_IN
    private val _filterStatus = MutableStateFlow("ALL")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _selectedMember = MutableStateFlow<GymMemberEntity?>(null)
    val selectedMember: StateFlow<GymMemberEntity?> = _selectedMember.asStateFlow()

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Raw streams
    val rawMembers: StateFlow<List<GymMemberEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getMembersByBusiness(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCheckIns: StateFlow<List<GymCheckInEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getTodayCheckIns(biz.id, todayDateString) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<GymPaymentEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getPayments(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockers: StateFlow<List<GymLockerEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) gymDao.getLockers(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packages: StateFlow<List<MembershipEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) membershipDao.getMemberships(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered members
    val filteredMembers: StateFlow<List<GymMemberEntity>> = combine(
        rawMembers,
        searchQuery,
        filterStatus
    ) { list, query, filter ->
        list.filter { m ->
            val matchQuery = query.isBlank() ||
                m.name.contains(query, ignoreCase = true) ||
                m.phone.contains(query, ignoreCase = true) ||
                m.plan.contains(query, ignoreCase = true) ||
                m.lockerNumber.contains(query, ignoreCase = true)

            val matchFilter = when (filter) {
                "ACTIVE" -> !m.isExpired
                "EXPIRING_SOON" -> m.isExpiringSoon
                "EXPIRED" -> m.isExpired
                "DEFAULTER" -> m.pendingDuePkr > 0.0
                "CHECKED_IN" -> m.isCheckedIn
                else -> true
            }

            matchQuery && matchFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // KPIs
    val kpis: StateFlow<GymKpis> = combine(
        rawMembers,
        todayCheckIns,
        payments,
        lockers
    ) { members, checkins, paymentsList, lockerList ->
        GymKpis(
            totalMembers = members.size,
            activeMembers = members.count { !it.isExpired },
            expiringSoon = members.count { it.isExpiringSoon },
            expiredOrDefaulters = members.count { it.isExpired || it.pendingDuePkr > 0.0 },
            todayCheckIns = checkins.size,
            currentlyInGym = members.count { it.isCheckedIn },
            monthlyRevenuePkr = paymentsList.sumOf { it.amountPkr },
            occupiedLockers = lockerList.count { it.isOccupied }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GymKpis())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    fun selectMember(member: GymMemberEntity?) {
        _selectedMember.value = member
    }

    // Save Member (with goal, plan, diet, locker)
    fun saveMember(
        id: Long = 0L,
        name: String,
        phone: String,
        gender: String = "Male",
        plan: String = "Monthly Standard",
        durationDays: Int = 30,
        amountPkr: Double = 3000.0,
        admissionFeePkr: Double = 1000.0,
        pendingDuePkr: Double = 0.0,
        fitnessGoal: String = "Weight Loss",
        workoutPlan: String = "Push-Pull-Legs",
        dietPlan: String = "High Protein",
        lockerNumber: String = "",
        assignedTrainer: String = "",
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
                plan = plan,
                durationDays = durationDays,
                amountPkr = amountPkr,
                admissionFeePkr = admissionFeePkr,
                pendingDuePkr = pendingDuePkr,
                fitnessGoal = fitnessGoal,
                workoutPlan = workoutPlan,
                dietPlan = dietPlan,
                lockerNumber = lockerNumber.trim(),
                assignedTrainer = assignedTrainer.trim(),
                isCheckedIn = existing?.isCheckedIn ?: false,
                lastCheckInTime = existing?.lastCheckInTime,
                startDate = existing?.startDate ?: System.currentTimeMillis()
            )
            val generatedId = if (id == 0L) gymDao.insertMember(member) else { gymDao.updateMember(member); id }

            // If new member and fee collected, record payment
            if (id == 0L && amountPkr > 0) {
                gymDao.insertPayment(
                    GymPaymentEntity(
                        businessId = bizId,
                        memberId = generatedId,
                        memberName = name.trim(),
                        amountPkr = amountPkr + admissionFeePkr,
                        paymentType = "Membership & Admission Fee",
                        paymentMethod = "Cash"
                    )
                )
            }
            onComplete(generatedId)
        }
    }

    // Toggle Attendance Check-In / Out
    fun toggleCheckIn(member: GymMemberEntity) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val now = System.currentTimeMillis()
            val dateStr = todayDateString

            if (!member.isCheckedIn) {
                gymDao.updateCheckInStatus(member.id, true, now)
                gymDao.insertCheckIn(
                    GymCheckInEntity(
                        businessId = bizId,
                        memberId = member.id,
                        memberName = member.name,
                        checkInTime = now,
                        dateString = dateStr
                    )
                )
            } else {
                gymDao.updateCheckInStatus(member.id, false, member.lastCheckInTime)
                val open = gymDao.getOpenCheckIn(member.id, dateStr)
                if (open != null) {
                    gymDao.updateCheckIn(open.copy(checkOutTime = now))
                }
            }
        }
    }

    // Instant Check-In or Check-Out by phone, member name or scanned code
    fun checkInByQuery(query: String, onResult: (String) -> Unit) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val q = query.trim()
            val member = rawMembers.value.find {
                it.phone.equals(q, ignoreCase = true) ||
                it.name.equals(q, ignoreCase = true) ||
                it.id.toString() == q ||
                it.lockerNumber.equals(q, ignoreCase = true)
            }
            if (member == null) {
                onResult("Member not found for: '$q'")
                return@launchWithLoading
            }

            toggleCheckIn(member)
            val action = if (member.isCheckedIn) "Checked Out" else "Checked In"
            onResult("Successfully $action: ${member.name}")
        }
    }

    // Record Fee Payment & Settle Dues
    fun collectFeePayment(
        memberId: Long,
        memberName: String,
        amountPkr: Double,
        paymentType: String = "Monthly Renewal Fee",
        paymentMethod: String = "Cash",
        additionalDays: Int = 30
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            gymDao.insertPayment(
                GymPaymentEntity(
                    businessId = bizId,
                    memberId = memberId,
                    memberName = memberName,
                    amountPkr = amountPkr,
                    paymentType = paymentType,
                    paymentMethod = paymentMethod
                )
            )

            // Renew member validity & reduce due
            val member = gymDao.getMemberByIdDirect(memberId)
            if (member != null) {
                val newDue = (member.pendingDuePkr - amountPkr).coerceAtLeast(0.0)
                val newStart = if (member.isExpired) System.currentTimeMillis() else member.startDate
                val newDuration = if (member.isExpired) additionalDays else member.durationDays + additionalDays
                gymDao.updateMember(member.copy(pendingDuePkr = newDue, startDate = newStart, durationDays = newDuration))
            }
        }
    }

    // Locker assignment
    fun assignLocker(lockerNumber: String, memberId: Long?, memberName: String, monthlyFee: Double = 500.0) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val isOccupied = memberId != null
            gymDao.insertLocker(
                GymLockerEntity(
                    businessId = bizId,
                    lockerNumber = lockerNumber,
                    assignedMemberId = memberId,
                    assignedMemberName = memberName,
                    monthlyFeePkr = monthlyFee,
                    isOccupied = isOccupied
                )
            )
        }
    }

    fun deleteMember(id: Long) {
        launchWithLoading {
            gymDao.deleteMemberById(id)
            if (_selectedMember.value?.id == id) _selectedMember.value = null
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GymManagementViewModel(application) as T
                }
            }
    }
}
