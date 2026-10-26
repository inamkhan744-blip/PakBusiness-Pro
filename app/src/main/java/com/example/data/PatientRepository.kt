package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository pattern implementation for managing Patients in the Hospital & Clinic module.
 * Provides a clean API for ViewModel consumption with reactive Flow streams and
 * asynchronous CRUD operations executed on [Dispatchers.IO].
 */
class PatientRepository(
    private val patientDao: PatientDao
) {

    // ==========================================
    // REACTIVE READ / QUERY OPERATIONS
    // ==========================================

    /**
     * Observes all patients belonging to a specific business tenant ordered by newest first.
     */
    fun getPatients(businessId: Long): Flow<List<PatientEntity>> =
        patientDao.getPatients(businessId)

    /**
     * Performs live search across patient names, phone numbers, and medical history.
     */
    fun searchPatients(businessId: Long, query: String): Flow<List<PatientEntity>> =
        if (query.isBlank()) {
            patientDao.getPatients(businessId)
        } else {
            patientDao.searchPatients(businessId, query.trim())
        }

    /**
     * Observes a single patient record by its ID.
     */
    fun getPatientById(id: Long): Flow<PatientEntity?> =
        patientDao.getPatientById(id)

    /**
     * Observes the total number of patients registered in a clinic.
     */
    fun getPatientCountFlow(businessId: Long): Flow<Int> =
        patientDao.getPatientCountFlow(businessId)

    /**
     * Observes recent patient registrations up to the specified limit.
     */
    fun getRecentPatients(businessId: Long, limit: Int = 10): Flow<List<PatientEntity>> =
        patientDao.getRecentPatients(businessId, limit)

    /**
     * Filters patients by specific blood group (e.g. A+, B+, O+, AB-).
     */
    fun getPatientsByBloodGroup(businessId: Long, bloodGroup: String): Flow<List<PatientEntity>> =
        patientDao.getPatientsByBloodGroup(businessId, bloodGroup)

    /**
     * Filters patients by gender.
     */
    fun getPatientsByGender(businessId: Long, gender: String): Flow<List<PatientEntity>> =
        patientDao.getPatientsByGender(businessId, gender)

    // ==========================================
    // SUSPEND CRUD OPERATIONS (Dispatchers.IO)
    // ==========================================

    /**
     * Synchronously retrieves a single patient record by ID.
     */
    suspend fun getPatientByIdDirect(id: Long): PatientEntity? = withContext(Dispatchers.IO) {
        patientDao.getPatientByIdDirect(id)
    }

    /**
     * Finds an existing patient by phone number within the clinic tenant.
     */
    suspend fun getPatientByPhone(businessId: Long, phone: String): PatientEntity? = withContext(Dispatchers.IO) {
        patientDao.getPatientByPhone(businessId, phone.trim())
    }

    /**
     * Inserts a new patient and returns the generated row ID.
     */
    suspend fun insertPatient(patient: PatientEntity): Long = withContext(Dispatchers.IO) {
        patientDao.insertPatient(patient)
    }

    /**
     * Batch inserts multiple patient records.
     */
    suspend fun insertPatients(patients: List<PatientEntity>) = withContext(Dispatchers.IO) {
        patientDao.insertPatients(patients)
    }

    /**
     * Updates an existing patient's details.
     */
    suspend fun updatePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.updatePatient(patient)
    }

    /**
     * Upsert helper: inserts if [patient.id] is 0, otherwise updates existing record.
     */
    suspend fun savePatient(patient: PatientEntity): Long = withContext(Dispatchers.IO) {
        if (patient.id == 0L) {
            patientDao.insertPatient(patient)
        } else {
            patientDao.updatePatient(patient)
            patient.id
        }
    }

    /**
     * Deletes a patient entity record.
     */
    suspend fun deletePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.deletePatient(patient)
    }

    /**
     * Deletes a patient by ID.
     */
    suspend fun deletePatientById(id: Long) = withContext(Dispatchers.IO) {
        patientDao.deletePatientById(id)
    }

    /**
     * Safely deletes a patient ensuring business tenant isolation.
     */
    suspend fun deletePatientByIdAndBusiness(id: Long, businessId: Long) = withContext(Dispatchers.IO) {
        patientDao.deletePatientByIdAndBusiness(id, businessId)
    }

    /**
     * Returns total count of registered patients for a business.
     */
    suspend fun getPatientCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        patientDao.getPatientCount(businessId)
    }
}
