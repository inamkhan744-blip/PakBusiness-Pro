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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import java.util.UUID

data class SalesStaffMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val monthlyTarget: Double,
    var achievedSales: Double,
    val commissionRatePercent: Double,
    val baseSalary: Double
) {
    val progress: Float get() = if (monthlyTarget > 0) (achievedSales / monthlyTarget).toFloat().coerceIn(0f, 1f) else 0f
    val earnedCommission: Double get() = (achievedSales * commissionRatePercent) / 100.0
    val totalTakeHome: Double get() = baseSalary + earnedCommission
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCommissionScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"
    val businessName = business?.name ?: "PakBusiness Store"

    var showAddSaleDialog by remember { mutableStateOf(false) }
    var selectedStaffForSale by remember { mutableStateOf<SalesStaffMember?>(null) }

    val staffMembers = remember {
        mutableStateListOf(
            SalesStaffMember(
                name = "Muhammad Hamza",
                role = "Senior Sales Executive",
                monthlyTarget = 1000000.0,
                achievedSales = 850000.0,
                commissionRatePercent = 2.5,
                baseSalary = 45000.0
            ),
            SalesStaffMember(
                name = "Waqas Ahmed",
                role = "Counter Salesman",
                monthlyTarget = 600000.0,
                achievedSales = 620000.0,
                commissionRatePercent = 2.0,
                baseSalary = 35000.0
            ),
            SalesStaffMember(
                name = "Ali Raza",
                role = "Retail Associate",
                monthlyTarget = 500000.0,
                achievedSales = 340000.0,
                commissionRatePercent = 1.5,
                baseSalary = 30000.0
            ),
            SalesStaffMember(
                name = "Zeeshan Ali",
                role = "Junior Sales Rep",
                monthlyTarget = 400000.0,
                achievedSales = 180000.0,
                commissionRatePercent = 1.5,
                baseSalary = 25000.0
            )
        )
    }

    val totalTeamSales = staffMembers.sumOf { it.achievedSales }
    val totalCommissions = staffMembers.sumOf { it.earnedCommission }
    val totalTeamTarget = staffMembers.sumOf { it.monthlyTarget }

    fun shareLeaderboardWhatsApp() {
        val leaderboardRows = staffMembers
            .sortedByDescending { it.achievedSales }
            .mapIndexed { idx, s ->
                val medal = when (idx) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "⭐"
                }
                "$medal ${s.name} (${s.role})\n   Sales: $currency ${String.format("%,.0f", s.achievedSales)} / Target: ${String.format("%,.0f", s.monthlyTarget)} (${(s.progress * 100).toInt()}%)\n   Earned Commission: $currency ${String.format("%,.0f", s.earnedCommission)}"
            }.joinToString("\n\n")

        val summary = """
*🏆 MONTHLY SALES TARGET & COMMISSION LEADERBOARD*
🏪 *${businessName}*
----------------------------------------
👥 *Total Team Sales:* $currency ${String.format("%,.0f", totalTeamSales)}
🎯 *Target Achievement:* ${if (totalTeamTarget > 0) ((totalTeamSales / totalTeamTarget) * 100).toInt() else 0}%
💰 *Total Commission Pool:* $currency ${String.format("%,.0f", totalCommissions)}

*STAFF RANKINGS:*
$leaderboardRows
----------------------------------------
_Well done team! Keep selling!_
_Generated via PakBusiness Suite_
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(intent, "Share Sales Leaderboard via WhatsApp"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Staff Commission & Targets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "سیلز کمیشن، ٹارگٹ اور لیڈر بورڈ",
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
                    IconButton(onClick = { shareLeaderboardWhatsApp() }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Team Performance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Monthly Sales Target", color = Color(0xFFA7F3D0), fontSize = 12.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = PakGoldSecondary) {
                                Text("TEAM PERFORMANCE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text(
                            text = "$currency ${String.format("%,.0f", totalTeamSales)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val overallProgress = if (totalTeamTarget > 0) (totalTeamSales / totalTeamTarget).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PakGoldSecondary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Target: $currency ${String.format("%,.0f", totalTeamTarget)}", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                            Text("${(overallProgress * 100).toInt()}% Done", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Metric Counters
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Active Sellers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${staffMembers.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Commission Pool", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format("%,.0f", totalCommissions)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Top Performer", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(staffMembers.maxByOrNull { it.achievedSales }?.name?.split(" ")?.firstOrNull() ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "🏆 Sales Leaderboard & Commissions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Staff Leaderboard Cards
            val sortedStaff = staffMembers.sortedByDescending { it.achievedSales }
            itemsIndexed(sortedStaff, key = { _, s -> s.id }) { index, staff ->
                val medalColor = when (index) {
                    0 -> Color(0xFFFFD700) // Gold
                    1 -> Color(0xFFC0C0C0) // Silver
                    2 -> Color(0xFFCD7F32) // Bronze
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = medalColor.copy(alpha = 0.25f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (index < 3) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Column {
                                    Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(staff.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Commission (${staff.commissionRatePercent}%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$currency ${String.format("%,.0f", staff.earnedCommission)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { staff.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (staff.progress >= 1f) Color(0xFF16A34A) else PakEmeraldPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sales: $currency ${String.format("%,.0f", staff.achievedSales)} / ${String.format("%,.0f", staff.monthlyTarget)}", fontSize = 11.sp)
                            Text("${(staff.progress * 100).toInt()}% Achieved", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (staff.progress >= 1f) Color(0xFF16A34A) else PakEmeraldPrimary)
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Take-Home (Salary + Comm): $currency ${String.format("%,.0f", staff.totalTakeHome)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)

                            Button(
                                onClick = {
                                    selectedStaffForSale = staff
                                    showAddSaleDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Deal", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Sales Deal Dialog
    if (showAddSaleDialog && selectedStaffForSale != null) {
        var dealAmountText by remember { mutableStateOf("") }
        val staff = selectedStaffForSale!!

        AlertDialog(
            onDismissRequest = { showAddSaleDialog = false },
            title = { Text("Credit Sale Deal: ${staff.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter value of closed deal to credit toward monthly target and commission (${staff.commissionRatePercent}%).", fontSize = 11.sp)
                    OutlinedTextField(
                        value = dealAmountText,
                        onValueChange = { dealAmountText = it },
                        label = { Text("Deal / Sale Amount ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dealVal = dealAmountText.toDoubleOrNull() ?: 0.0
                        if (dealVal > 0) {
                            staff.achievedSales += dealVal
                            showAddSaleDialog = false
                            Toast.makeText(context, "Rs. ${String.format("%,.0f", dealVal)} credited to ${staff.name}!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Record Sale")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSaleDialog = false }) { Text("Cancel") }
            }
        )
    }
}
