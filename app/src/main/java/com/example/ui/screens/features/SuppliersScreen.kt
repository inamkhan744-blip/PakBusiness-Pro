package com.example.ui.screens.features

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SupplierItem(
    val id: String,
    val companyName: String,
    val contactPerson: String,
    val phone: String,
    val city: String,
    var payableBalance: Double,
    val ntn: String = "",
    val paymentTerms: String = "15 Days"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"

    val suppliers = remember {
        mutableStateListOf(
            SupplierItem("1", "Al-Razi Pharma Distributors", "Zahid Mehmood", "0300-8889911", "Lahore", 125000.0, "1456789-2", "Credit 15 Days"),
            SupplierItem("2", "National Foods Official Agency", "Khurram Shahzad", "0321-4443322", "Karachi", 68500.0, "2345678-9", "Cash on Delivery"),
            SupplierItem("3", "Engro Commodities & Grains", "Haji Abdul Sattar", "0333-7776655", "Multan", 240000.0, "3456789-0", "Monthly Settlement"),
            SupplierItem("4", "Packages Packaging Industries", "Imran Rafiq", "0345-1122334", "Faisalabad", 34200.0, "4567890-1", "Weekly Bill")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierForPayment by remember { mutableStateOf<SupplierItem?>(null) }
    var supplierForPurchase by remember { mutableStateOf<SupplierItem?>(null) }
    var supplierToDelete by remember { mutableStateOf<SupplierItem?>(null) }

    var formCompany by remember { mutableStateOf("") }
    var formContact by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formCity by remember { mutableStateOf("") }
    var formBalance by remember { mutableStateOf("") }
    var formNtn by remember { mutableStateOf("") }

    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentModeInput by remember { mutableStateOf("Bank Transfer") }
    var purchaseAmountInput by remember { mutableStateOf("") }
    var purchaseInvoiceNum by remember { mutableStateOf("") }

    val filteredSuppliers = suppliers.filter {
        it.companyName.contains(searchQuery, ignoreCase = true) ||
                it.contactPerson.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true)
    }

    val totalPayable = suppliers.sumOf { it.payableBalance }

    fun sendWhatsAppRemittance(supplier: SupplierItem, amount: Double, mode: String) {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val msg = """
            *السلام علیکم ${supplier.contactPerson} صاحب (${supplier.companyName})*
            
            *${business?.name ?: "PakBusiness"}* کی جانب سے آپ کے واجب الادا بل کی ادائیگی کر دی گئی ہے:
            
            💰 *ادا شدہ رقم:* $currency ${String.format(Locale.getDefault(), "%,.2f", amount)}
            💳 *طریقہ کار:* $mode
            📅 *تاریخ:* $dateStr
            📌 *نیا بقایا بیلنس:* $currency ${String.format(Locale.getDefault(), "%,.2f", supplier.payableBalance)}
            
            برائے مہربانی تصدیق فرما دیں۔ شکریہ!
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, msg)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Send Payment Proof via WhatsApp"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Suppliers & Vendor Khata (وینڈر کھاتہ)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Payables & Purchase bills tracking", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_suppliers_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("suppliers_screen")
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Factory, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Text("Total Supplier Payables (ہم نے دینے ہیں)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDC2626)) {
                                    Text("${suppliers.size} Distributors", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Text(
                                text = "$currency ${String.format(Locale.getDefault(), "%,.2f", totalPayable)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )

                            Text(
                                text = "Distributor khata keeps track of incoming stock credit, bills, and payment remittances.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // PDF Statement Export
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val items = suppliers.map {
                                            InvoiceExportItem(name = "${it.companyName} (${it.city})", quantity = 1.0, unitPrice = it.payableBalance, total = it.payableBalance)
                                        }
                                        val data = InvoiceExportData(
                                            invoiceNumber = "SUPP-${System.currentTimeMillis() % 100000}",
                                            businessName = business?.name ?: "PakBusiness",
                                            businessAddress = "COMMERCIAL SUPPLIER & VENDOR AUDIT",
                                            businessPhone = business?.phone ?: "",
                                            customerName = "TOTAL VENDOR PAYABLES LEDGER",
                                            customerPhone = "Suppliers Count: ${suppliers.size}",
                                            items = items,
                                            subtotal = totalPayable,
                                            discount = 0.0,
                                            tax = 0.0,
                                            grandTotal = totalPayable,
                                            paymentMethod = "Total Due ($currency)",
                                            footerNote = "Official Vendor Payables Statement • Generated by PakBusiness Pro"
                                        )
                                        PdfExportUtil.printInvoice(context, data)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print Report", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        val items = suppliers.map {
                                            InvoiceExportItem(name = "${it.companyName} (${it.city})", quantity = 1.0, unitPrice = it.payableBalance, total = it.payableBalance)
                                        }
                                        val data = InvoiceExportData(
                                            invoiceNumber = "SUPP-${System.currentTimeMillis() % 100000}",
                                            businessName = business?.name ?: "PakBusiness",
                                            businessAddress = "COMMERCIAL SUPPLIER & VENDOR AUDIT",
                                            businessPhone = business?.phone ?: "",
                                            customerName = "TOTAL VENDOR PAYABLES LEDGER",
                                            customerPhone = "Suppliers Count: ${suppliers.size}",
                                            items = items,
                                            subtotal = totalPayable,
                                            discount = 0.0,
                                            tax = 0.0,
                                            grandTotal = totalPayable,
                                            paymentMethod = "Total Due ($currency)",
                                            footerNote = "Official Vendor Payables Statement • Generated by PakBusiness Pro"
                                        )
                                        PdfExportUtil.shareInvoice(context, data)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share PDF", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search supplier company, contact, or city...") },
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
                        modifier = Modifier.fillMaxWidth().testTag("search_suppliers_input")
                    )
                }

                items(filteredSuppliers, key = { it.id }) { supplier ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(shape = CircleShape, color = PakEmeraldContainer, modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Column {
                                        Text(supplier.companyName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${supplier.contactPerson} • ${supplier.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${supplier.city} • Terms: ${supplier.paymentTerms}", fontSize = 11.sp, color = PakEmeraldPrimary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Payable Due", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    Text(
                                        "$currency ${String.format(Locale.getDefault(), "%,.0f", supplier.payableBalance)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                            // Action Buttons: Pay Supplier, Add Purchase Bill, Call, Delete
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (supplier.phone.isNotBlank()) {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phone}")))
                                        }
                                    },
                                    modifier = Modifier.weight(0.9f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Call", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        supplierForPurchase = supplier
                                        purchaseAmountInput = ""
                                        purchaseInvoiceNum = "PUR-${System.currentTimeMillis() % 10000}"
                                    },
                                    modifier = Modifier.weight(1.1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+Bill", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }

                                Button(
                                    onClick = {
                                        supplierForPayment = supplier
                                        paymentAmountInput = supplier.payableBalance.toInt().toString()
                                        paymentModeInput = "Bank Transfer"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    modifier = Modifier.weight(1.1f)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Pay", fontSize = 11.sp)
                                }

                                IconButton(onClick = { supplierToDelete = supplier }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // FAB to Add Supplier
            FloatingActionButton(
                onClick = {
                    formCompany = ""
                    formContact = ""
                    formPhone = ""
                    formCity = ""
                    formBalance = ""
                    formNtn = ""
                    showAddDialog = true
                },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).testTag("btn_add_supplier_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }

            // Add Supplier Dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Add Distributor / Supplier", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = formCompany, onValueChange = { formCompany = it }, label = { Text("Company / Distributor Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = formContact, onValueChange = { formContact = it }, label = { Text("Contact Person Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = formPhone, onValueChange = { formPhone = it }, label = { Text("WhatsApp / Phone Number *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = formCity, onValueChange = { formCity = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = formBalance, onValueChange = { formBalance = it }, label = { Text("Opening Due ($currency)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (formCompany.isNotBlank() && formPhone.isNotBlank()) {
                                    suppliers.add(
                                        SupplierItem(
                                            id = System.currentTimeMillis().toString(),
                                            companyName = formCompany.trim(),
                                            contactPerson = formContact.trim(),
                                            phone = formPhone.trim(),
                                            city = formCity.trim(),
                                            payableBalance = formBalance.toDoubleOrNull() ?: 0.0
                                        )
                                    )
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Text("Save Supplier")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Pay Supplier Dialog
            if (supplierForPayment != null) {
                val supp = supplierForPayment!!
                AlertDialog(
                    onDismissRequest = { supplierForPayment = null },
                    title = { Text("Record Payment to ${supp.companyName}", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Current Due: $currency ${String.format(Locale.getDefault(), "%,.2f", supp.payableBalance)}", color = Color(0xFFDC2626))
                            OutlinedTextField(
                                value = paymentAmountInput,
                                onValueChange = { paymentAmountInput = it },
                                label = { Text("Amount Paid ($currency) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = paymentModeInput,
                                onValueChange = { paymentModeInput = it },
                                label = { Text("Payment Method (e.g. Bank Alfalah, Cash)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amt = paymentAmountInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    supp.payableBalance = (supp.payableBalance - amt).coerceAtLeast(0.0)
                                    sendWhatsAppRemittance(supp, amt, paymentModeInput)
                                    supplierForPayment = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Text("Confirm & WhatsApp")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { supplierForPayment = null }) { Text("Cancel") }
                    }
                )
            }

            // Add Purchase Bill Dialog
            if (supplierForPurchase != null) {
                val supp = supplierForPurchase!!
                AlertDialog(
                    onDismissRequest = { supplierForPurchase = null },
                    title = { Text("Add Purchase Bill from ${supp.companyName}", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = purchaseAmountInput,
                                onValueChange = { purchaseAmountInput = it },
                                label = { Text("Bill Amount ($currency) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = purchaseInvoiceNum,
                                onValueChange = { purchaseInvoiceNum = it },
                                label = { Text("Distributor Invoice / Bilty #") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amt = purchaseAmountInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    supp.payableBalance += amt
                                    supplierForPurchase = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Add to Payable")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { supplierForPurchase = null }) { Text("Cancel") }
                    }
                )
            }

            // Delete Supplier Dialog
            if (supplierToDelete != null) {
                val supp = supplierToDelete!!
                AlertDialog(
                    onDismissRequest = { supplierToDelete = null },
                    title = { Text("Remove Supplier?") },
                    text = { Text("Delete '${supp.companyName}' from vendor ledger?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                suppliers.remove(supp)
                                supplierToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { supplierToDelete = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
