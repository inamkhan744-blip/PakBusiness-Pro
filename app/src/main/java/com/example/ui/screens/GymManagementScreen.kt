package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import com.example.ui.components.BarcodeScannerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GymCheckInEntity
import com.example.data.GymLockerEntity
import com.example.data.GymMemberEntity
import com.example.data.GymPaymentEntity
import com.example.ui.GymKpis
import com.example.ui.GymManagementViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymManagementScreen(
    viewModel: GymManagementViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val kpis by viewModel.kpis.collectAsState()
    val members by viewModel.filteredMembers.collectAsState()
    val checkIns by viewModel.todayCheckIns.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val lockers by viewModel.lockers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var feePaymentTargetMember by remember { mutableStateOf<GymMemberEntity?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var showQuickCheckInDialog by remember { mutableStateOf(false) }
    var attendanceToastMessage by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("Members & Dues", "Live Attendance", "Lockers & PT", "Payment History")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("gym_management_screen"),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_gym_member_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top KPI Banner
            GymHeaderKpis(kpis = kpis)

            // Attendance Toast banner if present
            attendanceToastMessage?.let { msg ->
                Surface(
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { attendanceToastMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Members Tab
                    MembersTabContent(
                        members = members,
                        searchQuery = searchQuery,
                        filterStatus = filterStatus,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setFilterStatus(it) },
                        onToggleCheckIn = { viewModel.toggleCheckIn(it) },
                        onCollectFee = { feePaymentTargetMember = it },
                        onDelete = { viewModel.deleteMember(it.id) }
                    )
                }
                1 -> {
                    // Live Attendance Tab
                    AttendanceTabContent(
                        checkIns = checkIns,
                        allMembers = members,
                        onToggleMemberCheckIn = { viewModel.toggleCheckIn(it) },
                        onOpenCheckInDialog = { showQuickCheckInDialog = true },
                        onOpenScanner = { showScannerDialog = true }
                    )
                }
                2 -> {
                    // Lockers & Packages
                    LockersTabContent(
                        lockers = lockers,
                        onAssign = { locker, memberId, name, fee ->
                            viewModel.assignLocker(locker, memberId, name, fee)
                        }
                    )
                }
                3 -> {
                    // Payment Ledger History
                    PaymentHistoryTabContent(payments = payments)
                }
            }
        }
    }

    // Barcode / QR Scanner Dialog
    if (showScannerDialog) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                viewModel.checkInByQuery(barcode) { result ->
                    attendanceToastMessage = result
                }
                showScannerDialog = false
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Quick Member Check-in Dialog
    if (showQuickCheckInDialog) {
        QuickCheckInMemberDialog(
            members = members,
            onDismiss = { showQuickCheckInDialog = false },
            onToggleCheckIn = { member ->
                viewModel.toggleCheckIn(member)
                val action = if (member.isCheckedIn) "Checked Out" else "Checked In"
                attendanceToastMessage = "Successfully $action: ${member.name}"
                showQuickCheckInDialog = false
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AddGymMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, phone, gender, plan, days, amount, admission, goal, workout, diet, locker ->
                viewModel.saveMember(
                    name = name,
                    phone = phone,
                    gender = gender,
                    plan = plan,
                    durationDays = days,
                    amountPkr = amount,
                    admissionFeePkr = admission,
                    fitnessGoal = goal,
                    workoutPlan = workout,
                    dietPlan = diet,
                    lockerNumber = locker
                )
                showAddMemberDialog = false
            }
        )
    }

    // Collect Fee Dialog
    feePaymentTargetMember?.let { target ->
        CollectGymFeeDialog(
            member = target,
            onDismiss = { feePaymentTargetMember = null },
            onConfirmPayment = { amount, method, days ->
                viewModel.collectFeePayment(
                    memberId = target.id,
                    memberName = target.name,
                    amountPkr = amount,
                    paymentMethod = method,
                    additionalDays = days
                )
                feePaymentTargetMember = null
            }
        )
    }
}

@Composable
private fun GymHeaderKpis(kpis: GymKpis) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gym & Fitness Studio Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KpiPill("Total", "${kpis.totalMembers}", Icons.Default.People)
                KpiPill("Active", "${kpis.activeMembers}", Icons.Default.CheckCircle, Color(0xFF2E7D32))
                KpiPill("Inside Gym", "${kpis.currentlyInGym}", Icons.Default.FitnessCenter, Color(0xFF1565C0))
                KpiPill("Revenue", "PKR ${kpis.monthlyRevenuePkr.toInt()}", Icons.Default.Payment, Color(0xFFC2185B))
            }
        }
    }
}

