package com.example.ui.screens.restaurant

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.ui.dialogs.ThermalReceiptPreviewDialog
import com.example.ui.dialogs.ThermalReceiptType
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.KotPrintItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.components.BarcodeScannerDialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RestaurantMenuItemEntity
import com.example.data.RestaurantOrderEntity
import com.example.data.RestaurantOrderItemEntity
import com.example.data.RestaurantTableEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingPosView(
    allTables: List<RestaurantTableEntity>,
    selectedTable: RestaurantTableEntity?,
    existingOrder: RestaurantOrderEntity?,
    existingOrderItems: List<RestaurantOrderItemEntity>,
    menuItems: List<RestaurantMenuItemEntity>,
    onSelectTable: (RestaurantTableEntity) -> Unit,
    onSaveAndSendToKitchen: (order: RestaurantOrderEntity, items: List<RestaurantOrderItemEntity>) -> Unit,
    onSettleOrder: (orderId: Long, tableId: Long, paymentMethod: String) -> Unit,
    onCancelOrder: (orderId: Long, tableId: Long) -> Unit,
    onBackToTables: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current in-memory cart items
    val cartItems = remember { mutableStateListOf<RestaurantOrderItemEntity>() }

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var guestCount by remember { mutableIntStateOf(2) }
    var orderNotes by remember { mutableStateOf("") }

    var taxPercent by remember { mutableDoubleStateOf(5.0) } // Default 5% GST
    var discountPkrText by remember { mutableStateOf("0") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var showInstructionDialog by remember { mutableStateOf(false) }
    var itemForInstructionIndex by remember { mutableIntStateOf(-1) }

    var showSettleDialog by remember { mutableStateOf(false) }
    var showTablePickerSheet by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showThermalKotDialog by remember { mutableStateOf(false) }
    var showThermalBillDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun addProductByBarcodeOrCode(code: String) {
        val trimmed = code.trim()
        val matchedItem = menuItems.find { item ->
            item.id.toString() == trimmed ||
            item.name.equals(trimmed, ignoreCase = true) ||
            item.name.contains(trimmed, ignoreCase = true) ||
            trimmed.contains(item.name, ignoreCase = true)
        } ?: menuItems.firstOrNull()

        if (matchedItem != null) {
            val existingIndex = cartItems.indexOfFirst { it.menuItemId == matchedItem.id }
            if (existingIndex >= 0) {
                val current = cartItems[existingIndex]
                cartItems[existingIndex] = current.copy(quantity = current.quantity + 1)
            } else {
                val tbl = selectedTable ?: allTables.firstOrNull()
                cartItems.add(
                    RestaurantOrderItemEntity(
                        orderId = existingOrder?.id ?: 0L,
                        businessId = tbl?.businessId ?: 0L,
                        tableId = tbl?.id ?: 0L,
                        tableName = tbl?.name ?: "Takeaway",
                        menuItemId = matchedItem.id,
                        itemName = matchedItem.name,
                        itemCategory = matchedItem.category,
                        unitPricePkr = matchedItem.pricePkr,
                        quantity = 1,
                        instructions = "",
                        kotStatus = "PENDING"
                    )
                )
            }
            Toast.makeText(context, "Added ${matchedItem.name} (₨ ${matchedItem.pricePkr.toInt()}) to Cart!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No dish matching barcode '$trimmed'", Toast.LENGTH_SHORT).show()
        }
    }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
    }

    // Sync when existing order or table changes
    LaunchedEffect(selectedTable?.id, existingOrder?.id, existingOrderItems) {
        cartItems.clear()
        if (existingOrderItems.isNotEmpty()) {
            cartItems.addAll(existingOrderItems)
        }
        if (existingOrder != null) {
            customerName = existingOrder.customerName
            customerPhone = existingOrder.customerPhone
            guestCount = existingOrder.guestCount
            orderNotes = existingOrder.notes
            taxPercent = existingOrder.taxPercent
            discountPkrText = existingOrder.discountPkr.toInt().toString()
        } else {
            customerName = selectedTable?.reservedFor ?: ""
            customerPhone = selectedTable?.reservedPhone ?: ""
            guestCount = selectedTable?.capacity ?: 2
            orderNotes = ""
            discountPkrText = "0"
        }
    }

    // Live Calculations
    val subtotalPkr = cartItems.sumOf { it.unitPricePkr * it.quantity }
    val taxPkr = (subtotalPkr * taxPercent) / 100.0
    val discountPkr = discountPkrText.toDoubleOrNull() ?: 0.0
    val totalPkr = maxOf(0.0, subtotalPkr + taxPkr - discountPkr)

    val categories = listOf("All") + menuItems.map { it.category }.distinct()

    val filteredMenu = menuItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Table Header Banner
        Surface(
            color = PakEmeraldDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackToTables,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedTable?.name ?: "No Table Selected",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedTable != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PakGoldSecondary
                                ) {
                                    Text(
                                        text = selectedTable.section,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (existingOrder != null) "Order #${existingOrder.orderNumber} • ${cartItems.size} items" else "New Table Order",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Switch Table button
                OutlinedButton(
                    onClick = { showTablePickerSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Change Table", fontSize = 11.sp)
                }
            }
        }

        // Main Layout: Split Screen / Tabbed POS
        var activePosTab by remember { mutableIntStateOf(0) } // 0 = Menu & Add, 1 = Cart & Settle

        TabRow(
            selectedTabIndex = activePosTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PakEmeraldPrimary
        ) {
            Tab(
                selected = activePosTab == 0,
                onClick = { activePosTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("1. Select Dishes", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activePosTab == 1,
                onClick = { activePosTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("2. Table Cart (${cartItems.sumOf { it.quantity }})", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (activePosTab == 0) {
            // --- POS Tab 0: Menu Catalog ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Search bar & CameraX Barcode Scanner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search dishes or scan barcode...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PakEmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_search_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PakEmeraldPrimary,
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { showBarcodeScanner = true }
                            .testTag("btn_restaurant_barcode_scanner")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode with Camera",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Category Chips
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                }

                // Menu items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("pos_menu_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMenu, key = { it.id }) { menuItem ->
                        val inCartQuantity = cartItems.filter { it.menuItemId == menuItem.id }.sumOf { it.quantity }

                        PosMenuItemRow(
                            item = menuItem,
                            inCartQuantity = inCartQuantity,
                            currencyFormatter = currencyFormatter,
                            onAddToCart = {
                                if (menuItem.isAvailable && selectedTable != null) {
                                    val existingIndex = cartItems.indexOfFirst { it.menuItemId == menuItem.id }
                                    if (existingIndex >= 0) {
                                        val cur = cartItems[existingIndex]
                                        cartItems[existingIndex] = cur.copy(quantity = cur.quantity + 1)
                                    } else {
                                        cartItems.add(
                                            RestaurantOrderItemEntity(
                                                orderId = existingOrder?.id ?: 0L,
                                                businessId = selectedTable.businessId,
                                                tableId = selectedTable.id,
                                                tableName = selectedTable.name,
                                                menuItemId = menuItem.id,
                                                itemName = menuItem.name,
                                                itemCategory = menuItem.category,
                                                unitPricePkr = menuItem.pricePkr,
                                                quantity = 1,
                                                instructions = "",
                                                kotStatus = "PENDING"
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Bottom Cart Quick Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PakEmeraldPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .clickable { activePosTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${cartItems.sumOf { it.quantity }} items selected",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Subtotal: Rs. ${currencyFormatter.format(subtotalPkr)}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "View Cart & Settle →",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // --- POS Tab 1: Current Order Cart, Cooking Notes & Billing ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Guest & Customer info card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Guest Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Guests:", fontSize = 12.sp)
                                IconButton(
                                    onClick = { if (guestCount > 1) guestCount-- },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "$guestCount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { guestCount++ },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer Name (Optional)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Order items in cart
                Text(
                    text = "ORDER ITEMS (${cartItems.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (cartItems.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text("No items added to this table yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { activePosTab = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                            ) {
                                Text("+ Select Dishes from Menu")
                            }
                        }
                    }
                } else {
                    cartItems.forEachIndexed { index, item ->
                        CartItemRow(
                            item = item,
                            currencyFormatter = currencyFormatter,
                            onIncrement = {
                                cartItems[index] = item.copy(quantity = item.quantity + 1)
                            },
                            onDecrement = {
                                if (item.quantity > 1) {
                                    cartItems[index] = item.copy(quantity = item.quantity - 1)
                                } else {
                                    cartItems.removeAt(index)
                                }
                            },
                            onDelete = {
                                cartItems.removeAt(index)
                            },
                            onEditInstruction = {
                                itemForInstructionIndex = index
                                showInstructionDialog = true
                            }
                        )
                    }
                }

                // Billing Summary Box
                if (cartItems.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("BILLING SUMMARY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", fontSize = 13.sp)
                                Text("Rs. ${currencyFormatter.format(subtotalPkr)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Tax Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("GST / Tax:", fontSize = 12.sp)
                                    listOf(0.0, 5.0, 16.0).forEach { rate ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (taxPercent == rate) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { taxPercent = rate }
                                        ) {
                                            Text(
                                                text = "${rate.toInt()}%",
                                                fontSize = 10.sp,
                                                fontWeight = if (taxPercent == rate) FontWeight.Bold else FontWeight.Normal,
                                                color = if (taxPercent == rate) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text("Rs. ${currencyFormatter.format(taxPkr)}", fontSize = 13.sp)
                            }

                            // Discount field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Discount (PKR):", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = discountPkrText,
                                    onValueChange = { discountPkrText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.width(100.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Rs. ${currencyFormatter.format(totalPkr)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary
                                )
                            }
                        }
                    }

                    // Action Buttons: Send to Kitchen & Settle Bill
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Send to Kitchen (KOT) Button
                        Button(
                            onClick = {
                                if (selectedTable != null && cartItems.isNotEmpty()) {
                                    val order = RestaurantOrderEntity(
                                        id = existingOrder?.id ?: 0L,
                                        businessId = selectedTable.businessId,
                                        tableId = selectedTable.id,
                                        tableName = selectedTable.name,
                                        orderNumber = existingOrder?.orderNumber ?: 0,
                                        status = "KITCHEN",
                                        customerName = customerName.trim(),
                                        customerPhone = customerPhone.trim(),
                                        guestCount = guestCount,
                                        subtotalPkr = subtotalPkr,
                                        taxPercent = taxPercent,
                                        taxPkr = taxPkr,
                                        discountPkr = discountPkr,
                                        totalPkr = totalPkr,
                                        paymentMethod = existingOrder?.paymentMethod ?: "UNPAID",
                                        notes = orderNotes.trim(),
                                        createdAt = existingOrder?.createdAt ?: System.currentTimeMillis()
                                    )
                                    onSaveAndSendToKitchen(order, cartItems.toList())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_send_to_kitchen")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.SoupKitchen, contentDescription = null)
                                Text("Send to Kitchen (KOT)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Print Thermal KOT Ticket Preview Button
                        OutlinedButton(
                            onClick = { showThermalKotDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_print_thermal_kot")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = PakGoldSecondary)
                                Text("Print Kitchen Ticket (Bluetooth KOT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PakGoldSecondary)
                            }
                        }

                        // Settle & Print Bill Button
                        Button(
                            onClick = { showSettleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_settle_order")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Text("Settle & Complete Bill", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Cancel order
                        if (existingOrder != null) {
                            TextButton(
                                onClick = {
                                    if (selectedTable != null) {
                                        onCancelOrder(existingOrder.id, selectedTable.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Order & Free Table", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Special Cooking Instruction Dialog
    if (showInstructionDialog && itemForInstructionIndex in cartItems.indices) {
        val currentItem = cartItems[itemForInstructionIndex]
        var instructionText by remember { mutableStateOf(currentItem.instructions) }

        val commonSuggestions = listOf("Extra Spicy 🌶️", "Mild / Less Spicy", "No Onions", "Extra Chutney", "Separately Packed", "Crispy & Hot", "Less Sugar")

        AlertDialog(
            onDismissRequest = { showInstructionDialog = false },
            title = {
                Text("Kitchen Note for ${currentItem.itemName}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = instructionText,
                        onValueChange = { instructionText = it },
                        label = { Text("Chef Instructions") },
                        placeholder = { Text("e.g. Extra raita, no green chilies") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Quick Suggestions:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        commonSuggestions.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    instructionText = if (instructionText.isBlank()) suggestion else "$instructionText, $suggestion"
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cartItems[itemForInstructionIndex] = currentItem.copy(instructions = instructionText.trim())
                        showInstructionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Payment Settlement Dialog
    if (showSettleDialog && selectedTable != null) {
        var paymentMethod by remember { mutableStateOf("CASH") }

        AlertDialog(
            onDismissRequest = { showSettleDialog = false },
            title = {
                Text("Settle Bill • ${selectedTable.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PakEmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Total Payable (PKR)", fontSize = 11.sp, color = PakEmeraldPrimary)
                            Text(
                                text = "Rs. ${currencyFormatter.format(totalPkr)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary
                            )
                        }
                    }

                    Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    listOf("CASH", "CARD", "JAZZCASH", "EASYPAISA").forEach { method ->
                        val isSelected = paymentMethod == method
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PakEmeraldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = method }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (method) {
                                        "CASH" -> "Cash in PKR"
                                        "CARD" -> "Debit / Credit Card"
                                        "JAZZCASH" -> "JazzCash Mobile Account"
                                        "EASYPAISA" -> "EasyPaisa Mobile Account"
                                        else -> method
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showThermalBillDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_print_thermal_settle_bill")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = PakEmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Bill", fontSize = 12.sp, color = PakEmeraldPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val orderId = existingOrder?.id ?: 0L
                            onSettleOrder(orderId, selectedTable.id, paymentMethod)
                            showSettleDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Settle & Free", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Switch Table Dialog
    if (showTablePickerSheet) {
        AlertDialog(
            onDismissRequest = { showTablePickerSheet = false },
            title = {
                Text("Select Restaurant Table", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allTables) { table ->
                        val isCurrent = table.id == selectedTable?.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTable(table)
                                    showTablePickerSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(table.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${table.section} • ${table.capacity} Seats", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (table.status) {
                                        "OCCUPIED" -> Color(0xFFFFEBEE)
                                        "RESERVED" -> Color(0xFFF3E5F5)
                                        else -> Color(0xFFE8F5E9)
                                    }
                                ) {
                                    Text(
                                        text = table.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (table.status) {
                                            "OCCUPIED" -> Color(0xFFC62828)
                                            "RESERVED" -> Color(0xFF6A1B9A)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTablePickerSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    // CameraX Barcode & QR Scanner Dialog for rapid cart adding
    if (showBarcodeScanner) {
        val sampleDishBarcodes = remember(menuItems) {
            menuItems.take(5).map { it.name }
        }

        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                addProductByBarcodeOrCode(barcode)
            },
            onDismiss = { showBarcodeScanner = false },
            sampleBarcodes = sampleDishBarcodes
        )
    }

    // Thermal KOT Ticket Preview & Bluetooth Print Dialog
    if (showThermalKotDialog && selectedTable != null) {
        val printItems = cartItems.map {
            KotPrintItem(
                name = it.itemName,
                quantity = it.quantity,
                instructions = it.instructions
            )
        }
        ThermalReceiptPreviewDialog(
            receiptType = ThermalReceiptType.KitchenKot(
                tableName = selectedTable.name,
                orderId = existingOrder?.id ?: (System.currentTimeMillis() % 10000),
                items = printItems,
                guestCount = guestCount,
                serverName = "Captain / Waiter",
                orderNotes = orderNotes
            ),
            onDismiss = { showThermalKotDialog = false }
        )
    }

    // Thermal Customer Bill Preview & Bluetooth Print Dialog
    if (showThermalBillDialog && selectedTable != null) {
        val invoiceData = InvoiceExportData(
            invoiceNumber = "DINE-${selectedTable.name}-${existingOrder?.id ?: (System.currentTimeMillis() % 10000)}",
            customerName = if (customerName.isNotBlank()) customerName else "Table ${selectedTable.name} Guest",
            customerPhone = customerPhone,
            paymentMethod = existingOrder?.paymentMethod ?: "Cash",
            items = cartItems.map {
                InvoiceExportItem(
                    name = it.itemName,
                    quantity = it.quantity.toDouble(),
                    unitPrice = it.unitPricePkr,
                    total = it.quantity * it.unitPricePkr
                )
            },
            subtotal = subtotalPkr,
            tax = taxPkr,
            discount = discountPkr,
            grandTotal = totalPkr
        )
        ThermalReceiptPreviewDialog(
            receiptType = ThermalReceiptType.PosInvoice(invoiceData, cashierName = "Restaurant Cashier"),
            onDismiss = { showThermalBillDialog = false }
        )
    }
}

@Composable
fun PosMenuItemRow(
    item: RestaurantMenuItemEntity,
    inCartQuantity: Int,
    currencyFormatter: NumberFormat,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.isAvailable, onClick = onAddToCart)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.category} • ${item.prepTimeMinutes}m prep",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rs. ${currencyFormatter.format(item.pricePkr)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PakEmeraldPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (!item.isAvailable) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "SOLD OUT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (inCartQuantity > 0) {
                        Surface(
                            shape = CircleShape,
                            color = PakEmeraldPrimary
                        ) {
                            Text(
                                text = "$inCartQuantity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldContainer),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("+ Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: RestaurantOrderItemEntity,
    currencyFormatter: NumberFormat,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onEditInstruction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rs. ${currencyFormatter.format(item.unitPricePkr)} each",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quantity stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(28.dp)
                            .background(PakEmeraldContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = PakEmeraldPrimary, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "Rs. ${currencyFormatter.format(item.lineTotalPkr)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Instructions & Cooking Notes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.instructions.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onEditInstruction)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Note:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text(item.instructions, fontSize = 10.sp, color = Color(0xFFE65100), maxLines = 1)
                        }
                    }
                } else {
                    TextButton(
                        onClick = onEditInstruction,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = PakEmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Cooking Note", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
