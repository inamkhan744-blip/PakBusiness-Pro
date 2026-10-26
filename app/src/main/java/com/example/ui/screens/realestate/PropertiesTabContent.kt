package com.example.ui.screens.realestate

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun PropertiesTabContent(
    properties: List<PropertyEntity>,
    onAddProperty: () -> Unit,
    onEditProperty: (PropertyEntity) -> Unit,
    onDeleteProperty: (Long) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onScheduleVisitForProperty: (PropertyEntity) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedPurposeFilter by remember { mutableStateOf("ALL") } // ALL, SALE, RENT
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, HOUSE, PLOT, FLAT, COMMERCIAL
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, AVAILABLE, UNDER_OFFER, SOLD, RENTED

    val filteredProperties = remember(properties, searchQuery, selectedPurposeFilter, selectedTypeFilter, selectedStatusFilter) {
        properties.filter { p ->
            val matchSearch = searchQuery.isBlank() ||
                    p.title.contains(searchQuery, ignoreCase = true) ||
                    p.location.contains(searchQuery, ignoreCase = true) ||
                    p.city.contains(searchQuery, ignoreCase = true) ||
                    p.size.contains(searchQuery, ignoreCase = true)
            val matchPurpose = selectedPurposeFilter == "ALL" || p.purpose.equals(selectedPurposeFilter, ignoreCase = true)
            val matchType = selectedTypeFilter == "ALL" || p.propertyType.equals(selectedTypeFilter, ignoreCase = true)
            val matchStatus = selectedStatusFilter == "ALL" || p.status.equals(selectedStatusFilter, ignoreCase = true)
            matchSearch && matchPurpose && matchType && matchStatus
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search properties, location, city, size...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_properties"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PakEmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Purpose Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val purposes = listOf(
                        "ALL" to "All Listings",
                        "SALE" to "For Sale",
                        "RENT" to "For Rent"
                    )
                    items(purposes) { (key, label) ->
                        FilterChip(
                            selected = selectedPurposeFilter == key,
                            onClick = { selectedPurposeFilter = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = if (selectedPurposeFilter == key) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }

                    item {
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    val types = listOf(
                        "ALL" to "All Types",
                        "HOUSE" to "House/Villa",
                        "PLOT" to "Plot",
                        "FLAT" to "Flat/Apartment",
                        "COMMERCIAL" to "Commercial"
                    )
                    items(types) { (key, label) ->
                        FilterChip(
                            selected = selectedTypeFilter == key,
                            onClick = { selectedTypeFilter = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Listings List
        if (filteredProperties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedPurposeFilter != "ALL" || selectedTypeFilter != "ALL") {
                            "No properties match your filter"
                        } else {
                            "No properties added yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add your first real estate listing for sale or rent with pricing, size, and location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onAddProperty,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_empty_add_property")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Property Listing")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredProperties.size} Properties Found",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val forSaleCount = filteredProperties.count { it.purpose == "SALE" }
                            val forRentCount = filteredProperties.count { it.purpose == "RENT" }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PakEmeraldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "$forSaleCount Sale",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E40AF).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "$forRentCount Rent",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                items(filteredProperties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onEdit = { onEditProperty(property) },
                        onDelete = { onDeleteProperty(property.id) },
                        onScheduleVisit = { onScheduleVisitForProperty(property) },
                        onUpdateStatus = { newStatus -> onUpdateStatus(property.id, newStatus) },
                        onCallOwner = {
                            if (property.ownerPhone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.ownerPhone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyCard(
    property: PropertyEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onScheduleVisit: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onCallOwner: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isSale = property.purpose.equals("SALE", ignoreCase = true)
    val purposeColor = if (isSale) PakEmeraldPrimary else Color(0xFF2563EB)
    val purposeBg = if (isSale) PakEmeraldPrimary.copy(alpha = 0.12f) else Color(0xFF2563EB).copy(alpha = 0.12f)

    val statusColor = when (property.status.uppercase()) {
        "AVAILABLE" -> Color(0xFF059669)
        "UNDER_OFFER" -> PakGoldSecondary
        "SOLD" -> Color(0xFF6B7280)
        "RENTED" -> Color(0xFF6B7280)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusBg = when (property.status.uppercase()) {
        "AVAILABLE" -> Color(0xFFD1FAE5)
        "UNDER_OFFER" -> Color(0xFFFEF3C7)
        "SOLD", "RENTED" -> Color(0xFFF3F4F6)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (property.isFeatured) 1.5.dp else 1.dp,
            color = if (property.isFeatured) PakGoldSecondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("property_card_${property.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Badges & Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purpose Badge (Sale / Rent)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = purposeBg
                    ) {
                        Text(
                            text = if (isSale) "FOR SALE" else "FOR RENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = purposeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Status Badge (Clickable to change)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusBg,
                            modifier = Modifier.clickable { showStatusMenu = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = property.status.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = statusColor
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            val nextStatuses = if (isSale) {
                                listOf("AVAILABLE", "UNDER_OFFER", "SOLD")
                            } else {
                                listOf("AVAILABLE", "UNDER_OFFER", "RENTED")
                            }
                            nextStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.replace("_", " ")) },
                                    onClick = {
                                        onUpdateStatus(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (property.isFeatured) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PakGoldSecondary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = PakGoldSecondary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "FEATURED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakGoldSecondary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Property", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Property", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Highlight
            Text(
                text = RealEstateFormatters.formatPkrCombined(property.price, property.purpose),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = purposeColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = property.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Location with Pin
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${property.location}, ${property.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Features Row (Size, Beds, Baths, Property Type)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Size Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.SquareFoot, contentDescription = null, modifier = Modifier.size(14.dp), tint = PakEmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = property.size, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Property Type Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, modifier = Modifier.size(14.dp), tint = PakGoldSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = property.propertyType, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Beds & Baths (if residential)
                if (property.bedrooms > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${property.bedrooms} Bed", fontSize = 12.sp)
                        }
                    }
                }

                if (property.bathrooms > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Bathtub, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${property.bathrooms} Bath", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (property.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Owner Info & Schedule Visit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (property.ownerName.isNotBlank() || property.ownerPhone.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCallOwner() }
                    ) {
                        Text(
                            text = if (isSale) "Owner: ${property.ownerName}" else "Landlord: ${property.ownerName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (property.ownerPhone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = PakEmeraldPrimary)
                                Text(
                                    text = property.ownerPhone,
                                    fontSize = 11.sp,
                                    color = PakEmeraldPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = onScheduleVisit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_schedule_visit_${property.id}")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Book Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Property Listing?") },
            text = { Text("Are you sure you want to delete \"${property.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
