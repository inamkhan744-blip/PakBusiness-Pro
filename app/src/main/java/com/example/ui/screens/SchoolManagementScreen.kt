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
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.data.SchoolStaffEntity
import com.example.data.StudentAttendanceEntity
import com.example.data.StudentEntity
import com.example.data.StudentFeeEntity
import com.example.ui.SchoolKpis
import com.example.ui.SchoolManagementViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolManagementScreen(
    viewModel: SchoolManagementViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val kpis by viewModel.kpis.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val fees by viewModel.feesList.collectAsState()
    val staff by viewModel.staffList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var targetFeeStudent by remember { mutableStateOf<StudentEntity?>(null) }

    val tabs = listOf("Students", "Attendance", "Fee Collection", "Staff & Teachers")
    val classes = listOf("ALL", "Playgroup", "Nursery", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Matric")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("school_management_screen"),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddStudentDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_student_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student", tint = Color.White)
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddStaffDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Teacher", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header KPIs
            SchoolHeaderKpis(kpis = kpis)

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
                    // Students Tab
                    StudentsTabContent(
                        students = students,
                        classes = classes,
                        selectedClass = selectedClass,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onClassSelect = { viewModel.selectClass(it) },
                        onCollectFee = { targetFeeStudent = it },
                        onMarkAttendance = { student, status -> viewModel.markAttendance(student, status) }
                    )
                }
                1 -> {
                    // Attendance Tab
                    SchoolAttendanceTabContent(
                        students = students,
                        classes = classes,
                        selectedClass = selectedClass,
                        todayAttendance = todayAttendance,
                        onClassSelect = { viewModel.selectClass(it) },
                        onMarkAllPresent = { viewModel.markAllPresentForClass(it) },
                        onMarkAttendance = { student, status -> viewModel.markAttendance(student, status) }
                    )
                }
                2 -> {
                    // Fee Collection & Chalans Tab
                    SchoolFeesTabContent(fees = fees)
                }
                3 -> {
                    // Staff & Teachers Tab
                    SchoolStaffTabContent(staff = staff)
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onSave = { rollNo, name, father, cls, sec, phone, addr, tuition, admission ->
                viewModel.saveStudent(
                    rollNumber = rollNo,
                    name = name,
                    fatherName = father,
                    className = cls,
                    section = sec,
                    phone = phone,
                    address = addr,
                    monthlyFeePkr = tuition,
                    admissionFeePkr = admission
                )
                showAddStudentDialog = false
            }
        )
    }

    // Add Staff Dialog
    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, desig, subj, phone, salary, qual ->
                viewModel.saveStaff(name, desig, subj, phone, salary, qual)
                showAddStaffDialog = false
            }
        )
    }

    // Collect Fee Dialog
    targetFeeStudent?.let { s ->
        CollectStudentFeeDialog(
            student = s,
            monthYear = viewModel.currentMonthYear,
            onDismiss = { targetFeeStudent = null },
            onConfirm = { tuition, exam, fine, paid, method ->
                viewModel.collectFee(
                    studentId = s.id,
                    studentName = s.name,
                    rollNumber = s.rollNumber,
                    className = s.className,
                    tuitionFee = tuition,
                    examFee = exam,
                    fine = fine,
                    paidAmount = paid,
                    paymentMethod = method
                )
                targetFeeStudent = null
            }
        )
    }
}

@Composable
private fun SchoolHeaderKpis(kpis: SchoolKpis) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "School & Academy Administration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SchoolKpiPill("Enrolled", "${kpis.totalStudents}", Icons.Default.School)
                SchoolKpiPill("Attendance", "${kpis.todayAttendancePercent.toInt()}%", Icons.Default.FactCheck, Color(0xFF2E7D32))
                SchoolKpiPill("Fees (Mo)", "PKR ${kpis.monthlyFeesCollectedPkr.toInt()}", Icons.Default.Payments, Color(0xFF1565C0))
                SchoolKpiPill("Staff", "${kpis.totalStaffCount}", Icons.Default.SupervisorAccount, Color(0xFF7B1FA2))
            }
        }
    }
}

