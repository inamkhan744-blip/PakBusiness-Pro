package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.InventoryItemEntity
import com.example.ui.CartItem
import com.example.ui.PosViewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.dialogs.ThermalReceiptPreviewDialog
import com.example.ui.dialogs.ThermalReceiptType
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailPosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inventory by viewModel.filteredInventory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var pendingThermalReceiptData by remember { mutableStateOf<InvoiceExportData?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val cartTotal = cart.sumOf { it.total }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("retail_pos_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Retail POS & Shop Counter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Quick barcode sales, billing, and receipts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Button(
                    onClick = { showScanner = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Item")
                }
            }

            // Notification message
            toastMessage?.let { msg ->
                Surface(
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { toastMessage = null }, modifier = Modifier.size(18.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search products by name or barcode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main layout: Products list & Cart bar
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Products Grid / List
                LazyColumn(
                    modifier = Modifier.weight(1.3f).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(inventory) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("PKR ${item.salePrice.toInt()} • Stock: ${item.stockQuantity.toInt()}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { viewModel.addToCart(item) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Add", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Cart Summary Panel
                Card(
                    modifier = Modifier.weight(1.2f).fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Cart (${cart.size})", fontWeight = FontWeight.Bold)
                            if (cart.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearCart() }) {
                                    Text("Clear", color = Color(0xFFC62828), fontSize = 11.sp)
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(cart) { ci ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ci.item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("PKR ${ci.total.toInt()}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.updateCartQuantity(ci.item.id, ci.quantity - 1) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                        Text("${ci.quantity.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { viewModel.updateCartQuantity(ci.item.id, ci.quantity + 1) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Total: PKR ${cartTotal.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showCheckoutDialog = true },
                            enabled = cart.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checkout Bill")
                        }
                    }
                }
            }
        }
    }

    // Barcode scanner
    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { code ->
                viewModel.addByBarcode(code) { msg -> toastMessage = msg }
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }

    // Checkout Dialog with Print PDF & Thermal Options
    if (showCheckoutDialog) {
        CheckoutReceiptDialog(
            cart = cart,
            total = cartTotal,
            onDismiss = { showCheckoutDialog = false },
            onConfirmSale = { custName, phone, method, shouldPrintPdf, shouldPrintThermal ->
                val invoiceData = InvoiceExportData(
                    customerName = custName,
                    customerPhone = phone,
                    paymentMethod = method,
                    items = cart.map {
                        InvoiceExportItem(
                            name = it.item.name,
                            quantity = it.quantity,
                            unitPrice = it.unitPrice,
                            total = it.total
                        )
                    }
                )
                viewModel.checkout(customerName = custName, customerPhone = phone, paymentMethod = method) {
                    if (shouldPrintPdf) {
                        PdfExportUtil.printInvoice(context, invoiceData)
                    } else if (shouldPrintThermal) {
                        pendingThermalReceiptData = invoiceData
                    }
                    toastMessage = "Order completed successfully!"
                }
                showCheckoutDialog = false
            }
        )
    }

    // Thermal Receipt Preview Dialog
    if (pendingThermalReceiptData != null) {
        ThermalReceiptPreviewDialog(
            receiptType = ThermalReceiptType.PosInvoice(pendingThermalReceiptData!!),
            onDismiss = { pendingThermalReceiptData = null }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onSave = { name, category, cost, sale, stock, barcode ->
                viewModel.saveProduct(name = name, category = category, costPrice = cost, salePrice = sale, stock = stock, barcode = barcode)
                showAddProductDialog = false
                toastMessage = "Product added: $name"
            }
        )
    }
}

@Composable
private fun CheckoutReceiptDialog(
    cart: List<CartItem>,
    total: Double,
    onDismiss: () -> Unit,
    onConfirmSale: (customerName: String, customerPhone: String, paymentMethod: String, printPdf: Boolean, printThermal: Boolean) -> Unit
) {
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var customerPhone by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Complete Sale & Print Bill", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Total Payable: PKR ${total.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 16.sp)

                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = { Text("Customer Phone (Optional)") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "EasyPaisa", "JazzCash", "Card").forEach { m ->
                        FilterChip(selected = paymentMethod == m, onClick = { paymentMethod = m }, label = { Text(m) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(onClick = { onConfirmSale(customerName, customerPhone, paymentMethod, false, true) }) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thermal Print", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = { onConfirmSale(customerName, customerPhone, paymentMethod, true, false) }) {
                        Text("PDF Bill", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("50") }
    var barcode by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add New Product", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock Quantity") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode / SKU") }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, category, costPrice.toDoubleOrNull() ?: 0.0, salePrice.toDoubleOrNull() ?: 0.0, stock.toDoubleOrNull() ?: 10.0, barcode)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
