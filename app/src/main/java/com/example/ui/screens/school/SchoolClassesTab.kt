package com.example.ui.screens.school

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.SchoolClassEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import java.util.Locale

@Composable
fun SchoolClassesTab(
    activeBusiness: BusinessEntity?,
    classes: List<SchoolClassEntity>,
    students: List<StudentEntity>,
    onSaveClass: (SchoolClassEntity) -> Unit,
    onDeleteClass: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var classToEdit by remember { mutableStateOf<SchoolClassEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<SchoolClassEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Total Batches / Classes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${classes.size}",
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
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Total Enrolled",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF00381F)
                        )
                        Text(
                            text = "${students.size} Students",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00381F)
                        )
                    }
                }
            }

            if (classes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No classes or sections configured yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                classToEdit = null
                                showAddEditDialog = true
                            },
                            modifier = Modifier.testTag("add_first_class_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Class / Section")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(classes, key = { it.id }) { schoolClass ->
                        val enrolledCount = students.count {
                            it.className.equals(schoolClass.className, ignoreCase = true) &&
                                it.section.equals(schoolClass.section, ignoreCase = true)
                        }

                        ClassItemCard(
                            schoolClass = schoolClass,
                            enrolledCount = enrolledCount,
                            onEdit = {
                                classToEdit = schoolClass
                                showAddEditDialog = true
                            },
                            onDelete = { classToDelete = schoolClass }
                        )
                    }
                }
            }
        }

        // FAB to add class
        FloatingActionButton(
            onClick = {
                classToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_class_fab"),
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Class")
        }
    }

    if (showAddEditDialog) {
        AddEditClassDialog(
            schoolClass = classToEdit,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = { showAddEditDialog = false },
            onConfirm = {
                onSaveClass(it)
                showAddEditDialog = false
            }
        )
    }

    classToDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete ${c.className} (Section ${c.section})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(c.id)
                        classToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClassItemCard(
    schoolClass: SchoolClassEntity,
    enrolledCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("class_card_${schoolClass.className}_${schoolClass.section}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Class,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schoolClass.className,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Section: ${schoolClass.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Class",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Class",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Students count
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$enrolledCount / ${schoolClass.maxCapacity}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Room number
                if (schoolClass.roomNumber.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = schoolClass.roomNumber,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Default Fee
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PakEmeraldContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Standard Fee",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color(0xFF00381F)
                        )
                        Text(
                            text = "Rs ${schoolClass.defaultMonthlyFee.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00381F)
                        )
                    }
                }
            }

            if (schoolClass.classTeacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Class Incharge: ${schoolClass.classTeacher}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditClassDialog(
    schoolClass: SchoolClassEntity?,
    businessId: Long,
    onDismiss: () -> Unit,
    onConfirm: (SchoolClassEntity) -> Unit
) {
    var className by remember { mutableStateOf(schoolClass?.className ?: "") }
    var section by remember { mutableStateOf(schoolClass?.section ?: "A") }
    var roomNumber by remember { mutableStateOf(schoolClass?.roomNumber ?: "") }
    var classTeacher by remember { mutableStateOf(schoolClass?.classTeacher ?: "") }
    var defaultFeeStr by remember { mutableStateOf(schoolClass?.defaultMonthlyFee?.toInt()?.toString() ?: "3500") }
    var maxCapacityStr by remember { mutableStateOf(schoolClass?.maxCapacity?.toString() ?: "40") }

    var classNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (schoolClass == null) "Create Class & Section" else "Edit Class Details",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        classNameError = false
                    },
                    label = { Text("Class Name * (e.g. Class 9, 1st Year, Matric)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_class_name"),
                    isError = classNameError,
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section (e.g. A, B, Blue)") },
                        modifier = Modifier.weight(1f).testTag("dialog_class_section"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room / Hall") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = classTeacher,
                    onValueChange = { classTeacher = it },
                    label = { Text("Class Teacher / Incharge") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = defaultFeeStr,
                        onValueChange = { defaultFeeStr = it },
                        label = { Text("Default Fee (PKR)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = maxCapacityStr,
                        onValueChange = { maxCapacityStr = it },
                        label = { Text("Capacity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (className.isBlank()) {
                        classNameError = true
                    } else {
                        val fee = defaultFeeStr.toDoubleOrNull() ?: 3500.0
                        val cap = maxCapacityStr.toIntOrNull() ?: 40
                        val entity = (schoolClass ?: SchoolClassEntity(
                            businessId = businessId,
                            className = className.trim(),
                            section = section.trim().ifBlank { "A" },
                            roomNumber = roomNumber.trim(),
                            classTeacher = classTeacher.trim(),
                            defaultMonthlyFee = fee,
                            maxCapacity = cap
                        )).copy(
                            className = className.trim(),
                            section = section.trim().ifBlank { "A" },
                            roomNumber = roomNumber.trim(),
                            classTeacher = classTeacher.trim(),
                            defaultMonthlyFee = fee,
                            maxCapacity = cap
                        )
                        onConfirm(entity)
                    }
                },
                modifier = Modifier.testTag("save_class_confirm_button")
            ) {
                Text("Save Class")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
