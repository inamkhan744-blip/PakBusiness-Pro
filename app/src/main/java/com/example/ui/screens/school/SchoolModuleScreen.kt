package com.example.ui.screens.school

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.BusinessEntity
import com.example.data.ExamResultEntity
import com.example.data.FeeVoucherEntity
import com.example.data.SchoolClassEntity
import com.example.data.SchoolStaffEntity
import com.example.data.StudentAttendanceEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary

sealed class SchoolTab(val title: String, val icon: ImageVector) {
    object Students : SchoolTab("Students", Icons.Default.School)
    object Classes : SchoolTab("Classes", Icons.Default.Class)
    object FeeVouchers : SchoolTab("Fee Vouchers", Icons.Default.ReceiptLong)
    object Attendance : SchoolTab("Attendance", Icons.Default.EventAvailable)
    object Staff : SchoolTab("Teachers & Staff", Icons.Default.Badge)
    object Exams : SchoolTab("Exams & Results", Icons.Default.Assessment)
}

@Composable
fun SchoolModuleScreen(
    activeBusiness: BusinessEntity?,
    students: List<StudentEntity>,
    classes: List<SchoolClassEntity>,
    vouchers: List<FeeVoucherEntity>,
    attendanceRecords: List<StudentAttendanceEntity>,
    selectedAttendanceDate: String,
    onSaveStudent: (StudentEntity) -> Unit,
    onDeleteStudent: (Long) -> Unit,
    onSaveClass: (SchoolClassEntity) -> Unit,
    onDeleteClass: (Long) -> Unit,
    onGenerateBulkVouchers: (monthYear: String, dueDate: Long, examFee: Double, labFee: Double, (Int) -> Unit) -> Unit,
    onSaveVoucher: (FeeVoucherEntity) -> Unit,
    onMarkVoucherPaid: (Long, String) -> Unit,
    onDeleteVoucher: (Long) -> Unit,
    onAttendanceDateChange: (String) -> Unit,
    onRecordAttendance: (StudentAttendanceEntity) -> Unit,
    onMarkAllAttendancePresent: (students: List<StudentEntity>, dateString: String, dateMillis: Long) -> Unit,
    staffList: List<SchoolStaffEntity> = emptyList(),
    examResults: List<ExamResultEntity> = emptyList(),
    onSaveStaff: (SchoolStaffEntity) -> Unit = {},
    onDeleteStaff: (Long) -> Unit = {},
    onSaveExamResult: (ExamResultEntity) -> Unit = {},
    onDeleteExamResult: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        SchoolTab.Students,
        SchoolTab.Classes,
        SchoolTab.FeeVouchers,
        SchoolTab.Attendance,
        SchoolTab.Staff,
        SchoolTab.Exams
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("school_module_screen")
    ) {
        // Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PakEmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "School & Academy",
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeBusiness?.name ?: "School & Academy Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PakBusiness Pro • School & Academy Portal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Enrolled students pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${students.size} Enrolled",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = PakEmeraldPrimary
                            )
                        }
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("school_tab_${tab.title.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // Tab Content
        Crossfade(
            targetState = selectedTabIndex,
            label = "school_tab_content",
            modifier = Modifier.weight(1f)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SchoolStudentsTab(
                    activeBusiness = activeBusiness,
                    students = students,
                    classes = classes,
                    onSaveStudent = onSaveStudent,
                    onDeleteStudent = onDeleteStudent
                )
                1 -> SchoolClassesTab(
                    activeBusiness = activeBusiness,
                    classes = classes,
                    students = students,
                    onSaveClass = onSaveClass,
                    onDeleteClass = onDeleteClass
                )
                2 -> SchoolFeeVouchersTab(
                    activeBusiness = activeBusiness,
                    students = students,
                    vouchers = vouchers,
                    onGenerateBulk = onGenerateBulkVouchers,
                    onSaveVoucher = onSaveVoucher,
                    onMarkPaid = onMarkVoucherPaid,
                    onDeleteVoucher = onDeleteVoucher
                )
                3 -> SchoolAttendanceTab(
                    activeBusiness = activeBusiness,
                    students = students,
                    classes = classes,
                    attendanceRecords = attendanceRecords,
                    selectedDateString = selectedAttendanceDate,
                    onDateChange = onAttendanceDateChange,
                    onRecordAttendance = onRecordAttendance,
                    onMarkAllPresent = onMarkAllAttendancePresent
                )
                4 -> SchoolStaffTab(
                    activeBusiness = activeBusiness,
                    staffList = staffList,
                    onSaveStaff = onSaveStaff,
                    onDeleteStaff = onDeleteStaff
                )
                5 -> SchoolExamsTab(
                    activeBusiness = activeBusiness,
                    students = students,
                    classes = classes,
                    examResults = examResults,
                    onSaveExamResult = onSaveExamResult,
                    onDeleteExamResult = onDeleteExamResult
                )
            }
        }
    }
}
