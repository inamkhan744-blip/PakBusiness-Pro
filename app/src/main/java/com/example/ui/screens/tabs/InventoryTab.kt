package com.example.ui.screens.tabs

import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.ProductEntity
import com.example.ui.BusinessCategory
import com.example.ui.BusinessViewModel
import com.example.ui.components.BarcodeScannerDialog
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
import java.util.Locale

@Composable
fun InventoryTab(
    business: BusinessEntity?,
    modifier: Modifier = Modifier,
    viewModel: BusinessViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    val productDao = remember(businessId) {
        AppDatabase.getDatabase(context).productDao()
    }

    // Live Room DB Products list
    val dbProducts by productDao.getProducts(businessId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Auto-seed initial catalog into Room DB if empty for this business
    LaunchedEffect(businessId, dbProducts.size) {
        if (dbProducts.isEmpty()) {
            withContext(Dispatchers.IO) {
                val existingCount = productDao.getProductCount(businessId)
                if (existingCount == 0) {
                    val seeds = getInitialProductsForBusiness(businessId, business?.type)
                    productDao.insertProducts(seeds)
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Dialog States
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showBarcodeScannerDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var productForStockAdjust by remember { mutableStateOf<ProductEntity?>(null) }

    // Form inputs for Add/Edit Product
    var formName by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("General") }
    var formSku by remember { mutableStateOf("") }
    var formBarcode by remember { mutableStateOf("") }
    var formUnit by remember { mutableStateOf("Pcs") }
    var formCostPrice by remember { mutableStateOf("") }
    var formSalePrice by remember { mutableStateOf("") }
    var formStockQty by remember { mutableStateOf("") }
    var formMinThreshold by remember { mutableStateOf("10") }

    // Custom stock adjustment input
    var stockDeltaInput by remember { mutableStateOf("") }
    var stockAdjustReason by remember { mutableStateOf("Purchase Arrival") }

    val distinctCategories = remember(dbProducts) {
        listOf("All") + dbProducts.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredProducts = dbProducts.filter { item ->
        val matchesQuery = item.name.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)

        val matchesLowStock = if (filterLowStockOnly) item.stockQuantity <= item.minStockThreshold else true
        val matchesCategory = if (selectedCategoryFilter != "All") item.category == selectedCategoryFilter else true

        matchesQuery && matchesLowStock && matchesCategory
    }

    val totalValuation = dbProducts.sumOf { it.stockQuantity * it.salePricePkr }
    val totalCost = dbProducts.sumOf { it.stockQuantity * it.costPricePkr }
    val estimatedProfit = (totalValuation - totalCost).coerceAtLeast(0.0)
    val lowStockCount = dbProducts.count { it.stockQuantity <= it.minStockThreshold }

    fun exportStockReportPdf(isPrint: Boolean) {
        val invoiceItems = dbProducts.map { item ->
            InvoiceExportItem(
                name = "${item.name} [SKU: ${item.sku.ifBlank { "N/A" }} | ${item.category}]",
                quantity = item.stockQuantity,
                unitPrice = item.salePricePkr,
                total = item.stockQuantity * item.salePricePkr
            )
        }
        val exportData = InvoiceExportData(
            invoiceNumber = "STOCK-${System.currentTimeMillis() % 100000}",
            businessName = business?.name ?: "PakBusiness Enterprise",
            businessAddress = business?.address ?: "Commercial Hub",
            businessPhone = business?.phone ?: "",
            customerName = "OFFICIAL INVENTORY AUDIT & STOCK VALUATION REPORT",
            customerPhone = "Total SKUs: ${dbProducts.size}",
            items = invoiceItems,
            subtotal = totalValuation,
            discount = 0.0,
            tax = 0.0,
            grandTotal = totalValuation,
            paymentMethod = "Total Valuation ($currency)",
            footerNote = "Certified Stock Audit Report • Potential Gross Profit: $currency ${String.format(Locale.getDefault(), "%,.0f", estimatedProfit)}"
        )
        if (isPrint) {
            PdfExportUtil.printInvoice(context, exportData)
        } else {
            PdfExportUtil.shareInvoice(context, exportData)
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("inventory_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Valuation and Stock Health Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Total Inventory Valuation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PakEmeraldPrimary
                            ) {
                                Text(
                                    text = "${dbProducts.size} Registered SKUs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%,.2f", totalValuation)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Cost Value (Khareed)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalCost)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Expected Munafa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", estimatedProfit)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                            }
                            Column {
                                Text("Low Stock Alert", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$lowStockCount Items", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (lowStockCount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Export & Print Stock Valuation PDF Buttons
                        if (dbProducts.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { exportStockReportPdf(isPrint = true) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_print_stock_report")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = PakEmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print PDF Report", fontSize = 11.sp, color = PakEmeraldPrimary)
                                }

                                Button(
                                    onClick = { exportStockReportPdf(isPrint = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_share_stock_report")
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share PDF Audit", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar, Barcode Scanner, & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search product name, SKU, or barcode...") },
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
                            modifier = Modifier.weight(1f).testTag("search_inventory_input")
                        )

                        // Barcode Scan Button
                        IconButton(
                            onClick = { showBarcodeScannerDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(PakEmeraldContainer, RoundedCornerShape(12.dp))
                                .testTag("btn_scan_barcode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = PakEmeraldPrimary
                            )
                        }
                    }

                    // Category & Low Stock Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filterLowStockOnly,
                                onClick = { filterLowStockOnly = !filterLowStockOnly },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (filterLowStockOnly) Color.White else Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = { Text("Low Stock ($lowStockCount)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDC2626),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        items(distinctCategories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PakEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(40.dp))
                            Text("No Products Found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap '+' below to add new inventory products to local database.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(filteredProducts, key = { it.id }) { item ->
                val isLow = item.stockQuantity <= item.minStockThreshold
                val marginPct = if (item.costPricePkr > 0.0) {
                    ((item.salePricePkr - item.costPricePkr) / item.costPricePkr * 100).toInt()
                } else 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("inventory_item_${item.id}")
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = item.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (item.sku.isNotBlank()) {
                                        Text(
                                            text = "SKU: ${item.sku}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.barcode.isNotBlank()) {
                                        Text(
                                            text = "• Barcode: ${item.barcode}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLow) Color(0xFFFFEBEE) else PakEmeraldContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    if (isLow) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = "${item.stockQuantity.toInt()} ${item.unit}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isLow) Color(0xFFC62828) else PakEmeraldPrimary
                                    )
                                }
                            }
                        }

                        // Price and Margin Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Column {
                                    Text("Sale Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$currency ${String.format(Locale.getDefault(), "%,.2f", item.salePricePkr)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PakEmeraldPrimary
                                    )
                                }
                                Column {
                                    Text("Cost Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$currency ${String.format(Locale.getDefault(), "%,.2f", item.costPricePkr)}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column {
                                    Text("Profit Margin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "+$marginPct%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }

                            // Quick Adjust Stock Buttons (- / +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (item.stockQuantity > 0) {
                                            scope.launch(Dispatchers.IO) {
                                                productDao.adjustStock(item.id, -1.0)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease 1", tint = MaterialTheme.colorScheme.error)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        productForStockAdjust = item
                                        stockDeltaInput = "10"
                                        stockAdjustReason = "Stock Arrival"
                                    }
                                ) {
                                    Text(
                                        text = "${item.stockQuantity.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            productDao.adjustStock(item.id, 1.0)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase 1", tint = PakEmeraldPrimary)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // Secondary Action Buttons: Edit & Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Min Alert: ${item.minStockThreshold.toInt()} ${item.unit}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        productToEdit = item
                                        formName = item.name
                                        formCategory = item.category
                                        formSku = item.sku
                                        formBarcode = item.barcode
                                        formUnit = item.unit
                                        formCostPrice = item.costPricePkr.toInt().toString()
                                        formSalePrice = item.salePricePkr.toInt().toString()
                                        formStockQty = item.stockQuantity.toInt().toString()
                                        formMinThreshold = item.minStockThreshold.toInt().toString()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { productToDelete = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Product",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Product FAB
        FloatingActionButton(
            onClick = {
                formName = ""
                formCategory = "General"
                formSku = "SKU-${System.currentTimeMillis() % 10000}"
                formBarcode = ""
                formUnit = "Pcs"
                formCostPrice = ""
                formSalePrice = ""
                formStockQty = "20"
                formMinThreshold = "5"
                showAddProductDialog = true
            },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("btn_add_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }

        // Add Product Dialog
        if (showAddProductDialog) {
            AlertDialog(
                onDismissRequest = { showAddProductDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = PakEmeraldPrimary)
                        Text("Add Product to Inventory", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Product Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_product_name")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formCategory,
                                onValueChange = { formCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = formUnit,
                                onValueChange = { formUnit = it },
                                label = { Text("Unit (Pcs/Kg)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formSku,
                                onValueChange = { formSku = it },
                                label = { Text("SKU Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formBarcode,
                                onValueChange = { formBarcode = it },
                                label = { Text("Barcode") },
                                trailingIcon = {
                                    IconButton(onClick = { showBarcodeScannerDialog = true }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = PakEmeraldPrimary)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_product_barcode")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formCostPrice,
                                onValueChange = { formCostPrice = it },
                                label = { Text("Cost Price ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formSalePrice,
                                onValueChange = { formSalePrice = it },
                                label = { Text("Sale Price ($currency) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_product_price")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formStockQty,
                                onValueChange = { formStockQty = it },
                                label = { Text("Initial Stock") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_product_stock")
                            )
                            OutlinedTextField(
                                value = formMinThreshold,
                                onValueChange = { formMinThreshold = it },
                                label = { Text("Min Threshold") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formName.isNotBlank()) {
                                val cost = formCostPrice.toDoubleOrNull() ?: 0.0
                                val sale = formSalePrice.toDoubleOrNull() ?: 0.0
                                val qty = formStockQty.toDoubleOrNull() ?: 0.0
                                val threshold = formMinThreshold.toDoubleOrNull() ?: 10.0

                                scope.launch(Dispatchers.IO) {
                                    productDao.insertProduct(
                                        ProductEntity(
                                            businessId = businessId,
                                            name = formName.trim(),
                                            category = formCategory.trim().ifBlank { "General" },
                                            sku = formSku.trim(),
                                            barcode = formBarcode.trim(),
                                            unit = formUnit.trim().ifBlank { "Pcs" },
                                            costPricePkr = cost,
                                            salePricePkr = sale,
                                            stockQuantity = qty,
                                            minStockThreshold = threshold
                                        )
                                    )
                                }
                                showAddProductDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.testTag("btn_confirm_add_product")
                    ) {
                        Text("Save Product")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddProductDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Product Dialog
        if (productToEdit != null) {
            val prod = productToEdit!!
            AlertDialog(
                onDismissRequest = { productToEdit = null },
                title = { Text("Edit Product Details", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formCostPrice,
                                onValueChange = { formCostPrice = it },
                                label = { Text("Cost Price ($currency)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formSalePrice,
                                onValueChange = { formSalePrice = it },
                                label = { Text("Sale Price ($currency)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formStockQty,
                                onValueChange = { formStockQty = it },
                                label = { Text("Stock Quantity") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formMinThreshold,
                                onValueChange = { formMinThreshold = it },
                                label = { Text("Min Threshold") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cost = formCostPrice.toDoubleOrNull() ?: prod.costPricePkr
                            val sale = formSalePrice.toDoubleOrNull() ?: prod.salePricePkr
                            val qty = formStockQty.toDoubleOrNull() ?: prod.stockQuantity
                            val threshold = formMinThreshold.toDoubleOrNull() ?: prod.minStockThreshold

                            scope.launch(Dispatchers.IO) {
                                productDao.updateProduct(
                                    prod.copy(
                                        name = formName.trim().ifBlank { prod.name },
                                        costPricePkr = cost,
                                        salePricePkr = sale,
                                        stockQuantity = qty,
                                        minStockThreshold = threshold,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            productToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToEdit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Stock Adjustment Dialog
        if (productForStockAdjust != null) {
            val prod = productForStockAdjust!!
            AlertDialog(
                onDismissRequest = { productForStockAdjust = null },
                title = { Text("Adjust Stock: ${prod.name}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Stock: ${prod.stockQuantity.toInt()} ${prod.unit}", fontSize = 13.sp)
                        OutlinedTextField(
                            value = stockDeltaInput,
                            onValueChange = { stockDeltaInput = it },
                            label = { Text("Units to Add (+) or Deduct (-)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = stockAdjustReason,
                            onValueChange = { stockAdjustReason = it },
                            label = { Text("Adjustment Reason (e.g. Purchase, Damage)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val delta = stockDeltaInput.toDoubleOrNull() ?: 0.0
                            if (delta != 0.0) {
                                scope.launch(Dispatchers.IO) {
                                    productDao.adjustStock(prod.id, delta)
                                }
                                productForStockAdjust = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Text("Apply Adjustment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productForStockAdjust = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Product Dialog
        if (productToDelete != null) {
            val prod = productToDelete!!
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to remove '${prod.name}' from inventory? This cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                productDao.deleteProduct(prod)
                            }
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Barcode Scanner Dialog
        if (showBarcodeScannerDialog) {
            BarcodeScannerDialog(
                onDismiss = { showBarcodeScannerDialog = false },
                onBarcodeScanned = { scannedCode ->
                    showBarcodeScannerDialog = false
                    searchQuery = scannedCode
                    formBarcode = scannedCode
                }
            )
        }
    }
}

private fun getInitialProductsForBusiness(businessId: Long, businessType: String?): List<ProductEntity> {
    val category = resolveBusinessCategory(businessType)
    return when (category) {
        BusinessCategory.PHARMACY -> listOf(
            ProductEntity(businessId = businessId, name = "Panadol 500mg (Box of 200)", category = "Analgesics", sku = "MED-PAN-01", barcode = "896400011221", costPricePkr = 750.0, salePricePkr = 900.0, stockQuantity = 120.0, minStockThreshold = 20.0, unit = "Box"),
            ProductEntity(businessId = businessId, name = "Augmentin 625mg (Box of 14)", category = "Antibiotics", sku = "MED-AUG-02", barcode = "896400011222", costPricePkr = 320.0, salePricePkr = 390.0, stockQuantity = 45.0, minStockThreshold = 15.0, unit = "Box"),
            ProductEntity(businessId = businessId, name = "Brufen 400mg Strips", category = "Anti-Inflammatory", sku = "MED-BRU-03", barcode = "896400011223", costPricePkr = 50.0, salePricePkr = 65.0, stockQuantity = 8.0, minStockThreshold = 15.0, unit = "Strips"),
            ProductEntity(businessId = businessId, name = "Surbex Z Multivitamins 30s", category = "Supplements", sku = "MED-SRB-04", barcode = "896400011224", costPricePkr = 490.0, salePricePkr = 580.0, stockQuantity = 25.0, minStockThreshold = 10.0, unit = "Bottle"),
            ProductEntity(businessId = businessId, name = "Omeprazole 20mg Capsules", category = "Antacids", sku = "MED-OMP-05", barcode = "896400011225", costPricePkr = 110.0, salePricePkr = 140.0, stockQuantity = 5.0, minStockThreshold = 10.0, unit = "Box")
        )
        BusinessCategory.RESTAURANT -> listOf(
            ProductEntity(businessId = businessId, name = "Basmati Biryani Rice (50kg Bag)", category = "Grains", sku = "ING-RICE-01", barcode = "896400021001", costPricePkr = 13000.0, salePricePkr = 14500.0, stockQuantity = 14.0, minStockThreshold = 5.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Fresh Broiler Chicken (kg)", category = "Poultry", sku = "ING-CHK-02", barcode = "896400021002", costPricePkr = 540.0, salePricePkr = 620.0, stockQuantity = 42.0, minStockThreshold = 20.0, unit = "Kg"),
            ProductEntity(businessId = businessId, name = "Cooking Oil Master Tin (16L)", category = "Oil & Ghee", sku = "ING-OIL-03", barcode = "896400021003", costPricePkr = 7600.0, salePricePkr = 8200.0, stockQuantity = 6.0, minStockThreshold = 4.0, unit = "Tin"),
            ProductEntity(businessId = businessId, name = "Biryani & Karahi Masala Mix", category = "Spices", sku = "ING-MSL-04", barcode = "896400021004", costPricePkr = 360.0, salePricePkr = 450.0, stockQuantity = 18.0, minStockThreshold = 10.0, unit = "Pack"),
            ProductEntity(businessId = businessId, name = "Special Tea Leaves (10kg Sack)", category = "Beverages", sku = "ING-TEA-05", barcode = "896400021005", costPricePkr = 10500.0, salePricePkr = 12000.0, stockQuantity = 3.0, minStockThreshold = 2.0, unit = "Sack")
        )
        BusinessCategory.BAKERY -> listOf(
            ProductEntity(businessId = businessId, name = "Fine Cake Flour (Maida 50kg)", category = "Flour", sku = "RAW-FLR-01", barcode = "896400031001", costPricePkr = 5800.0, salePricePkr = 6500.0, stockQuantity = 12.0, minStockThreshold = 4.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Refined White Sugar (50kg)", category = "Sugar", sku = "RAW-SGR-02", barcode = "896400031002", costPricePkr = 6800.0, salePricePkr = 7200.0, stockQuantity = 8.0, minStockThreshold = 3.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Dark Cocoa Compound (10kg)", category = "Chocolate", sku = "RAW-CCA-03", barcode = "896400031003", costPricePkr = 7200.0, salePricePkr = 8500.0, stockQuantity = 5.0, minStockThreshold = 2.0, unit = "Pack"),
            ProductEntity(businessId = businessId, name = "Cream Margarine (16kg Tin)", category = "Dairy", sku = "RAW-MRG-04", barcode = "896400031004", costPricePkr = 8400.0, salePricePkr = 9200.0, stockQuantity = 7.0, minStockThreshold = 3.0, unit = "Tin"),
            ProductEntity(businessId = businessId, name = "Cake Packaging Boxes (Pack 100)", category = "Packaging", sku = "PKG-BOX-05", barcode = "896400031005", costPricePkr = 1400.0, salePricePkr = 1800.0, stockQuantity = 4.0, minStockThreshold = 5.0, unit = "Pack")
        )
        BusinessCategory.GYM -> listOf(
            ProductEntity(businessId = businessId, name = "Gold Whey Protein 2kg Tub", category = "Supplements", sku = "SUP-WHEY-01", barcode = "896400041001", costPricePkr = 12000.0, salePricePkr = 14500.0, stockQuantity = 18.0, minStockThreshold = 5.0, unit = "Tub"),
            ProductEntity(businessId = businessId, name = "Pre-Workout Energy 300g", category = "Supplements", sku = "SUP-PRE-02", barcode = "896400041002", costPricePkr = 3400.0, salePricePkr = 4200.0, stockQuantity = 12.0, minStockThreshold = 4.0, unit = "Tub"),
            ProductEntity(businessId = businessId, name = "BCAA 2:1:1 Fuel Powder", category = "Supplements", sku = "SUP-BCAA-03", barcode = "896400041003", costPricePkr = 3000.0, salePricePkr = 3800.0, stockQuantity = 7.0, minStockThreshold = 3.0, unit = "Tub"),
            ProductEntity(businessId = businessId, name = "Gym Shaker Bottles 700ml", category = "Accessories", sku = "ACC-SHK-04", barcode = "896400041004", costPricePkr = 420.0, salePricePkr = 650.0, stockQuantity = 35.0, minStockThreshold = 10.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "Heavy Duty Wrist Wraps", category = "Gear", sku = "ACC-WRST-05", barcode = "896400041005", costPricePkr = 480.0, salePricePkr = 750.0, stockQuantity = 22.0, minStockThreshold = 8.0, unit = "Pair")
        )
        BusinessCategory.ELECTRONICS -> listOf(
            ProductEntity(businessId = businessId, name = "Type-C Braided Cable 65W", category = "Cables", sku = "ACC-CAB-01", barcode = "896400051001", costPricePkr = 180.0, salePricePkr = 350.0, stockQuantity = 55.0, minStockThreshold = 15.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "GaN Wall Fast Charger 45W", category = "Chargers", sku = "ACC-CHG-02", barcode = "896400051002", costPricePkr = 950.0, salePricePkr = 1450.0, stockQuantity = 24.0, minStockThreshold = 10.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "9D Glass Screen Protector", category = "Protection", sku = "ACC-GLS-03", barcode = "896400051003", costPricePkr = 90.0, salePricePkr = 250.0, stockQuantity = 95.0, minStockThreshold = 20.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "20,000mAh Dual USB Powerbank", category = "Power", sku = "PWR-BNK-04", barcode = "896400051004", costPricePkr = 2900.0, salePricePkr = 3800.0, stockQuantity = 8.0, minStockThreshold = 5.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "Wireless Bluetooth Neckband", category = "Audio", sku = "AUD-NCK-05", barcode = "896400051005", costPricePkr = 1200.0, salePricePkr = 1850.0, stockQuantity = 16.0, minStockThreshold = 6.0, unit = "Pcs")
        )
        BusinessCategory.WORKSHOP -> listOf(
            ProductEntity(businessId = businessId, name = "Mobil 1 Synthetic Engine Oil 4L", category = "Lubricants", sku = "LUB-MOB-01", barcode = "896400061001", costPricePkr = 5800.0, salePricePkr = 6800.0, stockQuantity = 16.0, minStockThreshold = 5.0, unit = "Can"),
            ProductEntity(businessId = businessId, name = "Toyota Genuine Oil Filter", category = "Filters", sku = "FLT-OIL-02", barcode = "896400061002", costPricePkr = 550.0, salePricePkr = 850.0, stockQuantity = 30.0, minStockThreshold = 10.0, unit = "Pcs"),
            ProductEntity(businessId = businessId, name = "Ceramic Brake Pads Front Set", category = "Brakes", sku = "BRK-PAD-03", barcode = "896400061003", costPricePkr = 2100.0, salePricePkr = 2800.0, stockQuantity = 8.0, minStockThreshold = 4.0, unit = "Set"),
            ProductEntity(businessId = businessId, name = "Iridium Spark Plugs Set of 4", category = "Ignition", sku = "IGN-PLG-04", barcode = "896400061004", costPricePkr = 2400.0, salePricePkr = 3200.0, stockQuantity = 12.0, minStockThreshold = 4.0, unit = "Set"),
            ProductEntity(businessId = businessId, name = "Radiator Coolant Antifreeze 1L", category = "Fluids", sku = "FLD-CLN-05", barcode = "896400061005", costPricePkr = 680.0, salePricePkr = 950.0, stockQuantity = 22.0, minStockThreshold = 6.0, unit = "Bottle")
        )
        BusinessCategory.WHOLESALE -> listOf(
            ProductEntity(businessId = businessId, name = "Refined Sugar Commercial 50kg", category = "Sugar", sku = "WHL-SGR-01", barcode = "896400071001", costPricePkr = 6900.0, salePricePkr = 7200.0, stockQuantity = 85.0, minStockThreshold = 25.0, unit = "Sack"),
            ProductEntity(businessId = businessId, name = "Super Fine Wheat Flour 20kg", category = "Flour", sku = "WHL-FLR-02", barcode = "896400071002", costPricePkr = 2450.0, salePricePkr = 2650.0, stockQuantity = 140.0, minStockThreshold = 40.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Pure Cooking Oil Master 16L", category = "Oil & Ghee", sku = "WHL-OIL-03", barcode = "896400071003", costPricePkr = 7700.0, salePricePkr = 8200.0, stockQuantity = 65.0, minStockThreshold = 20.0, unit = "Tin"),
            ProductEntity(businessId = businessId, name = "Vanaspati Ghee Commercial Tin", category = "Oil & Ghee", sku = "WHL-GHE-04", barcode = "896400071004", costPricePkr = 7450.0, salePricePkr = 7900.0, stockQuantity = 50.0, minStockThreshold = 15.0, unit = "Tin"),
            ProductEntity(businessId = businessId, name = "Super Kernel Basmati 50kg Sack", category = "Rice", sku = "WHL-RCE-05", barcode = "896400071005", costPricePkr = 13800.0, salePricePkr = 14500.0, stockQuantity = 40.0, minStockThreshold = 10.0, unit = "Sack")
        )
        else -> listOf(
            ProductEntity(businessId = businessId, name = "Super Basmati Rice 5kg", category = "Groceries", sku = "RET-RICE-01", barcode = "896400081001", costPricePkr = 1300.0, salePricePkr = 1450.0, stockQuantity = 45.0, minStockThreshold = 10.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Sunflower Cooking Oil 1L", category = "Groceries", sku = "RET-OIL-02", barcode = "896400081002", costPricePkr = 480.0, salePricePkr = 520.0, stockQuantity = 28.0, minStockThreshold = 10.0, unit = "Bottle"),
            ProductEntity(businessId = businessId, name = "Danedar Tea Powder 500g", category = "Beverages", sku = "RET-TEA-03", barcode = "896400081003", costPricePkr = 720.0, salePricePkr = 780.0, stockQuantity = 14.0, minStockThreshold = 8.0, unit = "Pack"),
            ProductEntity(businessId = businessId, name = "Fine Wheat Flour 10kg", category = "Groceries", sku = "RET-FLR-04", barcode = "896400081004", costPricePkr = 1180.0, salePricePkr = 1250.0, stockQuantity = 5.0, minStockThreshold = 8.0, unit = "Bag"),
            ProductEntity(businessId = businessId, name = "Refined White Sugar 1kg", category = "Groceries", sku = "RET-SGR-05", barcode = "896400081005", costPricePkr = 145.0, salePricePkr = 160.0, stockQuantity = 60.0, minStockThreshold = 15.0, unit = "Pcs")
        )
    }
}
