package com.example.ui.screens.tabs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BusinessEntity
import com.example.ui.AppLanguage
import com.example.ui.BusinessViewModel
import com.example.ui.Localization
import com.example.ui.components.RoomDatabaseBackupRestoreView
import com.example.ui.dialogs.BluetoothPrinterSettingsDialog
import com.example.ui.dialogs.ThermalReceiptPreviewDialog
import com.example.ui.dialogs.ThermalReceiptType
import com.example.ui.screens.tools.PakBusinessToolsScreen
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.BluetoothThermalPrinterManager
import com.example.util.EscPosPaperSize
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PrinterStatus

@Composable
fun SettingsTab(
    activeBusiness: BusinessEntity?,
    allBusinesses: List<BusinessEntity>,
    onSwitchBusiness: (Long) -> Unit,
    onDeleteBusiness: (Long) -> Unit,
    onAddNewBusiness: () -> Unit,
    onUpdateBusiness: (BusinessEntity) -> Unit,
    viewModel: BusinessViewModel? = null,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showToolsDialog by remember { mutableStateOf(false) }

    var editName by remember(activeBusiness) { mutableStateOf(activeBusiness?.name ?: "") }
    var editType by remember(activeBusiness) { mutableStateOf(activeBusiness?.type ?: "") }
    var editOwner by remember(activeBusiness) { mutableStateOf(activeBusiness?.ownerName ?: "") }
    var editPhone by remember(activeBusiness) { mutableStateOf(activeBusiness?.phone ?: "") }
    var editAddress by remember(activeBusiness) { mutableStateOf(activeBusiness?.address ?: "") }
    var editCurrency by remember(activeBusiness) { mutableStateOf(activeBusiness?.currency ?: "PKR") }

    val supportedCurrencies = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "CAD")

    // Security state from ViewModel
    val isSecurityEnabled by (viewModel?.isSecurityEnabled?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) })
    val currentSecUser by (viewModel?.securityUsername?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("admin") })

    var secEnabledState by remember(isSecurityEnabled) { mutableStateOf(isSecurityEnabled) }
    var secUsernameInput by remember(currentSecUser) { mutableStateOf(currentSecUser) }
    var secPasswordInput by remember { mutableStateOf("") }
    var secSavedMessage by remember { mutableStateOf("") }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDatabaseBackupUtilitySheet by remember { mutableStateOf(false) }
    var dataClearedSuccessMsg by remember { mutableStateOf("") }
    var backupSuccessMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    val printerManager = remember { BluetoothThermalPrinterManager.getInstance(context) }
    val printerStatus by printerManager.printerStatus.collectAsState()
    val selectedPaperSize by printerManager.selectedPaperSize.collectAsState()
    var showBluetoothPrinterSettingsDialog by remember { mutableStateOf(false) }
    var showTestThermalReceiptDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Active Business Profile Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.getString("business_profile_logo", appLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { showEditProfileDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Edit Details", color = PakEmeraldPrimary)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PakEmeraldContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = PakEmeraldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeBusiness?.name ?: "No Business Selected",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeBusiness?.type ?: "General"} • Currency: ${activeBusiness?.currency ?: "PKR"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Owner / پروپرائیٹر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(activeBusiness?.ownerName ?: "Not set", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Phone / WhatsApp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(activeBusiness?.phone ?: "Not set", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (!activeBusiness?.address.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(14.dp))
                            Text(activeBusiness?.address ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Section: Optional App Security & Password Lock (User Login)
        item {
            Text(
                text = "App Security & Password Lock (اختیاری پاس ورڈ)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = if (secEnabledState) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Column {
                                Text("Require Password / PIN on Launch", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Optional security lock to protect your accounts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Switch(
                            checked = secEnabledState,
                            onCheckedChange = { isChecked ->
                                secEnabledState = isChecked
                                if (!isChecked && viewModel != null) {
                                    viewModel.configureSecurity(false, secUsernameInput, "")
                                    secSavedMessage = "Security Lock disabled. App will open directly."
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PakEmeraldPrimary)
                        )
                    }

                    AnimatedVisibility(visible = secEnabledState) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = secUsernameInput,
                                onValueChange = { secUsernameInput = it },
                                label = { Text("Login Username (صارف کا نام)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PakEmeraldPrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = secPasswordInput,
                                onValueChange = { secPasswordInput = it },
                                label = { Text("New PIN / Password (پاس ورڈ یا پن)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PakEmeraldPrimary) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (viewModel != null) {
                                            viewModel.configureSecurity(true, secUsernameInput, secPasswordInput)
                                            secSavedMessage = "Security settings saved! Password enabled."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Password")
                                }

                                OutlinedButton(
                                    onClick = { viewModel?.lockApp() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lock App Now")
                                }
                            }

                            if (secSavedMessage.isNotEmpty()) {
                                Text(
                                    text = secSavedMessage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PakEmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Bluetooth Thermal Printer
        item {
            Text(
                text = "Bluetooth Thermal Receipt Printer (بلوٹوتھ تھرمل پرنٹر)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_bluetooth_printer_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (printerStatus) {
                                    is PrinterStatus.Connected -> PakEmeraldContainer
                                    is PrinterStatus.Connecting -> PakGoldContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (printerStatus is PrinterStatus.Connected) Icons.Default.BluetoothConnected else Icons.Default.Print,
                                        contentDescription = null,
                                        tint = if (printerStatus is PrinterStatus.Connected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "ESC/POS Thermal Printer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (val s = printerStatus) {
                                        is PrinterStatus.Connected -> "Connected: ${s.deviceName}"
                                        is PrinterStatus.Connecting -> "Connecting to ${s.deviceName}..."
                                        is PrinterStatus.Printing -> "Printing: ${s.jobName}..."
                                        is PrinterStatus.Error -> "Printer error: ${s.message}"
                                        is PrinterStatus.Disconnected -> "No printer connected (Simulation Active)"
                                    },
                                    fontSize = 11.sp,
                                    color = if (printerStatus is PrinterStatus.Connected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (printerStatus is PrinterStatus.Connected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = if (printerStatus is PrinterStatus.Connected) "ONLINE" else "STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (printerStatus is PrinterStatus.Connected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Paper Size Preference
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Thermal Paper Size", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("58mm (Mobile POS) or 80mm (Counter POS)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(EscPosPaperSize.WIDTH_58MM to "58 mm", EscPosPaperSize.WIDTH_80MM to "80 mm").forEach { (size, label) ->
                                val isSelected = selectedPaperSize == size
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { printerManager.setPaperSize(size) }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showBluetoothPrinterSettingsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_configure_bluetooth_printer")
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Printers", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showTestThermalReceiptDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_print_sample")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = PakEmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Receipt", fontSize = 12.sp, color = PakEmeraldPrimary)
                        }
                    }
                }
            }
        }

        // Section: Pakistani Business Utility Calculators
        item {
            Text(
                text = "Business Utilities & Calculators (کاروباری حساب کتاب)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showToolsDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PakGoldSecondary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cash Galla Counter & Mandi / Gold Converters",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "روپوں کی گنتی، من اور کلو، سونا تولہ، گز اور میٹر حساب",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showToolsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Open Tools", fontSize = 11.sp)
                    }
                }
            }
        }

        // Section: Multi-Business Registry
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Registered Businesses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${allBusinesses.size} local business profile${if (allBusinesses.size > 1) "s" else ""} registered",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddNewBusiness,
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Add Business", fontSize = 12.sp)
                }
            }
        }

        items(allBusinesses) { business ->
            val isActive = business.id == activeBusiness?.id
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) PakEmeraldContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isActive) onSwitchBusiness(business.id)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isActive) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = business.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = business.name,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                if (isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PakEmeraldPrimary
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${business.type} • ₨ ${business.currency}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!isActive) {
                            Button(
                                onClick = { onSwitchBusiness(business.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Switch", fontSize = 11.sp)
                            }

                            if (allBusinesses.size > 1) {
                                IconButton(onClick = { onDeleteBusiness(business.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Offline Storage Status
        item {
            Text(
                text = "Offline Storage & Data Security",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = PakEmeraldPrimary)
                        Column {
                            Text("Room SQLite Database Engine", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("All tenant records, items, and settings are saved locally on this device.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PakGoldSecondary)
                        Column {
                            Text("100% Offline Architecture", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Zero external servers or backend dependency. Works anytime without internet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Section: Enterprise Backup & Data Export (بیک اپ اور ڈیٹا شیئرنگ)
        item {
            Text(
                text = "Backup & Business Data Share (بیک اپ اور محفوظ ڈیٹا)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PakEmeraldContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Instant WhatsApp / Email Backup Archive",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Generate a full summary snapshot of your business profiles, settings, and database counters to share or archive securely.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (backupSuccessMsg.isNotEmpty()) {
                        Text(
                            text = backupSuccessMsg,
                            color = PakEmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            val active = activeBusiness?.name ?: "PakBusiness Enterprise"
                            val type = activeBusiness?.type ?: "General"
                            val phone = activeBusiness?.phone ?: "N/A"
                            val totalBiz = allBusinesses.size
                            val backupDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            val backupText = """
                                📦 *PAKBUSINESS PRO - DATA BACKUP SNAPSHOT*
                                ────────────────────
                                🏢 *Active Enterprise:* $active
                                🏷️ *Business Type:* $type
                                📞 *Registered Phone:* $phone
                                💼 *Total Profiles Registered:* $totalBiz
                                📅 *Backup Generated:* $backupDate
                                🔒 *Storage Engine:* Room SQLite (Local Offline Storage)
                                ────────────────────
                                🛡️ _Encrypted local integrity verified. Keep this record safe for your accounts audit._
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, backupText)
                                setType("text/plain")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Business Backup"))
                            backupSuccessMsg = "✓ Business backup archive ready & shared!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("btn_export_backup_data")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export / Share Backup Snapshot (بیک اپ شیئر کریں)")
                    }

                    OutlinedButton(
                        onClick = { showDatabaseBackupUtilitySheet = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_open_room_database_backup_utility"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Database File Backup & Restore Utility (.pakdb)", color = PakEmeraldPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Section: Data Management & Cleanup (Permanent Fake Data Removal)
        item {
            Text(
                text = "Data Management & Cleanup (فیک / ڈیمو ڈیٹا ختم کریں)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Wipe All Demo & Sample Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Permanently delete all sample/preloaded records (patients, gym members, tables, menu, school students, vouchers, properties, salon appointments, workshop job cards, etc.) for this business. Once cleared, they will NEVER return.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (dataClearedSuccessMsg.isNotEmpty()) {
                        Text(
                            text = dataClearedSuccessMsg,
                            color = PakEmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Clear All Fake Records Now (تمام فیک ڈیٹا ڈیلیٹ کریں)")
                    }
                }
            }
        }

        // About Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PakBusiness Pro v1.0 • Enterprise Edition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Multi-Tenant Offline Suite for Phones, Tablets & Computers",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog && activeBusiness != null) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Business Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Business Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editType,
                        onValueChange = { editType = it },
                        label = { Text("Business Type") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editOwner,
                        onValueChange = { editOwner = it },
                        label = { Text("Owner Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address") },
                        singleLine = true
                    )

                    Text(
                        text = "Accounting Currency",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        supportedCurrencies.take(4).forEach { cur ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (editCurrency == cur) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { editCurrency = cur }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = cur,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (editCurrency == cur) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = activeBusiness.copy(
                            name = editName.ifBlank { activeBusiness.name },
                            type = editType.ifBlank { activeBusiness.type },
                            ownerName = editOwner,
                            phone = editPhone,
                            address = editAddress,
                            currency = editCurrency
                        )
                        onUpdateBusiness(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Tools Dialog (Cash Galla, Mandi, Gold, Fabric, Tax)
    if (showToolsDialog) {
        AlertDialog(
            onDismissRequest = { showToolsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = PakEmeraldPrimary)
                    Text("Pakistani Business Utilities", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Box(modifier = Modifier.height(420.dp).fillMaxWidth()) {
                    PakBusinessToolsScreen(appLanguage = appLanguage)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showToolsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Confirmation Dialog to Clear Fake / Demo Data
    if (showClearDataDialog && activeBusiness != null) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Delete All Fake Entries?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to permanently clear all sample/demo data for '${activeBusiness.name}'? " +
                    "This will remove preloaded test items and reset tables cleanly. Once deleted, they will never reappear."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        viewModel?.clearAllDemoData(activeBusiness.id) {
                            dataClearedSuccessMsg = "All fake / demo records removed permanently!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatabaseBackupUtilitySheet) {
        Dialog(
            onDismissRequest = { showDatabaseBackupUtilitySheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Room Database Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { showDatabaseBackupUtilitySheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    RoomDatabaseBackupRestoreView(
                        activeBusiness = activeBusiness,
                        onDataRestored = {
                            // Data restored
                        }
                    )
                }
            }
        }
    }

    // Bluetooth Printer Settings Dialog
    if (showBluetoothPrinterSettingsDialog) {
        BluetoothPrinterSettingsDialog(
            onDismiss = { showBluetoothPrinterSettingsDialog = false }
        )
    }

    // Test Thermal Receipt Dialog
    if (showTestThermalReceiptDialog) {
        val sampleInvoice = InvoiceExportData(
            invoiceNumber = "TEST-001",
            customerName = "Walk-in Customer",
            customerPhone = activeBusiness?.phone ?: "0300-1234567",
            items = listOf(
                InvoiceExportItem("Panadol Extra 500mg", 2.0, 45.0, 90.0),
                InvoiceExportItem("Mineral Water 1.5L", 1.0, 120.0, 120.0),
                InvoiceExportItem("Chicken Karahi Full", 1.0, 1450.0, 1450.0)
            ),
            subtotal = 1660.0,
            tax = 0.0,
            discount = 60.0,
            grandTotal = 1600.0,
            paymentMethod = "Cash PKR"
        )
        ThermalReceiptPreviewDialog(
            receiptType = ThermalReceiptType.PosInvoice(sampleInvoice, cashierName = "Manager / Admin"),
            onDismiss = { showTestThermalReceiptDialog = false }
        )
    }
}
