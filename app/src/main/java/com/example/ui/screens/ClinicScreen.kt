package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PatientEntity
import com.example.data.PrescriptionEntity
import com.example.ui.ClinicViewModel
import com.example.ui.components.DailySalesClinicAppointmentsVisualization
import com.example.util.PdfExportUtil
import com.example.util.PrescribedMedicineItem
import com.example.util.PrescriptionExportData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicScreen(
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.filteredPatients.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var targetPatientForRx by remember { mutableStateOf<PatientEntity?>(null) }

    val tabs = listOf("Patients & OPD", "Prescriptions (Rx)", "Daily Summary & Sales (PKR)")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("clinic_screen"),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddPatientDialog = true },
                    containerColor = Color(0xFF12594A)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Patient", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Clinic & Doctor Consultation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("OPD queue, digital Rx prescriptions & medical receipts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF12594A), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // Patients List
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search patient by MR #, name, phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(patients) { p ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${p.mrNumber} • ${p.age} Yrs • ${p.gender} • ${p.phone}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = { targetPatientForRx = p },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF12594A))
                                    ) {
                                        Text("Write Rx", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Prescriptions List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prescriptions) { rx ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rx.patientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Rx #${rx.rxNumber} • Fee: PKR ${rx.consultationFeePkr.toInt()}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Button(
                                            onClick = {
                                                val rxData = PrescriptionExportData(
                                                    rxNumber = rx.rxNumber,
                                                    patientName = rx.patientName,
                                                    bloodPressure = rx.bp,
                                                    pulse = rx.pulse,
                                                    temperature = rx.temp,
                                                    weight = rx.weight,
                                                    diagnosis = rx.diagnosis,
                                                    advice = rx.advice,
                                                    medicines = rx.medicinesRaw.split("\n").filter { it.isNotBlank() }.map {
                                                        PrescribedMedicineItem(name = it)
                                                    }
                                                )
                                                PdfExportUtil.printPrescription(context, rxData)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Print Rx", fontSize = 11.sp)
                                        }
                                    }
                                    if (rx.diagnosis.isNotBlank()) {
                                        Text("Diagnosis: ${rx.diagnosis}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Daily Summary of Sales and Clinic Appointments in PKR
                    DailySalesClinicAppointmentsVisualization(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Add Patient Dialog
    if (showAddPatientDialog) {
        AddPatientDialog(
            onDismiss = { showAddPatientDialog = false },
            onSave = { name, age, gender, phone, blood ->
                viewModel.registerPatient(name, age, gender, phone, blood)
                showAddPatientDialog = false
            }
        )
    }

    // Write Rx Dialog
    targetPatientForRx?.let { p ->
        WritePrescriptionDialog(
            patient = p,
            onDismiss = { targetPatientForRx = null },
            onSaveAndPrint = { bp, pulse, temp, weight, diag, meds, advice, fee, shouldPrint ->
                viewModel.savePrescription(
                    patientId = p.id,
                    patientName = p.name,
                    bp = bp,
                    pulse = pulse,
                    temp = temp,
                    weight = weight,
                    diagnosis = diag,
                    medicines = meds,
                    advice = advice,
                    fee = fee
                )
                if (shouldPrint) {
                    val rxData = PrescriptionExportData(
                        patientName = p.name,
                        patientAge = p.age.toString(),
                        patientGender = p.gender,
                        patientPhone = p.phone,
                        bloodPressure = bp,
                        pulse = pulse,
                        temperature = temp,
                        weight = weight,
                        diagnosis = diag,
                        advice = advice,
                        medicines = meds.split("\n").filter { it.isNotBlank() }.map {
                            PrescribedMedicineItem(name = it)
                        }
                    )
                    PdfExportUtil.printPrescription(context, rxData)
                }
                targetPatientForRx = null
            }
        )
    }
}

@Composable
private fun AddPatientDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Register New Patient", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Patient Full Name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("Blood Group") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female").forEach { g ->
                        FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                    }
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(onClick = { if (name.isNotBlank()) onSave(name, age, gender, phone, blood) }) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

@Composable
private fun WritePrescriptionDialog(
    patient: PatientEntity,
    onDismiss: () -> Unit,
    onSaveAndPrint: (String, String, String, String, String, String, String, Double, Boolean) -> Unit
) {
    var bp by remember { mutableStateOf("120/80") }
    var pulse by remember { mutableStateOf("72") }
    var temp by remember { mutableStateOf("98.6") }
    var weight by remember { mutableStateOf("") }
    var diag by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf("Tab. Paracetamol 500mg (1-0-1)\nCap. Amoxicillin 500mg (1-0-1)") }
    var advice by remember { mutableStateOf("Take plenty of fluids and rest.") }
    var fee by remember { mutableStateOf("1000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("Prescription for ${patient.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = bp, onValueChange = { bp = it }, label = { Text("BP") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pulse, onValueChange = { pulse = it }, label = { Text("Pulse") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = temp, onValueChange = { temp = it }, label = { Text("Temp") }, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = diag, onValueChange = { diag = it }, label = { Text("Clinical Diagnosis") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = meds, onValueChange = { meds = it }, label = { Text("Medicines (Line by line)") }, minLines = 3, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = advice, onValueChange = { advice = it }, label = { Text("Doctor's Advice") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("Consultation Fee (PKR)") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(onClick = { onSaveAndPrint(bp, pulse, temp, weight, diag, meds, advice, fee.toDoubleOrNull() ?: 1000.0, true) }) {
                            Text("Save & Print Rx")
                        }
                    }
                }
            }
        }
    }
}
