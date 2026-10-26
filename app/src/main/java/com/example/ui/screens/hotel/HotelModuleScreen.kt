package com.example.ui.screens.hotel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BusinessEntity
import com.example.data.HotelBookingEntity
import com.example.data.HotelGuestEntity
import com.example.data.HotelRoomEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HotelModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsStateWithLifecycle()
    val rooms by viewModel.hotelRooms.collectAsStateWithLifecycle()
    val bookings by viewModel.hotelBookings.collectAsStateWithLifecycle()
    val guests by viewModel.hotelGuests.collectAsStateWithLifecycle()

    HotelModuleScreen(
        activeBusiness = activeBusiness,
        rooms = rooms,
        bookings = bookings,
        guests = guests,
        onSaveRoom = { viewModel.saveHotelRoom(it) },
        onDeleteRoom = { viewModel.deleteHotelRoom(it) },
        onMarkRoomCleaned = { viewModel.markHotelRoomCleaned(it) },
        onSaveBooking = { viewModel.saveHotelBooking(it) },
        onCheckInGuest = { bId, rId -> viewModel.checkInHotelGuest(bId, rId) },
        onCheckOutGuest = { bId, rId -> viewModel.checkOutHotelGuest(bId, rId) },
        onCancelBooking = { bId, rId -> viewModel.cancelHotelBooking(bId, rId) },
        onDeleteBooking = { viewModel.deleteHotelBooking(it) },
        onSaveGuest = { viewModel.saveHotelGuest(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelModuleScreen(
    activeBusiness: BusinessEntity?,
    rooms: List<HotelRoomEntity>,
    bookings: List<HotelBookingEntity>,
    guests: List<HotelGuestEntity>,
    onSaveRoom: (HotelRoomEntity) -> Unit,
    onDeleteRoom: (Long) -> Unit,
    onMarkRoomCleaned: (Long) -> Unit,
    onSaveBooking: (HotelBookingEntity) -> Unit,
    onCheckInGuest: (bookingId: Long, roomId: Long) -> Unit,
    onCheckOutGuest: (bookingId: Long, roomId: Long) -> Unit,
    onCancelBooking: (bookingId: Long, roomId: Long) -> Unit,
    onDeleteBooking: (Long) -> Unit,
    onSaveGuest: (HotelGuestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog state
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToEdit by remember { mutableStateOf<HotelRoomEntity?>(null) }

    var showBookRoomDialog by remember { mutableStateOf(false) }
    var roomToBookPrefill by remember { mutableStateOf<HotelRoomEntity?>(null) }

    var bookingToCheckOut by remember { mutableStateOf<HotelBookingEntity?>(null) }
    var showAddGuestDialog by remember { mutableStateOf(false) }

    val businessId = activeBusiness?.id ?: 1L

    // Metrics for Header
    val totalRooms = rooms.size
    val bookedRooms = rooms.count { it.status == "BOOKED" }
    val occupancyRate = if (totalRooms > 0) (bookedRooms * 100) / totalRooms else 0

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayCheckIns = remember(bookings, todayDateStr) {
        bookings.count { it.checkInDate == todayDateStr && it.status != "CANCELLED" }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("hotel_module_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> {
                            roomToEdit = null
                            showAddRoomDialog = true
                        }
                        1 -> {
                            roomToBookPrefill = null
                            showBookRoomDialog = true
                        }
                        2 -> {
                            showAddGuestDialog = true
                        }
                    }
                },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = when (selectedTabIndex) {
                            0 -> Icons.Default.Add
                            1 -> Icons.Default.Event
                            else -> Icons.Default.PersonAdd
                        },
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Add Room"
                            1 -> "New Booking"
                            else -> "Add Guest"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("fab_hotel_action")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Stats Banner
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeBusiness?.name ?: "Hotel & Guest House",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Guest House Management • Daily Operations",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Occupancy Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (occupancyRate > 70) HotelColors.BookedContainer else HotelColors.AvailableContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = if (occupancyRate > 70) HotelColors.BookedOnContainer else HotelColors.AvailableOnContainer,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$occupancyRate% Occupancy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (occupancyRate > 70) HotelColors.BookedOnContainer else HotelColors.AvailableOnContainer
                                )
                            }
                        }
                    }

                    // 3 KPI Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Rooms", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalRooms", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HotelColors.AvailableContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Available", fontSize = 10.sp, color = HotelColors.AvailableOnContainer)
                                Text("${rooms.count { it.status == "AVAILABLE" }}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HotelColors.AvailableOnContainer)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Today In", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$todayCheckIns", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tab Navigation Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Rooms Grid (${rooms.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Bookings (${bookings.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Guest Registry (${guests.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> RoomsGridTabContent(
                    rooms = rooms,
                    onAddRoom = {
                        roomToEdit = null
                        showAddRoomDialog = true
                    },
                    onEditRoom = { room ->
                        roomToEdit = room
                        showAddRoomDialog = true
                    },
                    onDeleteRoom = onDeleteRoom,
                    onBookRoom = { room ->
                        roomToBookPrefill = room
                        showBookRoomDialog = true
                    },
                    onCheckOutRoom = { room ->
                        val activeBooking = bookings.firstOrNull { it.roomId == room.id && it.status == "CHECKED_IN" }
                        if (activeBooking != null) {
                            bookingToCheckOut = activeBooking
                        } else {
                            onCheckOutGuest(0L, room.id)
                        }
                    },
                    onMarkCleaned = onMarkRoomCleaned,
                    modifier = Modifier.weight(1f)
                )
                1 -> BookingsTabContent(
                    bookings = bookings,
                    onNewBooking = {
                        roomToBookPrefill = null
                        showBookRoomDialog = true
                    },
                    onCheckIn = { booking ->
                        onCheckInGuest(booking.id, booking.roomId)
                    },
                    onCheckOut = { booking ->
                        bookingToCheckOut = booking
                    },
                    onCancelBooking = { booking ->
                        onCancelBooking(booking.id, booking.roomId)
                    },
                    onDeleteBooking = onDeleteBooking,
                    modifier = Modifier.weight(1f)
                )
                2 -> GuestsTabContent(
                    guests = guests,
                    onAddGuest = { showAddGuestDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Add / Edit Room Dialog
    if (showAddRoomDialog) {
        AddEditRoomDialog(
            roomToEdit = roomToEdit,
            onDismiss = { showAddRoomDialog = false },
            onSave = { roomNumber, roomType, floor, price, status, amenities ->
                val newRoom = roomToEdit?.copy(
                    roomNumber = roomNumber,
                    roomType = roomType,
                    floor = floor,
                    pricePerNight = price,
                    status = status,
                    amenities = amenities
                ) ?: HotelRoomEntity(
                    businessId = businessId,
                    roomNumber = roomNumber,
                    roomType = roomType,
                    floor = floor,
                    pricePerNight = price,
                    status = status,
                    amenities = amenities
                )
                onSaveRoom(newRoom)
                showAddRoomDialog = false
            }
        )
    }

    // Book Room Dialog
    if (showBookRoomDialog) {
        BookRoomDialog(
            rooms = rooms,
            prefillRoom = roomToBookPrefill,
            onDismiss = { showBookRoomDialog = false },
            onConfirmBooking = { room, guestName, guestPhone, guestCnic, guestCity, checkInDate, checkOutDate, numberOfNights, advancePaid, specialRequests ->
                val totalAmount = room.pricePerNight * numberOfNights
                val remainingAmount = (totalAmount - advancePaid).coerceAtLeast(0.0)

                val newBooking = HotelBookingEntity(
                    businessId = businessId,
                    roomId = room.id,
                    roomNumber = room.roomNumber,
                    roomType = room.roomType,
                    guestName = guestName,
                    guestPhone = guestPhone,
                    guestCnic = guestCnic,
                    guestCity = guestCity,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    numberOfNights = numberOfNights,
                    pricePerNight = room.pricePerNight,
                    totalAmount = totalAmount,
                    advancePaid = advancePaid,
                    remainingAmount = remainingAmount,
                    status = "CONFIRMED",
                    specialRequests = specialRequests
                )
                onSaveBooking(newBooking)
                showBookRoomDialog = false
            }
        )
    }

    // Check-out Dialog
    if (bookingToCheckOut != null) {
        CheckOutDialog(
            booking = bookingToCheckOut!!,
            onDismiss = { bookingToCheckOut = null },
            onConfirmCheckOut = {
                val b = bookingToCheckOut!!
                onCheckOutGuest(b.id, b.roomId)
                bookingToCheckOut = null
            }
        )
    }

    // Add Guest Dialog
    if (showAddGuestDialog) {
        AddGuestDialog(
            onDismiss = { showAddGuestDialog = false },
            onSave = { name, phone, cnic, city ->
                onSaveGuest(
                    HotelGuestEntity(
                        businessId = businessId,
                        name = name,
                        phone = phone,
                        cnic = cnic,
                        city = city,
                        totalStays = 1,
                        totalSpent = 0.0,
                        lastStayDate = todayDateStr
                    )
                )
                showAddGuestDialog = false
            }
        )
    }
}
