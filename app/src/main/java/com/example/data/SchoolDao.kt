package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Students ---
    @Query("SELECT * FROM school_students WHERE businessId = :businessId ORDER BY className ASC, rollNo ASC, name ASC")
    fun getStudents(businessId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM school_students WHERE businessId = :businessId AND className = :className AND section = :section ORDER BY rollNo ASC")
    fun getStudentsByClass(businessId: Long, className: String, section: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM school_students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM school_students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdDirect(id: Long): StudentEntity?

    @Query("SELECT COUNT(*) FROM school_students WHERE businessId = :businessId")
    suspend fun getStudentsCount(businessId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM school_students WHERE id = :id")
    suspend fun deleteStudent(id: Long)

    @Query("DELETE FROM school_students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("UPDATE school_students SET pendingDuePkr = :due WHERE id = :id")
    suspend fun updateStudentDue(id: Long, due: Double)

    // --- Classes & Sections ---
    @Query("SELECT * FROM school_classes WHERE businessId = :businessId ORDER BY className ASC, section ASC")
    fun getClasses(businessId: Long): Flow<List<SchoolClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClassEntity): Long

    @Update
    suspend fun updateClass(schoolClass: SchoolClassEntity)

    @Query("DELETE FROM school_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    // --- Fee Vouchers ---
    @Query("SELECT * FROM school_fee_vouchers WHERE businessId = :businessId ORDER BY issueDate DESC, id DESC")
    fun getFeeVouchers(businessId: Long): Flow<List<FeeVoucherEntity>>

    @Query("SELECT * FROM school_fee_vouchers WHERE studentId = :studentId ORDER BY issueDate DESC")
    fun getVouchersByStudent(studentId: Long): Flow<List<FeeVoucherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: FeeVoucherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<FeeVoucherEntity>)

    @Update
    suspend fun updateVoucher(voucher: FeeVoucherEntity)

    @Query("DELETE FROM school_fee_vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: Long)

    @Query("UPDATE school_fee_vouchers SET isPaid = 1, paidDate = :paidDate, paymentMethod = :paymentMethod WHERE id = :id")
    suspend fun markVoucherPaid(id: Long, paidDate: Long, paymentMethod: String)

    // --- Fee Records (Chalan collections) ---
    @Query("SELECT * FROM school_fees WHERE businessId = :businessId ORDER BY date DESC")
    fun getFees(businessId: Long): Flow<List<StudentFeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: StudentFeeEntity): Long

    // --- Attendance ---
    @Query("SELECT * FROM school_attendance WHERE businessId = :businessId AND dateString = :dateString")
    fun getAttendanceByDate(businessId: Long, dateString: String): Flow<List<StudentAttendanceEntity>>

    @Query("SELECT * FROM school_attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<StudentAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: StudentAttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: StudentAttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendances: List<StudentAttendanceEntity>)

    // --- Staff & Teachers ---
    @Query("SELECT * FROM school_staff WHERE businessId = :businessId ORDER BY name ASC")
    fun getStaff(businessId: Long): Flow<List<SchoolStaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: SchoolStaffEntity): Long

    @Query("DELETE FROM school_staff WHERE id = :id")
    suspend fun deleteStaff(id: Long)

    // --- Exam Results ---
    @Query("SELECT * FROM school_exam_results WHERE businessId = :businessId ORDER BY id DESC")
    fun getExamResults(businessId: Long): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity): Long

    @Query("DELETE FROM school_exam_results WHERE id = :id")
    suspend fun deleteExamResult(id: Long)
}
