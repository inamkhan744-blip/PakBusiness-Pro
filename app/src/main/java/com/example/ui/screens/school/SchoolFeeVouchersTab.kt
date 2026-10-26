package com.example.ui.screens.school

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.FeeVoucherEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolFeeVouchersTab(
    activeBusiness: BusinessEntity?,
    students: List<StudentEntity>,
    vouchers: List<FeeVoucherEntity>,
    onGenerateBulk: (monthYear: String, dueDate: Long, examFee: Double, labFee: Double, (Int) -> Unit) -> Unit,
    onSaveVoucher: (FeeVoucherEntity) -> Unit,
    onMarkPaid: (Long, String) -> Unit,
    onDeleteVoucher: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Paid", "Unpaid", "Overdue"
    var selectedMonthFilter by remember { mutableStateOf("All") }

    var showBulkGenerateDialog by remember { mutableStateOf(false) }
    var voucherToPay by remember { mutableStateOf<FeeVoucherEntity?>(null) }
    var voucherToViewSlip by remember { mutableStateOf<FeeVoucherEntity?>(null) }
    var voucherToDelete by remember { mutableStateOf<FeeVoucherEntity?>(null) }
    var showCreateSingleVoucherDialog by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()

    // Distinct months list
    val monthOptions = remember(vouchers) {
        val list = mutableListOf("All")
        vouchers.map { it.monthYear }.distinct().forEach { list.add(it) }
        list
    }

    // Filter vouchers
    val filteredVouchers = remember(vouchers, searchQuery, selectedStatusFilter, selectedMonthFilter) {
        vouchers.filter { v ->
            val matchesSearch = searchQuery.isBlank() ||
                v.studentName.contains(searchQuery, ignoreCase = true) ||
                v.rollNo.contains(searchQuery, ignoreCase = true) ||
                v.voucherNumber.contains(searchQuery, ignoreCase = true) ||
                v.className.contains(searchQuery, ignoreCase = true)

            val matchesMonth = selectedMonthFilter == "All" || v.monthYear == selectedMonthFilter

            val matchesStatus = when (selectedStatusFilter) {
                "Paid" -> v.isPaid
                "Unpaid" -> !v.isPaid
                "Overdue" -> v.isOverdue(now)
                else -> true
            }

            matchesSearch && matchesMonth && matchesStatus
        }
    }

    // Financial Metrics
    val totalCollected = remember(vouchers) {
        vouchers.filter { it.isPaid }.sumOf { it.totalAmount }
    }
    val totalPending = remember(vouchers) {
        vouchers.filter { !it.isPaid }.sumOf { it.totalAmount }
    }
    val overdueCount = remember(vouchers) {
        vouchers.count { it.isOverdue(now) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Stats Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Collected
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Collected", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00381F))
                    Text(
                        text = "Rs ${String.format(Locale.US, "%,.0f", totalCollected)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00381F)
                    )
                }
            }

            // Pending
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = PakGoldContainer)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Pending Due", style = MaterialTheme.typography.labelSmall, color = Color(0xFF331E00))
                    Text(
                        text = "Rs ${String.format(Locale.US, "%,.0f", totalPending)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF331E00)
                    )
                }
            }

            // Overdue
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = if (overdueCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Overdue", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$overdueCount Chln",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Buttons Row: Generate Monthly Vouchers & Single
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showBulkGenerateDialog = true },
                modifier = Modifier.weight(1.3f).testTag("bulk_generate_vouchers_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Monthly Fee", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { showCreateSingleVoucherDialog = true },
                modifier = Modifier.weight(1f).testTag("single_voucher_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom Slip", fontSize = 13.sp)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("voucher_search_input"),
            placeholder = { Text("Search by Student, Roll #, Voucher #...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Status & Month Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val statusFilters = listOf("All", "Paid", "Unpaid", "Overdue")
            items(statusFilters) { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status },
                    label = { Text(status, fontSize = 12.sp) }
                )
            }

            if (monthOptions.size > 2) {
                items(monthOptions.filter { it != "All" }) { month ->
                    FilterChip(
                        selected = selectedMonthFilter == month,
                        onClick = {
                            selectedMonthFilter = if (selectedMonthFilter == month) "All" else month
                        },
                        label = { Text(month, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Voucher List
        if (filteredVouchers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (vouchers.isEmpty()) "No fee vouchers generated yet" else "No vouchers match your filter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredVouchers, key = { it.id }) { voucher ->
                    VoucherItemCard(
                        voucher = voucher,
                        onMarkPaid = { voucherToPay = voucher },
                        onViewSlip = { voucherToViewSlip = voucher },
                        onDelete = { voucherToDelete = voucher }
                    )
                }
            }
        }
    }

    // Bulk Voucher Generation Dialog
    if (showBulkGenerateDialog) {
        BulkGenerateFeeDialog(
            students = students,
            onDismiss = { showBulkGenerateDialog = false },
            onGenerate = { monthYear, dueDate, examFee, labFee ->
                onGenerateBulk(monthYear, dueDate, examFee, labFee) { count ->
                    Toast.makeText(context, "Generated $count Fee Vouchers for $monthYear", Toast.LENGTH_LONG).show()
                }
                showBulkGenerateDialog = false
            }
        )
    }

    // Mark Paid Dialog
    voucherToPay?.let { v ->
        MarkFeePaidDialog(
            voucher = v,
            onDismiss = { voucherToPay = null },
            onConfirm = { paymentMethod ->
                onMarkPaid(v.id, paymentMethod)
                voucherToPay = null
                Toast.makeText(context, "Marked ${v.voucherNumber} as PAID via $paymentMethod", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // View / Print Slip Dialog
    voucherToViewSlip?.let { v ->
        FeeChallanSlipDialog(
            voucher = v,
            business = activeBusiness,
            onDismiss = { voucherToViewSlip = null }
        )
    }

    // Delete Voucher Confirmation
    voucherToDelete?.let { v ->
        AlertDialog(
            onDismissRequest = { voucherToDelete = null },
            title = { Text("Delete Fee Voucher") },
            text = { Text("Are you sure you want to delete voucher #${v.voucherNumber} for ${v.studentName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVoucher(v.id)
                        voucherToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { voucherToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Single Voucher Dialog
    if (showCreateSingleVoucherDialog) {
        CreateCustomVoucherDialog(
            students = students,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = { showCreateSingleVoucherDialog = false },
            onConfirm = {
                onSaveVoucher(it)
                showCreateSingleVoucherDialog = false
            }
        )
    }
}

@Composable
fun VoucherItemCard(
    voucher: FeeVoucherEntity,
    onMarkPaid: () -> Unit,
    onViewSlip: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = voucher.isOverdue(now)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voucher_card_${voucher.voucherNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Voucher # & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = voucher.voucherNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Badge
                if (voucher.isPaid) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PakEmeraldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00381F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PAID (${voucher.paymentMethod.ifBlank { "Cash" }})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00381F)
                            )
                        }
                    }
                } else if (isOverdue) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OVERDUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PakGoldContainer
                    ) {
                        Text(
                            text = "DUE: ${voucher.formattedDueDate}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF331E00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Name & Class
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${voucher.studentName} (Roll #${voucher.rollNo})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${voucher.className} • ${voucher.section}  |  ${voucher.monthYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total PKR
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs ${String.format(Locale.US, "%,.0f", voucher.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                    if (voucher.examFee > 0 || voucher.labOrGenCharges > 0) {
                        Text(
                            text = "Tuition: Rs ${voucher.tuitionFee.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onViewSlip,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("view_challan_${voucher.voucherNumber}")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Challan", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!voucher.isPaid) {
                        Button(
                            onClick = onMarkPaid,
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("pay_voucher_${voucher.voucherNumber}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive Fee", fontSize = 12.sp)
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BulkGenerateFeeDialog(
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onGenerate: (monthYear: String, dueDate: Long, examFee: Double, labFee: Double) -> Unit
) {
    val currentMonthStr = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }
    var monthYear by remember { mutableStateOf(currentMonthStr) }
    var examFeeStr by remember { mutableStateOf("0") }
    var labFeeStr by remember { mutableStateOf("0") }
    var dueDaysFromNowStr by remember { mutableStateOf("10") }

    val activeStudentsCount = remember(students) { students.count { it.isActive } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Generate Monthly Vouchers", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "This will generate individual fee challans for all $activeStudentsCount enrolled active students with one tap.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Fee Month & Year") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = examFeeStr,
                        onValueChange = { examFeeStr = it },
                        label = { Text("Exam Fee (PKR)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = labFeeStr,
                        onValueChange = { labFeeStr = it },
                        label = { Text("Lab/Gen (PKR)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = dueDaysFromNowStr,
                    onValueChange = { dueDaysFromNowStr = it },
                    label = { Text("Due in Days (e.g. 10th of Month)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = dueDaysFromNowStr.toLongOrNull() ?: 10L
                    val dueTimestamp = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000)
                    val examFee = examFeeStr.toDoubleOrNull() ?: 0.0
                    val labFee = labFeeStr.toDoubleOrNull() ?: 0.0
                    onGenerate(monthYear.trim(), dueTimestamp, examFee, labFee)
                },
                modifier = Modifier.testTag("confirm_bulk_generate")
            ) {
                Text("Generate Now ($activeStudentsCount)")
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
fun MarkFeePaidDialog(
    voucher: FeeVoucherEntity,
    onDismiss: () -> Unit,
    onConfirm: (paymentMethod: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Cash") }
    val paymentMethods = listOf("Cash", "JazzCash", "EasyPaisa", "Bank Deposit (Meezan / Alfalah)", "Cheque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Receive Fee Payment", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = voucher.studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Challan: #${voucher.voucherNumber}  |  ${voucher.monthYear}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Amount: Rs ${String.format(Locale.US, "%,.0f", voucher.totalAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )
                    }
                }

                Text("Select Payment Method:", style = MaterialTheme.typography.labelMedium)
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMethod) },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("confirm_receive_fee_button")
            ) {
                Text("Mark as Paid")
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
fun FeeChallanSlipDialog(
    voucher: FeeVoucherEntity,
    business: BusinessEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val challanText = remember(voucher, business) {
        """
        ========================================
        ${business?.name ?: "PAKISTAN SCHOOL & ACADEMY SYSTEM"}
        ${business?.address ?: "Main Campus, Pakistan"}
        Phone: ${business?.phone ?: "0300-1234567"}
        ========================================
        FEE CHALLAN / VOUCHER
        Voucher #: ${voucher.voucherNumber}
        Month: ${voucher.monthYear}
        Issue Date: ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(voucher.issueDate))}
        Due Date: ${voucher.formattedDueDate}
        ----------------------------------------
        STUDENT PARTICULARS:
        Student Name: ${voucher.studentName}
        Father Name:  ${voucher.fatherName}
        Roll Number:  ${voucher.rollNo}
        Class & Sec:  ${voucher.className} - ${voucher.section}
        ----------------------------------------
        FEE BREAKDOWN:
        1. Tuition Fee:       Rs ${String.format(Locale.US, "%,.2f", voucher.tuitionFee)}
        2. Exam / Test Fee:   Rs ${String.format(Locale.US, "%,.2f", voucher.examFee)}
        3. Lab/Gen Charges:   Rs ${String.format(Locale.US, "%,.2f", voucher.labOrGenCharges)}
        ----------------------------------------
        TOTAL PAYABLE:        Rs ${String.format(Locale.US, "%,.2f", voucher.totalAmount)}
        STATUS:               ${if (voucher.isPaid) "PAID (${voucher.paymentMethod})" else "UNPAID"}
        ========================================
        INSTRUCTIONS:
        1. Deposit at School Accounts or via JazzCash/EasyPaisa.
        2. Keep student copy for records.
        3. Late fee fine applies after due date.
        ========================================
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fee Challan Preview", fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Fee Challan", challanText))
                        Toast.makeText(context, "Challan copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, challanText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Fee Challan"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAF9), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFDDE3DD), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                item {
                    Text(
                        text = challanText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color.Black
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun CreateCustomVoucherDialog(
    students: List<StudentEntity>,
    businessId: Long,
    onDismiss: () -> Unit,
    onConfirm: (FeeVoucherEntity) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var monthYear by remember {
        mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()))
    }
    var tuitionFeeStr by remember {
        mutableStateOf(selectedStudent?.monthlyFee?.toInt()?.toString() ?: "3500")
    }
    var examFeeStr by remember { mutableStateOf("0") }
    var labFeeStr by remember { mutableStateOf("0") }
    var dueDaysStr by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Voucher", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Student:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students) { s ->
                        FilterChip(
                            selected = selectedStudent?.id == s.id,
                            onClick = {
                                selectedStudent = s
                                tuitionFeeStr = s.monthlyFee.toInt().toString()
                            },
                            label = { Text("${s.name} (${s.rollNo})", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Fee Month") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tuitionFeeStr,
                        onValueChange = { tuitionFeeStr = it },
                        label = { Text("Tuition Fee") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = examFeeStr,
                        onValueChange = { examFeeStr = it },
                        label = { Text("Exam Fee") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = labFeeStr,
                    onValueChange = { labFeeStr = it },
                    label = { Text("Lab / Generator Charges") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent ?: return@Button
                    val tuition = tuitionFeeStr.toDoubleOrNull() ?: s.monthlyFee
                    val exam = examFeeStr.toDoubleOrNull() ?: 0.0
                    val lab = labFeeStr.toDoubleOrNull() ?: 0.0
                    val total = tuition + exam + lab
                    val now = System.currentTimeMillis()
                    val days = dueDaysStr.toLongOrNull() ?: 10L
                    val due = now + (days * 24L * 60 * 60 * 1000)
                    val voucherNum = "VCH-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now))}-${s.rollNo}"

                    val entity = FeeVoucherEntity(
                        businessId = businessId,
                        voucherNumber = voucherNum,
                        studentId = s.id,
                        studentName = s.name,
                        rollNo = s.rollNo,
                        className = s.className,
                        section = s.section,
                        fatherName = s.fatherName,
                        monthYear = monthYear,
                        tuitionFee = tuition,
                        examFee = exam,
                        labOrGenCharges = lab,
                        totalAmount = total,
                        issueDate = now,
                        dueDate = due,
                        isPaid = false
                    )
                    onConfirm(entity)
                }
            ) {
                Text("Create Voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
