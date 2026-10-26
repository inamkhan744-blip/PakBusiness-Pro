package com.example.ui.screens.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LeadEntity
import com.example.data.PropertyEntity
import com.example.data.SiteVisitEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPropertyDialog(
    initialProperty: PropertyEntity? = null,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (PropertyEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialProperty?.title ?: "") }
    var purpose by remember { mutableStateOf(initialProperty?.purpose ?: "SALE") }
    var propertyType by remember { mutableStateOf(initialProperty?.propertyType ?: "HOUSE") }
    var priceText by remember { mutableStateOf(initialProperty?.price?.toLong()?.toString() ?: "") }
    var size by remember { mutableStateOf(initialProperty?.size ?: "10 Marla") }
    var location by remember { mutableStateOf(initialProperty?.location ?: "") }
    var city by remember { mutableStateOf(initialProperty?.city ?: "Lahore") }
    var bedrooms by remember { mutableStateOf(initialProperty?.bedrooms ?: if (propertyType in listOf("HOUSE", "FLAT")) 3 else 0) }
    var bathrooms by remember { mutableStateOf(initialProperty?.bathrooms ?: if (propertyType in listOf("HOUSE", "FLAT")) 3 else 0) }
    var ownerName by remember { mutableStateOf(initialProperty?.ownerName ?: "") }
    var ownerPhone by remember { mutableStateOf(initialProperty?.ownerPhone ?: "") }
    var description by remember { mutableStateOf(initialProperty?.description ?: "") }
    var isFeatured by remember { mutableStateOf(initialProperty?.isFeatured ?: false) }
    var status by remember { mutableStateOf(initialProperty?.status ?: "AVAILABLE") }

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProperty == null) "Add Property Listing" else "Edit Property Listing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Purpose Selector (Sale / Rent)
                    Text("Listing Purpose", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isSale = purpose == "SALE"
                        Button(
                            onClick = { purpose = "SALE" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSale) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSale) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("For Sale", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { purpose = "RENT" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSale) Color(0xFF2563EB) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isSale) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("For Rent", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Property Type Chips
                    Text("Property Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val types = listOf("HOUSE", "PLOT", "FLAT", "COMMERCIAL", "FARMHOUSE")
                        items(types) { type ->
                            FilterChip(
                                selected = propertyType == type,
                                onClick = { propertyType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    // Property Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Property Title *") },
                        placeholder = { Text("e.g., 10 Marla Designer Modern Villa") },
                        modifier = Modifier.fillMaxWidth().testTag("input_property_title"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Price in PKR with live Pakistani notation preview
                    Column {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Price (PKR) *") },
                            placeholder = { Text(if (purpose == "SALE") "e.g., 45000000" else "e.g., 85000") },
                            leadingIcon = { Text("PKR", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PakEmeraldPrimary, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_property_price"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        if (parsedPrice > 0) {
                            Text(
                                text = "Preview: ${RealEstateFormatters.formatPkrCombined(parsedPrice, purpose)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    // Size input with suggestion chips
                    Column {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Size / Area *") },
                            placeholder = { Text("e.g. 10 Marla, 1 Kanal, 1800 Sq Ft") },
                            modifier = Modifier.fillMaxWidth().testTag("input_property_size"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val sizes = listOf("5 Marla", "10 Marla", "1 Kanal", "2 Kanal", "120 Sq Yards", "240 Sq Yards", "1500 Sq Ft")
                            items(sizes) { s ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { size = s }
                                ) {
                                    Text(
                                        text = s,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Location & City
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Sector *") },
                            placeholder = { Text("e.g. DHA Phase 6, Block D") },
                            modifier = Modifier.weight(1.5f).testTag("input_property_location"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City *") },
                            placeholder = { Text("Lahore") },
                            modifier = Modifier.weight(1f).testTag("input_property_city"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Quick City selection chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val cities = listOf("Lahore", "Islamabad", "Rawalpindi", "Karachi", "Faisalabad", "Multan", "Peshawar")
                        items(cities) { c ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (city == c) PakEmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { city = c }
                            ) {
                                Text(
                                    text = c,
                                    fontSize = 11.sp,
                                    fontWeight = if (city == c) FontWeight.Bold else FontWeight.Normal,
                                    color = if (city == c) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Beds & Baths (for residential)
                    if (propertyType in listOf("HOUSE", "FLAT", "FARMHOUSE")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bedrooms: $bedrooms", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { if (bedrooms > 0) bedrooms-- }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(text = "$bedrooms", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    IconButton(onClick = { bedrooms++ }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bathrooms: $bathrooms", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { if (bathrooms > 0) bathrooms-- }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(text = "$bathrooms", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    IconButton(onClick = { bathrooms++ }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }

                    // Owner / Contact Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner / Landlord Name") },
                            placeholder = { Text("e.g. Malik Tariq") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = ownerPhone,
                            onValueChange = { ownerPhone = it },
                            label = { Text("Owner Phone") },
                            placeholder = { Text("0300-1234567") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Key Features & Description") },
                        placeholder = { Text("Solid construction, solar installed, close to commercial market, clear documents...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Featured toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Mark as Featured Listing", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Displays golden badge in property list", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isFeatured,
                            onCheckedChange = { isFeatured = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PakGoldSecondary, checkedTrackColor = PakGoldSecondary.copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && location.isNotBlank()) {
                                onSave(
                                    PropertyEntity(
                                        id = initialProperty?.id ?: 0L,
                                        businessId = businessId,
                                        title = title.trim(),
                                        purpose = purpose,
                                        propertyType = propertyType,
                                        price = parsedPrice,
                                        location = location.trim(),
                                        city = city.trim(),
                                        size = size.trim(),
                                        bedrooms = bedrooms,
                                        bathrooms = bathrooms,
                                        status = status,
                                        ownerName = ownerName.trim(),
                                        ownerPhone = ownerPhone.trim(),
                                        description = description.trim(),
                                        isFeatured = isFeatured,
                                        createdAt = initialProperty?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && location.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_property")
                    ) {
                        Text(if (initialProperty == null) "Add Property" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditLeadDialog(
    initialLead: LeadEntity? = null,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (LeadEntity) -> Unit
) {
    var clientName by remember { mutableStateOf(initialLead?.clientName ?: "") }
    var phone by remember { mutableStateOf(initialLead?.phone ?: "") }
    var email by remember { mutableStateOf(initialLead?.email ?: "") }
    var requirementType by remember { mutableStateOf(initialLead?.requirementType ?: "BUY") }
    var preferredPropertyType by remember { mutableStateOf(initialLead?.preferredPropertyType ?: "HOUSE") }
    var preferredLocation by remember { mutableStateOf(initialLead?.preferredLocation ?: "") }
    var preferredSize by remember { mutableStateOf(initialLead?.preferredSize ?: "10 Marla") }
    var minBudgetText by remember { mutableStateOf(initialLead?.minBudget?.toLong()?.takeIf { it > 0 }?.toString() ?: "") }
    var maxBudgetText by remember { mutableStateOf(initialLead?.maxBudget?.toLong()?.takeIf { it > 0 }?.toString() ?: "") }
    var status by remember { mutableStateOf(initialLead?.status ?: "NEW") }
    var assignedAgent by remember { mutableStateOf(initialLead?.assignedAgent ?: "") }
    var notes by remember { mutableStateOf(initialLead?.notes ?: "") }

    val parsedMinBudget = minBudgetText.toDoubleOrNull() ?: 0.0
    val parsedMaxBudget = maxBudgetText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialLead == null) "Add Client Lead" else "Edit Client Lead",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Requirement Selector
                    Text("Client Requirement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val reqs = listOf("BUY" to "Buyer", "RENT" to "Tenant", "INVEST" to "Investor")
                        reqs.forEach { (key, label) ->
                            FilterChip(
                                selected = requirementType == key,
                                onClick = { requirementType = key },
                                label = { Text(label, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Client Name & Phone
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Full Name *") },
                        placeholder = { Text("e.g., Engr. Usman Qureshi") },
                        modifier = Modifier.fillMaxWidth().testTag("input_lead_client_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number *") },
                            placeholder = { Text("0300-1234567") },
                            modifier = Modifier.weight(1.2f).testTag("input_lead_phone"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email (Optional)") },
                            placeholder = { Text("client@mail.com") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Budget Inputs in PKR (Key user requirement)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldPrimary.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Client Budget Range (PKR) *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = minBudgetText,
                                    onValueChange = { minBudgetText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Min Budget (PKR)") },
                                    placeholder = { Text("30000000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_lead_min_budget"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = maxBudgetText,
                                    onValueChange = { maxBudgetText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Max Budget (PKR) *") },
                                    placeholder = { Text("45000000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_lead_max_budget"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            if (parsedMinBudget > 0 || parsedMaxBudget > 0) {
                                Text(
                                    text = "Budget: ${RealEstateFormatters.formatBudgetRange(parsedMinBudget, parsedMaxBudget)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakEmeraldPrimary
                                )
                            }
                        }
                    }

                    // Preferred Property Type & Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = preferredPropertyType,
                            onValueChange = { preferredPropertyType = it },
                            label = { Text("Preferred Property") },
                            placeholder = { Text("HOUSE, PLOT, FLAT") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = preferredSize,
                            onValueChange = { preferredSize = it },
                            label = { Text("Preferred Size") },
                            placeholder = { Text("10 Marla / 1 Kanal") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Target Location
                    OutlinedTextField(
                        value = preferredLocation,
                        onValueChange = { preferredLocation = it },
                        label = { Text("Target Areas / Societies") },
                        placeholder = { Text("e.g., DHA Lahore Phase 5 or 6, Bahria") },
                        modifier = Modifier.fillMaxWidth().testTag("input_lead_preferred_location"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Lead Status & Assigned Agent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = { status = it },
                            label = { Text("Pipeline Status") },
                            placeholder = { Text("NEW, SITE_VISIT") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = assignedAgent,
                            onValueChange = { assignedAgent = it },
                            label = { Text("Assigned Agent") },
                            placeholder = { Text("Ali Raza") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Requirements & Notes") },
                        placeholder = { Text("Family of 5, wants ready to move house, cash ready, wants 2 car garage...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && phone.isNotBlank()) {
                                onSave(
                                    LeadEntity(
                                        id = initialLead?.id ?: 0L,
                                        businessId = businessId,
                                        clientName = clientName.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        requirementType = requirementType,
                                        preferredPropertyType = preferredPropertyType.trim(),
                                        preferredLocation = preferredLocation.trim(),
                                        preferredSize = preferredSize.trim(),
                                        minBudget = parsedMinBudget,
                                        maxBudget = parsedMaxBudget,
                                        status = status,
                                        notes = notes.trim(),
                                        assignedAgent = assignedAgent.trim(),
                                        createdAt = initialLead?.createdAt ?: System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = clientName.isNotBlank() && phone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_lead")
                    ) {
                        Text(if (initialLead == null) "Add Lead" else "Save Lead", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleSiteVisitDialog(
    initialVisit: SiteVisitEntity? = null,
    prefillProperty: PropertyEntity? = null,
    prefillLead: LeadEntity? = null,
    availableProperties: List<PropertyEntity>,
    availableLeads: List<LeadEntity>,
    businessId: Long,
    onDismiss: () -> Unit,
    onSave: (SiteVisitEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val defaultDate = remember { dateFormat.format(Date(System.currentTimeMillis() + 86400000L)) }

    var selectedPropertyId by remember {
        mutableStateOf(initialVisit?.propertyId ?: prefillProperty?.id ?: availableProperties.firstOrNull()?.id ?: 0L)
    }
    var propertyTitle by remember {
        mutableStateOf(initialVisit?.propertyTitle ?: prefillProperty?.title ?: availableProperties.firstOrNull()?.title ?: "")
    }
    var propertyLocation by remember {
        mutableStateOf(initialVisit?.propertyLocation ?: prefillProperty?.location ?: availableProperties.firstOrNull()?.location ?: "")
    }

    var selectedLeadId by remember { mutableStateOf<Long?>(initialVisit?.leadId ?: prefillLead?.id) }
    var clientName by remember {
        mutableStateOf(initialVisit?.clientName ?: prefillLead?.clientName ?: "")
    }
    var clientPhone by remember {
        mutableStateOf(initialVisit?.clientPhone ?: prefillLead?.phone ?: "")
    }

    var visitDateString by remember { mutableStateOf(initialVisit?.visitDateString ?: defaultDate) }
    var visitTimeString by remember { mutableStateOf(initialVisit?.visitTimeString ?: "04:30 PM") }
    var agentName by remember { mutableStateOf(initialVisit?.agentName ?: prefillLead?.assignedAgent ?: "Consultant") }
    var clientFeedback by remember { mutableStateOf(initialVisit?.clientFeedback ?: "") }

    var showPropertyMenu by remember { mutableStateOf(false) }
    var showLeadMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialVisit == null) "Schedule Site Visit" else "Edit Site Visit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Property Selection
                    Text("Select Property for Visit *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box {
                        OutlinedTextField(
                            value = propertyTitle,
                            onValueChange = { propertyTitle = it },
                            label = { Text("Property Title") },
                            placeholder = { Text("Select from properties or enter title") },
                            trailingIcon = {
                                IconButton(onClick = { showPropertyMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select property")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("input_visit_property_title"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        DropdownMenu(
                            expanded = showPropertyMenu,
                            onDismissRequest = { showPropertyMenu = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            availableProperties.forEach { prop ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${prop.size} • ${prop.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedPropertyId = prop.id
                                        propertyTitle = prop.title
                                        propertyLocation = "${prop.location}, ${prop.city}"
                                        showPropertyMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = propertyLocation,
                        onValueChange = { propertyLocation = it },
                        label = { Text("Property Location") },
                        placeholder = { Text("Phase 6, DHA Lahore") },
                        modifier = Modifier.fillMaxWidth().testTag("input_visit_property_location"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Client Selection
                    Text("Client Details *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Client Name *") },
                            placeholder = { Text("Select from leads or type client name") },
                            trailingIcon = {
                                if (availableLeads.isNotEmpty()) {
                                    IconButton(onClick = { showLeadMenu = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select lead")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("input_visit_client_name"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        DropdownMenu(
                            expanded = showLeadMenu,
                            onDismissRequest = { showLeadMenu = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            availableLeads.forEach { lead ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(lead.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${lead.phone} • Budget: ${RealEstateFormatters.formatBudgetRange(lead.minBudget, lead.maxBudget)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedLeadId = lead.id
                                        clientName = lead.clientName
                                        clientPhone = lead.phone
                                        if (lead.assignedAgent.isNotBlank()) agentName = lead.assignedAgent
                                        showLeadMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Client Phone *") },
                        placeholder = { Text("0300-1234567") },
                        modifier = Modifier.fillMaxWidth().testTag("input_visit_client_phone"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Date & Time Scheduling
                    Text("Visit Schedule Timing *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = visitDateString,
                            onValueChange = { visitDateString = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            placeholder = { Text("2026-09-22") },
                            modifier = Modifier.weight(1f).testTag("input_visit_date"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = visitTimeString,
                            onValueChange = { visitTimeString = it },
                            label = { Text("Time") },
                            placeholder = { Text("04:30 PM") },
                            modifier = Modifier.weight(1f).testTag("input_visit_time"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Conducting Agent
                    OutlinedTextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text("Conducting Real Estate Agent") },
                        placeholder = { Text("Hamza Khan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Feedback / Preparation Notes
                    OutlinedTextField(
                        value = clientFeedback,
                        onValueChange = { clientFeedback = it },
                        label = { Text("Visit Notes / Instructions") },
                        placeholder = { Text("Owner will hand over keys at gate, client bringing family for inspection...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (propertyTitle.isNotBlank() && clientName.isNotBlank()) {
                                onSave(
                                    SiteVisitEntity(
                                        id = initialVisit?.id ?: 0L,
                                        businessId = businessId,
                                        leadId = selectedLeadId,
                                        clientName = clientName.trim(),
                                        clientPhone = clientPhone.trim(),
                                        propertyId = selectedPropertyId,
                                        propertyTitle = propertyTitle.trim(),
                                        propertyLocation = propertyLocation.trim(),
                                        visitDateTime = initialVisit?.visitDateTime ?: System.currentTimeMillis(),
                                        visitDateString = visitDateString.trim(),
                                        visitTimeString = visitTimeString.trim(),
                                        agentName = agentName.trim(),
                                        status = initialVisit?.status ?: "SCHEDULED",
                                        clientFeedback = clientFeedback.trim(),
                                        createdAt = initialVisit?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = propertyTitle.isNotBlank() && clientName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_visit")
                    ) {
                        Text(if (initialVisit == null) "Confirm Schedule" else "Update Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordVisitFeedbackDialog(
    visit: SiteVisitEntity,
    onDismiss: () -> Unit,
    onConfirm: (feedback: String, status: String) -> Unit
) {
    var feedback by remember { mutableStateOf(visit.clientFeedback) }
    var selectedStatus by remember { mutableStateOf("COMPLETED") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Record Site Visit Outcome",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PakEmeraldPrimary
                )

                Text(
                    text = "Visit with ${visit.clientName} for ${visit.propertyTitle}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedStatus == "COMPLETED",
                        onClick = { selectedStatus = "COMPLETED" },
                        label = { Text("Completed") }
                    )
                    FilterChip(
                        selected = selectedStatus == "CANCELLED",
                        onClick = { selectedStatus = "CANCELLED" },
                        label = { Text("Cancelled") }
                    )
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Client Feedback & Outcome Notes") },
                    placeholder = { Text("e.g. Liked modern elevation, requested 5% discount, negotiating with owner...") },
                    modifier = Modifier.fillMaxWidth().testTag("input_visit_feedback"),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick suggestions
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val quickNotes = listOf(
                        "Interested, submitting token offer",
                        "Loved the layout, discussing price",
                        "Too far from main road",
                        "Requested 2nd visit with family",
                        "Deal finalized successfully"
                    )
                    items(quickNotes) { note ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { feedback = note }
                        ) {
                            Text(
                                text = note,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(feedback.trim(), selectedStatus)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_confirm_feedback")
                    ) {
                        Text("Save Outcome", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
