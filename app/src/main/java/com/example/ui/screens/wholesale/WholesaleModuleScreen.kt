package com.example.ui.screens.wholesale

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WholesaleBulkOrderEntity
import com.example.data.WholesalePartyEntity
import com.example.data.WholesalePaymentEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesaleModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsState()
    val parties by viewModel.wholesaleParties.collectAsState()
    val bulkOrders by viewModel.wholesaleBulkOrders.collectAsState()
    val payments by viewModel.wholesalePayments.collectAsState()

    val currency = activeBusiness?.currency ?: "PKR"

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Parties & Khata", "Bulk Orders", "Payment Entries")

    // Dialog States
    var showAddPartyDialog by remember { mutableStateOf(false) }
    var partyToEdit by remember { mutableStateOf<WholesalePartyEntity?>(null) }
    var showNewBulkOrderDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var preselectedPartyForPayment by remember { mutableStateOf<WholesalePartyEntity?>(null) }

    // KPI computations
    val totalReceivable = parties.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    val totalPayable = parties.filter { it.currentBalance < 0 }.sumOf { -it.currentBalance }
    val activeOrdersCount = bulkOrders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("wholesale_module_screen")
    ) {
        // Wholesale Header KPIs
        Card(
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Wholesale & Distribution", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Udhaar/Khata Ledger & Bilty Tracking", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${parties.size} Parties",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Customer Receivables (They owe us)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(12.dp))
                                Text("Receivables (Udhaar)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$currency ${String.format(Locale.getDefault(), "%,.0f", totalReceivable)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFA5D6A7)
                            )
                        }
                    }

                    // Total Supplier Payables (We owe them)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFFFAB91), modifier = Modifier.size(12.dp))
                                Text("Payables (Due)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$currency ${String.format(Locale.getDefault(), "%,.0f", totalPayable)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFFFAB91)
                            )
                        }
                    }

                    // Active Bulk Orders
                    Card(
                        modifier = Modifier.weight(0.8f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pending Orders", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$activeOrdersCount active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }
        }

        // Secondary Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PakEmeraldPrimary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> WholesalePartiesTab(
                    parties = parties,
                    currency = currency,
                    onAddNewParty = {
                        partyToEdit = null
                        showAddPartyDialog = true
                    },
                    onEditParty = { party ->
                        partyToEdit = party
                        showAddPartyDialog = true
                    },
                    onDeleteParty = { viewModel.deleteWholesaleParty(it) },
                    onQuickPayment = { party ->
                        preselectedPartyForPayment = party
                        showRecordPaymentDialog = true
                    }
                )
                1 -> WholesaleBulkOrdersTab(
                    orders = bulkOrders,
                    parties = parties,
                    currency = currency,
                    onAddNewOrder = { showNewBulkOrderDialog = true },
                    onUpdateStatus = { orderId, newStatus ->
                        viewModel.updateWholesaleOrderStatus(orderId, newStatus)
                    },
                    onDeleteOrder = { viewModel.deleteWholesaleBulkOrder(it) }
                )
                2 -> WholesalePaymentsTab(
                    payments = payments,
                    parties = parties,
                    currency = currency,
                    onAddNewPayment = {
                        preselectedPartyForPayment = null
                        showRecordPaymentDialog = true
                    },
                    onDeletePayment = { viewModel.deleteWholesalePayment(it) }
                )
            }
        }
    }

    // Add / Edit Party Dialog
    if (showAddPartyDialog) {
        AddEditPartyDialog(
            party = partyToEdit,
            currency = currency,
            onDismiss = { showAddPartyDialog = false },
            onSave = { party ->
                viewModel.saveWholesaleParty(party)
                showAddPartyDialog = false
            }
        )
    }

    // New Bulk Order Dialog
    if (showNewBulkOrderDialog) {
        NewBulkOrderDialog(
            parties = parties,
            currency = currency,
            onDismiss = { showNewBulkOrderDialog = false },
            onSave = { order ->
                viewModel.saveWholesaleBulkOrder(order)
                showNewBulkOrderDialog = false
            }
        )
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog) {
        RecordWholesalePaymentDialog(
            parties = parties,
            preselectedParty = preselectedPartyForPayment,
            currency = currency,
            onDismiss = { showRecordPaymentDialog = false },
            onSave = { payment ->
                viewModel.recordWholesalePayment(payment)
                showRecordPaymentDialog = false
            }
        )
    }
}

