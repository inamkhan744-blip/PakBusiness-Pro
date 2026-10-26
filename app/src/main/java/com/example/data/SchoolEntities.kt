package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val rollNo: String = "101",
    val name: String,
    val fatherName: String = "",
    val className: String = "Class 9",
    val section: String = "A",
    val phone: String = "",
    val monthlyFee: Double = 3000.0,
    val admissionFee: Double = 0.0,
    val address: String = "",
    val emergencyContact: String = "",
    val admissionDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val pendingDuePkr: Double = 0.0,
    val gender: String = "Male",
    val notes: String = ""
) {
    val rollNumber: String get() = rollNo
    val monthlyFeePkr: Double get() = monthlyFee
    val admissionFeePkr: Double get() = admissionFee
}

@Entity(tableName = "school_classes")
data class SchoolClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val className: String,
    val section: String = "A",
    val roomNumber: String = "",
    val classTeacher: String = "",
    val defaultMonthlyFee: Double = 3000.0,
    val maxCapacity: Int = 40
) {
    val name: String get() = className
}

@Entity(tableName = "school_fee_vouchers")
data class FeeVoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val voucherNumber: String = "",
    val studentId: Long = 0,
    val studentName: String = "",
    val rollNo: String = "",
    val className: String = "",
    val section: String = "",
    val fatherName: String = "",
    val monthYear: String = "",
    val tuitionFee: Double = 0.0,
    val examFee: Double = 0.0,
    val labOrGenCharges: Double = 0.0,
    val discount: Double = 0.0,
    val fine: Double = 0.0,
    val totalAmount: Double = 0.0,
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val paymentMethod: String = "Cash"
) {
    fun isOverdue(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return !isPaid && currentTimeMillis > dueDate
    }

    val isOverdue: Boolean
        get() = isOverdue(System.currentTimeMillis())

    val formattedDueDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(dueDate))
        }
}

@Entity(tableName = "school_attendance")
data class StudentAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val studentId: Long = 0,
    val studentName: String = "",
    val rollNo: String = "",
    val className: String = "",
    val section: String = "",
    val date: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val status: String = "PRESENT" // PRESENT, ABSENT, LEAVE, LATE
) {
    val rollNumber: String get() = rollNo
}

@Entity(tableName = "school_fees")
data class StudentFeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val studentId: Long = 0,
    val studentName: String = "",
    val rollNumber: String = "",
    val className: String = "",
    val monthYear: String = "",
    val tuitionFeePkr: Double = 0.0,
    val examFeePkr: Double = 0.0,
    val finePkr: Double = 0.0,
    val totalFeePkr: Double = 0.0,
    val paidAmountPkr: Double = 0.0,
    val dueAmountPkr: Double = 0.0,
    val status: String = "PAID",
    val paymentMethod: String = "Cash",
    val date: Long = System.currentTimeMillis()
) {
    val chalanNumber: String get() = "CH-$id"
}

@Entity(tableName = "school_staff")
data class SchoolStaffEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val name: String = "",
    val designation: String = "Teacher",
    val subject: String = "Mathematics",
    val phone: String = "",
    val monthlySalaryPkr: Double = 25000.0,
    val qualification: String = "B.Sc / M.Sc"
)

@Entity(tableName = "school_exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long = 0,
    val studentId: Long = 0,
    val studentName: String = "",
    val rollNumber: String = "",
    val className: String = "",
    val examName: String = "",
    val subject: String = "",
    val totalMarks: Double = 100.0,
    val obtainedMarks: Double = 0.0,
    val grade: String = "A"
)
