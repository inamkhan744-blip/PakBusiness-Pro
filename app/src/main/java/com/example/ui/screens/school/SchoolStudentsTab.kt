package com.example.ui.screens.school

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.SchoolClassEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolStudentsTab(
    activeBusiness: BusinessEntity?,
    students: List<StudentEntity>,
    classes: List<SchoolClassEntity>,
    onSaveStudent: (StudentEntity) -> Unit,
    onDeleteStudent: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("All") }

    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }

    // Distinct class list for filter
    val classOptions = remember(classes, students) {
        val set = mutableSetOf("All")
        classes.forEach { set.add("${it.className} (${it.section})") }
        students.forEach { set.add("${it.className} (${it.section})") }
        set.toList()
    }

    // Filtered students
    val filteredStudents = remember(students, searchQuery, selectedClassFilter) {
        students.filter { student ->
            val matchesQuery = searchQuery.isBlank() ||
                student.name.contains(searchQuery, ignoreCase = true) ||
                student.rollNo.contains(searchQuery, ignoreCase = true) ||
                student.fatherName.contains(searchQuery, ignoreCase = true) ||
                student.phone.contains(searchQuery, ignoreCase = true)

            val classKey = "${student.className} (${student.section})"
            val matchesClass = selectedClassFilter == "All" || classKey == selectedClassFilter

            matchesQuery && matchesClass
        }
    }

    val totalMonthlyFee = remember(students) {
        students.filter { it.isActive }.sumOf { it.monthlyFee }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Enrolled Students",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${students.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Monthly Potential",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF00381F)
                        )
                        Text(
                            text = "Rs ${String.format(Locale.US, "%,.0f", totalMonthlyFee)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00381F)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("student_search_input"),
                placeholder = { Text("Search by Student, Roll #, Father...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Class Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classOptions) { opt ->
                    FilterChip(
                        selected = selectedClassFilter == opt,
                        onClick = { selectedClassFilter = opt },
                        label = { Text(opt, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (students.isEmpty()) "No students registered yet" else "No matching students found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                studentToEdit = null
                                showAddEditDialog = true
                            },
                            modifier = Modifier.testTag("add_first_student_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Student")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentItemCard(
                            student = student,
                            onEdit = {
                                studentToEdit = student
                                showAddEditDialog = true
                            },
                            onDelete = { studentToDelete = student },
                            onCall = {
                                if (student.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone}"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Student
        FloatingActionButton(
            onClick = {
                studentToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_student_fab"),
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Student")
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditStudentDialog(
            student = studentToEdit,
            classes = classes,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { savedStudent ->
                onSaveStudent(savedStudent)
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to remove ${student.name} (Roll #${student.rollNo})? This will not delete past fee vouchers.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(student.id)
                        studentToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.rollNo}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll No Badge Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PakEmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.rollNo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF00381F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${student.className} • ${student.section}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "S/O / D/O: ${student.fatherName.ifBlank { "Not specified" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Fee: Rs ${String.format(Locale.US, "%,.0f", student.monthlyFee)}/mo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )

                    if (student.phone.isNotBlank()) {
                        Text(
                            text = "• ${student.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (student.phone.isNotBlank()) {
                    IconButton(onClick = onCall, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call Parent",
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Student",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Student",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditStudentDialog(
    student: StudentEntity?,
    classes: List<SchoolClassEntity>,
    businessId: Long,
    onDismiss: () -> Unit,
    onConfirm: (StudentEntity) -> Unit
) {
    var rollNo by remember { mutableStateOf(student?.rollNo ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var className by remember { mutableStateOf(student?.className ?: (classes.firstOrNull()?.className ?: "Class 9")) }
    var section by remember { mutableStateOf(student?.section ?: (classes.firstOrNull()?.section ?: "A")) }
    var monthlyFeeStr by remember { mutableStateOf(student?.monthlyFee?.toInt()?.toString() ?: "3500") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var gender by remember { mutableStateOf(student?.gender ?: "Male") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    var rollNoError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var fatherError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (student == null) "Register New Student" else "Edit Student Record",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rollNo,
                            onValueChange = {
                                rollNo = it
                                rollNoError = false
                            },
                            label = { Text("Roll No *") },
                            modifier = Modifier.weight(1f).testTag("dialog_student_roll"),
                            isError = rollNoError,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = monthlyFeeStr,
                            onValueChange = { monthlyFeeStr = it },
                            label = { Text("Fee (PKR) *") },
                            modifier = Modifier.weight(1.2f).testTag("dialog_student_fee"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text("Student Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_student_name"),
                        isError = nameError,
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = {
                            fatherName = it
                            fatherError = false
                        },
                        label = { Text("Father / Guardian Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_student_father"),
                        isError = fatherError,
                        singleLine = true
                    )
                }

                item {
                    Text(text = "Assign Class & Section", style = MaterialTheme.typography.labelMedium)
                    if (classes.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            items(classes) { c ->
                                val isSelected = className == c.className && section == c.section
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        className = c.className
                                        section = c.section
                                        if (student == null && c.defaultMonthlyFee > 0) {
                                            monthlyFeeStr = c.defaultMonthlyFee.toInt().toString()
                                        }
                                    },
                                    label = { Text("${c.className} (${c.section})", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Class") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text("Section") },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Father Phone (e.g. 0300-1234567)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_student_phone"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gender:", style = MaterialTheme.typography.bodyMedium)
                        listOf("Male", "Female").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Home Address (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rollNo.isBlank()) rollNoError = true
                    if (name.isBlank()) nameError = true
                    if (fatherName.isBlank()) fatherError = true

                    if (rollNo.isNotBlank() && name.isNotBlank() && fatherName.isNotBlank()) {
                        val parsedFee = monthlyFeeStr.toDoubleOrNull() ?: 3000.0
                        val entity = (student ?: StudentEntity(
                            businessId = businessId,
                            rollNo = rollNo.trim(),
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            className = className.trim().ifBlank { "Class 9" },
                            section = section.trim().ifBlank { "A" },
                            monthlyFee = parsedFee,
                            phone = phone.trim(),
                            address = address.trim(),
                            gender = gender,
                            notes = notes.trim()
                        )).copy(
                            rollNo = rollNo.trim(),
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            className = className.trim().ifBlank { "Class 9" },
                            section = section.trim().ifBlank { "A" },
                            monthlyFee = parsedFee,
                            phone = phone.trim(),
                            address = address.trim(),
                            gender = gender,
                            notes = notes.trim()
                        )
                        onConfirm(entity)
                    }
                },
                modifier = Modifier.testTag("save_student_confirm_button")
            ) {
                Text("Save Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
