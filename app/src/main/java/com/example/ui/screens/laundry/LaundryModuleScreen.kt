package com.example.ui.screens.laundry

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LaundryCustomerEntity
import com.example.data.LaundryOrderEntity
import com.example.ui.BusinessViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Palette for Laundry & Dry Cleaners (Aqua / Sky Blue / Clean Indigo)
val LaundryPrimary = Color(0xFF0284C7)      // Vibrant Sky Blue
val LaundryDark = Color(0xFF0369A1)         // Ocean Deep Blue
val LaundryContainer = Color(0xFFE0F2FE)    // Soft Clean Sky Mist
val LaundryAccent = Color(0xFF06B6D4)       // Clean Cyan
val LaundryDarkIndigo = Color(0xFF1E3A8A)   // Navy Textile Blue

val StatusWashingColor = Color(0xFF0284C7)   // Blue
val StatusIroningColor = Color(0xFFD97706)   // Amber / Iron Heat
val StatusReadyColor = Color(0xFF16A34A)     // Emerald Green
val StatusDeliveredColor = Color(0xFF4B5563) // Slate Neutral

enum class LaundryTab {
    ORDERS,
    CUSTOMERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.laundryCustomers.collectAsStateWithLifecycle()
    val orders by viewModel.laundryOrders.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(LaundryTab.ORDERS) }

    // Dialog states
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<LaundryOrderEntity?>(null) }

    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<LaundryCustomerEntity?>(null) }

    var orderForPaymentDialog by remember { mutableStateOf<LaundryOrderEntity?>(null) }
    var orderForReceiptDialog by remember { mutableStateOf<LaundryOrderEntity?>(null) }

    // Search and Status filters
    var orderSearchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LaundryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(LaundryPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalLaundryService,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Laundry & Dry Cleaners",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LaundryDarkIndigo
                                )
                                Text(
                                    text = "Order tracking (Washing &rarr; Ironing &rarr; Ready &rarr; Delivered)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Active orders count in progress
                        val inProgressCount = orders.count { it.status in listOf("WASHING", "IRONING", "READY") }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LaundryPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, LaundryPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = LaundryPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$inProgressCount In Process",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LaundryDark
                                )
                            }
                        }
                    }
                }

                // Module Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = LaundryPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == LaundryTab.ORDERS,
                        onClick = { selectedTab = LaundryTab.ORDERS },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Orders (${orders.size})", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_laundry_orders")
                    )
                    Tab(
                        selected = selectedTab == LaundryTab.CUSTOMERS,
                        onClick = { selectedTab = LaundryTab.CUSTOMERS },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Customers (${customers.size})", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_laundry_customers")
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        LaundryTab.ORDERS -> {
                            orderToEdit = null
                            showNewOrderDialog = true
                        }
                        LaundryTab.CUSTOMERS -> {
                            customerToEdit = null
                            showCustomerDialog = true
                        }
                    }
                },
                containerColor = LaundryPrimary,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = if (selectedTab == LaundryTab.ORDERS) Icons.Default.Add else Icons.Default.Person,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = if (selectedTab == LaundryTab.ORDERS) "New Laundry Order" else "Add Customer",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("fab_laundry_action")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                LaundryTab.ORDERS -> {
                    LaundryOrdersTabContent(
                        orders = orders,
                        searchQuery = orderSearchQuery,
                        onSearchQueryChange = { orderSearchQuery = it },
                        selectedStatusFilter = selectedStatusFilter,
                        onStatusFilterChange = { selectedStatusFilter = it },
                        onAdvanceStatus = { order, nextStatus ->
                            viewModel.updateLaundryOrderStatus(order.id, nextStatus)
                        },
                        onEdit = { order ->
                            orderToEdit = order
                            showNewOrderDialog = true
                        },
                        onDelete = { order ->
                            viewModel.deleteLaundryOrder(order)
                        },
                        onCollectPayment = { order ->
                            orderForPaymentDialog = order
                        },
                        onViewReceipt = { order ->
                            orderForReceiptDialog = order
                        },
                        onShareWhatsApp = { order ->
                            shareLaundryReceipt(context, order)
                        }
                    )
                }
                LaundryTab.CUSTOMERS -> {
                    LaundryCustomersTabContent(
                        customers = customers,
                        orders = orders,
                        onEdit = { customer ->
                            customerToEdit = customer
                            showCustomerDialog = true
                        },
                        onDelete = { customer ->
                            viewModel.deleteLaundryCustomer(customer)
                        },
                        onCreateOrderForCustomer = { customer ->
                            orderToEdit = null
                            customerToEdit = customer
                            showNewOrderDialog = true
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------------------------------

    if (showNewOrderDialog) {
        AddEditLaundryOrderDialog(
            existingOrder = orderToEdit,
            preSelectedCustomer = customerToEdit,
            availableCustomers = customers,
            onDismiss = {
                showNewOrderDialog = false
                orderToEdit = null
                customerToEdit = null
            },
            onSave = { order ->
                viewModel.saveLaundryOrder(order)
                showNewOrderDialog = false
                orderToEdit = null
                customerToEdit = null
            }
        )
    }

    if (showCustomerDialog) {
        AddEditLaundryCustomerDialog(
            existingCustomer = customerToEdit,
            onDismiss = {
                showCustomerDialog = false
                customerToEdit = null
            },
            onSave = { customer ->
                viewModel.saveLaundryCustomer(customer)
                showCustomerDialog = false
                customerToEdit = null
            }
        )
    }

    orderForPaymentDialog?.let { order ->
        LaundryPaymentDialog(
            order = order,
            onDismiss = { orderForPaymentDialog = null },
            onPaymentRecorded = { additionalPayment, newBalance, paymentStatus ->
                viewModel.updateLaundryOrderPayment(order.id, additionalPayment, newBalance, paymentStatus)
                orderForPaymentDialog = null
            }
        )
    }

    orderForReceiptDialog?.let { order ->
        LaundryReceiptDialog(
            order = order,
            onDismiss = { orderForReceiptDialog = null },
            onShare = { shareLaundryReceipt(context, order) }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: LAUNDRY ORDERS LIST & TRACKING WORKFLOW
// -----------------------------------------------------------------------------------------

@Composable
fun LaundryOrdersTabContent(
    orders: List<LaundryOrderEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onAdvanceStatus: (LaundryOrderEntity, String) -> Unit,
    onEdit: (LaundryOrderEntity) -> Unit,
    onDelete: (LaundryOrderEntity) -> Unit,
    onCollectPayment: (LaundryOrderEntity) -> Unit,
    onViewReceipt: (LaundryOrderEntity) -> Unit,
    onShareWhatsApp: (LaundryOrderEntity) -> Unit
) {
    val filteredOrders = orders.filter { order ->
        val matchesQuery = order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.customerPhone.contains(searchQuery, ignoreCase = true) ||
                order.rackLocation.contains(searchQuery, ignoreCase = true)

        val matchesStatus = if (selectedStatusFilter == "ALL") true else order.status == selectedStatusFilter
        matchesQuery && matchesStatus
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_laundry_orders"),
                    placeholder = { Text("Search by Order #, Customer, Phone, Rack...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LaundryPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LaundryPrimary,
                        focusedLabelColor = LaundryPrimary
                    ),
                    singleLine = true
                )

                // Workflow status filter pills
                val filterOptions = listOf(
                    "ALL" to "All (${orders.size})",
                    "WASHING" to "Washing (${orders.count { it.status == "WASHING" }})",
                    "IRONING" to "Ironing (${orders.count { it.status == "IRONING" }})",
                    "READY" to "Ready (${orders.count { it.status == "READY" }})",
                    "DELIVERED" to "Delivered (${orders.count { it.status == "DELIVERED" }})"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { (statusKey, label) ->
                        val isSelected = selectedStatusFilter == statusKey
                        val chipColor by animateColorAsState(
                            targetValue = if (isSelected) LaundryPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            label = "filter_chip"
                        )
                        val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = chipColor,
                            modifier = Modifier
                                .clickable { onStatusFilterChange(statusKey) }
                                .testTag("chip_filter_$statusKey")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Summary metric strip
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LaundryContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, LaundryPrimary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkflowStagePill(
                        title = "1. Washing",
                        count = orders.count { it.status == "WASHING" },
                        icon = Icons.Default.WaterDamage,
                        color = StatusWashingColor
                    )
                    Text("&rarr;", color = LaundryPrimary, fontWeight = FontWeight.Bold)
                    WorkflowStagePill(
                        title = "2. Ironing",
                        count = orders.count { it.status == "IRONING" },
                        icon = Icons.Default.Iron,
                        color = StatusIroningColor
                    )
                    Text("&rarr;", color = LaundryPrimary, fontWeight = FontWeight.Bold)
                    WorkflowStagePill(
                        title = "3. Ready",
                        count = orders.count { it.status == "READY" },
                        icon = Icons.Default.CheckCircle,
                        color = StatusReadyColor
                    )
                    Text("&rarr;", color = LaundryPrimary, fontWeight = FontWeight.Bold)
                    WorkflowStagePill(
                        title = "4. Delivered",
                        count = orders.count { it.status == "DELIVERED" },
                        icon = Icons.Default.DoneAll,
                        color = StatusDeliveredColor
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
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
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = null,
                            tint = LaundryPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No laundry orders found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap '+ New Laundry Order' to register incoming clothes for washing or ironing.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                LaundryOrderCard(
                    order = order,
                    onAdvanceStatus = { nextStatus -> onAdvanceStatus(order, nextStatus) },
                    onEdit = { onEdit(order) },
                    onDelete = { onDelete(order) },
                    onCollectPayment = { onCollectPayment(order) },
                    onViewReceipt = { onViewReceipt(order) },
                    onShareWhatsApp = { onShareWhatsApp(order) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun WorkflowStagePill(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
        }
        Text(
            text = "$count",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun LaundryOrderCard(
    order: LaundryOrderEntity,
    onAdvanceStatus: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollectPayment: () -> Unit,
    onViewReceipt: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val statusColor = when (order.status) {
        "WASHING" -> StatusWashingColor
        "IRONING" -> StatusIroningColor
        "READY" -> StatusReadyColor
        "DELIVERED" -> StatusDeliveredColor
        else -> Color.Gray
    }

    val statusIcon = when (order.status) {
        "WASHING" -> Icons.Default.WaterDamage
        "IRONING" -> Icons.Default.Iron
        "READY" -> Icons.Default.CheckCircle
        "DELIVERED" -> Icons.Default.DoneAll
        else -> Icons.Default.Info
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_laundry_order_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Bar: Order # & Status Badge
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
                        shape = RoundedCornerShape(6.dp),
                        color = LaundryPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LaundryDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (order.urgentDelivery) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEF4444)
                        ) {
                            Text(
                                text = "URGENT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = order.serviceType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Badge with icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = order.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            // Customer Name, Phone & Rack
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = order.customerPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = order.rackLocation,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Clothes Quantity Breakdown Grid (Shirt, Pants, Bedsheet, etc.)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = LaundryContainer.copy(alpha = 0.4f),
                border = BorderStroke(0.5.dp, LaundryPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Clothes Manifest (${order.totalPieces} pcs total):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LaundryDarkIndigo
                        )
                        Text(
                            text = "Due: ${order.deliveryDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LaundryPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.shirtCount > 0) {
                            ClothesBadge(label = "Shirts", count = order.shirtCount)
                        }
                        if (order.pantsCount > 0) {
                            ClothesBadge(label = "Pants/Trousers", count = order.pantsCount)
                        }
                        if (order.bedsheetCount > 0) {
                            ClothesBadge(label = "Bedsheets", count = order.bedsheetCount)
                        }
                        if (order.shalwarKameezCount > 0) {
                            ClothesBadge(label = "Shalwar Kameez", count = order.shalwarKameezCount)
                        }
                        if (order.suitCount > 0) {
                            ClothesBadge(label = "Suits", count = order.suitCount)
                        }
                    }

                    if (order.otherItemsCount > 0 && order.otherItemsDescription.isNotBlank()) {
                        Text(
                            text = "+ ${order.otherItemsCount} other items: ${order.otherItemsDescription}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (order.specialInstructions.isNotBlank()) {
                        Text(
                            text = "Instructions: \"${order.specialInstructions}\"",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = LaundryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Financial breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: ₨ ${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paid: ₨ ${order.advancePaid.toInt()} | Balance: ₨ ${order.balanceDue.toInt()}",
                        fontSize = 11.sp,
                        color = if (order.balanceDue > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Payment Status chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (order.balanceDue <= 0.0) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = if (order.balanceDue <= 0.0) "PAID IN FULL" else "₨ ${order.balanceDue.toInt()} DUE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.balanceDue <= 0.0) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Action Workflow Progress Bar & Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Workflow advance button based on current stage: WASHING -> IRONING -> READY -> DELIVERED
                when (order.status) {
                    "WASHING" -> {
                        Button(
                            onClick = { onAdvanceStatus("IRONING") },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusIroningColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Iron, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send to Ironing &rarr;", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "IRONING" -> {
                        Button(
                            onClick = { onAdvanceStatus("READY") },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusReadyColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Ready &rarr;", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "READY" -> {
                        Button(
                            onClick = { onAdvanceStatus("DELIVERED") },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDeliveredColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Delivered &rarr;", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "DELIVERED" -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delivered & Closed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }
                    }
                }

                // Balance collect payment button
                if (order.balanceDue > 0.0) {
                    OutlinedButton(
                        onClick = onCollectPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Slip / Receipt view
                IconButton(onClick = onViewReceipt, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Receipt, contentDescription = "View Slip", tint = LaundryPrimary, modifier = Modifier.size(18.dp))
                }

                // Share slip on WhatsApp
                IconButton(onClick = onShareWhatsApp, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                }

                // Edit
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }

                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ClothesBadge(label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(0.5.dp, LaundryPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
            Surface(
                shape = CircleShape,
                color = LaundryPrimary
            ) {
                Text(
                    text = "$count",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: LAUNDRY CUSTOMERS DIRECTORY
// -----------------------------------------------------------------------------------------

@Composable
fun LaundryCustomersTabContent(
    customers: List<LaundryCustomerEntity>,
    orders: List<LaundryOrderEntity>,
    onEdit: (LaundryCustomerEntity) -> Unit,
    onDelete: (LaundryCustomerEntity) -> Unit,
    onCreateOrderForCustomer: (LaundryCustomerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_laundry_customers"),
                placeholder = { Text("Search by name, phone, address...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LaundryPrimary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LaundryPrimary,
                    focusedLabelColor = LaundryPrimary
                ),
                singleLine = true
            )
        }

        if (filteredCustomers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = LaundryPrimary, modifier = Modifier.size(44.dp))
                        Text("No customers found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Add customer records to quickly auto-fill orders.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                val customerActiveOrders = orders.filter {
                    it.customerId == customer.id && it.status in listOf("WASHING", "IRONING", "READY")
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_laundry_customer_${customer.id}")
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(LaundryPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = LaundryPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = customer.phone,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (customerActiveOrders.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "${customerActiveOrders.size} In Process",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (customer.address.isNotBlank()) {
                            Text(
                                text = "Address: ${customer.address}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (customer.notes.isNotBlank()) {
                            Text(
                                text = "Preferences: ${customer.notes}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = LaundryDark
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Orders: ${orders.count { it.customerId == customer.id }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { onCreateOrderForCustomer(customer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LaundryPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("New Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(onClick = { onEdit(customer) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }

                                IconButton(onClick = { onDelete(customer) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
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
}

// -----------------------------------------------------------------------------------------
// DIALOG: NEW / EDIT LAUNDRY ORDER (SHIRT, PANTS, BEDSHEET WITH QUANTITY)
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLaundryOrderDialog(
    existingOrder: LaundryOrderEntity?,
    preSelectedCustomer: LaundryCustomerEntity?,
    availableCustomers: List<LaundryCustomerEntity>,
    onDismiss: () -> Unit,
    onSave: (LaundryOrderEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }
    val defaultDeliveryStr = remember { dateFormat.format(Date(System.currentTimeMillis() + 86400000L * 2)) }

    // Customer fields
    var customerName by remember { mutableStateOf(existingOrder?.customerName ?: preSelectedCustomer?.name ?: "") }
    var customerPhone by remember { mutableStateOf(existingOrder?.customerPhone ?: preSelectedCustomer?.phone ?: "") }
    var customerId by remember { mutableStateOf(existingOrder?.customerId ?: preSelectedCustomer?.id ?: 0L) }

    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

    // Service & options
    var serviceType by remember { mutableStateOf(existingOrder?.serviceType ?: "Wash & Iron") }
    var urgentDelivery by remember { mutableStateOf(existingOrder?.urgentDelivery ?: false) }
    var rackLocation by remember { mutableStateOf(existingOrder?.rackLocation ?: "Rack A-1") }
    var specialInstructions by remember { mutableStateOf(existingOrder?.specialInstructions ?: "") }

    // Core Quantities: Shirt, Pants, Bedsheet
    var shirtCount by remember { mutableStateOf(existingOrder?.shirtCount ?: 0) }
    var pantsCount by remember { mutableStateOf(existingOrder?.pantsCount ?: 0) }
    var bedsheetCount by remember { mutableStateOf(existingOrder?.bedsheetCount ?: 0) }
    var shalwarKameezCount by remember { mutableStateOf(existingOrder?.shalwarKameezCount ?: 0) }
    var suitCount by remember { mutableStateOf(existingOrder?.suitCount ?: 0) }
    var otherCount by remember { mutableStateOf(existingOrder?.otherItemsCount ?: 0) }
    var otherDesc by remember { mutableStateOf(existingOrder?.otherItemsDescription ?: "") }

    // Rates per piece based on service type
    val rateShirt = when (serviceType) {
        "Dry Clean" -> 250.0
        "Iron Only" -> 40.0
        "Wash & Fold" -> 60.0
        else -> 100.0 // Wash & Iron
    }
    val ratePants = when (serviceType) {
        "Dry Clean" -> 250.0
        "Iron Only" -> 40.0
        "Wash & Fold" -> 60.0
        else -> 100.0
    }
    val rateBedsheet = when (serviceType) {
        "Dry Clean" -> 450.0
        "Iron Only" -> 80.0
        "Wash & Fold" -> 120.0
        else -> 200.0
    }
    val rateShalwarKameez = when (serviceType) {
        "Dry Clean" -> 400.0
        "Iron Only" -> 70.0
        "Wash & Fold" -> 100.0
        else -> 180.0
    }
    val rateSuit = when (serviceType) {
        "Dry Clean" -> 800.0
        "Iron Only" -> 150.0
        else -> 600.0
    }

    val calculatedTotal = (shirtCount * rateShirt) +
            (pantsCount * ratePants) +
            (bedsheetCount * rateBedsheet) +
            (shalwarKameezCount * rateShalwarKameez) +
            (suitCount * rateSuit) +
            (otherCount * 100.0) +
            (if (urgentDelivery) 200.0 else 0.0)

    var totalAmountInput by remember { mutableStateOf(existingOrder?.totalAmount?.toString() ?: calculatedTotal.toString()) }
    var advanceDepositInput by remember { mutableStateOf(existingOrder?.advancePaid?.toString() ?: "0") }

    var deliveryDate by remember { mutableStateOf(existingOrder?.deliveryDate ?: defaultDeliveryStr) }

    val totalPieces = shirtCount + pantsCount + bedsheetCount + shalwarKameezCount + suitCount + otherCount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocalLaundryService, contentDescription = null, tint = LaundryPrimary)
                Text(
                    text = if (existingOrder != null) "Edit Laundry Order" else "New Laundry Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Selector
                item {
                    Text("Customer Information", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LaundryDark)
                }

                if (availableCustomers.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = isCustomerDropdownExpanded,
                            onExpandedChange = { isCustomerDropdownExpanded = !isCustomerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("input_laundry_customer_name"),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCustomerDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = isCustomerDropdownExpanded,
                                onDismissRequest = { isCustomerDropdownExpanded = false }
                            ) {
                                availableCustomers.forEach { c ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(c.name, fontWeight = FontWeight.Bold)
                                                Text(c.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            customerName = c.name
                                            customerPhone = c.phone
                                            customerId = c.id
                                            isCustomerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_laundry_customer_name"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone Number *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_laundry_customer_phone"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Service Type selection
                item {
                    Text("Service Type & Delivery", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LaundryDark)
                    val services = listOf("Wash & Iron", "Dry Clean", "Iron Only", "Wash & Fold")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        services.forEach { service ->
                            val isSelected = serviceType == service
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) LaundryPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { serviceType = service }
                            ) {
                                Text(
                                    text = service,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = deliveryDate,
                            onValueChange = { deliveryDate = it },
                            label = { Text("Delivery Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rackLocation,
                            onValueChange = { rackLocation = it },
                            label = { Text("Rack Location") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { urgentDelivery = !urgentDelivery }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (urgentDelivery) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (urgentDelivery) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Column {
                            Text("Urgent / Same-Day Delivery (+₨ 200)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Priority washing & express steam pressing", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ITEM QUANTITIES ENTRY (Shirt, Pants, Bedsheet, Shalwar Kameez, Suit)
                item {
                    Text(
                        text = "Clothes Quantities ($totalPieces Pieces)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = LaundryDark
                    )
                }

                // 1. Shirt
                item {
                    ClothesQuantityCounterRow(
                        itemLabel = "Shirts / T-Shirts",
                        ratePerPiece = rateShirt,
                        count = shirtCount,
                        onCountChange = {
                            shirtCount = it
                            totalAmountInput = (((shirtCount * rateShirt) +
                                    (pantsCount * ratePants) +
                                    (bedsheetCount * rateBedsheet) +
                                    (shalwarKameezCount * rateShalwarKameez) +
                                    (suitCount * rateSuit) +
                                    (otherCount * 100.0) +
                                    (if (urgentDelivery) 200.0 else 0.0))).toString()
                        }
                    )
                }

                // 2. Pants
                item {
                    ClothesQuantityCounterRow(
                        itemLabel = "Pants / Trousers / Jeans",
                        ratePerPiece = ratePants,
                        count = pantsCount,
                        onCountChange = {
                            pantsCount = it
                            totalAmountInput = (((shirtCount * rateShirt) +
                                    (pantsCount * ratePants) +
                                    (bedsheetCount * rateBedsheet) +
                                    (shalwarKameezCount * rateShalwarKameez) +
                                    (suitCount * rateSuit) +
                                    (otherCount * 100.0) +
                                    (if (urgentDelivery) 200.0 else 0.0))).toString()
                        }
                    )
                }

                // 3. Bedsheet
                item {
                    ClothesQuantityCounterRow(
                        itemLabel = "Bedsheets / Linen Covers",
                        ratePerPiece = rateBedsheet,
                        count = bedsheetCount,
                        onCountChange = {
                            bedsheetCount = it
                            totalAmountInput = (((shirtCount * rateShirt) +
                                    (pantsCount * ratePants) +
                                    (bedsheetCount * rateBedsheet) +
                                    (shalwarKameezCount * rateShalwarKameez) +
                                    (suitCount * rateSuit) +
                                    (otherCount * 100.0) +
                                    (if (urgentDelivery) 200.0 else 0.0))).toString()
                        }
                    )
                }

                // 4. Shalwar Kameez
                item {
                    ClothesQuantityCounterRow(
                        itemLabel = "Shalwar Kameez (Suit)",
                        ratePerPiece = rateShalwarKameez,
                        count = shalwarKameezCount,
                        onCountChange = {
                            shalwarKameezCount = it
                            totalAmountInput = (((shirtCount * rateShirt) +
                                    (pantsCount * ratePants) +
                                    (bedsheetCount * rateBedsheet) +
                                    (shalwarKameezCount * rateShalwarKameez) +
                                    (suitCount * rateSuit) +
                                    (otherCount * 100.0) +
                                    (if (urgentDelivery) 200.0 else 0.0))).toString()
                        }
                    )
                }

                // 5. Coats / Suits
                item {
                    ClothesQuantityCounterRow(
                        itemLabel = "Coat / Formal Suits",
                        ratePerPiece = rateSuit,
                        count = suitCount,
                        onCountChange = {
                            suitCount = it
                            totalAmountInput = (((shirtCount * rateShirt) +
                                    (pantsCount * ratePants) +
                                    (bedsheetCount * rateBedsheet) +
                                    (shalwarKameezCount * rateShalwarKameez) +
                                    (suitCount * rateSuit) +
                                    (otherCount * 100.0) +
                                    (if (urgentDelivery) 200.0 else 0.0))).toString()
                        }
                    )
                }

                // Other items
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = otherDesc,
                            onValueChange = { otherDesc = it },
                            label = { Text("Other Items (Curtains, Blanket, etc.)") },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = otherCount.toString(),
                            onValueChange = { otherCount = it.toIntOrNull() ?: 0 },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                // Special notes / Collar starch
                item {
                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = { Text("Special Instructions (e.g. Starch, Delicate fabric)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Billing & Advance Deposit
                item {
                    Text("Payment & Billing (PKR)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LaundryDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalAmountInput,
                            onValueChange = { totalAmountInput = it },
                            label = { Text("Total Bill (₨) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = advanceDepositInput,
                            onValueChange = { advanceDepositInput = it },
                            label = { Text("Advance Paid (₨)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    val bill = totalAmountInput.toDoubleOrNull() ?: 0.0
                    val advance = advanceDepositInput.toDoubleOrNull() ?: 0.0
                    val rem = (bill - advance).coerceAtLeast(0.0)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (rem <= 0.0) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remaining Balance Due:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "₨ ${rem.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rem <= 0.0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank() && customerPhone.isNotBlank() && totalPieces > 0) {
                        val totalAmt = totalAmountInput.toDoubleOrNull() ?: 0.0
                        val adv = advanceDepositInput.toDoubleOrNull() ?: 0.0
                        val bal = (totalAmt - adv).coerceAtLeast(0.0)
                        val payStatus = if (bal <= 0.0) "PAID" else if (adv > 0.0) "PARTIAL" else "PENDING"

                        val orderNumber = existingOrder?.orderNumber
                            ?: "LD-${System.currentTimeMillis().toString().takeLast(4)}"

                        val newOrder = LaundryOrderEntity(
                            id = existingOrder?.id ?: 0L,
                            businessId = existingOrder?.businessId ?: 0L,
                            orderNumber = orderNumber,
                            customerId = customerId,
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            serviceType = serviceType,
                            urgentDelivery = urgentDelivery,
                            shirtCount = shirtCount,
                            pantsCount = pantsCount,
                            bedsheetCount = bedsheetCount,
                            shalwarKameezCount = shalwarKameezCount,
                            suitCount = suitCount,
                            otherItemsCount = otherCount,
                            otherItemsDescription = otherDesc.trim(),
                            totalPieces = totalPieces,
                            status = existingOrder?.status ?: "WASHING",
                            totalAmount = totalAmt,
                            advancePaid = adv,
                            balanceDue = bal,
                            paymentStatus = payStatus,
                            orderDate = existingOrder?.orderDate ?: todayStr,
                            deliveryDate = deliveryDate.ifBlank { defaultDeliveryStr },
                            specialInstructions = specialInstructions.trim(),
                            rackLocation = rackLocation.ifBlank { "Rack A-1" }
                        )
                        onSave(newOrder)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LaundryPrimary),
                shape = RoundedCornerShape(8.dp),
                enabled = customerName.isNotBlank() && customerPhone.isNotBlank() && totalPieces > 0,
                modifier = Modifier.testTag("btn_save_laundry_order")
            ) {
                Text("Save Order", fontWeight = FontWeight.Bold)
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
fun ClothesQuantityCounterRow(
    itemLabel: String,
    ratePerPiece: Double,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(itemLabel, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text("₨ ${ratePerPiece.toInt()} / pc", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { if (count > 0) onCountChange(count - 1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "$count",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.width(28.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                IconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(LaundryPrimary, CircleShape)
                ) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: ADD / EDIT LAUNDRY CUSTOMER
// -----------------------------------------------------------------------------------------

@Composable
fun AddEditLaundryCustomerDialog(
    existingCustomer: LaundryCustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (LaundryCustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(existingCustomer?.phone ?: "") }
    var address by remember { mutableStateOf(existingCustomer?.address ?: "") }
    var notes by remember { mutableStateOf(existingCustomer?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingCustomer != null) "Edit Laundry Customer" else "Add Customer Record",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_phone"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address / House #") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Washing Preferences (Starch, Fragrance)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            LaundryCustomerEntity(
                                id = existingCustomer?.id ?: 0L,
                                businessId = existingCustomer?.businessId ?: 0L,
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LaundryPrimary),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Save Customer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -----------------------------------------------------------------------------------------
// DIALOG: COLLECT PAYMENT
// -----------------------------------------------------------------------------------------

@Composable
fun LaundryPaymentDialog(
    order: LaundryOrderEntity,
    onDismiss: () -> Unit,
    onPaymentRecorded: (additionalPayment: Double, newBalance: Double, paymentStatus: String) -> Unit
) {
    var paymentAmountInput by remember { mutableStateOf(order.balanceDue.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Collect Laundry Payment", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Order: ${order.orderNumber} (${order.customerName})", fontWeight = FontWeight.SemiBold)
                Text("Total Amount: ₨ ${order.totalAmount.toInt()}", fontSize = 12.sp)
                Text("Already Paid: ₨ ${order.advancePaid.toInt()}", fontSize = 12.sp)
                Text("Balance Due: ₨ ${order.balanceDue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = paymentAmountInput,
                    onValueChange = { paymentAmountInput = it },
                    label = { Text("Payment Received (₨) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = paymentAmountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val newBal = (order.balanceDue - amt).coerceAtLeast(0.0)
                        val payStatus = if (newBal <= 0.0) "PAID" else "PARTIAL"
                        onPaymentRecorded(amt, newBal, payStatus)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Record Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -----------------------------------------------------------------------------------------
// DIALOG: PRINTABLE / SHAREABLE RECEIPT
// -----------------------------------------------------------------------------------------

@Composable
fun LaundryReceiptDialog(
    order: LaundryOrderEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Laundry Receipt Slip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = LaundryPrimary.copy(alpha = 0.15f)
                ) {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LaundryDark, modifier = Modifier.padding(4.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("CUSTOMER: ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("PHONE: ${order.customerPhone}", fontSize = 11.sp)
                Text("SERVICE: ${order.serviceType} | RACK: ${order.rackLocation}", fontSize = 11.sp)
                Text("STATUS: ${order.status}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LaundryDark)
                Text("DUE DATE: ${order.deliveryDate}", fontSize = 11.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                )

                Text("CLOTHES MANIFEST:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                if (order.shirtCount > 0) Text("• Shirts: ${order.shirtCount} pcs", fontSize = 11.sp)
                if (order.pantsCount > 0) Text("• Pants/Trousers: ${order.pantsCount} pcs", fontSize = 11.sp)
                if (order.bedsheetCount > 0) Text("• Bedsheets: ${order.bedsheetCount} pcs", fontSize = 11.sp)
                if (order.shalwarKameezCount > 0) Text("• Shalwar Kameez: ${order.shalwarKameezCount} suits", fontSize = 11.sp)
                if (order.suitCount > 0) Text("• Coats/Suits: ${order.suitCount} pcs", fontSize = 11.sp)
                if (order.otherItemsCount > 0) Text("• Other: ${order.otherItemsCount} pcs (${order.otherItemsDescription})", fontSize = 11.sp)
                Text("TOTAL PIECES: ${order.totalPieces}", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("₨ ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Advance Paid:", fontSize = 11.sp)
                    Text("₨ ${order.advancePaid.toInt()}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Balance Due:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                    Text("₨ ${order.balanceDue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Slip", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun shareLaundryReceipt(context: Context, order: LaundryOrderEntity) {
    val itemsSummary = buildString {
        if (order.shirtCount > 0) append("${order.shirtCount} Shirts, ")
        if (order.pantsCount > 0) append("${order.pantsCount} Pants, ")
        if (order.bedsheetCount > 0) append("${order.bedsheetCount} Bedsheets, ")
        if (order.shalwarKameezCount > 0) append("${order.shalwarKameezCount} Shalwar Kameez, ")
        if (order.suitCount > 0) append("${order.suitCount} Suits, ")
        if (order.otherItemsCount > 0) append("${order.otherItemsCount} Other, ")
    }.removeSuffix(", ")

    val text = """
        🧺 *LAUNDRY & DRY CLEANERS SLIP*
        ━━━━━━━━━━━━━━━━━━━━━
        *Order #:* ${order.orderNumber}
        *Customer:* ${order.customerName} (${order.customerPhone})
        *Service:* ${order.serviceType}
        *Rack Location:* ${order.rackLocation}
        *Status:* ${order.status}
        
        *Clothes Manifest (${order.totalPieces} Pcs):*
        $itemsSummary
        
        *Total Bill:* ₨ ${order.totalAmount.toInt()}
        *Advance Paid:* ₨ ${order.advancePaid.toInt()}
        *Remaining Balance:* ₨ ${order.balanceDue.toInt()}
        *Delivery Date:* ${order.deliveryDate}
        
        Thank you for choosing our Laundry & Dry Cleaning service!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Laundry Receipt"))
}
