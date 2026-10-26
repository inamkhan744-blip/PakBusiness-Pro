package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.BluetoothThermalPrinterManager
import com.example.util.EscPosPaperSize
import com.example.util.EscPosUtil
import com.example.util.InvoiceExportData
import com.example.util.KotPrintItem
import com.example.util.PdfExportUtil
import com.example.util.PrinterStatus

sealed class ThermalReceiptType {
    data class PosInvoice(val invoice: InvoiceExportData, val cashierName: String? = null) : ThermalReceiptType()
    data class KitchenKot(
        val tableName: String,
        val orderId: Long,
        val items: List<KotPrintItem>,
        val guestCount: Int = 1,
        val serverName: String = "Staff",
        val orderNotes: String = "",
        val timestamp: String = ""
    ) : ThermalReceiptType()
}

@Composable
fun ThermalReceiptPreviewDialog(
    receiptType: ThermalReceiptType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val printerManager = remember { BluetoothThermalPrinterManager.getInstance(context) }
    val printerStatus by printerManager.printerStatus.collectAsState()
    val defaultPaperSize by printerManager.selectedPaperSize.collectAsState()

    var activePaperSize by remember { mutableStateOf(defaultPaperSize) }
    var showPrinterSettingsDialog by remember { mutableStateOf(false) }
    var isPrintingJob by remember { mutableStateOf(false) }

    val formattedReceiptText = remember(receiptType, activePaperSize) {
        when (receiptType) {
            is ThermalReceiptType.PosInvoice -> {
                EscPosUtil.formatPosReceiptText(
                    invoice = receiptType.invoice,
                    paperSize = activePaperSize,
                    cashierName = receiptType.cashierName
                )
            }
            is ThermalReceiptType.KitchenKot -> {
                EscPosUtil.formatKotReceiptText(
                    tableName = receiptType.tableName,
                    orderId = receiptType.orderId,
                    items = receiptType.items,
                    guestCount = receiptType.guestCount,
                    serverName = receiptType.serverName,
                    orderNotes = receiptType.orderNotes,
                    timestamp = receiptType.timestamp.ifBlank { "Live" },
                    paperSize = activePaperSize
                )
            }
        }
    }

    val handlePrintThermal = {
        isPrintingJob = true
        val bytes = when (receiptType) {
            is ThermalReceiptType.PosInvoice -> {
                EscPosUtil.generatePosReceiptBytes(
                    invoice = receiptType.invoice,
                    paperSize = activePaperSize,
                    cashierName = receiptType.cashierName
                )
            }
            is ThermalReceiptType.KitchenKot -> {
                EscPosUtil.generateKotTicketBytes(
                    tableName = receiptType.tableName,
                    orderId = receiptType.orderId,
                    items = receiptType.items,
                    guestCount = receiptType.guestCount,
                    serverName = receiptType.serverName,
                    orderNotes = receiptType.orderNotes,
                    timestamp = receiptType.timestamp.ifBlank { "Now" },
                    paperSize = activePaperSize
                )
            }
        }

        val jobTitle = when (receiptType) {
            is ThermalReceiptType.PosInvoice -> "POS Invoice #${receiptType.invoice.invoiceNumber}"
            is ThermalReceiptType.KitchenKot -> "KOT #${receiptType.orderId} (${receiptType.tableName})"
        }

        printerManager.printRawBytes(bytes, jobTitle) { success, msg ->
            isPrintingJob = false
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_thermal_receipt_preview"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (receiptType is ThermalReceiptType.KitchenKot) PakGoldContainer else PakEmeraldContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = if (receiptType is ThermalReceiptType.KitchenKot) PakEmeraldDark else PakEmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = when (receiptType) {
                                is ThermalReceiptType.PosInvoice -> "Thermal POS Receipt"
                                is ThermalReceiptType.KitchenKot -> "Kitchen Order Ticket (KOT)"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activePaperSize.label} • ESC/POS",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showPrinterSettingsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Bluetooth Printer Settings",
                        tint = PakEmeraldPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Printer Status & Quick Pair Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (printerStatus) {
                        is PrinterStatus.Connected -> Color(0xFFE8F5E9)
                        is PrinterStatus.Connecting -> Color(0xFFFFF8E1)
                        is PrinterStatus.Error -> Color(0xFFFFEBEE)
                        PrinterStatus.Disconnected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        is PrinterStatus.Printing -> Color(0xFFE0F2FE)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrinterSettingsDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (printerStatus is PrinterStatus.Connected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = if (printerStatus is PrinterStatus.Connected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (printerStatus) {
                                    is PrinterStatus.Connected -> "Connected: ${(printerStatus as PrinterStatus.Connected).deviceName}"
                                    is PrinterStatus.Connecting -> "Connecting to ${(printerStatus as PrinterStatus.Connecting).deviceName}..."
                                    is PrinterStatus.Printing -> "Printing payload..."
                                    is PrinterStatus.Error -> "Warning: ${(printerStatus as PrinterStatus.Error).message}"
                                    PrinterStatus.Disconnected -> "No Bluetooth printer connected (Tap to pair)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (printerStatus is PrinterStatus.Connected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Configure",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )
                    }
                }

                // Paper Width Toggle Chips (58mm vs 80mm)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EscPosPaperSize.entries.forEach { size ->
                        val isSelected = activePaperSize == size
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activePaperSize = size
                                    printerManager.setPaperSize(size)
                                }
                        ) {
                            Text(
                                text = "${size.widthMm}mm (${size.columns} col)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Authentic Thermal Paper Roll Simulation Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Thermal paper off-white
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E2DC), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Jagged / torn top edge simulation
                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                            fontSize = 9.sp,
                            color = Color(0xFFA8A29E),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Scrollable receipt body in pure Monospace thermal print
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 280.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = formattedReceiptText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = if (activePaperSize == EscPosPaperSize.WIDTH_58MM) 11.sp else 9.5.sp,
                                lineHeight = if (activePaperSize == EscPosPaperSize.WIDTH_58MM) 14.sp else 12.sp,
                                color = Color(0xFF1C1917), // Thermal carbon black
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Jagged torn bottom edge
                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                            fontSize = 9.sp,
                            color = Color(0xFFA8A29E),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primary Action: Bluetooth Thermal Print
                Button(
                    onClick = handlePrintThermal,
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isPrintingJob,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_print_thermal_slip")
                ) {
                    if (isPrintingJob) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sending to Thermal Printer...")
                    } else {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (printerStatus) {
                                is PrinterStatus.Connected -> "Print to ${(printerStatus as PrinterStatus.Connected).deviceName}"
                                else -> "Print via Bluetooth Thermal"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary row: WhatsApp / System Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, formattedReceiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Thermal Receipt"))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }

                    if (receiptType is ThermalReceiptType.PosInvoice) {
                        OutlinedButton(
                            onClick = {
                                PdfExportUtil.printInvoice(context, (receiptType as ThermalReceiptType.PosInvoice).invoice)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("System Print", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showPrinterSettingsDialog) {
        BluetoothPrinterSettingsDialog(
            onDismiss = { showPrinterSettingsDialog = false }
        )
    }
}
