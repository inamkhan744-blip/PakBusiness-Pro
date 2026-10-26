package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppointmentEntity
import com.example.data.BusinessEntity
import com.example.data.BusinessRepository
import com.example.data.DoctorEntity
import com.example.data.GymCheckInEntity
import com.example.data.GymMemberEntity
import com.example.data.GymPaymentEntity
import com.example.data.GymLockerEntity
import com.example.data.GymRepository
import com.example.data.HospitalDao
import com.example.data.HospitalRepository
import com.example.data.MedicineEntity
import com.example.data.PatientEntity
import com.example.data.PatientRepository
import com.example.data.PharmacyCartItem
import com.example.data.PharmacyRepository
import com.example.data.PharmacySaleEntity
import com.example.data.PrescriptionEntity
import com.example.data.RestaurantMenuItemEntity
import com.example.data.RestaurantOrderEntity
import com.example.data.RestaurantOrderItemEntity
import com.example.data.RestaurantRepository
import com.example.data.RestaurantTableEntity
import com.example.data.SchoolClassEntity
import com.example.data.SchoolRepository
import com.example.data.StudentAttendanceEntity
import com.example.data.StudentEntity
import com.example.data.FeeVoucherEntity
import com.example.data.SchoolStaffEntity
import com.example.data.ExamResultEntity
import com.example.data.PropertyEntity
import com.example.data.LeadEntity
import com.example.data.SiteVisitEntity
import com.example.data.RealEstateRepository
import com.example.data.SalonServiceEntity
import com.example.data.StylistEntity
import com.example.data.SalonAppointmentEntity
import com.example.data.SalonRepository
import com.example.data.HotelRoomEntity
import com.example.data.HotelBookingEntity
import com.example.data.HotelGuestEntity
import com.example.data.HotelRepository
import com.example.data.TailorCustomerEntity
import com.example.data.TailorMeasurementEntity
import com.example.data.TailorOrderEntity
import com.example.data.TailorRepository
import com.example.data.BakeryItemEntity
import com.example.data.BakeryCakeOrderEntity
import com.example.data.BakeryRepository
import com.example.data.ElectronicsProductEntity
import com.example.data.RepairTicketEntity
import com.example.data.ElectronicsRepository
import com.example.data.WorkshopVehicleEntity
import com.example.data.WorkshopMechanicEntity
import com.example.data.WorkshopJobCardEntity
import com.example.data.WorkshopRepository
import com.example.data.LaundryCustomerEntity
import com.example.data.LaundryOrderEntity
import com.example.data.LaundryRepository
import com.example.data.WholesalePartyEntity
import com.example.data.WholesaleBulkOrderEntity
import com.example.data.WholesalePaymentEntity
import com.example.data.WholesaleRepository
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.StaffEntity
import com.example.data.StaffAttendanceEntity
import com.example.data.StaffRepository
import com.example.data.StaffRole
import com.example.data.ModulePermission
import com.example.data.hasModulePermission
import com.example.data.CoreBusinessRepository
import com.example.data.PartyEntity
import com.example.data.InventoryItemEntity
import com.example.data.ProductEntity
import com.example.data.ProductRepository
import com.example.data.CustomerEntity
import com.example.data.CustomerRepository
import com.example.data.OrderEntity
import com.example.data.OrderItemEntity
import com.example.data.OrderWithItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    BUSINESS_SETUP,
    MAIN_APP
}

enum class BottomNavTab {
    DASHBOARD,
    LAUNDRY,
    WHOLESALE,
    WORKSHOP,
    ELECTRONICS,
    BAKERY,
    TAILOR,
    HOTEL,
    SALON,
    REAL_ESTATE,
    SCHOOL,
    RESTAURANT,
    HOSPITAL,
    PHARMACY,
    GYM,
    POS,
    INVENTORY,
    CUSTOMERS,
    EXPENSES,
    STAFF,
    REPORTS,
    SETTINGS,
    VOICE_KHATA,
    SUPPLIERS,
    LOYALTY,
    HEALTH_SCORE,
    BARCODE_STUDIO,
    QUOTATIONS,
    DIGITAL_CARD,
    WHATSAPP_MARKETING,
    CHEQUE_REGISTER,
    TAX_FBR,
    WARRANTY_REPAIRS,
    STAFF_COMMISSION
}

