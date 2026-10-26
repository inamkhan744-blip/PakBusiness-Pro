package com.example.ui.screens.pharmacy

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.PharmacySaleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PharmacyInvoiceDialog(
    business: BusinessEntity?,
    sale: PharmacySaleEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val items = remember(sale.itemsJson) { sale.parseItems() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF0D9488), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Sale Completed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(sale.invoiceNumber, fontSize = 11.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Thermal receipt-style surface
                Surface(
                    color = Color(0xFFFBFBFB),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Store header
                        Text(
                            text = business?.name ?: "Pak Pharmacy & Medical Store",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0F172A)
                        )
                        if (!business?.address.isNullOrBlank()) {
                            Text(
                                text = business?.address ?: "",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF64748B)
                            )
                        }
                        if (!business?.phone.isNullOrBlank()) {
                            Text(
                                text = "Ph: ${business?.phone}",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = "--- MEDICAL CASH MEMO ---",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0D9488),
                            letterSpacing = 1.sp
                        )

                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                        // Invoice metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Invoice: ${sale.invoiceNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Date: ${dateFormat.format(Date(sale.saleDate))}", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Customer: ${sale.customerName}", fontSize = 11.sp)
                            if (sale.customerPhone.isNotBlank()) {
                                Text("Ph: ${sale.customerPhone}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                        if (sale.doctorPrescriber.isNotBlank()) {
                            Text("Prescribed By: Dr. ${sale.doctorPrescriber}", fontSize = 11.sp, color = Color(0xFF0D9488))
                        }

                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item / Batch", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                            Text("Rack", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("Qty", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                            Text("Price", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                        }

                        // Sold items rows
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF1E293B))
                                    Text("B: ${item.batchNumber}", fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Text(item.rackNumber, fontSize = 9.sp, color = Color(0xFF0284C7), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("${item.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                                Text("Rs. ${item.unitPrice.toInt()}", fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                Text("Rs. ${item.subtotal.toInt()}", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                        }

                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                        // Price summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Subtotal:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("Rs. ${sale.totalAmount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (sale.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount:", fontSize = 11.sp, color = Color(0xFF16A34A))
                                Text("- Rs. ${sale.discount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFCCFBF1), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET PAYABLE:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF134E4A))
                            Text("PKR ${sale.netAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F766E))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Mode: ${sale.paymentMethod}", fontSize = 11.sp, color = Color(0xFF475569))
                            Text("Stock Deducted: Yes", fontSize = 10.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Thank you for trusting Pak Pharmacy! Keep all medicines away from children and direct sunlight.",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Action buttons: Share & Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceipt(context, business, sale, items)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_pharmacy_receipt")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_done_pharmacy_receipt")
                    ) {
                        Text("Done / Next Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

private fun shareReceipt(
    context: Context,
    business: BusinessEntity?,
    sale: PharmacySaleEntity,
    items: List<com.example.data.PharmacySoldItem>
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    val sb = StringBuilder()
    sb.appendLine("===============================")
    sb.appendLine(business?.name ?: "Pak Pharmacy & Medical Store")
    if (!business?.address.isNullOrBlank()) sb.appendLine(business?.address)
    if (!business?.phone.isNullOrBlank()) sb.appendLine("Phone: ${business?.phone}")
    sb.appendLine("===============================")
    sb.appendLine("INVOICE: ${sale.invoiceNumber}")
    sb.appendLine("Date: ${dateFormat.format(Date(sale.saleDate))}")
    sb.appendLine("Customer: ${sale.customerName}")
    if (sale.customerPhone.isNotBlank()) sb.appendLine("Phone: ${sale.customerPhone}")
    if (sale.doctorPrescriber.isNotBlank()) sb.appendLine("Doctor: Dr. ${sale.doctorPrescriber}")
    sb.appendLine("-------------------------------")
    sb.appendLine(String.format(Locale.US, "%-16s %-4s %-8s", "Medicine [Batch]", "Qty", "Price"))
    sb.appendLine("-------------------------------")
    for (item in items) {
        val line = "${item.name} [${item.batchNumber}] (Rack: ${item.rackNumber})"
        sb.appendLine(line)
        sb.appendLine(String.format(Locale.US, "  %dx Rs.%.0f = Rs.%.0f", item.quantity, item.unitPrice, item.subtotal))
    }
    sb.appendLine("-------------------------------")
    sb.appendLine("Gross Total: PKR ${sale.totalAmount.toInt()}")
    if (sale.discount > 0) {
        sb.appendLine("Discount: -PKR ${sale.discount.toInt()}")
    }
    sb.appendLine("NET AMOUNT: PKR ${sale.netAmount.toInt()}")
    sb.appendLine("Payment Method: ${sale.paymentMethod}")
    sb.appendLine("===============================")
    sb.appendLine("Thank you! Generated by PakBusiness Pro")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Pharmacy Invoice - ${sale.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Pharmacy Invoice"))
}
