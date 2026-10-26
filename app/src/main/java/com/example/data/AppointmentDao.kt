package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Appointments and OPD tokens in the Hospital & Clinic module.
 * Provides multi-tenant reactive queries and CRUD operations.
 */
@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE businessId = :businessId ORDER BY appointmentDate DESC, tokenNumber ASC")
    fun getAppointments(businessId: Long): Flow<List<AppointmentEntity>>

    @Query("""
        SELECT * FROM appointments 
        WHERE businessId = :businessId AND appointmentDate >= :startOfDay AND appointmentDate <= :endOfDay
        ORDER BY tokenNumber ASC
    """)
    fun getAppointmentsForDate(businessId: Long, startOfDay: Long, endOfDay: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE businessId = :businessId AND doctorId = :doctorId ORDER BY appointmentDate DESC, tokenNumber ASC")
    fun getAppointmentsByDoctor(businessId: Long, doctorId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE businessId = :businessId AND patientId = :patientId ORDER BY appointmentDate DESC")
    fun getAppointmentsByPatient(businessId: Long, patientId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    fun getAppointmentById(id: Long): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentByIdDirect(id: Long): AppointmentEntity?

    @Query("""
        SELECT MAX(tokenNumber) FROM appointments 
        WHERE businessId = :businessId AND appointmentDate >= :startOfDay AND appointmentDate <= :endOfDay
    """)
    suspend fun getMaxTokenNumberForDate(businessId: Long, startOfDay: Long, endOfDay: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)

    @Query("DELETE FROM appointments WHERE id = :id AND businessId = :businessId")
    suspend fun deleteAppointmentByIdAndBusiness(id: Long, businessId: Long)

    @Query("SELECT COUNT(*) FROM appointments WHERE businessId = :businessId")
    suspend fun getAppointmentCount(businessId: Long): Int

    @Query("SELECT COUNT(*) FROM appointments WHERE businessId = :businessId AND status = :status")
    suspend fun getAppointmentCountByStatus(businessId: Long, status: String): Int
}
