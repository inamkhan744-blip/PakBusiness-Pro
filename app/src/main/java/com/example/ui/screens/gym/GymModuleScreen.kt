package com.example.ui.screens.gym

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.GymCheckInEntity
import com.example.data.GymLockerEntity
import com.example.data.GymMemberEntity
import com.example.data.GymPaymentEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import kotlinx.coroutines.flow.Flow

@Composable
fun GymModuleScreen(
    business: BusinessEntity?,
    members: List<GymMemberEntity>,
    todayCheckIns: List<GymCheckInEntity>,
    selectedMember: GymMemberEntity?,
    isAddEditOpen: Boolean,
    memberToEdit: GymMemberEntity?,
    onSelectMember: (Long?) -> Unit,
    onOpenAddMember: () -> Unit,
    onOpenEditMember: (GymMemberEntity) -> Unit,
    onCloseAddEdit: () -> Unit,
    onSaveMember: (name: String, phone: String, gender: String, plan: String, startDate: Long, durationDays: Int, amount: Double) -> Unit,
    onDeleteMember: (Long) -> Unit,
    onToggleCheckIn: (GymMemberEntity) -> Unit,
    getMemberCheckIns: (Long) -> Flow<List<GymCheckInEntity>>,
    viewModel: BusinessViewModel? = null,
    modifier: Modifier = Modifier
) {
    val payments = viewModel?.gymPayments?.collectAsState()?.value ?: emptyList()
    val lockers = viewModel?.gymLockers?.collectAsState()?.value ?: emptyList()

    if (isAddEditOpen) {
        GymAddEditMemberScreen(
            memberToEdit = memberToEdit,
            onSave = { name, phone, gender, plan, startDate, durationDays, amount ->
                onSaveMember(name, phone, gender, plan, startDate, durationDays, amount)
            },
            onSaveFull = { name, phone, gender, plan, startDate, durationDays, amount, adm, due, goal, wPlan, dPlan, locker ->
                if (viewModel != null) {
                    viewModel.saveGymMember(
                        name = name,
                        phone = phone,
                        gender = gender,
                        plan = plan,
                        startDate = startDate,
                        durationDays = durationDays,
                        amountPkr = amount,
                        id = memberToEdit?.id ?: 0L,
                        admissionFeePkr = adm,
                        pendingDuePkr = due,
                        fitnessGoal = goal,
                        workoutPlan = wPlan,
                        dietPlan = dPlan,
                        lockerNumber = locker
                    )
                } else {
                    onSaveMember(name, phone, gender, plan, startDate, durationDays, amount)
                }
            },
            onCancel = onCloseAddEdit,
            modifier = modifier
        )
    } else if (selectedMember != null) {
        GymMemberDetailsScreen(
            member = selectedMember,
            checkInsFlow = getMemberCheckIns(selectedMember.id),
            onToggleCheckIn = onToggleCheckIn,
            onEditMember = onOpenEditMember,
            onDeleteMember = { id ->
                onDeleteMember(id)
                onSelectMember(null)
            },
            onBack = { onSelectMember(null) },
            modifier = modifier
        )
    } else {
        var selectedSubTab by remember { mutableIntStateOf(0) }

        Column(modifier = modifier.fillMaxSize().testTag("gym_module_screen")) {
            // Secondary Sub-Tabs for Gym Module
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = PakEmeraldPrimary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            text = "Overview",
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = PakEmeraldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("gym_tab_dashboard")
                )

                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Text(
                            text = "Members (${members.size})",
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = PakEmeraldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("gym_tab_members")
                )

                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = {
                        Text(
                            text = "Attendance Log (${todayCheckIns.size})",
                            fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = PakEmeraldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("gym_tab_attendance")
                )

                Tab(
                    selected = selectedSubTab == 3,
                    onClick = { selectedSubTab = 3 },
                    text = {
                        Text(
                            text = "Fees & Lockers",
                            fontWeight = if (selectedSubTab == 3) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = PakEmeraldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("gym_tab_fees_lockers")
                )
            }

            Crossfade(targetState = selectedSubTab, label = "gymSubTabTransition") { tabIndex ->
                when (tabIndex) {
                    0 -> GymDashboardView(
                        members = members,
                        todayCheckIns = todayCheckIns,
                        onAddMemberClick = onOpenAddMember,
                        onViewAllMembersClick = { selectedSubTab = 1 },
                        onMemberClick = onSelectMember,
                        onToggleCheckIn = onToggleCheckIn
                    )
                    1 -> GymMembersListView(
                        members = members,
                        onMemberClick = onSelectMember,
                        onAddMemberClick = onOpenAddMember,
                        onToggleCheckIn = onToggleCheckIn
                    )
                    2 -> GymAttendanceTabView(
                        members = members,
                        todayCheckIns = todayCheckIns,
                        onToggleCheckIn = onToggleCheckIn,
                        onMemberClick = onSelectMember
                    )
                    3 -> GymFeesAndLockersTabView(
                        members = members,
                        payments = payments,
                        lockers = lockers,
                        onCollectPayment = { memberId, name, amount, type, method, days ->
                            viewModel?.collectGymFeePayment(memberId, name, amount, type, method, days)
                        },
                        onSaveLocker = { l -> viewModel?.saveGymLocker(l) },
                        onDeleteLocker = { id -> viewModel?.deleteGymLocker(id) },
                        onMemberClick = onSelectMember
                    )
                }
            }
        }
    }
}
