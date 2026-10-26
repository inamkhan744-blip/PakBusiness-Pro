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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ChequeDirection(val label: String, val urduLabel: String) {
    RECEIVED("Received (Customer Cheque)", "گاہک سے وصول شدہ"),
    ISSUED("Issued (Supplier Cheque)", "وینڈر کو جاری کردہ")
}

enum class ChequeStatus(val label: String, val urduLabel: String, val color: Color) {
    PENDING("PDC Due", "زیر التواء", Color(0xFFD97706)),
    DEPOSITED("Deposited in Bank", "بینک میں جمع", Color(0xFF0284C7)),
    CLEARED("Cleared / Cashed", "کیش ہو گیا", Color(0xFF16A34A)),
    BOUNCED("Bounced / Returned", "باؤنس ہو گیا", Color(0xFFDC2626))
}

data class ChequeItem(
    val id: String = UUID.randomUUID().toString(),
    val chequeNumber: String,
    val bankName: String,
    val partyName: String,
    val partyPhone: String,
    val amount: Double,
    val dueDate: String,
    val direction: ChequeDirection,
    var status: ChequeStatus,
    val notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequeRegisterScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val cheques = remember {
        mutableStateListOf(
            ChequeItem(
                chequeNumber = "44892102",
                bankName = "Meezan Bank Ltd",
                partyName = "Al-Razi Pharma Distributors",
                partyPhone = "0300-8889911",
                amount = 125000.0,
                dueDate = "20 Oct 2026",
                direction = ChequeDirection.ISSUED,
                status = ChequeStatus.PENDING,
                notes = "Invoice #INV-9921 settlement"
            ),
            ChequeItem(
                chequeNumber = "00874125",
                bankName = "Habib Bank Limited (HBL)",
                partyName = "Haji Aslam & Sons",
                partyPhone = "0321-1234567",
                amount = 85000.0,
                dueDate = "16 Oct 2026",
                direction = ChequeDirection.RECEIVED,
                status = ChequeStatus.DEPOSITED,
                notes = "Advance for building supplies"
            ),
            ChequeItem(
                chequeNumber = "99823145",
                bankName = "Bank Alfalah",
                partyName = "Tariq Auto Traders",
                partyPhone = "0333-9876543",
                amount = 45000.0,
                dueDate = "10 Oct 2026",
                direction = ChequeDirection.RECEIVED,
                status = ChequeStatus.CLEARED,
                notes = "Cleared into Meezan current account"
            ),
            ChequeItem(
                chequeNumber = "11234890",
                bankName = "MCB Bank Ltd",
                partyName = "National Traders",
                partyPhone = "0345-5556677",
                amount = 32000.0,
                dueDate = "05 Oct 2026",
                direction = ChequeDirection.RECEIVED,
                status = ChequeStatus.BOUNCED,
                notes = "Signatures mismatch - re-issue requested"
            )
        )
    }

    val displayedCheques = remember(selectedTabIndex, cheques.toList()) {
        when (selectedTabIndex) {
            1 -> cheques.filter { it.direction == ChequeDirection.RECEIVED }
            2 -> cheques.filter { it.direction == ChequeDirection.ISSUED }
            3 -> cheques.filter { it.status == ChequeStatus.PENDING || it.status == ChequeStatus.DEPOSITED }
            4 -> cheques.filter { it.status == ChequeStatus.BOUNCED }
            else -> cheques
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cheque & Bank Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "چیک بک، پی ڈی سی اور بینک کلیئرنس ٹریکر",
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
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Cheque", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Metrics Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pendingTotal = cheques.filter { it.status == ChequeStatus.PENDING || it.status == ChequeStatus.DEPOSITED }.sumOf { it.amount }
                    val clearedTotal = cheques.filter { it.status == ChequeStatus.CLEARED }.sumOf { it.amount }
                    val bouncedTotal = cheques.filter { it.status == ChequeStatus.BOUNCED }.sumOf { it.amount }

                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PDC In Hand", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format("%,.0f", pendingTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }

                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Cleared to Bank", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format("%,.0f", clearedTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }

                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Bounced / Alert", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format("%,.0f", bouncedTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("All (${cheques.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Received", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Issued", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 3, onClick = { selectedTabIndex = 3 }, text = { Text("PDC Due", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabIndex == 4, onClick = { selectedTabIndex = 4 }, text = { Text("Bounced", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedCheques, key = { it.id }) { cheque ->
                    ChequeCard(
                        cheque = cheque,
                        currency = currency,
                        onUpdateStatus = { newStatus ->
                            cheque.status = newStatus
                            Toast.makeText(context, "Cheque #${cheque.chequeNumber} marked as ${newStatus.label}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { cheques.remove(cheque) },
                        onShareParty = {
                            val msg = "*CHEQUE UPDATE: ${cheque.status.label}*\nCheque #${cheque.chequeNumber}\nBank: ${cheque.bankName}\nAmount: $currency ${String.format("%,.0f", cheque.amount)}\nDue Date: ${cheque.dueDate}\nStatus: ${cheque.status.urduLabel}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Cheque Details"))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddChequeDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onSave = { newCheque ->
                cheques.add(0, newCheque)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ChequeCard(
    cheque: ChequeItem,
    currency: String,
    onUpdateStatus: (ChequeStatus) -> Unit,
    onDelete: () -> Unit,
    onShareParty: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Surface(
                        shape = CircleShape,
                        color = if (cheque.direction == ChequeDirection.RECEIVED) PakEmeraldContainer else Color(0xFFEFF6FF),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (cheque.direction == ChequeDirection.RECEIVED) PakEmeraldPrimary else Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text("Cheque #${cheque.chequeNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(cheque.bankName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = cheque.status.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = cheque.status.urduLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = cheque.status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Party: ${cheque.partyName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Due Clearance: 📅 ${cheque.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (cheque.notes.isNotBlank()) {
                        Text("Note: ${cheque.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(cheque.direction.urduLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$currency ${String.format("%,.0f", cheque.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (cheque.direction == ChequeDirection.RECEIVED) PakEmeraldPrimary else Color(0xFFB91C1C)
                    )
                }
            }

            // Quick Status Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (cheque.status == ChequeStatus.PENDING) {
                    Button(
                        onClick = { onUpdateStatus(ChequeStatus.DEPOSITED) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Deposit in Bank", fontSize = 10.sp)
                    }
                }
                if (cheque.status == ChequeStatus.DEPOSITED || cheque.status == ChequeStatus.PENDING) {
                    Button(
                        onClick = { onUpdateStatus(ChequeStatus.CLEARED) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Mark Cleared", fontSize = 10.sp)
                    }
                }
                if (cheque.status != ChequeStatus.BOUNCED && cheque.status != ChequeStatus.CLEARED) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(ChequeStatus.BOUNCED) },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Bounce", fontSize = 10.sp, color = Color.Red)
                    }
                }

                IconButton(onClick = onShareParty, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddChequeDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ChequeItem) -> Unit
) {
    var chequeNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("Meezan Bank Ltd") }
    var partyName by remember { mutableStateOf("") }
    var partyPhone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var direction by remember { mutableStateOf(ChequeDirection.RECEIVED) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Cheque (چیک درج کریں)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == ChequeDirection.RECEIVED,
                        onClick = { direction = ChequeDirection.RECEIVED },
                        label = { Text("Customer Cheque", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = direction == ChequeDirection.ISSUED,
                        onClick = { direction = ChequeDirection.ISSUED },
                        label = { Text("Supplier Cheque", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = chequeNumber,
                    onValueChange = { chequeNumber = it },
                    label = { Text("Cheque Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name (HBL, Meezan, MCB...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (direction == ChequeDirection.RECEIVED) "Customer Name" else "Supplier Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currency)") },
                        modifier = Modifier.weight(1.2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Clearance Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Invoice Ref") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chequeNumber.isNotBlank() && partyName.isNotBlank() && amountText.isNotBlank()) {
                        val cheque = ChequeItem(
                            chequeNumber = chequeNumber.trim(),
                            bankName = bankName.trim(),
                            partyName = partyName.trim(),
                            partyPhone = partyPhone.trim(),
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            dueDate = dueDate.trim(),
                            direction = direction,
                            status = ChequeStatus.PENDING,
                            notes = notes.trim()
                        )
                        onSave(cheque)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save Cheque")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
