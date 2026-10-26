package com.example.ui.screens.gym

import android.content.Intent
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GymLockerEntity
import com.example.data.GymMemberEntity
import com.example.data.GymPaymentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

@Composable
fun GymFeesAndLockersTabView(
    members: List<GymMemberEntity>,
    payments: List<GymPaymentEntity>,
    lockers: List<GymLockerEntity>,
    onCollectPayment: (memberId: Long, memberName: String, amount: Double, paymentType: String, paymentMethod: String, additionalDays: Int) -> Unit,
    onSaveLocker: (GymLockerEntity) -> Unit,
    onDeleteLocker: (Long) -> Unit,
    onMemberClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subSection by remember { mutableStateOf(0) } // 0: Fees & Defaulters, 1: Lockers Grid, 2: Workout & Diet Notes

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedMemberForPayment by remember { mutableStateOf<GymMemberEntity?>(null) }
    var paymentAmountText by remember { mutableStateOf("3000") }
    var selectedPaymentMode by remember { mutableStateOf("Cash") }
    var selectedRenewalDuration by remember { mutableStateOf(30) }

    var showLockerDialog by remember { mutableStateOf(false) }
    var selectedLockerToAssign by remember { mutableStateOf<GymLockerEntity?>(null) }
    var lockerAssignMemberName by remember { mutableStateOf("") }

    val expiredOrExpiring = remember(members) { members.filter { it.isExpired || it.isExpiringSoon || it.pendingDuePkr > 0.0 } }
    val totalRevenueCollected = remember(payments) { payments.sumOf { it.amountPkr } }
    val totalPendingDues = remember(members) { members.sumOf { it.pendingDuePkr } }

    Column(modifier = modifier.fillMaxSize().testTag("gym_fees_lockers_tab")) {
        // Sub-tabs switch
        TabRow(
            selectedTabIndex = subSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PakEmeraldPrimary
        ) {
            Tab(
                selected = subSection == 0,
                onClick = { subSection = 0 },
                text = { Text("Fees & Defaulters", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = subSection == 1,
                onClick = { subSection = 1 },
                text = { Text("Lockers (${lockers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = subSection == 2,
                onClick = { subSection = 2 },
                text = { Text("Workout & Diet", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (subSection) {
            0 -> {
                // Fees & Defaulters List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Revenue and Dues KPI
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Collected Revenue", fontSize = 11.sp, color = PakEmeraldDark)
                                    Text("PKR ${totalRevenueCollected.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                                    Text("${payments.size} fee receipts", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Pending Dues", fontSize = 11.sp, color = Color(0xFFC62828))
                                    Text("PKR ${totalPendingDues.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    Text("${expiredOrExpiring.size} members need renewal", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Members Requiring Renewal & Fee Collection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (expiredOrExpiring.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("All member subscriptions are currently active and up to date!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(expiredOrExpiring) { member ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().clickable { onMemberClick(member.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${member.plan} • ${member.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (member.isExpired) "Expired on ${GymDateUtils.formatDate(member.expiryDate)}" else "${member.remainingDays} days remaining",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (member.isExpired) Color(0xFFC62828) else Color(0xFFE65100)
                                        )
                                        if (member.pendingDuePkr > 0.0) {
                                            Text("Pending Due: PKR ${member.pendingDuePkr.toInt()}", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            selectedMemberForPayment = member
                                            paymentAmountText = member.amountPkr.toInt().toString()
                                            showPaymentDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Collect Fee", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Payment History
                    item {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Recent Fee Payment Receipts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(payments.take(15)) { p ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(p.memberName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${p.paymentType} • ${p.paymentMethod} • ${GymDateUtils.formatDate(p.paymentDate)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "PKR ${p.amountPkr.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Lockers Grid
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Locker Management", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Assign secure locker keys to gym members", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    val nextNum = (lockers.size + 1).toString().padStart(2, '0')
                                    onSaveLocker(GymLockerEntity(lockerNumber = "L-$nextNum", isOccupied = false))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Locker", fontSize = 11.sp)
                            }
                        }
                    }

                    if (lockers.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("No lockers generated yet", fontWeight = FontWeight.Medium)
                                    Button(
                                        onClick = {
                                            for (i in 1..20) {
                                                val num = i.toString().padStart(2, '0')
                                                onSaveLocker(GymLockerEntity(lockerNumber = "L-$num", isOccupied = false))
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                                    ) {
                                        Text("Generate 20 Lockers (L-01 to L-20)")
                                    }
                                }
                            }
                        }
                    } else {
                        items(lockers.chunked(2)) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { l ->
                                    Card(
                                        modifier = Modifier.weight(1f).clickable {
                                            selectedLockerToAssign = l
                                            lockerAssignMemberName = l.assignedMemberName
                                            showLockerDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (l.isOccupied) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(l.lockerNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Icon(
                                                    imageVector = if (l.isOccupied) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = null,
                                                    tint = if (l.isOccupied) Color(0xFFC62828) else Color(0xFF2E7D32),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (l.isOccupied) "Occupied: ${l.assignedMemberName}" else "Available (Free)",
                                                fontSize = 11.sp,
                                                color = if (l.isOccupied) Color(0xFFC62828) else Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Workout & Diet Notes
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Member Fitness Goals & Diet Routines",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val membersWithPlans = members.filter { it.workoutPlan.isNotBlank() || it.dietPlan.isNotBlank() || it.fitnessGoal.isNotBlank() }

                    if (membersWithPlans.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No workout or diet plans recorded yet. Add goals when creating or editing a member.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(membersWithPlans) { m ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().clickable { onMemberClick(m.id) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (m.fitnessGoal.isNotBlank()) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = PakGoldContainer) {
                                                Text(m.fitnessGoal, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PakGoldSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    if (m.workoutPlan.isNotBlank()) {
                                        Text("🏋️ Workout: ${m.workoutPlan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (m.dietPlan.isNotBlank()) {
                                        Text("🥗 Diet: ${m.dietPlan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Collect Fee Dialog
    if (showPaymentDialog && selectedMemberForPayment != null) {
        val target = selectedMemberForPayment!!
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Collect Fee: ${target.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Plan: ${target.plan} • Phone: ${target.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Payment Method", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cash", "JazzCash", "EasyPaisa", "Bank").forEach { mode ->
                            FilterChip(
                                selected = selectedPaymentMode == mode,
                                onClick = { selectedPaymentMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Text("Renewal Validity", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30 to "1 Month", 90 to "3 Mos", 180 to "6 Mos").forEach { (days, label) ->
                            FilterChip(
                                selected = selectedRenewalDuration == days,
                                onClick = { selectedRenewalDuration = days },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull() ?: target.amountPkr
                        onCollectPayment(
                            target.id,
                            target.name,
                            amount,
                            "Membership Renewal",
                            selectedPaymentMode,
                            selectedRenewalDuration
                        )
                        showPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Locker Assign / Free Dialog
    if (showLockerDialog && selectedLockerToAssign != null) {
        val l = selectedLockerToAssign!!
        AlertDialog(
            onDismissRequest = { showLockerDialog = false },
            title = { Text("Locker ${l.lockerNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (l.isOccupied) "Current Member: ${l.assignedMemberName}" else "Currently Available")

                    OutlinedTextField(
                        value = lockerAssignMemberName,
                        onValueChange = { lockerAssignMemberName = it },
                        label = { Text("Assign to Member Name") },
                        placeholder = { Text("Enter member name...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isAssigned = lockerAssignMemberName.isNotBlank()
                        onSaveLocker(
                            l.copy(
                                assignedMemberName = lockerAssignMemberName.trim(),
                                isOccupied = isAssigned
                            )
                        )
                        showLockerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text(if (lockerAssignMemberName.isNotBlank()) "Save Assignment" else "Free Locker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
