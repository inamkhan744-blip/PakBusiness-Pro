package com.example.ui.screens.workshop

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WorkshopJobCardEntity
import com.example.data.WorkshopMechanicEntity
import com.example.data.WorkshopVehicleEntity
import com.example.ui.BusinessViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Workshop Brand Colors
private val WorkshopOrange = Color(0xFFEA580C)
private val WorkshopDarkOrange = Color(0xFFC2410C)
private val WorkshopSlate = Color(0xFF334155)
private val StatusSuccess = Color(0xFF16A34A)
private val StatusWarning = Color(0xFFD97706)
private val StatusInfo = Color(0xFF0284C7)
private val StatusDanger = Color(0xFFDC2626)

enum class WorkshopTab {
    JOB_CARDS,
    VEHICLES,
    MECHANICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by viewModel.workshopVehicles.collectAsStateWithLifecycle()
    val mechanics by viewModel.workshopMechanics.collectAsStateWithLifecycle()
    val jobCards by viewModel.workshopJobCards.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(WorkshopTab.JOB_CARDS) }

    // Dialog States
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<WorkshopVehicleEntity?>(null) }

    var showAddMechanicDialog by remember { mutableStateOf(false) }
    var mechanicToEdit by remember { mutableStateOf<WorkshopMechanicEntity?>(null) }

    var showAddJobCardDialog by remember { mutableStateOf(false) }
    var jobCardToEdit by remember { mutableStateOf<WorkshopJobCardEntity?>(null) }
    var jobCardForSlip by remember { mutableStateOf<WorkshopJobCardEntity?>(null) }
    var jobCardForPayment by remember { mutableStateOf<WorkshopJobCardEntity?>(null) }
    var jobCardForAssignMechanic by remember { mutableStateOf<WorkshopJobCardEntity?>(null) }

    val pkrFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(WorkshopOrange, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Auto Workshop",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Auto Workshop & Garage",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Job cards • Customer vehicles • Mechanic lab",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Module summary badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = WorkshopOrange.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, WorkshopOrange.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = WorkshopOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${jobCards.count { card -> card.status in listOf("PENDING", "IN_PROGRESS", "WAITING_PARTS") }} In Bay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkshopDarkOrange
                                )
                            }
                        }
                    }

                    // Navigation Tabs
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = WorkshopOrange
                    ) {
                        Tab(
                            selected = selectedTab == WorkshopTab.JOB_CARDS,
                            onClick = { selectedTab = WorkshopTab.JOB_CARDS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Job Cards (${jobCards.size})", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            modifier = Modifier.testTag("tab_workshop_job_cards")
                        )
                        Tab(
                            selected = selectedTab == WorkshopTab.VEHICLES,
                            onClick = { selectedTab = WorkshopTab.VEHICLES },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Vehicles (${vehicles.size})", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            modifier = Modifier.testTag("tab_workshop_vehicles")
                        )
                        Tab(
                            selected = selectedTab == WorkshopTab.MECHANICS,
                            onClick = { selectedTab = WorkshopTab.MECHANICS },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Mechanics (${mechanics.size})", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            modifier = Modifier.testTag("tab_workshop_mechanics")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        WorkshopTab.JOB_CARDS -> {
                            jobCardToEdit = null
                            showAddJobCardDialog = true
                        }
                        WorkshopTab.VEHICLES -> {
                            vehicleToEdit = null
                            showAddVehicleDialog = true
                        }
                        WorkshopTab.MECHANICS -> {
                            mechanicToEdit = null
                            showAddMechanicDialog = true
                        }
                    }
                },
                containerColor = WorkshopOrange,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = when (selectedTab) {
                            WorkshopTab.JOB_CARDS -> Icons.Default.NoteAdd
                            WorkshopTab.VEHICLES -> Icons.Default.DirectionsCar
                            WorkshopTab.MECHANICS -> Icons.Default.PersonAdd
                        },
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = when (selectedTab) {
                            WorkshopTab.JOB_CARDS -> "New Job Card"
                            WorkshopTab.VEHICLES -> "Register Vehicle"
                            WorkshopTab.MECHANICS -> "Add Mechanic"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("fab_workshop_add")
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                WorkshopTab.JOB_CARDS -> {
                    JobCardsTabContent(
                        jobCards = jobCards,
                        mechanics = mechanics,
                        pkrFormat = pkrFormat,
                        onEdit = {
                            jobCardToEdit = it
                            showAddJobCardDialog = true
                        },
                        onDelete = { viewModel.deleteWorkshopJobCard(it.id) },
                        onAdvanceStatus = { card, nextStatus ->
                            viewModel.updateWorkshopJobCardStatus(card.id, nextStatus)
                            Toast.makeText(context, "Job Card updated to $nextStatus", Toast.LENGTH_SHORT).show()
                        },
                        onCollectBalance = { jobCardForPayment = it },
                        onAssignMechanic = { jobCardForAssignMechanic = it },
                        onViewSlip = { jobCardForSlip = it }
                    )
                }
                WorkshopTab.VEHICLES -> {
                    VehiclesTabContent(
                        vehicles = vehicles,
                        jobCards = jobCards,
                        onEdit = {
                            vehicleToEdit = it
                            showAddVehicleDialog = true
                        },
                        onDelete = { viewModel.deleteWorkshopVehicle(it.id) },
                        onCreateJobCardForVehicle = { vehicle ->
                            jobCardToEdit = null
                            showAddJobCardDialog = true
                        }
                    )
                }
                WorkshopTab.MECHANICS -> {
                    MechanicsTabContent(
                        mechanics = mechanics,
                        jobCards = jobCards,
                        onEdit = {
                            mechanicToEdit = it
                            showAddMechanicDialog = true
                        },
                        onDelete = { viewModel.deleteWorkshopMechanic(it.id) }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    if (showAddVehicleDialog) {
        AddEditVehicleDialog(
            existingVehicle = vehicleToEdit,
            onDismiss = {
                showAddVehicleDialog = false
                vehicleToEdit = null
            },
            onSave = { vehicle ->
                viewModel.saveWorkshopVehicle(vehicle)
                showAddVehicleDialog = false
                vehicleToEdit = null
                Toast.makeText(context, "Vehicle record saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddMechanicDialog) {
        AddEditMechanicDialog(
            existingMechanic = mechanicToEdit,
            onDismiss = {
                showAddMechanicDialog = false
                mechanicToEdit = null
            },
            onSave = { mechanic ->
                viewModel.saveWorkshopMechanic(mechanic)
                showAddMechanicDialog = false
                mechanicToEdit = null
                Toast.makeText(context, "Mechanic saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddJobCardDialog) {
        AddEditJobCardDialog(
            existingJobCard = jobCardToEdit,
            vehicles = vehicles,
            mechanics = mechanics,
            onDismiss = {
                showAddJobCardDialog = false
                jobCardToEdit = null
            },
            onSave = { jobCard ->
                viewModel.saveWorkshopJobCard(jobCard)
                showAddJobCardDialog = false
                jobCardToEdit = null
                Toast.makeText(context, "Job Card saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (jobCardForAssignMechanic != null) {
        AssignMechanicDialog(
            jobCard = jobCardForAssignMechanic!!,
            mechanics = mechanics,
            onDismiss = { jobCardForAssignMechanic = null },
            onAssign = { mechanicId, mechanicName ->
                viewModel.assignMechanicToJobCard(jobCardForAssignMechanic!!.id, mechanicId, mechanicName)
                jobCardForAssignMechanic = null
                Toast.makeText(context, "Assigned to $mechanicName", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (jobCardForPayment != null) {
        CollectWorkshopBalanceDialog(
            jobCard = jobCardForPayment!!,
            pkrFormat = pkrFormat,
            onDismiss = { jobCardForPayment = null },
            onConfirmPayment = { updatedAdvance, updatedBalance ->
                viewModel.updateWorkshopJobCardPayment(jobCardForPayment!!.id, updatedAdvance, updatedBalance)
                jobCardForPayment = null
                Toast.makeText(context, "Payment updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (jobCardForSlip != null) {
        WorkshopJobCardSlipDialog(
            jobCard = jobCardForSlip!!,
            pkrFormat = pkrFormat,
            onDismiss = { jobCardForSlip = null }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: JOB CARDS & WORKFLOW
// -----------------------------------------------------------------------------------------

@Composable
fun JobCardsTabContent(
    jobCards: List<WorkshopJobCardEntity>,
    mechanics: List<WorkshopMechanicEntity>,
    pkrFormat: NumberFormat,
    onEdit: (WorkshopJobCardEntity) -> Unit,
    onDelete: (WorkshopJobCardEntity) -> Unit,
    onAdvanceStatus: (WorkshopJobCardEntity, String) -> Unit,
    onCollectBalance: (WorkshopJobCardEntity) -> Unit,
    onAssignMechanic: (WorkshopJobCardEntity) -> Unit,
    onViewSlip: (WorkshopJobCardEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredJobCards = remember(jobCards, searchQuery, selectedStatusFilter) {
        jobCards.filter { card ->
            val matchesStatus = selectedStatusFilter == "ALL" || card.status == selectedStatusFilter
            val matchesSearch = searchQuery.isBlank() ||
                    card.jobCardNumber.contains(searchQuery, ignoreCase = true) ||
                    card.vehiclePlateNumber.contains(searchQuery, ignoreCase = true) ||
                    card.customerName.contains(searchQuery, ignoreCase = true) ||
                    card.vehicleModel.contains(searchQuery, ignoreCase = true) ||
                    card.assignedMechanicName.contains(searchQuery, ignoreCase = true) ||
                    card.chassisNumber.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Status filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Plate, Job #, Customer, Chassis...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_job_cards")
                )

                // Status horizontal scroll
                val statuses = listOf(
                    "ALL" to "All Jobs",
                    "PENDING" to "Pending Intake",
                    "IN_PROGRESS" to "In Progress",
                    "WAITING_PARTS" to "Waiting Parts",
                    "QUALITY_CHECK" to "Quality Check",
                    "COMPLETED" to "Ready for Delivery",
                    "DELIVERED" to "Delivered"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statuses.forEach { (code, label) ->
                        val isSelected = selectedStatusFilter == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = code },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WorkshopOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Summary Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    title = "Active Bays",
                    value = jobCards.count { it.status in listOf("PENDING", "IN_PROGRESS", "WAITING_PARTS") }.toString(),
                    color = WorkshopOrange,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Waiting Parts",
                    value = jobCards.count { it.status == "WAITING_PARTS" }.toString(),
                    color = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Ready for Delivery",
                    value = jobCards.count { it.status == "COMPLETED" }.toString(),
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredJobCards.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No job cards found",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap '+ New Job Card' to create a repair ticket",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredJobCards, key = { it.id }) { jobCard ->
                WorkshopJobCardItem(
                    jobCard = jobCard,
                    pkrFormat = pkrFormat,
                    onEdit = { onEdit(jobCard) },
                    onDelete = { onDelete(jobCard) },
                    onAdvanceStatus = onAdvanceStatus,
                    onCollectBalance = { onCollectBalance(jobCard) },
                    onAssignMechanic = { onAssignMechanic(jobCard) },
                    onViewSlip = { onViewSlip(jobCard) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun WorkshopJobCardItem(
    jobCard: WorkshopJobCardEntity,
    pkrFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdvanceStatus: (WorkshopJobCardEntity, String) -> Unit,
    onCollectBalance: () -> Unit,
    onAssignMechanic: () -> Unit,
    onViewSlip: () -> Unit
) {
    val statusColor = when (jobCard.status) {
        "PENDING" -> StatusWarning
        "IN_PROGRESS" -> StatusInfo
        "WAITING_PARTS" -> Color(0xFF9333EA)
        "QUALITY_CHECK" -> Color(0xFF0D9488)
        "COMPLETED" -> StatusSuccess
        "DELIVERED" -> Color(0xFF64748B)
        else -> Color.Gray
    }

    val statusLabel = when (jobCard.status) {
        "PENDING" -> "Intake / Inspection"
        "IN_PROGRESS" -> "In Repair"
        "WAITING_PARTS" -> "Waiting Spare Parts"
        "QUALITY_CHECK" -> "Quality Testing"
        "COMPLETED" -> "Ready for Pickup"
        "DELIVERED" -> "Delivered & Closed"
        else -> jobCard.status
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_job_card_${jobCard.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Job Card # + Status Badge
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
                        shape = RoundedCornerShape(6.dp),
                        color = WorkshopSlate
                    ) {
                        Text(
                            text = jobCard.jobCardNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (jobCard.bayOrRackNumber.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WorkshopOrange.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = jobCard.bayOrRackNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkshopDarkOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Vehicle & Customer Info Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = WorkshopOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = jobCard.vehicleModel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Number plate
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24))
                        ) {
                            Text(
                                text = jobCard.vehiclePlateNumber,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Customer & Chassis info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Customer: ${jobCard.customerName} (${jobCard.customerPhone})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (jobCard.currentMileageKm > 0) {
                            Text(
                                text = "${jobCard.currentMileageKm} KM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (jobCard.chassisNumber.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Chassis No:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = jobCard.chassisNumber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Reported Complaints
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CUSTOMER COMPLAINTS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = jobCard.reportedCustomerComplaints,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Diagnostic & Work Notes
            if (jobCard.diagnosticNotes.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "DIAGNOSTIC & ACTION TAKEN:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = jobCard.diagnosticNotes,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // Mechanic Assignment Row
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = WorkshopSlate.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = WorkshopOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Assigned Mechanic",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (jobCard.assignedMechanicName.isNotBlank()) jobCard.assignedMechanicName else "Unassigned",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (jobCard.assignedMechanicName.isNotBlank()) MaterialTheme.colorScheme.onSurface else StatusDanger
                            )
                        }
                    }

                    TextButton(
                        onClick = onAssignMechanic,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (jobCard.assignedMechanicName.isBlank()) "Assign" else "Change", fontSize = 11.sp)
                    }
                }
            }

            // Financials Breakdown in PKR
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimate (Labor + Parts)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pkrFormat.format(jobCard.totalEstimatedCost),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Advance Paid",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pkrFormat.format(jobCard.advanceDeposit),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = StatusSuccess
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Balance",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pkrFormat.format(jobCard.remainingBalance),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (jobCard.remainingBalance > 0) StatusDanger else StatusSuccess
                        )
                    }
                }
            }

            // Action Buttons
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onViewSlip,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "View Printable Job Card",
                            tint = WorkshopOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Job Card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Job Card",
                            tint = StatusDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (jobCard.remainingBalance > 0) {
                        OutlinedButton(
                            onClick = onCollectBalance,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Collect Balance", fontSize = 11.sp)
                        }
                    }

                    // Next Status advancement button
                    val nextStatus = when (jobCard.status) {
                        "PENDING" -> "IN_PROGRESS" to "Start Repair"
                        "IN_PROGRESS" -> "WAITING_PARTS" to "Need Parts"
                        "WAITING_PARTS" -> "QUALITY_CHECK" to "Parts Arrived"
                        "QUALITY_CHECK" -> "COMPLETED" to "Pass & Ready"
                        "COMPLETED" -> "DELIVERED" to "Mark Delivered"
                        else -> null
                    }

                    if (nextStatus != null) {
                        Button(
                            onClick = { onAdvanceStatus(jobCard, nextStatus.first) },
                            colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(nextStatus.second, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: CUSTOMER VEHICLE RECORDS
// -----------------------------------------------------------------------------------------

@Composable
fun VehiclesTabContent(
    vehicles: List<WorkshopVehicleEntity>,
    jobCards: List<WorkshopJobCardEntity>,
    onEdit: (WorkshopVehicleEntity) -> Unit,
    onDelete: (WorkshopVehicleEntity) -> Unit,
    onCreateJobCardForVehicle: (WorkshopVehicleEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredVehicles = remember(vehicles, searchQuery) {
        if (searchQuery.isBlank()) {
            vehicles
        } else {
            vehicles.filter {
                it.plateNumber.contains(searchQuery, ignoreCase = true) ||
                        it.makeAndModel.contains(searchQuery, ignoreCase = true) ||
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                        it.chassisNumber.contains(searchQuery, ignoreCase = true) ||
                        it.customerPhone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Registration #, Model, Chassis, Customer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_vehicles")
            )
        }

        // Summary metric
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    title = "Registered Vehicles",
                    value = vehicles.size.toString(),
                    color = WorkshopOrange,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Active Repairs",
                    value = jobCards.count { it.status != "DELIVERED" && it.status != "CANCELLED" }.toString(),
                    color = StatusInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredVehicles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No vehicle records found",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap '+ Register Vehicle' to save a customer's car details",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredVehicles, key = { it.id }) { vehicle ->
                val vehicleJobCards = jobCards.filter { it.vehiclePlateNumber.equals(vehicle.plateNumber, ignoreCase = true) || it.vehicleId == vehicle.id }

                WorkshopVehicleCard(
                    vehicle = vehicle,
                    jobCount = vehicleJobCards.size,
                    activeJob = vehicleJobCards.firstOrNull { it.status != "DELIVERED" && it.status != "CANCELLED" },
                    onEdit = { onEdit(vehicle) },
                    onDelete = { onDelete(vehicle) },
                    onCreateJobCard = { onCreateJobCardForVehicle(vehicle) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun WorkshopVehicleCard(
    vehicle: WorkshopVehicleEntity,
    jobCount: Int,
    activeJob: WorkshopJobCardEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateJobCard: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_vehicle_${vehicle.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Registration Plate + Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // License plate badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, Color(0xFFFBBF24))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = vehicle.registeredCity.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = vehicle.plateNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WorkshopOrange.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${vehicle.vehicleType} • ${vehicle.modelYear}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkshopDarkOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Model name & customer
            Column {
                Text(
                    text = vehicle.makeAndModel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Owner: ${vehicle.customerName} (${vehicle.customerPhone})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chassis & Engine numbers with copy support
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (vehicle.chassisNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Chassis No: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = vehicle.chassisNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(vehicle.chassisNumber))
                                    Toast.makeText(context, "Chassis number copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Chassis", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    if (vehicle.engineNumber.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Engine No: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = vehicle.engineNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Color: ${vehicle.color} • ${vehicle.fuelType}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Odo: ${vehicle.currentMileageKm} KM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Active Repair Status or Job Count
            if (activeJob != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusInfo.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StatusInfo.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = StatusInfo, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Currently in Bay: ${activeJob.jobCardNumber} (${activeJob.status})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusInfo
                        )
                    }
                }
            } else {
                Text(
                    text = "Service History: $jobCount job card(s) completed",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Vehicle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Vehicle", tint = StatusDanger, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onCreateJobCard,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Job Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 3: MECHANICS & TECHNICIANS
// -----------------------------------------------------------------------------------------

@Composable
fun MechanicsTabContent(
    mechanics: List<WorkshopMechanicEntity>,
    jobCards: List<WorkshopJobCardEntity>,
    onEdit: (WorkshopMechanicEntity) -> Unit,
    onDelete: (WorkshopMechanicEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    title = "Total Mechanics",
                    value = mechanics.size.toString(),
                    color = WorkshopOrange,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Available",
                    value = mechanics.count { it.isAvailable }.toString(),
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Active Workload",
                    value = jobCards.count { it.status in listOf("IN_PROGRESS", "WAITING_PARTS") }.toString(),
                    color = StatusInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (mechanics.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No mechanics registered",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add mechanics to assign them to incoming job cards",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(mechanics, key = { it.id }) { mechanic ->
                val assignedCards = jobCards.filter {
                    (it.assignedMechanicId == mechanic.id || it.assignedMechanicName.equals(mechanic.name, ignoreCase = true)) &&
                            it.status in listOf("PENDING", "IN_PROGRESS", "WAITING_PARTS", "QUALITY_CHECK")
                }

                WorkshopMechanicCard(
                    mechanic = mechanic,
                    activeCards = assignedCards,
                    onEdit = { onEdit(mechanic) },
                    onDelete = { onDelete(mechanic) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun WorkshopMechanicCard(
    mechanic: WorkshopMechanicEntity,
    activeCards: List<WorkshopJobCardEntity>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mechanic_${mechanic.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(WorkshopOrange.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = WorkshopOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = mechanic.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mechanic.specialization,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (mechanic.isAvailable) StatusSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (mechanic.isAvailable) "Available" else "On Leave",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mechanic.isAvailable) StatusSuccess else Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Contact & Active load
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Phone: ${mechanic.phone}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Active Load: ${activeCards.size} Vehicle(s)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeCards.isEmpty()) StatusSuccess else WorkshopOrange
                )
            }

            // List of currently assigned jobs
            if (activeCards.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ASSIGNED VEHICLES ON FLOOR:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        activeCards.forEach { card ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${card.vehiclePlateNumber} (${card.vehicleModel})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = card.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkshopOrange
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Mechanic", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Mechanic", tint = StatusDanger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOGS & FORMS
// -----------------------------------------------------------------------------------------

@Composable
fun AddEditVehicleDialog(
    existingVehicle: WorkshopVehicleEntity?,
    onDismiss: () -> Unit,
    onSave: (WorkshopVehicleEntity) -> Unit
) {
    var customerName by remember { mutableStateOf(existingVehicle?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existingVehicle?.customerPhone ?: "+92 ") }
    var plateNumber by remember { mutableStateOf(existingVehicle?.plateNumber ?: "") }
    var makeAndModel by remember { mutableStateOf(existingVehicle?.makeAndModel ?: "") }
    var modelYear by remember { mutableStateOf(existingVehicle?.modelYear ?: "2021") }
    var chassisNumber by remember { mutableStateOf(existingVehicle?.chassisNumber ?: "") }
    var engineNumber by remember { mutableStateOf(existingVehicle?.engineNumber ?: "") }
    var vehicleType by remember { mutableStateOf(existingVehicle?.vehicleType ?: "Car / Sedan") }
    var color by remember { mutableStateOf(existingVehicle?.color ?: "White") }
    var mileageStr by remember { mutableStateOf(existingVehicle?.currentMileageKm?.toString() ?: "45000") }
    var fuelType by remember { mutableStateOf(existingVehicle?.fuelType ?: "Petrol") }
    var registeredCity by remember { mutableStateOf(existingVehicle?.registeredCity ?: "Lahore") }
    var notes by remember { mutableStateOf(existingVehicle?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (existingVehicle == null) "Register New Vehicle" else "Edit Vehicle Record",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customer details, plate number & chassis VIN record",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Plate Number & City
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = plateNumber,
                            onValueChange = { plateNumber = it.uppercase() },
                            label = { Text("Plate # *") },
                            placeholder = { Text("LEA-21-4589") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_vehicle_plate")
                        )
                        OutlinedTextField(
                            value = registeredCity,
                            onValueChange = { registeredCity = it },
                            label = { Text("City") },
                            placeholder = { Text("Lahore") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Vehicle Make & Model
                    OutlinedTextField(
                        value = makeAndModel,
                        onValueChange = { makeAndModel = it },
                        label = { Text("Make & Model *") },
                        placeholder = { Text("e.g. Toyota Corolla Altis 1.6") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_model")
                    )

                    // Chassis Number (VIN)
                    OutlinedTextField(
                        value = chassisNumber,
                        onValueChange = { chassisNumber = it.uppercase() },
                        label = { Text("Chassis Number (VIN) *") },
                        placeholder = { Text("e.g. NZE140-9082341") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_chassis")
                    )

                    // Engine Number & Year
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = engineNumber,
                            onValueChange = { engineNumber = it.uppercase() },
                            label = { Text("Engine Number") },
                            placeholder = { Text("1ZR-FE-56901") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = modelYear,
                            onValueChange = { modelYear = it },
                            label = { Text("Year") },
                            placeholder = { Text("2021") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    // Customer Name & Phone
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer / Owner Name *") },
                        placeholder = { Text("e.g. Chaudhry Kamran") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vehicle_owner")
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone *") },
                        placeholder = { Text("+92 300 1234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vehicle Type
                    Text("VEHICLE TYPE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val types = listOf("Car / Sedan", "SUV / 4x4", "Hatchback", "Commercial / Pickup", "Motorcycle")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { t ->
                            FilterChip(
                                selected = vehicleType == t,
                                onClick = { vehicleType = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WorkshopOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Color & Fuel Type & Mileage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mileageStr,
                            onValueChange = { mileageStr = it },
                            label = { Text("Mileage (KM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Special Instructions") },
                        placeholder = { Text("VIP customer, prefers original Denso filters") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (plateNumber.isBlank() || makeAndModel.isBlank() || customerName.isBlank()) {
                                return@Button
                            }
                            val vehicle = (existingVehicle ?: WorkshopVehicleEntity(
                                businessId = 0L,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                plateNumber = plateNumber,
                                makeAndModel = makeAndModel
                            )).copy(
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                plateNumber = plateNumber.trim().uppercase(),
                                makeAndModel = makeAndModel.trim(),
                                modelYear = modelYear.trim(),
                                chassisNumber = chassisNumber.trim().uppercase(),
                                engineNumber = engineNumber.trim().uppercase(),
                                vehicleType = vehicleType,
                                color = color.trim(),
                                currentMileageKm = mileageStr.toIntOrNull() ?: 0,
                                fuelType = fuelType,
                                registeredCity = registeredCity.trim(),
                                notes = notes.trim()
                            )
                            onSave(vehicle)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                        modifier = Modifier.testTag("btn_save_vehicle")
                    ) {
                        Text("Save Vehicle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditJobCardDialog(
    existingJobCard: WorkshopJobCardEntity?,
    vehicles: List<WorkshopVehicleEntity>,
    mechanics: List<WorkshopMechanicEntity>,
    onDismiss: () -> Unit,
    onSave: (WorkshopJobCardEntity) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { sdf.format(Date()) }
    val defaultPromisedDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        sdf.format(cal.time)
    }

    var selectedVehicleId by remember { mutableStateOf(existingJobCard?.vehicleId ?: (vehicles.firstOrNull()?.id ?: 0L)) }
    val selectedVehicle = vehicles.firstOrNull { it.id == selectedVehicleId }

    var plateNumber by remember { mutableStateOf(existingJobCard?.vehiclePlateNumber ?: (selectedVehicle?.plateNumber ?: "")) }
    var vehicleModel by remember { mutableStateOf(existingJobCard?.vehicleModel ?: (selectedVehicle?.makeAndModel ?: "")) }
    var chassisNumber by remember { mutableStateOf(existingJobCard?.chassisNumber ?: (selectedVehicle?.chassisNumber ?: "")) }
    var customerName by remember { mutableStateOf(existingJobCard?.customerName ?: (selectedVehicle?.customerName ?: "")) }
    var customerPhone by remember { mutableStateOf(existingJobCard?.customerPhone ?: (selectedVehicle?.customerPhone ?: "+92 ")) }
    var mileageStr by remember { mutableStateOf(existingJobCard?.currentMileageKm?.toString() ?: (selectedVehicle?.currentMileageKm?.toString() ?: "0")) }
    var fuelGaugeLevel by remember { mutableStateOf(existingJobCard?.fuelGaugeLevel ?: "1/2 Tank") }
    var bayOrRackNumber by remember { mutableStateOf(existingJobCard?.bayOrRackNumber ?: "Bay 1 (Lift)") }

    var complaints by remember { mutableStateOf(existingJobCard?.reportedCustomerComplaints ?: "") }
    var diagnosticNotes by remember { mutableStateOf(existingJobCard?.diagnosticNotes ?: "") }

    var selectedMechanicId by remember { mutableStateOf(existingJobCard?.assignedMechanicId ?: (mechanics.firstOrNull()?.id ?: 0L)) }
    val selectedMechanic = mechanics.firstOrNull { it.id == selectedMechanicId }

    var status by remember { mutableStateOf(existingJobCard?.status ?: "PENDING") }
    var laborCostStr by remember { mutableStateOf(existingJobCard?.laborCharges?.toInt()?.toString() ?: "3000") }
    var partsCostStr by remember { mutableStateOf(existingJobCard?.partsEstimatedCost?.toInt()?.toString() ?: "5000") }
    var advanceStr by remember { mutableStateOf(existingJobCard?.advanceDeposit?.toInt()?.toString() ?: "2000") }

    var receivedDate by remember { mutableStateOf(existingJobCard?.receivedDate ?: todayStr) }
    var promisedDeliveryDate by remember { mutableStateOf(existingJobCard?.promisedDeliveryDate ?: defaultPromisedDate) }
    var checklist by remember { mutableStateOf(existingJobCard?.itemsInventoryChecklist ?: "Spare tire, Jack, Wheel spanner, Rubber mats") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (existingJobCard == null) "Create Workshop Job Card" else "Edit Job Card",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Log vehicle intake, technician assignment & labor/parts estimate",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Select existing registered vehicle or enter manual
                    if (vehicles.isNotEmpty() && existingJobCard == null) {
                        Text("SELECT REGISTERED VEHICLE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            vehicles.forEach { v ->
                                FilterChip(
                                    selected = selectedVehicleId == v.id,
                                    onClick = {
                                        selectedVehicleId = v.id
                                        plateNumber = v.plateNumber
                                        vehicleModel = v.makeAndModel
                                        chassisNumber = v.chassisNumber
                                        customerName = v.customerName
                                        customerPhone = v.customerPhone
                                        mileageStr = v.currentMileageKm.toString()
                                    },
                                    label = { Text("${v.plateNumber} (${v.makeAndModel})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WorkshopOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Vehicle Plate & Model
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = plateNumber,
                            onValueChange = { plateNumber = it.uppercase() },
                            label = { Text("Plate # *") },
                            placeholder = { Text("LEA-21-4589") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_job_card_plate")
                        )
                        OutlinedTextField(
                            value = vehicleModel,
                            onValueChange = { vehicleModel = it },
                            label = { Text("Model *") },
                            placeholder = { Text("Corolla Altis") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("input_job_card_model")
                        )
                    }

                    // Chassis Number
                    OutlinedTextField(
                        value = chassisNumber,
                        onValueChange = { chassisNumber = it.uppercase() },
                        label = { Text("Chassis No (VIN)") },
                        placeholder = { Text("NZE140-9082341") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_job_card_chassis")
                    )

                    // Customer info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer *") },
                            placeholder = { Text("Customer Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone") },
                            placeholder = { Text("+92 300 1234567") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    // Fuel gauge & Bay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bayOrRackNumber,
                            onValueChange = { bayOrRackNumber = it },
                            label = { Text("Bay / Station") },
                            placeholder = { Text("Bay 1 (Lift)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fuelGaugeLevel,
                            onValueChange = { fuelGaugeLevel = it },
                            label = { Text("Fuel Level") },
                            placeholder = { Text("1/2 Tank") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Mileage
                    OutlinedTextField(
                        value = mileageStr,
                        onValueChange = { mileageStr = it },
                        label = { Text("Current Mileage (KM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Customer Complaints
                    OutlinedTextField(
                        value = complaints,
                        onValueChange = { complaints = it },
                        label = { Text("Reported Customer Complaints *") },
                        placeholder = { Text("e.g. Engine check light glowing, front brake pads worn, AC cooling weak") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_job_card_complaints"),
                        minLines = 2
                    )

                    // Diagnostic notes
                    OutlinedTextField(
                        value = diagnosticNotes,
                        onValueChange = { diagnosticNotes = it },
                        label = { Text("Diagnostic Notes & Initial Assessment") },
                        placeholder = { Text("Throttle body cleaned, brake rotor skimming needed") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // Assign Mechanic
                    Text("ASSIGN MECHANIC:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (mechanics.isEmpty()) {
                        Text("No mechanics available. You can register them in the Mechanics tab.", fontSize = 11.sp, color = StatusDanger)
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mechanics.forEach { m ->
                                FilterChip(
                                    selected = selectedMechanicId == m.id,
                                    onClick = { selectedMechanicId = m.id },
                                    label = { Text("${m.name} (${m.specialization.take(16)}...)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WorkshopOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Financials (Labor + Parts = Total)
                    Text("FINANCIAL ESTIMATE (PKR):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = laborCostStr,
                            onValueChange = { laborCostStr = it },
                            label = { Text("Labor ₨") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = partsCostStr,
                            onValueChange = { partsCostStr = it },
                            label = { Text("Parts ₨") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = advanceStr,
                            onValueChange = { advanceStr = it },
                            label = { Text("Advance ₨") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val labor = laborCostStr.toDoubleOrNull() ?: 0.0
                    val parts = partsCostStr.toDoubleOrNull() ?: 0.0
                    val total = labor + parts
                    val advance = advanceStr.toDoubleOrNull() ?: 0.0
                    val balance = (total - advance).coerceAtLeast(0.0)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: ₨ ${total.toInt()}", fontWeight = FontWeight.Bold)
                            Text("Balance Due: ₨ ${balance.toInt()}", fontWeight = FontWeight.Bold, color = if (balance > 0) StatusDanger else StatusSuccess)
                        }
                    }

                    // Promised delivery date
                    OutlinedTextField(
                        value = promisedDeliveryDate,
                        onValueChange = { promisedDeliveryDate = it },
                        label = { Text("Promised Delivery Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vehicle Inventory checklist (jack, spare wheel, etc.)
                    OutlinedTextField(
                        value = checklist,
                        onValueChange = { checklist = it },
                        label = { Text("Items Inside Vehicle Checklist") },
                        placeholder = { Text("Jack, Spare wheel, Music system, Tool kit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (plateNumber.isBlank() || vehicleModel.isBlank() || customerName.isBlank() || complaints.isBlank()) {
                                return@Button
                            }

                            val labor = laborCostStr.toDoubleOrNull() ?: 0.0
                            val parts = partsCostStr.toDoubleOrNull() ?: 0.0
                            val total = labor + parts
                            val advance = advanceStr.toDoubleOrNull() ?: 0.0
                            val balance = (total - advance).coerceAtLeast(0.0)

                            val mech = mechanics.firstOrNull { it.id == selectedMechanicId }
                            val mechName = mech?.name ?: (existingJobCard?.assignedMechanicName ?: "")

                            val jobCard = (existingJobCard ?: WorkshopJobCardEntity(
                                businessId = 0L,
                                jobCardNumber = "JC-${System.currentTimeMillis() % 10000}",
                                vehicleId = selectedVehicleId,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                vehiclePlateNumber = plateNumber,
                                vehicleModel = vehicleModel,
                                reportedCustomerComplaints = complaints,
                                receivedDate = receivedDate,
                                promisedDeliveryDate = promisedDeliveryDate
                            )).copy(
                                vehicleId = selectedVehicleId,
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                vehiclePlateNumber = plateNumber.trim().uppercase(),
                                vehicleModel = vehicleModel.trim(),
                                chassisNumber = chassisNumber.trim().uppercase(),
                                currentMileageKm = mileageStr.toIntOrNull() ?: 0,
                                fuelGaugeLevel = fuelGaugeLevel,
                                reportedCustomerComplaints = complaints.trim(),
                                diagnosticNotes = diagnosticNotes.trim(),
                                assignedMechanicId = selectedMechanicId,
                                assignedMechanicName = mechName,
                                bayOrRackNumber = bayOrRackNumber.trim(),
                                status = status,
                                laborCharges = labor,
                                partsEstimatedCost = parts,
                                totalEstimatedCost = total,
                                advanceDeposit = advance,
                                remainingBalance = balance,
                                receivedDate = receivedDate.trim(),
                                promisedDeliveryDate = promisedDeliveryDate.trim(),
                                itemsInventoryChecklist = checklist.trim()
                            )
                            onSave(jobCard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                        modifier = Modifier.testTag("btn_save_job_card")
                    ) {
                        Text("Save Job Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditMechanicDialog(
    existingMechanic: WorkshopMechanicEntity?,
    onDismiss: () -> Unit,
    onSave: (WorkshopMechanicEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingMechanic?.name ?: "") }
    var specialization by remember { mutableStateOf(existingMechanic?.specialization ?: "Master Engine Specialist") }
    var phone by remember { mutableStateOf(existingMechanic?.phone ?: "+92 300 ") }
    var isAvailable by remember { mutableStateOf(existingMechanic?.isAvailable ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (existingMechanic == null) "Add Mechanic / Technician" else "Edit Mechanic",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Mechanic Name *") },
                    placeholder = { Text("e.g. Ustad Tariq Mehmood") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_mechanic_name")
                )

                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialization *") },
                    placeholder = { Text("Auto Electrician / EFI Scanner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Currently Available for Duty")
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || specialization.isBlank()) return@Button
                            val mechanic = (existingMechanic ?: WorkshopMechanicEntity(
                                businessId = 0L,
                                name = name,
                                specialization = specialization
                            )).copy(
                                name = name.trim(),
                                specialization = specialization.trim(),
                                phone = phone.trim(),
                                isAvailable = isAvailable
                            )
                            onSave(mechanic)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                        modifier = Modifier.testTag("btn_save_mechanic")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AssignMechanicDialog(
    jobCard: WorkshopJobCardEntity,
    mechanics: List<WorkshopMechanicEntity>,
    onDismiss: () -> Unit,
    onAssign: (Long, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign Mechanic",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Assign technician for ${jobCard.vehicleModel} (${jobCard.vehiclePlateNumber})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (mechanics.isEmpty()) {
                    Text("No mechanics available. Please register them first.", color = StatusDanger, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mechanics, key = { it.id }) { mechanic ->
                            val isSelected = jobCard.assignedMechanicId == mechanic.id || jobCard.assignedMechanicName == mechanic.name
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) WorkshopOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) BorderStroke(1.5.dp, WorkshopOrange) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAssign(mechanic.id, mechanic.name) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(mechanic.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(mechanic.specialization, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = WorkshopOrange)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun CollectWorkshopBalanceDialog(
    jobCard: WorkshopJobCardEntity,
    pkrFormat: NumberFormat,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double, Double) -> Unit
) {
    var amountToCollectStr by remember { mutableStateOf(jobCard.remainingBalance.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Collect Workshop Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Job Card: ${jobCard.jobCardNumber} • ${jobCard.vehiclePlateNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Bill:", fontSize = 12.sp)
                            Text(pkrFormat.format(jobCard.totalEstimatedCost), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Already Received:", fontSize = 12.sp)
                            Text(pkrFormat.format(jobCard.advanceDeposit), color = StatusSuccess, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Outstanding Balance:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(pkrFormat.format(jobCard.remainingBalance), fontWeight = FontWeight.Bold, color = StatusDanger, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = amountToCollectStr,
                    onValueChange = { amountToCollectStr = it },
                    label = { Text("Amount Collecting Now (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_collect_amount")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val collecting = amountToCollectStr.toDoubleOrNull() ?: 0.0
                            if (collecting <= 0) return@Button
                            val newAdvance = jobCard.advanceDeposit + collecting
                            val newBalance = (jobCard.totalEstimatedCost - newAdvance).coerceAtLeast(0.0)
                            onConfirmPayment(newAdvance, newBalance)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                    ) {
                        Text("Confirm Received", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkshopJobCardSlipDialog(
    jobCard: WorkshopJobCardEntity,
    pkrFormat: NumberFormat,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val slipText = """
        ========================================
             AUTO WORKSHOP & GARAGE JOB CARD
        ========================================
        Job Card #: ${jobCard.jobCardNumber}
        Status:     ${jobCard.status}
        Bay/Rack:   ${jobCard.bayOrRackNumber}
        Received:   ${jobCard.receivedDate}
        Promised:   ${jobCard.promisedDeliveryDate}
        
        ----------------------------------------
        VEHICLE & CUSTOMER DETAILS
        ----------------------------------------
        Registration #: ${jobCard.vehiclePlateNumber}
        Make & Model:   ${jobCard.vehicleModel}
        Chassis (VIN):  ${jobCard.chassisNumber}
        Current ODO:    ${jobCard.currentMileageKm} KM
        Fuel Level:     ${jobCard.fuelGaugeLevel}
        Customer:       ${jobCard.customerName}
        Contact:        ${jobCard.customerPhone}
        
        ----------------------------------------
        REPORTED COMPLAINTS
        ----------------------------------------
        ${jobCard.reportedCustomerComplaints}
        
        DIAGNOSTIC / ACTION:
        ${if (jobCard.diagnosticNotes.isNotBlank()) jobCard.diagnosticNotes else "Under workshop diagnosis"}
        
        ASSIGNED MECHANIC: ${jobCard.assignedMechanicName}
        ITEMS CHECKLIST:   ${jobCard.itemsInventoryChecklist}
        
        ----------------------------------------
        ESTIMATED CHARGES (PKR)
        ----------------------------------------
        Labor Charges:     ${pkrFormat.format(jobCard.laborCharges)}
        Parts Estimate:    ${pkrFormat.format(jobCard.partsEstimatedCost)}
        Total Estimate:    ${pkrFormat.format(jobCard.totalEstimatedCost)}
        Advance Received:  ${pkrFormat.format(jobCard.advanceDeposit)}
        Remaining Balance: ${pkrFormat.format(jobCard.remainingBalance)}
        ========================================
        Customer copy • PakBusiness Pro Workshop
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Job Card Slip",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = slipText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Workshop Job Card - ${jobCard.vehiclePlateNumber}")
                                putExtra(Intent.EXTRA_TEXT, slipText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Job Card via WhatsApp / Message"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkshopOrange),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// REUSABLE HELPER COMPONENTS
// -----------------------------------------------------------------------------------------

@Composable
fun StatPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
