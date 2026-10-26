package com.example.ui.screens.salon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.data.SalonAppointmentEntity
import com.example.data.SalonServiceEntity
import com.example.data.StylistEntity
import com.example.ui.BusinessViewModel
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun SalonModuleScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsStateWithLifecycle()
    val services by viewModel.salonServices.collectAsStateWithLifecycle()
    val stylists by viewModel.salonStylists.collectAsStateWithLifecycle()
    val appointments by viewModel.salonAppointments.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAddEditServiceDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<SalonServiceEntity?>(null) }

    var showAddEditStylistDialog by remember { mutableStateOf(false) }
    var stylistToEdit by remember { mutableStateOf<StylistEntity?>(null) }

    var showBookAppointmentDialog by remember { mutableStateOf(false) }
    var prefillServiceForBooking by remember { mutableStateOf<SalonServiceEntity?>(null) }

    var showSettleDialog by remember { mutableStateOf(false) }
    var appointmentToSettle by remember { mutableStateOf<SalonAppointmentEntity?>(null) }

    val businessId = activeBusiness?.id ?: 1L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> {
                            serviceToEdit = null
                            showAddEditServiceDialog = true
                        }
                        1 -> {
                            stylistToEdit = null
                            showAddEditStylistDialog = true
                        }
                        2 -> {
                            prefillServiceForBooking = null
                            showBookAppointmentDialog = true
                        }
                    }
                },
                containerColor = PakEmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_salon_action")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Add Service"
                            1 -> "Add Stylist"
                            2 -> "Book Appointment"
                            else -> "Add"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Module Header Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PakEmeraldPrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Salon & Beauty Parlor",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeBusiness?.name ?: "PakBusiness"} • Services & Commissions",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PakGoldSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PKR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakGoldSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Navigation Tabs: 0 -> Service Menu, 1 -> Stylists, 2 -> Appointments
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = PakEmeraldPrimary
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            modifier = Modifier.testTag("tab_salon_services"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Services (${services.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )

                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            modifier = Modifier.testTag("tab_salon_stylists"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Stylists (${stylists.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )

                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            modifier = Modifier.testTag("tab_salon_appointments"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Bookings (${appointments.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> SalonServicesTabContent(
                        services = services,
                        onAddService = {
                            serviceToEdit = null
                            showAddEditServiceDialog = true
                        },
                        onEditService = { s ->
                            serviceToEdit = s
                            showAddEditServiceDialog = true
                        },
                        onDeleteService = { id ->
                            viewModel.deleteSalonService(id)
                        },
                        onBookService = { s ->
                            prefillServiceForBooking = s
                            showBookAppointmentDialog = true
                        }
                    )
                    1 -> StylistsTabContent(
                        stylists = stylists,
                        onAddStylist = {
                            stylistToEdit = null
                            showAddEditStylistDialog = true
                        },
                        onEditStylist = { st ->
                            stylistToEdit = st
                            showAddEditStylistDialog = true
                        },
                        onDeleteStylist = { id ->
                            viewModel.deleteSalonStylist(id)
                        }
                    )
                    2 -> AppointmentsTabContent(
                        appointments = appointments,
                        onBookAppointment = {
                            prefillServiceForBooking = null
                            showBookAppointmentDialog = true
                        },
                        onStartService = { appt ->
                            viewModel.updateSalonAppointmentStatus(
                                id = appt.id,
                                status = "IN_PROGRESS",
                                paymentStatus = appt.paymentStatus,
                                stylistId = appt.stylistId,
                                commissionAmount = appt.commissionAmount
                            )
                        },
                        onCompleteAppointment = { appt ->
                            appointmentToSettle = appt
                            showSettleDialog = true
                        },
                        onCancelAppointment = { appt ->
                            viewModel.updateSalonAppointmentStatus(
                                id = appt.id,
                                status = "CANCELLED",
                                paymentStatus = appt.paymentStatus,
                                stylistId = appt.stylistId,
                                commissionAmount = appt.commissionAmount
                            )
                        },
                        onDeleteAppointment = { id ->
                            viewModel.deleteSalonAppointment(id)
                        }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add/Edit Service Dialog
    if (showAddEditServiceDialog) {
        AddEditServiceDialog(
            serviceToEdit = serviceToEdit,
            onDismiss = {
                showAddEditServiceDialog = false
                serviceToEdit = null
            },
            onSave = { name, category, price, duration, description ->
                val service = SalonServiceEntity(
                    id = serviceToEdit?.id ?: 0L,
                    businessId = businessId,
                    name = name,
                    category = category,
                    price = price,
                    durationMinutes = duration,
                    description = description,
                    isActive = true
                )
                viewModel.saveSalonService(service)
                showAddEditServiceDialog = false
                serviceToEdit = null
            }
        )
    }

    // 2. Add/Edit Stylist Dialog
    if (showAddEditStylistDialog) {
        AddEditStylistDialog(
            stylistToEdit = stylistToEdit,
            onDismiss = {
                showAddEditStylistDialog = false
                stylistToEdit = null
            },
            onSave = { name, phone, specialty, commissionPercentage, isAvailable ->
                val stylist = StylistEntity(
                    id = stylistToEdit?.id ?: 0L,
                    businessId = businessId,
                    name = name,
                    phone = phone,
                    specialty = specialty,
                    commissionPercentage = commissionPercentage,
                    totalEarnedCommission = stylistToEdit?.totalEarnedCommission ?: 0.0,
                    totalServicesCompleted = stylistToEdit?.totalServicesCompleted ?: 0,
                    isAvailable = isAvailable
                )
                viewModel.saveSalonStylist(stylist)
                showAddEditStylistDialog = false
                stylistToEdit = null
            }
        )
    }

    // 3. Book Appointment Dialog
    if (showBookAppointmentDialog) {
        BookAppointmentDialog(
            services = services,
            stylists = stylists,
            prefillService = prefillServiceForBooking,
            onDismiss = {
                showBookAppointmentDialog = false
                prefillServiceForBooking = null
            },
            onConfirmBooking = { clientName, clientPhone, service, stylist, date, time, discount, notes ->
                val finalPrice = (service.price - discount).coerceAtLeast(0.0)
                val commissionAmount = finalPrice * (stylist.commissionPercentage / 100.0)

                val appointment = SalonAppointmentEntity(
                    businessId = businessId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    serviceId = service.id,
                    serviceName = service.name,
                    serviceCategory = service.category,
                    stylistId = stylist.id,
                    stylistName = stylist.name,
                    appointmentDate = date,
                    appointmentTime = time,
                    status = "SCHEDULED",
                    servicePrice = service.price,
                    discount = discount,
                    finalPrice = finalPrice,
                    commissionPercentage = stylist.commissionPercentage,
                    commissionAmount = commissionAmount,
                    paymentStatus = "PENDING",
                    notes = notes
                )
                viewModel.saveSalonAppointment(appointment)
                showBookAppointmentDialog = false
                prefillServiceForBooking = null
            }
        )
    }

    // 4. Settle / Complete Appointment Dialog
    if (showSettleDialog && appointmentToSettle != null) {
        SettleAppointmentDialog(
            appointment = appointmentToSettle!!,
            onDismiss = {
                showSettleDialog = false
                appointmentToSettle = null
            },
            onConfirmSettle = {
                val appt = appointmentToSettle!!
                viewModel.updateSalonAppointmentStatus(
                    id = appt.id,
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    stylistId = appt.stylistId,
                    commissionAmount = appt.commissionAmount
                )
                showSettleDialog = false
                appointmentToSettle = null
            }
        )
    }
}
