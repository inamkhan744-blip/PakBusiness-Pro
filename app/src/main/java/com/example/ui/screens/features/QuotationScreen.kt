package com.example.ui.screens.features

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.data.OrderEntity
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class QuotationStatus(val label: String, val urduLabel: String, val color: Color) {
    PENDING("Draft / Pending", "زیر غور", Color(0xFFD97706)),
    APPROVED("Accepted", "منظور شدہ", Color(0xFF16A34A)),
    CONVERTED("Converted to Sale", "بل میں تبدیل", PakEmeraldPrimary),
    EXPIRED("Expired", "ختم شدہ", Color(0xFFDC2626))
}

data class QuotationItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var quantity: Double,
    var unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

data class Quotation(
    val id: String = UUID.randomUUID().toString(),
    val quoteNumber: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val date: String,
    val validityDays: Int = 15,
    val items: List<QuotationItem>,
    val discount: Double = 0.0,
    val taxGstPercent: Double = 0.0,
    val termsAndNotes: String = "1. Rates valid for 15 days.\n2. 50% advance with Purchase Order.\n3. Taxes as applicable by Govt.",
    var status: QuotationStatus = QuotationStatus.PENDING
) {
    val subtotal: Double get() = items.sumOf { it.total }
    val taxAmount: Double get() = ((subtotal - discount).coerceAtLeast(0.0) * taxGstPercent) / 100.0
    val grandTotal: Double get() = (subtotal - discount).coerceAtLeast(0.0) + taxAmount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedQuotationForView by remember { mutableStateOf<Quotation?>(null) }

    // Seed initial quotations for instant business utility
    val quotations = remember {
        mutableStateListOf(
            Quotation(
                quoteNumber = "QT-2026-001",
                customerName = "Haji Aslam & Brothers",
                customerPhone = "0300-1234567",
                customerAddress = "Shah Alam Market, Lahore",
                date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                items = listOf(
                    QuotationItem(name = "Industrial Heavy Duty Switch 32A", quantity = 100.0, unitPrice = 450.0),
                    QuotationItem(name = "Copper Wire Coil 7/29 (Pakistan Cables)", quantity = 25.0, unitPrice = 8500.0)
                ),
                discount = 2500.0,
                taxGstPercent = 0.0,
                status = QuotationStatus.PENDING
            ),
            Quotation(
                quoteNumber = "QT-2026-002",
                customerName = "Falcon Engineering Works",
                customerPhone = "0321-9876543",
                customerAddress = "Industrial Area, Karachi",
                date = "12 Oct 2026",
                items = listOf(
                    QuotationItem(name = "Steel Bearings Size 6205", quantity = 50.0, unitPrice = 1200.0),
                    QuotationItem(name = "Hydraulic Oil Drum 200L", quantity = 2.0, unitPrice = 48000.0)
                ),
                discount = 5000.0,
                taxGstPercent = 18.0,
                status = QuotationStatus.APPROVED
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Quotations & Kacha Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "تخمینہ و کوٹیشن ساز (WhatsApp & PDF)",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PakEmeraldDark),
                actions = {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nayi Quotation",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary KPI Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalQuotes = quotations.size
                    val totalValue = quotations.sumOf { it.grandTotal }
                    val pendingCount = quotations.count { it.status == QuotationStatus.PENDING }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Quotes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalQuotes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pending Review", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$pendingCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.3f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pipeline Value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PakEmeraldDark)
                        }
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("All Quotations (${quotations.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Pending (${quotations.count { it.status == QuotationStatus.PENDING }})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Accepted / Converted", fontWeight = FontWeight.SemiBold) }
                )
            }

            val displayedQuotations = remember(selectedTabIndex, quotations.toList()) {
                when (selectedTabIndex) {
                    1 -> quotations.filter { it.status == QuotationStatus.PENDING }
                    2 -> quotations.filter { it.status == QuotationStatus.APPROVED || it.status == QuotationStatus.CONVERTED }
                    else -> quotations
                }
            }

            if (displayedQuotations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No quotations found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Nayi quotation banayein aur WhatsApp ya PDF pe bhejein",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Text("Create First Quotation")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedQuotations, key = { it.id }) { quote ->
                        QuotationCard(
                            quotation = quote,
                            currency = currency,
                            onViewDetails = { selectedQuotationForView = quote },
                            onShareWhatsApp = {
                                shareQuotationViaWhatsApp(context, quote, business)
                            },
                            onExportPdf = {
                                exportQuotationPdf(context, quote, business)
                            },
                            onConvertToSale = {
                                scope.launch {
                                    convertQuotationToOrder(context, quote, businessId)
                                    quote.status = QuotationStatus.CONVERTED
                                }
                            },
                            onDelete = {
                                quotations.remove(quote)
                            }
                        )
                    }
                }
            }
        }
    }

    // Create New Quotation Dialog
    if (showCreateDialog) {
        CreateQuotationDialog(
            currency = currency,
            onDismiss = { showCreateDialog = false },
            onSave = { newQuote ->
                quotations.add(0, newQuote)
                showCreateDialog = false
            }
        )
    }

    // View Quotation Details Dialog
    if (selectedQuotationForView != null) {
        val quote = selectedQuotationForView!!
        AlertDialog(
            onDismissRequest = { selectedQuotationForView = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(quote.quoteNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(quote.customerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = quote.status.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = quote.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = quote.status.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Phone: ${quote.customerPhone} | Address: ${quote.customerAddress}",
                        fontSize = 12.sp
                    )
                    HorizontalDivider()
                    Text("Items Summary:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    quote.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity.toInt()}x ${item.name}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", item.total)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", fontSize = 12.sp)
                        Text("$currency ${String.format(Locale.getDefault(), "%,.0f", quote.subtotal)}", fontSize = 12.sp)
                    }
                    if (quote.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:", fontSize = 12.sp, color = Color(0xFFDC2626))
                            Text("- $currency ${String.format(Locale.getDefault(), "%,.0f", quote.discount)}", fontSize = 12.sp, color = Color(0xFFDC2626))
                        }
                    }
                    if (quote.taxGstPercent > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GST (${quote.taxGstPercent}%):", fontSize = 12.sp)
                            Text("+ $currency ${String.format(Locale.getDefault(), "%,.0f", quote.taxAmount)}", fontSize = 12.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        Text("$currency ${String.format(Locale.getDefault(), "%,.0f", quote.grandTotal)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Terms & Conditions:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(quote.termsAndNotes, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareQuotationViaWhatsApp(context, quote, business)
                        selectedQuotationForView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedQuotationForView = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun QuotationCard(
    quotation: Quotation,
    currency: String,
    onViewDetails: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onExportPdf: () -> Unit,
    onConvertToSale: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PakEmeraldContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = quotation.quoteNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = quotation.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = quotation.status.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = quotation.status.urduLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = quotation.status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = quotation.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${quotation.items.size} Items • Phone: ${quotation.customerPhone}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Estimate Total",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency ${String.format(Locale.getDefault(), "%,.0f", quotation.grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PakEmeraldPrimary
                    )
                }
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // WhatsApp button
                OutlinedButton(
                    onClick = onShareWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF25D366)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF25D366))
                }

                // PDF button
                OutlinedButton(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.sp, color = Color(0xFFDC2626))
                }

                // Convert to POS Sale
                if (quotation.status != QuotationStatus.CONVERTED) {
                    Button(
                        onClick = onConvertToSale,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Make Bill", fontSize = 11.sp, color = Color.White)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateQuotationDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Quotation) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var validityDays by remember { mutableStateOf("15") }
    var discountInput by remember { mutableStateOf("") }
    var gstInput by remember { mutableStateOf("0") }

    val items = remember {
        mutableStateListOf(
            QuotationItem(name = "", quantity = 1.0, unitPrice = 0.0)
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Quotation / Kacha Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / Dukaan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone / WhatsApp") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = validityDays,
                            onValueChange = { validityDays = it },
                            label = { Text("Validity (Days)") },
                            modifier = Modifier.weight(0.7f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("City / Area Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quotation Items:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        TextButton(
                            onClick = {
                                items.add(QuotationItem(name = "", quantity = 1.0, unitPrice = 0.0))
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Add Item", fontSize = 12.sp)
                        }
                    }
                }

                items(items.size) { idx ->
                    val currentItem = items[idx]
                    var itemName by remember(currentItem) { mutableStateOf(currentItem.name) }
                    var itemQty by remember(currentItem) { mutableStateOf(currentItem.quantity.toString()) }
                    var itemPrice by remember(currentItem) { mutableStateOf(if (currentItem.unitPrice > 0) currentItem.unitPrice.toString() else "") }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = itemName,
                                    onValueChange = {
                                        itemName = it
                                        currentItem.name = it
                                    },
                                    placeholder = { Text("Product / Item description") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { items.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = itemQty,
                                    onValueChange = {
                                        itemQty = it
                                        currentItem.quantity = it.toDoubleOrNull() ?: 1.0
                                    },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = itemPrice,
                                    onValueChange = {
                                        itemPrice = it
                                        currentItem.unitPrice = it.toDoubleOrNull() ?: 0.0
                                    },
                                    label = { Text("Rate ($currency)") },
                                    modifier = Modifier.weight(1.5f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Discount ($currency)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gstInput,
                            onValueChange = { gstInput = it },
                            label = { Text("GST/Tax %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        errorMessage = "Please enter customer name"
                        return@Button
                    }
                    val validItems = items.filter { it.name.isNotBlank() && it.unitPrice > 0 }
                    if (validItems.isEmpty()) {
                        errorMessage = "Please add at least one valid item with rate"
                        return@Button
                    }

                    val quoteNum = "QT-${SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault()).format(Date())}"
                    val newQuote = Quotation(
                        quoteNumber = quoteNum,
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        customerAddress = customerAddress.trim(),
                        date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                        validityDays = validityDays.toIntOrNull() ?: 15,
                        items = validItems,
                        discount = discountInput.toDoubleOrNull() ?: 0.0,
                        taxGstPercent = gstInput.toDoubleOrNull() ?: 0.0,
                        status = QuotationStatus.PENDING
                    )
                    onSave(newQuote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save Quotation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shareQuotationViaWhatsApp(context: Context, quote: Quotation, business: BusinessEntity?) {
    val currency = business?.currency ?: "PKR"
    val businessName = business?.name ?: "PakBusiness Store"
    val itemsSummary = quote.items.mapIndexed { idx, it ->
        "${idx + 1}. ${it.name} - ${it.quantity.toInt()} x $currency ${it.unitPrice.toInt()} = $currency ${it.total.toInt()}"
    }.joinToString("\n")

    val message = """
*QUOTATION / کچا بل*
🏪 *${businessName}*
-----------------------------
📄 *Quote No:* ${quote.quoteNumber}
📅 *Date:* ${quote.date}
👤 *Client:* ${quote.customerName}
📞 *Phone:* ${quote.customerPhone}

*ITEMS & ESTIMATED RATES:*
$itemsSummary
-----------------------------
Subtotal: $currency ${String.format(Locale.getDefault(), "%,.0f", quote.subtotal)}
Discount: $currency ${String.format(Locale.getDefault(), "%,.0f", quote.discount)}
GST/Tax: $currency ${String.format(Locale.getDefault(), "%,.0f", quote.taxAmount)}
*GRAND TOTAL: $currency ${String.format(Locale.getDefault(), "%,.0f", quote.grandTotal)}*
-----------------------------
📌 *Validity:* Valid for ${quote.validityDays} days from date of issue.
${quote.termsAndNotes}

_Generated via PakBusiness Suite_
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        if (quote.customerPhone.isNotBlank()) {
            val cleanPhone = quote.customerPhone.replace(Regex("[^0-9]"), "")
            putExtra("jid", "$cleanPhone@s.whatsapp.net")
        }
    }
    context.startActivity(Intent.createChooser(intent, "Share Quotation via WhatsApp"))
}

private fun exportQuotationPdf(context: Context, quote: Quotation, business: BusinessEntity?) {
    val exportData = InvoiceExportData(
        invoiceNumber = quote.quoteNumber,
        businessName = business?.name ?: "PakBusiness Merchant",
        businessPhone = business?.phone ?: "",
        businessAddress = business?.address ?: "Pakistan",
        customerName = quote.customerName,
        customerPhone = quote.customerPhone,
        items = quote.items.map {
            InvoiceExportItem(
                name = it.name,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                total = it.total
            )
        },
        subtotal = quote.subtotal,
        discount = quote.discount,
        tax = quote.taxAmount,
        grandTotal = quote.grandTotal,
        paymentMethod = "QUOTATION ESTIMATE (کچا بل)",
        footerNote = "Validity: ${quote.validityDays} days. ${quote.termsAndNotes}"
    )
    PdfExportUtil.shareInvoice(context, exportData)
}

private suspend fun convertQuotationToOrder(context: Context, quote: Quotation, businessId: Long) {
    val orderDao = AppDatabase.getDatabase(context).orderDao()
    val order = OrderEntity(
        businessId = businessId,
        orderNumber = "ORD-${SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault()).format(Date())}",
        partyName = quote.customerName,
        partyPhone = quote.customerPhone,
        orderType = "SALE",
        subtotal = quote.subtotal,
        discountAmount = quote.discount,
        taxAmount = quote.taxAmount,
        totalAmount = quote.grandTotal,
        paidAmount = quote.grandTotal,
        balanceAmount = 0.0,
        paymentMode = "CASH",
        paymentStatus = "PAID",
        status = "COMPLETED",
        itemsCount = quote.items.size,
        notes = "Converted from Quote: ${quote.quoteNumber}"
    )
    orderDao.insertOrder(order)
}
