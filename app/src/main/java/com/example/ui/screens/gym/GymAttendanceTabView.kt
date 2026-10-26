package com.example.ui.screens.gym

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun GymAttendanceTabView(
    members: List<GymMemberEntity>,
    todayCheckIns: List<GymCheckInEntity>,
    onToggleCheckIn: (GymMemberEntity) -> Unit,
    onMemberClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE_NOW, CHECKED_OUT
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val currentlyInGymCount = remember(members) { members.count { it.isCheckedIn } }
    val totalTodayCount = remember(todayCheckIns) { todayCheckIns.size }
    val checkedOutCount = remember(todayCheckIns) { todayCheckIns.count { it.checkOutTime != null } }

    val filteredCheckIns = remember(todayCheckIns, searchQuery, selectedFilter) {
        todayCheckIns.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                item.memberName.contains(searchQuery, ignoreCase = true)
            val matchFilter = when (selectedFilter) {
                "ACTIVE_NOW" -> item.checkOutTime == null
                "CHECKED_OUT" -> item.checkOutTime != null
                else -> true
            }
            matchQuery && matchFilter
        }
    }

    // Quick match member if user typed phone or exact name in search bar
    val matchedMember = remember(searchQuery, members) {
        if (searchQuery.isNotBlank() && searchQuery.length >= 3) {
            members.find {
                it.phone.contains(searchQuery) ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.lockerNumber.equals(searchQuery, ignoreCase = true) ||
                it.id.toString() == searchQuery
            }
        } else null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("gym_attendance_tab_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Attendance Summary KPI Cards
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
                        Text("Total Today", fontSize = 11.sp, color = PakEmeraldDark, fontWeight = FontWeight.Medium)
                        Text("$totalTodayCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        Text("Check-ins", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Now", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        Text("$currentlyInGymCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("Working out", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Completed", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                        Text("$checkedOutCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Text("Checked out", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Fast Check-In Search Input
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1-Tap Member Check-In & Check-Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Member Name, Phone or Locker #...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PakEmeraldPrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gym_attendance_search_input")
                    )

                    // If a member matches the search query, show quick check-in banner
                    if (matchedMember != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (matchedMember.isCheckedIn) Color(0xFFFFF3E0) else PakEmeraldContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = matchedMember.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${matchedMember.plan} • ${matchedMember.phone}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (matchedMember.lockerNumber.isNotBlank()) {
                                        Text(
                                            text = "Locker: ${matchedMember.lockerNumber}",
                                            fontSize = 11.sp,
                                            color = PakEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onToggleCheckIn(matchedMember)
                                        statusMessage = if (matchedMember.isCheckedIn) "Checked Out: ${matchedMember.name}" else "Checked In: ${matchedMember.name}"
                                        searchQuery = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (matchedMember.isCheckedIn) Color(0xFFC62828) else PakEmeraldPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("fast_checkin_button")
                                ) {
                                    Icon(
                                        imageVector = if (matchedMember.isCheckedIn) Icons.Default.Logout else Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (matchedMember.isCheckedIn) "Check Out Now" else "Check In Now",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage!!,
                            color = PakEmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Attendance Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Log (${todayCheckIns.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PakEmeraldContainer,
                        selectedLabelColor = PakEmeraldPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == "ACTIVE_NOW",
                    onClick = { selectedFilter = "ACTIVE_NOW" },
                    label = { Text("In Gym ($currentlyInGymCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF2E7D32)
                    )
                )

                FilterChip(
                    selected = selectedFilter == "CHECKED_OUT",
                    onClick = { selectedFilter = "CHECKED_OUT" },
                    label = { Text("Checked Out ($checkedOutCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFF3E0),
                        selectedLabelColor = Color(0xFFE65100)
                    )
                )
            }
        }

        // Attendance Table / Records List
        if (filteredCheckIns.isEmpty()) {
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
                            text = if (todayCheckIns.isEmpty()) "No members have checked in today yet." else "No records match your filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredCheckIns) { record ->
                val member = members.find { it.id == record.memberId }
                val isCurrentlyActive = record.checkOutTime == null

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMemberClick(record.memberId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (isCurrentlyActive) Color(0xFF2E7D32) else PakEmeraldContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = record.memberName.take(1).uppercase(),
                                    color = if (isCurrentlyActive) Color.White else PakEmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Column {
                                Text(
                                    text = record.memberName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "In: ${GymDateUtils.formatTime(record.checkInTime)}${if (record.checkOutTime != null) " • Out: ${GymDateUtils.formatTime(record.checkOutTime)}" else " • Active in Gym"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (member != null && member.lockerNumber.isNotBlank()) {
                                    Text(
                                        text = "Locker: ${member.lockerNumber}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Right action: Check Out if currently in gym
                        if (isCurrentlyActive && member != null) {
                            Button(
                                onClick = { onToggleCheckIn(member) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Check Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "Completed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
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
