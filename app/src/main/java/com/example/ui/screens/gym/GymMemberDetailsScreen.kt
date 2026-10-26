package com.example.ui.screens.gym

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GymCheckInEntity
import com.example.data.GymMemberEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymMemberDetailsScreen(
    member: GymMemberEntity,
    checkInsFlow: Flow<List<GymCheckInEntity>>,
    onToggleCheckIn: (GymMemberEntity) -> Unit,
    onEditMember: (GymMemberEntity) -> Unit,
    onDeleteMember: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkIns by checkInsFlow.collectAsState(initial = emptyList())
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Member Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_from_details")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditMember(member) },
                        modifier = Modifier.testTag("btn_edit_gym_member")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = PakEmeraldPrimary)
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("btn_delete_gym_member")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Member", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("gym_member_details_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(
                                        if (member.isCheckedIn) Color(0xFF2E7D32) else PakEmeraldPrimary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.gender} • Joined ${GymDateUtils.formatDate(member.startDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Phone Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = member.phone,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // PRIMARY REQUIREMENT 4: Check-in / Check-out interactive hero section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (member.isCheckedIn) Color(0xFFE8F5E9) else Color(0xFFF1F8E9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (member.isCheckedIn) Color(0xFF2E7D32) else PakEmeraldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("gym_attendance_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            if (member.isCheckedIn) Color(0xFF2E7D32) else Color(0xFF757575),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = if (member.isCheckedIn) "ATTENDANCE: CURRENTLY IN GYM" else "ATTENDANCE: CHECKED OUT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (member.isCheckedIn) Color(0xFF2E7D32) else Color(0xFF424242)
                                )
                            }

                            if (member.isCheckedIn && member.lastCheckInTime != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (member.isCheckedIn) {
                                "Member checked in at ${GymDateUtils.formatTime(member.lastCheckInTime ?: System.currentTimeMillis())}. Tap below when session ends."
                            } else {
                                "Member is not currently logged into the gym. Tap below to register today's attendance."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Main Check-in / Check-out Button
                        Button(
                            onClick = { onToggleCheckIn(member) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (member.isCheckedIn) Color(0xFFC62828) else PakEmeraldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_member_checkin_checkout_action")
                        ) {
                            Icon(
                                imageVector = if (member.isCheckedIn) Icons.Default.Logout else Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (member.isCheckedIn) "CHECK OUT NOW" else "CHECK IN NOW",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Membership & Subscription Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = member.plan,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.durationDays} Days Plan Duration",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Status Tag
                            val statusText = when {
                                member.isExpired -> "EXPIRED"
                                member.isExpiringSoon -> "EXPIRING SOON"
                                else -> "ACTIVE"
                            }
                            val statusBg = when {
                                member.isExpired -> Color(0xFFFFEBEE)
                                member.isExpiringSoon -> Color(0xFFFFF3E0)
                                else -> PakEmeraldContainer
                            }
                            val statusColor = when {
                                member.isExpired -> Color(0xFFC62828)
                                member.isExpiringSoon -> Color(0xFFE65100)
                                else -> PakEmeraldPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = statusText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Details grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Start Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(GymDateUtils.formatDate(member.startDate), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Expiry Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(GymDateUtils.formatDate(member.expiryDate), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (member.isExpired) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${member.remainingDays} Days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Fee Paid (PKR):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = GymDateUtils.formatCurrency(member.amountPkr),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Facility, Locker & Fitness Profile Card
            item {
                val context = LocalContext.current
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("gym_member_fitness_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Fitness Profile & Locker",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (member.lockerNumber.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PakGoldContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = PakGoldSecondary, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "Locker: ${member.lockerNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PakGoldSecondary
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Goal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (member.fitnessGoal.isNotBlank()) member.fitnessGoal else "General Fitness",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary
                                )
                            }
                            Column {
                                Text("Admission Fee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = GymDateUtils.formatCurrency(member.admissionFeePkr),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Pending Dues", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = GymDateUtils.formatCurrency(member.pendingDuePkr),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.pendingDuePkr > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Workout Split
                        if (member.workoutPlan.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🏋 Workout Routine:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PakEmeraldDark)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(member.workoutPlan, fontSize = 12.sp)
                                }
                            }
                        }

                        // Diet Recommendation
                        if (member.dietPlan.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🥗 Nutrition / Diet Plan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PakEmeraldDark)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(member.dietPlan, fontSize = 12.sp)
                                }
                            }
                        }

                        // WhatsApp Share Routine Button
                        Button(
                            onClick = {
                                val routineText = """
                                    🏋 *GYM WORKOUT & DIET PLAN*
                                    👤 *Member:* ${member.name}
                                    🎯 *Goal:* ${if (member.fitnessGoal.isNotBlank()) member.fitnessGoal else "Fitness"}
                                    ${if (member.lockerNumber.isNotBlank()) "🔐 *Locker #:* " + member.lockerNumber else ""}
                                    ----------------------------
                                    ${if (member.workoutPlan.isNotBlank()) "🏋 *Workout Split:*\n" + member.workoutPlan + "\n----------------------------\n" else ""}
                                    ${if (member.dietPlan.isNotBlank()) "🥗 *Diet / Nutrition:*\n" + member.dietPlan + "\n----------------------------\n" else ""}
                                    Shared via PakBusiness Pro
                                """.trimIndent()

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, routineText)
                                }
                                val chooser = Intent.createChooser(intent, "Share Routine via WhatsApp")
                                context.startActivity(chooser)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Routine / Diet Card", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Attendance History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Check-In History (${checkIns.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (checkIns.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No past check-in sessions recorded yet",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(checkIns) { log ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(PakEmeraldContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = PakEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = GymDateUtils.formatDate(log.checkInTime),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "In: ${GymDateUtils.formatTime(log.checkInTime)}${if (log.checkOutTime != null) " • Out: ${GymDateUtils.formatTime(log.checkOutTime)}" else " • In Progress"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (log.checkOutTime != null) Color(0xFFE8F5E9) else Color(0xFFE0F2F1)
                            ) {
                                Text(
                                    text = if (log.checkOutTime != null) "Completed" else "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.checkOutTime != null) Color(0xFF2E7D32) else Color(0xFF00796B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Member?") },
            text = {
                Text("Are you sure you want to delete ${member.name}? This will remove all their membership records and attendance history offline.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteMember(member.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
