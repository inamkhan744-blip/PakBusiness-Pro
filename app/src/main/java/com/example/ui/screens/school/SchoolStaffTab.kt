package com.example.ui.screens.school

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.SchoolStaffEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

@Composable
fun SchoolStaffTab(
    activeBusiness: BusinessEntity?,
    staffList: List<SchoolStaffEntity>,
    onSaveStaff: (SchoolStaffEntity) -> Unit,
    onDeleteStaff: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<SchoolStaffEntity?>(null) }
    var staffToDelete by remember { mutableStateOf<SchoolStaffEntity?>(null) }

    val filteredStaff = remember(staffList, searchQuery) {
        if (searchQuery.isBlank()) {
            staffList
        } else {
            staffList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.designation.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("school_staff_tab")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Teachers & School Staff",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PakEmeraldDark
                        )
                        Text(
                            text = "${staffList.size} Active Staff Members",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakEmeraldPrimary
                    ) {
                        Text(
                            text = "Monthly Payroll: ₨ ${"%,.0f".format(staffList.sumOf { it.monthlySalaryPkr })}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search teachers by name, subject, designation...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PakEmeraldPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_staff_search_input")
            )

            // Staff List
            if (filteredStaff.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "No staff records found. Add your first teacher!" else "No teachers match '$searchQuery'",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStaff, key = { it.id }) { staff ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("school_staff_item_${staff.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(PakEmeraldPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = staff.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = staff.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PakEmeraldContainer.copy(alpha = 0.7f)
                                            ) {
                                                Text(
                                                    text = staff.designation,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PakEmeraldPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (staff.subject.isNotBlank()) {
                                                Text(
                                                    text = "• ${staff.subject}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (staff.phone.isNotBlank()) {
                                                Text(
                                                    text = "Ph: ${staff.phone}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "Salary: ₨ ${"%,.0f".format(staff.monthlySalaryPkr)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PakEmeraldPrimary
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            staffToEdit = staff
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { staffToDelete = staff },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to Add Teacher
        FloatingActionButton(
            onClick = {
                staffToEdit = null
                showAddEditDialog = true
            },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("btn_add_school_staff")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Teacher / Staff")
        }
    }

    // Add / Edit Staff Dialog
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(staffToEdit?.name ?: "") }
        var designation by remember { mutableStateOf(staffToEdit?.designation ?: "Senior Teacher") }
        var subject by remember { mutableStateOf(staffToEdit?.subject ?: "Mathematics") }
        var phone by remember { mutableStateOf(staffToEdit?.phone ?: "") }
        var salaryText by remember { mutableStateOf(staffToEdit?.let { "%.0f".format(it.monthlySalaryPkr) } ?: "30000") }
        var qualification by remember { mutableStateOf(staffToEdit?.qualification ?: "M.Sc / B.Ed") }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (staffToEdit == null) "Add Teacher / Staff" else "Edit Staff Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Teacher Name *") },
                        placeholder = { Text("e.g. Sir Aslam Khan") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_name")
                    )

                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation *") },
                        placeholder = { Text("e.g. Principal, Senior Teacher, Qari, Clerk") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_designation")
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Department") },
                        placeholder = { Text("e.g. Mathematics, Science, Urdu, Admin") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_subject")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+92 300 1234567") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_phone")
                    )

                    OutlinedTextField(
                        value = salaryText,
                        onValueChange = { salaryText = it },
                        label = { Text("Monthly Salary (PKR)") },
                        placeholder = { Text("35000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_salary")
                    )

                    OutlinedTextField(
                        value = qualification,
                        onValueChange = { qualification = it },
                        label = { Text("Qualification") },
                        placeholder = { Text("B.Sc, M.Ed, Hafiz-e-Quran") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_qualification")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val salary = salaryText.toDoubleOrNull() ?: 30000.0
                            val newStaff = SchoolStaffEntity(
                                id = staffToEdit?.id ?: 0L,
                                businessId = activeBusiness?.id ?: 0L,
                                name = name.trim(),
                                designation = designation.trim(),
                                subject = subject.trim(),
                                phone = phone.trim(),
                                monthlySalaryPkr = salary,
                                qualification = qualification.trim()
                            )
                            onSaveStaff(newStaff)
                            showAddEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier.testTag("btn_save_staff_confirm")
                ) {
                    Text(if (staffToEdit == null) "Add Staff" else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (staffToDelete != null) {
        AlertDialog(
            onDismissRequest = { staffToDelete = null },
            title = { Text("Delete Staff Record") },
            text = { Text("Are you sure you want to remove '${staffToDelete?.name}' from school staff? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        staffToDelete?.id?.let { onDeleteStaff(it) }
                        staffToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { staffToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