@Composable
fun WholesalePartiesTab(
    parties: List<WholesalePartyEntity>,
    currency: String,
    onAddNewParty: () -> Unit,
    onEditParty: (WholesalePartyEntity) -> Unit,
    onDeleteParty: (WholesalePartyEntity) -> Unit,
    onQuickPayment: (WholesalePartyEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // "ALL", "CUSTOMER", "SUPPLIER", "UDHAAR"

    val filteredParties = parties.filter { party ->
        val matchesSearch = party.name.contains(searchQuery, ignoreCase = true) ||
                party.contactPerson.contains(searchQuery, ignoreCase = true) ||
                party.phone.contains(searchQuery, ignoreCase = true) ||
                party.city.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterType) {
            "CUSTOMER" -> party.partyType == "CUSTOMER" || party.partyType == "BOTH"
            "SUPPLIER" -> party.partyType == "SUPPLIER" || party.partyType == "BOTH"
            "UDHAAR" -> party.currentBalance != 0.0
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search party by name, contact or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            label = { Text("All Parties (${parties.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "CUSTOMER",
                            onClick = { filterType = "CUSTOMER" },
                            label = { Text("Customers (${parties.count { it.partyType == "CUSTOMER" || it.partyType == "BOTH" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "SUPPLIER",
                            onClick = { filterType = "SUPPLIER" },
                            label = { Text("Suppliers (${parties.count { it.partyType == "SUPPLIER" || it.partyType == "BOTH" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "UDHAAR",
                            onClick = { filterType = "UDHAAR" },
                            label = { Text("With Udhaar/Balance (${parties.count { it.currentBalance != 0.0 }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                }
            }

            if (filteredParties.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Text("No wholesale parties found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Add dealers, distributors, or suppliers to track their Udhaar & Khata ledger.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredParties, key = { it.id }) { party ->
                    PartyLedgerCard(
                        party = party,
                        currency = currency,
                        onEdit = { onEditParty(party) },
                        onDelete = { onDeleteParty(party) },
                        onQuickPayment = { onQuickPayment(party) }
                    )
                }
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddNewParty,
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_wholesale_party_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Party", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PartyLedgerCard(
    party: WholesalePartyEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickPayment: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(party.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (party.partyType) {
                                "CUSTOMER" -> PakEmeraldContainer
                                "SUPPLIER" -> Color(0xFFFFECB3)
                                else -> Color(0xFFE1BEE7)
                            }
                        ) {
                            Text(
                                text = party.partyType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (party.partyType) {
                                    "CUSTOMER" -> PakEmeraldPrimary
                                    "SUPPLIER" -> Color(0xFFE65100)
                                    else -> Color(0xFF6A1B9A)
                                },
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (party.contactPerson.isNotBlank()) {
                        Text("Contact: ${party.contactPerson} • ${party.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(party.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (party.address.isNotBlank()) {
                        Text("${party.address}, ${party.city}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                }

                // Balance Box
                Column(horizontalAlignment = Alignment.End) {
                    val balance = party.currentBalance
                    if (balance > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.End) {
                                Text("Receivable", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", balance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    } else if (balance < 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.End) {
                                Text("Payable", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", -balance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                            }
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("Nil / Settled", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    if (party.creditLimit > 0) {
                        Text("Limit: $currency ${String.format(Locale.getDefault(), "%,.0f", party.creditLimit)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (party.taxNtnNumber.isNotBlank()) {
                    Text("NTN: ${party.taxNtnNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Credit: ${party.paymentTermsDays} Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onQuickPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Khata Payment", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = {
                            val statusNote = if (party.currentBalance > 0) "RECEIVABLE (UDHAAR)" else if (party.currentBalance < 0) "PAYABLE TO SUPPLIER" else "NIL / SETTLED"
                            val stmt = """
                                ══════════════════════════
                                WHOLESALE KHATA STATEMENT
                                ══════════════════════════
                                Party: ${party.name} (${party.partyType})
                                Contact: ${party.contactPerson} • ${party.phone}
                                Address: ${party.address}, ${party.city}
                                ──────────────────────────
                                Outstanding Balance: $currency ${String.format(Locale.getDefault(), "%,.2f", party.currentBalance)}
                                Ledger Status: $statusNote
                                Payment Credit Limit: $currency ${String.format(Locale.getDefault(), "%,.0f", party.creditLimit)}
                                Credit Terms: ${party.paymentTermsDays} Days
                                ══════════════════════════
                                Sent via PakBusiness Pro
                            """.trimIndent()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, stmt)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Khata Statement via WhatsApp"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Statement", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PakEmeraldPrimary, modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WholesaleBulkOrdersTab(
    orders: List<WholesaleBulkOrderEntity>,
    parties: List<WholesalePartyEntity>,
    currency: String,
    onAddNewOrder: () -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onDeleteOrder: (WholesaleBulkOrderEntity) -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") }

    val filteredOrders = orders.filter { order ->
        when (filterType) {
            "SALE" -> order.orderType == "SALE"
            "PURCHASE" -> order.orderType == "PURCHASE"
            "PENDING" -> order.status != "DELIVERED" && order.status != "CANCELLED"
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            label = { Text("All Orders (${orders.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "SALE",
                            onClick = { filterType = "SALE" },
                            label = { Text("Wholesale Sales (${orders.count { it.orderType == "SALE" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "PURCHASE",
                            onClick = { filterType = "PURCHASE" },
                            label = { Text("Bulk Purchases (${orders.count { it.orderType == "PURCHASE" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "PENDING",
                            onClick = { filterType = "PENDING" },
                            label = { Text("Dispatched/Pending (${orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }})") }
                        )
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Text("No bulk orders found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Create bulk shipments, transport bilty orders, and supply entries.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    BulkOrderCard(
                        order = order,
                        currency = currency,
                        onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) },
                        onDelete = { onDeleteOrder(order) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddNewOrder,
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_bulk_order_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("New Bulk Order", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BulkOrderCard(
    order: WholesaleBulkOrderEntity,
    currency: String,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (order.orderType == "SALE") PakEmeraldContainer else Color(0xFFFFE0B2)
                    ) {
                        Text(
                            text = if (order.orderType == "SALE") "BULK SALE" else "BULK PURCHASE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.orderType == "SALE") PakEmeraldPrimary else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (order.status) {
                        "DELIVERED" -> Color(0xFFE8F5E9)
                        "DISPATCHED" -> Color(0xFFE3F2FD)
                        "CANCELLED" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF9C4)
                    }
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            "DELIVERED" -> Color(0xFF2E7D32)
                            "DISPATCHED" -> Color(0xFF1565C0)
                            "CANCELLED" -> Color(0xFFC62828)
                            else -> Color(0xFFF57F17)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text("Party: ${order.partyName} (${order.partyPhone})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

            if (order.itemsSummary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = order.itemsSummary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (order.vehicleNumberOrBilty.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(14.dp))
                    Text("Bilty / Transport: ${order.vehicleNumberOrBilty}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Grand Total: $currency ${String.format(Locale.getDefault(), "%,.0f", order.grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Paid: $currency ${String.format(Locale.getDefault(), "%,.0f", order.paidAmount)} • Udhaar: $currency ${String.format(Locale.getDefault(), "%,.0f", order.creditBalanceAdded)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (order.status != "DELIVERED") {
                        OutlinedButton(
                            onClick = {
                                val nextStatus = if (order.status == "CONFIRMED") "DISPATCHED" else "DELIVERED"
                                onUpdateStatus(nextStatus)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (order.status == "CONFIRMED") "Dispatch" else "Deliver", fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = {
                            val slip = """
                                ══════════════════════════
                                WHOLESALE ORDER & BILTY SLIP
                                ══════════════════════════
                                Order No: ${order.orderNumber} (${order.orderType})
                                Party: ${order.partyName} (${order.partyPhone})
                                Status: ${order.status}
                                Bilty / Transport: ${order.vehicleNumberOrBilty.ifBlank { "Direct Delivery" }}
                                ──────────────────────────
                                Items:
                                ${order.itemsSummary}
                                Total Cartons/Units: ${order.totalUnitsCount}
                                ──────────────────────────
                                Grand Total: $currency ${String.format(Locale.getDefault(), "%,.2f", order.grandTotal)}
                                Paid: $currency ${String.format(Locale.getDefault(), "%,.2f", order.paidAmount)}
                                Udhaar Added: $currency ${String.format(Locale.getDefault(), "%,.2f", order.creditBalanceAdded)}
                                ══════════════════════════
                                Sent via PakBusiness Pro
                            """.trimIndent()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, slip)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Order Bilty Slip"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Slip", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WholesalePaymentsTab(
    payments: List<WholesalePaymentEntity>,
    parties: List<WholesalePartyEntity>,
    currency: String,
    onAddNewPayment: () -> Unit,
    onDeletePayment: (WholesalePaymentEntity) -> Unit
) {
    val totalReceipts = payments.filter { it.paymentType == "RECEIPT" }.sumOf { it.amount }
    val totalDisbursements = payments.filter { it.paymentType == "PAYMENT" }.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Collections (Receipts)", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalReceipts)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Paid to Suppliers", fontSize = 10.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalDisbursements)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        }
                    }
                }
            }

            if (payments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Text("No payment records yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Record cash receipts or bank transfers to settle Udhaar/Khata.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(payments, key = { it.id }) { payment ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(payment.voucherNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (payment.paymentType == "RECEIPT") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            text = if (payment.paymentType == "RECEIPT") "RECEIPT (IN)" else "PAYMENT (OUT)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (payment.paymentType == "RECEIPT") Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Party: ${payment.partyName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${payment.paymentMethod} • Date: ${payment.paymentDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (payment.bankAccountOrChequeNo.isNotBlank()) {
                                    Text("Ref: ${payment.bankAccountOrChequeNo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", payment.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (payment.paymentType == "RECEIPT") Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                IconButton(onClick = { onDeletePayment(payment) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddNewPayment,
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_wholesale_payment_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Record Payment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dialog: Add / Edit Wholesale Party
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPartyDialog(
    party: WholesalePartyEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (WholesalePartyEntity) -> Unit
) {
    var name by remember { mutableStateOf(party?.name ?: "") }
    var contactPerson by remember { mutableStateOf(party?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(party?.phone ?: "") }
    var address by remember { mutableStateOf(party?.address ?: "") }
    var city by remember { mutableStateOf(party?.city ?: "Lahore") }
    var partyType by remember { mutableStateOf(party?.partyType ?: "CUSTOMER") }
    var creditLimitStr by remember { mutableStateOf((party?.creditLimit ?: 500000.0).toString()) }
    var initialBalanceStr by remember { mutableStateOf((party?.currentBalance ?: 0.0).toString()) }
    var ntn by remember { mutableStateOf(party?.taxNtnNumber ?: "") }
    var paymentTermsDaysStr by remember { mutableStateOf((party?.paymentTermsDays ?: 15).toString()) }
    var notes by remember { mutableStateOf(party?.notes ?: "") }

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }
    val typeOptions = listOf("CUSTOMER", "SUPPLIER", "BOTH")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (party == null) "Add Wholesale Party" else "Edit Party Details") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Business / Party Name *") },
                        placeholder = { Text("e.g. Madina Traders") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = isTypeDropdownExpanded,
                        onExpandedChange = { isTypeDropdownExpanded = !isTypeDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = partyType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Party Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isTypeDropdownExpanded,
                            onDismissRequest = { isTypeDropdownExpanded = false }
                        ) {
                            typeOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        partyType = opt
                                        isTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Contact Person") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone / WhatsApp *") },
                        placeholder = { Text("+92 300 1234567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Market / Address") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = creditLimitStr,
                            onValueChange = { creditLimitStr = it },
                            label = { Text("Credit Limit ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = initialBalanceStr,
                            onValueChange = { initialBalanceStr = it },
                            label = { Text("Khata Balance ($currency)") },
                            supportingText = { Text("+ Receivable, - Payable", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paymentTermsDaysStr,
                            onValueChange = { paymentTermsDaysStr = it },
                            label = { Text("Credit Days") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ntn,
                            onValueChange = { ntn = it },
                            label = { Text("NTN / STRN #") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Payment Terms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val partyEntity = WholesalePartyEntity(
                            id = party?.id ?: 0L,
                            businessId = party?.businessId ?: 0L,
                            name = name.trim(),
                            contactPerson = contactPerson.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            partyType = partyType,
                            creditLimit = creditLimitStr.toDoubleOrNull() ?: 500000.0,
                            currentBalance = initialBalanceStr.toDoubleOrNull() ?: 0.0,
                            taxNtnNumber = ntn.trim(),
                            paymentTermsDays = paymentTermsDaysStr.toIntOrNull() ?: 15,
                            notes = notes.trim()
                        )
                        onSave(partyEntity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save Party")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog: New Bulk Order Entry
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBulkOrderDialog(
    parties: List<WholesalePartyEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (WholesaleBulkOrderEntity) -> Unit
) {
    var orderNumber by remember {
        mutableStateOf("WO-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}")
    }
    var selectedPartyId by remember { mutableStateOf(parties.firstOrNull()?.id ?: 0L) }
    var orderType by remember { mutableStateOf("SALE") } // "SALE" or "PURCHASE"
    var itemsSummary by remember { mutableStateOf("") }
    var unitsCountStr by remember { mutableStateOf("10") }
    var subtotalStr by remember { mutableStateOf("50000") }
    var discountStr by remember { mutableStateOf("0") }
    var taxGstStr by remember { mutableStateOf("0") }
    var paidAmountStr by remember { mutableStateOf("10000") }
    var biltyOrVehicle by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    var isPartyDropdownExpanded by remember { mutableStateOf(false) }

    val selectedParty = parties.find { it.id == selectedPartyId }

    val subtotal = subtotalStr.toDoubleOrNull() ?: 0.0
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val tax = taxGstStr.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)
    val paid = (paidAmountStr.toDoubleOrNull() ?: 0.0).coerceAtMost(grandTotal)
    val balanceAdded = grandTotal - paid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Bulk Order Entry") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = orderType == "SALE",
                            onClick = { orderType = "SALE" },
                            label = { Text("Sale to Customer") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = orderType == "PURCHASE",
                            onClick = { orderType = "PURCHASE" },
                            label = { Text("Bulk Purchase") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = isPartyDropdownExpanded,
                        onExpandedChange = { isPartyDropdownExpanded = !isPartyDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedParty?.name ?: "Select Party",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Party (Customer / Supplier) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPartyDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isPartyDropdownExpanded,
                            onDismissRequest = { isPartyDropdownExpanded = false }
                        ) {
                            parties.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.partyType})") },
                                    onClick = {
                                        selectedPartyId = p.id
                                        isPartyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Order Number / Bilty Ref") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = itemsSummary,
                        onValueChange = { itemsSummary = it },
                        label = { Text("Items Description (e.g. 50 Cartons Ghee, 20 Sacks Rice)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unitsCountStr,
                            onValueChange = { unitsCountStr = it },
                            label = { Text("Total Cartons/Units") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = biltyOrVehicle,
                            onValueChange = { biltyOrVehicle = it },
                            label = { Text("Vehicle / Bilty #") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = subtotalStr,
                            onValueChange = { subtotalStr = it },
                            label = { Text("Subtotal ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text("Discount ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taxGstStr,
                            onValueChange = { taxGstStr = it },
                            label = { Text("GST / Tax ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("Advance Paid ($currency)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Grand Total", fontSize = 11.sp)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Added to Khata Udhaar", fontSize = 11.sp)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", balanceAdded)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PakEmeraldPrimary)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Transport Remarks / Booking Station") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedParty != null && grandTotal > 0) {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val order = WholesaleBulkOrderEntity(
                            businessId = selectedParty.businessId,
                            orderNumber = orderNumber.trim(),
                            partyId = selectedParty.id,
                            partyName = selectedParty.name,
                            partyPhone = selectedParty.phone,
                            orderType = orderType,
                            status = "CONFIRMED",
                            orderDate = currentDate,
                            vehicleNumberOrBilty = biltyOrVehicle.trim(),
                            itemsSummary = itemsSummary.trim(),
                            totalUnitsCount = unitsCountStr.toIntOrNull() ?: 0,
                            subtotal = subtotal,
                            discount = discount,
                            taxGst = tax,
                            grandTotal = grandTotal,
                            paidAmount = paid,
                            creditBalanceAdded = balanceAdded,
                            remarks = remarks.trim()
                        )
                        onSave(order)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Confirm Bulk Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Dialog: Record Payment & Adjust Khata Balance
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordWholesalePaymentDialog(
    parties: List<WholesalePartyEntity>,
    preselectedParty: WholesalePartyEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (WholesalePaymentEntity) -> Unit
) {
    var selectedPartyId by remember { mutableStateOf(preselectedParty?.id ?: parties.firstOrNull()?.id ?: 0L) }
    var paymentType by remember {
        mutableStateOf(
            if (preselectedParty?.partyType == "SUPPLIER") "PAYMENT" else "RECEIPT"
        )
    }
    var voucherNumber by remember {
        val prefix = if (paymentType == "RECEIPT") "RV" else "PV"
        mutableStateOf("$prefix-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}")
    }
    var amountStr by remember {
        val initialAmt = if (preselectedParty != null && preselectedParty.currentBalance != 0.0) {
            Math.abs(preselectedParty.currentBalance).toString()
        } else "50000"
        mutableStateOf(initialAmt)
    }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var bankOrCheque by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var isPartyDropdownExpanded by remember { mutableStateOf(false) }
    var isMethodDropdownExpanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cash", "Online Bank Transfer (Meezan/HBL)", "Cheque", "EasyPaisa / JazzCash")

    val selectedParty = parties.find { it.id == selectedPartyId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Khata Payment Entry") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paymentType == "RECEIPT",
                            onClick = {
                                paymentType = "RECEIPT"
                                voucherNumber = "RV-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
                            },
                            label = { Text("Receipt (Cash In)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = paymentType == "PAYMENT",
                            onClick = {
                                paymentType = "PAYMENT"
                                voucherNumber = "PV-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
                            },
                            label = { Text("Payment (Cash Out)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = isPartyDropdownExpanded,
                        onExpandedChange = { isPartyDropdownExpanded = !isPartyDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedParty?.name ?: "Select Party",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Party") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPartyDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isPartyDropdownExpanded,
                            onDismissRequest = { isPartyDropdownExpanded = false }
                        ) {
                            parties.forEach { p ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(p.name, fontWeight = FontWeight.Bold)
                                            Text("Balance: $currency ${String.format(Locale.getDefault(), "%,.0f", p.currentBalance)}", fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedPartyId = p.id
                                        isPartyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedParty != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Balance:", fontSize = 12.sp)
                                Text(
                                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", selectedParty.currentBalance)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedParty.currentBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = voucherNumber,
                        onValueChange = { voucherNumber = it },
                        label = { Text("Voucher Number (RV / PV)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Payment Amount ($currency) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = isMethodDropdownExpanded,
                        onExpandedChange = { isMethodDropdownExpanded = !isMethodDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Mode") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMethodDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isMethodDropdownExpanded,
                            onDismissRequest = { isMethodDropdownExpanded = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        isMethodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = bankOrCheque,
                        onValueChange = { bankOrCheque = it },
                        label = { Text("Cheque # / Online Ref ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Remarks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (selectedParty != null && amount > 0) {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val payment = WholesalePaymentEntity(
                            businessId = selectedParty.businessId,
                            voucherNumber = voucherNumber.trim(),
                            partyId = selectedParty.id,
                            partyName = selectedParty.name,
                            paymentType = paymentType,
                            amount = amount,
                            paymentMethod = paymentMethod,
                            bankAccountOrChequeNo = bankOrCheque.trim(),
                            paymentDate = currentDate,
                            notes = notes.trim()
                        )
                        onSave(payment)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save & Adjust Khata")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
