package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExamResultEntity
import com.example.data.SchoolDao
import com.example.data.SchoolStaffEntity
import com.example.data.StudentAttendanceEntity
import com.example.data.StudentEntity
import com.example.data.StudentFeeEntity
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

data class SchoolKpis(
    val totalStudents: Int = 0,
    val todayAttendancePercent: Double = 0.0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val monthlyFeesCollectedPkr: Double = 0.0,
    val totalPendingDuesPkr: Double = 0.0,
    val totalStaffCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolManagementViewModel(
    application: Application,
    private val schoolDao: SchoolDao = AppDatabase.getDatabase(application).schoolDao()
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedClass = MutableStateFlow("ALL")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val currentMonthYear: String
        get() = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    // Raw Streams
    val rawStudents: StateFlow<List<StudentEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) schoolDao.getStudents(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<List<StudentAttendanceEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) schoolDao.getAttendanceByDate(biz.id, todayDateString) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feesList: StateFlow<List<StudentFeeEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) schoolDao.getFees(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffList: StateFlow<List<SchoolStaffEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) schoolDao.getStaff(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students
    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        rawStudents,
        searchQuery,
        selectedClass
    ) { list, query, filterClass ->
        list.filter { s ->
            val matchQuery = query.isBlank() ||
                s.name.contains(query, ignoreCase = true) ||
                s.rollNumber.contains(query, ignoreCase = true) ||
                s.fatherName.contains(query, ignoreCase = true) ||
                s.phone.contains(query, ignoreCase = true)

            val matchClass = filterClass == "ALL" || s.className.equals(filterClass, ignoreCase = true)
            matchQuery && matchClass
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // School KPIs
    val kpis: StateFlow<SchoolKpis> = combine(
        rawStudents,
        todayAttendance,
        feesList,
        staffList
    ) { students, attendance, fees, staff ->
        val total = students.size
        val present = attendance.count { it.status == "PRESENT" }
        val absent = attendance.count { it.status == "ABSENT" }
        val attendancePct = if (total > 0 && attendance.isNotEmpty()) (present.toDouble() / total.toDouble()) * 100.0 else 0.0

        SchoolKpis(
            totalStudents = total,
            todayAttendancePercent = attendancePct,
            presentCount = present,
            absentCount = absent,
            monthlyFeesCollectedPkr = fees.filter { it.monthYear.equals(currentMonthYear, ignoreCase = true) }.sumOf { it.paidAmountPkr },
            totalPendingDuesPkr = students.sumOf { it.pendingDuePkr },
            totalStaffCount = staff.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolKpis())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectClass(className: String) {
        _selectedClass.value = className
    }

    fun selectStudent(student: StudentEntity?) {
        _selectedStudent.value = student
    }

    // Enroll or update student
    fun saveStudent(
        id: Long = 0L,
        rollNumber: String,
        name: String,
        fatherName: String,
        className: String,
        section: String = "A",
        phone: String = "",
        address: String = "",
        monthlyFeePkr: Double = 2500.0,
        admissionFeePkr: Double = 3000.0,
        pendingDuePkr: Double = 0.0,
        onComplete: (Long) -> Unit = {}
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val student = StudentEntity(
                id = id,
                businessId = bizId,
                rollNo = rollNumber.trim(),
                name = name.trim(),
                fatherName = fatherName.trim(),
                className = className.trim(),
                section = section.trim(),
                phone = phone.trim(),
                address = address.trim(),
                monthlyFee = monthlyFeePkr,
                admissionFee = admissionFeePkr,
                pendingDuePkr = pendingDuePkr
            )
            val generatedId = if (id == 0L) schoolDao.insertStudent(student) else { schoolDao.updateStudent(student); id }
            onComplete(generatedId)
        }
    }

    // Collect Student Fee & Generate Chalan
    fun collectFee(
        studentId: Long,
        studentName: String,
        rollNumber: String,
        className: String,
        monthYear: String = currentMonthYear,
        tuitionFee: Double,
        examFee: Double = 0.0,
        fine: Double = 0.0,
        paidAmount: Double,
        paymentMethod: String = "Cash"
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val total = tuitionFee + examFee + fine
            val due = (total - paidAmount).coerceAtLeast(0.0)
            val status = when {
                due == 0.0 -> "PAID"
                paidAmount > 0.0 -> "PARTIAL"
                else -> "UNPAID"
            }
            val fee = StudentFeeEntity(
                businessId = bizId,
                studentId = studentId,
                studentName = studentName,
                rollNumber = rollNumber,
                className = className,
                monthYear = monthYear,
                tuitionFeePkr = tuitionFee,
                examFeePkr = examFee,
                finePkr = fine,
                totalFeePkr = total,
                paidAmountPkr = paidAmount,
                dueAmountPkr = due,
                status = status,
                paymentMethod = paymentMethod
            )
            schoolDao.insertFee(fee)

            // Update student's pending due
            val student = schoolDao.getStudentByIdDirect(studentId)
            if (student != null) {
                val newDue = (student.pendingDuePkr + due - paidAmount).coerceAtLeast(0.0)
                schoolDao.updateStudentDue(studentId, newDue)
            }
        }
    }

    // Mark Attendance (Single student)
    fun markAttendance(student: StudentEntity, status: String) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val attendance = StudentAttendanceEntity(
                businessId = bizId,
                studentId = student.id,
                studentName = student.name,
                rollNo = student.rollNumber,
                className = student.className,
                dateString = todayDateString,
                status = status
            )
            schoolDao.insertAttendance(attendance)
        }
    }

    // Mark entire class present with 1 tap
    fun markAllPresentForClass(className: String) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val classStudents = rawStudents.value.filter { it.className.equals(className, ignoreCase = true) }
            val list = classStudents.map { s ->
                StudentAttendanceEntity(
                    businessId = bizId,
                    studentId = s.id,
                    studentName = s.name,
                    rollNo = s.rollNumber,
                    className = s.className,
                    dateString = todayDateString,
                    status = "PRESENT"
                )
            }
            schoolDao.insertAttendanceList(list)
        }
    }

    // Add Staff / Teacher
    fun saveStaff(
        name: String,
        designation: String = "Teacher",
        subject: String = "Mathematics",
        phone: String = "",
        salary: Double = 25000.0,
        qualification: String = "B.Sc / M.Sc"
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            schoolDao.insertStaff(
                SchoolStaffEntity(
                    businessId = bizId,
                    name = name.trim(),
                    designation = designation,
                    subject = subject.trim(),
                    phone = phone.trim(),
                    monthlySalaryPkr = salary,
                    qualification = qualification
                )
            )
        }
    }

    // Record Exam Result
    fun saveExamResult(
        student: StudentEntity,
        examName: String,
        subject: String,
        totalMarks: Double,
        obtainedMarks: Double
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            val pct = if (totalMarks > 0) (obtainedMarks / totalMarks) * 100.0 else 0.0
            val grade = when {
                pct >= 85 -> "A+"
                pct >= 75 -> "A"
                pct >= 65 -> "B"
                pct >= 50 -> "C"
                pct >= 40 -> "D"
                else -> "F"
            }
            schoolDao.insertExamResult(
                ExamResultEntity(
                    businessId = bizId,
                    studentId = student.id,
                    studentName = student.name,
                    rollNumber = student.rollNumber,
                    className = student.className,
                    examName = examName,
                    subject = subject,
                    totalMarks = totalMarks,
                    obtainedMarks = obtainedMarks,
                    grade = grade
                )
            )
        }
    }

    fun deleteStudent(id: Long) {
        launchWithLoading {
            schoolDao.deleteStudent(id)
            if (_selectedStudent.value?.id == id) _selectedStudent.value = null
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SchoolManagementViewModel(application) as T
                }
            }
    }
}
