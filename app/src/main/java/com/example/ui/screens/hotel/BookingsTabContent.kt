package com.example.ui.screens.hotel

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HotelBookingEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun BookingsTabContent(
    bookings: List<HotelBookingEntity>,
    onNewBooking: () -> Unit,
    onCheckIn: (HotelBookingEntity) -> Unit,
    onCheckOut: (HotelBookingEntity) -> Unit,
    onCancelBooking: (HotelBookingEntity) -> Unit,
    onDeleteBooking: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredBookings = remember(bookings, selectedStatus, searchQuery) {
        bookings.filter { b ->
            val matchesStatus = selectedStatus == "ALL" || b.status.equals(selectedStatus, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    b.guestName.contains(searchQuery, ignoreCase = true) ||
                    b.guestPhone.contains(searchQuery, ignoreCase = true) ||
                    b.guestCnic.contains(searchQuery, ignoreCase = true) ||
                    b.roomNumber.contains(searchQuery, ignoreCase = true) ||
                    b.guestCity.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    val totalBookedRevenue = remember(bookings) {
        bookings.filter { it.status != "CANCELLED" }.sumOf { it.totalAmount }
    }
    val totalAdvanceCollected = remember(bookings) {
        bookings.filter { it.status != "CANCELLED" }.sumOf { it.advancePaid }
    }
    val totalRemaining = remember(bookings) {
        bookings.filter { it.status == "CONFIRMED" || it.status == "CHECKED_IN" }.sumOf { it.remainingAmount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookings_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Revenue & Collection Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.65f)),
                border = BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Stay Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatHotelPkr(totalBookedRevenue), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PakEmeraldPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Advance Collected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatHotelPkr(totalAdvanceCollected), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D9488))
                        }
                    }

                    if (totalRemaining > 0) {
                        HorizontalDivider(color = PakEmeraldPrimary.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pending Balance to Collect", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(formatHotelPkr(totalRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PakGoldSecondary)
                        }
                    }
                }
            }
        }

        // Search Bar (Guest Name, Phone, CNIC, Room)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_bookings"),
                placeholder = { Text("Search by Guest Name, Phone, CNIC, or Room...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Status Filter Chips
        item {
            val checkedInCount = bookings.count { it.status == "CHECKED_IN" }
            val confirmedCount = bookings.count { it.status == "CONFIRMED" }
            val checkedOutCount = bookings.count { it.status == "CHECKED_OUT" }
            val cancelledCount = bookings.count { it.status == "CANCELLED" }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == "ALL",
                        onClick = { selectedStatus = "ALL" },
                        label = { Text("All (${bookings.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "CHECKED_IN",
                        onClick = { selectedStatus = "CHECKED_IN" },
                        label = { Text("Checked-In ($checkedInCount)") },
                        leadingIcon = {
                            Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "CONFIRMED",
                        onClick = { selectedStatus = "CONFIRMED" },
                        label = { Text("Confirmed ($confirmedCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "CHECKED_OUT",
                        onClick = { selectedStatus = "CHECKED_OUT" },
                        label = { Text("Checked-Out ($checkedOutCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatus == "CANCELLED",
                        onClick = { selectedStatus = "CANCELLED" },
                        label = { Text("Cancelled ($cancelledCount)") }
                    )
                }
            }
        }

        // Empty state
        if (filteredBookings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No bookings match '$searchQuery'" else "No Hotel Bookings Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Reserve guest rooms with check-in & check-out dates, guest CNIC verification, and payment tracking.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onNewBooking,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Room Booking")
                        }
                    }
                }
            }
        } else {
            items(filteredBookings, key = { it.id }) { booking ->
                BookingCard(
                    booking = booking,
                    onCheckIn = { onCheckIn(booking) },
                    onCheckOut = { onCheckOut(booking) },
                    onCancel = { onCancelBooking(booking) },
                    onDelete = { onDeleteBooking(booking.id) },
                    onCallGuest = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${booking.guestPhone}")
                        }
                        try {
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                            // Dial launcher fallback
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: HotelBookingEntity,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onCallGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = getBookingStatusColors(booking.status)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_booking_${booking.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Room Pill, Status Pill, More Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Room ${booking.roomNumber} • ${booking.roomType}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = booking.status.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (booking.status != "CHECKED_OUT" && booking.status != "CANCELLED") {
                            DropdownMenuItem(
                                text = { Text("Cancel Booking") },
                                onClick = {
                                    showMenu = false
                                    onCancel()
                                },
                                leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Guest Details Section (Name, Phone, and CNIC)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = booking.guestName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = booking.guestCity,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = booking.guestPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onCallGuest,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Prominent Pakistani CNIC Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PakGoldSecondary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PakGoldSecondary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "CNIC (National ID)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakGoldSecondary
                        )
                        Text(
                            text = booking.guestCnic,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Stay Dates Section: Check-In -> Check-Out & Nights
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Check-In", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.checkInDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PakEmeraldPrimary
                        ) {
                            Text(
                                text = "${booking.numberOfNights} Night${if (booking.numberOfNights > 1) "s" else ""}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Check-Out", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.checkOutDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Financial Breakdown: Total, Advance, Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total: ${formatHotelPkr(booking.totalAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PakEmeraldPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Adv: ${formatHotelPkr(booking.advancePaid)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (booking.remainingAmount > 0) {
                            Text(
                                text = "Due: ${formatHotelPkr(booking.remainingAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HotelColors.Booked
                            )
                        }
                    }
                }

                // Workflow Action Buttons
                when (booking.status) {
                    "CONFIRMED" -> {
                        Button(
                            onClick = onCheckIn,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_checkin_booking_${booking.id}")
                        ) {
                            Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Check-In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "CHECKED_IN" -> {
                        Button(
                            onClick = onCheckOut,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HotelColors.Cleaning),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_checkout_booking_${booking.id}")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Check-Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "CHECKED_OUT" -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Text("Completed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            if (booking.specialRequests.isNotBlank()) {
                Text(
                    text = "Request: ${booking.specialRequests}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
