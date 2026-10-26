package com.example

import com.example.data.PatientDao
import com.example.data.PatientEntity
import com.example.data.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PatientRepositoryTest {

    private lateinit var fakePatientDao: FakePatientDao
    private lateinit var repository: PatientRepository

    @Before
    fun setup() {
        fakePatientDao = FakePatientDao()
        repository = PatientRepository(fakePatientDao)
    }

    @Test
    fun `test MR number format`() {
        val patient = PatientEntity(
            id = 42L,
            businessId = 1L,
            name = "Muhammad Ali",
            phone = "03001234567",
            age = 35,
            gender = "Male",
            bloodGroup = "B+"
        )
        assertEquals("MR-42", patient.mrNumber)
    }

    @Test
    fun `test insert and get patient by id`() = runBlocking {
        val newPatient = PatientEntity(
            businessId = 1L,
            name = "Fatima Bibi",
            phone = "03129876543",
            age = 28,
            gender = "Female",
            bloodGroup = "O+",
            address = "Lahore, Pakistan",
            medicalHistory = "Seasonal Allergies"
        )

        val generatedId = repository.insertPatient(newPatient)
        assertTrue(generatedId > 0)

        val retrieved = repository.getPatientByIdDirect(generatedId)
        assertNotNull(retrieved)
        assertEquals("Fatima Bibi", retrieved?.name)
        assertEquals("03129876543", retrieved?.phone)
        assertEquals("O+", retrieved?.bloodGroup)
    }

    @Test
    fun `test savePatient inserts new and updates existing`() = runBlocking {
        // 1. Insert when id == 0
        val p1 = PatientEntity(
            id = 0L,
            businessId = 1L,
            name = "Tariq Mahmood",
            phone = "03331112233",
            age = 45,
            gender = "Male"
        )
        val insertedId = repository.savePatient(p1)
        assertTrue(insertedId > 0)

        // 2. Update when id > 0
        val toUpdate = repository.getPatientByIdDirect(insertedId)!!
        val updated = toUpdate.copy(
            medicalHistory = "Hypertension diagnosed",
            bloodGroup = "A+"
        )
        val updateResultId = repository.savePatient(updated)
        assertEquals(insertedId, updateResultId)

        val fetchedUpdated = repository.getPatientByIdDirect(insertedId)
        assertEquals("Hypertension diagnosed", fetchedUpdated?.medicalHistory)
        assertEquals("A+", fetchedUpdated?.bloodGroup)
    }

    @Test
    fun `test search patients and flow observation`() = runBlocking {
        repository.insertPatient(
            PatientEntity(
                businessId = 1L,
                name = "Dr. Zahid Clinic Patient",
                phone = "03211112222",
                age = 50,
                gender = "Male"
            )
        )
        repository.insertPatient(
            PatientEntity(
                businessId = 1L,
                name = "Ayesha Khan",
                phone = "03455556666",
                age = 22,
                gender = "Female",
                bloodGroup = "AB+"
            )
        )

        val searchResult = repository.searchPatients(1L, "Ayesha").first()
        assertEquals(1, searchResult.size)
        assertEquals("Ayesha Khan", searchResult.first().name)

        val allPatients = repository.getPatients(1L).first()
        assertEquals(2, allPatients.size)
    }

    @Test
    fun `test delete patient operations`() = runBlocking {
        val id = repository.insertPatient(
            PatientEntity(
                businessId = 1L,
                name = "To Delete",
                phone = "03000000000",
                age = 40,
                gender = "Male"
            )
        )

        assertNotNull(repository.getPatientByIdDirect(id))

        repository.deletePatientById(id)
        assertNull(repository.getPatientByIdDirect(id))
        assertEquals(0, repository.getPatientCount(1L))
    }

    /**
     * In-memory Fake DAO implementing PatientDao for JVM unit testing.
     */
    private class FakePatientDao : PatientDao {
        private val storage = mutableListOf<PatientEntity>()
        private var autoId = 1L

        override fun getPatients(businessId: Long): Flow<List<PatientEntity>> =
            flowOf(storage.filter { it.businessId == businessId }.sortedByDescending { it.createdAt })

        override fun searchPatients(businessId: Long, query: String): Flow<List<PatientEntity>> =
            flowOf(storage.filter {
                it.businessId == businessId && (
                    it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.medicalHistory.contains(query, ignoreCase = true)
                )
            }.sortedBy { it.name })

        override fun getPatientById(id: Long): Flow<PatientEntity?> =
            flowOf(storage.find { it.id == id })

        override suspend fun getPatientByIdDirect(id: Long): PatientEntity? =
            storage.find { it.id == id }

        override suspend fun getPatientByPhone(businessId: Long, phone: String): PatientEntity? =
            storage.find { it.businessId == businessId && it.phone == phone }

        override suspend fun insertPatient(patient: PatientEntity): Long {
            val assignedId = if (patient.id <= 0L) autoId++ else patient.id
            val entity = patient.copy(id = assignedId)
            storage.removeAll { it.id == assignedId }
            storage.add(entity)
            return assignedId
        }

        override suspend fun insertPatients(patients: List<PatientEntity>) {
            patients.forEach { insertPatient(it) }
        }

        override suspend fun updatePatient(patient: PatientEntity) {
            storage.removeAll { it.id == patient.id }
            storage.add(patient)
        }

        override suspend fun deletePatient(patient: PatientEntity) {
            storage.removeAll { it.id == patient.id }
        }

        override suspend fun deletePatientById(id: Long) {
            storage.removeAll { it.id == id }
        }

        override suspend fun deletePatientByIdAndBusiness(id: Long, businessId: Long) {
            storage.removeAll { it.id == id && it.businessId == businessId }
        }

        override suspend fun getPatientCount(businessId: Long): Int =
            storage.count { it.businessId == businessId }

        override fun getPatientCountFlow(businessId: Long): Flow<Int> =
            flowOf(storage.count { it.businessId == businessId })

        override fun getRecentPatients(businessId: Long, limit: Int): Flow<List<PatientEntity>> =
            flowOf(storage.filter { it.businessId == businessId }.sortedByDescending { it.createdAt }.take(limit))

        override fun getPatientsByBloodGroup(businessId: Long, bloodGroup: String): Flow<List<PatientEntity>> =
            flowOf(storage.filter { it.businessId == businessId && it.bloodGroup.equals(bloodGroup, ignoreCase = true) })

        override fun getPatientsByGender(businessId: Long, gender: String): Flow<List<PatientEntity>> =
            flowOf(storage.filter { it.businessId == businessId && it.gender.equals(gender, ignoreCase = true) })
    }
}
