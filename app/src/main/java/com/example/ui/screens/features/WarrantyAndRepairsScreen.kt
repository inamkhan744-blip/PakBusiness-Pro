package com.example.ui.screens.features

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RepairStatus(val label: String, val urduLabel: String, val color: Color) {
    RECEIVED("Device Received", "وصول شدہ", Color(0xFFD97706)),
    IN_PROGRESS("In Progress / Testing", "کام جاری ہے", Color(0xFF0284C7)),
    READY("Ready for Pickup", "تیار ہے", Color(0xFF16A34A)),
    DELIVERED("Delivered to Customer", "ڈیلیور ہو گیا", Color(0xFF6B7280))
}

data class RepairJobSheet(
    val id: String = UUID.randomUUID().toString(),
    val tokenNumber: String,
    val deviceName: String,
    val serialOrImei: String,
    val customerName: String,
    val customerPhone: String,
    val issueReported: String,
    val estimatedCost: Double,
    val advancePaid: Double = 0.0,
    val dateReceived: String,
    var status: RepairStatus,
    val technicianNotes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyAndRepairsScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"
    val businessName = business?.name ?: "PakBusiness Tech"

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateJobDialog by remember { mutableStateOf(false) }

    val jobs = remember {
        mutableStateListOf(
            RepairJobSheet(
                tokenNumber = "JOB-1021",
                deviceName = "Samsung Galaxy S24 Ultra",
                serialOrImei = "358902123456789",
                customerName = "Bilal Farooq",
                customerPhone = "0300-4441122",
                issueReported = "Display flickering, glass cracked",
                estimatedCost = 35000.0,
                advancePaid = 10000.0,
                dateReceived = "12 Oct 2026",
                status = RepairStatus.IN_PROGRESS,
                technicianNotes = "Awaiting original AMOLED screen panel"
            ),
            RepairJobSheet(
                tokenNumber = "JOB-1022",
                deviceName = "Inverex Solar Inverter 3.2kW",
                serialOrImei = "INV-2026-X99",
                customerName = "Haji Usman Ghani",
                customerPhone = "0321-7778899",
                issueReported = "Error 09 - MOSFET replacement needed",
                estimatedCost = 14500.0,
                advancePaid = 5000.0,
                dateReceived = "13 Oct 2026",
                status = RepairStatus.READY,
                technicianNotes = "Tested on 10A load for 3 hours. 100% OK"
            ),
            RepairJobSheet(
                tokenNumber = "JOB-1023",
                deviceName = "HP EliteBook G8 Laptop",
                serialOrImei = "5CD1234ABC",
                customerName = "Dr. Samina Khan",
                customerPhone = "0333-2223344",
                issueReported = "Thermal throttling, fans noisy, SSD upgrade",
                estimatedCost = 18000.0,
                advancePaid = 18000.0,
                dateReceived = "10 Oct 2026",
                status = RepairStatus.DELIVERED,
                technicianNotes = "Installed 1TB NVMe + thermal paste renewed"
            )
        )
    }

    val filteredJobs = remember(selectedTabIndex, searchQuery, jobs.toList()) {
        val list = when (selectedTabIndex) {
            1 -> jobs.filter { it.status == RepairStatus.RECEIVED || it.status == RepairStatus.IN_PROGRESS }
            2 -> jobs.filter { it.status == RepairStatus.READY }
            3 -> jobs.filter { it.status == RepairStatus.DELIVERED }
            else -> jobs
        }
        if (searchQuery.isBlank()) list
        else list.filter {
            it.tokenNumber.contains(searchQuery, ignoreCase = true) ||
            it.serialOrImei.contains(searchQuery, ignoreCase = true) ||
            it.customerName.contains(searchQuery, ignoreCase = true) ||
            it.deviceName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun shareJobSheetWhatsApp(job: RepairJobSheet) {
        val msg = """
*🔧 REPAIR JOB SHEET / جاب کارڈ*
🏪 *${businessName}*
----------------------------------
🎫 *Token No:* ${job.tokenNumber}
📱 *Device:* ${job.deviceName}
🔢 *Serial / IMEI:* ${job.serialOrImei}
👤 *Customer:* ${job.customerName}
📅 *Date Received:* ${job.dateReceived}
⚠️ *Problem Reported:* ${job.issueReported}
----------------------------------
💰 *Estimated Cost:* $currency ${String.format("%,.0f", job.estimatedCost)}
💵 *Advance Paid:* $currency ${String.format("%,.0f", job.advancePaid)}
📌 *Current Status:* ${job.status.label} (${job.status.urduLabel})
----------------------------------
_Please keep your token number for device collection._
_Thank you for choosing ${businessName}_
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }
        context.startActivity(Intent.createChooser(intent, "Share Job Sheet on WhatsApp"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Warranty & Repair Job Sheets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "وارنٹی، سیریل / IMEI و ریپئرنگ جاب کارڈ",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PakEmeraldDark),
                actions = {
                    Button(
                        onClick = { showCreateJobDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Job Sheet", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Token #, IMEI, Serial, Device or Customer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("All (${jobs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("In Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Ready (${jobs.count { it.status == RepairStatus.READY }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 3, onClick = { selectedTabIndex = 3 }, text = { Text("Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredJobs, key = { it.id }) { job ->
                    RepairJobCard(
                        job = job,
                        currency = currency,
                        onUpdateStatus = { newStatus ->
                            job.status = newStatus
                            Toast.makeText(context, "Job #${job.tokenNumber} status updated to ${newStatus.label}", Toast.LENGTH_SHORT).show()
                        },
                        onShare = { shareJobSheetWhatsApp(job) },
                        onDelete = { jobs.remove(job) }
                    )
                }
            }
        }
    }

    if (showCreateJobDialog) {
        CreateJobSheetDialog(
            currency = currency,
            onDismiss = { showCreateJobDialog = false },
            onSave = { newJob ->
                jobs.add(0, newJob)
                showCreateJobDialog = false
            }
        )
    }
}

@Composable
fun RepairJobCard(
    job: RepairJobSheet,
    currency: String,
    onUpdateStatus: (RepairStatus) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        shape = CircleShape,
                        color = PakEmeraldContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(job.tokenNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(job.deviceName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = job.status.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = job.status.urduLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = job.status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Customer: ${job.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Phone: ${job.customerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Serial / IMEI: ${job.serialOrImei}", fontSize = 10.sp, color = PakEmeraldPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Estimated Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currency ${String.format("%,.0f", job.estimatedCost)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PakEmeraldPrimary)
                    if (job.advancePaid > 0) {
                        Text("Advance: $currency ${String.format("%,.0f", job.advancePaid)}", fontSize = 10.sp, color = Color(0xFF16A34A))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Fault Reported: ${job.issueReported}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    if (job.technicianNotes.isNotBlank()) {
                        Text("Technician Note: ${job.technicianNotes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Quick Status Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (job.status == RepairStatus.RECEIVED) {
                    Button(
                        onClick = { onUpdateStatus(RepairStatus.IN_PROGRESS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Start Work", fontSize = 10.sp)
                    }
                }
                if (job.status == RepairStatus.IN_PROGRESS) {
                    Button(
                        onClick = { onUpdateStatus(RepairStatus.READY) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Mark Ready", fontSize = 10.sp)
                    }
                }
                if (job.status == RepairStatus.READY) {
                    Button(
                        onClick = { onUpdateStatus(RepairStatus.DELIVERED) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Delivered", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 10.sp, color = Color.White)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CreateJobSheetDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (RepairJobSheet) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var serialOrImei by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var issueReported by remember { mutableStateOf("") }
    var estimatedCostText by remember { mutableStateOf("") }
    var advanceText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Repair Job Sheet / Token", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Device / Product Model") },
                    placeholder = { Text("e.g. Haier DC Inverter 1.5 Ton") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = serialOrImei,
                    onValueChange = { serialOrImei = it },
                    label = { Text("Serial / IMEI / Chassis #") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = issueReported,
                    onValueChange = { issueReported = it },
                    label = { Text("Fault / Problem Reported") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estimatedCostText,
                        onValueChange = { estimatedCostText = it },
                        label = { Text("Est. Cost ($currency)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = advanceText,
                        onValueChange = { advanceText = it },
                        label = { Text("Advance ($currency)") },
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
                    if (deviceName.isNotBlank() && customerName.isNotBlank()) {
                        val token = "JOB-${SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault()).format(Date())}"
                        val job = RepairJobSheet(
                            tokenNumber = token,
                            deviceName = deviceName.trim(),
                            serialOrImei = serialOrImei.trim(),
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            issueReported = issueReported.trim(),
                            estimatedCost = estimatedCostText.toDoubleOrNull() ?: 0.0,
                            advancePaid = advanceText.toDoubleOrNull() ?: 0.0,
                            dateReceived = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                            status = RepairStatus.RECEIVED
                        )
                        onSave(job)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Generate Job Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
