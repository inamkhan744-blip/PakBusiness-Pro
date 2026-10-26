package com.example.ui.screens.tabs

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.CustomerEntity
import com.example.ui.BusinessViewModel
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

enum class CustomerFilterType {
    ALL,
    RECEIVABLES_ONLY,
    CLEARED_ONLY
}

@Composable
fun CustomersTab(
    business: BusinessEntity?,
    modifier: Modifier = Modifier,
    viewModel: BusinessViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    val customerDao = remember(businessId) {
        AppDatabase.getDatabase(context).customerDao()
    }

    // Live Room DB Customers list
    val dbCustomers by customerDao.getCustomers(businessId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Auto-seed sample customers if database is completely empty for this business
    LaunchedEffect(businessId, dbCustomers.size) {
        if (dbCustomers.isEmpty()) {
            withContext(Dispatchers.IO) {
                val existingCount = customerDao.getCustomerCount(businessId)
                if (existingCount == 0) {
                    val seeds = listOf(
                        CustomerEntity(
                            businessId = businessId,
                            name = "Haji Muhammad Aslam",
                            phone = "0300-1234567",
                            city = "Lahore",
                            address = "Shop 14, Shah Alam Market",
                            openingBalancePkr = 15400.0,
                            currentBalancePkr = 15400.0,
                            creditLimitPkr = 50000.0,
                            ntnOrCnic = "35201-1234567-1",
                            notes = "Wholesale credit account • Reliable payer"
                        ),
                        CustomerEntity(
                            businessId = businessId,
                            name = "Chaudhry Tariq Mehmood",
                            phone = "0321-9876543",
                            city = "Faisalabad",
                            address = "Katchery Bazaar",
                            openingBalancePkr = 32850.0,
                            currentBalancePkr = 32850.0,
                            creditLimitPkr = 100000.0,
                            ntnOrCnic = "33100-7654321-3",
                            notes = "Monthly bill settlement on 5th"
                        ),
                        CustomerEntity(
                            businessId = businessId,
                            name = "Malik Rizwan Traders",
                            phone = "0333-5551234",
                            city = "Rawalpindi",
                            address = "Raja Bazaar, Block C",
                            openingBalancePkr = 0.0,
                            currentBalancePkr = 0.0,
                            creditLimitPkr = 75000.0,
                            ntnOrCnic = "37405-9988776-5",
                            notes = "Cash on delivery customer"
                        ),
                        CustomerEntity(
                            businessId = businessId,
                            name = "Sheikh Imran Ahmed",
                            phone = "0345-4447890",
                            city = "Gujranwala",
                            address = "Rail Bazar",
                            openingBalancePkr = 8200.0,
                            currentBalancePkr = 8200.0,
                            creditLimitPkr = 30000.0,
                            ntnOrCnic = "34101-3322110-7",
                            notes = "Weekly orders"
                        ),
                        CustomerEntity(
                            businessId = businessId,
                            name = "Al-Madina Store (Bhai Zafar)",
                            phone = "0312-3334455",
                            city = "Karachi",
                            address = "Jodia Bazaar",
                            openingBalancePkr = 47500.0,
                            currentBalancePkr = 47500.0,
                            creditLimitPkr = 150000.0,
                            ntnOrCnic = "42101-5544332-9",
                            notes = "Commercial partner"
                        )
                    )
                    customerDao.insertCustomers(seeds)
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CustomerFilterType.ALL) }

    // Dialog States
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForUdhar by remember { mutableStateOf<CustomerEntity?>(null) }

    // Form inputs for Add/Edit Customer
    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formCity by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formNtnCnic by remember { mutableStateOf("") }
    var formOpeningBalance by remember { mutableStateOf("") }
    var formCreditLimit by remember { mutableStateOf("") }
    var formNotes by remember { mutableStateOf("") }

    // Payment / Debit Amount Inputs
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentNoteInput by remember { mutableStateOf("Vasooli / Cash Received") }
    var udharAmountInput by remember { mutableStateOf("") }
    var udharNoteInput by remember { mutableStateOf("Naya Udhaar / Debit Bill") }

    // Filtering logic
    val filteredCustomers = dbCustomers.filter { customer ->
        val matchesQuery = customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery, ignoreCase = true) ||
                customer.city.contains(searchQuery, ignoreCase = true) ||
                customer.ntnOrCnic.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            CustomerFilterType.ALL -> true
            CustomerFilterType.RECEIVABLES_ONLY -> customer.currentBalancePkr > 0.0
            CustomerFilterType.CLEARED_ONLY -> customer.currentBalancePkr <= 0.0
        }
        matchesQuery && matchesFilter
    }

    val totalReceivables = dbCustomers.filter { it.currentBalancePkr > 0.0 }.sumOf { it.currentBalancePkr }
    val countWithBalance = dbCustomers.count { it.currentBalancePkr > 0.0 }

    fun sendWhatsAppReminder(customer: CustomerEntity) {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val reminderMessage = """
            *السلام علیکم ${customer.name} صاحب!*
            
            امید ہے آپ خیریت سے ہوں گے۔
            *${business?.name ?: "ہمارا بزنس"}* کی طرف سے آپ کا کھاتہ / ادھار بیلنس:
            
            💰 *باقیہ رقم:* $currency ${String.format(Locale.getDefault(), "%,.2f", customer.currentBalancePkr)}
            📅 *تاریخ:* $dateStr
            📍 *فون:* ${business?.phone ?: ""}
            
            برائے مہربانی جلد از جلد اس واجب الادا رقم کی ادائیگی فرمائیں تاکہ آپ کا کھاتہ صاف اور سروس جاری رہے۔
            
            جزاک اللہ خیر!
            _${business?.name ?: "PakBusiness"}_
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reminderMessage)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Send Khata Reminder via WhatsApp")
        context.startActivity(shareIntent)
    }

    fun callCustomer(phone: String) {
        if (phone.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phone.replace(" ", "")}")
            }
            context.startActivity(intent)
        }
    }

    fun printSingleCustomerStatement(customer: CustomerEntity) {
        val invoiceItems = listOf(
            InvoiceExportItem(
                name = "Opening Khata Balance",
                quantity = 1.0,
                unitPrice = customer.openingBalancePkr,
                total = customer.openingBalancePkr
            ),
            InvoiceExportItem(
                name = "Current Outstanding Receivable",
                quantity = 1.0,
                unitPrice = customer.currentBalancePkr,
                total = customer.currentBalancePkr
            )
        )
        val exportData = InvoiceExportData(
            invoiceNumber = "KHATA-${customer.id}-${System.currentTimeMillis() % 10000}",
            businessName = business?.name ?: "PakBusiness Enterprise",
            businessAddress = business?.address ?: "Commercial Market",
            businessPhone = business?.phone ?: "",
            customerName = "${customer.name} (City: ${customer.city})",
            customerPhone = "${customer.phone} • CNIC: ${customer.ntnOrCnic.ifBlank { "N/A" }}",
            items = invoiceItems,
            subtotal = customer.currentBalancePkr,
            discount = 0.0,
            tax = 0.0,
            grandTotal = customer.currentBalancePkr,
            paymentMethod = "Udhaar Balance ($currency)",
            footerNote = "Credit Limit: $currency ${String.format(Locale.getDefault(), "%,.0f", customer.creditLimitPkr)} • Thank you for your business!"
        )
        PdfExportUtil.printInvoice(context, exportData)
    }

    Box(modifier = modifier.fillMaxSize().testTag("customers_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Khata Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = PakGoldSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Total Udhaar / Khata Receivable",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PakGoldSecondary
                            ) {
                                Text(
                                    text = "$countWithBalance / ${dbCustomers.size} with Balance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%,.2f", totalReceivables)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )

                        Text(
                            text = "Customer ledger is 100% persisted in local SQLite with offline multi-tenant isolation.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Export / Print Full Khata Ledger PDF
                        if (dbCustomers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val ledgerItems = dbCustomers.map { cust ->
                                            InvoiceExportItem(
                                                name = "${cust.name} (${cust.phone}${if (cust.city.isNotBlank()) ", ${cust.city}" else ""})",
                                                quantity = 1.0,
                                                unitPrice = cust.currentBalancePkr,
                                                total = cust.currentBalancePkr
                                            )
                                        }
                                        val exportData = InvoiceExportData(
                                            invoiceNumber = "KHATA-${System.currentTimeMillis() % 100000}",
                                            businessName = business?.name ?: "PakBusiness Enterprise",
                                            businessAddress = business?.address ?: "",
                                            businessPhone = business?.phone ?: "",
                                            customerName = "OVERALL UDHAAR / KHATA LEDGER AUDIT",
                                            customerPhone = "Total Registered Customers: ${dbCustomers.size}",
                                            items = ledgerItems,
                                            subtotal = totalReceivables,
                                            discount = 0.0,
                                            tax = 0.0,
                                            grandTotal = totalReceivables,
                                            paymentMethod = "Total Receivables ($currency)",
                                            footerNote = "Official Udhaar Ledger Statement • Generated by PakBusiness Pro"
                                        )
                                        PdfExportUtil.printInvoice(context, exportData)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_print_khata_ledger")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFB45309))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print Khata PDF", fontSize = 11.sp, color = Color(0xFFB45309))
                                }

                                Button(
                                    onClick = {
                                        val ledgerItems = dbCustomers.map { cust ->
                                            InvoiceExportItem(
                                                name = "${cust.name} (${cust.phone})",
                                                quantity = 1.0,
                                                unitPrice = cust.currentBalancePkr,
                                                total = cust.currentBalancePkr
                                            )
                                        }
                                        val exportData = InvoiceExportData(
                                            invoiceNumber = "KHATA-${System.currentTimeMillis() % 100000}",
                                            businessName = business?.name ?: "PakBusiness Enterprise",
                                            businessAddress = business?.address ?: "",
                                            businessPhone = business?.phone ?: "",
                                            customerName = "OVERALL UDHAAR / KHATA LEDGER AUDIT",
                                            customerPhone = "Total Registered Customers: ${dbCustomers.size}",
                                            items = ledgerItems,
                                            subtotal = totalReceivables,
                                            discount = 0.0,
                                            tax = 0.0,
                                            grandTotal = totalReceivables,
                                            paymentMethod = "Total Receivables ($currency)",
                                            footerNote = "Official Udhaar Ledger Statement • Generated by PakBusiness Pro"
                                        )
                                        PdfExportUtil.shareInvoice(context, exportData)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_share_khata_ledger")
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Ledger PDF", fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search customer name, phone, city, or CNIC...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("search_customers_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedFilter == CustomerFilterType.ALL,
                                onClick = { selectedFilter = CustomerFilterType.ALL },
                                label = { Text("All (${dbCustomers.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PakEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == CustomerFilterType.RECEIVABLES_ONLY,
                                onClick = { selectedFilter = CustomerFilterType.RECEIVABLES_ONLY },
                                label = { Text("Udhaar Due ($countWithBalance)") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (selectedFilter == CustomerFilterType.RECEIVABLES_ONLY) Color.White else Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDC2626),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == CustomerFilterType.CLEARED_ONLY,
                                onClick = { selectedFilter = CustomerFilterType.CLEARED_ONLY },
                                label = { Text("Cleared (${dbCustomers.size - countWithBalance})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PakEmeraldContainer,
                                    selectedLabelColor = PakEmeraldDark
                                )
                            )
                        }
                    }
                }
            }

            // Customer Cards List
            if (filteredCustomers.isEmpty()) {
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
                            Icon(Icons.Default.Person, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(40.dp))
                            Text("No Customers Found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap '+' below to add new customer accounts to your Khata.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(filteredCustomers, key = { it.id }) { customer ->
                val hasUdhar = customer.currentBalancePkr > 0.0
                val creditLimit = customer.creditLimitPkr
                val limitRatio = if (creditLimit > 0.0) (customer.currentBalancePkr / creditLimit).coerceIn(0.0, 1.0).toFloat() else 0f

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("customer_item_${customer.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header Row: Avatar, Name, Phone, and Balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(if (hasUdhar) Color(0xFFFEF2F2) else PakEmeraldContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (hasUdhar) Color(0xFFDC2626) else PakEmeraldPrimary
                                    )
                                }

                                Column {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = customer.phone,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (customer.city.isNotBlank()) {
                                            Text(
                                                text = "• ${customer.city}",
                                                fontSize = 11.sp,
                                                color = PakEmeraldPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    if (customer.ntnOrCnic.isNotBlank()) {
                                        Text(
                                            text = "CNIC/NTN: ${customer.ntnOrCnic}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (hasUdhar) Color(0xFFFEE2E2) else PakEmeraldContainer
                                ) {
                                    Text(
                                        text = if (hasUdhar) "Udhaar Due" else "Cleared",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasUdhar) Color(0xFFDC2626) else PakEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", customer.currentBalancePkr)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (hasUdhar) Color(0xFFDC2626) else PakEmeraldPrimary
                                )
                            }
                        }

                        // Credit Limit Progress Bar
                        if (creditLimit > 0.0) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Limit: $currency ${String.format(Locale.getDefault(), "%,.0f", creditLimit)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(limitRatio * 100).toInt()}% Used",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (limitRatio > 0.85f) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { limitRatio },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = if (limitRatio > 0.85f) Color(0xFFDC2626) else PakEmeraldPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // Actions Row: Call, WhatsApp, Receive Payment, Add Udhaar, PDF & Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Call Button
                            OutlinedButton(
                                onClick = { callCustomer(customer.phone) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp), tint = PakEmeraldPrimary)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Call", fontSize = 11.sp, color = PakEmeraldPrimary)
                            }

                            // WhatsApp Reminder
                            if (hasUdhar) {
                                Button(
                                    onClick = { sendWhatsAppReminder(customer) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            // Vasooli / Payment Button
                            Button(
                                onClick = {
                                    customerForPayment = customer
                                    paymentAmountInput = if (customer.currentBalancePkr > 0) customer.currentBalancePkr.toInt().toString() else ""
                                    paymentNoteInput = "Cash Vasooli"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Vasooli", fontSize = 11.sp)
                            }

                            // Add Debit / Naya Udhaar
                            OutlinedButton(
                                onClick = {
                                    customerForUdhar = customer
                                    udharAmountInput = ""
                                    udharNoteInput = "Udhaar Bill"
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+Debit", fontSize = 10.sp, color = Color(0xFFD97706))
                            }

                            // Statement PDF
                            IconButton(
                                onClick = { printSingleCustomerStatement(customer) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = "Print Statement",
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Edit Customer
                            IconButton(
                                onClick = {
                                    customerToEdit = customer
                                    formName = customer.name
                                    formPhone = customer.phone
                                    formCity = customer.city
                                    formAddress = customer.address
                                    formNtnCnic = customer.ntnOrCnic
                                    formCreditLimit = customer.creditLimitPkr.toInt().toString()
                                    formNotes = customer.notes
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Customer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Delete Customer
                            IconButton(
                                onClick = { customerToDelete = customer },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Customer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Customer FAB
        FloatingActionButton(
            onClick = {
                formName = ""
                formPhone = ""
                formCity = ""
                formAddress = ""
                formNtnCnic = ""
                formOpeningBalance = ""
                formCreditLimit = "50000"
                formNotes = ""
                showAddCustomerDialog = true
            },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("btn_add_customer_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Customer")
        }

        // Add Customer Dialog
        if (showAddCustomerDialog) {
            AlertDialog(
                onDismissRequest = { showAddCustomerDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PakEmeraldPrimary)
                        Text("Add New Customer / Khata", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Customer Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_new_customer_name")
                        )
                        OutlinedTextField(
                            value = formPhone,
                            onValueChange = { formPhone = it },
                            label = { Text("Mobile / WhatsApp Phone *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_new_customer_phone")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formCity,
                                onValueChange = { formCity = it },
                                label = { Text("City (e.g. Lahore)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formNtnCnic,
                                onValueChange = { formNtnCnic = it },
                                label = { Text("CNIC / NTN") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = formAddress,
                            onValueChange = { formAddress = it },
                            label = { Text("Shop / Home Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formOpeningBalance,
                                onValueChange = { formOpeningBalance = it },
                                label = { Text("Opening Udhaar ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_new_customer_balance")
                            )
                            OutlinedTextField(
                                value = formCreditLimit,
                                onValueChange = { formCreditLimit = it },
                                label = { Text("Credit Limit ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = formNotes,
                            onValueChange = { formNotes = it },
                            label = { Text("Account Notes (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formName.isNotBlank() && formPhone.isNotBlank()) {
                                val openingBal = formOpeningBalance.toDoubleOrNull() ?: 0.0
                                val limitVal = formCreditLimit.toDoubleOrNull() ?: 50000.0
                                scope.launch(Dispatchers.IO) {
                                    customerDao.insertCustomer(
                                        CustomerEntity(
                                            businessId = businessId,
                                            name = formName.trim(),
                                            phone = formPhone.trim(),
                                            city = formCity.trim(),
                                            address = formAddress.trim(),
                                            ntnOrCnic = formNtnCnic.trim(),
                                            openingBalancePkr = openingBal,
                                            currentBalancePkr = openingBal,
                                            creditLimitPkr = limitVal,
                                            notes = formNotes.trim()
                                        )
                                    )
                                }
                                showAddCustomerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.testTag("btn_confirm_add_customer")
                    ) {
                        Text("Save Account")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCustomerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Customer Dialog
        if (customerToEdit != null) {
            val target = customerToEdit!!
            AlertDialog(
                onDismissRequest = { customerToEdit = null },
                title = { Text("Edit Customer Profile", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formPhone,
                            onValueChange = { formPhone = it },
                            label = { Text("Phone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formCity,
                            onValueChange = { formCity = it },
                            label = { Text("City") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formCreditLimit,
                            onValueChange = { formCreditLimit = it },
                            label = { Text("Credit Limit ($currency)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formAddress,
                            onValueChange = { formAddress = it },
                            label = { Text("Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val limitVal = formCreditLimit.toDoubleOrNull() ?: target.creditLimitPkr
                            scope.launch(Dispatchers.IO) {
                                customerDao.updateCustomer(
                                    target.copy(
                                        name = formName.trim().ifBlank { target.name },
                                        phone = formPhone.trim().ifBlank { target.phone },
                                        city = formCity.trim(),
                                        address = formAddress.trim(),
                                        creditLimitPkr = limitVal,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            customerToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerToEdit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Receive Payment (Vasooli) Dialog
        if (customerForPayment != null) {
            val cust = customerForPayment!!
            AlertDialog(
                onDismissRequest = { customerForPayment = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = PakEmeraldPrimary)
                        Text("Record Vasooli (Payment)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Customer: ${cust.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Outstanding Balance: $currency ${String.format(Locale.getDefault(), "%,.2f", cust.currentBalancePkr)}",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                        OutlinedTextField(
                            value = paymentAmountInput,
                            onValueChange = { paymentAmountInput = it },
                            label = { Text("Amount Received ($currency) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_payment_amount")
                        )
                        OutlinedTextField(
                            value = paymentNoteInput,
                            onValueChange = { paymentNoteInput = it },
                            label = { Text("Payment Note / Mode") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val paid = paymentAmountInput.toDoubleOrNull() ?: 0.0
                            if (paid > 0.0) {
                                scope.launch(Dispatchers.IO) {
                                    customerDao.adjustCustomerBalance(cust.id, -paid)
                                }
                                customerForPayment = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.testTag("btn_confirm_receive_payment")
                    ) {
                        Text("Confirm Vasooli")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerForPayment = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Udhaar (Debit) Dialog
        if (customerForUdhar != null) {
            val cust = customerForUdhar!!
            AlertDialog(
                onDismissRequest = { customerForUdhar = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFD97706))
                        Text("Add Udhaar (Debit Entry)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Customer: ${cust.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Current Balance: $currency ${String.format(Locale.getDefault(), "%,.2f", cust.currentBalancePkr)}",
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = udharAmountInput,
                            onValueChange = { udharAmountInput = it },
                            label = { Text("Debit / Bill Amount ($currency) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_udhar_amount")
                        )
                        OutlinedTextField(
                            value = udharNoteInput,
                            onValueChange = { udharNoteInput = it },
                            label = { Text("Invoice / Reason Note") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val added = udharAmountInput.toDoubleOrNull() ?: 0.0
                            if (added > 0.0) {
                                scope.launch(Dispatchers.IO) {
                                    customerDao.adjustCustomerBalance(cust.id, added)
                                }
                                customerForUdhar = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.testTag("btn_confirm_add_udhar")
                    ) {
                        Text("Add to Khata")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerForUdhar = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (customerToDelete != null) {
            val cust = customerToDelete!!
            AlertDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text("Delete Customer Account?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to delete '${cust.name}' from local database? All outstanding khata records for this customer will be removed permanently.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                customerDao.deleteCustomer(cust)
                            }
                            customerToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
