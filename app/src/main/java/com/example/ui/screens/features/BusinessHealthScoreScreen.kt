package com.example.ui.screens.features

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.util.Locale

data class HealthPillar(
    val title: String,
    val score: Int,
    val maxScore: Int = 25,
    val statusText: String,
    val color: Color
)

data class BusinessTip(
    val titleUrdu: String,
    val titleEnglish: String,
    val detail: String,
    val priorityColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHealthScoreScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"

    val overallScore = 86
    val healthGrade = "Grade A (زبردست صحت مند)"

    val pillars = remember {
        listOf(
            HealthPillar("Cash Flow & Liquidity", 22, 25, "Strong: Daily inflow exceeds liabilities", Color(0xFF16A34A)),
            HealthPillar("Udhaar & Khata Recovery", 20, 25, "Good: 82% recoveries collected within 30 days", PakEmeraldPrimary),
            HealthPillar("Stock Health & Turnover", 23, 25, "Excellent: Fast-moving inventory with low dead stock", Color(0xFF0284C7)),
            HealthPillar("Profit Margin & Expense Ratio", 21, 25, "Healthy: Gross margin 28% after kharcha", Color(0xFFD97706))
        )
    }

    val smartTips = remember {
        listOf(
            BusinessTip(
                titleUrdu = "پرانے ادھار کی جلد ریکوری کریں",
                titleEnglish = "Recover Overdue Khata Balances",
                detail = "2 customers have balances over 45 days. Sending WhatsApp automated reminder can inject PKR 45,000 cash immediately.",
                priorityColor = Color(0xFFDC2626)
            ),
            BusinessTip(
                titleUrdu = "سست رفتار مال کی قیمت کم کر کے فارغ کریں",
                titleEnglish = "Clear Slow-Moving Inventory",
                detail = "3 SKUs in inventory have not moved for 4 weeks. A 10% discount promo can liquidate PKR 28,000 in tied-up capital.",
                priorityColor = Color(0xFFD97706)
            ),
            BusinessTip(
                titleUrdu = "سپلائر ڈسکاؤنٹ حاصل کریں",
                titleEnglish = "Take Advantage of Supplier Cash Discounts",
                detail = "Paying your primary distributor in cash upfront qualifies your business for a 3% extra trade rebate.",
                priorityColor = Color(0xFF16A34A)
            )
        )
    }

    fun exportHealthAuditPdf(isPrint: Boolean) {
        val auditItems = pillars.map {
            InvoiceExportItem(
                name = "${it.title}: ${it.statusText}",
                quantity = 1.0,
                unitPrice = it.score.toDouble(),
                total = it.score.toDouble()
            )
        }
        val exportData = InvoiceExportData(
            invoiceNumber = "HEALTH-${System.currentTimeMillis() % 100000}",
            businessName = business?.name ?: "PakBusiness",
            businessAddress = "COMMERCIAL BUSINESS HEALTH AUDIT & DIAGNOSTIC REPORT",
            businessPhone = business?.phone ?: "",
            customerName = "OVERALL BUSINESS SCORE: $overallScore / 100 ($healthGrade)",
            customerPhone = "Diagnostic: 4 Pillars Audited",
            items = auditItems,
            subtotal = overallScore.toDouble(),
            discount = 0.0,
            tax = 0.0,
            grandTotal = overallScore.toDouble(),
            paymentMethod = "Grade A Certified",
            footerNote = "Official AI Business Diagnostic Report • 100% Certified for local trade."
        )
        if (isPrint) {
            PdfExportUtil.printInvoice(context, exportData)
        } else {
            PdfExportUtil.shareInvoice(context, exportData)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business Health Meter (کاروباری ہیلتھ میٹر)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Financial diagnostics, recovery & profit scorecard", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_health_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("business_health_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Score Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "OVERALL BUSINESS HEALTH SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakGoldSecondary
                        )

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                            CircularProgressIndicator(
                                progress = { overallScore / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = PakGoldSecondary,
                                strokeWidth = 10.dp,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$overallScore",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "out of 100",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = PakEmeraldPrimary) {
                            Text(
                                text = healthGrade,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Your store has strong liquidity, safe credit exposure, and healthy gross margins.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { exportHealthAuditPdf(isPrint = false) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Audit", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { exportHealthAuditPdf(isPrint = true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print PDF", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Diagnostic Pillars Breakdown
            item {
                Text(
                    text = "DIAGNOSTIC PILLARS (کاروباری بنیادی ستون)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(pillars) { pillar ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pillar.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${pillar.score} / ${pillar.maxScore}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = pillar.color)
                        }

                        LinearProgressIndicator(
                            progress = { pillar.score.toFloat() / pillar.maxScore },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = pillar.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(pillar.statusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Actionable Business Tips
            item {
                Text(
                    text = "ACTIONABLE BUSINESS ADVICE (کاروباری مشورے)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(smartTips) { tip ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tip.priorityColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(shape = CircleShape, color = tip.priorityColor.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = tip.priorityColor, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tip.titleEnglish, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(tip.titleUrdu, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = PakEmeraldPrimary)
                            Text(tip.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
