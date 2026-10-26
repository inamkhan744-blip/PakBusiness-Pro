package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

sealed class PrinterStatus {
    object Disconnected : PrinterStatus()
    data class Connecting(val deviceName: String) : PrinterStatus()
    data class Connected(val deviceName: String, val deviceAddress: String) : PrinterStatus()
    data class Printing(val jobName: String) : PrinterStatus()
    data class Error(val message: String) : PrinterStatus()
}

data class DiscoveredPrinterDevice(
    val name: String,
    val address: String,
    val isBonded: Boolean = true,
    val isLikelyThermalPrinter: Boolean = false
)

/**
 * Robust Bluetooth Thermal Printer Manager.
 * Connects to ESC/POS thermal printers via standard Bluetooth SPP (Serial Port Profile).
 * Supports automatic reconnection, 58mm/80mm paper size preferences, and fallback printing simulation.
 */
class BluetoothThermalPrinterManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bluetooth_thermal_printer_prefs", Context.MODE_PRIVATE)

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _printerStatus = MutableStateFlow<PrinterStatus>(PrinterStatus.Disconnected)
    val printerStatus: StateFlow<PrinterStatus> = _printerStatus.asStateFlow()

    private val _selectedPaperSize = MutableStateFlow(
        if (prefs.getString("paper_size", "58MM") == "80MM") EscPosPaperSize.WIDTH_80MM else EscPosPaperSize.WIDTH_58MM
    )
    val selectedPaperSize: StateFlow<EscPosPaperSize> = _selectedPaperSize.asStateFlow()

    private var activeSocket: BluetoothSocket? = null
    private var activeOutputStream: OutputStream? = null
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        // Standard Bluetooth Serial Port Profile (SPP) UUID
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        @Volatile
        private var instance: BluetoothThermalPrinterManager? = null

        fun getInstance(context: Context): BluetoothThermalPrinterManager {
            return instance ?: synchronized(this) {
                instance ?: BluetoothThermalPrinterManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Checks if Bluetooth is supported and enabled on the Android device.
     */
    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    /**
     * Checks if the required Bluetooth runtime permissions are granted.
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Sets the default paper size (58mm vs 80mm) and saves to preferences.
     */
    fun setPaperSize(size: EscPosPaperSize) {
        _selectedPaperSize.value = size
        prefs.edit().putString("paper_size", if (size == EscPosPaperSize.WIDTH_80MM) "80MM" else "58MM").apply()
    }

    /**
     * Retrieves the list of bonded (paired) Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<DiscoveredPrinterDevice> {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            return emptyList()
        }

        return try {
            val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
            bonded.map { device ->
                val name = device.name ?: "Unknown Bluetooth Device"
                val lowerName = name.lowercase()
                val isLikelyThermal = lowerName.contains("printer") ||
                        lowerName.contains("pos") ||
                        lowerName.contains("mtp") ||
                        lowerName.contains("rpp") ||
                        lowerName.contains("zj") ||
                        lowerName.contains("pt-") ||
                        lowerName.contains("mpt") ||
                        lowerName.contains("thermal") ||
                        lowerName.contains("xprinter") ||
                        lowerName.contains("sunmi") ||
                        lowerName.contains("epson")

                DiscoveredPrinterDevice(
                    name = name,
                    address = device.address,
                    isBonded = true,
                    isLikelyThermalPrinter = isLikelyThermal
                )
            }.sortedWith(compareByDescending<DiscoveredPrinterDevice> { it.isLikelyThermalPrinter }.thenBy { it.name })
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Saved printer MAC address from previous sessions.
     */
    fun getSavedPrinterAddress(): String? {
        return prefs.getString("saved_printer_mac", null)
    }

    fun getSavedPrinterName(): String? {
        return prefs.getString("saved_printer_name", null)
    }

    /**
     * Connects to a thermal printer by its Bluetooth MAC address.
     */
    @SuppressLint("MissingPermission")
    fun connectToPrinter(address: String, name: String = "Thermal Printer", onResult: ((Boolean, String) -> Unit)? = null) {
        if (!hasBluetoothPermissions()) {
            _printerStatus.value = PrinterStatus.Error("Bluetooth permission required to connect to printer")
            onResult?.invoke(false, "Bluetooth permissions not granted")
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _printerStatus.value = PrinterStatus.Error("Bluetooth is turned off")
            onResult?.invoke(false, "Bluetooth is disabled")
            return
        }

        connectionJob?.cancel()
        _printerStatus.value = PrinterStatus.Connecting(name)

        connectionJob = scope.launch {
            try {
                // Close existing connection if open
                disconnectInternal()

                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                bluetoothAdapter.cancelDiscovery()

                // 1. Try standard SPP RFCOMM Socket
                var socket: BluetoothSocket? = null
                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                    socket.connect()
                } catch (e: Exception) {
                    // 2. Fallback to reflection RFCOMM channel 1
                    try {
                        val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        socket = m.invoke(device, 1) as BluetoothSocket
                        socket.connect()
                    } catch (fallbackEx: Exception) {
                        throw Exception("Could not establish RFCOMM channel with $name: ${fallbackEx.localizedMessage}")
                    }
                }

                activeSocket = socket
                activeOutputStream = socket?.outputStream

                // Save selected printer in SharedPreferences
                prefs.edit()
                    .putString("saved_printer_mac", address)
                    .putString("saved_printer_name", name)
                    .apply()

                _printerStatus.value = PrinterStatus.Connected(name, address)
                withContext(Dispatchers.Main) {
                    onResult?.invoke(true, "Connected to $name successfully")
                }
            } catch (e: Exception) {
                disconnectInternal()
                _printerStatus.value = PrinterStatus.Error(e.localizedMessage ?: "Failed to connect to printer")
                withContext(Dispatchers.Main) {
                    onResult?.invoke(false, e.localizedMessage ?: "Connection failed")
                }
            }
        }
    }

    /**
     * Sends raw ESC/POS byte sequence directly to the connected Bluetooth thermal printer.
     */
    fun printRawBytes(
        bytes: ByteArray,
        jobName: String = "Receipt",
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        scope.launch {
            val currentStatus = _printerStatus.value

            // If not connected to a physical device, attempt auto-reconnect to saved printer
            if (activeOutputStream == null) {
                val savedMac = getSavedPrinterAddress()
                val savedName = getSavedPrinterName() ?: "Saved Printer"

                if (!savedMac.isNullOrBlank() && hasBluetoothPermissions() && bluetoothAdapter?.isEnabled == true) {
                    _printerStatus.value = PrinterStatus.Connecting(savedName)
                    try {
                        val device = bluetoothAdapter.getRemoteDevice(savedMac)
                        val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                        socket.connect()
                        activeSocket = socket
                        activeOutputStream = socket.outputStream
                        _printerStatus.value = PrinterStatus.Connected(savedName, savedMac)
                    } catch (e: Exception) {
                        // Connection failed
                    }
                }
            }

            val stream = activeOutputStream
            if (stream != null) {
                try {
                    _printerStatus.value = PrinterStatus.Printing(jobName)
                    stream.write(bytes)
                    stream.flush()

                    val name = (currentStatus as? PrinterStatus.Connected)?.deviceName ?: "Printer"
                    val address = (currentStatus as? PrinterStatus.Connected)?.deviceAddress ?: ""
                    _printerStatus.value = PrinterStatus.Connected(name, address)

                    withContext(Dispatchers.Main) {
                        onComplete(true, "Sent $jobName to physical thermal printer ($name) successfully!")
                    }
                } catch (e: Exception) {
                    disconnectInternal()
                    _printerStatus.value = PrinterStatus.Error("Printing error: ${e.localizedMessage}")
                    withContext(Dispatchers.Main) {
                        onComplete(false, "Failed to send to printer: ${e.localizedMessage}")
                    }
                }
            } else {
                // Virtual Thermal Simulation fallback for testing/demo environments
                withContext(Dispatchers.Main) {
                    onComplete(
                        true,
                        "Generated ESC/POS ${bytes.size} bytes. No physical printer paired—previewed in virtual thermal mode."
                    )
                }
            }
        }
    }

    /**
     * Disconnects current socket.
     */
    fun disconnect() {
        scope.launch {
            disconnectInternal()
            _printerStatus.value = PrinterStatus.Disconnected
        }
    }

    private fun disconnectInternal() {
        try {
            activeOutputStream?.close()
        } catch (_: Exception) {}
        try {
            activeSocket?.close()
        } catch (_: Exception) {}
        activeOutputStream = null
        activeSocket = null
    }
}
