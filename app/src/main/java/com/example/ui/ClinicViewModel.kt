package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClinicDao
import com.example.data.PatientEntity
import com.example.data.PatientRepository
import com.example.data.PrescriptionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ClinicViewModel(
    application: Application,
    private val clinicDao: ClinicDao = AppDatabase.getDatabase(application).clinicDao(),
    val patientRepository: PatientRepository = PatientRepository(AppDatabase.getDatabase(application).patientDao())
) : BaseBusinessViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val patients: StateFlow<List<PatientEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) patientRepository.getPatients(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prescriptions: StateFlow<List<PrescriptionEntity>> = activeBusiness.flatMapLatest { biz ->
        if (biz != null) clinicDao.getPrescriptions(biz.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPatients: StateFlow<List<PatientEntity>> = combine(patients, searchQuery) { list, q ->
        if (q.isBlank()) list else {
            list.filter { it.name.contains(q, ignoreCase = true) || it.phone.contains(q, ignoreCase = true) || it.mrNumber.contains(q, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun registerPatient(name: String, age: String, gender: String, phone: String, bloodGroup: String = "", address: String = "", medicalHistory: String = "") {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            patientRepository.insertPatient(
                PatientEntity(
                    businessId = bizId,
                    name = name.trim(),
                    age = age.trim().toIntOrNull() ?: 0,
                    gender = gender,
                    phone = phone.trim(),
                    bloodGroup = bloodGroup.trim(),
                    address = address.trim(),
                    medicalHistory = medicalHistory.trim()
                )
            )
        }
    }

    fun deletePatient(patientId: Long) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            patientRepository.deletePatientByIdAndBusiness(patientId, bizId)
        }
    }

    fun savePrescription(
        patientId: Long,
        patientName: String,
        bp: String,
        pulse: String,
        temp: String,
        weight: String,
        diagnosis: String,
        medicines: String,
        advice: String,
        fee: Double
    ) {
        val bizId = activeBusiness.value?.id ?: return
        launchWithLoading {
            clinicDao.insertPrescription(
                PrescriptionEntity(
                    businessId = bizId,
                    patientId = patientId,
                    patientName = patientName,
                    bp = bp,
                    pulse = pulse,
                    temp = temp,
                    weight = weight,
                    diagnosis = diagnosis,
                    medicinesRaw = medicines,
                    advice = advice,
                    consultationFeePkr = fee
                )
            )
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ClinicViewModel(application) as T
                }
            }
    }
}
