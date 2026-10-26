package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "staff_members")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val role: String, // Manager, Salesman, Cashier, Chef, Tailor Master, Mechanic, Cleaner, Trainer, Helper
    val phone: String,
    val monthlySalary: Double,
    val advancePaid: Double = 0.0, // Peshgi
    val joiningDate: String = "",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val staffCode: String = "", // e.g. STF-101 (Staff Login ID)
    val loginPin: String = "", // Staff 4-6 digit Login PIN
    val accessLevel: String = "STAFF", // OWNER, MANAGER, CASHIER, VIEW_ONLY, CUSTOM, STAFF
    val permissions: String = "" // Comma-separated granted permission codes (e.g. "POS,INVENTORY,KHATA")
)

enum class StaffRole(
    val code: String,
    val titleEn: String,
    val titleUrdu: String,
    val description: String,
    val defaultPermissions: List<String>
) {
    OWNER(
        code = "OWNER",
        titleEn = "Owner / Admin",
        titleUrdu = "مالک / ایڈمن",
        description = "مکمل اختیارات (تمام ماڈیولز، ترتیبات اور ڈیلیٹ)",
        defaultPermissions = listOf("POS", "INVENTORY", "KHATA", "EXPENSES", "REPORTS", "STAFF", "SETTINGS", "OPERATIONS", "DELETE")
    ),
    MANAGER(
        code = "MANAGER",
        titleEn = "Manager",
        titleUrdu = "منیجر",
        description = "سیلز، اسٹاک، کھاتہ، اخراجات، رپورٹس اور عملہ (بغیر سسٹم ترتیبات)",
        defaultPermissions = listOf("POS", "INVENTORY", "KHATA", "EXPENSES", "REPORTS", "STAFF", "OPERATIONS")
    ),
    CASHIER(
        code = "CASHIER",
        titleEn = "Cashier",
        titleUrdu = "کیشیئر",
        description = "سیلز، پی او ایس بلنگ، ادائیگیاں وصول کرنا، اسٹاک دیکھنا",
        defaultPermissions = listOf("POS", "KHATA", "INVENTORY", "OPERATIONS")
    ),
    VIEW_ONLY(
        code = "VIEW_ONLY",
        titleEn = "View-Only",
        titleUrdu = "صرف دیکھنے کی اجازت",
        description = "اشیاء، قیمتیں اور کھاتہ صرف دیکھ سکتے ہیں، کوئی تبدیلی یا اخراجات نہیں",
        defaultPermissions = listOf("INVENTORY", "KHATA", "REPORTS")
    ),
    CUSTOM(
        code = "CUSTOM",
        titleEn = "Custom Role",
        titleUrdu = "اپنی مرضی کے اختیارات",
        description = "مالک کی طرف سے منتخب کردہ مخصوص اختیارات",
        defaultPermissions = emptyList()
    )
}

enum class ModulePermission(
    val code: String,
    val titleEn: String,
    val titleUrdu: String,
    val description: String
) {
    POS("POS", "Sales & POS Billing", "فروخت اور پی او ایس بلنگ", "Create sales, invoices and cash receipts"),
    INVENTORY("INVENTORY", "Inventory & Stock", "اسٹاک اور اشیاء", "View, add or adjust inventory and prices"),
    KHATA("KHATA", "Customer & Khata Ledger", "کھاتہ اور پارٹیاں", "Manage customer & supplier credit ledgers"),
    EXPENSES("EXPENSES", "Daily Expenses", "روزانہ اخراجات", "Record petty cash and business expenses"),
    REPORTS("REPORTS", "Reports & Profit/Loss", "رپورٹس اور منافع", "View profit & loss and revenue analytics"),
    STAFF("STAFF", "Staff & Payroll", "عملہ اور تنخواہ", "Manage employee records, advances & attendance"),
    SETTINGS("SETTINGS", "Settings & Backup", "ترتیبات اور بیک اپ", "Business profile, backup and restore"),
    OPERATIONS("OPERATIONS", "Operations & Services", "کاروباری آپریشنز", "Appointments, tables, room bookings, tailoring"),
    DELETE("DELETE", "Delete Operations", "ریکارڈ ڈیلیٹ کرنے کا اختیار", "Authority to permanently delete records")
}

fun StaffEntity.hasModulePermission(permission: ModulePermission): Boolean {
    val level = accessLevel.trim().uppercase()
    if (level == "OWNER") return true

    // Check custom permissions string if specified
    if (permissions.isNotBlank()) {
        val grantedCodes = permissions.split(",").map { it.trim().uppercase() }
        if (permission.code in grantedCodes) return true
    }

    // Fallback to role presets
    val roleObj = StaffRole.entries.firstOrNull { it.code == level }
    return roleObj?.defaultPermissions?.contains(permission.code) ?: false
}

@Entity(tableName = "staff_attendance")
data class StaffAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val staffId: Long,
    val staffName: String,
    val dateString: String, // YYYY-MM-DD
    val status: String = "PRESENT", // PRESENT, ABSENT, HALF_DAY, LEAVE
    val notes: String = ""
)

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members WHERE businessId = :businessId ORDER BY name ASC")
    fun getStaffForBusiness(businessId: Long): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members WHERE businessId = :businessId AND (staffCode = :codeOrPhone OR phone = :codeOrPhone) LIMIT 1")
    suspend fun findStaffByCodeOrPhone(businessId: Long, codeOrPhone: String): StaffEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: Long)

    @Query("UPDATE staff_members SET advancePaid = advancePaid + :amount WHERE id = :staffId")
    suspend fun addSalaryAdvance(staffId: Long, amount: Double)

    @Query("UPDATE staff_members SET advancePaid = 0 WHERE id = :staffId")
    suspend fun clearSalaryAdvance(staffId: Long)

    // Attendance
    @Query("SELECT * FROM staff_attendance WHERE businessId = :businessId AND dateString = :dateString")
    fun getAttendanceForDate(businessId: Long, dateString: String): Flow<List<StaffAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendance(attendance: StaffAttendanceEntity): Long

    @Query("DELETE FROM staff_attendance WHERE staffId = :staffId AND dateString = :dateString")
    suspend fun deleteAttendanceForDay(staffId: Long, dateString: String)
}

class StaffRepository(private val dao: StaffDao) {
    fun getStaff(businessId: Long): Flow<List<StaffEntity>> =
        dao.getStaffForBusiness(businessId)

    suspend fun findStaff(businessId: Long, codeOrPhone: String): StaffEntity? =
        dao.findStaffByCodeOrPhone(businessId, codeOrPhone)

    suspend fun saveStaff(staff: StaffEntity): Long =
        dao.insertStaff(staff)

    suspend fun updateStaff(staff: StaffEntity) =
        dao.updateStaff(staff)

    suspend fun deleteStaff(id: Long) =
        dao.deleteStaffById(id)

    suspend fun addAdvance(staffId: Long, amount: Double) =
        dao.addSalaryAdvance(staffId, amount)

    suspend fun clearAdvance(staffId: Long) =
        dao.clearSalaryAdvance(staffId)

    fun getAttendance(businessId: Long, dateString: String): Flow<List<StaffAttendanceEntity>> =
        dao.getAttendanceForDate(businessId, dateString)

    suspend fun recordAttendance(attendance: StaffAttendanceEntity): Long =
        dao.recordAttendance(attendance)
}
