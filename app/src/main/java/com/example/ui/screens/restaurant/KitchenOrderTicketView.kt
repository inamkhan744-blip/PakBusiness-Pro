package com.example.ui.screens.restaurant

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Print
import com.example.ui.dialogs.BluetoothPrinterSettingsDialog
import com.example.ui.dialogs.ThermalReceiptPreviewDialog
import com.example.ui.dialogs.ThermalReceiptType
import com.example.util.KotPrintItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RestaurantOrderItemEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KitchenOrderTicketView(
    kotItems: List<RestaurantOrderItemEntity>,
    onUpdateKotStatus: (itemId: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var kotTicketToPrint by remember { mutableStateOf<Pair<Long, List<RestaurantOrderItemEntity>>?>(null) }
    var showPrinterSettingsDialog by remember { mutableStateOf(false) }

    val pendingCount = kotItems.count { it.kotStatus == "PENDING" }
    val preparingCount = kotItems.count { it.kotStatus == "PREPARING" }
    val readyCount = kotItems.count { it.kotStatus == "READY" }

    val filteredItems = kotItems.filter { item ->
        when (selectedStatusFilter) {
            "PENDING" -> item.kotStatus == "PENDING"
            "PREPARING" -> item.kotStatus == "PREPARING"
            "READY" -> item.kotStatus == "READY"
            else -> true
        }
    }

    // Group items by Order ID / Table
    val groupedTickets = filteredItems.groupBy { it.orderId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Kitchen Header KPI Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KotStatusBadge(
                count = pendingCount,
                label = "New / Pending",
                color = Color(0xFFE65100),
                containerColor = Color(0xFFFFF3E0),
                isSelected = selectedStatusFilter == "PENDING",
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == "PENDING") "ALL" else "PENDING"
                },
                modifier = Modifier.weight(1f)
            )
            KotStatusBadge(
                count = preparingCount,
                label = "In Cooking",
                color = Color(0xFF1565C0),
                containerColor = Color(0xFFE3F2FD),
                isSelected = selectedStatusFilter == "PREPARING",
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == "PREPARING") "ALL" else "PREPARING"
                },
                modifier = Modifier.weight(1f)
            )
            KotStatusBadge(
                count = readyCount,
                label = "Ready to Serve",
                color = Color(0xFF2E7D32),
                containerColor = Color(0xFFE8F5E9),
                isSelected = selectedStatusFilter == "READY",
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == "READY") "ALL" else "READY"
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Filter Bar & Live Kitchen Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "PENDING", "PREPARING", "READY").forEach { filter ->
                    val isSelected = selectedStatusFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    "ALL" -> "All Active (${kotItems.size})"
                                    "PENDING" -> "Pending ($pendingCount)"
                                    "PREPARING" -> "Cooking ($preparingCount)"
                                    "READY" -> "Ready ($readyCount)"
                                    else -> filter
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PakEmeraldContainer,
                            selectedLabelColor = PakEmeraldPrimary
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PakEmeraldContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable { showPrinterSettingsDialog = true }
                    .testTag("btn_kitchen_printer_settings")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = PakEmeraldPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Printer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                }
            }
        }

        if (groupedTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Kitchen is All Clear!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No pending or cooking order tickets right now.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("kot_tickets_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedTickets.forEach { (orderId, items) ->
                    item(key = orderId) {
                        KotTicketCard(
                            orderId = orderId,
                            items = items,
                            onUpdateKotStatus = onUpdateKotStatus,
                            onPrintTicket = { kotTicketToPrint = Pair(orderId, items) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // Thermal KOT Ticket Preview & Print Dialog
        if (kotTicketToPrint != null) {
            val ticket = kotTicketToPrint!!
            val orderId = ticket.first
            val items = ticket.second
            val tableName = items.firstOrNull()?.tableName ?: "TABLE"
            val printItems = items.map {
                KotPrintItem(
                    name = it.itemName,
                    quantity = it.quantity,
                    instructions = it.instructions
                )
            }
            ThermalReceiptPreviewDialog(
                receiptType = ThermalReceiptType.KitchenKot(
                    tableName = tableName,
                    orderId = orderId,
                    items = printItems,
                    guestCount = 1,
                    serverName = "Captain / Waiter"
                ),
                onDismiss = { kotTicketToPrint = null }
            )
        }

        // Bluetooth Printer Pairing / Settings Dialog
        if (showPrinterSettingsDialog) {
            BluetoothPrinterSettingsDialog(
                onDismiss = { showPrinterSettingsDialog = false }
            )
        }
    }
}

@Composable
fun KotStatusBadge(
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
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
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
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun KotTicketCard(
    orderId: Long,
    items: List<RestaurantOrderItemEntity>,
    onUpdateKotStatus: (itemId: Long, status: String) -> Unit,
    onPrintTicket: () -> Unit
) {
    val firstItem = items.firstOrNull()
    val tableName = firstItem?.tableName ?: "Table"
    val earliestTime = items.minOfOrNull { it.kotSentTime } ?: System.currentTimeMillis()
    val elapsedMinutes = (System.currentTimeMillis() - earliestTime) / (60 * 1000)

    val isUrgent = elapsedMinutes > 20

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kot_ticket_$orderId")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Ticket Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakEmeraldDark
                    ) {
                        Text(
                            text = tableName.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "KOT #$orderId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Elapsed timer
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isUrgent) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isUrgent) Icons.Default.NotificationsActive else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (isUrgent) Color(0xFFC62828) else Color(0xFFE65100),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${elapsedMinutes}m ago",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgent) Color(0xFFC62828) else Color(0xFFE65100)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Ticket Items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    KotItemRow(
                        item = item,
                        onUpdateStatus = { nextStatus ->
                            onUpdateKotStatus(item.id, nextStatus)
                        }
                    )
                }
            }

            // Quick Batch Action for this ticket
            val allPending = items.all { it.kotStatus == "PENDING" }
            val allPreparing = items.all { it.kotStatus == "PREPARING" }
            val allReady = items.all { it.kotStatus == "READY" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrintTicket,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .padding(end = 6.dp)
                        .testTag("btn_print_kot_$orderId")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = PakEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Print KOT",
                        fontSize = 11.sp,
                        color = PakEmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (allPending) {
                    OutlinedButton(
                        onClick = {
                            items.forEach { onUpdateKotStatus(it.id, "PREPARING") }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Start Cooking All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (allPreparing) {
                    OutlinedButton(
                        onClick = {
                            items.forEach { onUpdateKotStatus(it.id, "READY") }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mark All Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (allReady) {
                    Button(
                        onClick = {
                            items.forEach { onUpdateKotStatus(it.id, "SERVED") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mark All Served ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KotItemRow(
    item: RestaurantOrderItemEntity,
    onUpdateStatus: (String) -> Unit
) {
    val (badgeColor, containerBg, statusLabel) = when (item.kotStatus) {
        "PREPARING" -> Triple(Color(0xFF1565C0), Color(0xFFE3F2FD), "COOKING")
        "READY" -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "READY")
        "SERVED" -> Triple(Color(0xFF757575), Color(0xFFEEEEEE), "SERVED")
        else -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), "PENDING")
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PakEmeraldPrimary
                    ) {
                        Text(
                            text = "${item.quantity}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = item.itemName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (item.instructions.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Note: ${item.instructions}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD84315)
                        )
                    }
                }
            }

            // Status Badge & Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = containerBg
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                when (item.kotStatus) {
                    "PENDING" -> {
                        Button(
                            onClick = { onUpdateStatus("PREPARING") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Cook", fontSize = 11.sp)
                        }
                    }
                    "PREPARING" -> {
                        Button(
                            onClick = { onUpdateStatus("READY") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Ready", fontSize = 11.sp)
                        }
                    }
                    "READY" -> {
                        Button(
                            onClick = { onUpdateStatus("SERVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Served ✓", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
