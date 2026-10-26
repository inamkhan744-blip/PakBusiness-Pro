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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DoctorEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DoctorsListView(
    doctors: List<DoctorEntity>,
    onSaveDoctor: (name: String, specialization: String, qualification: String, fee: Double, days: String, hours: String, phone: String, isAvailable: Boolean, id: Long) -> Unit,
    onDeleteDoctor: (id: Long) -> Unit,
    onToggleAvailability: (id: Long, isAvailable: Boolean) -> Unit,
    onBookAppointmentWithDoctor: (doctor: DoctorEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSpecialization by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDoctor by remember { mutableStateOf<DoctorEntity?>(null) }
    var doctorToDelete by remember { mutableStateOf<DoctorEntity?>(null) }

    val specializations = remember(doctors) {
        listOf("All") + doctors.map { it.specialization }.distinct().sorted()
    }

    val filteredDoctors = remember(doctors, selectedSpecialization) {
        if (selectedSpecialization == "All") doctors
        else doctors.filter { it.specialization.equals(selectedSpecialization, ignoreCase = true) }
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
            // Specialization Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(specializations) { spec ->
                    val isSelected = selectedSpecialization == spec
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedSpecialization = spec }
                    ) {
                        Text(
                            text = spec,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val availableCount = doctors.count { it.isAvailable }
                Text(
                    text = "${doctors.size} Specialists • $availableCount on Duty",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredDoctors.isEmpty()) {
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
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No doctors listed in this category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to add doctors, clinics, and visiting consultants",
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
                    items(filteredDoctors, key = { it.id }) { doctor ->
                        DoctorCard(
                            doctor = doctor,
                            currencyFormatter = currencyFormatter,
                            onEditClick = { editingDoctor = doctor },
                            onDeleteClick = { doctorToDelete = doctor },
                            onToggleAvailability = { onToggleAvailability(doctor.id, it) },
                            onBookAppointment = { onBookAppointmentWithDoctor(doctor) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // FAB to Add Doctor
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("btn_add_doctor")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Doctor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // Dialog: Add / Edit Doctor
    if (showAddDialog || editingDoctor != null) {
        val target = editingDoctor
        AddEditDoctorDialog(
            doctor = target,
            onDismiss = {
                showAddDialog = false
                editingDoctor = null
            },
            onConfirm = { name, spec, qual, fee, days, hours, phone, isAvail ->
                onSaveDoctor(name, spec, qual, fee, days, hours, phone, isAvail, target?.id ?: 0L)
                showAddDialog = false
                editingDoctor = null
            }
        )
    }

    // Dialog: Delete Confirmation
    doctorToDelete?.let { doctor ->
        AlertDialog(
            onDismissRequest = { doctorToDelete = null },
            title = { Text("Remove Doctor Record?") },
            text = { Text("Are you sure you want to remove ${doctor.name} from the medical registry?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDoctor(doctor.id)
                        doctorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { doctorToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorEntity,
    currencyFormatter: NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onBookAppointment: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_card_${doctor.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Doctor Avatar
                Surface(
                    shape = CircleShape,
                    color = if (doctor.isAvailable) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = if (doctor.isAvailable) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = doctor.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Consultation Fee Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakEmeraldContainer
                        ) {
                            Text(
                                text = "${currencyFormatter.format(doctor.consultationFeePkr)} Fee",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = doctor.qualification,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Specialization Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = doctor.specialization,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timings and Availability Row
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Days: ${doctor.availableDays}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Timing: ${doctor.availableHours}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (doctor.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Contact: ${doctor.phone}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row with Availability switch & Book button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("switch_doctor_avail_${doctor.id}")
                ) {
                    Switch(
                        checked = doctor.isAvailable,
                        onCheckedChange = onToggleAvailability,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PakEmeraldPrimary
                        ),
                        modifier = Modifier.size(height = 24.dp, width = 44.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (doctor.isAvailable) "On Duty" else "On Leave",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (doctor.isAvailable) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Doctor", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Doctor", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }

                    Button(
                        onClick = onBookAppointment,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Book", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditDoctorDialog(
    doctor: DoctorEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, specialization: String, qualification: String, fee: Double, days: String, hours: String, phone: String, isAvailable: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(doctor?.name ?: "") }
    var specialization by remember { mutableStateOf(doctor?.specialization ?: "General Physician") }
    var qualification by remember { mutableStateOf(doctor?.qualification ?: "MBBS, FCPS") }
    var feeText by remember { mutableStateOf(doctor?.consultationFeePkr?.toInt()?.toString() ?: "1500") }
    var days by remember { mutableStateOf(doctor?.availableDays ?: "Mon - Sat") }
    var hours by remember { mutableStateOf(doctor?.availableHours ?: "05:00 PM - 09:00 PM") }
    var phone by remember { mutableStateOf(doctor?.phone ?: "") }
    var isAvailable by remember { mutableStateOf(doctor?.isAvailable ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonSpecialties = listOf(
        "General Physician",
        "Cardiologist",
        "Gynecologist & Obstetrician",
        "Pediatrician (Child Specialist)",
        "Orthopedic Surgeon",
        "Dermatologist (Skin)",
        "ENT Specialist",
        "Dentist",
        "Neurologist",
        "Psychiatrist"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (doctor == null) "Add New Doctor" else "Edit Doctor Details",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Doctor Name *") },
                    placeholder = { Text("e.g. Dr. Muhammad Tariq") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Specialization *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonSpecialties) { spec ->
                        val isSel = specialization == spec
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                            modifier = Modifier.clickable { specialization = spec }
                        ) {
                            Text(
                                text = spec,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qualification,
                        onValueChange = { qualification = it },
                        label = { Text("Qualifications") },
                        placeholder = { Text("MBBS, FCPS") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Fee (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Available Days *") },
                    placeholder = { Text("e.g. Mon, Wed, Fri or Daily") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Consultation Hours *") },
                    placeholder = { Text("e.g. 05:00 PM - 09:00 PM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Doctor Contact Phone") },
                    placeholder = { Text("0300-1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
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
                    if (name.isBlank()) {
                        errorMessage = "Please enter doctor's name"
                        return@Button
                    }
                    val fee = feeText.toDoubleOrNull()
                    if (fee == null || fee < 0) {
                        errorMessage = "Please enter valid consultation fee"
                        return@Button
                    }
                    onConfirm(
                        name.trim(),
                        specialization.trim(),
                        qualification.trim(),
                        fee,
                        days.trim(),
                        hours.trim(),
                        phone.trim(),
                        isAvailable
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text(if (doctor == null) "Add Doctor" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
