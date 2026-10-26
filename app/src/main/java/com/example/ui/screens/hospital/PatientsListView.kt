package com.example.ui.screens.hospital

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PatientEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary

@Composable
fun PatientsListView(
    patients: List<PatientEntity>,
    onSavePatient: (name: String, phone: String, age: Int, gender: String, bloodGroup: String, address: String, history: String, id: Long) -> Unit,
    onDeletePatient: (id: Long) -> Unit,
    onBookAppointmentForPatient: (patient: PatientEntity) -> Unit,
    onWritePrescriptionForPatient: (patient: PatientEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var viewingPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var patientToDelete by remember { mutableStateOf<PatientEntity?>(null) }

    val filteredPatients = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients
        else patients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.medicalHistory.contains(searchQuery, ignoreCase = true) ||
                it.bloodGroup.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Patients (Name, Phone, Medical History)") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PakEmeraldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PakEmeraldPrimary,
                    focusedLabelColor = PakEmeraldPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_patient_search")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Patients count & summary banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredPatients.size} Registered Patients",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap a patient for actions",
                    fontSize = 11.sp,
                    color = PakEmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredPatients.isEmpty()) {
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
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No patients registered yet" else "No matching patients found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Tap + to register a new clinic patient" else "Try searching with a different name or phone number",
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
                    items(filteredPatients, key = { it.id }) { patient ->
                        PatientCard(
                            patient = patient,
                            onCardClick = { viewingPatient = patient },
                            onEditClick = { editingPatient = patient },
                            onDeleteClick = { patientToDelete = patient },
                            onBookAppointment = { onBookAppointmentForPatient(patient) },
                            onWritePrescription = { onWritePrescriptionForPatient(patient) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // FAB to Add Patient
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("btn_add_patient")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Patient", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // Dialog: Add / Edit Patient
    if (showAddDialog || editingPatient != null) {
        val target = editingPatient
        AddEditPatientDialog(
            patient = target,
            onDismiss = {
                showAddDialog = false
                editingPatient = null
            },
            onConfirm = { name, phone, age, gender, bloodGroup, address, history ->
                onSavePatient(name, phone, age, gender, bloodGroup, address, history, target?.id ?: 0L)
                showAddDialog = false
                editingPatient = null
            }
        )
    }

    // Dialog: View Patient Full Profile & Actions
    viewingPatient?.let { patient ->
        PatientDetailsDialog(
            patient = patient,
            onDismiss = { viewingPatient = null },
            onEdit = {
                viewingPatient = null
                editingPatient = patient
            },
            onBookAppointment = {
                viewingPatient = null
                onBookAppointmentForPatient(patient)
            },
            onWritePrescription = {
                viewingPatient = null
                onWritePrescriptionForPatient(patient)
            }
        )
    }

    // Dialog: Delete Confirmation
    patientToDelete?.let { patient ->
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            title = { Text("Delete Patient Record?") },
            text = { Text("Are you sure you want to delete ${patient.name}? All associated prescriptions and appointments will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePatient(patient.id)
                        patientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { patientToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PatientCard(
    patient: PatientEntity,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBookAppointment: () -> Unit,
    onWritePrescription: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                val avatarBg = if (patient.gender == "Female") Color(0xFFFCE7F3) else Color(0xFFE0F2FE)
                val avatarTint = if (patient.gender == "Female") Color(0xFFDB2777) else Color(0xFF0284C7)
                Surface(
                    shape = CircleShape,
                    color = avatarBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = patient.name.take(2).uppercase(),
                            color = avatarTint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = patient.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (patient.bloodGroup.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = patient.bloodGroup,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${patient.age} Yrs • ${patient.gender} • ${patient.phone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Patient",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete Patient",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (patient.medicalHistory.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "History: ${patient.medicalHistory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBookAppointment,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Appointment", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onWritePrescription,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rx Prescribe", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AddEditPatientDialog(
    patient: PatientEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, age: Int, gender: String, bloodGroup: String, address: String, history: String) -> Unit
) {
    var name by remember { mutableStateOf(patient?.name ?: "") }
    var phone by remember { mutableStateOf(patient?.phone ?: "") }
    var ageText by remember { mutableStateOf(patient?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(patient?.gender ?: "Male") }
    var bloodGroup by remember { mutableStateOf(patient?.bloodGroup ?: "B+") }
    var address by remember { mutableStateOf(patient?.address ?: "") }
    var history by remember { mutableStateOf(patient?.medicalHistory ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bloodGroups = listOf("A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-", "Unknown")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (patient == null) "Register New Patient" else "Edit Patient Profile",
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
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("0300-1234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age *") },
                        placeholder = { Text("30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Gender Selector Chips
                Text("Gender", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { g ->
                        val isSel = gender == g
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gender = g }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Blood Group Chips
                Text("Blood Group", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bloodGroups.take(5).forEach { bg ->
                        val isSel = bloodGroup == bg
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)) else null,
                            modifier = Modifier.clickable { bloodGroup = bg }
                        ) {
                            Text(
                                text = bg,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / City") },
                    placeholder = { Text("e.g. Gulberg, Lahore") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = history,
                    onValueChange = { history = it },
                    label = { Text("Medical History / Known Allergies") },
                    placeholder = { Text("e.g. Diabetes, Penicillin allergy") },
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
                    if (name.isBlank()) {
                        errorMessage = "Please enter patient name"
                        return@Button
                    }
                    val age = ageText.toIntOrNull()
                    if (age == null || age <= 0) {
                        errorMessage = "Please enter valid age"
                        return@Button
                    }
                    onConfirm(name.trim(), phone.trim(), age, gender, bloodGroup, address.trim(), history.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text(if (patient == null) "Register" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PatientDetailsDialog(
    patient: PatientEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onBookAppointment: () -> Unit,
    onWritePrescription: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = PakEmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(patient.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Age / Gender:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${patient.age} Years • ${patient.gender}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Phone:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (patient.phone.isNotBlank()) patient.phone else "Not recorded", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Blood Group:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (patient.bloodGroup.isNotBlank()) patient.bloodGroup else "Not specified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (patient.address.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Address:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(patient.address, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (patient.medicalHistory.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Medical History & Allergies:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = patient.medicalHistory,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Button(
                    onClick = onWritePrescription,
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Write Medical Prescription")
                }

                OutlinedButton(
                    onClick = onBookAppointment,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Book Clinic Appointment")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
