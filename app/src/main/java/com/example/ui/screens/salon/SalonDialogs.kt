package com.example.ui.screens.salon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.SalonAppointmentEntity
import com.example.data.SalonServiceEntity
import com.example.data.StylistEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditServiceDialog(
    serviceToEdit: SalonServiceEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, price: Double, duration: Int, description: String) -> Unit
) {
    var name by remember { mutableStateOf(serviceToEdit?.name ?: "") }
    var category by remember { mutableStateOf(serviceToEdit?.category ?: "HAIR") }
    var priceText by remember { mutableStateOf(serviceToEdit?.price?.toInt()?.toString() ?: "2500") }
    var durationText by remember { mutableStateOf(serviceToEdit?.durationMinutes?.toString() ?: "45") }
    var description by remember { mutableStateOf(serviceToEdit?.description ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (serviceToEdit != null) "Edit Salon Service" else "Add Salon Service",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector Chips
                Text("Service Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("HAIR" to "Hair", "SKIN" to "Skin", "SPA" to "Spa").forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = category == catKey,
                            onClick = { category = catKey },
                            label = { Text(catLabel) },
                            leadingIcon = {
                                val icon = when (catKey) {
                                    "HAIR" -> Icons.Default.ContentCut
                                    "SKIN" -> Icons.Default.Face
                                    else -> Icons.Default.Spa
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Service Name *") },
                    placeholder = { Text("e.g. Keratin Therapy, HydraFacial") },
                    modifier = Modifier.fillMaxWidth().testTag("input_service_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_service_price"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (mins) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_service_duration"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Included treatments, products used...") },
                    modifier = Modifier.fillMaxWidth().testTag("input_service_description"),
                    minLines = 2,
                    maxLines = 4
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter service name"
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (price <= 0.0) {
                        errorText = "Please enter a valid price in PKR"
                        return@Button
                    }
                    val duration = durationText.toIntOrNull() ?: 30
                    onSave(name.trim(), category, price, duration, description.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_service")
            ) {
                Text(if (serviceToEdit != null) "Update" else "Save Service")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditStylistDialog(
    stylistToEdit: StylistEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, specialty: String, commissionPercentage: Double, isAvailable: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(stylistToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(stylistToEdit?.phone ?: "+92 300 ") }
    var specialty by remember { mutableStateOf(stylistToEdit?.specialty ?: "Hair Specialist & Colorist") }
    var commissionText by remember { mutableStateOf(stylistToEdit?.commissionPercentage?.toInt()?.toString() ?: "25") }
    var isAvailable by remember { mutableStateOf(stylistToEdit?.isAvailable ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val specialties = listOf(
        "Hair Specialist & Colorist",
        "Master Hair Stylist & Cuts",
        "Skin & Facial Expert",
        "Spa & Massage Therapist",
        "Bridal Makeup Artist",
        "Nail Art & Pedicure Specialist"
    )
    var expandedSpecialty by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (stylistToEdit != null) "Edit Stylist & Commission" else "Add Salon Stylist",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Stylist Name *") },
                    placeholder = { Text("e.g. Ayesha Malik") },
                    modifier = Modifier.fillMaxWidth().testTag("input_stylist_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("+92 300 1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_stylist_phone"),
                    singleLine = true
                )

                // Specialty Selector with Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Specialty / Designation *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedSpecialty = !expandedSpecialty }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_stylist_specialty"),
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = expandedSpecialty,
                        onDismissRequest = { expandedSpecialty = false }
                    ) {
                        specialties.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec) },
                                onClick = {
                                    specialty = spec
                                    expandedSpecialty = false
                                }
                            )
                        }
                    }
                }

                // Commission Percentage
                OutlinedTextField(
                    value = commissionText,
                    onValueChange = { commissionText = it },
                    label = { Text("Commission Rate (%) *") },
                    placeholder = { Text("e.g. 20, 25, 30") },
                    trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_stylist_commission"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Available for Bookings", fontSize = 14.sp)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PakEmeraldPrimary)
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter stylist name"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorText = "Please enter stylist phone number"
                        return@Button
                    }
                    val comm = commissionText.toDoubleOrNull() ?: 20.0
                    if (comm < 0.0 || comm > 100.0) {
                        errorText = "Commission must be between 0% and 100%"
                        return@Button
                    }
                    onSave(name.trim(), phone.trim(), specialty.trim(), comm, isAvailable)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_stylist")
            ) {
                Text(if (stylistToEdit != null) "Update" else "Save Stylist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookAppointmentDialog(
    services: List<SalonServiceEntity>,
    stylists: List<StylistEntity>,
    prefillService: SalonServiceEntity? = null,
    onDismiss: () -> Unit,
    onConfirmBooking: (
        clientName: String,
        clientPhone: String,
        service: SalonServiceEntity,
        stylist: StylistEntity,
        date: String,
        time: String,
        discount: Double,
        notes: String
    ) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("+92 3") }

    var selectedService by remember {
        mutableStateOf(prefillService ?: services.firstOrNull())
    }
    var selectedStylist by remember {
        mutableStateOf(stylists.firstOrNull { it.isAvailable } ?: stylists.firstOrNull())
    }

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var appointmentDate by remember { mutableStateOf(todayDate) }
    var appointmentTime by remember { mutableStateOf("03:00 PM") }
    var discountText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var expandedServiceDropdown by remember { mutableStateOf(false) }
    var expandedStylistDropdown by remember { mutableStateOf(false) }

    val servicePrice = selectedService?.price ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val finalPrice = (servicePrice - discount).coerceAtLeast(0.0)
    val commissionPercent = selectedStylist?.commissionPercentage ?: 20.0
    val commissionAmount = finalPrice * (commissionPercent / 100.0)

    val timeSlots = listOf(
        "10:00 AM", "11:30 AM", "01:00 PM", "02:30 PM", "04:00 PM", "05:30 PM", "07:00 PM", "08:30 PM"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = PakEmeraldPrimary)
                Text("Book Salon Appointment", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Client Info
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it; errorText = null },
                    label = { Text("Client Name *") },
                    placeholder = { Text("e.g. Fatima Noor") },
                    modifier = Modifier.fillMaxWidth().testTag("input_appt_client_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Client Phone *") },
                    placeholder = { Text("+92 300 1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_appt_client_phone"),
                    singleLine = true
                )

                // Service Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedService != null) "${selectedService!!.name} (${formatSalonPkr(selectedService!!.price)})" else "Select Service",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Treatment / Service *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedServiceDropdown = !expandedServiceDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedServiceDropdown = true }
                    )
                    DropdownMenu(
                        expanded = expandedServiceDropdown,
                        onDismissRequest = { expandedServiceDropdown = false }
                    ) {
                        services.forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${s.category} • ${formatSalonPkr(s.price)} • ${s.durationMinutes} min", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedService = s
                                    expandedServiceDropdown = false
                                }
                            )
                        }
                    }
                }

                // Stylist Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedStylist != null) "${selectedStylist!!.name} (${selectedStylist!!.commissionPercentage.toInt()}% comm.)" else "Select Stylist",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Stylist *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedStylistDropdown = !expandedStylistDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedStylistDropdown = true }
                    )
                    DropdownMenu(
                        expanded = expandedStylistDropdown,
                        onDismissRequest = { expandedStylistDropdown = false }
                    ) {
                        stylists.forEach { st ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(st.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${st.specialty} • ${st.commissionPercentage.toInt()}% Commission", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedStylist = st
                                    expandedStylistDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = appointmentDate,
                        onValueChange = { appointmentDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = appointmentTime,
                        onValueChange = { appointmentTime = it },
                        label = { Text("Time Slot") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Quick Time Slots
                Text("Suggested Slots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("11:30 AM", "02:30 PM", "04:30 PM", "06:00 PM").forEach { slot ->
                        FilterChip(
                            selected = appointmentTime == slot,
                            onClick = { appointmentTime = slot },
                            label = { Text(slot, fontSize = 10.sp) }
                        )
                    }
                }

                // Discount Input
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Financial Summary Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service Price:", fontSize = 12.sp)
                            Text(formatSalonPkr(servicePrice), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount:", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                Text("-${formatSalonPkr(discount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Final Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(formatSalonPkr(finalPrice), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PakEmeraldPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stylist Commission (${commissionPercent.toInt()}%):", fontSize = 12.sp, color = PakGoldSecondary)
                            Text(formatSalonPkr(commissionAmount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PakGoldSecondary)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Preferences") },
                    placeholder = { Text("e.g. Skin sensitivity, specific toner") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientName.isBlank()) {
                        errorText = "Please enter client name"
                        return@Button
                    }
                    if (clientPhone.isBlank()) {
                        errorText = "Please enter client phone number"
                        return@Button
                    }
                    if (selectedService == null) {
                        errorText = "Please select a service"
                        return@Button
                    }
                    if (selectedStylist == null) {
                        errorText = "Please assign a stylist"
                        return@Button
                    }
                    onConfirmBooking(
                        clientName.trim(),
                        clientPhone.trim(),
                        selectedService!!,
                        selectedStylist!!,
                        appointmentDate.trim(),
                        appointmentTime.trim(),
                        discount,
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_book_appointment")
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettleAppointmentDialog(
    appointment: SalonAppointmentEntity,
    onDismiss: () -> Unit,
    onConfirmSettle: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PakEmeraldPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Complete & Settle Service",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Confirm completion of service for ${appointment.clientName}. This marks payment as collected and credits the stylist's commission.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service:", fontSize = 12.sp)
                            Text(appointment.serviceName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client Paid:", fontSize = 12.sp)
                            Text(formatSalonPkr(appointment.finalPrice), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PakEmeraldPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Credited to ${appointment.stylistName}:", fontSize = 12.sp)
                            Text(formatSalonPkr(appointment.commissionAmount), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PakGoldSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSettle,
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_settle_payout")
            ) {
                Text("Confirm & Settle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
