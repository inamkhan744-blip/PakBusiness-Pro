package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BusinessViewModel
import com.example.ui.Screen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.BusinessSetupScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.PakBusinessTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode = viewModel.isDarkMode.collectAsStateWithLifecycle().value
            val currentScreen = viewModel.currentScreen.collectAsStateWithLifecycle().value
            val isAppLocked = viewModel.isAppLocked.collectAsStateWithLifecycle().value
            val securityUsername = viewModel.securityUsername.collectAsStateWithLifecycle().value
            val appLanguage = viewModel.appLanguage.collectAsStateWithLifecycle().value
            val activeBusiness = viewModel.activeBusiness.collectAsStateWithLifecycle().value
            val allBusinesses = viewModel.businesses.collectAsStateWithLifecycle().value
            val currentTab = viewModel.currentTab.collectAsStateWithLifecycle().value

            // Domain data streams from BusinessViewModel
            val gymMembers = viewModel.gymMembers.collectAsStateWithLifecycle().value
            val todayGymCheckIns = viewModel.todayGymCheckIns.collectAsStateWithLifecycle().value
            val selectedMember = viewModel.selectedMember.collectAsStateWithLifecycle().value
            val isAddEditOpen = viewModel.isAddEditMemberOpen.collectAsStateWithLifecycle().value
            val memberToEdit = viewModel.memberToEdit.collectAsStateWithLifecycle().value

            val restaurantTables = viewModel.restaurantTables.collectAsStateWithLifecycle().value
            val restaurantMenuItems = viewModel.restaurantMenuItems.collectAsStateWithLifecycle().value
            val activeKotItems = viewModel.activeKotItems.collectAsStateWithLifecycle().value
            val selectedRestaurantTable = viewModel.selectedRestaurantTable.collectAsStateWithLifecycle().value
            val currentTableOrder = viewModel.currentTableOrder.collectAsStateWithLifecycle().value
            val currentTableOrderItems = viewModel.currentTableOrderItems.collectAsStateWithLifecycle().value

            val hospitalPatients = viewModel.hospitalPatients.collectAsStateWithLifecycle().value
            val hospitalDoctors = viewModel.hospitalDoctors.collectAsStateWithLifecycle().value
            val hospitalAppointments = viewModel.hospitalAppointments.collectAsStateWithLifecycle().value
            val hospitalPrescriptions = viewModel.hospitalPrescriptions.collectAsStateWithLifecycle().value

            val pharmacyMedicines = viewModel.pharmacyMedicines.collectAsStateWithLifecycle().value
            val pharmacySales = viewModel.pharmacySales.collectAsStateWithLifecycle().value

            val schoolStudents = viewModel.schoolStudents.collectAsStateWithLifecycle().value
            val schoolClasses = viewModel.schoolClasses.collectAsStateWithLifecycle().value
            val schoolFeeVouchers = viewModel.schoolFeeVouchers.collectAsStateWithLifecycle().value
            val schoolAttendanceRecords = viewModel.schoolAttendanceRecords.collectAsStateWithLifecycle().value
            val selectedAttendanceDate = viewModel.selectedAttendanceDate.collectAsStateWithLifecycle().value
            val schoolStaff = viewModel.schoolStaff.collectAsStateWithLifecycle().value
            val schoolExamResults = viewModel.schoolExamResults.collectAsStateWithLifecycle().value

            PakBusinessTheme(darkTheme = isDarkMode) {
                if (isAppLocked) {
                    AppLockScreen(
                        savedUsername = securityUsername,
                        onUnlock = { password -> viewModel.unlockApp(password) },
                        onUnlockWithCredentials = { userOrCode, passOrPin ->
                            viewModel.unlockAppWithCredentials(userOrCode, passOrPin)
                        },
                        onBypassSecurity = { viewModel.bypassSecurity() },
                        appLanguage = appLanguage,
                        businessName = activeBusiness?.name ?: "PakBusiness Pro"
                    )
                } else {
                    when (currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen()
                        }
                        Screen.BUSINESS_SETUP -> {
                            BusinessSetupScreen(
                                onSaveBusiness = { name, type, owner, phone, address, currency, logoUri, tagline ->
                                    viewModel.createBusiness(
                                        name = name,
                                        type = type,
                                        ownerName = owner,
                                        phone = phone,
                                        address = address,
                                        currency = currency,
                                        logoUri = logoUri,
                                        tagline = tagline,
                                        onSuccess = { viewModel.navigateTo(Screen.MAIN_APP) }
                                    )
                                },
                                canCancel = allBusinesses.isNotEmpty(),
                                onCancel = {
                                    if (allBusinesses.isNotEmpty()) {
                                        viewModel.navigateTo(Screen.MAIN_APP)
                                    }
                                },
                                appLanguage = appLanguage,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() }
                            )
                        }
                        Screen.MAIN_APP -> {
                            MainAppScreen(
                                activeBusiness = activeBusiness,
                                allBusinesses = allBusinesses,
                                currentTab = currentTab,
                                gymMembers = gymMembers,
                                todayGymCheckIns = todayGymCheckIns,
                                selectedMember = selectedMember,
                                isAddEditOpen = isAddEditOpen,
                                memberToEdit = memberToEdit,
                                restaurantTables = restaurantTables,
                                restaurantMenuItems = restaurantMenuItems,
                                activeKotItems = activeKotItems,
                                selectedRestaurantTable = selectedRestaurantTable,
                                currentTableOrder = currentTableOrder,
                                currentTableOrderItems = currentTableOrderItems,
                                hospitalPatients = hospitalPatients,
                                hospitalDoctors = hospitalDoctors,
                                hospitalAppointments = hospitalAppointments,
                                hospitalPrescriptions = hospitalPrescriptions,
                                pharmacyMedicines = pharmacyMedicines,
                                pharmacySales = pharmacySales,
                                schoolStudents = schoolStudents,
                                schoolClasses = schoolClasses,
                                schoolFeeVouchers = schoolFeeVouchers,
                                schoolAttendanceRecords = schoolAttendanceRecords,
                                selectedAttendanceDate = selectedAttendanceDate,
                                schoolStaff = schoolStaff,
                                schoolExamResults = schoolExamResults,
                                onSelectMember = { memberId -> viewModel.selectMember(memberId) },
                                onOpenAddMember = { viewModel.openAddMember() },
                                onOpenEditMember = { member -> viewModel.openEditMember(member) },
                                onCloseAddEdit = { viewModel.closeAddEditMember() },
                                onSaveMember = { name, phone, gender, plan, startDate, durationDays, amount ->
                                    viewModel.saveGymMember(name, phone, gender, plan, startDate, durationDays, amount)
                                },
                                onDeleteMember = { memberId -> viewModel.deleteGymMember(memberId) },
                                onToggleCheckIn = { member -> viewModel.toggleGymCheckIn(member) },
                                getMemberCheckIns = { memberId -> viewModel.getMemberCheckIns(memberId) },
                                onSelectRestaurantTable = { table -> viewModel.selectRestaurantTable(table?.id) },
                                onSaveRestaurantTable = { name, cap, sec, id -> viewModel.saveRestaurantTable(name, cap, sec, id) },
                                onDeleteRestaurantTable = { id -> viewModel.deleteRestaurantTable(id) },
                                onReserveRestaurantTable = { id, name, phone -> viewModel.reserveRestaurantTable(id, name, phone) },
                                onClearRestaurantTableReservation = { id -> viewModel.clearRestaurantTableReservation(id) },
                                onSaveRestaurantMenuItem = { name, cat, price, desc, prep, avail, id ->
                                    viewModel.saveRestaurantMenuItem(name, cat, price, desc, prep, avail, id)
                                },
                                onDeleteRestaurantMenuItem = { id -> viewModel.deleteRestaurantMenuItem(id) },
                                onToggleMenuAvailability = { id, avail -> viewModel.toggleMenuAvailability(id, avail) },
                                onSaveAndSendOrderToKitchen = { order, items -> viewModel.sendOrderToKitchen(order, items) },
                                onSettleRestaurantOrder = { orderId, tableId, method -> viewModel.settleOrder(orderId, tableId, method) },
                                onCancelRestaurantOrder = { orderId, tableId -> viewModel.cancelOrder(orderId, tableId) },
                                onUpdateKotItemStatus = { itemId, status -> viewModel.updateKotItemStatus(itemId, status) },
                                onSavePatient = { name, phone, age, gender, bg, addr, hist, id ->
                                    viewModel.savePatient(name, phone, age, gender, bg, addr, hist, id)
                                },
                                onDeletePatient = { id -> viewModel.deletePatient(id) },
                                onSaveDoctor = { name, spec, qual, fee, days, hours, phone, avail, id ->
                                    viewModel.saveDoctor(name, spec, qual, fee, days, hours, phone, avail, id)
                                },
                                onDeleteDoctor = { id -> viewModel.deleteDoctor(id) },
                                onToggleDoctorAvailability = { id, avail -> viewModel.toggleDoctorAvailability(id, avail) },
                                onBookAppointment = { pId, pName, pPhone, dId, dName, spec, date, slot, fee, symp ->
                                    viewModel.bookAppointment(pId, pName, pPhone, dId, dName, spec, date, slot, fee, symp)
                                },
                                onUpdateAppointmentStatus = { id, status -> viewModel.updateAppointmentStatus(id, status) },
                                onDeleteAppointment = { id -> viewModel.deleteAppointment(id) },
                                onSavePrescription = { rx, onDone -> viewModel.savePrescription(rx, onDone) },
                                onDeletePrescription = { id -> viewModel.deletePrescription(id) },
                                onSaveMedicine = { med -> viewModel.saveMedicine(med) },
                                onDeleteMedicine = { id -> viewModel.deleteMedicine(id) },
                                onUpdateMedicineStock = { id, delta -> viewModel.updateMedicineStock(id, delta) },
                                onCompletePharmacySale = { items, cName, cPhone, doc, disc, pay, notes, onDone ->
                                    viewModel.completePharmacySale(items, cName, cPhone, doc, disc, pay, notes, onDone)
                                },
                                onDeletePharmacySale = { id -> viewModel.deletePharmacySale(id) },
                                onSaveStudent = { student -> viewModel.saveStudent(student) },
                                onDeleteStudent = { id -> viewModel.deleteStudent(id) },
                                onSaveSchoolClass = { scClass -> viewModel.saveSchoolClass(scClass) },
                                onDeleteSchoolClass = { id -> viewModel.deleteSchoolClass(id) },
                                onGenerateBulkVouchers = { my, due, exam, lab, onDone ->
                                    viewModel.generateBulkVouchers(my, due, exam, lab, onDone)
                                },
                                onSaveFeeVoucher = { vch -> viewModel.saveFeeVoucher(vch) },
                                onMarkFeeVoucherPaid = { id, method -> viewModel.markFeeVoucherPaid(id, method) },
                                onDeleteFeeVoucher = { id -> viewModel.deleteFeeVoucher(id) },
                                onAttendanceDateChange = { dateStr -> viewModel.setAttendanceDate(dateStr) },
                                onRecordAttendance = { att -> viewModel.recordAttendance(att) },
                                onMarkAllAttendancePresent = { list, dateStr, millis ->
                                    viewModel.markAllAttendancePresent(list, dateStr, millis)
                                },
                                onSaveSchoolStaff = { staff -> viewModel.saveSchoolStaff(staff) },
                                onDeleteSchoolStaff = { id -> viewModel.deleteSchoolStaff(id) },
                                onSaveSchoolExamResult = { res -> viewModel.saveSchoolExamResult(res) },
                                onDeleteSchoolExamResult = { id -> viewModel.deleteSchoolExamResult(id) },
                                onTabSelected = { tab -> viewModel.selectTab(tab) },
                                onSwitchBusiness = { id -> viewModel.switchBusiness(id) },
                                onDeleteBusiness = { id -> viewModel.deleteBusiness(id) },
                                onAddNewBusiness = { viewModel.navigateTo(Screen.BUSINESS_SETUP) },
                                onUpdateBusiness = { biz -> viewModel.updateBusiness(biz) },
                                viewModel = viewModel,
                                appLanguage = appLanguage,
                                onSelectLanguage = { lang -> viewModel.setAppLanguage(lang) },
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
