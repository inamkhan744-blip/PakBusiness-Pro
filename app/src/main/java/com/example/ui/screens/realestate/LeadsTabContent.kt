package com.example.ui.screens.realestate

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LeadEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun LeadsTabContent(
    leads: List<LeadEntity>,
    onAddLead: () -> Unit,
    onEditLead: (LeadEntity) -> Unit,
    onDeleteLead: (Long) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onScheduleVisitForLead: (LeadEntity) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedReqFilter by remember { mutableStateOf("ALL") }

    val filteredLeads = remember(leads, searchQuery, selectedStatusFilter, selectedReqFilter) {
        leads.filter { l ->
            val matchSearch = searchQuery.isBlank() ||
                    l.clientName.contains(searchQuery, ignoreCase = true) ||
                    l.phone.contains(searchQuery, ignoreCase = true) ||
                    l.preferredLocation.contains(searchQuery, ignoreCase = true) ||
                    l.assignedAgent.contains(searchQuery, ignoreCase = true)
            val matchStatus = selectedStatusFilter == "ALL" || l.status.equals(selectedStatusFilter, ignoreCase = true)
            val matchReq = selectedReqFilter == "ALL" || l.requirementType.equals(selectedReqFilter, ignoreCase = true)
            matchSearch && matchStatus && matchReq
        }
    }

    // Calculate budget statistics
    val totalMaxBudget = remember(leads) {
        leads.filter { it.status != "LOST" }.sumOf { it.maxBudget }
    }
    val activePipelineCount = remember(leads) {
        leads.count { it.status != "LOST" && it.status != "CLOSED_WON" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pipeline & Budget Metrics Banner
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLIENT LEADS & BUDGET PIPELINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${leads.size} Total Leads",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PakEmeraldPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$activePipelineCount In Pipeline",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Pipeline Budget Potential",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = RealEstateFormatters.formatPkrShort(totalMaxBudget),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PakEmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search client name, phone, location, agent...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_leads"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filters Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statuses = listOf(
                        "ALL" to "All Statuses",
                        "NEW" to "New",
                        "CONTACTED" to "Contacted",
                        "SITE_VISIT" to "Site Visit",
                        "NEGOTIATION" to "Negotiation",
                        "CLOSED_WON" to "Closed Won",
                        "LOST" to "Lost"
                    )
                    items(statuses) { (key, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == key,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }

                    item {
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    val reqs = listOf(
                        "ALL" to "All Types",
                        "BUY" to "Buyer",
                        "RENT" to "Tenant",
                        "INVEST" to "Investor"
                    )
                    items(reqs) { (key, label) ->
                        FilterChip(
                            selected = selectedReqFilter == key,
                            onClick = { selectedReqFilter = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Leads List
        if (filteredLeads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedStatusFilter != "ALL") {
                            "No client leads match your criteria"
                        } else {
                            "No client leads yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add client leads to record buyer budgets, property preferences, and track deal stages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onAddLead,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_empty_add_lead")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Client Lead")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLeads, key = { it.id }) { lead ->
                    LeadCard(
                        lead = lead,
                        onEdit = { onEditLead(lead) },
                        onDelete = { onDeleteLead(lead.id) },
                        onUpdateStatus = { newStatus -> onUpdateStatus(lead.id, newStatus) },
                        onScheduleVisit = { onScheduleVisitForLead(lead) },
                        onCallClient = {
                            if (lead.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LeadCard(
    lead: LeadEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onScheduleVisit: () -> Unit,
    onCallClient: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val statusColor = when (lead.status.uppercase()) {
        "NEW" -> Color(0xFF2563EB)
        "CONTACTED" -> Color(0xFF0D9488)
        "SITE_VISIT" -> Color(0xFFEA580C)
        "NEGOTIATION" -> PakGoldSecondary
        "CLOSED_WON" -> Color(0xFF059669)
        "LOST" -> Color(0xFF9CA3AF)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBg = when (lead.status.uppercase()) {
        "NEW" -> Color(0xFFDBEAFE)
        "CONTACTED" -> Color(0xFFCCFBF1)
        "SITE_VISIT" -> Color(0xFFFFEDD5)
        "NEGOTIATION" -> Color(0xFFFEF3C7)
        "CLOSED_WON" -> Color(0xFFD1FAE5)
        "LOST" -> Color(0xFFF3F4F6)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lead_card_${lead.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Client Name, Avatar, and Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Initial Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PakEmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = lead.clientName.firstOrNull()?.uppercase() ?: "L"
                        Text(
                            text = initial,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PakEmeraldPrimary
                        )
                    }

                    Column {
                        Text(
                            text = lead.clientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (lead.requirementType.uppercase()) {
                                    "BUY" -> PakEmeraldPrimary.copy(alpha = 0.12f)
                                    "RENT" -> Color(0xFF2563EB).copy(alpha = 0.12f)
                                    else -> PakGoldSecondary.copy(alpha = 0.12f)
                                }
                            ) {
                                Text(
                                    text = when (lead.requirementType.uppercase()) {
                                        "BUY" -> "BUYER"
                                        "RENT" -> "TENANT"
                                        else -> "INVESTOR"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (lead.requirementType.uppercase()) {
                                        "BUY" -> PakEmeraldPrimary
                                        "RENT" -> Color(0xFF2563EB)
                                        else -> PakGoldSecondary
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (lead.phone.isNotBlank()) {
                                Text(
                                    text = lead.phone,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Status Dropdown Button
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg,
                        modifier = Modifier.clickable { showStatusMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = lead.status.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = statusColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        val stages = listOf("NEW", "CONTACTED", "SITE_VISIT", "NEGOTIATION", "CLOSED_WON", "LOST")
                        stages.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage.replace("_", " ")) },
                                onClick = {
                                    onUpdateStatus(stage)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted Client Budget Card (Key user requirement)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldPrimary.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Client Budget",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = RealEstateFormatters.formatBudgetRange(lead.minBudget, lead.maxBudget),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary
                            )
                        }
                    }

                    if (lead.maxBudget > 0) {
                        Text(
                            text = "Max: ${RealEstateFormatters.formatPkrFull(lead.maxBudget)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preferences Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Seeking: ${lead.preferredPropertyType} • ${if (lead.preferredSize.isNotBlank()) lead.preferredSize else "Any Size"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (lead.preferredLocation.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Target Area: ${lead.preferredLocation}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (lead.assignedAgent.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(15.dp), tint = PakGoldSecondary)
                        Text(
                            text = "Agent: ${lead.assignedAgent}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (lead.notes.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(
                            text = lead.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (lead.phone.isNotBlank()) {
                        FilledTonalIconButton(
                            onClick = onCallClient,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PakEmeraldPrimary.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call Lead", tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Lead", modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Lead", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onScheduleVisit,
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_schedule_visit_lead_${lead.id}")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Client Lead?") },
            text = { Text("Are you sure you want to remove ${lead.clientName} from your leads pipeline?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
