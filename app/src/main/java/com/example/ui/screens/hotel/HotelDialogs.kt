package com.example.ui.screens.hotel

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
import com.example.data.HotelBookingEntity
import com.example.data.HotelRoomEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddEditRoomDialog(
    roomToEdit: HotelRoomEntity?,
    onDismiss: () -> Unit,
    onSave: (roomNumber: String, roomType: String, floor: String, pricePerNight: Double, status: String, amenities: String) -> Unit
) {
    var roomNumber by remember { mutableStateOf(roomToEdit?.roomNumber ?: "") }
    var roomType by remember { mutableStateOf(roomToEdit?.roomType ?: "Deluxe Double") }
    var floor by remember { mutableStateOf(roomToEdit?.floor ?: "1st Floor") }
    var priceText by remember { mutableStateOf(roomToEdit?.pricePerNight?.toInt()?.toString() ?: "8500") }
    var status by remember { mutableStateOf(roomToEdit?.status ?: "AVAILABLE") }
    var amenities by remember { mutableStateOf(roomToEdit?.amenities ?: "AC, Wi-Fi, Smart TV, Hot Water Geyser") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var expandedType by remember { mutableStateOf(false) }
    var expandedFloor by remember { mutableStateOf(false) }

    val roomTypes = listOf("Standard Single", "Standard Double", "Deluxe Double", "Deluxe Twin", "Executive Suite", "Family Suite")
    val floors = listOf("Ground Floor", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor", "Penthouse")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (roomToEdit != null) "Edit Room ${roomToEdit.roomNumber}" else "Add Hotel Room",
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
                    value = roomNumber,
                    onValueChange = { roomNumber = it; errorText = null },
                    label = { Text("Room Number *") },
                    placeholder = { Text("e.g. 101, 202, 305") },
                    modifier = Modifier.fillMaxWidth().testTag("input_room_number"),
                    singleLine = true
                )

                // Room Type Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Type *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedType = !expandedType }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Type")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedType = true }
                    )
                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        roomTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    roomType = type
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Floor Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Floor Location *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedFloor = !expandedFloor }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Floor")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedFloor = true }
                    )
                    DropdownMenu(
                        expanded = expandedFloor,
                        onDismissRequest = { expandedFloor = false }
                    ) {
                        floors.forEach { fl ->
                            DropdownMenuItem(
                                text = { Text(fl) },
                                onClick = {
                                    floor = fl
                                    expandedFloor = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price Per Night (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_room_price"),
                    singleLine = true
                )

                // Initial Status
                Text("Room Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AVAILABLE", "BOOKED", "CLEANING").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amenities,
                    onValueChange = { amenities = it },
                    label = { Text("Amenities") },
                    placeholder = { Text("AC, Wi-Fi, King Bed, Mini-Bar, Geyser...") },
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
                    if (roomNumber.isBlank()) {
                        errorText = "Please enter room number"
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (price <= 0.0) {
                        errorText = "Please enter valid price in PKR"
                        return@Button
                    }
                    onSave(roomNumber.trim(), roomType, floor, price, status, amenities.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_room")
            ) {
                Text(if (roomToEdit != null) "Update Room" else "Save Room")
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
fun BookRoomDialog(
    rooms: List<HotelRoomEntity>,
    prefillRoom: HotelRoomEntity? = null,
    onDismiss: () -> Unit,
    onConfirmBooking: (
        room: HotelRoomEntity,
        guestName: String,
        guestPhone: String,
        guestCnic: String,
        guestCity: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfNights: Int,
        advancePaid: Double,
        specialRequests: String
    ) -> Unit
) {
    var selectedRoom by remember {
        mutableStateOf(prefillRoom ?: rooms.firstOrNull { it.status == "AVAILABLE" } ?: rooms.firstOrNull())
    }

    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("+92 3") }
    var guestCnic by remember { mutableStateOf("") }
    var guestCity by remember { mutableStateOf("Lahore") }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val todayStr = sdf.format(cal.time)

    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowStr = sdf.format(cal.time)

    var checkInDate by remember { mutableStateOf(todayStr) }
    var checkOutDate by remember { mutableStateOf(tomorrowStr) }
    var advanceText by remember { mutableStateOf("0") }
    var specialRequests by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var expandedRoomDropdown by remember { mutableStateOf(false) }

    val nights = remember(checkInDate, checkOutDate) {
        try {
            val d1 = sdf.parse(checkInDate)
            val d2 = sdf.parse(checkOutDate)
            if (d1 != null && d2 != null) {
                val diffMs = d2.time - d1.time
                val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                days.coerceAtLeast(1)
            } else 1
        } catch (_: Exception) {
            1
        }
    }

    val pricePerNight = selectedRoom?.pricePerNight ?: 0.0
    val totalAmount = pricePerNight * nights
    val advancePaid = advanceText.toDoubleOrNull() ?: 0.0
    val remainingAmount = (totalAmount - advancePaid).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = PakEmeraldPrimary)
                Text("Guest Room Booking", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Room Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedRoom != null) "Room ${selectedRoom!!.roomNumber} - ${selectedRoom!!.roomType} (${formatHotelPkr(selectedRoom!!.pricePerNight)}/night)" else "Select Room",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Room *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedRoomDropdown = !expandedRoomDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Room")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedRoomDropdown = true }
                    )
                    DropdownMenu(
                        expanded = expandedRoomDropdown,
                        onDismissRequest = { expandedRoomDropdown = false }
                    ) {
                        rooms.forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Room ${r.roomNumber} - ${r.roomType}", fontWeight = FontWeight.Bold)
                                        Text("${r.floor} • ${formatHotelPkr(r.pricePerNight)}/night • Status: ${r.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedRoom = r
                                    expandedRoomDropdown = false
                                }
                            )
                        }
                    }
                }

                // Guest Details (Name, Phone, CNIC)
                Text("Guest Identification", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it; errorText = null },
                    label = { Text("Guest Full Name *") },
                    placeholder = { Text("e.g. Tariq Mehmood") },
                    modifier = Modifier.fillMaxWidth().testTag("input_booking_guest_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = guestPhone,
                        onValueChange = { guestPhone = it },
                        label = { Text("Phone *") },
                        placeholder = { Text("+92 300 1234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f).testTag("input_booking_guest_phone"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = guestCity,
                        onValueChange = { guestCity = it },
                        label = { Text("City") },
                        placeholder = { Text("Lahore") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Pakistani CNIC Field with formatted placeholder
                OutlinedTextField(
                    value = guestCnic,
                    onValueChange = { guestCnic = it },
                    label = { Text("Guest CNIC (Pakistani National ID) *") },
                    placeholder = { Text("35201-1234567-1") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = PakGoldSecondary)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_booking_guest_cnic"),
                    singleLine = true
                )

                // Stay Dates (Check-In & Check-Out)
                Text("Stay Dates", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("Check-In Date") },
                        modifier = Modifier.weight(1f).testTag("input_booking_checkin"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = checkOutDate,
                        onValueChange = { checkOutDate = it },
                        label = { Text("Check-Out Date") },
                        modifier = Modifier.weight(1f).testTag("input_booking_checkout"),
                        singleLine = true
                    )
                }

                // Advance Payment
                OutlinedTextField(
                    value = advanceText,
                    onValueChange = { advanceText = it },
                    label = { Text("Advance Payment (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Financial Summary
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
                            Text("Rate / Night:", fontSize = 12.sp)
                            Text(formatHotelPkr(pricePerNight), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Duration:", fontSize = 12.sp)
                            Text("$nights Night${if (nights > 1) "s" else ""}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Stay Amount:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(formatHotelPkr(totalAmount), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PakEmeraldPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Due on Check-In:", fontSize = 12.sp, color = if (remainingAmount > 0) HotelColors.Booked else PakEmeraldPrimary)
                            Text(formatHotelPkr(remainingAmount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (remainingAmount > 0) HotelColors.Booked else PakEmeraldPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = { specialRequests = it },
                    label = { Text("Special Requests / Airport Pickup") },
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
                    if (selectedRoom == null) {
                        errorText = "Please select a room"
                        return@Button
                    }
                    if (guestName.isBlank()) {
                        errorText = "Please enter guest full name"
                        return@Button
                    }
                    if (guestPhone.isBlank()) {
                        errorText = "Please enter guest phone number"
                        return@Button
                    }
                    if (guestCnic.isBlank()) {
                        errorText = "Please enter guest CNIC (National ID)"
                        return@Button
                    }
                    onConfirmBooking(
                        selectedRoom!!,
                        guestName.trim(),
                        guestPhone.trim(),
                        guestCnic.trim(),
                        guestCity.trim(),
                        checkInDate.trim(),
                        checkOutDate.trim(),
                        nights,
                        advancePaid,
                        specialRequests.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_booking")
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
fun CheckOutDialog(
    booking: HotelBookingEntity,
    onDismiss: () -> Unit,
    onConfirmCheckOut: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = PakEmeraldPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Guest Check-Out & Settlement",
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
                    text = "Confirm check-out for Room ${booking.roomNumber}. The room will be set to CLEANING status for housekeeping.",
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
                            Text("Guest:", fontSize = 12.sp)
                            Text(booking.guestName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CNIC:", fontSize = 12.sp)
                            Text(booking.guestCnic, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dates:", fontSize = 12.sp)
                            Text("${booking.checkInDate} to ${booking.checkOutDate} (${booking.numberOfNights} Nights)", fontSize = 12.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Stay:", fontSize = 12.sp)
                            Text(formatHotelPkr(booking.totalAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Due to Collect:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(formatHotelPkr(booking.remainingAmount), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (booking.remainingAmount > 0) HotelColors.Booked else PakEmeraldPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmCheckOut,
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_checkout")
            ) {
                Text("Check-Out & Send to Cleaning")
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
fun AddGuestDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, cnic: String, city: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+92 3") }
    var cnic by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Islamabad") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Register Hotel Guest", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cnic,
                    onValueChange = { cnic = it },
                    label = { Text("CNIC (National ID) *") },
                    placeholder = { Text("35201-1234567-1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City of Origin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || cnic.isBlank()) {
                        errorText = "Please fill in Name, Phone, and CNIC"
                        return@Button
                    }
                    onSave(name.trim(), phone.trim(), cnic.trim(), city.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
            ) {
                Text("Save Guest")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
