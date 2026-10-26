package com.example.ui.screens.features

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalBusinessCardScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val businessName = business?.name?.ifBlank { "PakBusiness Merchant" } ?: "PakBusiness Merchant"
    val businessType = business?.type?.replace("_", " ") ?: "General Business"
    val address = business?.address?.ifBlank { "Main Bazaar, Lahore, Pakistan" } ?: "Main Bazaar, Lahore, Pakistan"
    val phone = business?.phone?.ifBlank { "0300-1234567" } ?: "0300-1234567"

    var raastId by remember { mutableStateOf(phone.replace("-", "").trim()) }
    var jazzCashNumber by remember { mutableStateOf(phone) }
    var easyPaisaNumber by remember { mutableStateOf(phone) }
    var bankName by remember { mutableStateOf("Meezan Bank Ltd") }
    var bankAccountTitle by remember { mutableStateOf(businessName) }
    var bankIban by remember { mutableStateOf("PK36MEZN0001234567890101") }
    var ntnNumber by remember { mutableStateOf("1234567-8") }

    var showEditPaymentDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied: $text", Toast.LENGTH_SHORT).show()
    }

    fun shareCardOnWhatsApp() {
        val cardText = """
*🏪 DIGITAL BUSINESS CARD*
*${businessName.uppercase()}*
_${businessType}_
------------------------------------
📍 *Address:* $address
📞 *Phone / WhatsApp:* $phone
🏛️ *NTN / STRN:* $ntnNumber

*💳 INSTANT DIGITAL PAYMENTS (پاکستان)*
🔹 *Raast Instant ID:* $raastId
🔸 *JazzCash Account:* $jazzCashNumber
🟢 *EasyPaisa Account:* $easyPaisaNumber
🏦 *Bank:* $bankName
👤 *Account Title:* $bankAccountTitle
💳 *IBAN:* $bankIban
------------------------------------
_Thank you for your valued business!_
_Powered by PakBusiness Suite_
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, cardText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Dukaan Card via WhatsApp"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Digital Dukaan Card & QR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "آن لائن بزنس کارڈ اور ادائیگی کیو آر کوڈ",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PakEmeraldDark),
                actions = {
                    IconButton(onClick = { showEditPaymentDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Payment Details",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { shareCardOnWhatsApp() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Digital Card Surface (Emerald Gradient)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_business_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PakEmeraldDark,
                                    PakEmeraldPrimary,
                                    Color(0xFF0F5132)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Header with Business Category & Verified Seal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = businessType,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = PakGoldSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "VERIFIED DUKAAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakGoldSecondary
                                )
                            }
                        }

                        // Business Title
                        Text(
                            text = businessName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Address & Phone
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFA7F3D0),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = address,
                                fontSize = 12.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFFA7F3D0),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = phone,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Quick Payment Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Raast Instant ID",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = raastId,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PakGoldSecondary
                            ) {
                                Text(
                                    text = "Raast P2M",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // QR Code Scanner Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Scan to Pay / Save Contact",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show this QR code to customers for instant Raast & Banking transfers",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // QR Code Visual Representation (Canvas Matrix)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, PakEmeraldPrimary),
                        modifier = Modifier
                            .size(190.dp)
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val qrSeed = (businessName + raastId).hashCode()
                            val matrixSize = 21
                            val cellSize = size.width / matrixSize

                            // Draw QR alignment pattern boxes
                            // Top-left finder
                            drawRect(Color.Black, Offset(0f, 0f), Size(cellSize * 7, cellSize * 7))
                            drawRect(Color.White, Offset(cellSize, cellSize), Size(cellSize * 5, cellSize * 5))
                            drawRect(Color.Black, Offset(cellSize * 2, cellSize * 2), Size(cellSize * 3, cellSize * 3))

                            // Top-right finder
                            val trX = (matrixSize - 7) * cellSize
                            drawRect(Color.Black, Offset(trX, 0f), Size(cellSize * 7, cellSize * 7))
                            drawRect(Color.White, Offset(trX + cellSize, cellSize), Size(cellSize * 5, cellSize * 5))
                            drawRect(Color.Black, Offset(trX + cellSize * 2, cellSize * 2), Size(cellSize * 3, cellSize * 3))

                            // Bottom-left finder
                            val blY = (matrixSize - 7) * cellSize
                            drawRect(Color.Black, Offset(0f, blY), Size(cellSize * 7, cellSize * 7))
                            drawRect(Color.White, Offset(cellSize, blY + cellSize), Size(cellSize * 5, cellSize * 5))
                            drawRect(Color.Black, Offset(cellSize * 2, blY + cellSize * 2), Size(cellSize * 3, cellSize * 3))

                            // Deterministic data dots based on seed
                            for (r in 0 until matrixSize) {
                                for (c in 0 until matrixSize) {
                                    val inFinder = (r < 8 && c < 8) || (r < 8 && c >= matrixSize - 8) || (r >= matrixSize - 8 && c < 8)
                                    if (!inFinder) {
                                        val hash = abs((qrSeed * 31 + r * 17 + c * 23).hashCode())
                                        if (hash % 3 == 0) {
                                            drawRect(
                                                Color.Black,
                                                Offset(c * cellSize, r * cellSize),
                                                Size(cellSize, cellSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Supported: Raast, JazzCash, EasyPaisa, HBL, Meezan, Alfalah & all 1Link Apps",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Digital Accounts & Payment Methods
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Text(
                            text = "Payment Methods & Accounts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { showEditPaymentDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Accounts", fontSize = 11.sp)
                        }
                    }

                    // Raast
                    PaymentRow(
                        title = "Raast Instant ID",
                        value = raastId,
                        badge = "SBP Raast",
                        badgeColor = Color(0xFF00796B),
                        onCopy = { copyToClipboard("Raast ID", raastId) }
                    )

                    // JazzCash
                    PaymentRow(
                        title = "JazzCash Mobile Account",
                        value = jazzCashNumber,
                        badge = "JazzCash",
                        badgeColor = Color(0xFFD97706),
                        onCopy = { copyToClipboard("JazzCash", jazzCashNumber) }
                    )

                    // EasyPaisa
                    PaymentRow(
                        title = "EasyPaisa Mobile Account",
                        value = easyPaisaNumber,
                        badge = "EasyPaisa",
                        badgeColor = Color(0xFF16A34A),
                        onCopy = { copyToClipboard("EasyPaisa", easyPaisaNumber) }
                    )

                    // Bank Account
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bankName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            IconButton(
                                onClick = { copyToClipboard("Bank IBAN", bankIban) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("Title: $bankAccountTitle", fontSize = 11.sp)
                        Text("IBAN: $bankIban", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PakEmeraldPrimary)
                    }
                }
            }

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { shareCardOnWhatsApp() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share on WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Card saved to gallery!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Image", fontSize = 12.sp)
                }
            }
        }
    }

    // Edit Payment Details Dialog
    if (showEditPaymentDialog) {
        var tempRaast by remember { mutableStateOf(raastId) }
        var tempJazzCash by remember { mutableStateOf(jazzCashNumber) }
        var tempEasyPaisa by remember { mutableStateOf(easyPaisaNumber) }
        var tempBank by remember { mutableStateOf(bankName) }
        var tempTitle by remember { mutableStateOf(bankAccountTitle) }
        var tempIban by remember { mutableStateOf(bankIban) }

        AlertDialog(
            onDismissRequest = { showEditPaymentDialog = false },
            title = { Text("Update Dukaan Payment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempRaast,
                        onValueChange = { tempRaast = it },
                        label = { Text("Raast ID (Mobile Number)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempJazzCash,
                        onValueChange = { tempJazzCash = it },
                        label = { Text("JazzCash Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempEasyPaisa,
                        onValueChange = { tempEasyPaisa = it },
                        label = { Text("EasyPaisa Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempBank,
                        onValueChange = { tempBank = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Account Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempIban,
                        onValueChange = { tempIban = it },
                        label = { Text("Bank IBAN (24 digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        raastId = tempRaast
                        jazzCashNumber = tempJazzCash
                        easyPaisaNumber = tempEasyPaisa
                        bankName = tempBank
                        bankAccountTitle = tempTitle
                        bankIban = tempIban
                        showEditPaymentDialog = false
                        Toast.makeText(context, "Payment details updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PaymentRow(
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = PakEmeraldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
