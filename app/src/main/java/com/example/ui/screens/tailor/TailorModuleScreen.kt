package com.example.ui.screens.tailor

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TailorCustomerEntity
import com.example.data.TailorMeasurementEntity
import com.example.data.TailorOrderEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakGoldSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Colors for status badges
private val ColorCutting = Color(0xFF7B1FA2) // Deep Purple
private val ColorCuttingBg = Color(0xFFF3E5F5)
private val ColorStitching = Color(0xFFE65100) // Deep Amber/Orange
private val ColorStitchingBg = Color(0xFFFFF3E0)
private val ColorReady = Color(0xFF2E7D32) // Emerald Green
private val ColorReadyBg = Color(0xFFE8F5E9)
private val ColorDelivered = Color(0xFF1565C0) // Deep Blue
private val ColorDeliveredBg = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailorModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsStateWithLifecycle()
    val customers by viewModel.tailorCustomers.collectAsStateWithLifecycle()
    val measurements by viewModel.tailorMeasurements.collectAsStateWithLifecycle()
    val orders by viewModel.tailorOrders.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Orders & Delivery", "Paimaish Register", "Customers")

    // Filter and search states
    var statusFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<TailorOrderEntity?>(null) }
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var measurementToEdit by remember { mutableStateOf<TailorMeasurementEntity?>(null) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<TailorCustomerEntity?>(null) }
    var viewMeasurementOrder by remember { mutableStateOf<TailorOrderEntity?>(null) }
    var orderToSettlePayment by remember { mutableStateOf<TailorOrderEntity?>(null) }

    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> {
                            orderToEdit = null
                            showNewOrderDialog = true
                        }
                        1 -> {
                            measurementToEdit = null
                            showMeasurementDialog = true
                        }
                        2 -> {
                            customerToEdit = null
                            showCustomerDialog = true
                        }
                    }
                },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("tailor_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Tabs
            PrimaryTabRow(
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
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Checkroom
                                    1 -> Icons.Default.Straighten
                                    else -> Icons.Default.People
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // TAB 1: Orders & Delivery Tracking
                    OrdersTrackingTab(
                        orders = orders,
                        statusFilter = statusFilter,
                        onStatusFilterChange = { statusFilter = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onAdvanceStatus = { order, nextStatus ->
                            viewModel.updateTailorOrderStatus(order.id, nextStatus)
                        },
                        onEditOrder = {
                            orderToEdit = it
                            showNewOrderDialog = true
                        },
                        onDeleteOrder = { viewModel.deleteTailorOrder(it.id) },
                        onSettlePayment = { orderToSettlePayment = it },
                        onViewMeasurement = { viewMeasurementOrder = it }
                    )
                }
                1 -> {
                    // TAB 2: Measurements Book (Paimaish Register)
                    MeasurementsTab(
                        measurements = measurements,
                        customers = customers,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onEditMeasurement = {
                            measurementToEdit = it
                            showMeasurementDialog = true
                        },
                        onDeleteMeasurement = { viewModel.deleteTailorMeasurement(it.id) },
                        onCreateOrderFromMeasurement = { m ->
                            // Pre-fill a new order for this customer
                            orderToEdit = TailorOrderEntity(
                                businessId = activeBusiness?.id ?: 1L,
                                orderNumber = "#TK-${(100..999).random()}",
                                customerId = m.customerId,
                                customerName = m.customerName,
                                customerPhone = customers.find { it.id == m.customerId }?.phone ?: "",
                                orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                deliveryDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
                                    Date(System.currentTimeMillis() + 4 * 86400000L)
                                ),
                                measurementSummary = "L:${m.length}, C:${m.chest}, W:${m.waist}, Sh:${m.shoulder}, Sl:${m.sleeves}, P:${m.paincha}"
                            )
                            showNewOrderDialog = true
                        }
                    )
                }
                2 -> {
                    // TAB 3: Customers Directory
                    CustomersTab(
                        customers = customers,
                        orders = orders,
                        measurements = measurements,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onEditCustomer = {
                            customerToEdit = it
                            showCustomerDialog = true
                        },
                        onDeleteCustomer = { viewModel.deleteTailorCustomer(it.id) },
                        onAddMeasurementForCustomer = { cust ->
                            measurementToEdit = TailorMeasurementEntity(
                                businessId = activeBusiness?.id ?: 1L,
                                customerId = cust.id,
                                customerName = cust.name,
                                gender = if (cust.gender.contains("Ladies")) "Ladies" else "Gents"
                            )
                            showMeasurementDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- Dialogs ---
    if (showNewOrderDialog) {
        OrderDialog(
            order = orderToEdit,
            customers = customers,
            measurements = measurements,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = {
                showNewOrderDialog = false
                orderToEdit = null
            },
            onSave = { order ->
                viewModel.saveTailorOrder(order)
                showNewOrderDialog = false
                orderToEdit = null
            }
        )
    }

    if (showMeasurementDialog) {
        MeasurementDialog(
            measurement = measurementToEdit,
            customers = customers,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = {
                showMeasurementDialog = false
                measurementToEdit = null
            },
            onSave = { m ->
                viewModel.saveTailorMeasurement(m)
                showMeasurementDialog = false
                measurementToEdit = null
            }
        )
    }

    if (showCustomerDialog) {
        CustomerDialog(
            customer = customerToEdit,
            businessId = activeBusiness?.id ?: 1L,
            onDismiss = {
                showCustomerDialog = false
                customerToEdit = null
            },
            onSave = { c ->
                viewModel.saveTailorCustomer(c)
                showCustomerDialog = false
                customerToEdit = null
            }
        )
    }

    if (orderToSettlePayment != null) {
        PaymentSettlementDialog(
            order = orderToSettlePayment!!,
            onDismiss = { orderToSettlePayment = null },
            onSave = { advance, remaining ->
                viewModel.updateTailorOrderPayment(orderToSettlePayment!!.id, advance, remaining)
                orderToSettlePayment = null
            }
        )
    }

    if (viewMeasurementOrder != null) {
        val linkedMeasurement = measurements.find { it.customerId == viewMeasurementOrder!!.customerId }
        ViewMeasurementDialog(
            order = viewMeasurementOrder!!,
            measurement = linkedMeasurement,
            onDismiss = { viewMeasurementOrder = null }
        )
    }
}

// ==========================================
// TAB 1: Orders & Delivery Tracking
// ==========================================
@Composable
private fun OrdersTrackingTab(
    orders: List<TailorOrderEntity>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAdvanceStatus: (TailorOrderEntity, String) -> Unit,
    onEditOrder: (TailorOrderEntity) -> Unit,
    onDeleteOrder: (TailorOrderEntity) -> Unit,
    onSettlePayment: (TailorOrderEntity) -> Unit,
    onViewMeasurement: (TailorOrderEntity) -> Unit
) {
    val context = LocalContext.current
    val pkrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "PK")) }

    val filteredOrders = remember(orders, statusFilter, searchQuery) {
        orders.filter { order ->
            val matchesStatus = if (statusFilter == "ALL") true else order.status.equals(statusFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                        order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                        order.customerPhone.contains(searchQuery) ||
                        order.suitType.contains(searchQuery, ignoreCase = true)
            }
            matchesStatus && matchesSearch
        }
    }

    // Stats calculations
    val totalActive = orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }
    val cuttingCount = orders.count { it.status == "CUTTING" }
    val stitchingCount = orders.count { it.status == "STITCHING" }
    val readyCount = orders.count { it.status == "READY" }
    val totalRemainingReceivable = orders.filter { it.status != "CANCELLED" }.sumOf { it.remainingBalance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("orders_tracking_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stats Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Boutique Production Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Surface(
                            color = PakEmeraldContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Receivable: ₨ ${pkrFormat.format(totalRemainingReceivable).replace("PKR", "").trim()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatCard(
                            label = "Cutting",
                            count = cuttingCount,
                            color = ColorCutting,
                            bgColor = ColorCuttingBg,
                            icon = Icons.Default.ContentCut,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatCard(
                            label = "Stitching",
                            count = stitchingCount,
                            color = ColorStitching,
                            bgColor = ColorStitchingBg,
                            icon = Icons.Default.Build,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatCard(
                            label = "Ready",
                            count = readyCount,
                            color = ColorReady,
                            bgColor = ColorReadyBg,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatCard(
                            label = "Total Active",
                            count = totalActive,
                            color = MaterialTheme.colorScheme.primary,
                            bgColor = MaterialTheme.colorScheme.primaryContainer,
                            icon = Icons.Default.Checkroom,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Search & Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tailor_search_input"),
                    placeholder = { Text("Search by #Order, Customer, Phone, Garment...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Workflow Stage Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf("ALL", "CUTTING", "STITCHING", "READY", "DELIVERED")
                    filterOptions.forEach { filter ->
                        val isSelected = statusFilter == filter
                        val label = when (filter) {
                            "ALL" -> "All (${orders.size})"
                            "CUTTING" -> "Cutting ($cuttingCount)"
                            "STITCHING" -> "Stitching ($stitchingCount)"
                            "READY" -> "Ready ($readyCount)"
                            "DELIVERED" -> "Delivered"
                            else -> filter
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStatusFilterChange(filter) },
                            label = { Text(text = label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Orders List
        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No orders found for this filter",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                TailorOrderCard(
                    order = order,
                    onAdvanceStatus = { nextStatus -> onAdvanceStatus(order, nextStatus) },
                    onEdit = { onEditOrder(order) },
                    onDelete = { onDeleteOrder(order) },
                    onSettlePayment = { onSettlePayment(order) },
                    onViewMeasurement = { onViewMeasurement(order) },
                    onWhatsAppNotify = {
                        val message = when (order.status) {
                            "READY" -> "Assalam-o-Alaikum ${order.customerName}! Your order ${order.orderNumber} (${order.suitType}) is READY for trial & pickup. Remaining balance: ₨ ${order.remainingBalance.toInt()}. Thank you!"
                            "CUTTING" -> "Assalam-o-Alaikum ${order.customerName}! Your order ${order.orderNumber} is currently under Cutting by our Master. Expected delivery date: ${order.deliveryDate}."
                            "STITCHING" -> "Assalam-o-Alaikum ${order.customerName}! Your order ${order.orderNumber} is now being stitched. Expected delivery date: ${order.deliveryDate}."
                            "DELIVERED" -> "Assalam-o-Alaikum ${order.customerName}! Thank you for choosing our boutique. We hope you love the fit of your suit!"
                            else -> "Assalam-o-Alaikum ${order.customerName}! Update on order ${order.orderNumber}: Status is ${order.status}. Delivery Date: ${order.deliveryDate}."
                        }
                        val cleanPhone = order.customerPhone.replace(Regex("[^0-9+]"), "")
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to regular dialer/sms
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = color.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// Order Card Component with Progress Pipeline
// ==========================================
@Composable
private fun TailorOrderCard(
    order: TailorOrderEntity,
    onAdvanceStatus: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSettlePayment: () -> Unit,
    onViewMeasurement: () -> Unit,
    onWhatsAppNotify: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        "CUTTING" -> ColorCutting
        "STITCHING" -> ColorStitching
        "READY" -> ColorReady
        "DELIVERED" -> ColorDelivered
        else -> Color.Gray
    }
    val statusBg = when (order.status) {
        "CUTTING" -> ColorCuttingBg
        "STITCHING" -> ColorStitchingBg
        "READY" -> ColorReadyBg
        "DELIVERED" -> ColorDeliveredBg
        else -> Color.LightGray.copy(alpha = 0.4f)
    }

    // Check delivery urgency
    val isReady = order.status == "READY"
    val isDelivered = order.status == "DELIVERED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Number, Customer, Status Badge, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = PakEmeraldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PakEmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = order.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = order.customerPhone,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = order.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Measurements") },
                                onClick = {
                                    expandedMenu = false
                                    onViewMeasurement()
                                },
                                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Update Payment") },
                                onClick = {
                                    expandedMenu = false
                                    onSettlePayment()
                                },
                                leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Order") },
                                onClick = {
                                    expandedMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Order", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expandedMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Garment Details & Fabric
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${order.suitType} (Qty: ${order.quantity})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Master: ${order.assignedMaster}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (order.fabricDetails.isNotBlank()) {
                        Text(
                            text = "Fabric: ${order.fabricDetails}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (order.specialInstructions.isNotBlank()) {
                        Text(
                            text = "Note: ${order.specialInstructions}",
                            fontSize = 11.sp,
                            color = Color(0xFFC2185B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Workflow Step Indicator: Cutting -> Stitching -> Ready -> Delivered
            WorkflowProgressBar(currentStatus = order.status)

            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Date & Pricing Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delivery Date badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isReady) ColorReady else PakEmeraldPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Column {
                        Text(
                            text = "Delivery Date",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = order.deliveryDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReady) ColorReady else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Payment Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ₨ ${order.totalAmount.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Adv: ₨ ${order.advancePaid.toInt()}",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Due: ₨ ${order.remainingBalance.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.remainingBalance > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Advance Workflow Status & WhatsApp Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Button
                OutlinedButton(
                    onClick = onWhatsAppNotify,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Advance Workflow Button
                when (order.status) {
                    "CUTTING" -> {
                        Button(
                            onClick = { onAdvanceStatus("STITCHING") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorStitching)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Stitching", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "STITCHING" -> {
                        Button(
                            onClick = { onAdvanceStatus("READY") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorReady)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "READY" -> {
                        Button(
                            onClick = { onAdvanceStatus("DELIVERED") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorDelivered)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deliver to Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "DELIVERED" -> {
                        Surface(
                            modifier = Modifier.weight(1.5f),
                            color = ColorDeliveredBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ColorDelivered,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Order Completed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorDelivered
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Visual Step ProgressBar for Cutting -> Stitching -> Ready -> Delivered
@Composable
private fun WorkflowProgressBar(currentStatus: String) {
    val steps = listOf("CUTTING", "STITCHING", "READY", "DELIVERED")
    val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = index <= currentIndex
            val isCurrent = index == currentIndex
            val stepColor = when (step) {
                "CUTTING" -> ColorCutting
                "STITCHING" -> ColorStitching
                "READY" -> ColorReady
                "DELIVERED" -> ColorDelivered
                else -> Color.Gray
            }

            // Step Indicator Dot
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isDone) stepColor else Color.LightGray.copy(alpha = 0.5f))
                    .border(
                        width = if (isCurrent) 2.dp else 0.dp,
                        color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Connector Line between dots
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (index < currentIndex) stepColor else Color.LightGray.copy(alpha = 0.4f))
                )
            }
        }
    }

    // Step Labels below bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            val isCurrent = index == currentIndex
            val label = when (step) {
                "CUTTING" -> "1. Cutting"
                "STITCHING" -> "2. Stitching"
                "READY" -> "3. Ready"
                "DELIVERED" -> "4. Delivered"
                else -> step
            }
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (index <= currentIndex) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }
    }
}

// ==========================================
// TAB 2: Measurements Book (Paimaish Register)
// ==========================================
@Composable
private fun MeasurementsTab(
    measurements: List<TailorMeasurementEntity>,
    customers: List<TailorCustomerEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditMeasurement: (TailorMeasurementEntity) -> Unit,
    onDeleteMeasurement: (TailorMeasurementEntity) -> Unit,
    onCreateOrderFromMeasurement: (TailorMeasurementEntity) -> Unit
) {
    val context = LocalContext.current
    val filteredMeasurements = remember(measurements, searchQuery) {
        if (searchQuery.isBlank()) measurements else {
            measurements.filter {
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                        it.profileTitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measurements_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("measurement_search_input"),
                placeholder = { Text("Search customer paimaish register...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filteredMeasurements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No measurement records saved yet",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap '+' to add a customer's paimaish profile",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredMeasurements, key = { it.id }) { m ->
                MeasurementCard(
                    measurement = m,
                    onEdit = { onEditMeasurement(m) },
                    onDelete = { onDeleteMeasurement(m) },
                    onCreateOrder = { onCreateOrderFromMeasurement(m) },
                    onShareWhatsApp = {
                        val text = """
                            *Paimaish (Measurements) - ${m.customerName}*
                            Profile: ${m.profileTitle} (${m.gender})
                            
                            *Kameez / Shirt (Inches):*
                            • Length (Lambai): ${m.length}"
                            • Chest (Chhati): ${m.chest}"
                            • Waist (Kamar): ${m.waist}"
                            • Hip: ${m.hip}"
                            • Shoulder (Teera): ${m.shoulder}"
                            • Sleeves (Bazu): ${m.sleeves}"
                            • Collar (Gala): ${m.collar}" (${m.collarType})
                            • Daman: ${m.daman}" (${m.damanStyle})
                            • Cuff (Kaf): ${m.cuff}" (${m.cuffStyle})
                            • Armhole (Mora): ${m.armhole}"
                            
                            *Shalwar / Trouser (Inches):*
                            • Shalwar Length: ${m.shalwarLength}"
                            • Paincha: ${m.paincha}"
                            • Asan: ${m.asan}"
                            
                            *Styling:*
                            • Pocket: ${m.pocketStyle}
                            • Note: ${m.notes}
                        """.trimIndent()

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Measurement via"))
                    }
                )
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    measurement: TailorMeasurementEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateOrder: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("measurement_card_${measurement.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = measurement.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = PakEmeraldContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = measurement.profileTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = PakEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = measurement.gender,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareWhatsApp) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = PakEmeraldPrimary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary measurements preview grid (Key measures in inches)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Kameez / Shirt (Inches)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PakEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MeasurementPill("Length", "${measurement.length}\"")
                        MeasurementPill("Chest", "${measurement.chest}\"")
                        MeasurementPill("Waist", "${measurement.waist}\"")
                        MeasurementPill("Shoulder", "${measurement.shoulder}\"")
                        MeasurementPill("Sleeves", "${measurement.sleeves}\"")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Shalwar / Trouser & Collar (Inches)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PakEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MeasurementPill("Collar", "${measurement.collar}\"")
                        MeasurementPill("Daman", "${measurement.daman}\"")
                        MeasurementPill("Shalwar", "${measurement.shalwarLength}\"")
                        MeasurementPill("Paincha", "${measurement.paincha}\"")
                        MeasurementPill("Cuff", "${measurement.cuff}\"")
                    }
                }
            }

            // Expandable details (Style, Asan, Notes)
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Collar: ${measurement.collarType}", fontSize = 11.sp)
                        Text(text = "Cuff: ${measurement.cuffStyle}", fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Pocket: ${measurement.pocketStyle}", fontSize = 11.sp)
                        Text(text = "Daman: ${measurement.damanStyle}", fontSize = 11.sp)
                    }
                    if (measurement.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Special Note: ${measurement.notes}",
                            fontSize = 11.sp,
                            color = Color(0xFFC2185B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Show Less" else "View Style Details",
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onCreateOrder,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Order With This", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MeasurementPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// TAB 3: Customer Directory
// ==========================================
@Composable
private fun CustomersTab(
    customers: List<TailorCustomerEntity>,
    orders: List<TailorOrderEntity>,
    measurements: List<TailorMeasurementEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditCustomer: (TailorCustomerEntity) -> Unit,
    onDeleteCustomer: (TailorCustomerEntity) -> Unit,
    onAddMeasurementForCustomer: (TailorCustomerEntity) -> Unit
) {
    val context = LocalContext.current
    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery) ||
                        it.city.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tailor_customers_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                placeholder = { Text("Search customers by name, phone or city...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filteredCustomers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No customers registered yet",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                val customerOrders = orders.filter { it.customerId == customer.id }
                val hasMeasurement = measurements.any { it.customerId == customer.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_card_${customer.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = if (customer.gender.contains("Ladies")) Color(0xFFFCE4EC) else PakEmeraldContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = if (customer.gender.contains("Ladies")) Color(0xFFC2185B) else PakEmeraldPrimary
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${customer.phone} • ${customer.city}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = PakEmeraldPrimary)
                                }
                                IconButton(onClick = { onEditCustomer(customer) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDeleteCustomer(customer) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${customerOrders.size} Orders",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = if (hasMeasurement) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (hasMeasurement) "Paimaish Saved" else "No Paimaish",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (hasMeasurement) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!hasMeasurement) {
                                OutlinedButton(
                                    onClick = { onAddMeasurementForCustomer(customer) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Measurements", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: New / Edit Order
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDialog(
    order: TailorOrderEntity?,
    customers: List<TailorCustomerEntity>,
    measurements: List<TailorMeasurementEntity>,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (TailorOrderEntity) -> Unit
) {
    val isEdit = order != null && order.id != 0L

    var selectedCustomerId by remember {
        mutableStateOf(order?.customerId ?: customers.firstOrNull()?.id ?: 0L)
    }
    var customerName by remember {
        mutableStateOf(order?.customerName ?: customers.firstOrNull()?.name ?: "")
    }
    var customerPhone by remember {
        mutableStateOf(order?.customerPhone ?: customers.firstOrNull()?.phone ?: "")
    }
    var orderNumber by remember {
        mutableStateOf(order?.orderNumber ?: "#TK-${(100..999).random()}")
    }
    var suitType by remember {
        mutableStateOf(order?.suitType ?: "Men's Shalwar Kameez")
    }
    var quantity by remember {
        mutableStateOf((order?.quantity ?: 1).toString())
    }
    var fabricDetails by remember {
        mutableStateOf(order?.fabricDetails ?: "")
    }
    var orderDate by remember {
        mutableStateOf(
            order?.orderDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
    }
    var deliveryDate by remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 4)
        mutableStateOf(
            order?.deliveryDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        )
    }
    var status by remember {
        mutableStateOf(order?.status ?: "CUTTING")
    }
    var stitchingRate by remember {
        mutableStateOf((order?.stitchingRate ?: 1800.0).toInt().toString())
    }
    var advancePaid by remember {
        mutableStateOf((order?.advancePaid ?: 500.0).toInt().toString())
    }
    var specialInstructions by remember {
        mutableStateOf(order?.specialInstructions ?: "")
    }
    var assignedMaster by remember {
        mutableStateOf(order?.assignedMaster ?: "Master Ustad Rafique")
    }

    val garmentOptions = listOf(
        "Men's Shalwar Kameez",
        "Designer Kurta Pajama",
        "Waistcoat / Wasket",
        "2-Piece Suit",
        "Sherwani / Prince Coat",
        "Ladies 3-Piece Boutique Suit",
        "Party Wear Maxi / Frock",
        "Kids Kurta"
    )

    val statusOptions = listOf("CUTTING", "STITCHING", "READY", "DELIVERED")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Order ${order?.orderNumber}" else "New Boutique Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PakEmeraldPrimary
                )

                // Customer Selection or Input
                if (customers.isNotEmpty()) {
                    Text("Select Customer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    var customerMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = customerMenuExpanded,
                        onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = customerMenuExpanded,
                            onDismissRequest = { customerMenuExpanded = false }
                        ) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.phone})") },
                                    onClick = {
                                        selectedCustomerId = c.id
                                        customerName = c.name
                                        customerPhone = c.phone
                                        customerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Order Number & Garment Type
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Order #") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Garment Type Dropdown
                Text("Garment Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                var garmentMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = garmentMenuExpanded,
                    onExpandedChange = { garmentMenuExpanded = !garmentMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = suitType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = garmentMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = garmentMenuExpanded,
                        onDismissRequest = { garmentMenuExpanded = false }
                    ) {
                        garmentOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    suitType = g
                                    garmentMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fabric Details
                OutlinedTextField(
                    value = fabricDetails,
                    onValueChange = { fabricDetails = it },
                    label = { Text("Fabric & Color Details") },
                    placeholder = { Text("e.g. Navy Blue Egyptian Cotton") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Delivery Date & Order Date
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = orderDate,
                        onValueChange = { orderDate = it },
                        label = { Text("Order Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deliveryDate,
                        onValueChange = { deliveryDate = it },
                        label = { Text("Delivery Date *") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Workflow Status
                Text("Production Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusOptions.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Rates & Advance
                val rate = stitchingRate.toDoubleOrNull() ?: 0.0
                val adv = advancePaid.toDoubleOrNull() ?: 0.0
                val qty = quantity.toIntOrNull() ?: 1
                val total = rate * qty
                val remaining = (total - adv).coerceAtLeast(0.0)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stitchingRate,
                        onValueChange = { stitchingRate = it },
                        label = { Text("Rate per Suit (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = advancePaid,
                        onValueChange = { advancePaid = it },
                        label = { Text("Advance (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total: ₨ ${total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = "Remaining Due: ₨ ${remaining.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                // Assigned Master & Special Instructions
                OutlinedTextField(
                    value = assignedMaster,
                    onValueChange = { assignedMaster = it },
                    label = { Text("Assigned Tailor / Master") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Special Instructions / Collar style") },
                    placeholder = { Text("e.g. Hard ban collar, secret mobile pocket") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank()) {
                                onSave(
                                    TailorOrderEntity(
                                        id = order?.id ?: 0L,
                                        businessId = businessId,
                                        orderNumber = orderNumber,
                                        customerId = selectedCustomerId,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        suitType = suitType,
                                        quantity = qty,
                                        fabricDetails = fabricDetails,
                                        orderDate = orderDate,
                                        deliveryDate = deliveryDate,
                                        status = status,
                                        stitchingRate = rate,
                                        totalAmount = total,
                                        advancePaid = adv,
                                        remainingBalance = remaining,
                                        specialInstructions = specialInstructions,
                                        assignedMaster = assignedMaster,
                                        measurementSummary = order?.measurementSummary ?: ""
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isEdit) "Update Order" else "Save Order")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: New / Edit Paimaish (Measurements)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementDialog(
    measurement: TailorMeasurementEntity?,
    customers: List<TailorCustomerEntity>,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (TailorMeasurementEntity) -> Unit
) {
    val isEdit = measurement != null && measurement.id != 0L

    var selectedCustomerId by remember {
        mutableStateOf(measurement?.customerId ?: customers.firstOrNull()?.id ?: 0L)
    }
    var customerName by remember {
        mutableStateOf(measurement?.customerName ?: customers.firstOrNull()?.name ?: "")
    }
    var profileTitle by remember {
        mutableStateOf(measurement?.profileTitle ?: "Standard Shalwar Kameez")
    }
    var gender by remember {
        mutableStateOf(measurement?.gender ?: "Gents")
    }

    // Kameez (Inches)
    var length by remember { mutableStateOf((measurement?.length ?: 40.0).toString()) }
    var chest by remember { mutableStateOf((measurement?.chest ?: 38.0).toString()) }
    var waist by remember { mutableStateOf((measurement?.waist ?: 34.0).toString()) }
    var hip by remember { mutableStateOf((measurement?.hip ?: 40.0).toString()) }
    var shoulder by remember { mutableStateOf((measurement?.shoulder ?: 17.5).toString()) }
    var sleeves by remember { mutableStateOf((measurement?.sleeves ?: 23.5).toString()) }
    var collar by remember { mutableStateOf((measurement?.collar ?: 15.5).toString()) }
    var daman by remember { mutableStateOf((measurement?.daman ?: 22.0).toString()) }
    var cuff by remember { mutableStateOf((measurement?.cuff ?: 9.0).toString()) }
    var armhole by remember { mutableStateOf((measurement?.armhole ?: 9.5).toString()) }

    // Shalwar (Inches)
    var shalwarLength by remember { mutableStateOf((measurement?.shalwarLength ?: 38.0).toString()) }
    var paincha by remember { mutableStateOf((measurement?.paincha ?: 7.5).toString()) }
    var asan by remember { mutableStateOf((measurement?.asan ?: 16.0).toString()) }
    var trouserWaist by remember { mutableStateOf((measurement?.trouserWaist ?: 34.0).toString()) }

    // Style
    var collarType by remember { mutableStateOf(measurement?.collarType ?: "Sherwani Ban") }
    var pocketStyle by remember { mutableStateOf(measurement?.pocketStyle ?: "1 Front + 2 Side") }
    var cuffStyle by remember { mutableStateOf(measurement?.cuffStyle ?: "Single Kaf") }
    var damanStyle by remember { mutableStateOf(measurement?.damanStyle ?: "Chauras (Square)") }
    var notes by remember { mutableStateOf(measurement?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Paimaish Register" else "New Customer Paimaish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PakEmeraldPrimary
                )

                // Customer Selection
                if (customers.isNotEmpty()) {
                    Text("Customer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    var customerMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = customerMenuExpanded,
                        onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = customerMenuExpanded,
                            onDismissRequest = { customerMenuExpanded = false }
                        ) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedCustomerId = c.id
                                        customerName = c.name
                                        gender = if (c.gender.contains("Ladies")) "Ladies" else "Gents"
                                        customerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = profileTitle,
                        onValueChange = { profileTitle = it },
                        label = { Text("Profile Name") },
                        modifier = Modifier.weight(1.2f)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = gender == "Gents",
                            onClick = { gender = "Gents" },
                            label = { Text("Gents", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = gender == "Ladies",
                            onClick = { gender = "Ladies" },
                            label = { Text("Ladies", fontSize = 10.sp) }
                        )
                    }
                }

                HorizontalDivider()
                Text("Kameez / Shirt Measurements (Inches)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = length,
                        onValueChange = { length = it },
                        label = { Text("Length (Lambai)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = chest,
                        onValueChange = { chest = it },
                        label = { Text("Chest (Chhati)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = waist,
                        onValueChange = { waist = it },
                        label = { Text("Waist (Kamar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hip,
                        onValueChange = { hip = it },
                        label = { Text("Hip / Ghera") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shoulder,
                        onValueChange = { shoulder = it },
                        label = { Text("Shoulder (Teera)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleeves,
                        onValueChange = { sleeves = it },
                        label = { Text("Sleeves (Bazu)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = collar,
                        onValueChange = { collar = it },
                        label = { Text("Collar (Gala)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = daman,
                        onValueChange = { daman = it },
                        label = { Text("Daman Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cuff,
                        onValueChange = { cuff = it },
                        label = { Text("Cuff (Kaf)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = armhole,
                        onValueChange = { armhole = it },
                        label = { Text("Armhole (Mora)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()
                Text("Shalwar / Trouser Measurements (Inches)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shalwarLength,
                        onValueChange = { shalwarLength = it },
                        label = { Text("Shalwar Length") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paincha,
                        onValueChange = { paincha = it },
                        label = { Text("Paincha (Bottom)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = asan,
                        onValueChange = { asan = it },
                        label = { Text("Crotch (Asan)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = trouserWaist,
                        onValueChange = { trouserWaist = it },
                        label = { Text("Trouser Waist") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()
                Text("Styling & Tailoring Cut", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = collarType,
                        onValueChange = { collarType = it },
                        label = { Text("Collar Type (Ban / Shirt)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cuffStyle,
                        onValueChange = { cuffStyle = it },
                        label = { Text("Cuff Style (Gol / Kaf)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pocketStyle,
                        onValueChange = { pocketStyle = it },
                        label = { Text("Pocket (Front / Side)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = damanStyle,
                        onValueChange = { damanStyle = it },
                        label = { Text("Daman (Square/Round)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Fitting Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank()) {
                                onSave(
                                    TailorMeasurementEntity(
                                        id = measurement?.id ?: 0L,
                                        businessId = businessId,
                                        customerId = selectedCustomerId,
                                        customerName = customerName,
                                        profileTitle = profileTitle,
                                        gender = gender,
                                        length = length.toDoubleOrNull() ?: 40.0,
                                        chest = chest.toDoubleOrNull() ?: 38.0,
                                        waist = waist.toDoubleOrNull() ?: 34.0,
                                        hip = hip.toDoubleOrNull() ?: 40.0,
                                        shoulder = shoulder.toDoubleOrNull() ?: 17.5,
                                        sleeves = sleeves.toDoubleOrNull() ?: 23.5,
                                        collar = collar.toDoubleOrNull() ?: 15.5,
                                        daman = daman.toDoubleOrNull() ?: 22.0,
                                        cuff = cuff.toDoubleOrNull() ?: 9.0,
                                        armhole = armhole.toDoubleOrNull() ?: 9.5,
                                        shalwarLength = shalwarLength.toDoubleOrNull() ?: 38.0,
                                        paincha = paincha.toDoubleOrNull() ?: 7.5,
                                        asan = asan.toDoubleOrNull() ?: 16.0,
                                        trouserWaist = trouserWaist.toDoubleOrNull() ?: 34.0,
                                        collarType = collarType,
                                        pocketStyle = pocketStyle,
                                        cuffStyle = cuffStyle,
                                        damanStyle = damanStyle,
                                        notes = notes,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isEdit) "Update Paimaish" else "Save Paimaish")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: New / Edit Customer
// ==========================================
@Composable
private fun CustomerDialog(
    customer: TailorCustomerEntity?,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (TailorCustomerEntity) -> Unit
) {
    val isEdit = customer != null && customer.id != 0L

    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var gender by remember { mutableStateOf(customer?.gender ?: "Gents / Men's") }
    var city by remember { mutableStateOf(customer?.city ?: "Lahore") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Customer Profile" else "New Customer Register",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PakEmeraldPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Gents / Men's", "Ladies / Boutique", "Kids").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Customer Preferences / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onSave(
                                    TailorCustomerEntity(
                                        id = customer?.id ?: 0L,
                                        businessId = businessId,
                                        name = name,
                                        phone = phone,
                                        gender = gender,
                                        city = city,
                                        notes = notes,
                                        totalOrders = customer?.totalOrders ?: 0
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isEdit) "Update" else "Save Customer")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: Settle Payment
// ==========================================
@Composable
private fun PaymentSettlementDialog(
    order: TailorOrderEntity,
    onDismiss: () -> Unit,
    onSave: (advance: Double, remaining: Double) -> Unit
) {
    var additionalPaymentStr by remember { mutableStateOf(order.remainingBalance.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Payment Settlement",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PakEmeraldPrimary
                )

                Text(
                    text = "Order ${order.orderNumber} • ${order.customerName}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Bill:", fontSize = 12.sp)
                            Text("₨ ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Already Paid:", fontSize = 12.sp)
                            Text("₨ ${order.advancePaid.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Due:", fontSize = 12.sp)
                            Text("₨ ${order.remainingBalance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }

                OutlinedTextField(
                    value = additionalPaymentStr,
                    onValueChange = { additionalPaymentStr = it },
                    label = { Text("Payment Received Now (₨)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val add = additionalPaymentStr.toDoubleOrNull() ?: 0.0
                            val newAdvance = order.advancePaid + add
                            val newRemaining = (order.totalAmount - newAdvance).coerceAtLeast(0.0)
                            onSave(newAdvance, newRemaining)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Record Payment")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: View Measurements for Order
// ==========================================
@Composable
private fun ViewMeasurementDialog(
    order: TailorOrderEntity,
    measurement: TailorMeasurementEntity?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Order Measurements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PakEmeraldPrimary
                )

                Text(
                    text = "${order.orderNumber} • ${order.customerName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (measurement != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Kameez (Inches)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PakEmeraldPrimary)
                            Text("• Length: ${measurement.length}\"   Chest: ${measurement.chest}\"   Waist: ${measurement.waist}\"")
                            Text("• Shoulder: ${measurement.shoulder}\"   Sleeves: ${measurement.sleeves}\"   Collar: ${measurement.collar}\"")
                            Text("• Daman: ${measurement.daman}\"   Cuff: ${measurement.cuff}\"   Armhole: ${measurement.armhole}\"")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("Shalwar / Trouser (Inches)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PakEmeraldPrimary)
                            Text("• Shalwar Length: ${measurement.shalwarLength}\"   Paincha: ${measurement.paincha}\"")
                            Text("• Asan: ${measurement.asan}\"   Waist: ${measurement.trouserWaist}\"")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("Style Cuts", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PakEmeraldPrimary)
                            Text("• Collar: ${measurement.collarType}")
                            Text("• Cuff: ${measurement.cuffStyle}")
                            Text("• Pocket: ${measurement.pocketStyle}")
                            Text("• Daman: ${measurement.damanStyle}")

                            if (measurement.notes.isNotBlank()) {
                                Text("• Special: ${measurement.notes}", color = Color(0xFFC2185B))
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (order.measurementSummary.isNotBlank()) {
                            "Summary: ${order.measurementSummary}"
                        } else {
                            "No linked measurement book entry found for ${order.customerName}. Please add their paimaish in the register tab."
                        },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
