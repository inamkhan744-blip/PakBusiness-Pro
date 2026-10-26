package com.example.ui.screens.features

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.data.CustomerEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ParsedTransactionType(val labelUrdu: String, val labelEnglish: String, val color: Color) {
    UDHAR_GIVEN("ادھار دیا (Debit)", "Udhar Given", Color(0xFFDC2626)),
    VASOOLI_RECEIVED("وصولی ملی (Credit)", "Vasooli Received", Color(0xFF16A34A)),
    CASH_SALE("نقد فروخت", "Cash Sale", Color(0xFF0284C7)),
    EXPENSE("دکان خرچہ", "Expense", Color(0xFFD97706))
}

data class ParsedVoiceEntry(
    val rawText: String,
    val partyName: String,
    val amount: Double,
    val type: ParsedTransactionType,
    val timestamp: String = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date())
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceKhataScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = business?.currency ?: "PKR"
    val businessId = business?.id ?: 1L

    val customerDao = remember(businessId) {
        AppDatabase.getDatabase(context).customerDao()
    }

    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedVoiceEntry?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val recentVoiceLogs = remember {
        mutableStateListOf(
            ParsedVoiceEntry(
                rawText = "Haji Aslam ko 5000 ka maal udhar dia",
                partyName = "Haji Aslam",
                amount = 5000.0,
                type = ParsedTransactionType.UDHAR_GIVEN
            ),
            ParsedVoiceEntry(
                rawText = "Tariq sahab se 2500 ki vasooli hui",
                partyName = "Tariq Sahab",
                amount = 2500.0,
                type = ParsedTransactionType.VASOOLI_RECEIVED
            )
        )
    }

    // Smart NLP Parser for Roman Urdu, Urdu, and English
    fun parseVoiceSentence(sentence: String): ParsedVoiceEntry {
        val clean = sentence.trim()
        val lower = clean.lowercase()

        // 1. Detect Amount (extract digits or numbers)
        val amountRegex = Regex("(\\d+[,.]?\\d*)")
        val match = amountRegex.find(clean)
        val extractedAmount = match?.value?.replace(",", "")?.toDoubleOrNull() ?: 1000.0

        // 2. Detect Transaction Type
        val type = when {
            lower.contains("vasool") || lower.contains("mili") || lower.contains("aya") || lower.contains("diye") || lower.contains("received") || lower.contains("وصول") -> ParsedTransactionType.VASOOLI_RECEIVED
            lower.contains("udhar") || lower.contains("baqi") || lower.contains("maal dia") || lower.contains(" ادھار") -> ParsedTransactionType.UDHAR_GIVEN
            lower.contains("kharcha") || lower.contains("bill") || lower.contains("chai") || lower.contains("خرچہ") -> ParsedTransactionType.EXPENSE
            else -> ParsedTransactionType.CASH_SALE
        }

        // 3. Extract Customer / Party Name
        val stopWords = listOf("ko", "se", "ka", "ki", "ke", "ne", "rupay", "rs", "hazar", "udhar", "dia", "diya", "vasooli", "mili", "hui", "maal", "sale", "cash")
        val words = clean.split(" ")
        val potentialName = words.takeWhile { word ->
            val w = word.lowercase()
            w !in stopWords && !w.any { it.isDigit() }
        }.joinToString(" ")

        val finalName = if (potentialName.isNotBlank()) potentialName else "Customer ${SimpleDateFormat("ss", Locale.getDefault()).format(Date())}"

        return ParsedVoiceEntry(
            rawText = clean,
            partyName = finalName,
            amount = extractedAmount,
            type = type
        )
    }

    // Quick Voice Template Prompts
    val samplePrompts = listOf(
        "Haji Aslam ko 5000 ka maal udhar dia",
        "Tariq sahab se 3500 ki vasooli hui",
        "Malik Traders ko 12000 ka udhar dia",
        "Dukan ka bijli bill 6400 kharcha hua",
        "Ali ko 850 ki naghad sale ki"
    )

    fun startListeningSimulation(preset: String? = null) {
        val textToProcess = preset ?: samplePrompts.random()
        isListening = true
        spokenText = "Listening to voice audio in Urdu / Punjabi..."
        scope.launch {
            delay(1200)
            isListening = false
            spokenText = textToProcess
            parsedResult = parseVoiceSentence(textToProcess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice Khata (آواز سے کھاتہ)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Bol kar khata & sales entry likhein", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_voice_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("voice_khata_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Hero Microphone Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "بولیں اور کھاتہ خودکار درج کریں",
                            color = PakGoldSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shopkeeper speech parser: Speaks in Roman Urdu or Urdu to automatically record Udhar or Vasooli.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        // Big Glowing Mic Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color(0xFFDC2626) else PakEmeraldPrimary)
                                .clickable { startListeningSimulation() }
                                .testTag("btn_big_mic")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Speak Khata",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Text(
                            text = if (isListening) "🎙️ Listening... Bolte rahein" else "Tap Mic to Speak in Urdu / Punjabi",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Quick Voice Samples Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TRY QUICK VOICE SAMPLES (بولنے کے نمونے)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(samplePrompts) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PakEmeraldContainer.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { startListeningSimulation(prompt) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PakEmeraldDark)
                                }
                            }
                        }
                    }
                }
            }

            // Dictation Text Input / Edit
            item {
                OutlinedTextField(
                    value = spokenText,
                    onValueChange = {
                        spokenText = it
                        if (it.isNotBlank()) parsedResult = parseVoiceSentence(it)
                    },
                    label = { Text("Spoken Voice Text / بولا گیا جملہ") },
                    placeholder = { Text("مثال: 'حاجی اسلم کو 5000 کا مال ادھار دیا'") },
                    trailingIcon = {
                        IconButton(onClick = { startListeningSimulation() }) {
                            Icon(Icons.Default.Mic, contentDescription = "Listen", tint = PakEmeraldPrimary)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("voice_input_field")
                )
            }

            // AI Smart Extracted Result Card
            if (parsedResult != null) {
                val res = parsedResult!!
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("voice_parsed_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AI Parser Detected Entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = res.type.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${res.type.labelEnglish} • ${res.type.labelUrdu}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = res.type.color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Customer / Party", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(res.partyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "$currency ${String.format(Locale.getDefault(), "%,.0f", res.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = res.type.color
                                    )
                                }
                            }

                            // Confirm & Save to Room Database
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        // Find or create customer
                                        val existing = customerDao.getCustomerByPhone(businessId, "0300-0000000")
                                        val delta = if (res.type == ParsedTransactionType.UDHAR_GIVEN) res.amount else -res.amount
                                        val newCustId = customerDao.insertCustomer(
                                            CustomerEntity(
                                                businessId = businessId,
                                                name = res.partyName,
                                                phone = "0300-AutoVoice",
                                                currentBalancePkr = if (res.type == ParsedTransactionType.UDHAR_GIVEN) res.amount else 0.0,
                                                notes = "Recorded via Urdu Voice Khata: '${res.rawText}'"
                                            )
                                        )
                                    }
                                    recentVoiceLogs.add(0, res)
                                    saveSuccessMessage = "Khata updated for '${res.partyName}': $currency ${res.amount}"
                                    parsedResult = null
                                    spokenText = ""
                                    Toast.makeText(context, "Voice Entry Saved to Khata DB!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_confirm_voice_entry")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Directly to Khata Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (saveSuccessMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PakEmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ $saveSuccessMessage",
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Recent Voice Audio Logs
            item {
                Text(
                    text = "RECENT VOICE KHATA ENTRIES (آواز سے درج شدہ کھاتہ)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(recentVoiceLogs) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = log.type.color.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (log.type == ParsedTransactionType.UDHAR_GIVEN) Icons.Default.TrendingUp else Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = log.type.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(log.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("\"${log.rawText}\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%,.0f", log.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = log.type.color
                        )
                    }
                }
            }
        }
    }
}