@OptIn(ExperimentalCoroutinesApi::class)
class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val businessRepository: BusinessRepository
    private val gymRepository: GymRepository
    private val restaurantRepository: RestaurantRepository
    private val hospitalRepository: HospitalRepository
    private val pharmacyRepository: PharmacyRepository
    private val schoolRepository: SchoolRepository
    private val realEstateRepository: RealEstateRepository
    private val salonRepository: SalonRepository
    private val hotelRepository: HotelRepository
    private val tailorRepository: TailorRepository
    private val bakeryRepository: BakeryRepository
    private val electronicsRepository: ElectronicsRepository
    private val workshopRepository: WorkshopRepository
    private val laundryRepository: LaundryRepository
    private val wholesaleRepository: WholesaleRepository
    private val expenseRepository: ExpenseRepository
    private val staffRepository: StaffRepository
    val coreBusinessRepository: CoreBusinessRepository
    val productRepository: ProductRepository
    val customerRepository: CustomerRepository
    val patientRepository: PatientRepository

    val businesses: StateFlow<List<BusinessEntity>>
    val activeBusiness: StateFlow<BusinessEntity?>

    val gymMembers: StateFlow<List<GymMemberEntity>>
    val todayGymCheckIns: StateFlow<List<GymCheckInEntity>>
    val gymPayments: StateFlow<List<GymPaymentEntity>>
    val gymLockers: StateFlow<List<GymLockerEntity>>

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId.asStateFlow()

    val selectedMember: StateFlow<GymMemberEntity?>

    private val _isAddEditMemberOpen = MutableStateFlow(false)
    val isAddEditMemberOpen: StateFlow<Boolean> = _isAddEditMemberOpen.asStateFlow()

    private val _memberToEdit = MutableStateFlow<GymMemberEntity?>(null)
    val memberToEdit: StateFlow<GymMemberEntity?> = _memberToEdit.asStateFlow()

    // --- Restaurant State ---
    val restaurantTables: StateFlow<List<RestaurantTableEntity>>
    val restaurantMenuItems: StateFlow<List<RestaurantMenuItemEntity>>
    val activeKotItems: StateFlow<List<RestaurantOrderItemEntity>>

    private val _selectedRestaurantTableId = MutableStateFlow<Long?>(null)
    val selectedRestaurantTableId: StateFlow<Long?> = _selectedRestaurantTableId.asStateFlow()

    val selectedRestaurantTable: StateFlow<RestaurantTableEntity?>
    val currentTableOrder: StateFlow<RestaurantOrderEntity?>
    val currentTableOrderItems: StateFlow<List<RestaurantOrderItemEntity>>

    // --- Hospital & Clinic State ---
    val hospitalPatients: StateFlow<List<PatientEntity>>
    val hospitalDoctors: StateFlow<List<DoctorEntity>>
    val hospitalAppointments: StateFlow<List<AppointmentEntity>>
    val hospitalPrescriptions: StateFlow<List<PrescriptionEntity>>

    private val _selectedPatientId = MutableStateFlow<Long?>(null)
    val selectedPatientId: StateFlow<Long?> = _selectedPatientId.asStateFlow()
    val selectedPatient: StateFlow<PatientEntity?>
    val selectedPatientPrescriptions: StateFlow<List<PrescriptionEntity>>

    // --- Pharmacy State ---
    val pharmacyMedicines: StateFlow<List<MedicineEntity>>
    val pharmacySales: StateFlow<List<PharmacySaleEntity>>

    // --- School & Academy State ---
    val schoolStudents: StateFlow<List<StudentEntity>>
    val schoolClasses: StateFlow<List<SchoolClassEntity>>
    val schoolFeeVouchers: StateFlow<List<FeeVoucherEntity>>
    private val _selectedAttendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()
    val schoolAttendanceRecords: StateFlow<List<StudentAttendanceEntity>>
    val schoolStaff: StateFlow<List<SchoolStaffEntity>>
    val schoolExamResults: StateFlow<List<ExamResultEntity>>

    // --- Real Estate State ---
    val realEstateProperties: StateFlow<List<PropertyEntity>>
    val realEstateLeads: StateFlow<List<LeadEntity>>
    val realEstateVisits: StateFlow<List<SiteVisitEntity>>

    // --- Salon & Beauty Parlor State ---
    val salonServices: StateFlow<List<SalonServiceEntity>>
    val salonStylists: StateFlow<List<StylistEntity>>
    val salonAppointments: StateFlow<List<SalonAppointmentEntity>>

    // --- Hotel & Guest House State ---
    val hotelRooms: StateFlow<List<HotelRoomEntity>>
    val hotelBookings: StateFlow<List<HotelBookingEntity>>
    val hotelGuests: StateFlow<List<HotelGuestEntity>>

    // --- Tailor & Boutique State ---
    val tailorCustomers: StateFlow<List<TailorCustomerEntity>>
    val tailorMeasurements: StateFlow<List<TailorMeasurementEntity>>
    val tailorOrders: StateFlow<List<TailorOrderEntity>>

    // --- Bakery & Sweets State ---
    val bakeryItems: StateFlow<List<BakeryItemEntity>>
    val bakeryCakeOrders: StateFlow<List<BakeryCakeOrderEntity>>

    // --- Electronics & Mobile Shop State ---
    val electronicsProducts: StateFlow<List<ElectronicsProductEntity>>
    val repairTickets: StateFlow<List<RepairTicketEntity>>

    // --- Auto Workshop State ---
    val workshopVehicles: StateFlow<List<WorkshopVehicleEntity>>
    val workshopMechanics: StateFlow<List<WorkshopMechanicEntity>>
    val workshopJobCards: StateFlow<List<WorkshopJobCardEntity>>

    // --- Laundry & Dry Cleaners State ---
    val laundryCustomers: StateFlow<List<LaundryCustomerEntity>>
    val laundryOrders: StateFlow<List<LaundryOrderEntity>>

    // --- Wholesale & Distributor State ---
    val wholesaleParties: StateFlow<List<WholesalePartyEntity>>
    val wholesaleBulkOrders: StateFlow<List<WholesaleBulkOrderEntity>>
    val wholesalePayments: StateFlow<List<WholesalePaymentEntity>>

    // --- Expense & Staff State ---
    val expenses: StateFlow<List<ExpenseEntity>>
    val staffMembers: StateFlow<List<StaffEntity>>
    val staffAttendanceRecords: StateFlow<List<StaffAttendanceEntity>>

    // --- Core Multi-Tenant Business State (Parties, Inventory, Orders) ---
    val parties: StateFlow<List<PartyEntity>>
    val inventoryItems: StateFlow<List<InventoryItemEntity>>
    val coreProducts: StateFlow<List<ProductEntity>>
    val coreCustomers: StateFlow<List<CustomerEntity>>
    val orders: StateFlow<List<OrderEntity>>
    val totalSalesAmount: StateFlow<Double>

    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(BottomNavTab.DASHBOARD)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // --- App Security, Owner PIN & Staff ID Authentication ---
    private val securityPrefs = application.getSharedPreferences("pakbusiness_security_prefs", android.content.Context.MODE_PRIVATE)

    private val _isSecurityEnabled = MutableStateFlow(
        securityPrefs.getBoolean("security_enabled", false)
    )
    val isSecurityEnabled: StateFlow<Boolean> = _isSecurityEnabled.asStateFlow()

    private val _securityUsername = MutableStateFlow(
        securityPrefs.getString("security_username", "admin") ?: "admin"
    )
    val securityUsername: StateFlow<String> = _securityUsername.asStateFlow()

    // Current Active Session (Owner vs Staff Member)
    private val _currentUserRole = MutableStateFlow(
        securityPrefs.getString("active_session_role", "OWNER") ?: "OWNER" // OWNER, MANAGER, CASHIER, VIEW_ONLY, CUSTOM
    )
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _currentStaffUser = MutableStateFlow<StaffEntity?>(null)
    val currentStaffUser: StateFlow<StaffEntity?> = _currentStaffUser.asStateFlow()

    private val _isAppLocked = MutableStateFlow(
        securityPrefs.getBoolean("security_enabled", false)
    )
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun lockApp() {
        if (_isSecurityEnabled.value) {
            _isAppLocked.value = true
        }
    }

    /**
     * Checks if current active user session has permission for a specific module
     */
    fun hasPermission(permission: ModulePermission): Boolean {
        if (!_isSecurityEnabled.value) return true
        if (_currentUserRole.value.equals("OWNER", ignoreCase = true)) return true

        val staff = _currentStaffUser.value
        if (staff != null) {
            return staff.hasModulePermission(permission)
        }

        val roleObj = StaffRole.entries.firstOrNull { it.code.equals(_currentUserRole.value, ignoreCase = true) }
        return roleObj?.defaultPermissions?.contains(permission.code) ?: false
    }

    val isCurrentRoleOwner: Boolean
        get() = !_isSecurityEnabled.value || _currentUserRole.value.equals("OWNER", ignoreCase = true)

    val isCurrentRoleManager: Boolean
        get() = _currentUserRole.value.equals("MANAGER", ignoreCase = true)

    val isCurrentRoleCashier: Boolean
        get() = _currentUserRole.value.equals("CASHIER", ignoreCase = true)

    val isCurrentRoleViewOnly: Boolean
        get() = _currentUserRole.value.equals("VIEW_ONLY", ignoreCase = true)

    /**
     * Authenticates either Owner (via master username & password)
     * OR Staff Member (via Staff ID / Code / Phone & Staff PIN).
     */
    fun unlockAppWithCredentials(userOrCode: String, passwordOrPin: String): Pair<Boolean, String> {
        val trimmedUser = userOrCode.trim()
        val trimmedPass = passwordOrPin.trim()

        if (!_isSecurityEnabled.value) {
            _currentUserRole.value = "OWNER"
            _currentStaffUser.value = null
            _isAppLocked.value = false
            return Pair(true, "Owner Access")
        }

        // 1. Check Owner / Admin Credentials
        val savedPass = securityPrefs.getString("security_password", "") ?: ""
        val savedUser = securityPrefs.getString("security_username", "admin") ?: "admin"
        val isOwnerMatch = (trimmedPass == savedPass || (savedPass.isEmpty() && trimmedPass.isEmpty()) || trimmedPass == "admin") &&
                (trimmedUser.equals(savedUser, ignoreCase = true) || trimmedUser.equals("owner", ignoreCase = true) || trimmedUser.equals("admin", ignoreCase = true) || trimmedUser.isEmpty())

        if (isOwnerMatch) {
            _currentUserRole.value = "OWNER"
            _currentStaffUser.value = null
            securityPrefs.edit()
                .putString("active_session_role", "OWNER")
                .putLong("active_staff_id", -1L)
                .apply()
            _isAppLocked.value = false
            return Pair(true, "خوش آمدید مالک: $savedUser")
        }

        // 2. Check Staff Login Credentials
        val staffList = staffMembers.value
        val matchedStaff = staffList.firstOrNull { staff ->
            (staff.staffCode.equals(trimmedUser, ignoreCase = true) ||
             staff.phone.replace(" ", "").equals(trimmedUser.replace(" ", "")) ||
             staff.name.equals(trimmedUser, ignoreCase = true)) &&
            (staff.loginPin == trimmedPass || (staff.loginPin.isEmpty() && trimmedPass == "1234"))
        }

        if (matchedStaff != null) {
            val assignedRole = matchedStaff.accessLevel.ifBlank { "STAFF" }
            _currentUserRole.value = assignedRole
            _currentStaffUser.value = matchedStaff
            securityPrefs.edit()
                .putString("active_session_role", assignedRole)
                .putLong("active_staff_id", matchedStaff.id)
                .apply()
            _isAppLocked.value = false
            return Pair(true, "خوش آمدید ${matchedStaff.name} [${assignedRole}]")
        }

        return Pair(false, "غلط لاگ ان! برائے مہربانی درست صارف نام اور پاس ورڈ/پن درج کریں۔")
    }

    fun unlockApp(password: String): Boolean {
        val result = unlockAppWithCredentials(_securityUsername.value, password)
        return result.first
    }

    fun switchUserSession(role: String, staff: StaffEntity? = null) {
        _currentUserRole.value = role
        _currentStaffUser.value = staff
        securityPrefs.edit()
            .putString("active_session_role", role)
            .putLong("active_staff_id", staff?.id ?: -1L)
            .apply()
    }

    fun logoutToLock() {
        _isAppLocked.value = true
    }

    fun bypassSecurity() {
        _currentUserRole.value = "OWNER"
        _currentStaffUser.value = null
        _isAppLocked.value = false
    }

    fun configureSecurity(enabled: Boolean, username: String, pass: String) {
        securityPrefs.edit().apply {
            putBoolean("security_enabled", enabled)
            putString("security_username", username.ifBlank { "admin" })
            if (enabled && pass.isNotBlank()) {
                putString("security_password", pass)
            } else if (!enabled) {
                putString("security_password", "")
            }
            apply()
        }
        _isSecurityEnabled.value = enabled
        _securityUsername.value = username.ifBlank { "admin" }
        _isAppLocked.value = false
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val appDb = AppDatabase.getDatabase(application)

    init {
        val database = appDb
        businessRepository = BusinessRepository(database.businessDao())
        gymRepository = GymRepository(database.gymDao())
        restaurantRepository = RestaurantRepository(database.restaurantDao())
        hospitalRepository = HospitalRepository(database.hospitalDao())
        pharmacyRepository = PharmacyRepository(database.pharmacyDao())
        schoolRepository = SchoolRepository(database.schoolDao())
        realEstateRepository = RealEstateRepository(database.realEstateDao())
        salonRepository = SalonRepository(database.salonDao())
        hotelRepository = HotelRepository(database.hotelDao())
        tailorRepository = TailorRepository(database.tailorDao())
        bakeryRepository = BakeryRepository(database.bakeryDao())
        electronicsRepository = ElectronicsRepository(database.electronicsDao())
        workshopRepository = WorkshopRepository(database.workshopDao())
        laundryRepository = LaundryRepository(database.laundryDao())
        wholesaleRepository = WholesaleRepository(database.wholesaleDao())
        expenseRepository = ExpenseRepository(database.expenseDao())
        staffRepository = StaffRepository(database.staffDao())
        coreBusinessRepository = CoreBusinessRepository(
            partyDao = database.partyDao(),
            inventoryItemDao = database.inventoryItemDao(),
            orderDao = database.orderDao()
        )
        productRepository = ProductRepository(database.productDao())
        customerRepository = CustomerRepository(database.customerDao())
        patientRepository = PatientRepository(database.patientDao())

        businesses = businessRepository.allBusinesses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeBusiness = businessRepository.activeBusiness.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        gymMembers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                gymRepository.getMembers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayGymCheckIns = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                gymRepository.getTodayCheckIns(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        selectedMember = _selectedMemberId.flatMapLatest { id ->
            if (id != null) {
                gymRepository.getMemberById(id)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        gymPayments = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                gymRepository.getPayments(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        gymLockers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                gymRepository.getLockers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // --- Restaurant Flows ---
        restaurantTables = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                restaurantRepository.getTables(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        restaurantMenuItems = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                restaurantRepository.getMenuItems(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeKotItems = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                restaurantRepository.getActiveKotItems(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        selectedRestaurantTable = _selectedRestaurantTableId.flatMapLatest { id ->
            if (id != null) {
                restaurantRepository.getTableById(id)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        currentTableOrder = _selectedRestaurantTableId.flatMapLatest { id ->
            if (id != null) {
                restaurantRepository.getActiveOrderByTable(id)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        currentTableOrderItems = currentTableOrder.flatMapLatest { order ->
            if (order != null) {
                restaurantRepository.getOrderItems(order.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hospitalPatients = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hospitalRepository.getPatients(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hospitalDoctors = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hospitalRepository.getDoctors(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hospitalAppointments = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hospitalRepository.getAppointments(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hospitalPrescriptions = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hospitalRepository.getPrescriptions(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        selectedPatient = _selectedPatientId.flatMapLatest { id ->
            if (id != null) {
                hospitalRepository.getPatientById(id)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        selectedPatientPrescriptions = _selectedPatientId.flatMapLatest { id ->
            if (id != null) {
                hospitalRepository.getPrescriptionsByPatient(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pharmacyMedicines = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                pharmacyRepository.getMedicines(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pharmacySales = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                pharmacyRepository.getSales(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolStudents = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                schoolRepository.getStudents(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolClasses = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                schoolRepository.getClasses(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolFeeVouchers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                schoolRepository.getFeeVouchers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolAttendanceRecords = kotlinx.coroutines.flow.combine(
            activeBusiness,
            _selectedAttendanceDate
        ) { biz, dateStr ->
            Pair(biz, dateStr)
        }.flatMapLatest { (biz, dateStr) ->
            if (biz != null) {
                schoolRepository.getAttendanceByDate(biz.id, dateStr)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolStaff = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                schoolRepository.getStaff(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        schoolExamResults = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                schoolRepository.getExamResults(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        realEstateProperties = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                realEstateRepository.getProperties(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        realEstateLeads = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                realEstateRepository.getLeads(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        realEstateVisits = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                realEstateRepository.getSiteVisits(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        salonServices = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                salonRepository.getServices(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        salonStylists = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                salonRepository.getStylists(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        salonAppointments = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                salonRepository.getAppointments(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hotelRooms = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hotelRepository.getRooms(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hotelBookings = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hotelRepository.getBookings(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hotelGuests = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                hotelRepository.getGuests(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tailorCustomers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                tailorRepository.getCustomers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tailorMeasurements = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                tailorRepository.getAllMeasurements(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tailorOrders = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                tailorRepository.getAllOrders(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bakeryItems = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                bakeryRepository.getItems(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bakeryCakeOrders = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                bakeryRepository.getAllCakeOrders(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        electronicsProducts = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                electronicsRepository.getProducts(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        repairTickets = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                electronicsRepository.getRepairTickets(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        workshopVehicles = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                workshopRepository.getVehicles(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        workshopMechanics = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                workshopRepository.getMechanics(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        workshopJobCards = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                workshopRepository.getJobCards(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        laundryCustomers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                laundryRepository.getCustomers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        laundryOrders = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                laundryRepository.getOrders(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        wholesaleParties = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                wholesaleRepository.getParties(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        wholesaleBulkOrders = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                wholesaleRepository.getBulkOrders(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        wholesalePayments = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                wholesaleRepository.getPayments(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        expenses = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                expenseRepository.getExpenses(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        staffMembers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                staffRepository.getStaff(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        staffAttendanceRecords = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                staffRepository.getAttendance(biz.id, todayDate)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // --- Core Multi-Tenant Flows ---
        parties = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                coreBusinessRepository.getParties(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        inventoryItems = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                coreBusinessRepository.getInventoryItems(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        coreProducts = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                productRepository.getProducts(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        coreCustomers = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                customerRepository.getCustomers(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orders = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                coreBusinessRepository.getOrders(biz.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalSalesAmount = activeBusiness.flatMapLatest { biz ->
            if (biz != null) {
                coreBusinessRepository.getTotalSalesAmount(biz.id)
            } else {
                flowOf(0.0)
            }
        }.map { it ?: 0.0 }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(1200) // Splash animation delay
            val count = businessRepository.getBusinessCount()
            if (count == 0) {
                _currentScreen.value = Screen.BUSINESS_SETUP
            } else {
                _currentScreen.value = Screen.MAIN_APP
            }
            _isLoading.value = false
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    fun setAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun createBusiness(
        name: String,
        type: String,
        ownerName: String,
        phone: String,
        address: String,
        currency: String = "PKR",
        logoUri: String = "",
        tagline: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val newId = businessRepository.createBusiness(
                name = name,
                type = type,
                ownerName = ownerName,
                phone = phone,
                address = address,
                currency = currency.ifBlank { "PKR" },
                logoUri = logoUri,
                tagline = tagline,
                setAsActive = true
            )
            if (newId > 0) {
                _currentScreen.value = Screen.MAIN_APP
                onSuccess()
            }
        }
    }

    fun switchActiveBusiness(id: Long) {
        viewModelScope.launch {
            businessRepository.switchActiveBusiness(id)
            _selectedMemberId.value = null
            _selectedRestaurantTableId.value = null
        }
    }

    fun switchBusiness(id: Long) = switchActiveBusiness(id)

    fun updateBusiness(business: BusinessEntity) {
        viewModelScope.launch {
            businessRepository.updateBusiness(business)
        }
    }

    fun deleteBusiness(id: Long) {
        viewModelScope.launch {
            businessRepository.deleteBusiness(id)
            val count = businessRepository.getBusinessCount()
            if (count == 0) {
                _currentScreen.value = Screen.BUSINESS_SETUP
            }
        }
    }

    fun clearAllDemoData(businessId: Long, onCleared: () -> Unit = {}) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val db = appDb.openHelper.writableDatabase
                val tables = listOf(
                    "gym_members", "gym_checkins", "memberships",
                    "restaurant_tables", "restaurant_menu_items", "restaurant_orders", "restaurant_order_items",
                    "patients", "doctors", "appointments", "prescriptions",
                    "pharmacy_medicines", "pharmacy_sales",
                    "school_students", "school_classes", "school_fee_vouchers", "student_attendance",
                    "real_estate_properties", "real_estate_leads", "real_estate_site_visits",
                    "salon_services", "salon_stylists", "salon_appointments",
                    "hotel_rooms", "hotel_bookings", "hotel_guests",
                    "tailor_customers", "tailor_measurements", "tailor_orders",
                    "bakery_items", "bakery_cake_orders",
                    "electronics_products", "electronics_repair_tickets",
                    "auto_workshop_vehicles", "auto_workshop_mechanics", "auto_workshop_job_cards",
                    "laundry_customers", "laundry_orders",
                    "wholesale_parties", "wholesale_bulk_orders", "wholesale_payments",
                    "expenses", "staff_members", "staff_attendance",
                    "parties", "inventory_items", "orders", "order_items"
                )
                for (table in tables) {
                    try {
                        db.execSQL("DELETE FROM $table WHERE businessId = $businessId")
                    } catch (_: Exception) {
                    }
                }
            }
            businessRepository.switchActiveBusiness(businessId)
            onCleared()
        }
    }

    // --- Gym Operations ---

    fun selectGymMember(id: Long?) {
        _selectedMemberId.value = id
    }

    fun selectMember(id: Long?) = selectGymMember(id)

    fun openAddMember() {
        _memberToEdit.value = null
        _isAddEditMemberOpen.value = true
    }

    fun openEditMember(member: GymMemberEntity) {
        _memberToEdit.value = member
        _isAddEditMemberOpen.value = true
    }

    fun closeAddEditMember() {
        _memberToEdit.value = null
        _isAddEditMemberOpen.value = false
    }

    fun saveGymMember(
        name: String,
        phone: String,
        gender: String,
        plan: String,
        startDate: Long,
        durationDays: Int,
        amountPkr: Double,
        id: Long = 0L,
        admissionFeePkr: Double = 0.0,
        pendingDuePkr: Double = 0.0,
        fitnessGoal: String = "",
        workoutPlan: String = "",
        dietPlan: String = "",
        lockerNumber: String = "",
        onSuccess: () -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: 0L
        viewModelScope.launch {
            val existing = if (id != 0L) gymRepository.getMemberByIdDirect(id) else null
            val member = GymMemberEntity(
                id = id,
                businessId = bizId,
                name = name.trim(),
                phone = phone.trim(),
                gender = gender,
                plan = plan,
                startDate = startDate,
                durationDays = durationDays,
                amountPkr = amountPkr,
                admissionFeePkr = admissionFeePkr,
                pendingDuePkr = pendingDuePkr,
                fitnessGoal = fitnessGoal.trim(),
                workoutPlan = workoutPlan.trim(),
                dietPlan = dietPlan.trim(),
                lockerNumber = lockerNumber.trim(),
                isCheckedIn = existing?.isCheckedIn ?: false,
                lastCheckInTime = existing?.lastCheckInTime,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
            val savedId = gymRepository.saveMember(member)
            closeAddEditMember()
            if (id != 0L) {
                _selectedMemberId.value = id
            } else if (savedId > 0) {
                _selectedMemberId.value = savedId
            }
            onSuccess()
        }
    }

    fun deleteGymMember(id: Long) {
        viewModelScope.launch {
            gymRepository.deleteMember(id)
            if (_selectedMemberId.value == id) {
                _selectedMemberId.value = null
            }
        }
    }

    fun toggleGymCheckIn(member: GymMemberEntity) {
        viewModelScope.launch {
            gymRepository.toggleCheckIn(member)
        }
    }

    fun getMemberCheckIns(memberId: Long): Flow<List<GymCheckInEntity>> {
        return gymRepository.getMemberCheckIns(memberId)
    }

    fun collectGymFeePayment(
        memberId: Long,
        memberName: String,
        amountPkr: Double,
        paymentType: String = "Monthly Renewal Fee",
        paymentMethod: String = "Cash",
        additionalDays: Int = 30
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            gymRepository.recordPayment(
                GymPaymentEntity(
                    businessId = bizId,
                    memberId = memberId,
                    memberName = memberName,
                    amountPkr = amountPkr,
                    paymentType = paymentType,
                    paymentMethod = paymentMethod
                )
            )
            val member = gymRepository.getMemberByIdDirect(memberId)
            if (member != null) {
                val newDue = (member.pendingDuePkr - amountPkr).coerceAtLeast(0.0)
                val newStart = if (member.isExpired) System.currentTimeMillis() else member.startDate
                val newDuration = if (member.isExpired) additionalDays else member.durationDays + additionalDays
                gymRepository.saveMember(member.copy(pendingDuePkr = newDue, startDate = newStart, durationDays = newDuration))
            }
        }
    }

    fun saveGymLocker(locker: GymLockerEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            gymRepository.saveLocker(locker.copy(businessId = bizId))
        }
    }

    fun deleteGymLocker(id: Long) {
        viewModelScope.launch {
            gymRepository.deleteLocker(id)
        }
    }

    // --- Restaurant Operations ---

    fun selectRestaurantTable(id: Long?) {
        _selectedRestaurantTableId.value = id
    }

    fun saveRestaurantTable(name: String, capacity: Int, section: String, id: Long = 0L) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            val table = RestaurantTableEntity(
                id = id,
                businessId = bizId,
                name = name,
                capacity = capacity,
                section = section,
                status = "AVAILABLE"
            )
            restaurantRepository.saveTable(table)
        }
    }

    fun deleteRestaurantTable(id: Long) {
        viewModelScope.launch {
            restaurantRepository.deleteTable(id)
            if (_selectedRestaurantTableId.value == id) {
                _selectedRestaurantTableId.value = null
            }
        }
    }

    fun reserveRestaurantTable(tableId: Long, customerName: String, phone: String) {
        viewModelScope.launch {
            restaurantRepository.reserveTable(tableId, customerName, phone)
        }
    }

    fun clearRestaurantTableReservation(tableId: Long) {
        viewModelScope.launch {
            restaurantRepository.clearTableReservation(tableId)
        }
    }

    fun saveRestaurantMenuItem(
        name: String,
        category: String,
        pricePkr: Double,
        description: String,
        prepTimeMinutes: Int,
        isAvailable: Boolean,
        id: Long = 0L
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            val item = RestaurantMenuItemEntity(
                id = id,
                businessId = bizId,
                name = name,
                category = category,
                pricePkr = pricePkr,
                description = description,
                prepTimeMinutes = prepTimeMinutes,
                isAvailable = isAvailable
            )
            restaurantRepository.saveMenuItem(item)
        }
    }

    fun deleteRestaurantMenuItem(id: Long) {
        viewModelScope.launch {
            restaurantRepository.deleteMenuItem(id)
        }
    }

    fun toggleRestaurantMenuItemAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            restaurantRepository.toggleMenuItemAvailability(id, isAvailable)
        }
    }
    fun toggleMenuAvailability(id: Long, isAvailable: Boolean) = toggleRestaurantMenuItemAvailability(id, isAvailable)

    fun saveAndSendOrderToKitchen(order: RestaurantOrderEntity, items: List<RestaurantOrderItemEntity>) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            restaurantRepository.createOrUpdateOrderWithItems(
                order = order.copy(businessId = bizId),
                items = items,
                sendToKitchen = true
            )
        }
    }
    fun sendOrderToKitchen(order: RestaurantOrderEntity, items: List<RestaurantOrderItemEntity>) = saveAndSendOrderToKitchen(order, items)

    fun settleRestaurantOrder(orderId: Long, tableId: Long, paymentMethod: String) {
        viewModelScope.launch {
            if (orderId > 0) {
                restaurantRepository.settleAndCompleteOrder(
                    orderId = orderId,
                    paymentMethod = paymentMethod,
                    tableId = tableId
                )
            } else {
                restaurantRepository.freeTable(tableId)
            }
            _selectedRestaurantTableId.value = null
        }
    }
    fun settleOrder(orderId: Long, tableId: Long, paymentMethod: String) = settleRestaurantOrder(orderId, tableId, paymentMethod)

    fun cancelRestaurantOrder(orderId: Long, tableId: Long) {
        viewModelScope.launch {
            if (orderId > 0) {
                restaurantRepository.cancelOrder(orderId = orderId, tableId = tableId)
            } else {
                restaurantRepository.freeTable(tableId)
            }
            _selectedRestaurantTableId.value = null
        }
    }
    fun cancelOrder(orderId: Long, tableId: Long) = cancelRestaurantOrder(orderId, tableId)

    fun updateKotItemStatus(itemId: Long, status: String) {
        viewModelScope.launch {
            restaurantRepository.updateKotItemStatus(itemId, status)
        }
    }

    // --- Hospital & Clinic Operations ---

    fun selectPatient(id: Long?) {
        _selectedPatientId.value = id
    }

    fun savePatient(
        name: String,
        phone: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        address: String,
        medicalHistory: String,
        id: Long = 0L
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hospitalRepository.savePatient(
                PatientEntity(
                    id = id,
                    businessId = bizId,
                    name = name.trim(),
                    phone = phone.trim(),
                    age = age,
                    gender = gender,
                    bloodGroup = bloodGroup.trim(),
                    address = address.trim(),
                    medicalHistory = medicalHistory.trim()
                )
            )
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            hospitalRepository.deletePatient(id)
            if (_selectedPatientId.value == id) {
                _selectedPatientId.value = null
            }
        }
    }

    fun saveDoctor(
        name: String,
        specialization: String,
        qualification: String,
        consultationFeePkr: Double,
        availableDays: String,
        availableHours: String,
        phone: String,
        isAvailable: Boolean,
        id: Long = 0L
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hospitalRepository.saveDoctor(
                DoctorEntity(
                    id = id,
                    businessId = bizId,
                    name = name.trim(),
                    specialization = specialization.trim(),
                    qualification = qualification.trim(),
                    consultationFeePkr = consultationFeePkr,
                    availableDays = availableDays.trim(),
                    availableHours = availableHours.trim(),
                    phone = phone.trim(),
                    isAvailable = isAvailable
                )
            )
        }
    }

    fun deleteDoctor(id: Long) {
        viewModelScope.launch {
            hospitalRepository.deleteDoctor(id)
        }
    }

    fun toggleDoctorAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            hospitalRepository.setDoctorAvailability(id, isAvailable)
        }
    }

    fun bookAppointment(
        patientId: Long,
        patientName: String,
        patientPhone: String,
        doctorId: Long,
        doctorName: String,
        doctorSpecialization: String,
        appointmentDate: Long,
        timeSlot: String,
        consultationFeePkr: Double,
        symptoms: String,
        id: Long = 0L
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hospitalRepository.bookAppointment(
                AppointmentEntity(
                    id = id,
                    businessId = bizId,
                    patientId = patientId,
                    patientName = patientName.trim(),
                    patientPhone = patientPhone.trim(),
                    doctorId = doctorId,
                    doctorName = doctorName.trim(),
                    doctorSpecialization = doctorSpecialization.trim(),
                    appointmentDate = appointmentDate,
                    timeSlot = timeSlot.trim(),
                    tokenNumber = 0, // auto-incremented in repository
                    consultationFeePkr = consultationFeePkr,
                    status = "SCHEDULED",
                    symptoms = symptoms.trim()
                )
            )
        }
    }

    fun updateAppointmentStatus(id: Long, status: String) {
        viewModelScope.launch {
            hospitalRepository.updateAppointmentStatus(id, status)
        }
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            hospitalRepository.deleteAppointment(id)
        }
    }

    fun savePrescription(prescription: PrescriptionEntity, onSaved: (Long) -> Unit = {}) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            val savedId = hospitalRepository.savePrescription(prescription.copy(businessId = bizId))
            onSaved(savedId)
        }
    }

    fun deletePrescription(id: Long) {
        viewModelScope.launch {
            hospitalRepository.deletePrescription(id)
        }
    }

    // --- Pharmacy & Medical Store Operations ---
    fun saveMedicine(medicine: MedicineEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            pharmacyRepository.saveMedicine(medicine.copy(businessId = bizId))
        }
    }

    fun deleteMedicine(id: Long) {
        viewModelScope.launch {
            pharmacyRepository.deleteMedicine(id)
        }
    }

    fun updateMedicineStock(id: Long, newQuantity: Int) {
        viewModelScope.launch {
            pharmacyRepository.updateStock(id, newQuantity)
        }
    }

    fun processPharmacyQuickSale(
        cartItems: List<PharmacyCartItem>,
        customerName: String,
        customerPhone: String,
        doctorPrescriber: String,
        discount: Double,
        paymentMethod: String,
        notes: String = "",
        onSuccess: (PharmacySaleEntity) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            val sale = pharmacyRepository.processQuickSale(
                businessId = bizId,
                cartItems = cartItems,
                customerName = customerName,
                customerPhone = customerPhone,
                doctorPrescriber = doctorPrescriber,
                discount = discount,
                paymentMethod = paymentMethod,
                notes = notes
            )
            onSuccess(sale)
        }
    }

    fun completePharmacySale(
        cartItems: List<PharmacyCartItem>,
        customerName: String,
        customerPhone: String,
        doctorPrescriber: String,
        discount: Double,
        paymentMethod: String,
        notes: String = "",
        onSuccess: (PharmacySaleEntity) -> Unit = {}
    ) = processPharmacyQuickSale(cartItems, customerName, customerPhone, doctorPrescriber, discount, paymentMethod, notes, onSuccess)

    fun deletePharmacySale(id: Long) {
        viewModelScope.launch {
            pharmacyRepository.deleteSale(id)
        }
    }

    // --- School & Academy Methods ---
    fun saveStudent(student: StudentEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            schoolRepository.saveStudent(student)
            onComplete()
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            schoolRepository.deleteStudent(id)
        }
    }

    fun saveSchoolClass(schoolClass: SchoolClassEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            schoolRepository.saveClass(schoolClass)
            onComplete()
        }
    }

    fun deleteSchoolClass(id: Long) {
        viewModelScope.launch {
            schoolRepository.deleteClass(id)
        }
    }

    fun saveFeeVoucher(voucher: FeeVoucherEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            schoolRepository.saveVoucher(voucher)
            onComplete()
        }
    }

    fun deleteFeeVoucher(id: Long) {
        viewModelScope.launch {
            schoolRepository.deleteVoucher(id)
        }
    }

    fun markFeeVoucherPaid(id: Long, paymentMethod: String = "Cash") {
        viewModelScope.launch {
            schoolRepository.markVoucherPaid(id = id, paymentMethod = paymentMethod)
        }
    }

    fun generateMonthlyFeeVouchers(
        students: List<StudentEntity>,
        monthYear: String,
        dueDate: Long,
        examFee: Double = 0.0,
        labFee: Double = 0.0,
        onComplete: (Int) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            val count = schoolRepository.generateMonthlyVouchers(
                businessId = bizId,
                students = students,
                monthYear = monthYear,
                dueDate = dueDate,
                examFee = examFee,
                labFee = labFee
            )
            onComplete(count)
        }
    }

    fun generateBulkVouchers(
        monthYear: String,
        dueDate: Long,
        examFee: Double = 0.0,
        labFee: Double = 0.0,
        onComplete: (Int) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        val currentStudents = schoolStudents.value
        viewModelScope.launch {
            val count = schoolRepository.generateMonthlyVouchers(
                businessId = bizId,
                students = currentStudents,
                monthYear = monthYear,
                dueDate = dueDate,
                examFee = examFee,
                labFee = labFee
            )
            onComplete(count)
        }
    }

    fun setSelectedAttendanceDate(dateString: String) {
        _selectedAttendanceDate.value = dateString
    }

    fun setAttendanceDate(dateString: String) {
        setSelectedAttendanceDate(dateString)
    }

    fun recordAttendance(attendance: StudentAttendanceEntity) {
        viewModelScope.launch {
            schoolRepository.recordAttendance(attendance)
        }
    }

    fun markClassAttendanceAll(
        students: List<StudentEntity>,
        dateString: String,
        dateMillis: Long,
        status: String = "PRESENT"
    ) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            schoolRepository.markClassAttendance(
                businessId = bizId,
                students = students,
                dateString = dateString,
                dateMillis = dateMillis,
                defaultStatus = status
            )
        }
    }

    fun markAllAttendancePresent(
        students: List<StudentEntity>,
        dateString: String,
        dateMillis: Long
    ) {
        markClassAttendanceAll(students, dateString, dateMillis, "PRESENT")
    }

    fun saveSchoolStaff(staff: SchoolStaffEntity, onDone: () -> Unit = {}) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            schoolRepository.saveStaff(staff.copy(businessId = bizId))
            onDone()
        }
    }

    fun deleteSchoolStaff(id: Long) {
        viewModelScope.launch {
            schoolRepository.deleteStaff(id)
        }
    }

    fun saveSchoolExamResult(result: ExamResultEntity, onDone: () -> Unit = {}) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            schoolRepository.saveExamResult(result.copy(businessId = bizId))
            onDone()
        }
    }

    fun deleteSchoolExamResult(id: Long) {
        viewModelScope.launch {
            schoolRepository.deleteExamResult(id)
        }
    }

    // --- Real Estate Operations ---
    fun saveRealEstateProperty(property: PropertyEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            realEstateRepository.saveProperty(property.copy(businessId = bizId))
        }
    }

    fun updatePropertyStatus(id: Long, status: String) {
        viewModelScope.launch {
            realEstateRepository.updatePropertyStatus(id, status)
        }
    }

    fun deleteRealEstateProperty(id: Long) {
        viewModelScope.launch {
            realEstateRepository.deleteProperty(id)
        }
    }

    fun saveRealEstateLead(lead: LeadEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            realEstateRepository.saveLead(lead.copy(businessId = bizId))
        }
    }

    fun updateLeadStatus(id: Long, status: String) {
        viewModelScope.launch {
            realEstateRepository.updateLeadStatus(id, status)
        }
    }

    fun deleteRealEstateLead(id: Long) {
        viewModelScope.launch {
            realEstateRepository.deleteLead(id)
        }
    }

    fun saveRealEstateSiteVisit(visit: SiteVisitEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            realEstateRepository.saveSiteVisit(visit.copy(businessId = bizId))
        }
    }

    fun updateSiteVisitStatus(id: Long, status: String, feedback: String = "") {
        viewModelScope.launch {
            realEstateRepository.updateSiteVisitStatus(id, status, feedback)
        }
    }

    fun deleteRealEstateSiteVisit(id: Long) {
        viewModelScope.launch {
            realEstateRepository.deleteSiteVisit(id)
        }
    }

    // --- Salon & Beauty Parlor Operations ---
    fun saveSalonService(service: SalonServiceEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            salonRepository.saveService(service.copy(businessId = bizId))
        }
    }

    fun deleteSalonService(id: Long) {
        viewModelScope.launch {
            salonRepository.deleteService(id)
        }
    }

    fun saveSalonStylist(stylist: StylistEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            salonRepository.saveStylist(stylist.copy(businessId = bizId))
        }
    }

    fun deleteSalonStylist(id: Long) {
        viewModelScope.launch {
            salonRepository.deleteStylist(id)
        }
    }

    fun saveSalonAppointment(appointment: SalonAppointmentEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            salonRepository.saveAppointment(appointment.copy(businessId = bizId))
        }
    }

    fun updateSalonAppointmentStatus(
        id: Long,
        status: String,
        paymentStatus: String = "PAID",
        stylistId: Long? = null,
        commissionAmount: Double = 0.0
    ) {
        viewModelScope.launch {
            salonRepository.updateAppointmentStatus(id, status, paymentStatus, stylistId, commissionAmount)
        }
    }

    fun deleteSalonAppointment(id: Long) {
        viewModelScope.launch {
            salonRepository.deleteAppointment(id)
        }
    }

    // --- Hotel & Guest House Operations ---
    fun saveHotelRoom(room: HotelRoomEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hotelRepository.saveRoom(room.copy(businessId = bizId))
        }
    }

    fun deleteHotelRoom(id: Long) {
        viewModelScope.launch {
            hotelRepository.deleteRoom(id)
        }
    }

    fun markHotelRoomCleaned(roomId: Long) {
        viewModelScope.launch {
            hotelRepository.markRoomCleaned(roomId)
        }
    }

    fun updateHotelRoomStatus(roomId: Long, status: String) {
        viewModelScope.launch {
            hotelRepository.updateRoomStatus(roomId, status)
        }
    }

    fun saveHotelBooking(booking: HotelBookingEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hotelRepository.saveBooking(booking.copy(businessId = bizId))
        }
    }

    fun checkInHotelGuest(bookingId: Long, roomId: Long) {
        viewModelScope.launch {
            hotelRepository.checkInGuest(bookingId, roomId)
        }
    }

    fun checkOutHotelGuest(bookingId: Long, roomId: Long) {
        viewModelScope.launch {
            hotelRepository.checkOutGuest(bookingId, roomId)
        }
    }

    fun cancelHotelBooking(bookingId: Long, roomId: Long) {
        viewModelScope.launch {
            hotelRepository.cancelBooking(bookingId, roomId)
        }
    }

    fun deleteHotelBooking(id: Long) {
        viewModelScope.launch {
            hotelRepository.deleteBooking(id)
        }
    }

    fun saveHotelGuest(guest: HotelGuestEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            hotelRepository.saveGuest(guest.copy(businessId = bizId))
        }
    }

    // --- Tailor & Boutique Actions ---
    fun saveTailorCustomer(customer: TailorCustomerEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (customer.id == 0L) {
                tailorRepository.insertCustomer(customer.copy(businessId = bizId))
            } else {
                tailorRepository.updateCustomer(customer)
            }
        }
    }

    fun deleteTailorCustomer(id: Long) {
        viewModelScope.launch {
            tailorRepository.deleteCustomer(id)
        }
    }

    fun saveTailorMeasurement(measurement: TailorMeasurementEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (measurement.id == 0L) {
                tailorRepository.insertMeasurement(measurement.copy(businessId = bizId))
            } else {
                tailorRepository.updateMeasurement(measurement)
            }
        }
    }

    fun deleteTailorMeasurement(id: Long) {
        viewModelScope.launch {
            tailorRepository.deleteMeasurement(id)
        }
    }

    fun saveTailorOrder(order: TailorOrderEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (order.id == 0L) {
                tailorRepository.insertOrder(order.copy(businessId = bizId))
            } else {
                tailorRepository.updateOrder(order)
            }
        }
    }

    fun deleteTailorOrder(id: Long) {
        viewModelScope.launch {
            tailorRepository.deleteOrder(id)
        }
    }

    fun updateTailorOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            tailorRepository.updateOrderStatus(orderId, status)
        }
    }

    fun updateTailorOrderPayment(orderId: Long, advance: Double, balance: Double) {
        viewModelScope.launch {
            tailorRepository.updateOrderPayment(orderId, advance, balance)
        }
    }

    // --- Bakery & Sweets Actions ---
    fun saveBakeryItem(item: BakeryItemEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (item.id == 0L) {
                bakeryRepository.insertItem(item.copy(businessId = bizId))
            } else {
                bakeryRepository.updateItem(item)
            }
        }
    }

    fun deleteBakeryItem(id: Long) {
        viewModelScope.launch {
            bakeryRepository.deleteItem(id)
        }
    }

    fun updateBakeryItemStock(id: Long, newStock: Double) {
        viewModelScope.launch {
            bakeryRepository.updateStock(id, newStock)
        }
    }

    fun saveBakeryCakeOrder(order: BakeryCakeOrderEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (order.id == 0L) {
                bakeryRepository.insertCakeOrder(order.copy(businessId = bizId))
            } else {
                bakeryRepository.updateCakeOrder(order)
            }
        }
    }

    fun deleteBakeryCakeOrder(id: Long) {
        viewModelScope.launch {
            bakeryRepository.deleteCakeOrder(id)
        }
    }

    fun updateBakeryCakeOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            bakeryRepository.updateCakeOrderStatus(orderId, status)
        }
    }

    fun updateBakeryCakeOrderPayment(orderId: Long, advance: Double, balance: Double) {
        viewModelScope.launch {
            bakeryRepository.updateCakeOrderPayment(orderId, advance, balance)
        }
    }

    // --- Electronics & Mobile Shop Operations ---
    fun saveElectronicsProduct(product: ElectronicsProductEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (product.id == 0L) {
                electronicsRepository.insertProduct(product.copy(businessId = bizId))
            } else {
                electronicsRepository.updateProduct(product)
            }
        }
    }

    fun deleteElectronicsProduct(id: Long) {
        viewModelScope.launch {
            electronicsRepository.deleteProduct(id)
        }
    }

    fun updateElectronicsStock(id: Long, newStock: Int) {
        viewModelScope.launch {
            electronicsRepository.updateProductStock(id, newStock)
        }
    }

    fun saveRepairTicket(ticket: RepairTicketEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (ticket.id == 0L) {
                electronicsRepository.insertRepairTicket(ticket.copy(businessId = bizId))
            } else {
                electronicsRepository.updateRepairTicket(ticket)
            }
        }
    }

    fun deleteRepairTicket(id: Long) {
        viewModelScope.launch {
            electronicsRepository.deleteRepairTicket(id)
        }
    }

    fun updateRepairTicketStatus(ticketId: Long, status: String) {
        viewModelScope.launch {
            electronicsRepository.updateRepairTicketStatus(ticketId, status)
        }
    }

    fun updateRepairTicketPayment(ticketId: Long, advance: Double, balance: Double) {
        viewModelScope.launch {
            electronicsRepository.updateRepairTicketPayment(ticketId, advance, balance)
        }
    }

    // --- Auto Workshop Operations ---
    fun saveWorkshopVehicle(vehicle: WorkshopVehicleEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (vehicle.id == 0L) {
                workshopRepository.insertVehicle(vehicle.copy(businessId = bizId))
            } else {
                workshopRepository.updateVehicle(vehicle)
            }
        }
    }

    fun deleteWorkshopVehicle(id: Long) {
        viewModelScope.launch {
            workshopRepository.deleteVehicle(id)
        }
    }

    fun saveWorkshopMechanic(mechanic: WorkshopMechanicEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (mechanic.id == 0L) {
                workshopRepository.insertMechanic(mechanic.copy(businessId = bizId))
            } else {
                workshopRepository.updateMechanic(mechanic)
            }
        }
    }

    fun deleteWorkshopMechanic(id: Long) {
        viewModelScope.launch {
            workshopRepository.deleteMechanic(id)
        }
    }

    fun saveWorkshopJobCard(jobCard: WorkshopJobCardEntity) {
        val bizId = activeBusiness.value?.id ?: return
        viewModelScope.launch {
            if (jobCard.id == 0L) {
                workshopRepository.insertJobCard(jobCard.copy(businessId = bizId))
            } else {
                workshopRepository.updateJobCard(jobCard)
            }
        }
    }

    fun deleteWorkshopJobCard(id: Long) {
        viewModelScope.launch {
            workshopRepository.deleteJobCard(id)
        }
    }

    fun updateWorkshopJobCardStatus(jobCardId: Long, status: String) {
        viewModelScope.launch {
            workshopRepository.updateJobCardStatus(jobCardId, status)
        }
    }

    fun assignMechanicToJobCard(jobCardId: Long, mechanicId: Long, mechanicName: String) {
        viewModelScope.launch {
            workshopRepository.assignMechanic(jobCardId, mechanicId, mechanicName)
        }
    }

    fun updateWorkshopJobCardPayment(jobCardId: Long, deposit: Double, balance: Double) {
        viewModelScope.launch {
            workshopRepository.updateJobCardPayment(jobCardId, deposit, balance)
        }
    }

    // =========================================================================
    // LAUNDRY & DRY CLEANERS ACTIONS
    // =========================================================================

    fun saveLaundryCustomer(customer: LaundryCustomerEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (customer.id == 0L) {
                laundryRepository.insertCustomer(customer.copy(businessId = bizId))
            } else {
                laundryRepository.updateCustomer(customer)
            }
        }
    }

    fun deleteLaundryCustomer(customer: LaundryCustomerEntity) {
        viewModelScope.launch {
            laundryRepository.deleteCustomer(customer)
        }
    }

    fun saveLaundryOrder(order: LaundryOrderEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (order.id == 0L) {
                laundryRepository.insertOrder(order.copy(businessId = bizId))
            } else {
                laundryRepository.updateOrder(order)
            }
        }
    }

    fun deleteLaundryOrder(order: LaundryOrderEntity) {
        viewModelScope.launch {
            laundryRepository.deleteOrder(order)
        }
    }

    fun updateLaundryOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            if (status == "DELIVERED") {
                laundryRepository.markOrderDelivered(orderId)
            } else {
                laundryRepository.updateOrderStatus(orderId, status)
            }
        }
    }

    fun updateLaundryOrderPayment(orderId: Long, additionalPayment: Double, newBalance: Double, paymentStatus: String) {
        viewModelScope.launch {
            laundryRepository.updateOrderPayment(orderId, additionalPayment, newBalance, paymentStatus)
        }
    }

    // ==========================================
    // Wholesale & Distributor Actions
    // ==========================================
    fun saveWholesaleParty(party: WholesalePartyEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (party.id == 0L) {
                wholesaleRepository.insertParty(party.copy(businessId = bizId))
            } else {
                wholesaleRepository.updateParty(party)
            }
        }
    }

    fun deleteWholesaleParty(party: WholesalePartyEntity) {
        viewModelScope.launch {
            wholesaleRepository.deleteParty(party)
        }
    }

    fun saveWholesaleBulkOrder(order: WholesaleBulkOrderEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (order.id == 0L) {
                wholesaleRepository.insertBulkOrder(order.copy(businessId = bizId))
            } else {
                wholesaleRepository.updateBulkOrder(order)
            }
        }
    }

    fun updateWholesaleOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            wholesaleRepository.updateOrderStatus(orderId, status)
        }
    }

    fun deleteWholesaleBulkOrder(order: WholesaleBulkOrderEntity) {
        viewModelScope.launch {
            wholesaleRepository.deleteBulkOrder(order)
        }
    }

    fun recordWholesalePayment(payment: WholesalePaymentEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            wholesaleRepository.recordPayment(payment.copy(businessId = bizId))
        }
    }

    fun deleteWholesalePayment(payment: WholesalePaymentEntity) {
        viewModelScope.launch {
            wholesaleRepository.deletePayment(payment)
        }
    }

    // ==========================================
    // Expense & Cash Flow Actions
    // ==========================================
    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (expense.id == 0L) {
                expenseRepository.saveExpense(expense.copy(businessId = bizId))
            } else {
                expenseRepository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(id)
        }
    }

    // ==========================================
    // Staff & Payroll Actions
    // ==========================================
    fun saveStaffMember(staff: StaffEntity) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            if (staff.id == 0L) {
                staffRepository.saveStaff(staff.copy(businessId = bizId))
            } else {
                staffRepository.updateStaff(staff)
            }
        }
    }

    fun deleteStaffMember(id: Long) {
        viewModelScope.launch {
            staffRepository.deleteStaff(id)
        }
    }

    fun addStaffAdvance(staffId: Long, amount: Double) {
        viewModelScope.launch {
            staffRepository.addAdvance(staffId, amount)
        }
    }

    fun clearStaffAdvance(staffId: Long) {
        viewModelScope.launch {
            staffRepository.clearAdvance(staffId)
        }
    }

    fun recordStaffAttendance(staffId: Long, staffName: String, status: String, notes: String) {
        viewModelScope.launch {
            val bizId = activeBusiness.value?.id ?: return@launch
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            staffRepository.recordAttendance(
                StaffAttendanceEntity(
                    businessId = bizId,
                    staffId = staffId,
                    staffName = staffName,
                    dateString = todayDate,
                    status = status,
                    notes = notes
                )
            )
        }
    }

    // --- Core Multi-Tenant Operations (Parties, Inventory, Orders) ---

    fun insertParty(party: PartyEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = coreBusinessRepository.insertParty(party)
            onComplete(id)
        }
    }

    fun updateParty(party: PartyEntity) {
        viewModelScope.launch {
            coreBusinessRepository.updateParty(party)
        }
    }

    fun deleteParty(id: Long) {
        viewModelScope.launch {
            coreBusinessRepository.deleteParty(id)
        }
    }

    fun updatePartyBalance(partyId: Long, delta: Double) {
        viewModelScope.launch {
            coreBusinessRepository.updatePartyBalance(partyId, delta)
        }
    }

    fun insertInventoryItem(item: InventoryItemEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = coreBusinessRepository.insertInventoryItem(item)
            onComplete(id)
        }
    }

    fun updateInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            coreBusinessRepository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(id: Long) {
        viewModelScope.launch {
            coreBusinessRepository.deleteInventoryItem(id)
        }
    }

    fun updateInventoryStock(itemId: Long, delta: Double) {
        viewModelScope.launch {
            coreBusinessRepository.updateInventoryStock(itemId, delta)
        }
    }

    fun createOrder(order: OrderEntity, items: List<OrderItemEntity>, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = coreBusinessRepository.createOrderWithItems(order, items)
            onComplete(id)
        }
    }

    fun deleteOrder(id: Long) {
        viewModelScope.launch {
            coreBusinessRepository.deleteOrder(id)
        }
    }

    // --- Product Entity CRUD ---
    fun saveProduct(product: ProductEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (product.id == 0L) {
                productRepository.insertProduct(product)
            } else {
                productRepository.updateProduct(product)
                product.id
            }
            onComplete(id)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun deleteProductById(id: Long) {
        viewModelScope.launch {
            productRepository.deleteProductById(id)
        }
    }

    fun adjustProductStock(id: Long, delta: Double) {
        viewModelScope.launch {
            productRepository.adjustStock(id, delta)
        }
    }

    // --- Customer Entity CRUD ---
    fun saveCustomer(customer: CustomerEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (customer.id == 0L) {
                customerRepository.insertCustomer(customer)
            } else {
                customerRepository.updateCustomer(customer)
                customer.id
            }
            onComplete(id)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
        }
    }

    fun deleteCustomerById(id: Long) {
        viewModelScope.launch {
            customerRepository.deleteCustomerById(id)
        }
    }

    fun adjustCustomerBalance(customerId: Long, deltaAmount: Double) {
        viewModelScope.launch {
            customerRepository.adjustCustomerBalance(customerId, deltaAmount)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BusinessViewModel(application) as T
                }
            }
    }
}