@Composable
private fun KpiPill(label: String, value: String, icon: ImageVector, iconColor: Color = Color.DarkGray) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun MembersTabContent(
    members: List<GymMemberEntity>,
    searchQuery: String,
    filterStatus: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleCheckIn: (GymMemberEntity) -> Unit,
    onCollectFee: (GymMemberEntity) -> Unit,
    onDelete: (GymMemberEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by name, phone, or locker...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "ACTIVE", "EXPIRING_SOON", "EXPIRED", "DEFAULTER").forEach { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = { onFilterChange(status) },
                    label = { Text(status.replace("_", " "), fontSize = 11.sp) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(members) { member ->
                GymMemberItemCard(
                    member = member,
                    onToggleCheckIn = { onToggleCheckIn(member) },
                    onCollectFee = { onCollectFee(member) }
                )
            }
        }
    }
}

@Composable
private fun GymMemberItemCard(
    member: GymMemberEntity,
    onToggleCheckIn: () -> Unit,
    onCollectFee: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (member.isCheckedIn) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (member.isCheckedIn) Icons.Default.FitnessCenter else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (member.isCheckedIn) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${member.phone} • ${member.gender}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        member.isCheckedIn -> Color(0xFFC8E6C9)
                        member.isExpired -> Color(0xFFFFCDD2)
                        member.isExpiringSoon -> Color(0xFFFFF9C4)
                        else -> Color(0xFFE1F5FE)
                    }
                ) {
                    Text(
                        text = when {
                            member.isCheckedIn -> "INSIDE GYM"
                            member.isExpired -> "EXPIRED"
                            member.isExpiringSoon -> "${member.remainingDays}d LEFT"
                            else -> "ACTIVE"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            member.isCheckedIn -> Color(0xFF2E7D32)
                            member.isExpired -> Color(0xFFC62828)
                            member.isExpiringSoon -> Color(0xFFF57F17)
                            else -> Color(0xFF0277BD)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row: Package, Goal, Workout, Locker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Plan: ${member.plan} (PKR ${member.amountPkr.toInt()})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (member.lockerNumber.isNotBlank()) {
                    Text(
                        text = "Locker: #${member.lockerNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Goal: ${member.fitnessGoal} • Routine: ${member.workoutPlan}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (member.pendingDuePkr > 0.0) {
                    Text(
                        text = "Due: PKR ${member.pendingDuePkr.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleCheckIn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (member.isCheckedIn) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(if (member.isCheckedIn) "Check-Out" else "Check-In", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onCollectFee,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Collect Fee", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AttendanceTabContent(
    checkIns: List<GymCheckInEntity>,
    allMembers: List<GymMemberEntity>,
    onToggleMemberCheckIn: (GymMemberEntity) -> Unit,
    onOpenCheckInDialog: () -> Unit,
    onOpenScanner: () -> Unit
) {
    var attendanceFilter by remember { mutableStateOf("ALL") }

    val activeCount = checkIns.count { it.checkOutTime == null }
    val completedCount = checkIns.count { it.checkOutTime != null }

    val filteredLogs = when (attendanceFilter) {
        "ACTIVE" -> checkIns.filter { it.checkOutTime == null }
        "COMPLETED" -> checkIns.filter { it.checkOutTime != null }
        else -> checkIns
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Quick Action Card with Check-in Button & Barcode Scanner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Member Attendance & Floor Access",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Track member entry timestamps, duration, and checkout",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenCheckInDialog,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("check_in_member_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check-In Member", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("scan_attendance_qr_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan QR", fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Today's Attendance Report Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Attendance Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${checkIns.size} check-ins logged today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                Text(
                    text = "$activeCount ON FLOOR",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Filter chips: All, Active, Completed
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = attendanceFilter == "ALL",
                onClick = { attendanceFilter = "ALL" },
                label = { Text("All Logs (${checkIns.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = attendanceFilter == "ACTIVE",
                onClick = { attendanceFilter = "ACTIVE" },
                label = { Text("Active on Floor ($activeCount)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = attendanceFilter == "COMPLETED",
                onClick = { attendanceFilter = "COMPLETED" },
                label = { Text("Completed ($completedCount)", fontSize = 11.sp) }
            )
        }

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No attendance records match the selected filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { item ->
                    val memberObj = allMembers.find { it.id == item.memberId }
                    val isStillInside = item.checkOutTime == null
                    val now = System.currentTimeMillis()
                    val durationMillis = (item.checkOutTime ?: now) - item.checkInTime
                    val durationMinutes = (durationMillis / (1000 * 60)).coerceAtLeast(1)
                    val durationHours = durationMinutes / 60
                    val durationRemainderMinutes = durationMinutes % 60
                    val durationStr = if (durationHours > 0) "${durationHours}h ${durationRemainderMinutes}m" else "${durationMinutes} mins"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (isStillInside) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isStillInside) Icons.Default.FitnessCenter else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isStillInside) Color(0xFF2E7D32) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    val inTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.checkInTime))
                                    val outTime = item.checkOutTime?.let {
                                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                                    } ?: "Active Now"
                                    Text(
                                        text = "In: $inTime • Out: $outTime",
                                        fontSize = 11.sp,
                                        color = if (isStillInside) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Workout Time: $durationStr", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }

                            if (isStillInside && memberObj != null) {
                                Button(
                                    onClick = { onToggleMemberCheckIn(memberObj) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.height(32.dp).testTag("checkout_member_btn_${item.memberId}")
                                ) {
                                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check-Out", fontSize = 11.sp)
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = "Completed",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCheckInMemberDialog(
    members: List<GymMemberEntity>,
    onDismiss: () -> Unit,
    onToggleCheckIn: (GymMemberEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = members.filter {
        search.isBlank() || it.name.contains(search, ignoreCase = true) || it.phone.contains(search, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(480.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Member to Check-In / Out", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search by name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { m ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${m.plan} • ${if (m.isCheckedIn) "Inside Gym" else "Outside"}", fontSize = 11.sp, color = if (m.isCheckedIn) Color(0xFF2E7D32) else Color.Gray)
                                }
                                Button(
                                    onClick = { onToggleCheckIn(m) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (m.isCheckedIn) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                    ),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (m.isCheckedIn) "Check-Out" else "Check-In", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun LockersTabContent(
    lockers: List<GymLockerEntity>,
    onAssign: (String, Long?, String, Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gym Locker Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Assign individual secure lockers to annual and monthly VIP members", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val sampleLockers = if (lockers.isEmpty()) {
                (1..12).map { num ->
                    GymLockerEntity(id = num.toLong(), lockerNumber = "$num", isOccupied = num % 3 == 0, assignedMemberName = if (num % 3 == 0) "Member #$num" else "")
                }
            } else lockers

            items(sampleLockers) { l ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (l.isOccupied) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (l.isOccupied) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Locker #${l.lockerNumber}", fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (l.isOccupied) "Occupied: ${l.assignedMemberName}" else "Available (PKR ${l.monthlyFeePkr.toInt()}/mo)",
                                    fontSize = 11.sp,
                                    color = if (l.isOccupied) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryTabContent(payments: List<GymPaymentEntity>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Fee Payments & Receipts Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (payments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No fee payments recorded yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { p ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(p.memberName, fontWeight = FontWeight.Bold)
                                Text("${p.paymentType} • ${p.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PKR ${p.amountPkr.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(p.paymentDate))
                                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGymMemberDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int, Double, Double, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var plan by remember { mutableStateOf("Monthly Standard") }
    var amount by remember { mutableStateOf("3000") }
    var admission by remember { mutableStateOf("1000") }
    var goal by remember { mutableStateOf("Weight Loss") }
    var workout by remember { mutableStateOf("Push-Pull-Legs") }
    var diet by remember { mutableStateOf("High Protein") }
    var locker by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Add New Gym Member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Member Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { g ->
                            FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                        }
                    }
                }
                item {
                    OutlinedTextField(value = plan, onValueChange = { plan = it }, label = { Text("Membership Package") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Monthly Fee (PKR)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = admission, onValueChange = { admission = it }, label = { Text("Admission Fee (PKR)") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Fitness Goal (e.g. Muscle Gain, Fat Loss)") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = workout, onValueChange = { workout = it }, label = { Text("Workout Split (e.g. PPL, Upper-Lower)") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = diet, onValueChange = { diet = it }, label = { Text("Diet Plan Notes") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = locker, onValueChange = { locker = it }, label = { Text("Locker Number (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(name, phone, gender, plan, 30, amount.toDoubleOrNull() ?: 3000.0, admission.toDoubleOrNull() ?: 1000.0, goal, workout, diet, locker)
                                }
                            }
                        ) {
                            Text("Save Member")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectGymFeeDialog(
    member: GymMemberEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double, String, Int) -> Unit
) {
    var amount by remember { mutableStateOf(member.amountPkr.toInt().toString()) }
    var method by remember { mutableStateOf("Cash") }
    var days by remember { mutableStateOf("30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Collect Membership Fee", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Member: ${member.name}", fontWeight = FontWeight.SemiBold)
                Text("Plan: ${member.plan} • Current Expiry: ${if (member.isExpired) "Expired" else "${member.remainingDays} days left"}", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount Paid (PKR)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Extend Validity (Days)") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "EasyPaisa", "JazzCash").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: member.amountPkr
                            val d = days.toIntOrNull() ?: 30
                            onConfirmPayment(amt, method, d)
                        }
                    ) {
                        Text("Record & Renew")
                    }
                }
            }
        }
    }
}
