package com.example.ui.screens.school

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.SchoolClassEntity
import com.example.data.StudentAttendanceEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SchoolAttendanceTab(
    activeBusiness: BusinessEntity?,
    students: List<StudentEntity>,
    classes: List<SchoolClassEntity>,
    attendanceRecords: List<StudentAttendanceEntity>,
    selectedDateString: String,
    onDateChange: (String) -> Unit,
    onRecordAttendance: (StudentAttendanceEntity) -> Unit,
    onMarkAllPresent: (students: List<StudentEntity>, dateString: String, dateMillis: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedClassFilter by remember { mutableStateOf("All") }

    // Parse current selected date
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }

    val currentDateMillis = remember(selectedDateString) {
        try {
            sdf.parse(selectedDateString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    val displayDateText = remember(currentDateMillis) {
        displayFormat.format(Date(currentDateMillis))
    }

    // Class filter list
    val classOptions = remember(classes, students) {
        val set = mutableSetOf("All")
        classes.forEach { set.add("${it.className} (${it.section})") }
        students.forEach { set.add("${it.className} (${it.section})") }
        set.toList()
    }

    // Filter students for attendance
    val filteredStudents = remember(students, selectedClassFilter) {
        students.filter { student ->
            if (!student.isActive) return@filter false
            val key = "${student.className} (${student.section})"
            selectedClassFilter == "All" || key == selectedClassFilter
        }
    }

    // Map of studentId -> attendance record
    val attendanceMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentId }
    }

    // Attendance stats
    val totalCount = filteredStudents.size
    val presentCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "PRESENT" }
    }
    val absentCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "ABSENT" }
    }
    val lateCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "LATE" }
    }
    val leaveCount = remember(filteredStudents, attendanceMap) {
        filteredStudents.count { attendanceMap[it.id]?.status == "LEAVE" }
    }

    val attendancePercentage = if (totalCount > 0) {
        ((presentCount + lateCount).toDouble() / totalCount * 100).toInt()
    } else 0

    Column(modifier = modifier.fillMaxSize()) {
        // Date Navigator Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = currentDateMillis
                            add(Calendar.DAY_OF_YEAR, -1)
                        }
                        onDateChange(sdf.format(cal.time))
                    },
                    modifier = Modifier.testTag("attendance_prev_date_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val todayStr = sdf.format(Date())
                        onDateChange(todayStr)
                    }
                ) {
                    Text(
                        text = displayDateText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to jump to Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = currentDateMillis
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        onDateChange(sdf.format(cal.time))
                    },
                    modifier = Modifier.testTag("attendance_next_date_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                }
            }
        }

        // Attendance Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AttendanceStatBadge("Total", "$totalCount", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            AttendanceStatBadge("Present", "$presentCount", Color(0xFFE8F5E9), Color(0xFF1B5E20), Modifier.weight(1f))
            AttendanceStatBadge("Absent", "$absentCount", Color(0xFFFFEBEE), Color(0xFFB71C1C), Modifier.weight(1f))
            AttendanceStatBadge("Late", "$lateCount", Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f))
            AttendanceStatBadge("Leave", "$leaveCount", Color(0xFFE3F2FD), Color(0xFF0D47A1), Modifier.weight(1f))
            AttendanceStatBadge("Rate", "$attendancePercentage%", PakEmeraldContainer, Color(0xFF00381F), Modifier.weight(1.1f))
        }

        // Class Filter Chips & "Mark All Present" Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(classOptions) { opt ->
                    FilterChip(
                        selected = selectedClassFilter == opt,
                        onClick = { selectedClassFilter = opt },
                        label = { Text(opt, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onMarkAllPresent(filteredStudents, selectedDateString, currentDateMillis)
                    Toast.makeText(context, "Marked all ${filteredStudents.size} students PRESENT", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp).testTag("mark_all_present_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark All Present", fontSize = 11.sp)
            }
        }

        // Student Attendance Roster List
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active students in selected class",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val existingRecord = attendanceMap[student.id]
                    val currentStatus = existingRecord?.status ?: "UNMARKED"

                    StudentAttendanceRow(
                        student = student,
                        currentStatus = currentStatus,
                        onStatusChange = { newStatus ->
                            val record = (existingRecord ?: StudentAttendanceEntity(
                                businessId = activeBusiness?.id ?: 1L,
                                studentId = student.id,
                                studentName = student.name,
                                rollNo = student.rollNo,
                                className = student.className,
                                section = student.section,
                                date = currentDateMillis,
                                dateString = selectedDateString,
                                status = newStatus
                            )).copy(
                                status = newStatus,
                                date = currentDateMillis,
                                dateString = selectedDateString
                            )
                            onRecordAttendance(record)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceStatBadge(
    label: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = textColor)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun StudentAttendanceRow(
    student: StudentEntity,
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${student.rollNo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll # Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.rollNo,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${student.className} (${student.section}) • S/O: ${student.fatherName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4 Status Toggle Buttons: P, A, L, Lv
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusToggleButton(
                    label = "P",
                    isSelected = currentStatus == "PRESENT",
                    activeBg = Color(0xFF2E7D32),
                    activeTextColor = Color.White,
                    onClick = { onStatusChange("PRESENT") }
                )

                StatusToggleButton(
                    label = "A",
                    isSelected = currentStatus == "ABSENT",
                    activeBg = Color(0xFFC62828),
                    activeTextColor = Color.White,
                    onClick = { onStatusChange("ABSENT") }
                )

                StatusToggleButton(
                    label = "L",
                    isSelected = currentStatus == "LATE",
                    activeBg = Color(0xFFEF6C00),
                    activeTextColor = Color.White,
                    onClick = { onStatusChange("LATE") }
                )

                StatusToggleButton(
                    label = "Lv",
                    isSelected = currentStatus == "LEAVE",
                    activeBg = Color(0xFF1565C0),
                    activeTextColor = Color.White,
                    onClick = { onStatusChange("LEAVE") }
                )
            }
        }
    }
}

@Composable
fun StatusToggleButton(
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeTextColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeBg else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .testTag("status_toggle_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeTextColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
