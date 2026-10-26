package com.example.ui.screens.hospital

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentEntity
import com.example.data.BusinessEntity
import com.example.data.DoctorEntity
import com.example.data.PatientEntity
import com.example.data.PrescriptionEntity
import com.example.data.PrescriptionMedicineItem
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import com.example.util.PrescriptionPdfGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrescriptionStudioView(
    business: BusinessEntity?,
    prescriptions: List<PrescriptionEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    preselectedPatient: PatientEntity? = null,
    preselectedAppointment: AppointmentEntity? = null,
    onSavePrescription: (prescription: PrescriptionEntity, onSaved: (Long) -> Unit) -> Unit,
    onDeletePrescription: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isWritingMode by remember { mutableStateOf(preselectedPatient != null || preselectedAppointment != null) }
    var searchQuery by remember { mutableStateOf("") }
    var previewPrescription by remember { mutableStateOf<PrescriptionEntity?>(null) }
    var prescriptionToDelete by remember { mutableStateOf<PrescriptionEntity?>(null) }

    if (isWritingMode) {
        PrescriptionWriterScreen(
            business = business,
            patients = patients,
            doctors = doctors,
            preselectedPatient = preselectedPatient,
            preselectedAppointment = preselectedAppointment,
            onBack = { isWritingMode = false },
            onSaveAndExport = { rx, exportPdf ->
                onSavePrescription(rx) { savedId ->
                    isWritingMode = false
                    val fullRx = rx.copy(id = savedId)
                    if (exportPdf) {
                        val pdfFile = PrescriptionPdfGenerator.generatePdf(context, business, fullRx)
                        if (pdfFile != null) {
                            PrescriptionPdfGenerator.sharePdf(context, pdfFile)
                        }
                    }
                }
            }
        )
    } else {
        // List of past prescriptions
        val filtered = remember(prescriptions, searchQuery) {
            if (searchQuery.isBlank()) prescriptions
            else prescriptions.filter {
                it.patientName.contains(searchQuery, ignoreCase = true) ||
                    it.diagnosis.contains(searchQuery, ignoreCase = true) ||
                    it.doctorName.contains(searchQuery, ignoreCase = true)
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Prescriptions (Patient, Diagnosis, Doctor)") },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filtered.size} Medical Prescriptions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Standard A4 PDF Export",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filtered.isEmpty()) {
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
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = PakEmeraldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No prescriptions found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap '+ Write Rx' to prescribe medications and export PDF",
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
                        items(filtered, key = { it.id }) { rx ->
                            PrescriptionCard(
                                prescription = rx,
                                onPreviewClick = { previewPrescription = rx },
                                onExportPdf = {
                                    val pdf = PrescriptionPdfGenerator.generatePdf(context, business, rx)
                                    if (pdf != null) {
                                        PrescriptionPdfGenerator.sharePdf(context, pdf)
                                    }
                                },
                                onDelete = { prescriptionToDelete = rx }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            // FAB to Write Prescription
            FloatingActionButton(
                onClick = { isWritingMode = true },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("btn_write_rx")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Write Rx")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Write Rx", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // Modal: PDF Preview and Export Actions
    previewPrescription?.let { rx ->
        PrescriptionPreviewDialog(
            business = business,
            prescription = rx,
            onDismiss = { previewPrescription = null },
            onSharePdf = {
                val pdf = PrescriptionPdfGenerator.generatePdf(context, business, rx)
                if (pdf != null) {
                    PrescriptionPdfGenerator.sharePdf(context, pdf)
                }
            },
            onViewPdf = {
                val pdf = PrescriptionPdfGenerator.generatePdf(context, business, rx)
                if (pdf != null) {
                    PrescriptionPdfGenerator.viewPdf(context, pdf)
                }
            }
        )
    }

    // Delete confirmation
    prescriptionToDelete?.let { rx ->
        AlertDialog(
            onDismissRequest = { prescriptionToDelete = null },
            title = { Text("Delete Prescription?") },
            text = { Text("Remove prescription #${rx.id} for ${rx.patientName} from archive?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePrescription(rx.id)
                        prescriptionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { prescriptionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PrescriptionCard(
    prescription: PrescriptionEntity,
    onPreviewClick: () -> Unit,
    onExportPdf: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(prescription.createdAt))
    val medicines = remember(prescription) { prescription.parseMedicines() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prescription_card_${prescription.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "℞",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rx #${prescription.id.toString().padStart(4, '0')}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
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

            Spacer(modifier = Modifier.height(6.dp))

            // Patient Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prescription.patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${prescription.patientAge} Yrs / ${prescription.patientGender})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${prescription.doctorName} • ${prescription.doctorSpecialization}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            if (prescription.diagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PakEmeraldContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Diagnosis: ${prescription.diagnosis}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PakEmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Medicines summary
            Text(
                text = "${medicines.size} Prescribed Medicines: " + medicines.take(3).joinToString(", ") { it.name } + if (medicines.size > 3) "..." else "",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviewClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Rx", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PrescriptionWriterScreen(
    business: BusinessEntity?,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    preselectedPatient: PatientEntity?,
    preselectedAppointment: AppointmentEntity?,
    onBack: () -> Unit,
    onSaveAndExport: (prescription: PrescriptionEntity, exportPdf: Boolean) -> Unit
) {
    var selectedDoctor by remember {
        mutableStateOf(
            if (preselectedAppointment != null) doctors.firstOrNull { it.id == preselectedAppointment.doctorId } ?: doctors.firstOrNull()
            else doctors.firstOrNull()
        )
    }

    var selectedPatient by remember {
        mutableStateOf(
            preselectedPatient ?: (if (preselectedAppointment != null) patients.firstOrNull { it.id == preselectedAppointment.patientId } else patients.firstOrNull())
        )
    }

    var customPatientName by remember { mutableStateOf(preselectedAppointment?.patientName ?: "") }
    var customPatientAge by remember { mutableStateOf("30") }
    var customPatientGender by remember { mutableStateOf("Male") }
    var customPatientPhone by remember { mutableStateOf(preselectedAppointment?.patientPhone ?: "") }

    // Vitals
    var bp by remember { mutableStateOf("120/80") }
    var pulse by remember { mutableStateOf("72 bpm") }
    var temp by remember { mutableStateOf("98.6 °F") }
    var weight by remember { mutableStateOf("70 kg") }

    // Symptoms & Diagnosis
    var symptoms by remember { mutableStateOf(preselectedAppointment?.symptoms ?: "") }
    var diagnosis by remember { mutableStateOf("") }

    // Medicines List
    val medicineList = remember { mutableStateOf(mutableListOf<PrescriptionMedicineItem>()) }

    // New Medicine Input Fields
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("1-0-1") }
    var medDuration by remember { mutableStateOf("5 Days") }
    var medInstructions by remember { mutableStateOf("After meal") }

    // Diagnostic tests & advice
    var labTests by remember { mutableStateOf("") }
    var advice by remember { mutableStateOf("Drink plenty of water. Adequate rest. Avoid oily & spicy foods.") }
    var followUpDays by remember { mutableIntStateOf(7) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonDiagnoses = listOf(
        "Acute Viral Fever",
        "Essential Hypertension",
        "Upper Respiratory Tract Infection (URTI)",
        "Type 2 Diabetes Mellitus",
        "Acute Gastritis & Acid Peptic Disease",
        "Gastroenteritis",
        "Allergic Rhinitis / Asthma",
        "Migraine Headache"
    )

    val popularMedicines = listOf(
        "Tab. Panadol 500mg",
        "Tab. Augmentin 625mg",
        "Cap. Omeprazole 20mg",
        "Tab. Arinac Forte",
        "Tab. Brufen 400mg",
        "Tab. Flagyl 400mg",
        "Tab. Cetirizine 10mg",
        "Tab. Amlodipine 5mg",
        "Syp. Hydryllin",
        "Tab. Leflox 500mg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text("Prescription Writing Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val patientName = selectedPatient?.name ?: customPatientName.trim()
                            if (patientName.isBlank()) {
                                errorMessage = "Please select or enter patient"
                                return@OutlinedButton
                            }
                            val doc = selectedDoctor
                            if (doc == null) {
                                errorMessage = "Please select a doctor"
                                return@OutlinedButton
                            }
                            val rx = PrescriptionEntity(
                                businessId = business?.id ?: 0L,
                                appointmentId = preselectedAppointment?.id,
                                patientId = selectedPatient?.id ?: 0L,
                                patientName = patientName,
                                patientAge = selectedPatient?.age ?: (customPatientAge.toIntOrNull() ?: 30),
                                patientGender = selectedPatient?.gender ?: customPatientGender,
                                patientPhone = selectedPatient?.phone ?: customPatientPhone,
                                doctorId = doc.id,
                                doctorName = doc.name,
                                doctorSpecialization = doc.specialization,
                                vitalsBp = bp,
                                vitalsPulse = pulse,
                                vitalsTemp = temp,
                                vitalsWeight = weight,
                                diagnosis = diagnosis,
                                symptoms = symptoms,
                                medicinesJson = PrescriptionEntity.encodeMedicines(medicineList.value),
                                labTests = labTests,
                                advice = advice,
                                followUpDays = followUpDays
                            )
                            onSaveAndExport(rx, false)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Save", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val patientName = selectedPatient?.name ?: customPatientName.trim()
                            if (patientName.isBlank()) {
                                errorMessage = "Please select or enter patient"
                                return@Button
                            }
                            val doc = selectedDoctor
                            if (doc == null) {
                                errorMessage = "Please select a doctor"
                                return@Button
                            }
                            val rx = PrescriptionEntity(
                                businessId = business?.id ?: 0L,
                                appointmentId = preselectedAppointment?.id,
                                patientId = selectedPatient?.id ?: 0L,
                                patientName = patientName,
                                patientAge = selectedPatient?.age ?: (customPatientAge.toIntOrNull() ?: 30),
                                patientGender = selectedPatient?.gender ?: customPatientGender,
                                patientPhone = selectedPatient?.phone ?: customPatientPhone,
                                doctorId = doc.id,
                                doctorName = doc.name,
                                doctorSpecialization = doc.specialization,
                                vitalsBp = bp,
                                vitalsPulse = pulse,
                                vitalsTemp = temp,
                                vitalsWeight = weight,
                                diagnosis = diagnosis,
                                symptoms = symptoms,
                                medicinesJson = PrescriptionEntity.encodeMedicines(medicineList.value),
                                labTests = labTests,
                                advice = advice,
                                followUpDays = followUpDays
                            )
                            onSaveAndExport(rx, true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        errorMessage?.let {
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Doctor & Patient Selector
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Prescribing Doctor & Patient", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                        // Doctor Chips
                        Text("Doctor:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(doctors) { doc ->
                                val isSel = selectedDoctor?.id == doc.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                                    modifier = Modifier.clickable { selectedDoctor = doc }
                                ) {
                                    Text(
                                        text = "${doc.name} (${doc.specialization})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Patient Chips
                        Text("Patient:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (patients.isNotEmpty()) {
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
                                            text = "${p.name} (${p.age} Y, ${p.gender})",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedPatient == null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customPatientName,
                                    onValueChange = { customPatientName = it },
                                    label = { Text("Patient Name *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f)
                                )
                                OutlinedTextField(
                                    value = customPatientAge,
                                    onValueChange = { customPatientAge = it },
                                    label = { Text("Age") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Clinical Vitals
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("2. Patient Vitals", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bp,
                                onValueChange = { bp = it },
                                label = { Text("Blood Pressure") },
                                placeholder = { Text("120/80") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pulse,
                                onValueChange = { pulse = it },
                                label = { Text("Pulse") },
                                placeholder = { Text("72 bpm") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = temp,
                                onValueChange = { temp = it },
                                label = { Text("Temperature") },
                                placeholder = { Text("98.6 °F") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Weight") },
                                placeholder = { Text("70 kg") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 3: Symptoms & Diagnosis
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("3. Clinical Assessment & Diagnosis", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            label = { Text("Primary Diagnosis *") },
                            placeholder = { Text("e.g. Essential Hypertension") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Diagnosis Chips
                        Text("Common Diagnoses:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(commonDiagnoses) { diag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { diagnosis = diag }
                                ) {
                                    Text(
                                        text = diag,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = symptoms,
                            onValueChange = { symptoms = it },
                            label = { Text("Chief Complaints / Symptoms") },
                            placeholder = { Text("e.g. High grade fever with chills since 2 days") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 4: Prescribe Medicines (Rx Builder)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "℞",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary,
                                fontStyle = FontStyle.Italic
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("4. Medicines Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)
                        }

                        // Added Medicines List Table
                        if (medicineList.value.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                medicineList.value.forEachIndexed { index, med ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${index + 1}. ${med.name}",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Dosage: ${med.dosage} • Duration: ${med.duration} • ${med.instructions}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val updated = medicineList.value.toMutableList()
                                                updated.removeAt(index)
                                                medicineList.value = updated
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Medicine Subform
                        Text("Add Medicine:", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)

                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("Medicine Name & Strength") },
                            placeholder = { Text("e.g. Tab. Panadol 500mg") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Drug Suggestions
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(popularMedicines) { med ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { medName = med }
                                ) {
                                    Text(
                                        text = med,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Dosage Chips (1-0-1, etc.)
                        Text("Dosage:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("1-0-1", "1-0-0", "0-0-1", "1-1-1", "OD", "BD", "TDS", "SOS")) { d ->
                                val isSel = medDosage == d
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { medDosage = d }
                                ) {
                                    Text(
                                        text = d,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medDuration,
                                onValueChange = { medDuration = it },
                                label = { Text("Duration") },
                                placeholder = { Text("5 Days") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = medInstructions,
                                onValueChange = { medInstructions = it },
                                label = { Text("Instructions") },
                                placeholder = { Text("After meal") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Button(
                            onClick = {
                                if (medName.isNotBlank()) {
                                    val updated = medicineList.value.toMutableList()
                                    updated.add(
                                        PrescriptionMedicineItem(
                                            name = medName.trim(),
                                            dosage = medDosage.trim(),
                                            duration = medDuration.trim(),
                                            instructions = medInstructions.trim()
                                        )
                                    )
                                    medicineList.value = updated
                                    medName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Medicine to Rx")
                        }
                    }
                }
            }

            // Section 5: Lab Tests & Advice
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("5. Investigations & Special Advice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                        OutlinedTextField(
                            value = labTests,
                            onValueChange = { labTests = it },
                            label = { Text("Diagnostic Tests / Investigations") },
                            placeholder = { Text("e.g. Complete Blood Count (CBC), Urine R/E, Fasting Blood Sugar") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = advice,
                            onValueChange = { advice = it },
                            label = { Text("Special Medical Advice & Dietary Notes") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Next Follow-up:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(3, 7, 14, 30).forEach { days ->
                                    val isSel = followUpDays == days
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { followUpDays = days }
                                    ) {
                                        Text(
                                            text = "$days Days",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PrescriptionPreviewDialog(
    business: BusinessEntity?,
    prescription: PrescriptionEntity,
    onDismiss: () -> Unit,
    onSharePdf: () -> Unit,
    onViewPdf: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(prescription.createdAt))
    val medicines = remember(prescription) { prescription.parseMedicines() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PakEmeraldPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Prescription Document", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Simulation
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = (business?.name ?: "PakBusiness Healthcare Clinic").uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )
                        Text(
                            text = "${business?.address ?: "Medical Complex"} • Phone: ${business?.phone ?: "0300-0000000"}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prescribed by: ${prescription.doctorName} (${prescription.doctorSpecialization})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Date: $dateStr • Rx ID: #${prescription.id.toString().padStart(5, '0')}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Patient Info Card
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Patient: ${prescription.patientName} (${prescription.patientAge} Yrs / ${prescription.patientGender})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (prescription.patientPhone.isNotBlank()) {
                                Text("Contact: ${prescription.patientPhone}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (prescription.vitalsBp.isNotBlank() || prescription.vitalsPulse.isNotBlank()) {
                                Text(
                                    text = "Vitals: BP ${prescription.vitalsBp} | Pulse ${prescription.vitalsPulse} | Temp ${prescription.vitalsTemp}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Diagnosis
                if (prescription.diagnosis.isNotBlank()) {
                    item {
                        Text(
                            text = "Diagnosis: ${prescription.diagnosis}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )
                    }
                }

                // Medicines List
                item {
                    Text(
                        text = "℞ Prescribed Medicines:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary,
                        fontStyle = FontStyle.Italic
                    )
                }

                items(medicines) { med ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(med.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${med.dosage} • ${med.instructions}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = PakEmeraldContainer) {
                                Text(
                                    text = med.duration,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (prescription.labTests.isNotBlank()) {
                    item {
                        Text("Investigations: ${prescription.labTests}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (prescription.advice.isNotBlank()) {
                    item {
                        Text("Advice: ${prescription.advice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (prescription.followUpDays > 0) {
                    item {
                        Text("Next Follow-up: After ${prescription.followUpDays} Days", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = PakEmeraldPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSharePdf,
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
