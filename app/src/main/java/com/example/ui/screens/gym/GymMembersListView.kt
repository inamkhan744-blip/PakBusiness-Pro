package com.example.ui.screens.gym

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GymMemberEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

enum class GymMemberFilter {
    ALL,
    ACTIVE,
    IN_GYM_NOW,
    EXPIRING_OR_EXPIRED
}

@Composable
fun GymMembersListView(
    members: List<GymMemberEntity>,
    onMemberClick: (Long) -> Unit,
    onAddMemberClick: () -> Unit,
    onToggleCheckIn: (GymMemberEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(GymMemberFilter.ALL) }

    val filteredMembers = members.filter { member ->
        val matchesQuery = member.name.contains(searchQuery, ignoreCase = true) ||
                member.phone.contains(searchQuery) ||
                member.plan.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            GymMemberFilter.ALL -> true
            GymMemberFilter.ACTIVE -> !member.isExpired
            GymMemberFilter.IN_GYM_NOW -> member.isCheckedIn
            GymMemberFilter.EXPIRING_OR_EXPIRED -> member.isExpiringSoon || member.isExpired
        }

        matchesQuery && matchesFilter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("gym_members_list_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Box and Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search member by name, phone, or plan...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PakEmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gym_members_search_input")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == GymMemberFilter.ALL,
                            onClick = { selectedFilter = GymMemberFilter.ALL },
                            label = { Text("All (${members.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == GymMemberFilter.ACTIVE,
                            onClick = { selectedFilter = GymMemberFilter.ACTIVE },
                            label = { Text("Active (${members.count { !it.isExpired }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == GymMemberFilter.IN_GYM_NOW,
                            onClick = { selectedFilter = GymMemberFilter.IN_GYM_NOW },
                            label = { Text("In Gym Now (${members.count { it.isCheckedIn }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F2F1),
                                selectedLabelColor = Color(0xFF00796B)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == GymMemberFilter.EXPIRING_OR_EXPIRED,
                            onClick = { selectedFilter = GymMemberFilter.EXPIRING_OR_EXPIRED },
                            label = { Text("Expiring (${members.count { it.isExpiringSoon || it.isExpired }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFF3E0),
                                selectedLabelColor = Color(0xFFE65100)
                            )
                        )
                    }
                }
            }

            // Members LazyColumn List
            if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No members found matching '$searchQuery'" else "No members in this category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMembers, key = { it.id }) { member ->
                        GymMemberCard(
                            member = member,
                            onClick = { onMemberClick(member.id) },
                            onToggleCheckIn = { onToggleCheckIn(member) }
                        )
                    }
                }
            }
        }

        // FAB to add a new member (Requirement 2)
        FloatingActionButton(
            onClick = onAddMemberClick,
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_new_gym_member")
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add New Gym Member")
        }
    }
}

@Composable
fun GymMemberCard(
    member: GymMemberEntity,
    onClick: () -> Unit,
    onToggleCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("gym_member_card_${member.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (member.isCheckedIn) Color(0xFF2E7D32) else PakEmeraldContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(1).uppercase(),
                            color = if (member.isCheckedIn) Color.White else PakEmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = member.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (member.isCheckedIn) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "IN GYM",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${member.gender} • ${member.phone}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Subscription Status Pill
                val statusText = when {
                    member.isExpired -> "Expired"
                    member.isExpiringSoon -> "${member.remainingDays}d left"
                    else -> "Active"
                }

                val statusBg = when {
                    member.isExpired -> Color(0xFFFFEBEE)
                    member.isExpiringSoon -> Color(0xFFFFF3E0)
                    else -> PakEmeraldContainer
                }

                val statusTextColor = when {
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Plan Info & 1-Tap Check-in/Out Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = member.plan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${GymDateUtils.formatCurrency(member.amountPkr)} • Exp: ${GymDateUtils.formatDate(member.expiryDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (member.isExpired || member.isExpiringSoon) {
                        IconButton(
                            onClick = {
                                val statusNote = if (member.isExpired) "expired on ${GymDateUtils.formatDate(member.expiryDate)}" else "is expiring soon on ${GymDateUtils.formatDate(member.expiryDate)}"
                                val msg = """
                                    Assalam-o-Alaikum ${member.name},
                                    
                                    Your gym membership (${member.plan}) at our fitness center $statusNote.
                                    
                                    Please renew your membership fee (${GymDateUtils.formatCurrency(member.amountPkr)}) to continue your workouts without interruption.
                                    
                                    Thank you!
                                """.trimIndent()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send Renewal Alert via WhatsApp"))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Renewal Alert", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                        }
                    }

                    Button(
                        onClick = onToggleCheckIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (member.isCheckedIn) Color(0xFFC62828) else PakEmeraldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("quick_checkin_btn_${member.id}")
                    ) {
                        Icon(
                            imageVector = if (member.isCheckedIn) Icons.Default.Logout else Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (member.isCheckedIn) "Check Out" else "Check In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
