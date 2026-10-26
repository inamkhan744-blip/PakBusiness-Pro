package com.example.ui.screens.electronics

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ElectronicsProductEntity
import com.example.data.RepairTicketEntity
import com.example.ui.BusinessViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ElectronicsBlue = Color(0xFF0284C7)
private val ElectronicsDarkBlue = Color(0xFF0369A1)
private val TechCyan = Color(0xFF06B6D4)
private val HardwarePurple = Color(0xFF7C3AED)
private val StatusSuccess = Color(0xFF16A34A)
private val StatusWarning = Color(0xFFD97706)
private val StatusDanger = Color(0xFFDC2626)

enum class ElectronicsTab {
    INVENTORY_IMEI,
    REPAIR_TICKETS,
    WARRANTY_LOOKUP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicsModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.electronicsProducts.collectAsStateWithLifecycle()
    val tickets by viewModel.repairTickets.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(ElectronicsTab.INVENTORY_IMEI) }

    // Dialog states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ElectronicsProductEntity?>(null) }

    var showAddTicketDialog by remember { mutableStateOf(false) }
    var ticketToEdit by remember { mutableStateOf<RepairTicketEntity?>(null) }
    var ticketForSlip by remember { mutableStateOf<RepairTicketEntity?>(null) }
    var ticketForPayment by remember { mutableStateOf<RepairTicketEntity?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectronicsBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Electronics & Mobile Shop",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "IMEI & Warranty Tracking • Mobile Repair Lab",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            if (selectedTab == ElectronicsTab.INVENTORY_IMEI) {
                                productToEdit = null
                                showAddProductDialog = true
                            } else {
                                ticketToEdit = null
                                showAddTicketDialog = true
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectronicsBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_electronics_primary_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedTab == ElectronicsTab.INVENTORY_IMEI) "Add Device" else "New Ticket",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Module Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == ElectronicsTab.INVENTORY_IMEI,
                        onClick = { selectedTab = ElectronicsTab.INVENTORY_IMEI },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Inventory & IMEI", fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("tab_electronics_inventory")
                    )
                    Tab(
                        selected = selectedTab == ElectronicsTab.REPAIR_TICKETS,
                        onClick = { selectedTab = ElectronicsTab.REPAIR_TICKETS },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Repair Lab", fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("tab_electronics_repairs")
                    )
                    Tab(
                        selected = selectedTab == ElectronicsTab.WARRANTY_LOOKUP,
                        onClick = { selectedTab = ElectronicsTab.WARRANTY_LOOKUP },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Warranty & IMEI", fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("tab_electronics_warranty")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ElectronicsTab.INVENTORY_IMEI -> {
                    ElectronicsInventoryView(
                        products = products,
                        onEditProduct = {
                            productToEdit = it
                            showAddProductDialog = true
                        },
                        onDeleteProduct = { viewModel.deleteElectronicsProduct(it.id) },
                        onUpdateStock = { id, qty -> viewModel.updateElectronicsStock(id, qty) },
                        onAddNew = {
                            productToEdit = null
                            showAddProductDialog = true
                        }
                    )
                }
                ElectronicsTab.REPAIR_TICKETS -> {
                    ElectronicsRepairTicketsView(
                        tickets = tickets,
                        onEditTicket = {
                            ticketToEdit = it
                            showAddTicketDialog = true
                        },
                        onDeleteTicket = { viewModel.deleteRepairTicket(it.id) },
                        onUpdateStatus = { id, status -> viewModel.updateRepairTicketStatus(id, status) },
                        onPayBalance = { ticketForPayment = it },
                        onPrintSlip = { ticketForSlip = it },
                        onAddNew = {
                            ticketToEdit = null
                            showAddTicketDialog = true
                        }
                    )
                }
                ElectronicsTab.WARRANTY_LOOKUP -> {
                    WarrantyAndImeiLookupView(
                        products = products,
                        tickets = tickets
                    )
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog) {
        AddEditElectronicsProductDialog(
            product = productToEdit,
            onDismiss = { showAddProductDialog = false },
            onSave = { product ->
                viewModel.saveElectronicsProduct(product)
                showAddProductDialog = false
                Toast.makeText(context, "Product inventory saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add / Edit Repair Ticket Dialog
    if (showAddTicketDialog) {
        AddEditRepairTicketDialog(
            ticket = ticketToEdit,
            onDismiss = { showAddTicketDialog = false },
            onSave = { ticket ->
                viewModel.saveRepairTicket(ticket)
                showAddTicketDialog = false
                Toast.makeText(context, "Repair Ticket #${ticket.ticketNumber} saved", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Repair Slip Dialog
    ticketForSlip?.let { ticket ->
        RepairSlipDialog(
            ticket = ticket,
            onDismiss = { ticketForSlip = null }
        )
    }

    // Balance Payment Dialog
    ticketForPayment?.let { ticket ->
        RepairBalancePaymentDialog(
            ticket = ticket,
            onDismiss = { ticketForPayment = null },
            onConfirmPayment = { amountReceived ->
                val newAdvance = ticket.advancePaid + amountReceived
                val newBalance = (ticket.estimatedCost - newAdvance).coerceAtLeast(0.0)
                viewModel.updateRepairTicketPayment(ticket.id, newAdvance, newBalance)
                if (newBalance == 0.0 && ticket.status != "DELIVERED") {
                    viewModel.updateRepairTicketStatus(ticket.id, "DELIVERED")
                }
                ticketForPayment = null
                Toast.makeText(context, "Payment recorded. Remaining: ₨ ${newBalance.toInt()}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 1. INVENTORY & IMEI TRACKING VIEW
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicsInventoryView(
    products: List<ElectronicsProductEntity>,
    onEditProduct: (ElectronicsProductEntity) -> Unit,
    onDeleteProduct: (ElectronicsProductEntity) -> Unit,
    onUpdateStock: (Long, Int) -> Unit,
    onAddNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Smartphones", "Feature Phones", "Laptops", "Tablets", "Audio & Accessories", "Smart Watches")

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesCategory = (selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.brand.contains(searchQuery, ignoreCase = true) ||
                    prod.imei1.contains(searchQuery, ignoreCase = true) ||
                    prod.imei2.contains(searchQuery, ignoreCase = true) ||
                    prod.serialNumber.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val totalDevices = products.sumOf { it.stockQuantity }
    val totalInventoryValue = products.sumOf { it.purchasePrice * it.stockQuantity }
    val ptaApprovedCount = products.count { it.ptaStatus.contains("Approved", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("electronics_product_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    border = BorderStroke(1.dp, ElectronicsBlue.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Stock", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$totalDevices units", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ElectronicsDarkBlue)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = BorderStroke(1.dp, HardwarePurple.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Asset Value", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("₨ ${NumberFormat.getNumberInstance(Locale.US).format(totalInventoryValue.toLong())}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HardwarePurple)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PTA Approved", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$ptaApprovedCount models", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_electronics"),
                placeholder = { Text("Search by IMEI, Serial #, Brand, Model...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectronicsBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectronicsBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DevicesOther, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products or IMEI found", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Add new smartphones, laptops or accessories to start tracking IMEI and warranty.", fontSize = 12.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddNew,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Device")
                        }
                    }
                }
            }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                ElectronicsProductCard(
                    product = product,
                    onEdit = { onEditProduct(product) },
                    onDelete = { onDeleteProduct(product) },
                    onUpdateStock = { newStock -> onUpdateStock(product.id, newStock) }
                )
            }
        }
    }
}

@Composable
fun ElectronicsProductCard(
    product: ElectronicsProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Int) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_electronics_item_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = product.brand.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (product.condition) {
                                "Brand New (Box Pack)" -> Color(0xFFDCFCE7)
                                "Used (Kit Only)" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFF1F5F9)
                            }
                        ) {
                            Text(
                                text = product.condition,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (product.condition) {
                                    "Brand New (Box Pack)" -> Color(0xFF15803D)
                                    "Used (Kit Only)" -> Color(0xFFB45309)
                                    else -> Color(0xFF475569)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // PTA Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                product.ptaStatus.contains("Approved", ignoreCase = true) -> Color(0xFFDCFCE7)
                                product.ptaStatus.contains("CPID", ignoreCase = true) -> Color(0xFFF3E8FF)
                                else -> Color(0xFFFEE2E2)
                            }
                        ) {
                            Text(
                                text = product.ptaStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    product.ptaStatus.contains("Approved", ignoreCase = true) -> Color(0xFF15803D)
                                    product.ptaStatus.contains("CPID", ignoreCase = true) -> HardwarePurple
                                    else -> StatusDanger
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    if (product.storageSpecs.isNotBlank() || product.color.isNotBlank()) {
                        Text(
                            text = listOfNotNull(
                                product.storageSpecs.takeIf { it.isNotBlank() },
                                product.color.takeIf { it.isNotBlank() }
                            ).joinToString(" • "),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // IMEI / Serial Number Box
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (product.imei1.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "IMEI 1: ${product.imei1}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(product.imei1))
                                    Toast.makeText(context, "IMEI 1 copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElectronicsBlue, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    if (product.imei2.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "IMEI 2: ${product.imei2}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(product.imei2))
                                    Toast.makeText(context, "IMEI 2 copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElectronicsBlue, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    if (product.serialNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Serial #: ${product.serialNumber}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569)
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(product.serialNumber))
                                    Toast.makeText(context, "Serial # copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElectronicsBlue, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warranty & Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Warranty indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (product.isWarrantyActive) Icons.Default.VerifiedUser else Icons.Default.GppBad,
                        contentDescription = null,
                        tint = if (product.isWarrantyActive) StatusSuccess else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = product.warrantyType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (product.isWarrantyActive) StatusSuccess else Color(0xFF64748B)
                        )
                        if (product.warrantyExpiryDate.isNotBlank()) {
                            Text(
                                text = if (product.isWarrantyActive) "Exp: ${product.warrantyExpiryDate}" else "Expired: ${product.warrantyExpiryDate}",
                                fontSize = 10.sp,
                                color = if (product.isWarrantyActive) Color(0xFF64748B) else StatusDanger
                            )
                        }
                    }
                }

                // Prices
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₨ ${NumberFormat.getNumberInstance(Locale.US).format(product.salePrice.toLong())}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectronicsDarkBlue
                    )
                    Text(
                        text = "Cost: ₨ ${NumberFormat.getNumberInstance(Locale.US).format(product.purchasePrice.toLong())}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Stock Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("In Stock: ", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(
                        text = "${product.stockQuantity} pcs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stockQuantity <= 1) StatusWarning else Color(0xFF0F172A)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (product.stockQuantity > 0) {
                                onUpdateStock(product.stockQuantity - 1)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = product.stockQuantity > 0
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Stock", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${product.stockQuantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { onUpdateStock(product.stockQuantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Stock", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. MOBILE REPAIR TICKETS VIEW
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicsRepairTicketsView(
    tickets: List<RepairTicketEntity>,
    onEditTicket: (RepairTicketEntity) -> Unit,
    onDeleteTicket: (RepairTicketEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onPayBalance: (RepairTicketEntity) -> Unit,
    onPrintSlip: (RepairTicketEntity) -> Unit,
    onAddNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }

    val statusFilters = listOf("ALL", "RECEIVED", "DIAGNOSING", "IN_REPAIR", "READY", "DELIVERED")

    val filteredTickets = remember(tickets, searchQuery, selectedStatus) {
        tickets.filter { ticket ->
            val matchesStatus = (selectedStatus == "ALL" || ticket.status.equals(selectedStatus, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    ticket.ticketNumber.contains(searchQuery, ignoreCase = true) ||
                    ticket.customerName.contains(searchQuery, ignoreCase = true) ||
                    ticket.customerPhone.contains(searchQuery, ignoreCase = true) ||
                    ticket.deviceModel.contains(searchQuery, ignoreCase = true) ||
                    ticket.imeiOrSerial.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    val inLabCount = tickets.count { it.status in listOf("RECEIVED", "DIAGNOSING", "IN_REPAIR") }
    val readyCount = tickets.count { it.status == "READY" }
    val totalPendingBalance = tickets.sumOf { it.remainingBalance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("electronics_repair_ticket_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("In Repair Lab", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$inLabCount devices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusWarning)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ready for Pickup", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$readyCount ready", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Due Balance", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("₨ ${NumberFormat.getNumberInstance(Locale.US).format(totalPendingBalance.toLong())}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusDanger)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_repairs"),
                placeholder = { Text("Search Ticket #, Customer, Phone, Model...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusFilters.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st.replace("_", " "), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectronicsBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredTickets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BuildCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No repair tickets found", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Create a new job ticket when a customer brings in a phone or laptop for repair.", fontSize = 12.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddNew,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Job Ticket")
                        }
                    }
                }
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                RepairTicketCard(
                    ticket = ticket,
                    onEdit = { onEditTicket(ticket) },
                    onDelete = { onDeleteTicket(ticket) },
                    onUpdateStatus = { newStatus -> onUpdateStatus(ticket.id, newStatus) },
                    onPayBalance = { onPayBalance(ticket) },
                    onPrintSlip = { onPrintSlip(ticket) }
                )
            }
        }
    }
}

@Composable
fun RepairTicketCard(
    ticket: RepairTicketEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onPayBalance: () -> Unit,
    onPrintSlip: () -> Unit
) {
    val statusColor = when (ticket.status) {
        "RECEIVED" -> Color(0xFF64748B)
        "DIAGNOSING" -> Color(0xFFD97706)
        "IN_REPAIR" -> ElectronicsBlue
        "READY" -> StatusSuccess
        "DELIVERED" -> Color(0xFF0F172A)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_repair_ticket_${ticket.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Ticket #, Status, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = ticket.ticketNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = ticket.status.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrintSlip,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Receipt Slip", tint = ElectronicsBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Device Model & Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticket.deviceModel + if (ticket.deviceColor.isNotBlank()) " (${ticket.deviceColor})" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Customer: ${ticket.customerName} • ${ticket.customerPhone}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    if (ticket.imeiOrSerial.isNotBlank()) {
                        Text(
                            text = "IMEI/SN: ${ticket.imeiOrSerial}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                if (ticket.devicePasscode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ticket.devicePasscode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Problem Description Box
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Fault: ${ticket.problemDescription}",
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                    if (ticket.conditionNotes.isNotBlank()) {
                        Text(
                            text = "Received condition: ${ticket.conditionNotes}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    if (ticket.technicianRemarks.isNotBlank()) {
                        Text(
                            text = "Technician Note: ${ticket.technicianRemarks}",
                            fontSize = 11.sp,
                            color = ElectronicsDarkBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial & Delivery Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Due: ${ticket.expectedDeliveryDate}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Tech: ${ticket.assignedTechnician}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Est. ₨ ${ticket.estimatedCost.toInt()} (Adv: ₨ ${ticket.advancePaid.toInt()})",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bal: ", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = if (ticket.remainingBalance <= 0) "Paid in Full" else "₨ ${ticket.remainingBalance.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (ticket.remainingBalance <= 0) StatusSuccess else StatusDanger
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Interactive Workflow Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (ticket.status) {
                    "RECEIVED" -> {
                        Button(
                            onClick = { onUpdateStatus("DIAGNOSING") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Diagnosing", fontSize = 12.sp)
                        }
                    }
                    "DIAGNOSING" -> {
                        Button(
                            onClick = { onUpdateStatus("IN_REPAIR") },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Move to Repairing", fontSize = 12.sp)
                        }
                    }
                    "IN_REPAIR" -> {
                        Button(
                            onClick = { onUpdateStatus("READY") },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Ready for Pickup", fontSize = 12.sp)
                        }
                    }
                    "READY" -> {
                        Button(
                            onClick = {
                                if (ticket.remainingBalance > 0) {
                                    onPayBalance()
                                } else {
                                    onUpdateStatus("DELIVERED")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (ticket.remainingBalance > 0) "Collect ₨ ${ticket.remainingBalance.toInt()} & Deliver" else "Deliver Device", fontSize = 12.sp)
                        }
                    }
                    "DELIVERED" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivered to Customer", fontSize = 12.sp, color = StatusSuccess, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (ticket.remainingBalance > 0 && ticket.status != "DELIVERED") {
                    OutlinedButton(
                        onClick = onPayBalance,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Add Payment", fontSize = 11.sp, color = ElectronicsDarkBlue)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. WARRANTY & IMEI LOOKUP VIEW
// -------------------------------------------------------------------------------------------------
@Composable
fun WarrantyAndImeiLookupView(
    products: List<ElectronicsProductEntity>,
    tickets: List<RepairTicketEntity>
) {
    var query by remember { mutableStateOf("") }

    val matchedProduct = remember(products, query) {
        if (query.length < 3) null
        else products.firstOrNull {
            it.imei1.contains(query, ignoreCase = true) ||
            it.imei2.contains(query, ignoreCase = true) ||
            it.serialNumber.contains(query, ignoreCase = true)
        }
    }

    val matchedTickets = remember(tickets, query) {
        if (query.length < 3) emptyList()
        else tickets.filter {
            it.imeiOrSerial.contains(query, ignoreCase = true) ||
            it.ticketNumber.contains(query, ignoreCase = true)
        }
    }

    val activeWarrantyProducts = remember(products) {
        products.filter { it.isWarrantyActive }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant IMEI & Serial Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Verify warranty validity, PTA registration, original purchase, and service history.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Enter 15-digit IMEI or Serial Number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechCyan) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("input_imei_warranty_checker"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Search Results
        if (query.length >= 3) {
            Text("Verification Result", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            if (matchedProduct != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inventory Match Verified", fontWeight = FontWeight.Bold, color = StatusSuccess, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(matchedProduct.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Brand: ${matchedProduct.brand} • Condition: ${matchedProduct.condition}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("PTA Status: ${matchedProduct.ptaStatus}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (matchedProduct.ptaStatus.contains("Approved", ignoreCase = true)) StatusSuccess else StatusDanger)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("IMEI 1: ${matchedProduct.imei1}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        if (matchedProduct.imei2.isNotBlank()) Text("IMEI 2: ${matchedProduct.imei2}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (matchedProduct.isWarrantyActive) StatusSuccess else StatusDanger
                        ) {
                            Text(
                                text = if (matchedProduct.isWarrantyActive) "Active Warranty (${matchedProduct.warrantyType}) - Exp: ${matchedProduct.warrantyExpiryDate}" else "Warranty Expired on ${matchedProduct.warrantyExpiryDate}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (matchedTickets.isNotEmpty()) {
                Text("Service / Repair History (${matchedTickets.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                matchedTickets.forEach { t ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(t.ticketNumber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                Text(t.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ElectronicsBlue)
                            }
                            Text("Customer: ${t.customerName} (${t.customerPhone})", fontSize = 12.sp)
                            Text("Issue: ${t.problemDescription}", fontSize = 12.sp, color = Color(0xFF475569))
                            Text("Delivered/Due: ${t.expectedDeliveryDate} • Cost: ₨ ${t.estimatedCost.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            if (matchedProduct == null && matchedTickets.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("No matching device found in inventory or repair records for '$query'", color = StatusDanger, fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Warranties Section
        Text("Active Warranty Devices (${activeWarrantyProducts.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        if (activeWarrantyProducts.isEmpty()) {
            Text("No active official warranties currently recorded.", fontSize = 12.sp, color = Color(0xFF64748B))
        } else {
            activeWarrantyProducts.forEach { p ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("IMEI: ${p.imei1.ifBlank { p.serialNumber }}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = p.warrantyType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text("Valid till: ${p.warrantyExpiryDate}", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DIALOGS: ADD / EDIT PRODUCT & REPAIR TICKET
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditElectronicsProductDialog(
    product: ElectronicsProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ElectronicsProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "Samsung") }
    var category by remember { mutableStateOf(product?.category ?: "Smartphones") }
    var condition by remember { mutableStateOf(product?.condition ?: "Brand New (Box Pack)") }
    var imei1 by remember { mutableStateOf(product?.imei1 ?: "") }
    var imei2 by remember { mutableStateOf(product?.imei2 ?: "") }
    var serialNumber by remember { mutableStateOf(product?.serialNumber ?: "") }
    var ptaStatus by remember { mutableStateOf(product?.ptaStatus ?: "PTA Approved") }
    var storageSpecs by remember { mutableStateOf(product?.storageSpecs ?: "") }
    var color by remember { mutableStateOf(product?.color ?: "") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toInt()?.toString() ?: "") }
    var salePrice by remember { mutableStateOf(product?.salePrice?.toInt()?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "1") }
    var warrantyType by remember { mutableStateOf(product?.warrantyType ?: "1 Year Official") }
    var warrantyExpiryDate by remember {
        mutableStateOf(
            product?.warrantyExpiryDate ?: run {
                val cal = Calendar.getInstance()
                cal.add(Calendar.YEAR, 1)
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            }
        )
    }
    var supplierName by remember { mutableStateOf(product?.supplierName ?: "") }
    var notes by remember { mutableStateOf(product?.notes ?: "") }

    val brands = listOf("Samsung", "Apple", "Xiaomi", "Infinix", "Vivo", "Tecno", "Realme", "Dell", "HP", "Other")
    val ptaOptions = listOf("PTA Approved", "Non-PTA", "CPID / Patch", "Not Applicable")
    val conditionOptions = listOf("Brand New (Box Pack)", "Used (Kit Only)", "Refurbished", "Box Open")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = if (product == null) "Add Device / Inventory" else "Edit Device",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Model / Product Name *") },
                        placeholder = { Text("e.g. Galaxy S23 Ultra, iPhone 14 Pro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Brand Selection
                    Text("Brand", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        brands.forEach { b ->
                            FilterChip(
                                selected = brand == b,
                                onClick = { brand = b },
                                label = { Text(b, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Condition
                    Text("Condition", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        conditionOptions.forEach { cond ->
                            FilterChip(
                                selected = condition == cond,
                                onClick = { condition = cond },
                                label = { Text(cond, fontSize = 11.sp) }
                            )
                        }
                    }

                    // PTA Status
                    Text("PTA Registration Status", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ptaOptions.forEach { p ->
                            FilterChip(
                                selected = ptaStatus == p,
                                onClick = { ptaStatus = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    // IMEI 1 & IMEI 2
                    OutlinedTextField(
                        value = imei1,
                        onValueChange = { imei1 = it },
                        label = { Text("Primary IMEI 1 (15 digits)") },
                        placeholder = { Text("35892011...") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = imei2,
                        onValueChange = { imei2 = it },
                        label = { Text("Secondary IMEI 2 (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number (e.g. for Laptops/Apple)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = storageSpecs,
                            onValueChange = { storageSpecs = it },
                            label = { Text("RAM / Storage") },
                            placeholder = { Text("8GB / 128GB") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            placeholder = { Text("Black") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Financials
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Cost Price (₨) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = { salePrice = it },
                            label = { Text("Sale Price (₨) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stockQuantity,
                            onValueChange = { stockQuantity = it },
                            label = { Text("Stock Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = warrantyType,
                            onValueChange = { warrantyType = it },
                            label = { Text("Warranty Coverage") },
                            placeholder = { Text("1 Year Official") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = warrantyExpiryDate,
                        onValueChange = { warrantyExpiryDate = it },
                        label = { Text("Warranty Expiry (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier / Dealer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || salePrice.isBlank()) return@Button
                            val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                            val sPrice = salePrice.toDoubleOrNull() ?: 0.0
                            val qty = stockQuantity.toIntOrNull() ?: 1

                            val entity = product?.copy(
                                name = name.trim(),
                                brand = brand,
                                category = category,
                                condition = condition,
                                imei1 = imei1.trim(),
                                imei2 = imei2.trim(),
                                serialNumber = serialNumber.trim(),
                                ptaStatus = ptaStatus,
                                storageSpecs = storageSpecs.trim(),
                                color = color.trim(),
                                purchasePrice = pPrice,
                                salePrice = sPrice,
                                stockQuantity = qty,
                                warrantyType = warrantyType.trim(),
                                warrantyExpiryDate = warrantyExpiryDate.trim(),
                                supplierName = supplierName.trim(),
                                notes = notes.trim()
                            ) ?: ElectronicsProductEntity(
                                businessId = 0L,
                                name = name.trim(),
                                brand = brand,
                                category = category,
                                condition = condition,
                                imei1 = imei1.trim(),
                                imei2 = imei2.trim(),
                                serialNumber = serialNumber.trim(),
                                ptaStatus = ptaStatus,
                                storageSpecs = storageSpecs.trim(),
                                color = color.trim(),
                                purchasePrice = pPrice,
                                salePrice = sPrice,
                                stockQuantity = qty,
                                warrantyType = warrantyType.trim(),
                                warrantyExpiryDate = warrantyExpiryDate.trim(),
                                supplierName = supplierName.trim(),
                                purchaseDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                notes = notes.trim()
                            )
                            onSave(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Device")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRepairTicketDialog(
    ticket: RepairTicketEntity?,
    onDismiss: () -> Unit,
    onSave: (RepairTicketEntity) -> Unit
) {
    var customerName by remember { mutableStateOf(ticket?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(ticket?.customerPhone ?: "") }
    var deviceModel by remember { mutableStateOf(ticket?.deviceModel ?: "") }
    var deviceColor by remember { mutableStateOf(ticket?.deviceColor ?: "") }
    var imeiOrSerial by remember { mutableStateOf(ticket?.imeiOrSerial ?: "") }
    var devicePasscode by remember { mutableStateOf(ticket?.devicePasscode ?: "") }
    var problemDescription by remember { mutableStateOf(ticket?.problemDescription ?: "") }
    var conditionNotes by remember { mutableStateOf(ticket?.conditionNotes ?: "Minor scratches, without charger") }
    var assignedTechnician by remember { mutableStateOf(ticket?.assignedTechnician ?: "Master Farooq") }
    var estimatedCost by remember { mutableStateOf(ticket?.estimatedCost?.toInt()?.toString() ?: "") }
    var advancePaid by remember { mutableStateOf(ticket?.advancePaid?.toInt()?.toString() ?: "0") }
    var expectedDeliveryDate by remember {
        mutableStateOf(
            ticket?.expectedDeliveryDate ?: run {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 2)
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            }
        )
    }
    var technicianRemarks by remember { mutableStateOf(ticket?.technicianRemarks ?: "") }

    val quickFaults = listOf("Broken OLED / Glass", "Battery Swelling / Draining", "Charging Port Fault", "Water Damaged (Dead)", "Mic & Speaker Dead", "Camera Blur")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = if (ticket == null) "New Mobile Repair Ticket" else "Edit Ticket #${ticket.ticketNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Customer info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    // Device Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = deviceModel,
                            onValueChange = { deviceModel = it },
                            label = { Text("Device Model *") },
                            placeholder = { Text("e.g. iPhone 13 Pro") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = deviceColor,
                            onValueChange = { deviceColor = it },
                            label = { Text("Color") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = imeiOrSerial,
                            onValueChange = { imeiOrSerial = it },
                            label = { Text("Device IMEI / Serial") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = devicePasscode,
                            onValueChange = { devicePasscode = it },
                            label = { Text("Passcode/Pattern") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }

                    // Fault description
                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        label = { Text("Fault / Problem Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // Quick faults chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickFaults.forEach { f ->
                            AssistChip(
                                onClick = {
                                    problemDescription = if (problemDescription.isBlank()) f else "$problemDescription, $f"
                                },
                                label = { Text(f, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = conditionNotes,
                        onValueChange = { conditionNotes = it },
                        label = { Text("Received Physical Condition / Accessories") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = estimatedCost,
                            onValueChange = { estimatedCost = it },
                            label = { Text("Estimated Cost (₨) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = advancePaid,
                            onValueChange = { advancePaid = it },
                            label = { Text("Advance Paid (₨)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = assignedTechnician,
                            onValueChange = { assignedTechnician = it },
                            label = { Text("Assigned Technician") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = expectedDeliveryDate,
                            onValueChange = { expectedDeliveryDate = it },
                            label = { Text("Expected Due Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = technicianRemarks,
                        onValueChange = { technicianRemarks = it },
                        label = { Text("Technician Diagnostic Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isBlank() || deviceModel.isBlank() || estimatedCost.isBlank()) return@Button
                            val cost = estimatedCost.toDoubleOrNull() ?: 0.0
                            val adv = advancePaid.toDoubleOrNull() ?: 0.0
                            val balance = (cost - adv).coerceAtLeast(0.0)

                            val ticketNumber = ticket?.ticketNumber ?: "REP-${(100..999).random()}"
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                            val entity = ticket?.copy(
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                deviceModel = deviceModel.trim(),
                                deviceColor = deviceColor.trim(),
                                imeiOrSerial = imeiOrSerial.trim(),
                                devicePasscode = devicePasscode.trim(),
                                problemDescription = problemDescription.trim(),
                                conditionNotes = conditionNotes.trim(),
                                assignedTechnician = assignedTechnician.trim(),
                                estimatedCost = cost,
                                advancePaid = adv,
                                remainingBalance = balance,
                                expectedDeliveryDate = expectedDeliveryDate.trim(),
                                technicianRemarks = technicianRemarks.trim()
                            ) ?: RepairTicketEntity(
                                businessId = 0L,
                                ticketNumber = ticketNumber,
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                deviceModel = deviceModel.trim(),
                                deviceColor = deviceColor.trim(),
                                imeiOrSerial = imeiOrSerial.trim(),
                                devicePasscode = devicePasscode.trim(),
                                problemDescription = problemDescription.trim(),
                                conditionNotes = conditionNotes.trim(),
                                assignedTechnician = assignedTechnician.trim(),
                                status = "RECEIVED",
                                estimatedCost = cost,
                                advancePaid = adv,
                                remainingBalance = balance,
                                receivedDate = today,
                                expectedDeliveryDate = expectedDeliveryDate.trim(),
                                technicianRemarks = technicianRemarks.trim()
                            )
                            onSave(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Ticket")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// REPAIR SLIP / RECEIPT DIALOG
// -------------------------------------------------------------------------------------------------
@Composable
fun RepairSlipDialog(
    ticket: RepairTicketEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PAK ELECTRONICS & MOBILE LAB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text("REPAIR JOB TICKET / CUSTOMER COPY", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "TICKET #${ticket.ticketNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                // Customer & Device Specs
                Text("CUSTOMER & DEVICE DETAILS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Customer: ${ticket.customerName} (${ticket.customerPhone})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Device: ${ticket.deviceModel} ${if (ticket.deviceColor.isNotBlank()) "• ${ticket.deviceColor}" else ""}", fontSize = 13.sp)
                if (ticket.imeiOrSerial.isNotBlank()) {
                    Text("IMEI / Serial: ${ticket.imeiOrSerial}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF475569))
                }
                if (ticket.devicePasscode.isNotBlank()) {
                    Text("Unlock PIN: ${ticket.devicePasscode}", fontSize = 12.sp, color = Color(0xFFB45309))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("FAULT & CONDITION", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                Text("Reported Issue: ${ticket.problemDescription}", fontSize = 12.sp, color = Color(0xFF0F172A))
                Text("Accessories/Condition: ${ticket.conditionNotes}", fontSize = 12.sp, color = Color(0xFF64748B))

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                // Schedule & Billing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Received Date:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(ticket.receivedDate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Expected Delivery:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(ticket.expectedDeliveryDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectronicsBlue)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Technician Assigned:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(ticket.assignedTechnician, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Charges:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("₨ ${ticket.estimatedCost.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Advance Deposit Paid:", fontSize = 12.sp, color = StatusSuccess)
                    Text("₨ ${ticket.advancePaid.toInt()}", fontSize = 13.sp, color = StatusSuccess, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining Balance Due:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusDanger)
                    Text("₨ ${ticket.remainingBalance.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusDanger)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "* Please present this ticket at time of device collection. Devices uncollected after 30 days are not our liability.",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    """
                                    *MOBILE REPAIR SLIP*
                                    Ticket: #${ticket.ticketNumber}
                                    Device: ${ticket.deviceModel}
                                    Customer: ${ticket.customerName}
                                    Issue: ${ticket.problemDescription}
                                    Est. Charges: ₨ ${ticket.estimatedCost.toInt()}
                                    Advance: ₨ ${ticket.advancePaid.toInt()}
                                    Balance: ₨ ${ticket.remainingBalance.toInt()}
                                    Expected Due: ${ticket.expectedDeliveryDate}
                                    """.trimIndent()
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Repair Slip"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectronicsBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via WhatsApp")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// BALANCE PAYMENT DIALOG
// -------------------------------------------------------------------------------------------------
@Composable
fun RepairBalancePaymentDialog(
    ticket: RepairTicketEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf(ticket.remainingBalance.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text("Collect Repair Balance", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                Text("Ticket #${ticket.ticketNumber} • ${ticket.deviceModel}", fontSize = 12.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Current Balance Due:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("₨ ${ticket.remainingBalance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StatusDanger)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Received (₨)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val paid = amountInput.toDoubleOrNull() ?: 0.0
                            if (paid > 0) {
                                onConfirmPayment(paid)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Payment")
                    }
                }
            }
        }
    }
}