@Composable
private fun SchoolKpiPill(label: String, value: String, icon: ImageVector, iconColor: Color = Color.DarkGray) {
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
private fun StudentsTabContent(
    students: List<StudentEntity>,
    classes: List<String>,
    selectedClass: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClassSelect: (String) -> Unit,
    onCollectFee: (StudentEntity) -> Unit,
    onMarkAttendance: (StudentEntity, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by name, roll no, or father...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = classes.indexOf(selectedClass).coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            classes.forEach { cls ->
                FilterChip(
                    selected = selectedClass == cls,
                    onClick = { onClassSelect(cls) },
                    label = { Text(cls, fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(students) { student ->
                StudentItemCard(
                    student = student,
                    onCollectFee = { onCollectFee(student) }
                )
            }
        }
    }
}

@Composable
private fun StudentItemCard(
    student: StudentEntity,
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
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.rollNumber.takeLast(3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "S/D/O ${student.fatherName} • ${student.className} (${student.section})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (student.pendingDuePkr > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (student.pendingDuePkr > 0) "DUE: PKR ${student.pendingDuePkr.toInt()}" else "PAID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.pendingDuePkr > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tuition Fee: PKR ${student.monthlyFeePkr.toInt()}/mo",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onCollectFee,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Collect Fee", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SchoolAttendanceTabContent(
    students: List<StudentEntity>,
    classes: List<String>,
    selectedClass: String,
    todayAttendance: List<StudentAttendanceEntity>,
    onClassSelect: (String) -> Unit,
    onMarkAllPresent: (String) -> Unit,
    onMarkAttendance: (StudentEntity, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Daily Attendance Register", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Mark today's student attendance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selectedClass != "ALL") {
                Button(
                    onClick = { onMarkAllPresent(selectedClass) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("All Present", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ScrollableTabRow(
            selectedTabIndex = classes.indexOf(selectedClass).coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            classes.forEach { cls ->
                FilterChip(
                    selected = selectedClass == cls,
                    onClick = { onClassSelect(cls) },
                    label = { Text(cls, fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(students) { s ->
                val att = todayAttendance.find { it.studentId == s.id }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.name, fontWeight = FontWeight.Bold)
                            Text("Roll #${s.rollNumber} • ${s.className}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("P" to "PRESENT", "A" to "ABSENT", "L" to "LEAVE").forEach { (short, status) ->
                                val isSelected = att?.status == status
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        isSelected && status == "PRESENT" -> Color(0xFF2E7D32)
                                        isSelected && status == "ABSENT" -> Color(0xFFC62828)
                                        isSelected && status == "LEAVE" -> Color(0xFFF57F17)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    onClick = { onMarkAttendance(s, status) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = short,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun SchoolFeesTabContent(fees: List<StudentFeeEntity>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Student Fee Chalans & Collection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (fees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No fee payments logged yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fees) { f ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(f.studentName, fontWeight = FontWeight.Bold)
                                Text("${f.className} • ${f.monthYear} • ${f.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PKR ${f.paidAmountPkr.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Chalan: ${f.chalanNumber}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolStaffTabContent(staff: List<SchoolStaffEntity>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Teachers & School Faculty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val sampleStaff = if (staff.isEmpty()) {
                listOf(
                    SchoolStaffEntity(id = 1, name = "Prof. Muhammad Aslam", designation = "Principal", subject = "Administration", monthlySalaryPkr = 80000.0),
                    SchoolStaffEntity(id = 2, name = "Mrs. Fatima Tariq", designation = "Senior Teacher", subject = "Mathematics", monthlySalaryPkr = 45000.0),
                    SchoolStaffEntity(id = 3, name = "Sir Usman Ali", designation = "Science Teacher", subject = "Physics & Chemistry", monthlySalaryPkr = 40000.0),
                    SchoolStaffEntity(id = 4, name = "Qari Abdul Rehman", designation = "Islamic Teacher", subject = "Islamiyat & Nazra", monthlySalaryPkr = 30000.0)
                )
            } else staff

            items(sampleStaff) { s ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.name, fontWeight = FontWeight.Bold)
                            Text("${s.designation} • ${s.subject}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PKR ${s.monthlySalaryPkr.toInt()}/mo", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), fontSize = 12.sp)
                            Text(s.qualification, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Double, Double) -> Unit
) {
    var rollNo by remember { mutableStateOf("101") }
    var name by remember { mutableStateOf("") }
    var father by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 5") }
    var section by remember { mutableStateOf("A") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var tuition by remember { mutableStateOf("2500") }
    var admission by remember { mutableStateOf("3000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            LazyColumn(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("Student Admission Form", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item { OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll Number") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = father, onValueChange = { father = it }, label = { Text("Father / Guardian Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class / Grade") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text("Section") }, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Guardian Phone Number") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = tuition, onValueChange = { tuition = it }, label = { Text("Monthly Tuition (PKR)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = admission, onValueChange = { admission = it }, label = { Text("Admission Fee (PKR)") }, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Residential Address") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(rollNo, name, father, className, section, phone, address, tuition.toDoubleOrNull() ?: 2500.0, admission.toDoubleOrNull() ?: 3000.0)
                                }
                            }
                        ) {
                            Text("Enrol Student")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectStudentFeeDialog(
    student: StudentEntity,
    monthYear: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, Double, Double, String) -> Unit
) {
    var tuition by remember { mutableStateOf(student.monthlyFeePkr.toInt().toString()) }
    var exam by remember { mutableStateOf("0") }
    var fine by remember { mutableStateOf("0") }
    var paid by remember { mutableStateOf(student.monthlyFeePkr.toInt().toString()) }
    var method by remember { mutableStateOf("Cash") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Student Fee Collection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Student: ${student.name} (Roll #${student.rollNumber})", fontWeight = FontWeight.SemiBold)
                Text("Class: ${student.className} • Month: $monthYear", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(value = tuition, onValueChange = { tuition = it }, label = { Text("Tuition Fee (PKR)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = exam, onValueChange = { exam = it }, label = { Text("Exam Fee (PKR)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fine, onValueChange = { fine = it }, label = { Text("Late Fine (PKR)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = paid, onValueChange = { paid = it }, label = { Text("Paid Amount (PKR)") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "EasyPaisa", "Bank").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val t = tuition.toDoubleOrNull() ?: student.monthlyFeePkr
                            val e = exam.toDoubleOrNull() ?: 0.0
                            val f = fine.toDoubleOrNull() ?: 0.0
                            val p = paid.toDoubleOrNull() ?: t
                            onConfirm(t, e, f, p, method)
                        }
                    ) {
                        Text("Issue Chalan")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desig by remember { mutableStateOf("Teacher") }
    var subject by remember { mutableStateOf("Mathematics") }
    var phone by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("30000") }
    var qual by remember { mutableStateOf("M.Sc") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add Staff / Teacher", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Teacher Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desig, onValueChange = { desig = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Teaching Subject") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Salary (PKR)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("Qualification") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, desig, subject, phone, salary.toDoubleOrNull() ?: 30000.0, qual)
                            }
                        }
                    ) {
                        Text("Save Faculty")
                    }
                }
            }
        }
    }
}
