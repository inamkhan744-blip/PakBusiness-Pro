package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Patients in the Hospital & Clinic module.
 * Provides multi-tenant reactive queries and CRUD operations.
 */
@Dao
interface PatientDao {

    @Query("SELECT * FROM patients WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getPatients(businessId: Long): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients 
        WHERE businessId = :businessId 
        AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR medicalHistory LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchPatients(businessId: Long, query: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun getPatientById(id: Long): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientByIdDirect(id: Long): PatientEntity?

    @Query("SELECT * FROM patients WHERE businessId = :businessId AND phone = :phone LIMIT 1")
    suspend fun getPatientByPhone(businessId: Long, phone: String): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deletePatientById(id: Long)

    @Query("DELETE FROM patients WHERE id = :id AND businessId = :businessId")
    suspend fun deletePatientByIdAndBusiness(id: Long, businessId: Long)

    @Query("SELECT COUNT(*) FROM patients WHERE businessId = :businessId")
    suspend fun getPatientCount(businessId: Long): Int

    @Query("SELECT COUNT(*) FROM patients WHERE businessId = :businessId")
    fun getPatientCountFlow(businessId: Long): Flow<Int>

    @Query("SELECT * FROM patients WHERE businessId = :businessId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPatients(businessId: Long, limit: Int = 10): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE businessId = :businessId AND bloodGroup = :bloodGroup ORDER BY name ASC")
    fun getPatientsByBloodGroup(businessId: Long, bloodGroup: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE businessId = :businessId AND gender = :gender ORDER BY name ASC")
    fun getPatientsByGender(businessId: Long, gender: String): Flow<List<PatientEntity>>
}
