package com.example.ui.screens.restaurant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RestaurantTableEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TableGridView(
    tables: List<RestaurantTableEntity>,
    onSelectTableForOrder: (RestaurantTableEntity) -> Unit,
    onSaveTable: (name: String, capacity: Int, section: String, id: Long) -> Unit,
    onDeleteTable: (Long) -> Unit,
    onReserveTable: (tableId: Long, customerName: String, phone: String) -> Unit,
    onClearReservation: (Long) -> Unit,
    onSettleTableBill: (RestaurantTableEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSectionFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var tableToEdit by remember { mutableStateOf<RestaurantTableEntity?>(null) }

    var showReservationDialog by remember { mutableStateOf(false) }
    var tableToReserve by remember { mutableStateOf<RestaurantTableEntity?>(null) }

    val sections = listOf("All") + tables.map { it.section }.distinct()

    val filteredTables = tables.filter { table ->
        val matchSection = selectedSectionFilter == "All" || table.section == selectedSectionFilter
        val matchStatus = selectedStatusFilter == "All" || table.status.equals(selectedStatusFilter, ignoreCase = true)
        matchSection && matchStatus
    }

    val availableCount = tables.count { it.status == "AVAILABLE" }
    val occupiedCount = tables.count { it.status == "OCCUPIED" }
    val reservedCount = tables.count { it.status == "RESERVED" }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // KPI Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TableKpiBadge(
                    count = availableCount,
                    label = "Available",
                    color = Color(0xFF2E7D32),
                    containerColor = Color(0xFFE8F5E9),
                    isSelected = selectedStatusFilter == "AVAILABLE",
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == "AVAILABLE") "All" else "AVAILABLE"
                    },
                    modifier = Modifier.weight(1f)
                )
                TableKpiBadge(
                    count = occupiedCount,
                    label = "Occupied",
                    color = Color(0xFFC62828),
                    containerColor = Color(0xFFFFEBEE),
                    isSelected = selectedStatusFilter == "OCCUPIED",
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == "OCCUPIED") "All" else "OCCUPIED"
                    },
                    modifier = Modifier.weight(1f)
                )
                TableKpiBadge(
                    count = reservedCount,
                    label = "Reserved",
                    color = Color(0xFF6A1B9A),
                    containerColor = Color(0xFFF3E5F5),
                    isSelected = selectedStatusFilter == "RESERVED",
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == "RESERVED") "All" else "RESERVED"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section Filter Chips
            if (sections.size > 2) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sections.forEach { section ->
                        val isSelected = selectedSectionFilter == section
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSectionFilter = section },
                            label = { Text(section, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                }
            }

            if (filteredTables.isEmpty()) {
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
                            imageVector = Icons.Default.TableBar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No tables match current filters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        OutlinedButton(
                            onClick = {
                                selectedSectionFilter = "All"
                                selectedStatusFilter = "All"
                            }
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("table_grid_list"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTables, key = { it.id }) { table ->
                        TableGridItemCard(
                            table = table,
                            onTakeOrder = { onSelectTableForOrder(table) },
                            onReserve = {
                                tableToReserve = table
                                showReservationDialog = true
                            },
                            onClearReservation = { onClearReservation(table.id) },
                            onSettleBill = { onSettleTableBill(table) },
                            onEdit = {
                                tableToEdit = table
                                showAddEditDialog = true
                            },
                            onDelete = { onDeleteTable(table.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Table
        FloatingActionButton(
            onClick = {
                tableToEdit = null
                showAddEditDialog = true
            },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_table")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Table")
                Text("Add Table", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Table Dialog
    if (showAddEditDialog) {
        AddEditTableDialog(
            table = tableToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, capacity, section ->
                onSaveTable(name, capacity, section, tableToEdit?.id ?: 0L)
                showAddEditDialog = false
            }
        )
    }

    // Reservation Dialog
    if (showReservationDialog && tableToReserve != null) {
        ReserveTableDialog(
            table = tableToReserve!!,
            onDismiss = { showReservationDialog = false },
            onConfirm = { name, phone ->
                onReserveTable(tableToReserve!!.id, name, phone)
                showReservationDialog = false
            }
        )
    }
}

@Composable
fun TableKpiBadge(
    count: Int,
    label: String,
    color: Color,
    containerColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TableGridItemCard(
    table: RestaurantTableEntity,
    onTakeOrder: () -> Unit,
    onReserve: () -> Unit,
    onClearReservation: () -> Unit,
    onSettleBill: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val (statusColor, containerBg, statusLabel) = when (table.status) {
        "OCCUPIED" -> Triple(Color(0xFFC62828), Color(0xFFFFEBEE), "OCCUPIED")
        "RESERVED" -> Triple(Color(0xFF6A1B9A), Color(0xFFF3E5F5), "RESERVED")
        else -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "AVAILABLE")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_card_${table.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Table Name & Context Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = table.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Table") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Table", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Section & Capacity Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = table.section,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${table.capacity} Seats",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = containerBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    if (table.status == "OCCUPIED" && table.occupiedSince != null) {
                        val elapsedMins = (System.currentTimeMillis() - table.occupiedSince) / (60 * 1000)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${elapsedMins}m ago",
                            fontSize = 10.sp,
                            color = statusColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Status-specific info
            when (table.status) {
                "RESERVED" -> {
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "For: ${table.reservedFor ?: "Guest"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!table.reservedPhone.isNullOrBlank()) {
                            Text(
                                text = table.reservedPhone,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                "OCCUPIED" -> {
                    Text(
                        text = "Dining in progress",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "Ready for guests",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            when (table.status) {
                "AVAILABLE" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onTakeOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_take_order_${table.id}")
                        ) {
                            Text("Take Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onReserve,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Reserve", fontSize = 11.sp)
                        }
                    }
                }
                "OCCUPIED" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onTakeOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("btn_manage_order_${table.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Manage Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = onSettleBill,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("btn_settle_bill_${table.id}")
                        ) {
                            Text("Settle & Clear", fontSize = 10.sp, color = PakEmeraldPrimary)
                        }
                    }
                }
                "RESERVED" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onTakeOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text("Seat Guests", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onClearReservation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        ) {
                            Text("Cancel Reservation", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditTableDialog(
    table: RestaurantTableEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, capacity: Int, section: String) -> Unit
) {
    var name by remember { mutableStateOf(table?.name ?: "") }
    var capacityText by remember { mutableStateOf(table?.capacity?.toString() ?: "4") }
    var section by remember { mutableStateOf(table?.section ?: "Main Dining") }

    val standardSections = listOf("Main Dining", "Family Hall", "Outdoor Terrace", "Rooftop", "VIP Room")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (table == null) "Add New Table" else "Edit Table", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Table Name / Number *") },
                    placeholder = { Text("e.g. Table 5, VIP Corner") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_table_name")
                )

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text("Capacity (Seats)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Section / Area",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standardSections.take(3).forEach { sec ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (section == sec) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (section == sec) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { section = sec }
                        ) {
                            Text(
                                text = sec,
                                fontSize = 10.sp,
                                fontWeight = if (section == sec) FontWeight.Bold else FontWeight.Normal,
                                color = if (section == sec) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 4
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), cap, section)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_table")
            ) {
                Text("Save Table")
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
fun ReserveTableDialog(
    table: RestaurantTableEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reserve ${table.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Area: ${table.section} • Capacity: ${table.capacity} Persons",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Guest Name *") },
                    placeholder = { Text("e.g. Tariq Mahmood") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("e.g. +92 300 1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank()) {
                        onConfirm(customerName.trim(), phone.trim())
                    }
                },
                enabled = customerName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text("Confirm Reservation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
