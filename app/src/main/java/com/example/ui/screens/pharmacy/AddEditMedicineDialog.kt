package com.example.ui.screens.pharmacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val MEDICINE_CATEGORIES = listOf(
    "Tablet",
    "Capsule",
    "Syrup",
    "Suspension",
    "Injection",
    "Ointment",
    "Drops",
    "Sachet",
    "Inhaler"
)

val COMMON_RACK_SUGGESTIONS = listOf(
    "Rack A-01",
    "Rack A-02",
    "Rack B-01",
    "Rack B-02",
    "Rack C-01",
    "Shelf 1",
    "Shelf 2",
    "Cold Storage / Fridge",
    "Counter Drawer"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicineDialog(
    initialMedicine: MedicineEntity? = null,
    onDismiss: () -> Unit,
    onSave: (MedicineEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var genericName by remember { mutableStateOf(initialMedicine?.genericName ?: "") }
    var selectedCategory by remember { mutableStateOf(initialMedicine?.category ?: "Tablet") }
    var batchNumber by remember { mutableStateOf(initialMedicine?.batchNumber ?: "") }
    var expiryDateMillis by remember {
        mutableStateOf(
            initialMedicine?.expiryDate ?: (System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L)
        )
    }
    var rackNumber by remember { mutableStateOf(initialMedicine?.rackNumber ?: "Rack A-01") }
    var purchasePriceStr by remember {
        mutableStateOf(if ((initialMedicine?.purchasePrice ?: 0.0) > 0) initialMedicine?.purchasePrice.toString() else "")
    }
    var salePriceStr by remember {
        mutableStateOf(if ((initialMedicine?.salePrice ?: 0.0) > 0) initialMedicine?.salePrice.toString() else "")
    }
    var quantityStr by remember {
        mutableStateOf(if ((initialMedicine?.quantity ?: 0) > 0) initialMedicine?.quantity.toString() else "50")
    }
    var minAlertStr by remember {
        mutableStateOf(if ((initialMedicine?.minStockAlert ?: 0) > 0) initialMedicine?.minStockAlert.toString() else "15")
    }
    var supplierName by remember { mutableStateOf(initialMedicine?.supplierName ?: "") }
    var supplierPhone by remember { mutableStateOf(initialMedicine?.supplierPhone ?: "") }
    var supplierInvoiceRef by remember { mutableStateOf(initialMedicine?.supplierInvoiceRef ?: "") }
    var dosageInstructions by remember { mutableStateOf(initialMedicine?.dosageInstructions ?: "") }
    var notes by remember { mutableStateOf(initialMedicine?.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryDateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expiryDateMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val expiryFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialMedicine == null) "Add Medicine" else "Edit Medicine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Medicine Name & Generic Formula
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Brand / Medicine Name *") },
                    placeholder = { Text("e.g. Panadol 500mg, Augmentin 625mg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = genericName,
                    onValueChange = { genericName = it },
                    label = { Text("Generic Formula (Active Salt)") },
                    placeholder = { Text("e.g. Paracetamol, Amoxicillin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_generic"),
                    singleLine = true
                )

                // Category chips
                Text(
                    text = "Dosage Form / Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MEDICINE_CATEGORIES.forEach { cat ->
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

                // Batch Number & Expiry Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it; errorMessage = null },
                        label = { Text("Batch No. *") },
                        placeholder = { Text("e.g. BT-2401") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_batch_number"),
                        singleLine = true
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D9488)),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { showDatePicker = true }
                            .testTag("button_select_expiry"),
                        color = Color(0xFFF0FDFA)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Expiry Date *", fontSize = 10.sp, color = Color(0xFF0D9488))
                                Text(
                                    expiryFormat.format(Date(expiryDateMillis)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF134E4A)
                                )
                            }
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Rack / Shelf Location
                Column {
                    OutlinedTextField(
                        value = rackNumber,
                        onValueChange = { rackNumber = it },
                        label = { Text("Rack / Shelf Location *") },
                        placeholder = { Text("e.g. Rack A-01, Fridge") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0D9488))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_rack_number"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quick Racks:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        COMMON_RACK_SUGGESTIONS.take(5).forEach { sug ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (rackNumber == sug) Color(0xFF0D9488) else Color(0xFFE2E8F0),
                                modifier = Modifier.clickable { rackNumber = sug }
                            ) {
                                Text(
                                    text = sug,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (rackNumber == sug) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Stock & Low Stock Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Current Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_stock_quantity"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minAlertStr,
                        onValueChange = { minAlertStr = it },
                        label = { Text("Low Stock Alert *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_min_alert_threshold"),
                        singleLine = true
                    )
                }

                // Purchase & Sale Price (PKR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text("Cost Price (PKR)") },
                        prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_purchase_price"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it; errorMessage = null },
                        label = { Text("Retail Price (PKR) *") },
                        prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_sale_price"),
                        singleLine = true
                    )
                }

                // Supplier Details
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Supplier & Distributor Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B)
                        )

                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("Supplier / Distributor Name") },
                            placeholder = { Text("e.g. GSK Pakistan, Getz Pharma, Searle") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_supplier_name"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = supplierPhone,
                                onValueChange = { supplierPhone = it },
                                label = { Text("Supplier Phone") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_supplier_phone"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = supplierInvoiceRef,
                                onValueChange = { supplierInvoiceRef = it },
                                label = { Text("Invoice / Challan #") },
                                placeholder = { Text("e.g. INV-882") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_supplier_invoice"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Dosage / Storage instructions
                OutlinedTextField(
                    value = dosageInstructions,
                    onValueChange = { dosageInstructions = it },
                    label = { Text("Dosage / Patient Directions") },
                    placeholder = { Text("e.g. 1 tablet after food twice daily") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Storage Warnings") },
                    placeholder = { Text("e.g. Store below 25°C, protect from light") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter medicine name."
                        return@Button
                    }
                    if (batchNumber.isBlank()) {
                        errorMessage = "Please provide batch number for tracking."
                        return@Button
                    }
                    val salePrice = salePriceStr.toDoubleOrNull()
                    if (salePrice == null || salePrice <= 0.0) {
                        errorMessage = "Please enter a valid retail sale price in PKR."
                        return@Button
                    }
                    val qty = quantityStr.toIntOrNull() ?: 0
                    val minAlert = minAlertStr.toIntOrNull() ?: 10
                    val costPrice = purchasePriceStr.toDoubleOrNull() ?: 0.0

                    val medicine = (initialMedicine ?: MedicineEntity(
                        businessId = 0L,
                        name = "",
                        batchNumber = "",
                        expiryDate = 0L,
                        salePrice = 0.0
                    )).copy(
                        name = name.trim(),
                        genericName = genericName.trim(),
                        category = selectedCategory,
                        batchNumber = batchNumber.trim(),
                        expiryDate = expiryDateMillis,
                        rackNumber = if (rackNumber.isBlank()) "Rack A-01" else rackNumber.trim(),
                        purchasePrice = costPrice,
                        salePrice = salePrice,
                        quantity = qty,
                        minStockAlert = minAlert,
                        supplierName = supplierName.trim(),
                        supplierPhone = supplierPhone.trim(),
                        supplierInvoiceRef = supplierInvoiceRef.trim(),
                        dosageInstructions = dosageInstructions.trim(),
                        notes = notes.trim()
                    )
                    onSave(medicine)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                modifier = Modifier.testTag("btn_save_medicine")
            ) {
                Text(if (initialMedicine == null) "Add Medicine" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
