package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.util.BarcodeScannerUtil

/**
 * Fullscreen or Dialog Barcode Scanner component with camera viewfinder reticle,
 * laser scanline animation, torch toggle, and fast manual SKU/barcode input for POS lookup.
 */
@Composable
fun BarcodeScannerDialog(
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sampleBarcodes: List<String> = listOf("8964000123456", "8964000789012", "7622210987654", "PKG-1004")
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isTorchOn by remember { mutableStateOf(false) }
    var manualBarcodeInput by remember { mutableStateOf("") }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && BarcodeScannerUtil.hasCameraHardware(context)) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("barcode_scanner_dialog"),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. Viewfinder Layer & Scanner Animation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        // Real CameraX viewfinder stream & scanner reticle
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                                .testTag("camera_viewfinder")
                        ) {
                            CameraXLivePreviewView(
                                isTorchOn = isTorchOn,
                                onBarcodeDetected = { code ->
                                    val sanitized = BarcodeScannerUtil.sanitizeBarcode(code)
                                    if (sanitized.isNotBlank()) {
                                        BarcodeScannerUtil.triggerSuccessHaptic(context)
                                        lastScannedCode = sanitized
                                        onBarcodeScanned(sanitized)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            AnimatedScanLaser()

                            // Corner Target Markers
                            TargetCornerReticles()
                        }
                    } else {
                        // Permission Request Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Grant camera permission to instantly scan product barcodes and EAN labels at POS checkout.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    modifier = Modifier.fillMaxWidth().testTag("grant_camera_permission_btn")
                                ) {
                                    Text("Enable Camera")
                                }
                            }
                        }
                    }
                }

                // 2. Top Bar (Close Button, Title, Torch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("close_scanner_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Text(
                        text = "Scan Barcode / QR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = { isTorchOn = !isTorchOn },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("toggle_torch_btn")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isTorchOn) Color(0xFFFFD600) else Color.White
                        )
                    }
                }

                // 3. Bottom Controls & Manual Input Sheet
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Align barcode within the green frame",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Manual Barcode Entry Field
                        OutlinedTextField(
                            value = manualBarcodeInput,
                            onValueChange = { manualBarcodeInput = it },
                            placeholder = { Text("Or enter Barcode / SKU manually") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_barcode_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    val code = BarcodeScannerUtil.sanitizeBarcode(manualBarcodeInput)
                                    if (code.isNotBlank()) {
                                        BarcodeScannerUtil.triggerSuccessHaptic(context)
                                        lastScannedCode = code
                                        onBarcodeScanned(code)
                                    }
                                }
                            ),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                            },
                            trailingIcon = {
                                if (manualBarcodeInput.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val code = BarcodeScannerUtil.sanitizeBarcode(manualBarcodeInput)
                                            if (code.isNotBlank()) {
                                                BarcodeScannerUtil.triggerSuccessHaptic(context)
                                                lastScannedCode = code
                                                onBarcodeScanned(code)
                                            }
                                        },
                                        modifier = Modifier.testTag("submit_manual_barcode_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Quick Test Barcodes Row (Useful for quick selection & POS emulator workflows)
                        if (sampleBarcodes.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Quick Test:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                sampleBarcodes.take(3).forEach { sample ->
                                    Surface(
                                        onClick = {
                                            BarcodeScannerUtil.triggerSuccessHaptic(context)
                                            lastScannedCode = sample
                                            onBarcodeScanned(sample)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = sample,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Last Scanned Success Notification
                        AnimatedVisibility(visible = lastScannedCode != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Text("Scanned: ${lastScannedCode.orEmpty()}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedScanLaser() {
    val infiniteTransition = rememberInfiniteTransition()
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val y = size.height * laserOffsetY
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00E676),
                    Color(0xFF00E676),
                    Color.Transparent
                )
            ),
            start = Offset(20f, y),
            end = Offset(size.width - 20f, y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
private fun TargetCornerReticles() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cornerLength = 24.dp.toPx()
        val strokeW = 4.dp.toPx()
        val c = Color(0xFF00E676)

        // Top Left
        drawLine(c, Offset(0f, 0f), Offset(cornerLength, 0f), strokeW)
        drawLine(c, Offset(0f, 0f), Offset(0f, cornerLength), strokeW)

        // Top Right
        drawLine(c, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeW)
        drawLine(c, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeW)

        // Bottom Left
        drawLine(c, Offset(0f, size.height), Offset(cornerLength, size.height), strokeW)
        drawLine(c, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeW)

        // Bottom Right
        drawLine(c, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeW)
        drawLine(c, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeW)
    }
}

/**
 * CameraX Live Preview with Google ML Kit Barcode Analyzer.
 * Streams device camera preview and rapidly parses 1D & 2D barcodes in real time.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXLivePreviewView(
    isTorchOn: Boolean,
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var lastDetectedCode by remember { mutableStateOf("") }
    var lastDetectedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isTorchOn, camera) {
        try {
            camera?.cameraControl?.enableTorch(isTorchOn)
        } catch (_: Exception) {}
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { raw ->
                                            val now = System.currentTimeMillis()
                                            if (raw.isNotBlank() && (raw != lastDetectedCode || now - lastDetectedTime > 1800L)) {
                                                lastDetectedCode = raw
                                                lastDetectedTime = now
                                                onBarcodeDetected(raw)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

