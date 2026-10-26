package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalonDao {

    // --- Services ---
    @Query("SELECT * FROM salon_services WHERE businessId = :businessId ORDER BY name ASC")
    fun getServices(businessId: Long): Flow<List<SalonServiceEntity>>

    @Query("SELECT * FROM salon_services WHERE businessId = :businessId AND category = :category ORDER BY name ASC")
    fun getServicesByCategory(businessId: Long, category: String): Flow<List<SalonServiceEntity>>

    @Query("SELECT * FROM salon_services WHERE id = :id")
    fun getServiceById(id: Long): Flow<SalonServiceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: SalonServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<SalonServiceEntity>)

    @Update
    suspend fun updateService(service: SalonServiceEntity)

    @Query("DELETE FROM salon_services WHERE id = :id")
    suspend fun deleteService(id: Long)

    @Query("SELECT COUNT(*) FROM salon_services WHERE businessId = :businessId")
    suspend fun getServicesCount(businessId: Long): Int

    // --- Stylists ---
    @Query("SELECT * FROM salon_stylists WHERE businessId = :businessId ORDER BY totalEarnedCommission DESC, name ASC")
    fun getStylists(businessId: Long): Flow<List<StylistEntity>>

    @Query("SELECT * FROM salon_stylists WHERE id = :id")
    fun getStylistById(id: Long): Flow<StylistEntity?>

    @Query("SELECT * FROM salon_stylists WHERE id = :id")
    suspend fun getStylistByIdDirect(id: Long): StylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylist(stylist: StylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylists(stylists: List<StylistEntity>)

    @Update
    suspend fun updateStylist(stylist: StylistEntity)

    @Query("UPDATE salon_stylists SET totalEarnedCommission = totalEarnedCommission + :commission, totalServicesCompleted = totalServicesCompleted + 1 WHERE id = :id")
    suspend fun addCommissionToStylist(id: Long, commission: Double)

    @Query("DELETE FROM salon_stylists WHERE id = :id")
    suspend fun deleteStylist(id: Long)

    @Query("SELECT COUNT(*) FROM salon_stylists WHERE businessId = :businessId")
    suspend fun getStylistsCount(businessId: Long): Int

    // --- Appointments ---
    @Query("SELECT * FROM salon_appointments WHERE businessId = :businessId ORDER BY appointmentDate DESC, appointmentTime ASC")
    fun getAppointments(businessId: Long): Flow<List<SalonAppointmentEntity>>

    @Query("SELECT * FROM salon_appointments WHERE businessId = :businessId AND appointmentDate = :date ORDER BY appointmentTime ASC")
    fun getAppointmentsByDate(businessId: Long, date: String): Flow<List<SalonAppointmentEntity>>

    @Query("SELECT * FROM salon_appointments WHERE businessId = :businessId AND status = :status ORDER BY appointmentDate DESC")
    fun getAppointmentsByStatus(businessId: Long, status: String): Flow<List<SalonAppointmentEntity>>

    @Query("SELECT * FROM salon_appointments WHERE businessId = :businessId AND stylistId = :stylistId ORDER BY appointmentDate DESC")
    fun getAppointmentsByStylist(businessId: Long, stylistId: Long): Flow<List<SalonAppointmentEntity>>

    @Query("SELECT * FROM salon_appointments WHERE id = :id")
    suspend fun getAppointmentByIdDirect(id: Long): SalonAppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: SalonAppointmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<SalonAppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: SalonAppointmentEntity)

    @Query("UPDATE salon_appointments SET status = :status, paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String, paymentStatus: String)

    @Query("DELETE FROM salon_appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Long)

    @Query("SELECT COUNT(*) FROM salon_appointments WHERE businessId = :businessId")
    suspend fun getAppointmentsCount(businessId: Long): Int
}
