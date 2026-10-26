package com.example.ui.screens.bakery

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BakeryCakeOrderEntity
import com.example.data.BakeryItemEntity
import com.example.ui.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AmberBakery = Color(0xFFD97706)
private val DeepAmber = Color(0xFFB45309)
private val LightBakeryBg = Color(0xFFFFFBEB)
private val PinkCake = Color(0xFFDB2777)
private val PurpleWedding = Color(0xFF7C3AED)
private val BlueAnniversary = Color(0xFF2563EB)
private val GreenReady = Color(0xFF059669)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakeryModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.bakeryItems.collectAsState()
    val cakeOrders by viewModel.bakeryCakeOrders.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Custom Cakes", "Product Inventory", "Expiry & Stock")

    // Dialog states
    var showAddCakeOrderDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<BakeryCakeOrderEntity?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<BakeryItemEntity?>(null) }
    var orderForPaymentDialog by remember { mutableStateOf<BakeryCakeOrderEntity?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    .background(AmberBakery, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cake,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Bakery & Sweets Hub",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mithai, Custom Cakes & Fresh Inventory",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Quick action button based on tab
                        FilledTonalButton(
                            onClick = {
                                if (selectedTabIndex == 0) {
                                    orderToEdit = null
                                    showAddCakeOrderDialog = true
                                } else {
                                    itemToEdit = null
                                    showAddItemDialog = true
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AmberBakery.copy(alpha = 0.15f),
                                contentColor = DeepAmber
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_bakery_add_new")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedTabIndex == 0) "New Cake" else "New Product",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Module Tab Bar
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = DeepAmber,
                        divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (index) {
                                                0 -> Icons.Default.Cake
                                                1 -> Icons.Default.Inventory2
                                                else -> Icons.Default.DateRange
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                modifier = Modifier.testTag("tab_bakery_$index")
                            )
                        }
                    }
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
            when (selectedTabIndex) {
                0 -> CustomCakesTab(
                    cakeOrders = cakeOrders,
                    onAddNewOrder = {
                        orderToEdit = null
                        showAddCakeOrderDialog = true
                    },
                    onEditOrder = { order ->
                        orderToEdit = order
                        showAddCakeOrderDialog = true
                    },
                    onDeleteOrder = { orderId -> viewModel.deleteBakeryCakeOrder(orderId) },
                    onAdvanceStatus = { order ->
                        val nextStatus = when (order.status) {
                            "RECEIVED" -> "BAKING"
                            "BAKING" -> "DECORATING"
                            "DECORATING" -> "READY"
                            "READY" -> "DELIVERED"
                            else -> order.status
                        }
                        viewModel.updateBakeryCakeOrderStatus(order.id, nextStatus)
                    },
                    onOpenPaymentDialog = { order -> orderForPaymentDialog = order },
                    onShareOrder = { order ->
                        val shareText = """
                            *${order.occasion.uppercase()} CAKE ORDER - ${order.orderNumber}*
                            Customer: ${order.customerName} (${order.customerPhone})
                            Flavor: ${order.flavor}
                            Weight: ${order.weightLbs} Lbs (${order.spongeType})
                            Message: "${order.messageOnCake}"
                            Design Notes: ${order.customDesignNotes.ifBlank { "Standard Decoration" }}
                            Delivery: ${order.deliveryDate} at ${order.deliveryTime}
                            Status: ${order.status}
                            Total Bill: Rs. ${order.totalPrice.toInt()}
                            Advance Paid: Rs. ${order.advancePaid.toInt()}
                            Balance Due: Rs. ${order.remainingBalance.toInt()}
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Cake Order Slip"))
                    }
                )

                1 -> ProductInventoryTab(
                    items = items,
                    onAddNewItem = {
                        itemToEdit = null
                        showAddItemDialog = true
                    },
                    onEditItem = { item ->
                        itemToEdit = item
                        showAddItemDialog = true
                    },
                    onDeleteItem = { id -> viewModel.deleteBakeryItem(id) },
                    onUpdateStock = { id, newStock -> viewModel.updateBakeryItemStock(id, newStock) }
                )

                2 -> ExpiryAndStockAuditTab(
                    items = items,
                    onEditItem = { item ->
                        itemToEdit = item
                        showAddItemDialog = true
                    }
                )
            }
        }
    }

    // Add / Edit Cake Order Dialog
    if (showAddCakeOrderDialog) {
        AddEditCakeOrderDialog(
            orderToEdit = orderToEdit,
            onDismiss = { showAddCakeOrderDialog = false },
            onSave = { savedOrder ->
                viewModel.saveBakeryCakeOrder(savedOrder)
                showAddCakeOrderDialog = false
            }
        )
    }

    // Add / Edit Inventory Product Dialog
    if (showAddItemDialog) {
        AddEditBakeryItemDialog(
            itemToEdit = itemToEdit,
            onDismiss = { showAddItemDialog = false },
            onSave = { savedItem ->
                viewModel.saveBakeryItem(savedItem)
                showAddItemDialog = false
            }
        )
    }

    // Payment Settlement Dialog
    if (orderForPaymentDialog != null) {
        val currentOrder = orderForPaymentDialog!!
        CakePaymentDialog(
            order = currentOrder,
            onDismiss = { orderForPaymentDialog = null },
            onConfirm = { advance, balance ->
                viewModel.updateBakeryCakeOrderPayment(currentOrder.id, advance, balance)
                orderForPaymentDialog = null
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 1: CUSTOM CAKE ORDERS & STATUS TRACKING
// -------------------------------------------------------------
@Composable
private fun CustomCakesTab(
    cakeOrders: List<BakeryCakeOrderEntity>,
    onAddNewOrder: () -> Unit,
    onEditOrder: (BakeryCakeOrderEntity) -> Unit,
    onDeleteOrder: (Long) -> Unit,
    onAdvanceStatus: (BakeryCakeOrderEntity) -> Unit,
    onOpenPaymentDialog: (BakeryCakeOrderEntity) -> Unit,
    onShareOrder: (BakeryCakeOrderEntity) -> Unit
) {
    var selectedOccasionFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val occasions = listOf("All", "Birthday", "Wedding", "Anniversary", "Custom Event")
    val statuses = listOf("All", "RECEIVED", "BAKING", "DECORATING", "READY", "DELIVERED")

    val filteredOrders = cakeOrders.filter { order ->
        val occasionMatch = selectedOccasionFilter == "All" || order.occasion.equals(selectedOccasionFilter, ignoreCase = true)
        val statusMatch = selectedStatusFilter == "All" || order.status == selectedStatusFilter
        occasionMatch && statusMatch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stats Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightBakeryBg),
                border = BorderStroke(1.dp, AmberBakery.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniBadge(
                        label = "Total Orders",
                        value = "${cakeOrders.size}",
                        icon = Icons.Default.ReceiptLong,
                        color = DeepAmber
                    )
                    MetricMiniBadge(
                        label = "In Kitchen",
                        value = "${cakeOrders.count { it.status in listOf("RECEIVED", "BAKING", "DECORATING") }}",
                        icon = Icons.Default.SoupKitchen,
                        color = PinkCake
                    )
                    MetricMiniBadge(
                        label = "Ready to Pickup",
                        value = "${cakeOrders.count { it.status == "READY" }}",
                        icon = Icons.Default.CheckCircle,
                        color = GreenReady
                    )
                    MetricMiniBadge(
                        label = "Delivered",
                        value = "${cakeOrders.count { it.status == "DELIVERED" }}",
                        icon = Icons.Default.DoneAll,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }

        // Occasion Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Occasion",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(occasions) { occasion ->
                        val isSelected = selectedOccasionFilter == occasion
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedOccasionFilter = occasion },
                            label = { Text(occasion, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (occasion) {
                                    "Birthday" -> PinkCake.copy(alpha = 0.15f)
                                    "Wedding" -> PurpleWedding.copy(alpha = 0.15f)
                                    "Anniversary" -> BlueAnniversary.copy(alpha = 0.15f)
                                    else -> AmberBakery.copy(alpha = 0.15f)
                                },
                                selectedLabelColor = when (occasion) {
                                    "Birthday" -> PinkCake
                                    "Wedding" -> PurpleWedding
                                    "Anniversary" -> BlueAnniversary
                                    else -> DeepAmber
                                }
                            )
                        )
                    }
                }
            }
        }

        // Status Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Order Stage Pipeline",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(statuses) { status ->
                        val isSelected = selectedStatusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = status },
                            label = {
                                Text(
                                    when (status) {
                                        "RECEIVED" -> "Received"
                                        "BAKING" -> "Baking"
                                        "DECORATING" -> "Decorating"
                                        "READY" -> "Ready"
                                        "DELIVERED" -> "Delivered"
                                        else -> "All Status"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepAmber,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = DeepAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No custom cake orders found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Take a new custom Birthday, Wedding, or Anniversary cake order with weight, flavor, and delivery details.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onAddNewOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberBakery)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Book Cake Order")
                        }
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                CakeOrderCard(
                    order = order,
                    onEdit = { onEditOrder(order) },
                    onDelete = { onDeleteOrder(order.id) },
                    onAdvanceStatus = { onAdvanceStatus(order) },
                    onOpenPayment = { onOpenPaymentDialog(order) },
                    onShare = { onShareOrder(order) }
                )
            }
        }
    }
}

@Composable
private fun MetricMiniBadge(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// CAKE ORDER CARD WITH 5-STEP PIPELINE TRACKING
// -------------------------------------------------------------
@Composable
private fun CakeOrderCard(
    order: BakeryCakeOrderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onOpenPayment: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val occasionColor = when (order.occasion.lowercase()) {
        "birthday" -> PinkCake
        "wedding" -> PurpleWedding
        "anniversary" -> BlueAnniversary
        else -> AmberBakery
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("cake_order_card_${order.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Order Number, Occasion & Dropdown Menu
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
                        color = occasionColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, occasionColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (order.occasion.lowercase()) {
                                    "birthday" -> Icons.Default.Cake
                                    "wedding" -> Icons.Default.Favorite
                                    "anniversary" -> Icons.Default.Celebration
                                    else -> Icons.Default.Stars
                                },
                                contentDescription = null,
                                tint = occasionColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = order.occasion.uppercase(),
                                color = occasionColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Update Payment") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenPayment()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Order Slip") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Call Customer") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                                context.startActivity(callIntent)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Order", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Customer Name & Flavor Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.customerPhone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${order.weightLbs} Lbs • ${order.spongeType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Flavor & Inscription Callout
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AmberBakery.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, AmberBakery.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = DeepAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Flavor: ${order.flavor}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepAmber
                        )
                    }

                    if (order.messageOnCake.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = PinkCake,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "\"${order.messageOnCake}\"",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (order.customDesignNotes.isNotBlank()) {
                        Text(
                            text = "Design: ${order.customDesignNotes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delivery Timeline & Payment Snapshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = DeepAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "Delivery: ${order.deliveryDate}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Time: ${order.deliveryTime}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs. ${order.totalPrice.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (order.remainingBalance > 0) {
                        Text(
                            text = "Due: Rs. ${order.remainingBalance.toInt()}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Fully Paid",
                            color = GreenReady,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.25f))

            // 5-STAGE STATUS PIPELINE TRACKER
            CakeOrderStatusPipeline(currentStatus = order.status)

            // Bottom Actions: Advance Stage & Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenPayment,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (order.remainingBalance > 0) "Settle (Rs. ${order.remainingBalance.toInt()})" else "Paid in Full",
                        fontSize = 11.sp
                    )
                }

                if (order.status != "DELIVERED") {
                    Button(
                        onClick = onAdvanceStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (order.status) {
                                "RECEIVED" -> AmberBakery
                                "BAKING" -> PinkCake
                                "DECORATING" -> PurpleWedding
                                "READY" -> GreenReady
                                else -> DeepAmber
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_advance_status_${order.id}")
                    ) {
                        Text(
                            text = when (order.status) {
                                "RECEIVED" -> "Start Baking ➔"
                                "BAKING" -> "To Decorating ➔"
                                "DECORATING" -> "Mark Ready ➔"
                                "READY" -> "Deliver Cake ✓"
                                else -> "Next Stage"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenReady.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GreenReady.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenReady, modifier = Modifier.size(14.dp))
                            Text("Delivered to Customer", color = GreenReady, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5-STAGE PIPELINE COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun CakeOrderStatusPipeline(currentStatus: String) {
    val stages = listOf("RECEIVED", "BAKING", "DECORATING", "READY", "DELIVERED")
    val currentIndex = stages.indexOf(currentStatus).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stages.forEachIndexed { index, stage ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex

            val stageColor = when {
                isCurrent -> DeepAmber
                isCompleted -> GreenReady
                else -> Color.LightGray
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isCompleted) stageColor else Color.Transparent,
                            CircleShape
                        )
                        .then(
                            if (!isCompleted) Modifier.clip(CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = when (stage) {
                                "RECEIVED" -> Icons.Default.Receipt
                                "BAKING" -> Icons.Default.SoupKitchen
                                "DECORATING" -> Icons.Default.Brush
                                "READY" -> Icons.Default.Check
                                "DELIVERED" -> Icons.Default.DoneAll
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, Color.LightGray),
                            modifier = Modifier.size(18.dp),
                            color = Color.Transparent
                        ) {}
                    }
                }

                Text(
                    text = when (stage) {
                        "RECEIVED" -> "Received"
                        "BAKING" -> "Baking"
                        "DECORATING" -> "Decorating"
                        "READY" -> "Ready"
                        "DELIVERED" -> "Delivered"
                        else -> stage
                    },
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) DeepAmber else if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (index < stages.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(if (index < currentIndex) GreenReady else Color.LightGray.copy(alpha = 0.5f))
                        .offset(y = (-6).dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: PRODUCT INVENTORY WITH PRODUCTION & EXPIRY DATES
// -------------------------------------------------------------
@Composable
private fun ProductInventoryTab(
    items: List<BakeryItemEntity>,
    onAddNewItem: () -> Unit,
    onEditItem: (BakeryItemEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onUpdateStock: (Long, Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Mithai / Sweets", "Bakery & Breads", "Pastries & Cakes", "Snacks & Savories", "Biscuits & Cookies")

    val filteredItems = items.filter { item ->
        val categoryMatch = selectedCategory == "All" || item.category == selectedCategory
        val searchMatch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.batchNumber.contains(searchQuery, ignoreCase = true)
        categoryMatch && searchMatch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search sweets, breads, cakes, or batch...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bakery_search"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 2.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepAmber,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Inventory Items List
        if (filteredItems.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = DeepAmber, modifier = Modifier.size(44.dp))
                        Text("No bakery products match criteria", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = onAddNewItem,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberBakery)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Add Product")
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                BakeryInventoryCard(
                    item = item,
                    onEdit = { onEditItem(item) },
                    onDelete = { onDeleteItem(item.id) },
                    onUpdateStock = { newStock -> onUpdateStock(item.id, newStock) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// BAKERY PRODUCT INVENTORY CARD (PRODUCTION & EXPIRY DATES)
// -------------------------------------------------------------
@Composable
private fun BakeryInventoryCard(
    item: BakeryItemEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Double) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val freshnessColor = when {
        item.isExpired -> MaterialTheme.colorScheme.error
        item.isExpiringSoon -> Color(0xFFEA580C) // Orange
        else -> GreenReady
    }

    val freshnessLabel = when {
        item.isExpired -> "EXPIRED"
        item.isExpiringSoon -> "EXPIRING SOON"
        else -> "FRESH"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (item.isExpired) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            else if (item.isExpiringSoon) Color(0xFFEA580C).copy(alpha = 0.5f)
            else Color.LightGray.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bakery_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Name, Category, Freshness badge & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${item.category} • Batch: ${item.batchNumber.ifBlank { "N/A" }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = freshnessColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, freshnessColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = freshnessLabel,
                            color = freshnessColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Product") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Product", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // PRODUCTION & EXPIRY DATES HIGHLIGHT
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Production Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = DeepAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("Production", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = item.productionDate.ifBlank { "Unset" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )

                    // Expiry Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = freshnessColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("Best Before", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = item.expiryDate.ifBlank { "Unset" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = freshnessColor
                            )
                        }
                    }
                }
            }

            // Price and Stock Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rs. ${item.pricePerUnit.toInt()} / ${item.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepAmber
                    )
                    if (item.isLowStock) {
                        Text(
                            text = "Low Stock Alert (min ${item.lowStockThreshold.toInt()} ${item.unit})",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Stock Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            val newStock = (item.currentStock - 1.0).coerceAtLeast(0.0)
                            onUpdateStock(newStock)
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Stock", modifier = Modifier.size(16.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (item.isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(min = 60.dp)
                    ) {
                        Text(
                            text = "${if (item.currentStock % 1.0 == 0.0) item.currentStock.toInt() else item.currentStock} ${item.unit}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            val newStock = item.currentStock + 1.0
                            onUpdateStock(newStock)
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Stock", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: EXPIRY AUDIT & STOCK SUMMARY
// -------------------------------------------------------------
@Composable
private fun ExpiryAndStockAuditTab(
    items: List<BakeryItemEntity>,
    onEditItem: (BakeryItemEntity) -> Unit
) {
    val expiredItems = items.filter { it.isExpired }
    val expiringSoonItems = items.filter { it.isExpiringSoon && !it.isExpired }
    val lowStockItems = items.filter { it.isLowStock && !it.isExpired }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuditSummaryCard(
                    title = "Expired",
                    count = "${expiredItems.size}",
                    subtitle = "Needs Immediate Removal",
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                AuditSummaryCard(
                    title = "Expiring Soon",
                    count = "${expiringSoonItems.size}",
                    subtitle = "Next 48 Hours",
                    color = Color(0xFFEA580C),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AuditSummaryCard(
                title = "Low Stock Warning",
                count = "${lowStockItems.size}",
                subtitle = "Items below minimum replenishment threshold",
                color = DeepAmber,
                icon = Icons.Default.ProductionQuantityLimits,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Expired Section
        if (expiredItems.isNotEmpty()) {
            item {
                Text(
                    text = "Expired Items (Action Required)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            items(expiredItems, key = { "exp_${it.id}" }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Expired on: ${item.expiryDate} (Batch: ${item.batchNumber})", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Text("${item.currentStock} ${item.unit} in store", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Expiring Soon Section
        if (expiringSoonItems.isNotEmpty()) {
            item {
                Text(
                    text = "Expiring in Next 48 Hours",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFEA580C)
                )
            }
            items(expiringSoonItems, key = { "soon_${it.id}" }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBakeryBg),
                    border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Expires: ${item.expiryDate} (Sell with priority)", fontSize = 11.sp, color = Color(0xFFEA580C))
                        }
                        Text("${item.currentStock} ${item.unit}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditSummaryCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            }
            Text(count, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -------------------------------------------------------------
// DIALOG: ADD / EDIT CUSTOM CAKE ORDER
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditCakeOrderDialog(
    orderToEdit: BakeryCakeOrderEntity?,
    onDismiss: () -> Unit,
    onSave: (BakeryCakeOrderEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val defaultDeliveryDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        sdf.format(cal.time)
    }

    var orderNumber by remember { mutableStateOf(orderToEdit?.orderNumber ?: "CAKE-${(100..999).random()}") }
    var customerName by remember { mutableStateOf(orderToEdit?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(orderToEdit?.customerPhone ?: "+92 ") }
    var occasion by remember { mutableStateOf(orderToEdit?.occasion ?: "Birthday") }
    var flavor by remember { mutableStateOf(orderToEdit?.flavor ?: "Belgian Chocolate Fudge") }
    var weightLbsStr by remember { mutableStateOf(orderToEdit?.weightLbs?.toString() ?: "2.5") }
    var spongeType by remember { mutableStateOf(orderToEdit?.spongeType ?: "Regular (Egg)") }
    var messageOnCake by remember { mutableStateOf(orderToEdit?.messageOnCake ?: "") }
    var customDesignNotes by remember { mutableStateOf(orderToEdit?.customDesignNotes ?: "") }
    var deliveryDate by remember { mutableStateOf(orderToEdit?.deliveryDate ?: defaultDeliveryDate) }
    var deliveryTime by remember { mutableStateOf(orderToEdit?.deliveryTime ?: "06:00 PM") }
    var status by remember { mutableStateOf(orderToEdit?.status ?: "RECEIVED") }
    var totalPriceStr by remember { mutableStateOf(orderToEdit?.totalPrice?.toInt()?.toString() ?: "3500") }
    var advancePaidStr by remember { mutableStateOf(orderToEdit?.advancePaid?.toInt()?.toString() ?: "1500") }

    val occasions = listOf("Birthday", "Wedding", "Anniversary", "Custom Event")
    val popularFlavors = listOf(
        "Belgian Chocolate Fudge", "Red Velvet & Cream Cheese",
        "Lotus Biscoff Dream", "Fresh Pineapple Cream",
        "Vanilla Salted Caramel", "Pistachio Kulfi", "Nutella Hazelnut"
    )
    val spongeTypes = listOf("Regular (Egg)", "Eggless")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Cake, contentDescription = null, tint = DeepAmber)
                Text(
                    text = if (orderToEdit == null) "Book Custom Cake Order" else "Edit Cake Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                // Occasion Selector
                Text("Occasion", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    occasions.forEach { occ ->
                        FilterChip(
                            selected = occasion == occ,
                            onClick = { occasion = occ },
                            label = { Text(occ, fontSize = 11.sp) }
                        )
                    }
                }

                // Customer Name & Phone
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Phone *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Flavor Selection
                Text("Popular Flavors", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularFlavors.forEach { flv ->
                        FilterChip(
                            selected = flavor == flv,
                            onClick = { flavor = flv },
                            label = { Text(flv, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = flavor,
                    onValueChange = { flavor = it },
                    label = { Text("Selected Flavor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Weight & Sponge Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightLbsStr,
                        onValueChange = { weightLbsStr = it },
                        label = { Text("Weight (Lbs) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Sponge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            spongeTypes.forEach { st ->
                                FilterChip(
                                    selected = spongeType == st,
                                    onClick = { spongeType = st },
                                    label = { Text(st, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                // Inscription & Design Notes
                OutlinedTextField(
                    value = messageOnCake,
                    onValueChange = { messageOnCake = it },
                    label = { Text("Message on Cake (e.g., Happy 5th Birthday)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2
                )

                OutlinedTextField(
                    value = customDesignNotes,
                    onValueChange = { customDesignNotes = it },
                    label = { Text("Design / Theme Notes (Fondant, Roses, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2
                )

                // Delivery Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryDate,
                        onValueChange = { deliveryDate = it },
                        label = { Text("Delivery Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deliveryTime,
                        onValueChange = { deliveryTime = it },
                        label = { Text("Delivery Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalPriceStr,
                        onValueChange = { totalPriceStr = it },
                        label = { Text("Total Bill (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = advancePaidStr,
                        onValueChange = { advancePaidStr = it },
                        label = { Text("Advance Paid (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank()) {
                        val total = totalPriceStr.toDoubleOrNull() ?: 0.0
                        val advance = advancePaidStr.toDoubleOrNull() ?: 0.0
                        val weight = weightLbsStr.toDoubleOrNull() ?: 2.0
                        val balance = (total - advance).coerceAtLeast(0.0)

                        val cakeOrder = BakeryCakeOrderEntity(
                            id = orderToEdit?.id ?: 0L,
                            businessId = orderToEdit?.businessId ?: 0L,
                            orderNumber = orderNumber,
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            occasion = occasion,
                            flavor = flavor.trim(),
                            weightLbs = weight,
                            spongeType = spongeType,
                            messageOnCake = messageOnCake.trim(),
                            customDesignNotes = customDesignNotes.trim(),
                            deliveryDate = deliveryDate.trim(),
                            deliveryTime = deliveryTime.trim(),
                            status = status,
                            totalPrice = total,
                            advancePaid = advance,
                            remainingBalance = balance
                        )
                        onSave(cakeOrder)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberBakery)
            ) {
                Text("Save Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// DIALOG: ADD / EDIT BAKERY INVENTORY PRODUCT
// -------------------------------------------------------------
@Composable
private fun AddEditBakeryItemDialog(
    itemToEdit: BakeryItemEntity?,
    onDismiss: () -> Unit,
    onSave: (BakeryItemEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayStr = remember { sdf.format(Date()) }
    val defaultExpiryStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 3)
        sdf.format(cal.time)
    }

    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "Mithai / Sweets") }
    var unit by remember { mutableStateOf(itemToEdit?.unit ?: "kg") }
    var priceStr by remember { mutableStateOf(itemToEdit?.pricePerUnit?.toInt()?.toString() ?: "1200") }
    var stockStr by remember { mutableStateOf(itemToEdit?.currentStock?.toString() ?: "10.0") }
    var productionDate by remember { mutableStateOf(itemToEdit?.productionDate ?: todayStr) }
    var expiryDate by remember { mutableStateOf(itemToEdit?.expiryDate ?: defaultExpiryStr) }
    var batchNumber by remember { mutableStateOf(itemToEdit?.batchNumber ?: "BTH-${(1000..9999).random()}") }

    val categories = listOf("Mithai / Sweets", "Bakery & Breads", "Pastries & Cakes", "Snacks & Savories", "Biscuits & Cookies")
    val units = listOf("kg", "piece", "pack", "box")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = DeepAmber)
                Text(
                    text = if (itemToEdit == null) "Add Bakery Product" else "Edit Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name * (e.g. Gulab Jamun, Chicken Bread)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Current Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Unit of Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Production & Expiry Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = productionDate,
                        onValueChange = { productionDate = it },
                        label = { Text("Production Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val price = priceStr.toDoubleOrNull() ?: 0.0
                        val stock = stockStr.toDoubleOrNull() ?: 0.0

                        val item = BakeryItemEntity(
                            id = itemToEdit?.id ?: 0L,
                            businessId = itemToEdit?.businessId ?: 0L,
                            name = name.trim(),
                            category = category,
                            unit = unit,
                            pricePerUnit = price,
                            currentStock = stock,
                            productionDate = productionDate.trim(),
                            expiryDate = expiryDate.trim(),
                            batchNumber = batchNumber.trim()
                        )
                        onSave(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberBakery)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// DIALOG: CAKE PAYMENT SETTLEMENT
// -------------------------------------------------------------
@Composable
private fun CakePaymentDialog(
    order: BakeryCakeOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var advanceStr by remember { mutableStateOf(order.advancePaid.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = GreenReady)
                Text("Cake Payment Settlement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Order: ${order.orderNumber} - ${order.customerName}", fontWeight = FontWeight.SemiBold)
                Text("Total Bill: Rs. ${order.totalPrice.toInt()}", fontSize = 14.sp)
                Text("Current Due: Rs. ${order.remainingBalance.toInt()}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = advanceStr,
                    onValueChange = { advanceStr = it },
                    label = { Text("Total Paid Amount (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { advanceStr = order.totalPrice.toInt().toString() }
                    ) {
                        Text("Mark Full Payment")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPaid = advanceStr.toDoubleOrNull() ?: order.advancePaid
                    val newBalance = (order.totalPrice - newPaid).coerceAtLeast(0.0)
                    onConfirm(newPaid, newBalance)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenReady)
            ) {
                Text("Update Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
