package com.example.ui.screens.features

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.util.Locale

enum class BarcodeLayoutType(val title: String, val labelsPerPage: Int) {
    SINGLE_THERMAL("Single 50x25mm Roll", 1),
    A4_SHEET_24("A4 Sheet (24 Stickers)", 24),
    A4_SHEET_40("A4 Sheet (40 Stickers)", 40),
    GARMENT_TAG("Clothing Hang Tag", 1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeLabelStudioScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    val productDao = remember(businessId) { AppDatabase.getDatabase(context).productDao() }
    val dbProducts by productDao.getProducts(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedProductName by remember { mutableStateOf("Panadol 500mg Box") }
    var selectedPrice by remember { mutableStateOf("900") }
    var selectedBarcode by remember { mutableStateOf("896400011221") }
    var selectedLayout by remember { mutableStateOf(BarcodeLayoutType.A4_SHEET_24) }
    var labelCopiesCount by remember { mutableFloatStateOf(24f) }

    fun exportBarcodeSheetPdf(isPrint: Boolean) {
        val count = labelCopiesCount.toInt()
        val invoiceItems = (1..count).map { idx ->
            InvoiceExportItem(
                name = "Sticker #$idx: $selectedProductName [${selectedBarcode}]",
                quantity = 1.0,
                unitPrice = selectedPrice.toDoubleOrNull() ?: 0.0,
                total = selectedPrice.toDoubleOrNull() ?: 0.0
            )
        }
        val exportData = InvoiceExportData(
            invoiceNumber = "BARCODE-${System.currentTimeMillis() % 100000}",
            businessName = business?.name ?: "PakBusiness Enterprise",
            businessAddress = "PRINTABLE BARCODE LABELS SHEET (${selectedLayout.title})",
            businessPhone = business?.phone ?: "",
            customerName = "$selectedProductName • $currency $selectedPrice",
            customerPhone = "Barcode: $selectedBarcode • Total Stickers: $count",
            items = invoiceItems,
            subtotal = (selectedPrice.toDoubleOrNull() ?: 0.0) * count,
            discount = 0.0,
            tax = 0.0,
            grandTotal = (selectedPrice.toDoubleOrNull() ?: 0.0) * count,
            paymentMethod = "Ready for Print",
            footerNote = "Official Barcode Label Sheet • Compatible with Thermal and Laser sticker paper."
        )
        if (isPrint) {
            PdfExportUtil.printInvoice(context, exportData)
        } else {
            PdfExportUtil.shareInvoice(context, exportData)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Barcode Label Studio (بارکوڈ پرنٹر)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Print retail barcode & price stickers", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_barcode_studio_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("barcode_label_studio_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Interactive Label Preview Sticker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LIVE STICKER PREVIEW (اسٹیکر کا نمونہ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Store Header
                            Text(
                                text = (business?.name ?: "PAK BUSINESS STORE").uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Product Title
                            Text(
                                text = selectedProductName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            // Visual Rendered Barcode Bars
                            Canvas(modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)) {
                                val barWidth = size.width / 50
                                val chars = (selectedBarcode + "101").toCharArray()
                                for (i in 0 until 48) {
                                    val isBar = (i % 2 == 0) || (chars[i % chars.size].code % 3 != 0)
                                    if (isBar) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(i * barWidth, 0f),
                                            size = Size(barWidth * 0.75f, size.height)
                                        )
                                    }
                                }
                            }

                            // Numerical Barcode Text
                            Text(
                                text = selectedBarcode,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            // Bold Price Tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black
                            ) {
                                Text(
                                    text = "PRICE: $currency ${String.format(Locale.getDefault(), "%,.0f", selectedPrice.toDoubleOrNull() ?: 0.0)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Select Product from Inventory
            if (dbProducts.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SELECT PRODUCT FROM INVENTORY (موجودہ پروڈکٹ منتخب کریں)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dbProducts) { prod ->
                                val isSelected = selectedProductName == prod.name
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        selectedProductName = prod.name
                                        selectedPrice = prod.salePricePkr.toInt().toString()
                                        selectedBarcode = if (prod.barcode.isNotBlank()) prod.barcode else "8964${prod.id.toString().padStart(6, '0')}"
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = prod.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$currency ${prod.salePricePkr.toInt()}",
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Custom Product Inputs
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Customize Label Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = selectedProductName,
                            onValueChange = { selectedProductName = it },
                            label = { Text("Product Title on Sticker") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_label_title")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedPrice,
                                onValueChange = { selectedPrice = it },
                                label = { Text("Price ($currency)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_label_price")
                            )
                            OutlinedTextField(
                                value = selectedBarcode,
                                onValueChange = { selectedBarcode = it },
                                label = { Text("Barcode / SKU") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f).testTag("input_label_barcode")
                            )
                        }
                    }
                }
            }

            // Sheet Layout Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SHEET FORMAT & PAPER SIZE (کاغذ کا سائز)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BarcodeLayoutType.entries) { layout ->
                            val isSelected = selectedLayout == layout
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedLayout = layout
                                    labelCopiesCount = layout.labelsPerPage.toFloat()
                                },
                                label = { Text(layout.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PakEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Number of Labels Copies
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Labels to Print:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${labelCopiesCount.toInt()} Stickers", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        }
                        Slider(
                            value = labelCopiesCount,
                            onValueChange = { labelCopiesCount = it },
                            valueRange = 1f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth().testTag("slider_label_copies")
                        )
                    }
                }
            }

            // Print & Share Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportBarcodeSheetPdf(isPrint = false) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_share_barcode_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { exportBarcodeSheetPdf(isPrint = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_print_barcode_sheet")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Stickers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
