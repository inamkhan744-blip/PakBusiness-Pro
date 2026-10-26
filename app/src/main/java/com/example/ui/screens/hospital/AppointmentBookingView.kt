package com.example.ui.screens.hospital

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentEntity
import com.example.data.DoctorEntity
import com.example.data.PatientEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AppointmentBookingView(
    appointments: List<AppointmentEntity>,
    doctors: List<DoctorEntity>,
    patients: List<PatientEntity>,
    onBookAppointment: (patientId: Long, patientName: String, patientPhone: String, doctorId: Long, doctorName: String, specialization: String, date: Long, timeSlot: String, fee: Double, symptoms: String) -> Unit,
    onUpdateStatus: (id: Long, status: String) -> Unit,
    onDeleteAppointment: (id: Long) -> Unit,
    onWritePrescriptionForAppointment: (appointment: AppointmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf("ALL") }
    var selectedDoctorId by remember { mutableStateOf<Long?>(null) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<AppointmentEntity?>(null) }

    val filteredAppointments = remember(appointments, statusFilter, selectedDoctorId) {
        appointments.filter { appt ->
            val matchesStatus = when (statusFilter) {
                "SCHEDULED" -> appt.status == "SCHEDULED"
                "COMPLETED" -> appt.status == "COMPLETED"
                "CANCELLED" -> appt.status == "CANCELLED"
                else -> true
            }
            val matchesDoctor = selectedDoctorId == null || appt.doctorId == selectedDoctorId
            matchesStatus && matchesDoctor
        }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
            maximumFractionDigits = 0
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Stats Row (Total, Scheduled, Completed, Cancelled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    title = "Scheduled",
                    count = appointments.count { it.status == "SCHEDULED" },
                    color = Color(0xFF0284C7),
                    bgColor = Color(0xFFE0F2FE),
                    isSelected = statusFilter == "SCHEDULED",
                    onClick = { statusFilter = if (statusFilter == "SCHEDULED") "ALL" else "SCHEDULED" },
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    title = "Completed",
                    count = appointments.count { it.status == "COMPLETED" },
                    color = PakEmeraldPrimary,
                    bgColor = PakEmeraldContainer,
                    isSelected = statusFilter == "COMPLETED",
                    onClick = { statusFilter = if (statusFilter == "COMPLETED") "ALL" else "COMPLETED" },
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    title = "Cancelled",
                    count = appointments.count { it.status == "CANCELLED" },
                    color = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2),
                    isSelected = statusFilter == "CANCELLED",
                    onClick = { statusFilter = if (statusFilter == "CANCELLED") "ALL" else "CANCELLED" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Doctor Filter Bar
            if (doctors.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedDoctorId == null) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedDoctorId = null }
                        ) {
                            Text(
                                text = "All Doctors",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedDoctorId == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedDoctorId == null) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    items(doctors) { doc ->
                        val isSel = selectedDoctorId == doc.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedDoctorId = doc.id }
                        ) {
                            Text(
                                text = doc.name,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PakEmeraldContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No appointments found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to book a clinic consultation token",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAppointments, key = { it.id }) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            currencyFormatter = currencyFormatter,
                            onMarkCompleted = { onUpdateStatus(appt.id, "COMPLETED") },
                            onCancel = { onUpdateStatus(appt.id, "CANCELLED") },
                            onDelete = { appointmentToDelete = appt },
                            onWritePrescription = { onWritePrescriptionForAppointment(appt) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // FAB to Book Appointment
        FloatingActionButton(
            onClick = { showBookingDialog = true },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("btn_book_appointment")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Book Appointment")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Book Token", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // Dialog: Book Appointment
    if (showBookingDialog) {
        BookAppointmentDialog(
            doctors = doctors,
            patients = patients,
            onDismiss = { showBookingDialog = false },
            onConfirm = { patientId, patientName, patientPhone, doctorId, doctorName, spec, date, slot, fee, symptoms ->
                onBookAppointment(patientId, patientName, patientPhone, doctorId, doctorName, spec, date, slot, fee, symptoms)
                showBookingDialog = false
            }
        )
    }

    // Dialog: Delete Confirmation
    appointmentToDelete?.let { appt ->
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text("Delete Appointment?") },
            text = { Text("Remove Token #${appt.tokenNumber} for ${appt.patientName} from schedule?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAppointment(appt.id)
                        appointmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatPill(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else bgColor,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    currencyFormatter: NumberFormat,
    onMarkCompleted: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onWritePrescription: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(appointment.appointmentDate))

    val statusColor = when (appointment.status) {
        "COMPLETED" -> PakEmeraldPrimary
        "CANCELLED" -> Color(0xFFDC2626)
        else -> Color(0xFF0284C7)
    }

    val statusBg = when (appointment.status) {
        "COMPLETED" -> PakEmeraldContainer
        "CANCELLED" -> Color(0xFFFEE2E2)
        else -> Color(0xFFE0F2FE)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Token #, Status & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Token Number Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakEmeraldPrimary
                    ) {
                        Text(
                            text = "Token #${appointment.tokenNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = appointment.status,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateStr • ${appointment.timeSlot}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Patient details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = appointment.patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (appointment.patientPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${appointment.patientPhone})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Doctor details & Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${appointment.doctorName} (${appointment.doctorSpecialization})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = currencyFormatter.format(appointment.consultationFeePkr),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PakEmeraldPrimary
                )
            }

            if (appointment.symptoms.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Chief Complaints: ${appointment.symptoms}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (appointment.status == "SCHEDULED") {
                    Button(
                        onClick = onMarkCompleted,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onWritePrescription,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Prescription", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentDialog(
    doctors: List<DoctorEntity>,
    patients: List<PatientEntity>,
    onDismiss: () -> Unit,
    onConfirm: (patientId: Long, patientName: String, patientPhone: String, doctorId: Long, doctorName: String, specialization: String, date: Long, timeSlot: String, fee: Double, symptoms: String) -> Unit
) {
    var selectedDoctor by remember { mutableStateOf(doctors.firstOrNull()) }
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var customPatientName by remember { mutableStateOf("") }
    var customPatientPhone by remember { mutableStateOf("") }
    var isNewPatient by remember { mutableStateOf(patients.isEmpty()) }

    var appointmentDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedTimeSlot by remember { mutableStateOf("05:30 PM") }
    var symptoms by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetSlots = listOf(
        "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM",
        "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM",
        "08:00 PM", "08:30 PM", "09:00 PM"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Book Clinic Appointment", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Doctor selection
                Text("Select Doctor *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (doctors.isEmpty()) {
                    Text("Please add at least one doctor first in Doctors tab", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(doctors) { doc ->
                            val isSel = selectedDoctor?.id == doc.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                                modifier = Modifier.clickable { selectedDoctor = doc }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(doc.name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface)
                                    Text("${doc.specialization} • PKR ${doc.consultationFeePkr.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Patient Mode switch (Existing vs New)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Patient Details *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (patients.isNotEmpty()) {
                        TextButton(onClick = { isNewPatient = !isNewPatient }) {
                            Text(if (isNewPatient) "Select Existing" else "+ Enter New", fontSize = 11.sp, color = PakEmeraldPrimary)
                        }
                    }
                }

                if (!isNewPatient && patients.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(patients) { p ->
                            val isSel = selectedPatient?.id == p.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                                modifier = Modifier.clickable { selectedPatient = p }
                            ) {
                                Text(
                                    text = "${p.name} (${p.phone})",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customPatientName,
                        onValueChange = { customPatientName = it },
                        label = { Text("Patient Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customPatientPhone,
                        onValueChange = { customPatientPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Time slot selector
                Text("Select Time Slot *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetSlots) { slot ->
                        val isSel = selectedTimeSlot == slot
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedTimeSlot = slot }
                        ) {
                            Text(
                                text = slot,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms / Reason for Visit") },
                    placeholder = { Text("e.g. Fever, Cough, Regular Checkup") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoctor
                    if (doc == null) {
                        errorMessage = "Please select a doctor"
                        return@Button
                    }

                    val patientName = if (isNewPatient) customPatientName.trim() else selectedPatient?.name.orEmpty()
                    val patientPhone = if (isNewPatient) customPatientPhone.trim() else selectedPatient?.phone.orEmpty()
                    val patientId = if (isNewPatient) 0L else (selectedPatient?.id ?: 0L)

                    if (patientName.isBlank()) {
                        errorMessage = "Please specify patient name"
                        return@Button
                    }

                    onConfirm(
                        patientId,
                        patientName,
                        patientPhone,
                        doc.id,
                        doc.name,
                        doc.specialization,
                        appointmentDateMillis,
                        selectedTimeSlot,
                        doc.consultationFeePkr,
                        symptoms.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Issue Token")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
