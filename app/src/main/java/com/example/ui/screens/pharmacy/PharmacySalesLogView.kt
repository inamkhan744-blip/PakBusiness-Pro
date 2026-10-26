package com.example.ui.screens.pharmacy

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.PharmacySaleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PharmacySalesLogView(
    business: BusinessEntity?,
    sales: List<PharmacySaleEntity>,
    onDeleteSale: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSaleForInvoice by remember { mutableStateOf<PharmacySaleEntity?>(null) }
    var saleToDelete by remember { mutableStateOf<PharmacySaleEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    val filteredSales = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) sales
        else {
            sales.filter {
                it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                        it.customerPhone.contains(searchQuery, ignoreCase = true) ||
                        it.doctorPrescriber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val totalSalesSum = sales.sumOf { it.netAmount }

    if (selectedSaleForInvoice != null) {
        PharmacyInvoiceDialog(
            business = business,
            sale = selectedSaleForInvoice!!,
            onDismiss = { selectedSaleForInvoice = null }
        )
    }

    if (saleToDelete != null) {
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Sale Record?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete invoice ${saleToDelete?.invoiceNumber}? Note: Stock was already deducted at sale time.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(saleToDelete!!.id)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Record", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top KPI Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDFA))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Pharmacy Revenue", fontSize = 11.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Medium)
                Text("PKR ${totalSalesSum.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF134E4A))
                Text("${sales.size} total customer invoices", fontSize = 10.sp, color = Color(0xFF0F766E))
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF0D9488), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search invoice #, customer, or doctor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0D9488)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D9488),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }

        // Sales List
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No sales invoices found", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSales, key = { it.id }) { sale ->
                    val items = remember(sale.itemsJson) { sale.parseItems() }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSaleForInvoice = sale }
                            .testTag("card_sale_${sale.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFCCFBF1)
                                    ) {
                                        Text(
                                            text = sale.invoiceNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0F766E),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = sale.paymentMethod,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "PKR ${sale.netAmount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0D9488)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Customer: ${sale.customerName}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = dateFormat.format(Date(sale.saleDate)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            if (sale.doctorPrescriber.isNotBlank()) {
                                Text(
                                    text = "Prescribed by: Dr. ${sale.doctorPrescriber}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0D9488)
                                )
                            }

                            // Items summary preview
                            Text(
                                text = items.joinToString(", ") { "${it.name} (${it.quantity}x)" },
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                maxLines = 2
                            )

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${items.size} medicines sold",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = { selectedSaleForInvoice = sale },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Invoice", fontSize = 11.sp, color = Color(0xFF0D9488))
                                    }

                                    IconButton(
                                        onClick = { saleToDelete = sale },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
