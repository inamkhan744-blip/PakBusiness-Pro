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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpiryAndStockAlertsView(
    medicines: List<MedicineEntity>,
    onEditMedicine: (MedicineEntity) -> Unit,
    onUpdateStock: (Long, Int) -> Unit
) {
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    var selectedFilter by remember { mutableStateOf("All Alerts") }

    val expiredMedicines = remember(medicines) { medicines.filter { it.isExpired(now) } }
    val expiringSoonMedicines = remember(medicines) { medicines.filter { it.isExpiringSoon(30, now) } }
    val lowStockMedicines = remember(medicines) { medicines.filter { it.isLowStock() } }

    val alertMedicines = remember(medicines, selectedFilter) {
        when (selectedFilter) {
            "Expired" -> expiredMedicines
            "Expiring Soon" -> expiringSoonMedicines
            "Low Stock" -> lowStockMedicines
            else -> medicines.filter { it.isExpired(now) || it.isExpiringSoon(30, now) || it.isLowStock() }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary KPI Banner
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Medical Store Health & Safety Monitor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Track expiring batches for supplier returns and low inventory to prevent stockouts.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Expired Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = "Expired" },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                Text("Expired", fontSize = 11.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                            }
                            Text("${expiredMedicines.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                            Text("Remove from shelf", fontSize = 9.sp, color = Color(0xFFB91C1C))
                        }
                    }

                    // Expiring in 30 Days Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = "Expiring Soon" },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                Text("Exp < 30 Days", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                            }
                            Text("${expiringSoonMedicines.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                            Text("Return or discount", fontSize = 9.sp, color = Color(0xFFB45309))
                        }
                    }

                    // Low Stock Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = "Low Stock" },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                                Text("Low Stock", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                            }
                            Text("${lowStockMedicines.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                            Text("Reorder soon", fontSize = 9.sp, color = Color(0xFF1D4ED8))
                        }
                    }
                }
            }
        }

        // Filter chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "All Alerts (${expiredMedicines.size + expiringSoonMedicines.size + lowStockMedicines.size})",
                    "Expired (${expiredMedicines.size})",
                    "Expiring Soon (${expiringSoonMedicines.size})",
                    "Low Stock (${lowStockMedicines.size})"
                ).forEach { filterLabel ->
                    val filterKey = when {
                        filterLabel.startsWith("Expired") -> "Expired"
                        filterLabel.startsWith("Expiring") -> "Expiring Soon"
                        filterLabel.startsWith("Low") -> "Low Stock"
                        else -> "All Alerts"
                    }
                    FilterChip(
                        selected = selectedFilter == filterKey,
                        onClick = { selectedFilter = filterKey },
                        label = { Text(filterLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (filterKey) {
                                "Expired" -> Color(0xFFDC2626)
                                "Expiring Soon" -> Color(0xFFD97706)
                                "Low Stock" -> Color(0xFF2563EB)
                                else -> Color(0xFF0D9488)
                            },
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Empty state
        if (alertMedicines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
                        Text("All Good! No Alerts Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF166534))
                        Text(
                            "All medicine stock levels are above threshold and no batches are expiring within 30 days.",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(alertMedicines, key = { it.id }) { med ->
                val isExpired = med.isExpired(now)
                val isExpiringSoon = med.isExpiringSoon(30, now)
                val isLowStock = med.isLowStock()

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isExpired -> Color(0xFFFEF2F2)
                            isExpiringSoon -> Color(0xFFFFFBEB)
                            else -> Color.White
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isExpired -> Color(0xFFF87171)
                            isExpiringSoon -> Color(0xFFFCD34D)
                            else -> Color(0xFFBFDBFE)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_alert_${med.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Alert Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isExpired) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDC2626)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "EXPIRED (${dateFormat.format(Date(med.expiryDate))})",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isExpiringSoon) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD97706)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "EXPIRING IN ${med.daysUntilExpiry(now)} DAYS",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (isLowStock) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (med.quantity <= 0) Color(0xFFDC2626) else Color(0xFF2563EB)
                                ) {
                                    Text(
                                        text = if (med.quantity <= 0) "OUT OF STOCK" else "LOW STOCK (${med.quantity} left)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Medicine Name & Active Salt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = med.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (med.genericName.isNotBlank()) {
                                    Text(
                                        text = "Active Formula: ${med.genericName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = "Rs. ${med.salePrice.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0D9488)
                            )
                        }

                        // Rack and Batch Tracking Details
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Physical Location: ${med.rackNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Text(
                                    text = "Batch: ${med.batchNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        // Supplier Details & Quick Contact
                        if (med.supplierName.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Distributor: ${med.supplierName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    if (med.supplierInvoiceRef.isNotBlank()) {
                                        Text(
                                            text = "Inv / Challan: ${med.supplierInvoiceRef}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (med.supplierPhone.isNotBlank()) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${med.supplierPhone}"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call Supplier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // Action Buttons: Edit medicine & Quick Dispose / Stock update
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isExpired) {
                                OutlinedButton(
                                    onClick = {
                                        // Dispose: set stock to 0
                                        onUpdateStock(med.id, 0)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Text("Quarantine / Zero Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { onEditMedicine(med) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Batch / Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
