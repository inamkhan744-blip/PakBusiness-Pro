package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolRepository(private val schoolDao: SchoolDao) {

    // --- Students ---
    fun getStudents(businessId: Long): Flow<List<StudentEntity>> =
        schoolDao.getStudents(businessId)

    fun getStudentsByClass(businessId: Long, className: String, section: String): Flow<List<StudentEntity>> =
        schoolDao.getStudentsByClass(businessId, className, section)

    fun getStudentById(id: Long): Flow<StudentEntity?> =
        schoolDao.getStudentById(id)

    suspend fun saveStudent(student: StudentEntity): Long {
        return if (student.id == 0L) {
            schoolDao.insertStudent(student)
        } else {
            schoolDao.updateStudent(student)
            student.id
        }
    }

    suspend fun deleteStudent(id: Long) =
        schoolDao.deleteStudentById(id)

    // --- Classes & Sections ---
    fun getClasses(businessId: Long): Flow<List<SchoolClassEntity>> =
        schoolDao.getClasses(businessId)

    suspend fun saveClass(schoolClass: SchoolClassEntity): Long {
        return if (schoolClass.id == 0L) {
            schoolDao.insertClass(schoolClass)
        } else {
            schoolDao.updateClass(schoolClass)
            schoolClass.id
        }
    }

    suspend fun deleteClass(id: Long) =
        schoolDao.deleteClassById(id)

    // --- Fee Vouchers ---
    fun getFeeVouchers(businessId: Long): Flow<List<FeeVoucherEntity>> =
        schoolDao.getFeeVouchers(businessId)

    fun getVouchersByStudent(studentId: Long): Flow<List<FeeVoucherEntity>> =
        schoolDao.getVouchersByStudent(studentId)

    suspend fun saveVoucher(voucher: FeeVoucherEntity): Long {
        return if (voucher.id == 0L) {
            schoolDao.insertVoucher(voucher)
        } else {
            schoolDao.updateVoucher(voucher)
            voucher.id
        }
    }

    suspend fun generateMonthlyVouchers(
        businessId: Long,
        students: List<StudentEntity>,
        monthYear: String,
        dueDate: Long,
        examFee: Double = 0.0,
        labFee: Double = 0.0
    ): Int {
        val now = System.currentTimeMillis()
        val vouchers = students.filter { it.isActive }.mapIndexed { index, student ->
            val total = student.monthlyFee + examFee + labFee
            val voucherNum = "VCH-${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(now))}-${student.rollNo.replace(" ", "")}-${index + 1}"
            FeeVoucherEntity(
                businessId = businessId,
                voucherNumber = voucherNum,
                studentId = student.id,
                studentName = student.name,
                rollNo = student.rollNo,
                className = student.className,
                section = student.section,
                fatherName = student.fatherName,
                monthYear = monthYear,
                tuitionFee = student.monthlyFee,
                examFee = examFee,
                labOrGenCharges = labFee,
                discount = 0.0,
                fine = 0.0,
                totalAmount = total,
                issueDate = now,
                dueDate = dueDate,
                isPaid = false
            )
        }
        schoolDao.insertVouchers(vouchers)
        return vouchers.size
    }

    suspend fun markVoucherPaid(id: Long, paidDate: Long = System.currentTimeMillis(), paymentMethod: String = "Cash") {
        schoolDao.markVoucherPaid(id, paidDate, paymentMethod)
    }

    suspend fun deleteVoucher(id: Long) =
        schoolDao.deleteVoucherById(id)

    // --- Attendance ---
    fun getAttendanceByDate(businessId: Long, dateString: String): Flow<List<StudentAttendanceEntity>> =
        schoolDao.getAttendanceByDate(businessId, dateString)

    fun getAttendanceByStudent(studentId: Long): Flow<List<StudentAttendanceEntity>> =
        schoolDao.getAttendanceByStudent(studentId)

    suspend fun recordAttendance(attendance: StudentAttendanceEntity): Long =
        schoolDao.insertOrUpdateAttendance(attendance)

    suspend fun markClassAttendance(
        businessId: Long,
        students: List<StudentEntity>,
        dateString: String,
        dateMillis: Long,
        defaultStatus: String = "PRESENT"
    ) {
        val records = students.map { student ->
            StudentAttendanceEntity(
                businessId = businessId,
                studentId = student.id,
                studentName = student.name,
                rollNo = student.rollNo,
                className = student.className,
                section = student.section,
                date = dateMillis,
                dateString = dateString,
                status = defaultStatus
            )
        }
        schoolDao.insertAttendanceList(records)
    }

    // --- Staff & Teachers ---
    fun getStaff(businessId: Long): Flow<List<SchoolStaffEntity>> =
        schoolDao.getStaff(businessId)

    suspend fun saveStaff(staff: SchoolStaffEntity): Long =
        schoolDao.insertStaff(staff)

    suspend fun deleteStaff(id: Long) =
        schoolDao.deleteStaff(id)

    // --- Exam Results ---
    fun getExamResults(businessId: Long): Flow<List<ExamResultEntity>> =
        schoolDao.getExamResults(businessId)

    suspend fun saveExamResult(result: ExamResultEntity): Long =
        schoolDao.insertExamResult(result)

    suspend fun deleteExamResult(id: Long) =
        schoolDao.deleteExamResult(id)

    suspend fun seedSampleSchoolDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake school entries
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val count = schoolDao.getStudentsCount(businessId)
            if (count > 0) return@withContext

            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000
            val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(now))
            val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

            // 1. Classes
            val sampleClasses = listOf(
                SchoolClassEntity(
                    businessId = businessId,
                    className = "Class 9",
                    section = "A",
                    roomNumber = "Room 101",
                    classTeacher = "Sir Asim Qureshi",
                    defaultMonthlyFee = 3500.0,
                    maxCapacity = 35
                ),
                SchoolClassEntity(
                    businessId = businessId,
                    className = "Class 9",
                    section = "B",
                    roomNumber = "Room 102",
                    classTeacher = "Sir Naeem Akhter",
                    defaultMonthlyFee = 3200.0,
                    maxCapacity = 35
                ),
                SchoolClassEntity(
                    businessId = businessId,
                    className = "Class 10",
                    section = "A",
                    roomNumber = "Room 201",
                    classTeacher = "Madam Farzana",
                    defaultMonthlyFee = 4000.0,
                    maxCapacity = 40
                ),
                SchoolClassEntity(
                    businessId = businessId,
                    className = "1st Year (FSc)",
                    section = "Pre-Medical",
                    roomNumber = "Hall 1",
                    classTeacher = "Prof. Tariq Mahmood",
                    defaultMonthlyFee = 5500.0,
                    maxCapacity = 50
                ),
                SchoolClassEntity(
                    businessId = businessId,
                    className = "2nd Year (ICS)",
                    section = "Computer Science",
                    roomNumber = "Lab 2",
                    classTeacher = "Sir Hammad Ali",
                    defaultMonthlyFee = 5500.0,
                    maxCapacity = 45
                ),
                SchoolClassEntity(
                    businessId = businessId,
                    className = "Academy Evening Batch",
                    section = "MDCAT Prep",
                    roomNumber = "Auditorium",
                    classTeacher = "Dr. Bilal & Team",
                    defaultMonthlyFee = 8000.0,
                    maxCapacity = 60
                )
            )
            for (c in sampleClasses) {
                schoolDao.insertClass(c)
            }

            // 2. Students
            val sampleStudents = listOf(
                StudentEntity(
                    businessId = businessId,
                    rollNo = "101",
                    name = "Muhammad Ali",
                    fatherName = "Tariq Mehmood",
                    className = "Class 9",
                    section = "A",
                    monthlyFee = 3500.0,
                    phone = "0300-4521890",
                    gender = "Male",
                    admissionDate = now - (180 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "102",
                    name = "Fatima Zahra",
                    fatherName = "Rashid Minhas",
                    className = "Class 9",
                    section = "A",
                    monthlyFee = 3500.0,
                    phone = "0312-9876543",
                    gender = "Female",
                    admissionDate = now - (175 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "103",
                    name = "Hamza Khan",
                    fatherName = "Sheraz Khan",
                    className = "Class 9",
                    section = "B",
                    monthlyFee = 3200.0,
                    phone = "0321-5551234",
                    gender = "Male",
                    admissionDate = now - (150 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "201",
                    name = "Zainab Bibi",
                    fatherName = "Ghulam Nabi",
                    className = "Class 10",
                    section = "A",
                    monthlyFee = 4000.0,
                    phone = "0333-8812345",
                    gender = "Female",
                    admissionDate = now - (360 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "202",
                    name = "Bilal Ahmed",
                    fatherName = "Mushtaq Ahmed",
                    className = "Class 10",
                    section = "A",
                    monthlyFee = 4000.0,
                    phone = "0345-1239876",
                    gender = "Male",
                    admissionDate = now - (350 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "301",
                    name = "Ayesha Siddiqui",
                    fatherName = "Dr. Khalid Siddiqui",
                    className = "1st Year (FSc)",
                    section = "Pre-Medical",
                    monthlyFee = 5500.0,
                    phone = "0301-7788990",
                    gender = "Female",
                    admissionDate = now - (60 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "302",
                    name = "Usman Afzal",
                    fatherName = "Afzal Hussain",
                    className = "2nd Year (ICS)",
                    section = "Computer Science",
                    monthlyFee = 5500.0,
                    phone = "0315-4433221",
                    gender = "Male",
                    admissionDate = now - (400 * dayMillis)
                ),
                StudentEntity(
                    businessId = businessId,
                    rollNo = "A-12",
                    name = "Daniyal Shah",
                    fatherName = "Syed Farooq Shah",
                    className = "Academy Evening Batch",
                    section = "MDCAT Prep",
                    monthlyFee = 8000.0,
                    phone = "0322-1199884",
                    gender = "Male",
                    admissionDate = now - (30 * dayMillis)
                )
            )

            val studentIds = mutableListOf<Long>()
            for (s in sampleStudents) {
                val sId = schoolDao.insertStudent(s)
                studentIds.add(sId)
            }

            // 3. Vouchers
            val sampleVouchers = listOf(
                FeeVoucherEntity(
                    businessId = businessId,
                    voucherNumber = "VCH-202609-101",
                    studentId = studentIds[0],
                    studentName = "Muhammad Ali",
                    rollNo = "101",
                    className = "Class 9",
                    section = "A",
                    fatherName = "Tariq Mehmood",
                    monthYear = currentMonthYear,
                    tuitionFee = 3500.0,
                    examFee = 300.0,
                    labOrGenCharges = 200.0,
                    totalAmount = 4000.0,
                    issueDate = now - (10 * dayMillis),
                    dueDate = now + (5 * dayMillis),
                    isPaid = true,
                    paidDate = now - (3 * dayMillis),
                    paymentMethod = "JazzCash"
                ),
                FeeVoucherEntity(
                    businessId = businessId,
                    voucherNumber = "VCH-202609-102",
                    studentId = studentIds[1],
                    studentName = "Fatima Zahra",
                    rollNo = "102",
                    className = "Class 9",
                    section = "A",
                    fatherName = "Rashid Minhas",
                    monthYear = currentMonthYear,
                    tuitionFee = 3500.0,
                    examFee = 300.0,
                    labOrGenCharges = 200.0,
                    totalAmount = 4000.0,
                    issueDate = now - (10 * dayMillis),
                    dueDate = now + (5 * dayMillis),
                    isPaid = true,
                    paidDate = now - (2 * dayMillis),
                    paymentMethod = "Cash"
                ),
                FeeVoucherEntity(
                    businessId = businessId,
                    voucherNumber = "VCH-202609-201",
                    studentId = studentIds[3],
                    studentName = "Zainab Bibi",
                    rollNo = "201",
                    className = "Class 10",
                    section = "A",
                    fatherName = "Ghulam Nabi",
                    monthYear = currentMonthYear,
                    tuitionFee = 4000.0,
                    examFee = 500.0,
                    labOrGenCharges = 250.0,
                    totalAmount = 4750.0,
                    issueDate = now - (10 * dayMillis),
                    dueDate = now + (5 * dayMillis),
                    isPaid = false
                ),
                FeeVoucherEntity(
                    businessId = businessId,
                    voucherNumber = "VCH-202609-202",
                    studentId = studentIds[4],
                    studentName = "Bilal Ahmed",
                    rollNo = "202",
                    className = "Class 10",
                    section = "A",
                    fatherName = "Mushtaq Ahmed",
                    monthYear = currentMonthYear,
                    tuitionFee = 4000.0,
                    examFee = 500.0,
                    labOrGenCharges = 250.0,
                    totalAmount = 4750.0,
                    issueDate = now - (15 * dayMillis),
                    dueDate = now - (2 * dayMillis), // overdue
                    isPaid = false
                ),
                FeeVoucherEntity(
                    businessId = businessId,
                    voucherNumber = "VCH-202609-301",
                    studentId = studentIds[5],
                    studentName = "Ayesha Siddiqui",
                    rollNo = "301",
                    className = "1st Year (FSc)",
                    section = "Pre-Medical",
                    fatherName = "Dr. Khalid Siddiqui",
                    monthYear = currentMonthYear,
                    tuitionFee = 5500.0,
                    examFee = 500.0,
                    labOrGenCharges = 500.0,
                    totalAmount = 6500.0,
                    issueDate = now - (10 * dayMillis),
                    dueDate = now + (5 * dayMillis),
                    isPaid = true,
                    paidDate = now - (1 * dayMillis),
                    paymentMethod = "EasyPaisa"
                )
            )
            for (v in sampleVouchers) {
                schoolDao.insertVoucher(v)
            }

            // 4. Sample Attendance for Today
            val statuses = listOf("PRESENT", "PRESENT", "PRESENT", "ABSENT", "LATE", "PRESENT", "LEAVE", "PRESENT")
            val sampleAttendance = sampleStudents.mapIndexed { index, student ->
                StudentAttendanceEntity(
                    businessId = businessId,
                    studentId = studentIds[index],
                    studentName = student.name,
                    rollNo = student.rollNo,
                    className = student.className,
                    section = student.section,
                    date = now,
                    dateString = todayDateStr,
                    status = statuses[index % statuses.size]
                )
            }
            schoolDao.insertAttendanceList(sampleAttendance)
        }
    }
}
