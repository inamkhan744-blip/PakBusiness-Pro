package com.example.ui.screens.hospital

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentEntity
import com.example.data.BusinessEntity
import com.example.data.DoctorEntity
import com.example.data.PatientEntity
import com.example.data.PrescriptionEntity
import com.example.ui.components.DailySalesClinicAppointmentsVisualization
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary

enum class HospitalSubTab(val title: String, val icon: ImageVector) {
    PATIENTS("Patients", Icons.Default.People),
    DOCTORS("Doctors", Icons.Default.MedicalServices),
    APPOINTMENTS("Appointments", Icons.Default.CalendarMonth),
    PRESCRIPTIONS("Prescriptions", Icons.Default.Description),
    DAILY_SUMMARY("Daily Summary (PKR)", Icons.Default.Assessment)
}

@Composable
fun HospitalModuleScreen(
    business: BusinessEntity?,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    appointments: List<AppointmentEntity>,
    prescriptions: List<PrescriptionEntity>,
    onSavePatient: (name: String, phone: String, age: Int, gender: String, bloodGroup: String, address: String, history: String, id: Long) -> Unit,
    onDeletePatient: (id: Long) -> Unit,
    onSaveDoctor: (name: String, spec: String, qual: String, fee: Double, days: String, hours: String, phone: String, isAvail: Boolean, id: Long) -> Unit,
    onDeleteDoctor: (id: Long) -> Unit,
    onToggleDoctorAvailability: (id: Long, isAvailable: Boolean) -> Unit,
    onBookAppointment: (patientId: Long, patientName: String, patientPhone: String, doctorId: Long, doctorName: String, spec: String, date: Long, timeSlot: String, fee: Double, symptoms: String) -> Unit,
    onUpdateAppointmentStatus: (id: Long, status: String) -> Unit,
    onDeleteAppointment: (id: Long) -> Unit,
    onSavePrescription: (prescription: PrescriptionEntity, onSaved: (Long) -> Unit) -> Unit,
    onDeletePrescription: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(HospitalSubTab.PATIENTS) }
    var rxTargetPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var rxTargetAppointment by remember { mutableStateOf<AppointmentEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hospital Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PakEmeraldContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Hospital & Clinic Care",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = business?.name ?: "PakBusiness Health Suite",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PakEmeraldContainer
                    ) {
                        Text(
                            text = "${appointments.count { it.status == "SCHEDULED" }} In Queue",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Sub-tabs (Patients, Doctors, Appointments, Prescriptions)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = PakEmeraldPrimary,
                            height = 3.dp
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    HospitalSubTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val badgeCount = when (tab) {
                            HospitalSubTab.PATIENTS -> patients.size
                            HospitalSubTab.DOCTORS -> doctors.size
                            HospitalSubTab.APPOINTMENTS -> appointments.count { it.status == "SCHEDULED" }
                            HospitalSubTab.PRESCRIPTIONS -> prescriptions.size
                            HospitalSubTab.DAILY_SUMMARY -> 0
                        }

                        Tab(
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                if (tab != HospitalSubTab.PRESCRIPTIONS) {
                                    rxTargetPatient = null
                                    rxTargetAppointment = null
                                }
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp,
                                        color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (badgeCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = badgeCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("tab_hospital_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Sub-tab content with Crossfade
        Crossfade(targetState = selectedTab, label = "hospital_tab_transition") { tab ->
            when (tab) {
                HospitalSubTab.PATIENTS -> {
                    PatientsListView(
                        patients = patients,
                        onSavePatient = onSavePatient,
                        onDeletePatient = onDeletePatient,
                        onBookAppointmentForPatient = { patient ->
                            selectedTab = HospitalSubTab.APPOINTMENTS
                        },
                        onWritePrescriptionForPatient = { patient ->
                            rxTargetPatient = patient
                            rxTargetAppointment = null
                            selectedTab = HospitalSubTab.PRESCRIPTIONS
                        }
                    )
                }

                HospitalSubTab.DOCTORS -> {
                    DoctorsListView(
                        doctors = doctors,
                        onSaveDoctor = onSaveDoctor,
                        onDeleteDoctor = onDeleteDoctor,
                        onToggleAvailability = onToggleDoctorAvailability,
                        onBookAppointmentWithDoctor = { doctor ->
                            selectedTab = HospitalSubTab.APPOINTMENTS
                        }
                    )
                }

                HospitalSubTab.APPOINTMENTS -> {
                    AppointmentBookingView(
                        appointments = appointments,
                        doctors = doctors,
                        patients = patients,
                        onBookAppointment = onBookAppointment,
                        onUpdateStatus = onUpdateAppointmentStatus,
                        onDeleteAppointment = onDeleteAppointment,
                        onWritePrescriptionForAppointment = { appt ->
                            rxTargetAppointment = appt
                            rxTargetPatient = patients.firstOrNull { it.id == appt.patientId }
                            selectedTab = HospitalSubTab.PRESCRIPTIONS
                        }
                    )
                }

                HospitalSubTab.PRESCRIPTIONS -> {
                    PrescriptionStudioView(
                        business = business,
                        prescriptions = prescriptions,
                        patients = patients,
                        doctors = doctors,
                        preselectedPatient = rxTargetPatient,
                        preselectedAppointment = rxTargetAppointment,
                        onSavePrescription = onSavePrescription,
                        onDeletePrescription = onDeletePrescription
                    )
                }

                HospitalSubTab.DAILY_SUMMARY -> {
                    DailySalesClinicAppointmentsVisualization(
                        appointments = appointments,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
