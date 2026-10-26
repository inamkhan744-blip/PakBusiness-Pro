package com.example.ui.screens.pharmacy

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineEntity
import com.example.data.PharmacyCartItem
import com.example.data.PharmacySaleEntity
import com.example.ui.components.BarcodeScannerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickSaleView(
    medicines: List<MedicineEntity>,
    onCompleteSale: (
        cartItems: List<PharmacyCartItem>,
        customerName: String,
        customerPhone: String,
        doctorPrescriber: String,
        discount: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (PharmacySaleEntity) -> Unit
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Cart state
    val cart = remember { mutableStateListOf<PharmacyCartItem>() }
    var isCartExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var saleErrorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun addMedicineByBarcodeOrCode(code: String) {
        val trimmed = code.trim()
        val matchedMed = medicines.find { med ->
            med.id.toString() == trimmed ||
            med.batchNumber.equals(trimmed, ignoreCase = true) ||
            med.name.contains(trimmed, ignoreCase = true) ||
            trimmed.contains(med.name, ignoreCase = true) ||
            med.genericName.contains(trimmed, ignoreCase = true)
        } ?: medicines.firstOrNull()

        if (matchedMed != null) {
            val existingIndex = cart.indexOfFirst { it.medicine.id == matchedMed.id }
            if (existingIndex >= 0) {
                val current = cart[existingIndex]
                if (current.quantity < matchedMed.quantity) {
                    cart[existingIndex] = current.copy(quantity = current.quantity + 1)
                    Toast.makeText(context, "Added +1 ${matchedMed.name} to Bill", Toast.LENGTH_SHORT).show()
                } else {
                    saleErrorMessage = "Stock limit reached (${matchedMed.quantity} available)"
                    Toast.makeText(context, "Stock limit reached (${matchedMed.quantity} available)", Toast.LENGTH_SHORT).show()
                }
            } else {
                cart.add(PharmacyCartItem(medicine = matchedMed, quantity = 1))
                Toast.makeText(context, "Added ${matchedMed.name} (₨ ${matchedMed.salePrice.toInt()}) to Bill", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No medicine matched barcode '$trimmed'", Toast.LENGTH_SHORT).show()
        }
    }

    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var customerPhone by remember { mutableStateOf("") }
    var doctorPrescriber by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }

    var completedSaleDialog by remember { mutableStateOf<PharmacySaleEntity?>(null) }

    val now = remember { System.currentTimeMillis() }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.US) }

    val categories = listOf("All") + MEDICINE_CATEGORIES

    val filteredMedicines = remember(medicines, searchQuery, selectedCategory) {
        medicines.filter { med ->
            val matchesCategory = selectedCategory == "All" || med.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    med.name.contains(searchQuery, ignoreCase = true) ||
                    med.genericName.contains(searchQuery, ignoreCase = true) ||
                    med.batchNumber.contains(searchQuery, ignoreCase = true) ||
                    med.rackNumber.contains(searchQuery, ignoreCase = true) ||
                    med.supplierName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val cartTotalCount = cart.sumOf { it.quantity }
    val cartGrossAmount = cart.sumOf { it.subtotal }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val cartNetAmount = (cartGrossAmount - discount).coerceAtLeast(0.0)

    if (completedSaleDialog != null) {
        PharmacyInvoiceDialog(
            business = null,
            sale = completedSaleDialog!!,
            onDismiss = {
                completedSaleDialog = null
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cart.isNotEmpty()) 90.dp else 16.dp)
        ) {
            // Search Bar & Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search bar & CameraX Barcode Scanner for Medicine Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search medicine, generic, or scan barcode...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF0D9488))
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
                            .weight(1f)
                            .testTag("input_pharmacy_sale_search"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0D9488),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { showBarcodeScanner = true }
                            .testTag("btn_pharmacy_barcode_scanner")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Medicine Barcode",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Category Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
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

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredMedicines.size} medicines available",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tap item to add to bill",
                    fontSize = 11.sp,
                    color = Color(0xFF0D9488),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Medicines List
            if (filteredMedicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No medicines match your search",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
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
                    items(filteredMedicines, key = { it.id }) { med ->
                        val isExpired = med.isExpired(now)
                        val isExpiringSoon = med.isExpiringSoon(30, now)
                        val isOutOfStock = med.quantity <= 0
                        val isLowStock = med.isLowStock()

                        val inCartItem = cart.find { it.medicine.id == med.id }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpired) Color(0xFFFEF2F2) else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    isExpired -> Color(0xFFFCA5A5)
                                    inCartItem != null -> Color(0xFF0D9488)
                                    else -> Color(0xFFE2E8F0)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isExpired) {
                                        saleErrorMessage = "Cannot sell expired medicine (${med.name})! Return to supplier."
                                        return@clickable
                                    }
                                    if (isOutOfStock) {
                                        saleErrorMessage = "${med.name} is Out of Stock!"
                                        return@clickable
                                    }
                                    val existingIndex = cart.indexOfFirst { it.medicine.id == med.id }
                                    if (existingIndex >= 0) {
                                        val existing = cart[existingIndex]
                                        if (existing.quantity < med.quantity) {
                                            cart[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                                        } else {
                                            saleErrorMessage = "Cannot add more. Only ${med.quantity} available in stock."
                                        }
                                    } else {
                                        cart.add(PharmacyCartItem(medicine = med, quantity = 1))
                                    }
                                }
                                .testTag("card_med_${med.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                            color = Color(0xFFE2E8F0)
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
                                            text = med.genericName,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Tracking details row: Batch, Rack, Expiry
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Rack badge
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF0FDF4),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = med.rackNumber,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }

                                        // Batch badge
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = "B: ${med.batchNumber}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Expiry Badge
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when {
                                                isExpired -> Color(0xFFFEE2E2)
                                                isExpiringSoon -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFF0FDFA)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    isExpired -> "EXPIRED"
                                                    isExpiringSoon -> "Exp in ${med.daysUntilExpiry(now)}d"
                                                    else -> "Exp: ${dateFormat.format(Date(med.expiryDate))}"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isExpired -> Color(0xFFDC2626)
                                                    isExpiringSoon -> Color(0xFFD97706)
                                                    else -> Color(0xFF0F766E)
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Stock and Price Column
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Rs. ${med.salePrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0D9488)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            isOutOfStock -> Color(0xFFFEE2E2)
                                            isLowStock -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFDCFCE7)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                isOutOfStock -> "Out of Stock"
                                                isLowStock -> "${med.quantity} left (Low)"
                                                else -> "${med.quantity} in stock"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isOutOfStock -> Color(0xFFDC2626)
                                                isLowStock -> Color(0xFFD97706)
                                                else -> Color(0xFF15803D)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (inCartItem != null) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF0D9488)
                                        ) {
                                            Text(
                                                text = "${inCartItem.quantity} in bill",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message banner if any
        if (saleErrorMessage != null) {
            Surface(
                color = Color(0xFFDC2626),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(saleErrorMessage ?: "", color = Color.White, fontSize = 12.sp)
                    }
                    IconButton(onClick = { saleErrorMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = Color.White)
                    }
                }
            }
        }

        // Sticky Bottom Cart Bar & Expandable Drawer
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Cart Top Header (Always visible when cart has items)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCartExpanded = !isCartExpanded }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0D9488), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "$cartTotalCount Items in Bill",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isCartExpanded) "Tap to collapse" else "Tap to review & checkout",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PKR ${cartNetAmount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0D9488)
                                )
                                if (discount > 0) {
                                    Text(
                                        text = "Discount: Rs. ${discount.toInt()}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                            Icon(
                                if (isCartExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    // Expanded Cart Details
                    AnimatedVisibility(
                        visible = isCartExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Cart items list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                cart.forEachIndexed { index, cartItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cartItem.medicine.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Rack: ${cartItem.medicine.rackNumber} | B: ${cartItem.medicine.batchNumber}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = "Rs. ${cartItem.medicine.salePrice.toInt()} x ${cartItem.quantity} = Rs. ${cartItem.subtotal.toInt()}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0D9488)
                                            )
                                        }

                                        // Stepper: - and +
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFE2E8F0),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable {
                                                        if (cartItem.quantity > 1) {
                                                            cart[index] = cartItem.copy(quantity = cartItem.quantity - 1)
                                                        } else {
                                                            cart.removeAt(index)
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Text(
                                                text = "${cartItem.quantity}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )

                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFCCFBF1),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable {
                                                        if (cartItem.quantity < cartItem.medicine.quantity) {
                                                            cart[index] = cartItem.copy(quantity = cartItem.quantity + 1)
                                                        } else {
                                                            saleErrorMessage = "Stock limit reached (${cartItem.medicine.quantity} available)"
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF0D9488), modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            IconButton(
                                                onClick = { cart.removeAt(index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Customer & Prescriber details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Customer Name") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_sale_customer_name"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = { customerPhone = it },
                                    label = { Text("Phone") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_sale_customer_phone"),
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = doctorPrescriber,
                                    onValueChange = { doctorPrescriber = it },
                                    label = { Text("Prescribing Doctor (Optional)") },
                                    placeholder = { Text("e.g. Dr. Rashid") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_sale_doctor"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = discountStr,
                                    onValueChange = { discountStr = it },
                                    label = { Text("Discount (PKR)") },
                                    prefix = { Text("Rs. ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_sale_discount"),
                                    singleLine = true
                                )
                            }

                            // Payment Mode Chips
                            Text("Payment Mode", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Cash", "EasyPaisa / JazzCash", "Card / Bank").forEach { method ->
                                    FilterChip(
                                        selected = selectedPaymentMethod == method,
                                        onClick = { selectedPaymentMethod = method },
                                        label = { Text(method, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0D9488),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Checkout Button
                            Button(
                                onClick = {
                                    if (cart.isEmpty()) return@Button
                                    onCompleteSale(
                                        cart.toList(),
                                        customerName,
                                        customerPhone,
                                        doctorPrescriber,
                                        discount,
                                        selectedPaymentMethod,
                                        notes
                                    ) { savedSale ->
                                        completedSaleDialog = savedSale
                                        cart.clear()
                                        isCartExpanded = false
                                        customerName = "Walk-in Customer"
                                        customerPhone = ""
                                        doctorPrescriber = ""
                                        discountStr = ""
                                        notes = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_complete_pharmacy_sale"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Complete Sale (PKR ${cartNetAmount.toInt()}) & Deduct Stock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // CameraX Barcode & QR Scanner Dialog for Rapid Medicine Billing
        if (showBarcodeScanner) {
            val sampleMedicineBarcodes = remember(medicines) {
                medicines.take(6).map { it.name }
            }

            BarcodeScannerDialog(
                onBarcodeScanned = { barcode ->
                    addMedicineByBarcodeOrCode(barcode)
                },
                onDismiss = { showBarcodeScanner = false },
                sampleBarcodes = sampleMedicineBarcodes
            )
        }
    }
}
