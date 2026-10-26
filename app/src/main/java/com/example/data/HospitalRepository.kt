package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HospitalRepository(private val hospitalDao: HospitalDao) {

    // --- Patients ---

    fun getPatients(businessId: Long): Flow<List<PatientEntity>> =
        hospitalDao.getPatients(businessId)

    fun searchPatients(businessId: Long, query: String): Flow<List<PatientEntity>> =
        if (query.isBlank()) hospitalDao.getPatients(businessId)
        else hospitalDao.searchPatients(businessId, query.trim())

    fun getPatientById(id: Long): Flow<PatientEntity?> =
        hospitalDao.getPatientById(id)

    suspend fun savePatient(patient: PatientEntity): Long {
        return if (patient.id == 0L) {
            hospitalDao.insertPatient(patient)
        } else {
            hospitalDao.updatePatient(patient)
            patient.id
        }
    }

    suspend fun deletePatient(id: Long) {
        hospitalDao.deletePatientById(id)
    }

    // --- Doctors ---

    fun getDoctors(businessId: Long): Flow<List<DoctorEntity>> =
        hospitalDao.getDoctors(businessId)

    fun getDoctorById(id: Long): Flow<DoctorEntity?> =
        hospitalDao.getDoctorById(id)

    suspend fun saveDoctor(doctor: DoctorEntity): Long {
        return if (doctor.id == 0L) {
            hospitalDao.insertDoctor(doctor)
        } else {
            hospitalDao.updateDoctor(doctor)
            doctor.id
        }
    }

    suspend fun deleteDoctor(id: Long) {
        hospitalDao.deleteDoctorById(id)
    }

    suspend fun setDoctorAvailability(id: Long, isAvailable: Boolean) {
        hospitalDao.setDoctorAvailability(id, isAvailable)
    }

    // --- Appointments ---

    fun getAppointments(businessId: Long): Flow<List<AppointmentEntity>> =
        hospitalDao.getAppointments(businessId)

    suspend fun getNextTokenNumber(businessId: Long, dateTimestamp: Long): Int {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        val currentMax = hospitalDao.getMaxTokenNumberForDate(businessId, startOfDay, endOfDay) ?: 0
        return currentMax + 1
    }

    suspend fun bookAppointment(appointment: AppointmentEntity): Long {
        val token = if (appointment.tokenNumber <= 0) {
            getNextTokenNumber(appointment.businessId, appointment.appointmentDate)
        } else {
            appointment.tokenNumber
        }
        return if (appointment.id == 0L) {
            hospitalDao.insertAppointment(appointment.copy(tokenNumber = token))
        } else {
            hospitalDao.updateAppointment(appointment.copy(tokenNumber = token))
            appointment.id
        }
    }

    suspend fun updateAppointmentStatus(id: Long, status: String) {
        hospitalDao.updateAppointmentStatus(id, status)
    }

    suspend fun deleteAppointment(id: Long) {
        hospitalDao.deleteAppointmentById(id)
    }

    // --- Prescriptions ---

    fun getPrescriptions(businessId: Long): Flow<List<PrescriptionEntity>> =
        hospitalDao.getPrescriptions(businessId)

    fun getPrescriptionsByPatient(patientId: Long): Flow<List<PrescriptionEntity>> =
        hospitalDao.getPrescriptionsByPatient(patientId)

    suspend fun savePrescription(prescription: PrescriptionEntity): Long {
        return if (prescription.id == 0L) {
            hospitalDao.insertPrescription(prescription)
        } else {
            hospitalDao.updatePrescription(prescription)
            prescription.id
        }
    }

    suspend fun deletePrescription(id: Long) {
        hospitalDao.deletePrescriptionById(id)
    }

    // --- Seed Demo Data ---

    suspend fun ensureInitialHospitalData(businessId: Long) {
        return // Disabled: do not seed fake hospital entries
        val doctorCount = hospitalDao.getDoctorCount(businessId)
        if (doctorCount == 0) {
            val initialDoctors = listOf(
                DoctorEntity(
                    businessId = businessId,
                    name = "Dr. Muhammad Tariq",
                    specialization = "General Physician",
                    qualification = "MBBS, FCPS (Medicine)",
                    consultationFeePkr = 1500.0,
                    availableDays = "Mon, Tue, Wed, Thu, Fri, Sat",
                    availableHours = "05:00 PM - 09:30 PM",
                    phone = "0300-1234567",
                    isAvailable = true
                ),
                DoctorEntity(
                    businessId = businessId,
                    name = "Dr. Ayesha Siddiqui",
                    specialization = "Gynecologist & Obstetrician",
                    qualification = "MBBS, MCPS, FCPS (Gynae)",
                    consultationFeePkr = 2000.0,
                    availableDays = "Mon, Wed, Fri",
                    availableHours = "02:00 PM - 06:00 PM",
                    phone = "0321-7654321",
                    isAvailable = true
                ),
                DoctorEntity(
                    businessId = businessId,
                    name = "Dr. Usman Farooq",
                    specialization = "Pediatrician (Child Specialist)",
                    qualification = "MBBS, DCH, FCPS (Pediatrics)",
                    consultationFeePkr = 1800.0,
                    availableDays = "Daily (Except Sun)",
                    availableHours = "06:00 PM - 10:00 PM",
                    phone = "0333-9876543",
                    isAvailable = true
                ),
                DoctorEntity(
                    businessId = businessId,
                    name = "Dr. Hamza Bilal",
                    specialization = "Cardiologist",
                    qualification = "MBBS, MD (Cardiology), FACC",
                    consultationFeePkr = 2500.0,
                    availableDays = "Tue, Thu, Sat",
                    availableHours = "04:00 PM - 08:00 PM",
                    phone = "0345-5551234",
                    isAvailable = true
                )
            )
            initialDoctors.forEach { hospitalDao.insertDoctor(it) }
        }

        val patientCount = hospitalDao.getPatientCount(businessId)
        if (patientCount == 0) {
            val initialPatients = listOf(
                PatientEntity(
                    businessId = businessId,
                    name = "Zahid Mahmood",
                    phone = "0301-4455667",
                    age = 45,
                    gender = "Male",
                    bloodGroup = "B+",
                    address = "Gulberg III, Lahore",
                    medicalHistory = "Hypertension, Mild Gastritis"
                ),
                PatientEntity(
                    businessId = businessId,
                    name = "Fatima Noor",
                    phone = "0312-9988776",
                    age = 29,
                    gender = "Female",
                    bloodGroup = "O+",
                    address = "F-8/2, Islamabad",
                    medicalHistory = "Seasonal Allergy, Asthma"
                ),
                PatientEntity(
                    businessId = businessId,
                    name = "Bilal Ahmed",
                    phone = "0334-1122334",
                    age = 8,
                    gender = "Male",
                    bloodGroup = "A+",
                    address = "Clifton Block 5, Karachi",
                    medicalHistory = "Tonsillitis recurrence"
                )
            )
            val p1Id = hospitalDao.insertPatient(initialPatients[0])
            val p2Id = hospitalDao.insertPatient(initialPatients[1])
            val p3Id = hospitalDao.insertPatient(initialPatients[2])

            // Seed sample appointment
            val today = System.currentTimeMillis()
            hospitalDao.insertAppointment(
                AppointmentEntity(
                    businessId = businessId,
                    patientId = p1Id,
                    patientName = "Zahid Mahmood",
                    patientPhone = "0301-4455667",
                    doctorId = 1L,
                    doctorName = "Dr. Muhammad Tariq",
                    doctorSpecialization = "General Physician",
                    appointmentDate = today,
                    timeSlot = "05:30 PM",
                    tokenNumber = 1,
                    consultationFeePkr = 1500.0,
                    status = "SCHEDULED",
                    symptoms = "Headache, fluctuating BP readings"
                )
            )
            hospitalDao.insertAppointment(
                AppointmentEntity(
                    businessId = businessId,
                    patientId = p2Id,
                    patientName = "Fatima Noor",
                    patientPhone = "0312-9988776",
                    doctorId = 1L,
                    doctorName = "Dr. Muhammad Tariq",
                    doctorSpecialization = "General Physician",
                    appointmentDate = today,
                    timeSlot = "06:00 PM",
                    tokenNumber = 2,
                    consultationFeePkr = 1500.0,
                    status = "SCHEDULED",
                    symptoms = "Persistent dry cough, wheezing"
                )
            )

            // Seed sample prescription
            val sampleMeds = listOf(
                PrescriptionMedicineItem(
                    name = "Tab. Amlodipine 5mg",
                    dosage = "1-0-0 (Morning)",
                    duration = "30 Days",
                    instructions = "Take after breakfast"
                ),
                PrescriptionMedicineItem(
                    name = "Tab. Panadol 500mg",
                    dosage = "1-0-1 (SOS)",
                    duration = "5 Days",
                    instructions = "If severe headache"
                ),
                PrescriptionMedicineItem(
                    name = "Cap. Omeprazole 20mg",
                    dosage = "1-0-0",
                    duration = "14 Days",
                    instructions = "30 mins before breakfast"
                )
            )
            hospitalDao.insertPrescription(
                PrescriptionEntity(
                    businessId = businessId,
                    patientId = p1Id,
                    patientName = "Zahid Mahmood",
                    patientAge = 45,
                    patientGender = "Male",
                    patientPhone = "0301-4455667",
                    doctorId = 1L,
                    doctorName = "Dr. Muhammad Tariq",
                    doctorSpecialization = "General Physician",
                    vitalsBp = "135/85 mmHg",
                    vitalsPulse = "76 bpm",
                    vitalsTemp = "98.4 °F",
                    vitalsWeight = "78 kg",
                    diagnosis = "Essential Hypertension & Mild Dyspepsia",
                    symptoms = "Occasional morning dizziness, post-prandial fullness",
                    medicinesJson = PrescriptionEntity.encodeMedicines(sampleMeds),
                    labTests = "Lipid Profile, Serum Creatinine, Fasting Blood Sugar",
                    advice = "Reduce dietary sodium intake. Walk 30 minutes daily. Monitor BP twice a week.",
                    followUpDays = 14
                )
            )
        }
    }
}
