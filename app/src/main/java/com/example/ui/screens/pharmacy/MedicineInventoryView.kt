package com.example.ui.screens.pharmacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicineInventoryView(
    medicines: List<MedicineEntity>,
    onSaveMedicine: (MedicineEntity) -> Unit,
    onDeleteMedicine: (Long) -> Unit,
    onUpdateStock: (Long, Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Low Stock, Expiring Soon, Expired, In Stock

    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }
    var stockAdjustMedicine by remember { mutableStateOf<MedicineEntity?>(null) }
    var stockAdjustValue by remember { mutableStateOf("") }

    val now = remember { System.currentTimeMillis() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    // Summary calculations
    val totalMedicinesCount = medicines.size
    val totalStockUnits = medicines.sumOf { it.quantity }
    val totalInventoryValue = medicines.sumOf { it.quantity * it.salePrice }
    val lowStockCount = medicines.count { it.isLowStock() }
    val expiredCount = medicines.count { it.isExpired(now) }
    val expiringSoonCount = medicines.count { it.isExpiringSoon(30, now) }

    val filteredMedicines = remember(medicines, searchQuery, selectedCategory, selectedFilter) {
        medicines.filter { med ->
            val matchesCategory = selectedCategory == "All" || med.category.equals(selectedCategory, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Low Stock" -> med.isLowStock()
                "Expiring Soon" -> med.isExpiringSoon(30, now)
                "Expired" -> med.isExpired(now)
                "In Stock" -> med.quantity > 0 && !med.isExpired(now)
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    med.name.contains(searchQuery, ignoreCase = true) ||
                    med.genericName.contains(searchQuery, ignoreCase = true) ||
                    med.batchNumber.contains(searchQuery, ignoreCase = true) ||
                    med.rackNumber.contains(searchQuery, ignoreCase = true) ||
                    med.supplierName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesFilter && matchesQuery
        }
    }

    if (isAddDialogOpen || medicineToEdit != null) {
        AddEditMedicineDialog(
            initialMedicine = medicineToEdit,
            onDismiss = {
                isAddDialogOpen = false
                medicineToEdit = null
            },
            onSave = { med ->
                onSaveMedicine(med)
                isAddDialogOpen = false
                medicineToEdit = null
            }
        )
    }

    // Quick stock adjustment dialog
    if (stockAdjustMedicine != null) {
        AlertDialog(
            onDismissRequest = { stockAdjustMedicine = null },
            title = { Text("Adjust Stock: ${stockAdjustMedicine?.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Current in-stock: ${stockAdjustMedicine?.quantity} units. Enter new updated count:",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = stockAdjustValue,
                        onValueChange = { stockAdjustValue = it },
                        label = { Text("New Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = stockAdjustValue.toIntOrNull()
                        if (newQty != null && newQty >= 0) {
                            onUpdateStock(stockAdjustMedicine!!.id, newQty)
                            stockAdjustMedicine = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text("Update Stock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { stockAdjustMedicine = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("Delete Medicine?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove '${medicineToDelete?.name}' (Batch: ${medicineToDelete?.batchNumber}) from the pharmacy inventory?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMedicine(medicineToDelete!!.id)
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Metrics Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Medicines
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF99F6E4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Medicines", fontSize = 11.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Medium)
                            Text("$totalMedicinesCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF134E4A))
                            Text("$totalStockUnits units total", fontSize = 10.sp, color = Color(0xFF0F766E))
                        }
                    }

                    // Stock Worth (PKR)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Inventory Value", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                            Text("Rs. ${totalInventoryValue.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            Text("Retail price basis", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }

                    // Low Stock Alert Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lowStockCount > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (lowStockCount > 0) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Low Stock", fontSize = 11.sp, color = if (lowStockCount > 0) Color(0xFF92400E) else Color(0xFF475569), fontWeight = FontWeight.Medium)
                            Text("$lowStockCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (lowStockCount > 0) Color(0xFFB45309) else Color(0xFF1E293B))
                            Text("Below min alert", fontSize = 10.sp, color = if (lowStockCount > 0) Color(0xFFB45309) else Color(0xFF64748B))
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by medicine name, generic, batch, rack...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0D9488))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inventory_search"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D9488),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            // Quick Status Filter Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All", "In Stock", "Low Stock", "Expiring Soon", "Expired").forEach { filter ->
                        val count = when (filter) {
                            "Low Stock" -> lowStockCount
                            "Expiring Soon" -> expiringSoonCount
                            "Expired" -> expiredCount
                            else -> null
                        }
                        val label = if (count != null && count > 0) "$filter ($count)" else filter

                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (filter) {
                                    "Expired" -> Color(0xFFDC2626)
                                    "Low Stock", "Expiring Soon" -> Color(0xFFD97706)
                                    else -> Color(0xFF0D9488)
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Category Filter Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (listOf("All") + MEDICINE_CATEGORIES).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0D9488),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredMedicines.size} of $totalMedicinesCount medicines",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Medicines List
            if (filteredMedicines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No medicines match criteria", color = Color(0xFF64748B), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredMedicines, key = { it.id }) { med ->
                    val isExpired = med.isExpired(now)
                    val isExpiringSoon = med.isExpiringSoon(30, now)
                    val isOutOfStock = med.quantity <= 0
                    val isLowStock = med.isLowStock()

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                isExpired -> Color(0xFFFCA5A5)
                                isOutOfStock -> Color(0xFFFECACA)
                                isLowStock || isExpiringSoon -> Color(0xFFFCD34D)
                                else -> Color(0xFFE2E8F0)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_inv_med_${med.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header: Name, Generic formula, Category
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = med.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isExpired) Color(0xFF991B1B) else Color(0xFF0F172A)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = med.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (med.genericName.isNotBlank()) {
                                        Text(
                                            text = "Active Salt: ${med.genericName}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                // Retail Sale Price
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rs. ${med.salePrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0D9488)
                                    )
                                    if (med.purchasePrice > 0) {
                                        val margin = (med.salePrice - med.purchasePrice)
                                        Text(
                                            text = "Cost: Rs.${med.purchasePrice.toInt()} (+${margin.toInt()})",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            // Rack Number & Batch Number & Expiry Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rack Location Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = med.rackNumber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E40AF)
                                        )
                                    }
                                }

                                // Batch Number Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = "Batch: ${med.batchNumber}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                // Expiry Date Badge with alert status
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        isExpired -> Color(0xFFFEE2E2)
                                        isExpiringSoon -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFF0FDF4)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isExpired -> Color(0xFFFCA5A5)
                                            isExpiringSoon -> Color(0xFFFCD34D)
                                            else -> Color(0xFF86EFAC)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isExpired || isExpiringSoon) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (isExpired) Color(0xFFDC2626) else Color(0xFFD97706),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        Text(
                                            text = when {
                                                isExpired -> "EXPIRED (${dateFormat.format(Date(med.expiryDate))})"
                                                isExpiringSoon -> "Exp in ${med.daysUntilExpiry(now)} days"
                                                else -> "Exp: ${dateFormat.format(Date(med.expiryDate))}"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isExpired -> Color(0xFFDC2626)
                                                isExpiringSoon -> Color(0xFFD97706)
                                                else -> Color(0xFF166534)
                                            }
                                        )
                                    }
                                }
                            }

                            // Stock progress bar & quantity
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Stock: ${med.quantity} units",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when {
                                            isOutOfStock -> Color(0xFFDC2626)
                                            isLowStock -> Color(0xFFD97706)
                                            else -> Color(0xFF0F172A)
                                        }
                                    )
                                    Text(
                                        text = "Alert threshold: ${med.minStockAlert} units",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                val progress = if (med.minStockAlert > 0) {
                                    (med.quantity.toFloat() / (med.minStockAlert * 2f)).coerceIn(0f, 1f)
                                } else 1f

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = when {
                                        isOutOfStock -> Color(0xFFDC2626)
                                        isLowStock -> Color(0xFFD97706)
                                        else -> Color(0xFF16A34A)
                                    },
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }

                            // Supplier Details Panel
                            if (med.supplierName.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Supplier: ${med.supplierName}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E293B)
                                            )
                                            if (med.supplierInvoiceRef.isNotBlank()) {
                                                Text(
                                                    text = "Challan / Inv: ${med.supplierInvoiceRef}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        if (med.supplierPhone.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFCCFBF1),
                                                modifier = Modifier.clickable {
                                                    try {
                                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${med.supplierPhone}"))
                                                        context.startActivity(dialIntent)
                                                    } catch (_: Exception) {}
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(med.supplierPhone, fontSize = 10.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Bottom actions: Stock (+ / -), Edit, Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Stock stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Stock:", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clickable {
                                                if (med.quantity > 0) onUpdateStock(med.id, med.quantity - 1)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable {
                                            stockAdjustMedicine = med
                                            stockAdjustValue = med.quantity.toString()
                                        }
                                    ) {
                                        Text(
                                            text = "${med.quantity}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFCCFBF1),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clickable {
                                                onUpdateStock(med.id, med.quantity + 1)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                // Edit and Delete buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { medicineToEdit = med },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 11.sp, color = Color(0xFF0D9488))
                                    }

                                    IconButton(
                                        onClick = { medicineToDelete = med },
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

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to Add Medicine
        FloatingActionButton(
            onClick = {
                medicineToEdit = null
                isAddDialogOpen = true
            },
            containerColor = Color(0xFF0D9488),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_medicine")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                Text("Add Medicine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
