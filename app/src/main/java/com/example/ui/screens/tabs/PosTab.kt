package com.example.ui.screens.tabs

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.data.CustomerEntity
import com.example.data.OrderEntity
import com.example.data.OrderItemEntity
import com.example.data.ProductEntity
import com.example.ui.BusinessCategory
import com.example.ui.BusinessViewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.dialogs.BluetoothPrinterSettingsDialog
import com.example.ui.dialogs.ThermalReceiptPreviewDialog
import com.example.ui.dialogs.ThermalReceiptType
import com.example.ui.resolveBusinessCategory
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTab(
    business: BusinessEntity?,
    modifier: Modifier = Modifier,
    viewModel: BusinessViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    val db = remember(businessId) { AppDatabase.getDatabase(context) }
    val productDao = remember(businessId) { db.productDao() }
    val customerDao = remember(businessId) { db.customerDao() }
    val orderDao = remember(businessId) { db.orderDao() }

    // Live Room DB Products & Customers
    val dbProducts by productDao.getProducts(businessId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val dbCustomers by customerDao.getCustomers(businessId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Cart: Product ID -> Quantity
    val cart = remember { mutableStateMapOf<Long, Int>() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog States
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showBarcodeScannerDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showThermalReceiptDialog by remember { mutableStateOf(false) }
    var showPrinterSettingsDialog by remember { mutableStateOf(false) }

    var lastReceiptText by remember { mutableStateOf("") }
    var lastInvoiceExportData by remember { mutableStateOf<InvoiceExportData?>(null) }

    // Customer & Checkout Form State
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("Cash") }
    var discountPercent by remember { mutableIntStateOf(0) }
    var gstTaxPercent by remember { mutableIntStateOf(0) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val distinctCategories = remember(dbProducts) {
        listOf("All") + dbProducts.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredProducts = dbProducts.filter { item ->
        val matchesCategory = (selectedCategory == "All" || item.category == selectedCategory)
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Cart calculations
    val totalItemsCount = cart.values.sum()
    val subtotalAmount = cart.entries.sumOf { (productId, qty) ->
        val item = dbProducts.find { it.id == productId }
        (item?.salePricePkr ?: 0.0) * qty
    }

    val discountAmount = subtotalAmount * (discountPercent.toDouble() / 100.0)
    val taxableAmount = subtotalAmount - discountAmount
    val taxAmount = taxableAmount * (gstTaxPercent.toDouble() / 100.0)
    val netTotalAmount = (taxableAmount + taxAmount).coerceAtLeast(0.0)

    Box(modifier = modifier.fillMaxSize().testTag("pos_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top POS Toolbar: Search & Barcode Scan
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search catalog or scan barcode...") },
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
                            modifier = Modifier.weight(1f).testTag("pos_search_input")
                        )

                        // Barcode Scan Camera Button
                        IconButton(
                            onClick = { showBarcodeScannerDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(PakEmeraldContainer, RoundedCornerShape(12.dp))
                                .testTag("pos_barcode_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = PakEmeraldPrimary
                            )
                        }

                        // Printer Settings Button
                        IconButton(
                            onClick = { showPrinterSettingsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(PakGoldContainer.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .testTag("pos_printer_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Thermal Printer Settings",
                                tint = PakGoldSecondary
                            )
                        }
                    }

                    // Category Filter Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(distinctCategories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PakEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Products Catalog Grid/List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = if (totalItemsCount > 0) 100.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PointOfSale, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(40.dp))
                                Text("No Products Found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Add products in Inventory tab or search with a different keyword.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                items(filteredProducts, key = { it.id }) { item ->
                    val quantityInCart = cart[item.id] ?: 0
                    val isOutOfStock = item.stockQuantity <= 0.0

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pos_item_${item.id}")
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
                                    text = item.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "• Stock: ${item.stockQuantity.toInt()} ${item.unit}",
                                        fontSize = 11.sp,
                                        color = if (isOutOfStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$currency ${String.format(Locale.getDefault(), "%,.2f", item.salePricePkr)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (quantityInCart > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (quantityInCart > 1) {
                                                cart[item.id] = quantityInCart - 1
                                            } else {
                                                cart.remove(item.id)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PakEmeraldContainer,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$quantityInCart",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = PakEmeraldPrimary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { cart[item.id] = quantityInCart + 1 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = PakEmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { cart[item.id] = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Cart Checkout Bar
        if (totalItemsCount > 0) {
            Card(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .testTag("pos_cart_bar")
            ) {
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
                        Surface(
                            shape = CircleShape,
                            color = PakGoldSecondary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$totalItemsCount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Cart Total ($totalItemsCount items)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$currency ${String.format(Locale.getDefault(), "%,.2f", subtotalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { cart.clear() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Cart", tint = Color.White.copy(alpha = 0.7f))
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_checkout_pos")
                        ) {
                            Text("Checkout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Checkout Dialog with Room Customer & Order persistence
        if (showCheckoutDialog) {
            AlertDialog(
                onDismissRequest = { showCheckoutDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = PakEmeraldPrimary)
                        Text("Finalize Sale Order", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Customer Selection Dropdown
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (selectedCustomerId != null) {
                                    val cust = dbCustomers.find { it.id == selectedCustomerId }
                                    cust?.let { "${it.name} (${it.phone})" } ?: "Walk-in Customer"
                                } else if (customerNameInput.isNotBlank()) {
                                    customerNameInput
                                } else {
                                    "Walk-in Customer (Select from Khata)"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Customer Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Walk-in Customer (Cash)") },
                                    onClick = {
                                        selectedCustomerId = null
                                        customerNameInput = "Walk-in Customer"
                                        customerPhoneInput = ""
                                        customerDropdownExpanded = false
                                    }
                                )
                                dbCustomers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cust.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${cust.phone} • Khata: $currency ${String.format(Locale.getDefault(), "%,.0f", cust.currentBalancePkr)}",
                                                    fontSize = 11.sp,
                                                    color = if (cust.currentBalancePkr > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCustomerId = cust.id
                                            customerNameInput = cust.name
                                            customerPhoneInput = cust.phone
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedCustomerId == null) {
                            OutlinedTextField(
                                value = customerNameInput,
                                onValueChange = { customerNameInput = it },
                                label = { Text("Customer Name (Walk-in)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = customerPhoneInput,
                                onValueChange = { customerPhoneInput = it },
                                label = { Text("WhatsApp Phone (for Receipt)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Payment Mode Selection
                        Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cash", "EasyPaisa", "JazzCash", "Card", "Udhaar").forEach { mode ->
                                val isSelected = selectedPaymentMode == mode
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) (if (mode == "Udhaar") Color(0xFFDC2626) else PakEmeraldPrimary) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPaymentMode = mode }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Discount Selector
                        Text("Discount (Riayat)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0, 5, 10, 15).forEach { pct ->
                                val isSelected = discountPercent == pct
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { discountPercent = pct }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (pct == 0) "0%" else "$pct% Off",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // GST / FBR Sales Tax
                        Text("Sales Tax / GST (FBR)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0 to "0% (Exempt)", 5 to "5% (Services)", 18 to "18% (GST)").forEach { (taxPct, label) ->
                                val isSelected = gstTaxPercent == taxPct
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { gstTaxPercent = taxPct }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Summary Calculation Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PakEmeraldContainer.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal ($totalItemsCount items):", fontSize = 12.sp)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.2f", subtotalAmount)}", fontSize = 12.sp)
                                }
                                if (discountPercent > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Discount ($discountPercent%):", fontSize = 12.sp, color = Color(0xFFD97706))
                                        Text("- $currency ${String.format(Locale.getDefault(), "%,.2f", discountAmount)}", fontSize = 12.sp, color = Color(0xFFD97706))
                                    }
                                }
                                if (gstTaxPercent > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sales Tax ($gstTaxPercent%):", fontSize = 12.sp, color = Color(0xFF0284C7))
                                        Text("+ $currency ${String.format(Locale.getDefault(), "%,.2f", taxAmount)}", fontSize = 12.sp, color = Color(0xFF0284C7))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "$currency ${String.format(Locale.getDefault(), "%,.2f", netTotalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (selectedPaymentMode == "Udhaar") Color(0xFFDC2626) else PakEmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                            val orderNum = "INV-${System.currentTimeMillis() % 1000000}"
                            val custName = if (customerNameInput.isNotBlank()) customerNameInput.trim() else "Walk-in Customer"
                            val custPhone = customerPhoneInput.trim()

                            val itemsLines = cart.entries.joinToString("\n") { (productId, qty) ->
                                val item = dbProducts.find { it.id == productId }
                                "• ${item?.name ?: "Item"} x$qty = $currency ${String.format(Locale.getDefault(), "%,.2f", (item?.salePricePkr ?: 0.0) * qty)}"
                            }

                            lastReceiptText = """
                                ══════════════════════════
                                ${business?.name ?: "BUSINESS STORE"}
                                POS TAX INVOICE • #$orderNum
                                ══════════════════════════
                                Date: $timeStr
                                Customer: $custName
                                Phone: ${if (custPhone.isNotBlank()) custPhone else "N/A"}
                                Payment Mode: $selectedPaymentMode
                                NTN / STRN: ${business?.id ?: 1001}-TAX-PK
                                ──────────────────────────
                                Items Purchased:
                                $itemsLines
                                ──────────────────────────
                                Subtotal: $currency ${String.format(Locale.getDefault(), "%,.2f", subtotalAmount)}
                                Discount: $currency ${String.format(Locale.getDefault(), "%,.2f", discountAmount)} ($discountPercent%)
                                Taxable: $currency ${String.format(Locale.getDefault(), "%,.2f", taxableAmount)}
                                GST Tax ($gstTaxPercent%): $currency ${String.format(Locale.getDefault(), "%,.2f", taxAmount)}
                                NET TOTAL: $currency ${String.format(Locale.getDefault(), "%,.2f", netTotalAmount)}
                                ══════════════════════════
                                Thank you for your business!
                                100% Offline POS by PakBusiness Pro.
                            """.trimIndent()

                            val exportItems = cart.entries.mapNotNull { (productId, qty) ->
                                val item = dbProducts.find { it.id == productId }
                                item?.let {
                                    InvoiceExportItem(
                                        name = it.name,
                                        quantity = qty.toDouble(),
                                        unitPrice = it.salePricePkr,
                                        total = it.salePricePkr * qty
                                    )
                                }
                            }

                            lastInvoiceExportData = InvoiceExportData(
                                invoiceNumber = orderNum,
                                businessName = business?.name ?: "PakBusiness Enterprise",
                                businessAddress = business?.address ?: "Commercial Hub",
                                businessPhone = business?.phone ?: "",
                                customerName = custName,
                                customerPhone = custPhone,
                                items = exportItems,
                                subtotal = subtotalAmount,
                                discount = discountAmount,
                                tax = taxAmount,
                                grandTotal = netTotalAmount,
                                paymentMethod = selectedPaymentMode,
                                footerNote = "Computer Generated Invoice • 100% Offline POS"
                            )

                            // Perform Database Transactions: Decrement Stock, Record Order, Adjust Khata if Udhaar
                            scope.launch(Dispatchers.IO) {
                                // 1. Deduct Product Stock in Room DB
                                cart.forEach { (productId, qty) ->
                                    productDao.adjustStock(productId, -qty.toDouble())
                                }

                                // 2. If Udhaar, add to Customer Khata Balance
                                if (selectedPaymentMode == "Udhaar") {
                                    if (selectedCustomerId != null) {
                                        customerDao.adjustCustomerBalance(selectedCustomerId!!, netTotalAmount)
                                    } else if (custName.isNotBlank() && custName != "Walk-in Customer") {
                                        val newCustId = customerDao.insertCustomer(
                                            CustomerEntity(
                                                businessId = businessId,
                                                name = custName,
                                                phone = custPhone,
                                                openingBalancePkr = 0.0,
                                                currentBalancePkr = netTotalAmount,
                                                creditLimitPkr = 50000.0,
                                                notes = "Created via POS Udhaar sale $orderNum"
                                            )
                                        )
                                    }
                                }

                                // 3. Record Order in Room DB
                                orderDao.insertOrder(
                                    OrderEntity(
                                        businessId = businessId,
                                        orderNumber = orderNum,
                                        partyId = selectedCustomerId,
                                        partyName = custName,
                                        partyPhone = custPhone,
                                        orderType = "SALE",
                                        status = "COMPLETED",
                                        subtotal = subtotalAmount,
                                        discountAmount = discountAmount,
                                        taxAmount = taxAmount,
                                        totalAmount = netTotalAmount,
                                        paidAmount = if (selectedPaymentMode == "Udhaar") 0.0 else netTotalAmount,
                                        balanceAmount = if (selectedPaymentMode == "Udhaar") netTotalAmount else 0.0,
                                        paymentMode = selectedPaymentMode.uppercase(),
                                        paymentStatus = if (selectedPaymentMode == "Udhaar") "UNPAID" else "PAID",
                                        itemsCount = totalItemsCount,
                                        notes = "POS Checkout Order"
                                    )
                                )
                            }

                            showCheckoutDialog = false
                            cart.clear()
                            showSuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.testTag("btn_confirm_checkout_order")
                    ) {
                        Text("Confirm & Bill")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Sale Success & Receipt Actions Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PakEmeraldPrimary)
                        Text("Sale Completed Successfully!", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Inventory stock updated & order recorded in local database.", fontSize = 13.sp)
                        if (selectedPaymentMode == "Udhaar") {
                            Text("Udhaar balance has been added to customer khata ledger.", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                        Text("Choose an option to share or print the invoice receipt:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            showThermalReceiptDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.testTag("btn_preview_thermal_receipt")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thermal Print")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        lastInvoiceExportData?.let { data ->
                            OutlinedButton(
                                onClick = {
                                    PdfExportUtil.shareInvoice(context, data)
                                    showSuccessDialog = false
                                }
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF Share", fontSize = 11.sp)
                            }
                        }

                        TextButton(onClick = { showSuccessDialog = false }) {
                            Text("Done")
                        }
                    }
                }
            )
        }

        // Thermal Receipt Preview Dialog (ESC/POS 58mm/80mm)
        if (showThermalReceiptDialog && lastInvoiceExportData != null) {
            ThermalReceiptPreviewDialog(
                receiptType = ThermalReceiptType.PosInvoice(
                    invoice = lastInvoiceExportData!!,
                    cashierName = "Cashier"
                ),
                onDismiss = { showThermalReceiptDialog = false }
            )
        }

        // Bluetooth Printer Setup Dialog
        if (showPrinterSettingsDialog) {
            BluetoothPrinterSettingsDialog(
                onDismiss = { showPrinterSettingsDialog = false }
            )
        }

        // Barcode Scanner Dialog
        if (showBarcodeScannerDialog) {
            BarcodeScannerDialog(
                onDismiss = { showBarcodeScannerDialog = false },
                onBarcodeScanned = { scannedCode ->
                    showBarcodeScannerDialog = false
                    searchQuery = scannedCode
                    val matchedItem = dbProducts.find { it.barcode.equals(scannedCode, ignoreCase = true) || it.sku.equals(scannedCode, ignoreCase = true) }
                    if (matchedItem != null) {
                        cart[matchedItem.id] = (cart[matchedItem.id] ?: 0) + 1
                    }
                }
            )
        }
    }
}
