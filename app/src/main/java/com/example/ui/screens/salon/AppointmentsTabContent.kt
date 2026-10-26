package com.example.ui.screens.salon

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SalonAppointmentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun AppointmentsTabContent(
    appointments: List<SalonAppointmentEntity>,
    onBookAppointment: () -> Unit,
    onStartService: (SalonAppointmentEntity) -> Unit,
    onCompleteAppointment: (SalonAppointmentEntity) -> Unit,
    onCancelAppointment: (SalonAppointmentEntity) -> Unit,
    onDeleteAppointment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredAppointments = remember(appointments, selectedStatus, searchQuery) {
        appointments.filter { appt ->
            val matchesStatus = selectedStatus == "ALL" || appt.status.equals(selectedStatus, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    appt.clientName.contains(searchQuery, ignoreCase = true) ||
                    appt.clientPhone.contains(searchQuery, ignoreCase = true) ||
                    appt.serviceName.contains(searchQuery, ignoreCase = true) ||
                    appt.stylistName.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    val totalBookedRevenue = remember(appointments) {
        appointments.filter { it.status != "CANCELLED" }.sumOf { it.finalPrice }
    }
    val totalCommissionPayout = remember(appointments) {
        appointments.filter { it.status == "COMPLETED" }.sumOf { it.commissionAmount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("appointments_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Booked Revenue",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSalonPkr(totalBookedRevenue),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PakEmeraldPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Completed Stylist Payout",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSalonPkr(totalCommissionPayout),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PakGoldSecondary
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_appointments"),
                placeholder = { Text("Search by client, stylist, or service...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Status Filter Chips
        item {
            val scheduledCount = appointments.count { it.status == "SCHEDULED" }
            val inProgressCount = appointments.count { it.status == "IN_PROGRESS" }
            val completedCount = appointments.count { it.status == "COMPLETED" }
            val cancelledCount = appointments.count { it.status == "CANCELLED" }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == "ALL",
                        onClick = { selectedStatus = "ALL" },
                        label = { Text("All (${appointments.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "SCHEDULED",
                        onClick = { selectedStatus = "SCHEDULED" },
                        label = { Text("Scheduled ($scheduledCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "IN_PROGRESS",
                        onClick = { selectedStatus = "IN_PROGRESS" },
                        label = { Text("In Progress ($inProgressCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "COMPLETED",
                        onClick = { selectedStatus = "COMPLETED" },
                        label = { Text("Completed ($completedCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "CANCELLED",
                        onClick = { selectedStatus = "CANCELLED" },
                        label = { Text("Cancelled ($cancelledCount)") }
                    )
                }
            }
        }

        // Empty state
        if (filteredAppointments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No appointments match '$searchQuery'" else "No Appointments Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Schedule appointments with clients, assign stylists, and track real-time commissions upon service completion.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onBookAppointment,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Book First Appointment")
                        }
                    }
                }
            }
        } else {
            items(filteredAppointments, key = { it.id }) { appt ->
                AppointmentCard(
                    appointment = appt,
                    onStartService = { onStartService(appt) },
                    onComplete = { onCompleteAppointment(appt) },
                    onCancel = { onCancelAppointment(appt) },
                    onDelete = { onDeleteAppointment(appt.id) },
                    onCallClient = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${appt.clientPhone}")
                        }
                        try {
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                            // Dial launcher fallback
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: SalonAppointmentEntity,
    onStartService: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onCallClient: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = getAppointmentStatusColors(appointment.status)
    val (catColor, catBg, catOnBg) = getServiceCategoryColors(appointment.serviceCategory)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_appointment_${appointment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Date & Time Pill + Status Pill + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${appointment.appointmentDate} • ${appointment.appointmentTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = appointment.status.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (appointment.status != "COMPLETED" && appointment.status != "CANCELLED") {
                            DropdownMenuItem(
                                text = { Text("Cancel Appointment") },
                                onClick = {
                                    showMenu = false
                                    onCancel()
                                },
                                leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Client & Service Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = appointment.clientName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = appointment.clientPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onCallClient,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Client",
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = catBg
                ) {
                    Text(
                        text = appointment.serviceCategory,
                        color = catOnBg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Service Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = PakEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = appointment.serviceName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Stylist & Commission Breakdown
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Assigned Stylist",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = appointment.stylistName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Stylist Commission (${appointment.commissionPercentage.toInt()}%)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSalonPkr(appointment.commissionAmount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakGoldSecondary
                        )
                    }
                }
            }

            // Financial Breakdown: Price, Discount & Payment Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatSalonPkr(appointment.finalPrice),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PakEmeraldPrimary
                        )
                        if (appointment.discount > 0) {
                            Text(
                                text = "-${formatSalonPkr(appointment.discount)} disc",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (appointment.paymentStatus == "PAID") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = appointment.paymentStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appointment.paymentStatus == "PAID") Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Workflow Quick Action Buttons
                when (appointment.status) {
                    "SCHEDULED" -> {
                        Button(
                            onClick = onStartService,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Start Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "IN_PROGRESS" -> {
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Complete & Settle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "COMPLETED" -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Settled & Paid",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            if (appointment.notes.isNotBlank()) {
                Text(
                    text = "Note: ${appointment.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
