package com.example.ui.screens.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LeadEntity
import com.example.data.PropertyEntity
import com.example.data.SiteVisitEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun RealEstateModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsStateWithLifecycle()
    val properties by viewModel.realEstateProperties.collectAsStateWithLifecycle()
    val leads by viewModel.realEstateLeads.collectAsStateWithLifecycle()
    val visits by viewModel.realEstateVisits.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAddEditPropertyDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<PropertyEntity?>(null) }

    var showAddEditLeadDialog by remember { mutableStateOf(false) }
    var leadToEdit by remember { mutableStateOf<LeadEntity?>(null) }

    var showScheduleVisitDialog by remember { mutableStateOf(false) }
    var visitToEdit by remember { mutableStateOf<SiteVisitEntity?>(null) }
    var prefillVisitProperty by remember { mutableStateOf<PropertyEntity?>(null) }
    var prefillVisitLead by remember { mutableStateOf<LeadEntity?>(null) }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var visitForFeedback by remember { mutableStateOf<SiteVisitEntity?>(null) }

    val businessId = activeBusiness?.id ?: 1L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> {
                            propertyToEdit = null
                            showAddEditPropertyDialog = true
                        }
                        1 -> {
                            leadToEdit = null
                            showAddEditLeadDialog = true
                        }
                        2 -> {
                            visitToEdit = null
                            prefillVisitProperty = null
                            prefillVisitLead = null
                            showScheduleVisitDialog = true
                        }
                    }
                },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_real_estate")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Add Property"
                            1 -> "Add Lead"
                            2 -> "Schedule Visit"
                            else -> "Add"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Module Top Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PakEmeraldPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = PakEmeraldPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Real Estate Agency Hub",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PakEmeraldPrimary
                                )
                                Text(
                                    text = "${activeBusiness?.name ?: "PakBusiness"} • Properties & Deal Pipeline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PakGoldSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PKR PORTFOLIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PakGoldSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Key Real Estate Metrics Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Properties metric
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Listings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${properties.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PakEmeraldPrimary)
                                val saleCount = properties.count { it.purpose == "SALE" }
                                val rentCount = properties.count { it.purpose == "RENT" }
                                Text("$saleCount Sale • $rentCount Rent", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Leads metric
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Client Leads", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${leads.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                                val totalBudget = leads.filter { it.status != "LOST" }.sumOf { it.maxBudget }
                                Text(RealEstateFormatters.formatPkrShort(totalBudget), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }

                        // Site visits metric
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Site Visits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val scheduled = visits.count { it.status == "SCHEDULED" }
                                Text("$scheduled", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PakGoldSecondary)
                                Text("${visits.size} total visits", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Primary Real Estate Module Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Properties", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Badge(containerColor = PakEmeraldPrimary.copy(alpha = 0.15f), contentColor = PakEmeraldPrimary) {
                                Text("${properties.size}", fontSize = 10.sp)
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_real_estate_properties")
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Leads & Budget", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Badge(containerColor = Color(0xFF2563EB).copy(alpha = 0.15f), contentColor = Color(0xFF2563EB)) {
                                Text("${leads.size}", fontSize = 10.sp)
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_real_estate_leads")
                )

                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Site Visits", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val scheduledCount = visits.count { it.status == "SCHEDULED" }
                            if (scheduledCount > 0) {
                                Badge(containerColor = PakGoldSecondary.copy(alpha = 0.2f), contentColor = PakGoldSecondary) {
                                    Text("$scheduledCount", fontSize = 10.sp)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_real_estate_visits")
                )
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> PropertiesTabContent(
                        properties = properties,
                        onAddProperty = {
                            propertyToEdit = null
                            showAddEditPropertyDialog = true
                        },
                        onEditProperty = { prop ->
                            propertyToEdit = prop
                            showAddEditPropertyDialog = true
                        },
                        onDeleteProperty = { id ->
                            viewModel.deleteRealEstateProperty(id)
                        },
                        onUpdateStatus = { id, status ->
                            viewModel.updatePropertyStatus(id, status)
                        },
                        onScheduleVisitForProperty = { prop ->
                            visitToEdit = null
                            prefillVisitProperty = prop
                            prefillVisitLead = null
                            showScheduleVisitDialog = true
                        }
                    )

                    1 -> LeadsTabContent(
                        leads = leads,
                        onAddLead = {
                            leadToEdit = null
                            showAddEditLeadDialog = true
                        },
                        onEditLead = { lead ->
                            leadToEdit = lead
                            showAddEditLeadDialog = true
                        },
                        onDeleteLead = { id ->
                            viewModel.deleteRealEstateLead(id)
                        },
                        onUpdateStatus = { id, status ->
                            viewModel.updateLeadStatus(id, status)
                        },
                        onScheduleVisitForLead = { lead ->
                            visitToEdit = null
                            prefillVisitProperty = null
                            prefillVisitLead = lead
                            showScheduleVisitDialog = true
                        }
                    )

                    2 -> SiteVisitsTabContent(
                        visits = visits,
                        onScheduleVisit = {
                            visitToEdit = null
                            prefillVisitProperty = null
                            prefillVisitLead = null
                            showScheduleVisitDialog = true
                        },
                        onEditVisit = { visit ->
                            visitToEdit = visit
                            showScheduleVisitDialog = true
                        },
                        onDeleteVisit = { id ->
                            viewModel.deleteRealEstateSiteVisit(id)
                        },
                        onCompleteVisit = { visit ->
                            visitForFeedback = visit
                            showFeedbackDialog = true
                        },
                        onCancelVisit = { id ->
                            viewModel.updateSiteVisitStatus(id, "CANCELLED", "Visit cancelled")
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddEditPropertyDialog) {
        AddEditPropertyDialog(
            initialProperty = propertyToEdit,
            businessId = businessId,
            onDismiss = {
                showAddEditPropertyDialog = false
                propertyToEdit = null
            },
            onSave = { prop ->
                viewModel.saveRealEstateProperty(prop)
                showAddEditPropertyDialog = false
                propertyToEdit = null
            }
        )
    }

    if (showAddEditLeadDialog) {
        AddEditLeadDialog(
            initialLead = leadToEdit,
            businessId = businessId,
            onDismiss = {
                showAddEditLeadDialog = false
                leadToEdit = null
            },
            onSave = { lead ->
                viewModel.saveRealEstateLead(lead)
                showAddEditLeadDialog = false
                leadToEdit = null
            }
        )
    }

    if (showScheduleVisitDialog) {
        ScheduleSiteVisitDialog(
            initialVisit = visitToEdit,
            prefillProperty = prefillVisitProperty,
            prefillLead = prefillVisitLead,
            availableProperties = properties,
            availableLeads = leads,
            businessId = businessId,
            onDismiss = {
                showScheduleVisitDialog = false
                visitToEdit = null
                prefillVisitProperty = null
                prefillVisitLead = null
            },
            onSave = { visit ->
                viewModel.saveRealEstateSiteVisit(visit)
                showScheduleVisitDialog = false
                visitToEdit = null
                prefillVisitProperty = null
                prefillVisitLead = null
            }
        )
    }

    if (showFeedbackDialog && visitForFeedback != null) {
        RecordVisitFeedbackDialog(
            visit = visitForFeedback!!,
            onDismiss = {
                showFeedbackDialog = false
                visitForFeedback = null
            },
            onConfirm = { feedback, status ->
                visitForFeedback?.let { v ->
                    viewModel.updateSiteVisitStatus(v.id, status, feedback)
                }
                showFeedbackDialog = false
                visitForFeedback = null
            }
        )
    }
}
