package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao : PatientDao, DoctorDao, AppointmentDao {

    // --- Prescriptions ---

    @Query("SELECT * FROM prescriptions WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getPrescriptions(businessId: Long): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getPrescriptionsByPatient(patientId: Long): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getPrescriptionById(id: Long): Flow<PrescriptionEntity?>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionByIdDirect(id: Long): PrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long

    @Update
    suspend fun updatePrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: Long)
}
