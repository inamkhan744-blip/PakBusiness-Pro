package com.example.ui.dialogs

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import com.example.util.BluetoothThermalPrinterManager
import com.example.util.DiscoveredPrinterDevice
import com.example.util.EscPosPaperSize
import com.example.util.EscPosUtil
import com.example.util.PrinterStatus

@Composable
fun BluetoothPrinterSettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val printerManager = remember { BluetoothThermalPrinterManager.getInstance(context) }
    val printerStatus by printerManager.printerStatus.collectAsState()
    val selectedPaperSize by printerManager.selectedPaperSize.collectAsState()

    var pairedDevices by remember { mutableStateOf<List<DiscoveredPrinterDevice>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(printerManager.hasBluetoothPermissions()) }
    var actionMessage by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasPermission = granted
        if (granted) {
            pairedDevices = printerManager.getPairedPrinters()
        } else {
            actionMessage = "Bluetooth permission is required to discover thermal printers."
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            pairedDevices = printerManager.getPairedPrinters()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_bluetooth_printer"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PakEmeraldContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Thermal Printer",
                        tint = PakEmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bluetooth Thermal Printer",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "POS Receipts & Kitchen KOT (58mm / 80mm)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    if (hasPermission) {
                        pairedDevices = printerManager.getPairedPrinters()
                        actionMessage = "Refreshed paired devices (${pairedDevices.size} found)"
                    } else {
                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                android.Manifest.permission.BLUETOOTH_CONNECT,
                                android.Manifest.permission.BLUETOOTH_SCAN
                            )
                        } else {
                            arrayOf(android.Manifest.permission.BLUETOOTH)
                        }
                        permissionLauncher.launch(perms)
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Devices")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Connection Status Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (printerStatus) {
                        is PrinterStatus.Connected -> Color(0xFFE8F5E9)
                        is PrinterStatus.Connecting, is PrinterStatus.Printing -> Color(0xFFFFF8E1)
                        is PrinterStatus.Error -> Color(0xFFFFEBEE)
                        PrinterStatus.Disconnected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            when (printerStatus) {
                                is PrinterStatus.Connected -> {
                                    Icon(
                                        Icons.Default.BluetoothConnected,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Connected: ${(printerStatus as PrinterStatus.Connected).deviceName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "Ready for POS & KOT printing",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                is PrinterStatus.Connecting -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFF57F17)
                                    )
                                    Column {
                                        Text(
                                            text = "Connecting to ${(printerStatus as PrinterStatus.Connecting).deviceName}...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE65100)
                                        )
                                        Text(
                                            text = "Opening Bluetooth SPP socket",
                                            fontSize = 11.sp,
                                            color = Color(0xFFF57F17)
                                        )
                                    }
                                }
                                is PrinterStatus.Printing -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PakEmeraldPrimary
                                    )
                                    Text(
                                        text = (printerStatus as PrinterStatus.Printing).jobName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PakEmeraldPrimary
                                    )
                                }
                                is PrinterStatus.Error -> {
                                    Icon(
                                        Icons.Default.BluetoothDisabled,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Connection Warning",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFB71C1C)
                                        )
                                        Text(
                                            text = (printerStatus as PrinterStatus.Error).message,
                                            fontSize = 10.sp,
                                            color = Color(0xFFC62828),
                                            maxLines = 2
                                        )
                                    }
                                }
                                PrinterStatus.Disconnected -> {
                                    Icon(
                                        Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "No Printer Connected",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Select a paired device below to connect",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (printerStatus is PrinterStatus.Connected) {
                            TextButton(onClick = { printerManager.disconnect() }) {
                                Text("Disconnect", fontSize = 11.sp, color = Color(0xFFC62828))
                            }
                        }
                    }
                }

                // Paper Width Selector (58mm vs 80mm)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Thermal Paper Roll Width (کاغذ کا سائز):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EscPosPaperSize.entries.forEach { size ->
                            val isSelected = selectedPaperSize == size
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PakEmeraldPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { printerManager.setPaperSize(size) }
                                    .testTag("btn_select_paper_${size.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${size.widthMm}mm Roll",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) PakEmeraldDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${size.columns} Chars/Line",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PakEmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Paired Bluetooth Devices List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paired Bluetooth Printers (${pairedDevices.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!hasPermission) {
                        TextButton(
                            onClick = {
                                val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    arrayOf(
                                        android.Manifest.permission.BLUETOOTH_CONNECT,
                                        android.Manifest.permission.BLUETOOTH_SCAN
                                    )
                                } else {
                                    arrayOf(android.Manifest.permission.BLUETOOTH)
                                }
                                permissionLauncher.launch(perms)
                            }
                        ) {
                            Text("Grant Permission", fontSize = 11.sp, color = PakEmeraldPrimary)
                        }
                    }
                }

                if (pairedDevices.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "No paired Bluetooth devices detected.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pair your thermal printer in Android Bluetooth Settings first, then tap Refresh.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pairedDevices) { device ->
                            val isConnected = (printerStatus as? PrinterStatus.Connected)?.deviceAddress == device.address
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isConnected) PakEmeraldContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isConnected) androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (device.isLikelyThermalPrinter) Icons.Default.Print else Icons.Default.Bluetooth,
                                            contentDescription = null,
                                            tint = if (device.isLikelyThermalPrinter) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = device.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (device.isLikelyThermalPrinter) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = PakGoldSecondary.copy(alpha = 0.2f)
                                                    ) {
                                                        Text("Thermal", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PakEmeraldDark, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = device.address,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isConnected) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PakEmeraldPrimary
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Text("Connected", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                printerManager.connectToPrinter(device.address, device.name) { success, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("btn_connect_${device.address.replace(":", "_")}")
                                        ) {
                                            Text("Connect", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (actionMessage.isNotBlank()) {
                    Text(
                        text = actionMessage,
                        fontSize = 11.sp,
                        color = PakEmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Test Print Button
                Button(
                    onClick = {
                        val activeName = (printerStatus as? PrinterStatus.Connected)?.deviceName ?: "POS Thermal"
                        val testBytes = EscPosUtil.generateTestPrintBytes(activeName, selectedPaperSize)
                        printerManager.printRawBytes(testBytes, "Test Calibration Slip") { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier.testTag("btn_test_print")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Print")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
