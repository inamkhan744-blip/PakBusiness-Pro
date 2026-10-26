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
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

data class SupplierSummary(
    val name: String,
    val phone: String,
    val invoiceRefs: Set<String>,
    val medicines: List<MedicineEntity>
)

data class RackSummary(
    val rackName: String,
    val medicines: List<MedicineEntity>,
    val totalStockUnits: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuppliersAndRacksView(
    medicines: List<MedicineEntity>
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Suppliers") } // "Suppliers" or "Racks"
    var searchQuery by remember { mutableStateOf("") }

    // Grouping by Supplier
    val suppliersList = remember(medicines) {
        medicines.filter { it.supplierName.isNotBlank() }
            .groupBy { it.supplierName.trim() }
            .map { (supplierName, meds) ->
                val phone = meds.firstOrNull { it.supplierPhone.isNotBlank() }?.supplierPhone ?: ""
                val invoices = meds.mapNotNull { it.supplierInvoiceRef.takeIf { ref -> ref.isNotBlank() } }.toSet()
                SupplierSummary(
                    name = supplierName,
                    phone = phone,
                    invoiceRefs = invoices,
                    medicines = meds
                )
            }
            .sortedBy { it.name }
    }

    // Grouping by Rack / Shelf
    val racksList = remember(medicines) {
        medicines.groupBy { if (it.rackNumber.isBlank()) "Unassigned Rack" else it.rackNumber.trim() }
            .map { (rack, meds) ->
                RackSummary(
                    rackName = rack,
                    medicines = meds,
                    totalStockUnits = meds.sumOf { it.quantity }
                )
            }
            .sortedBy { it.rackName }
    }

    val filteredSuppliers = remember(suppliersList, searchQuery) {
        if (searchQuery.isBlank()) suppliersList
        else suppliersList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    it.medicines.any { med -> med.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredRacks = remember(racksList, searchQuery) {
        if (searchQuery.isBlank()) racksList
        else racksList.filter {
            it.rackName.contains(searchQuery, ignoreCase = true) ||
                    it.medicines.any { med -> med.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Section Switcher: Suppliers vs Racks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSection == "Suppliers",
                onClick = { selectedSection = "Suppliers" },
                leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("Suppliers & Distributors (${suppliersList.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0D9488),
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedSection == "Racks",
                onClick = { selectedSection = "Racks" },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("Racks & Shelves (${racksList.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0D9488),
                    selectedLabelColor = Color.White
                )
            )
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(if (selectedSection == "Suppliers") "Search supplier or supplied medicine..." else "Search rack number or located medicine...")
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0D9488)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D9488),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }

        // List display
        if (selectedSection == "Suppliers") {
            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No supplier details found", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSuppliers, key = { it.name }) { sup ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color(0xFFCCFBF1), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text("${sup.medicines.size} medicines supplied", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    if (sup.phone.isNotBlank()) {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sup.phone}"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (sup.phone.isNotBlank() || sup.invoiceRefs.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (sup.phone.isNotBlank()) {
                                            Text("Ph: ${sup.phone}", fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                                        }
                                        if (sup.invoiceRefs.isNotEmpty()) {
                                            Text("Invoices: ${sup.invoiceRefs.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Text("Supplied Medicines:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    sup.medicines.forEach { med ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = "${med.name} (${med.quantity} in stock)",
                                                fontSize = 10.sp,
                                                color = Color(0xFF1E293B),
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
        } else {
            // Racks Section
            if (filteredRacks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rack details found", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRacks, key = { it.rackName }) { rack ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color(0xFFEFF6FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(rack.rackName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text("${rack.medicines.size} medicine varieties", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDBEAFE)
                                    ) {
                                        Text(
                                            text = "${rack.totalStockUnits} units stored",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E40AF),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Text("Medicines on this shelf:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rack.medicines.forEach { med ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(med.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                                Text("Batch: ${med.batchNumber}", fontSize = 9.sp, color = Color(0xFF64748B))
                                            }
                                            Text("${med.quantity} units", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
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
}
