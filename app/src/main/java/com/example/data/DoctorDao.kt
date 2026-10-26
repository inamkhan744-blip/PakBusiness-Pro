package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Doctors / Consultants in the Hospital & Clinic module.
 * Provides multi-tenant reactive queries and CRUD operations.
 */
@Dao
interface DoctorDao {

    @Query("SELECT * FROM doctors WHERE businessId = :businessId ORDER BY name ASC")
    fun getDoctors(businessId: Long): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE businessId = :businessId AND isAvailable = 1 ORDER BY name ASC")
    fun getAvailableDoctors(businessId: Long): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE businessId = :businessId AND specialization = :specialization ORDER BY name ASC")
    fun getDoctorsBySpecialization(businessId: Long, specialization: String): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    fun getDoctorById(id: Long): Flow<DoctorEntity?>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    suspend fun getDoctorByIdDirect(id: Long): DoctorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteDoctorById(id: Long)

    @Query("DELETE FROM doctors WHERE id = :id AND businessId = :businessId")
    suspend fun deleteDoctorByIdAndBusiness(id: Long, businessId: Long)

    @Query("UPDATE doctors SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setDoctorAvailability(id: Long, isAvailable: Boolean)

    @Query("SELECT COUNT(*) FROM doctors WHERE businessId = :businessId")
    suspend fun getDoctorCount(businessId: Long): Int
}
