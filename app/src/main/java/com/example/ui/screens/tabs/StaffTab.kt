package com.example.ui.screens.tabs

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.BusinessEntity
import com.example.data.StaffAttendanceEntity
import com.example.data.StaffEntity
import com.example.data.StaffRole
import com.example.data.ModulePermission
import com.example.data.hasModulePermission
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTab(
    business: BusinessEntity?,
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val staffList by viewModel.staffMembers.collectAsState()
    val attendanceRecords by viewModel.staffAttendanceRecords.collectAsState()
    val currency = business?.currency ?: "PKR"

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Attendance, 1: Staff Directory & Payroll
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var staffForAdvanceDialog by remember { mutableStateOf<StaffEntity?>(null) }
    var staffForPermissionsDialog by remember { mutableStateOf<StaffEntity?>(null) }

    val isOwner = viewModel.isCurrentRoleOwner
    val canManageStaff = viewModel.hasPermission(ModulePermission.STAFF)

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val attendanceMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.staffId }
    }

    val presentCount = remember(attendanceRecords) {
        attendanceRecords.count { it.status == "PRESENT" }
    }

    val totalSalaryExpenditure = remember(staffList) {
        staffList.sumOf { it.monthlySalary }
    }

    val totalAdvancesGiven = remember(staffList) {
        staffList.sumOf { it.advancePaid }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("staff_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
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
                        Column {
                            Text(
                                text = "Staff Haziri & Payroll Manager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = todayDateFormatted,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { showAddStaffDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_staff")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Stat metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Present Today", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("$presentCount / ${staffList.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5D6A7))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Monthly Payroll", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalSalaryExpenditure)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Peshgi", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalAdvancesGiven)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCC80))
                            }
                        }
                    }

                    // Print & Export Staff Payroll Register
                    if (staffList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val payrollItems = staffList.map { staff ->
                                        val net = maxOf(0.0, staff.monthlySalary - staff.advancePaid)
                                        InvoiceExportItem(
                                            name = "${staff.name} (${staff.role})",
                                            quantity = 1.0,
                                            unitPrice = staff.monthlySalary,
                                            total = net
                                        )
                                    }
                                    val exportData = InvoiceExportData(
                                        invoiceNumber = "PAYROLL-${System.currentTimeMillis() % 100000}",
                                        businessName = business?.name ?: "PakBusiness Enterprise",
                                        businessAddress = business?.address ?: "",
                                        businessPhone = business?.phone ?: "",
                                        customerName = "MONTHLY STAFF SALARY & ATTENDANCE AUDIT REGISTER",
                                        customerPhone = "Total Staff: ${staffList.size} | Present Today: $presentCount",
                                        items = payrollItems,
                                        subtotal = totalSalaryExpenditure,
                                        discount = totalAdvancesGiven,
                                        tax = 0.0,
                                        grandTotal = maxOf(0.0, totalSalaryExpenditure - totalAdvancesGiven),
                                        paymentMethod = "Net Payroll Liability ($currency)",
                                        footerNote = "Authorized Monthly Salary Sheet • PakBusiness Pro"
                                    )
                                    PdfExportUtil.printInvoice(context, exportData)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("btn_print_staff_payroll")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print Payroll PDF", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val payrollSummary = buildString {
                                        appendLine("📋 *OFFICIAL STAFF PAYROLL & ATTENDANCE REPORT*")
                                        appendLine("🏢 *${business?.name ?: "Business"}*")
                                        appendLine("📅 *Date:* $todayDateFormatted")
                                        appendLine("👥 *Total Staff:* ${staffList.size} | *Present Today:* $presentCount")
                                        appendLine("💵 *Monthly Budget:* $currency ${String.format(Locale.getDefault(), "%,.0f", totalSalaryExpenditure)}")
                                        appendLine("🔻 *Total Advances Paid:* $currency ${String.format(Locale.getDefault(), "%,.0f", totalAdvancesGiven)}")
                                        appendLine("────────────────────")
                                        staffList.forEachIndexed { idx, s ->
                                            val net = maxOf(0.0, s.monthlySalary - s.advancePaid)
                                            val att = attendanceMap[s.id]?.status ?: "NOT MARKED"
                                            appendLine("${idx + 1}. *${s.name}* (${s.role})")
                                            appendLine("   • Salary: $currency ${String.format(Locale.getDefault(), "%,.0f", s.monthlySalary)} | Advance: $currency ${String.format(Locale.getDefault(), "%,.0f", s.advancePaid)}")
                                            appendLine("   • Net Payable: $currency ${String.format(Locale.getDefault(), "%,.0f", net)} | Today: $att")
                                        }
                                        appendLine("────────────────────")
                                        appendLine("_Generated via PakBusiness Pro_")
                                    }

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, payrollSummary)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Payroll Report"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("btn_share_staff_payroll")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Report", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Sub-tabs (Attendance Register vs Staff & Salaries)
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Daily Attendance (Haziri)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Staff & Salaries (Peshgi)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }
        }

        if (staffList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("No staff members added yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Add your sales team, cashiers, helpers, mechanics, tailor masters or instructors to mark attendance and manage salaries.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showAddStaffDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add First Staff Member")
                        }
                    }
                }
            }
        } else {
            if (selectedSubTab == 0) {
                // ATTENDANCE TAB
                items(staffList, key = { it.id }) { staff ->
                    val record = attendanceMap[staff.id]
                    val currentStatus = record?.status ?: "PENDING"

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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = staff.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${staff.role} • ${staff.phone}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quick attendance toggles (P, A, H, L)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatusButton(
                                    label = "Present",
                                    shortCode = "P",
                                    isSelected = currentStatus == "PRESENT",
                                    selectedColor = Color(0xFF2E7D32),
                                    onClick = {
                                        viewModel.recordStaffAttendance(staff.id, staff.name, "PRESENT", "")
                                    }
                                )
                                StatusButton(
                                    label = "Absent",
                                    shortCode = "A",
                                    isSelected = currentStatus == "ABSENT",
                                    selectedColor = Color(0xFFC62828),
                                    onClick = {
                                        viewModel.recordStaffAttendance(staff.id, staff.name, "ABSENT", "")
                                    }
                                )
                                StatusButton(
                                    label = "Half-Day",
                                    shortCode = "1/2",
                                    isSelected = currentStatus == "HALF_DAY",
                                    selectedColor = Color(0xFFEF6C00),
                                    onClick = {
                                        viewModel.recordStaffAttendance(staff.id, staff.name, "HALF_DAY", "")
                                    }
                                )
                                StatusButton(
                                    label = "Leave",
                                    shortCode = "L",
                                    isSelected = currentStatus == "LEAVE",
                                    selectedColor = Color(0xFF1565C0),
                                    onClick = {
                                        viewModel.recordStaffAttendance(staff.id, staff.name, "LEAVE", "")
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // STAFF & SALARY TAB
                items(staffList, key = { it.id }) { staff ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = staff.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (staff.staffCode.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PakGoldContainer
                                            ) {
                                                Text(
                                                    text = "ID: ${staff.staffCode}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PakEmeraldDark,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (staff.accessLevel) {
                                                "MANAGER" -> Color(0xFFE0F2FE)
                                                "CASHIER" -> Color(0xFFFEF3C7)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Text(
                                                text = staff.accessLevel.ifBlank { "STAFF" },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (staff.accessLevel) {
                                                    "MANAGER" -> Color(0xFF0284C7)
                                                    "CASHIER" -> Color(0xFFB45309)
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${staff.role} • 📞 ${staff.phone} • Login PIN: ${staff.loginPin.ifBlank { "1234" }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteStaffMember(staff.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PakEmeraldContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "Salary: $currency ${String.format(Locale.getDefault(), "%,.0f", staff.monthlySalary)}/mo",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = PakEmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (staff.advancePaid > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Peshgi Advance: $currency ${String.format(Locale.getDefault(), "%,.0f", staff.advancePaid)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (staff.advancePaid > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // RBAC Module Permissions Badges
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val moduleList = listOf(
                                    ModulePermission.POS to "POS",
                                    ModulePermission.INVENTORY to "Stock",
                                    ModulePermission.KHATA to "Khata",
                                    ModulePermission.EXPENSES to "Expense",
                                    ModulePermission.REPORTS to "Reports",
                                    ModulePermission.STAFF to "Staff",
                                    ModulePermission.SETTINGS to "Settings"
                                )
                                moduleList.forEach { (perm, tag) ->
                                    val hasPerm = staff.hasModulePermission(perm)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (hasPerm) PakEmeraldContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = if (hasPerm) "✓ $tag" else "✕ $tag",
                                            fontSize = 9.sp,
                                            fontWeight = if (hasPerm) FontWeight.Bold else FontWeight.Normal,
                                            color = if (hasPerm) PakEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Actions row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isOwner) {
                                    OutlinedButton(
                                        onClick = { staffForPermissionsDialog = staff },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.padding(end = 4.dp).testTag("btn_edit_rbac_${staff.id}")
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(13.dp), tint = PakEmeraldPrimary)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Permissions", fontSize = 11.sp, color = PakEmeraldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(onClick = { staffForAdvanceDialog = staff }) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = PakEmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Advance", fontSize = 11.sp, color = PakEmeraldPrimary)
                                }

                                val netPayable = maxOf(0.0, staff.monthlySalary - staff.advancePaid)

                                OutlinedButton(
                                    onClick = {
                                        val slipData = InvoiceExportData(
                                            invoiceNumber = "SLIP-${staff.id}-${System.currentTimeMillis() % 10000}",
                                            businessName = business?.name ?: "PakBusiness Enterprise",
                                            businessAddress = business?.address ?: "",
                                            businessPhone = business?.phone ?: "",
                                            customerName = "${staff.name} (${staff.role})",
                                            customerPhone = staff.phone,
                                            items = listOf(
                                                InvoiceExportItem(name = "Monthly Basic Salary", quantity = 1.0, unitPrice = staff.monthlySalary, total = staff.monthlySalary),
                                                InvoiceExportItem(name = "Advance / Peshgi Deducted", quantity = 1.0, unitPrice = -staff.advancePaid, total = -staff.advancePaid)
                                            ),
                                            subtotal = staff.monthlySalary,
                                            discount = staff.advancePaid,
                                            tax = 0.0,
                                            grandTotal = netPayable,
                                            paymentMethod = "Salary Payment (${currency})",
                                            amountPaid = netPayable,
                                            changeDue = 0.0,
                                            footerNote = "Authorized Monthly Salary Parcha • PakBusiness Pro"
                                        )
                                        PdfExportUtil.printInvoice(context, slipData)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(end = 4.dp).testTag("btn_print_salary_slip_${staff.id}")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp), tint = PakEmeraldPrimary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Print", fontSize = 11.sp, color = PakEmeraldPrimary)
                                }

                                Button(
                                    onClick = {
                                        val slip = """
                                            *EMPLOYEE SALARY SLIP / PARCHA*
                                            🏢 *${business?.name ?: "Business"}*
                                            ────────────────────
                                            👤 *Employee:* ${staff.name}
                                            💼 *Designation:* ${staff.role}
                                            📞 *Contact:* ${staff.phone}
                                            ────────────────────
                                            💵 *Basic Salary:* $currency ${String.format(Locale.getDefault(), "%,.0f", staff.monthlySalary)}
                                            🔻 *Advance / Peshgi Deducted:* $currency ${String.format(Locale.getDefault(), "%,.0f", staff.advancePaid)}
                                            💰 *NET PAYABLE:* $currency ${String.format(Locale.getDefault(), "%,.0f", netPayable)}
                                            📅 *Generated:* $todayDateFormatted
                                            ────────────────────
                                            _Authorized Signature / PakBusiness Pro ERP_
                                        """.trimIndent()

                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, slip)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Salary Slip"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Staff Dialog
    if (showAddStaffDialog) {
        AddStaffDialog(
            currency = currency,
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, role, phone, salary, staffCode, loginPin, accessLevel, permissions ->
                viewModel.saveStaffMember(
                    StaffEntity(
                        businessId = business?.id ?: 0L,
                        name = name,
                        role = role,
                        phone = phone,
                        monthlySalary = salary,
                        joiningDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                        staffCode = staffCode,
                        loginPin = loginPin,
                        accessLevel = accessLevel,
                        permissions = permissions
                    )
                )
                showAddStaffDialog = false
            }
        )
    }

    // Edit Staff RBAC Permissions Dialog
    if (staffForPermissionsDialog != null) {
        val staff = staffForPermissionsDialog!!
        EditStaffPermissionsDialog(
            staff = staff,
            onDismiss = { staffForPermissionsDialog = null },
            onSave = { updatedStaff ->
                viewModel.saveStaffMember(updatedStaff)
                staffForPermissionsDialog = null
            }
        )
    }

    // Advance Peshgi Dialog
    if (staffForAdvanceDialog != null) {
        val staff = staffForAdvanceDialog!!
        var advanceAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { staffForAdvanceDialog = null },
            title = { Text("Give Advance (Peshgi) to ${staff.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Advance Balance: $currency ${String.format(Locale.getDefault(), "%,.0f", staff.advancePaid)}", fontSize = 13.sp)
                    OutlinedTextField(
                        value = advanceAmount,
                        onValueChange = { advanceAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Advance Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = advanceAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.addStaffAdvance(staff.id, amount)
                        }
                        staffForAdvanceDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Add Advance")
                }
            },
            dismissButton = {
                TextButton(onClick = { staffForAdvanceDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusButton(
    label: String,
    shortCode: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clickable(onClick = onClick)
            .size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = shortCode,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AddStaffDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, phone: String, salary: Double, staffCode: String, loginPin: String, accessLevel: String, permissions: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("") }
    var staffCode by remember { mutableStateOf("STF-${(100..999).random()}") }
    var loginPin by remember { mutableStateOf("1234") }
    var accessLevel by remember { mutableStateOf("CASHIER") } // MANAGER, CASHIER, VIEW_ONLY, CUSTOM
    var customPermissions by remember {
        mutableStateOf(setOf("POS", "INVENTORY", "KHATA"))
    }

    val presetRoles = listOf("Salesman", "Cashier", "Manager", "Helper", "Chef", "Tailor Master", "Mechanic", "Trainer", "Accountant")
    val rbacRoles = listOf(
        StaffRole.MANAGER,
        StaffRole.CASHIER,
        StaffRole.VIEW_ONLY,
        StaffRole.CUSTOM
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = PakEmeraldPrimary)
                Text("Add Staff Member & RBAC Role", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name * (نام)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Designation * (عہدہ)") },
                    placeholder = { Text("e.g. Salesman, Cashier, Manager") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(presetRoles) { preset ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                role = preset
                                when {
                                    preset.equals("Manager", true) -> {
                                        accessLevel = "MANAGER"
                                        customPermissions = StaffRole.MANAGER.defaultPermissions.toSet()
                                    }
                                    preset.equals("Cashier", true) -> {
                                        accessLevel = "CASHIER"
                                        customPermissions = StaffRole.CASHIER.defaultPermissions.toSet()
                                    }
                                }
                            }
                        ) {
                            Text(preset, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number * (موبائل نمبر)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Salary ($currency) * (ماہانہ تنخواہ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Login Credentials for Staff
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PakGoldContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Staff Login Credentials (عملہ لاگ ان آئی ڈی و پن)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = staffCode,
                                onValueChange = { staffCode = it },
                                label = { Text("Staff ID") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = loginPin,
                                onValueChange = { loginPin = it.filter { ch -> ch.isDigit() }.take(6) },
                                label = { Text("Login PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // RBAC Role Selection
                Text(
                    text = "Assign RBAC Role (اختیارات کا درجہ)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                rbacRoles.forEach { rbacRole ->
                    val isSelected = accessLevel.equals(rbacRole.code, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PakEmeraldPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                accessLevel = rbacRole.code
                                if (rbacRole != StaffRole.CUSTOM) {
                                    customPermissions = rbacRole.defaultPermissions.toSet()
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rbacRole.titleEn} (${rbacRole.titleUrdu})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PakEmeraldDark else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = rbacRole.description,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Granular Module Permissions if CUSTOM or expanded
                if (accessLevel == "CUSTOM") {
                    Text(
                        text = "Customize Modules (ماڈیول کے اختیارات منتخب کریں)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )

                    ModulePermission.entries.forEach { perm ->
                        val isChecked = customPermissions.contains(perm.code)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customPermissions = if (isChecked) customPermissions - perm.code else customPermissions + perm.code
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    customPermissions = if (checked) customPermissions + perm.code else customPermissions - perm.code
                                },
                                colors = CheckboxDefaults.colors(checkedColor = PakEmeraldPrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${perm.titleEn} • ${perm.titleUrdu}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = perm.description,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sal = salaryText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && role.isNotBlank()) {
                        val permissionsStr = if (accessLevel == "CUSTOM") {
                            customPermissions.joinToString(",")
                        } else {
                            val preset = StaffRole.entries.firstOrNull { it.code == accessLevel }
                            preset?.defaultPermissions?.joinToString(",") ?: ""
                        }
                        onSave(
                            name,
                            role,
                            phone,
                            sal,
                            staffCode.ifBlank { "STF-${(100..999).random()}" },
                            loginPin.ifBlank { "1234" },
                            accessLevel,
                            permissionsStr
                        )
                    }
                },
                enabled = name.isNotBlank() && role.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Add Member & RBAC Role")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditStaffPermissionsDialog(
    staff: StaffEntity,
    onDismiss: () -> Unit,
    onSave: (StaffEntity) -> Unit
) {
    var staffCode by remember { mutableStateOf(staff.staffCode) }
    var loginPin by remember { mutableStateOf(staff.loginPin) }
    var selectedRole by remember { mutableStateOf(staff.accessLevel.ifBlank { "STAFF" }) }
    var customPermissions by remember {
        mutableStateOf(
            if (staff.permissions.isNotBlank()) {
                staff.permissions.split(",").map { it.trim().uppercase() }.toSet()
            } else {
                val preset = StaffRole.entries.firstOrNull { it.code.equals(staff.accessLevel, ignoreCase = true) }
                preset?.defaultPermissions?.toSet() ?: setOf("POS", "INVENTORY")
            }
        )
    }

    val rbacRoles = listOf(
        StaffRole.MANAGER,
        StaffRole.CASHIER,
        StaffRole.VIEW_ONLY,
        StaffRole.CUSTOM
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = PakEmeraldPrimary)
                Text(
                    text = "RBAC: ${staff.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign role permissions and login credentials for ${staff.name} (${staff.role}).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Credentials Section
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PakGoldContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Staff Login ID & PIN (لاگ ان تفصیلات)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PakEmeraldDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = staffCode,
                                onValueChange = { staffCode = it },
                                label = { Text("Staff ID") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = loginPin,
                                onValueChange = { loginPin = it.filter { ch -> ch.isDigit() }.take(6) },
                                label = { Text("Login PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Roles Selection
                Text(
                    text = "Select Role Level (اختیارات کا درجہ)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                rbacRoles.forEach { roleItem ->
                    val isSelected = selectedRole.equals(roleItem.code, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PakEmeraldPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRole = roleItem.code
                                if (roleItem != StaffRole.CUSTOM) {
                                    customPermissions = roleItem.defaultPermissions.toSet()
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${roleItem.titleEn} (${roleItem.titleUrdu})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) PakEmeraldDark else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PakEmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = roleItem.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Granular Module Permissions
                Text(
                    text = "Granular Permissions (تفصیلی ماڈیولز)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                ModulePermission.entries.forEach { perm ->
                    val isChecked = customPermissions.contains(perm.code)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRole = "CUSTOM"
                                customPermissions = if (isChecked) customPermissions - perm.code else customPermissions + perm.code
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedRole = "CUSTOM"
                                customPermissions = if (checked) customPermissions + perm.code else customPermissions - perm.code
                            },
                            colors = CheckboxDefaults.colors(checkedColor = PakEmeraldPrimary)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${perm.titleEn} • ${perm.titleUrdu}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = perm.description,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = staff.copy(
                        staffCode = staffCode.ifBlank { staff.staffCode },
                        loginPin = loginPin.ifBlank { "1234" },
                        accessLevel = selectedRole,
                        permissions = customPermissions.joinToString(",")
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save RBAC Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
