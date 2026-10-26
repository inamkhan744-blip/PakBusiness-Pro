package com.example.ui.screens.hotel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.HotelRoomEntity
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun RoomsGridTabContent(
    rooms: List<HotelRoomEntity>,
    onAddRoom: () -> Unit,
    onEditRoom: (HotelRoomEntity) -> Unit,
    onDeleteRoom: (Long) -> Unit,
    onBookRoom: (HotelRoomEntity) -> Unit,
    onCheckOutRoom: (HotelRoomEntity) -> Unit,
    onMarkCleaned: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedFloorFilter by remember { mutableStateOf("ALL") }

    val availableCount = remember(rooms) { rooms.count { it.status == "AVAILABLE" } }
    val bookedCount = remember(rooms) { rooms.count { it.status == "BOOKED" } }
    val cleaningCount = remember(rooms) { rooms.count { it.status == "CLEANING" } }

    val floors = remember(rooms) {
        listOf("ALL") + rooms.map { it.floor }.distinct().sorted()
    }

    val filteredRooms = remember(rooms, selectedStatusFilter, selectedFloorFilter) {
        rooms.filter { room ->
            val matchesStatus = selectedStatusFilter == "ALL" || room.status.equals(selectedStatusFilter, ignoreCase = true)
            val matchesFloor = selectedFloorFilter == "ALL" || room.floor.equals(selectedFloorFilter, ignoreCase = true)
            matchesStatus && matchesFloor
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("rooms_grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status KPI Row
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${rooms.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Available", fontSize = 11.sp, color = HotelColors.Available)
                        Text("$availableCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = HotelColors.Available)
                    }
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Booked", fontSize = 11.sp, color = HotelColors.Booked)
                        Text("$bookedCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = HotelColors.Booked)
                    }
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cleaning", fontSize = 11.sp, color = HotelColors.Cleaning)
                        Text("$cleaningCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = HotelColors.Cleaning)
                    }
                }
            }
        }

        // Status Filter Chips
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == "ALL",
                        onClick = { selectedStatusFilter = "ALL" },
                        label = { Text("All (${rooms.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == "AVAILABLE",
                        onClick = { selectedStatusFilter = "AVAILABLE" },
                        label = { Text("Available ($availableCount)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HotelColors.Available,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HotelColors.AvailableContainer,
                            selectedLabelColor = HotelColors.AvailableOnContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == "BOOKED",
                        onClick = { selectedStatusFilter = "BOOKED" },
                        label = { Text("Booked ($bookedCount)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Hotel,
                                contentDescription = null,
                                tint = HotelColors.Booked,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HotelColors.BookedContainer,
                            selectedLabelColor = HotelColors.BookedOnContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == "CLEANING",
                        onClick = { selectedStatusFilter = "CLEANING" },
                        label = { Text("Cleaning ($cleaningCount)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = HotelColors.Cleaning,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HotelColors.CleaningContainer,
                            selectedLabelColor = HotelColors.CleaningOnContainer
                        )
                    )
                }
            }
        }

        // Empty state
        if (filteredRooms.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Rooms Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Add single, double, or executive suites to monitor room availability, guest bookings, and housekeeping cleaning.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onAddRoom,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add First Room")
                        }
                    }
                }
            }
        } else {
            items(filteredRooms, key = { it.id }) { room ->
                RoomGridCard(
                    room = room,
                    onEdit = { onEditRoom(room) },
                    onDelete = { onDeleteRoom(room.id) },
                    onBook = { onBookRoom(room) },
                    onCheckOut = { onCheckOutRoom(room) },
                    onMarkClean = { onMarkCleaned(room.id) }
                )
            }
        }
    }
}

@Composable
fun RoomGridCard(
    room: HotelRoomEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBook: () -> Unit,
    onCheckOut: () -> Unit,
    onMarkClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, statusOnBg) = getRoomStatusColors(room.status)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_room_${room.roomNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Room Number & Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Room ${room.roomNumber}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = room.floor,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
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
                        DropdownMenuItem(
                            text = { Text("Edit Room") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
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

            // Room Type Pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = room.roomType,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Status Badge with Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val icon = when (room.status.uppercase()) {
                        "AVAILABLE" -> Icons.Default.CheckCircle
                        "BOOKED" -> Icons.Default.Hotel
                        "CLEANING" -> Icons.Default.CleaningServices
                        else -> Icons.Default.Info
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = room.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusOnBg
                    )
                }
            }

            // Price per night
            Text(
                text = "${formatHotelPkr(room.pricePerNight)} / night",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PakEmeraldPrimary
            )

            // Amenities preview
            if (room.amenities.isNotBlank()) {
                Text(
                    text = room.amenities,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Quick Interactive Action Button based on room status
            when (room.status.uppercase()) {
                "AVAILABLE" -> {
                    Button(
                        onClick = onBook,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_book_room_${room.roomNumber}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Book Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "BOOKED" -> {
                    OutlinedButton(
                        onClick = onCheckOut,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HotelColors.Booked),
                        border = BorderStroke(1.dp, HotelColors.Booked),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_checkout_room_${room.roomNumber}")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Check-Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "CLEANING" -> {
                    Button(
                        onClick = onMarkClean,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HotelColors.Cleaning),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_clean_room_${room.roomNumber}")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Clean", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
