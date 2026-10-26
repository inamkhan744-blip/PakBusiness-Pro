package com.example.ui.screens.features

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxAndFbrCalculatorScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"
    val businessName = business?.name ?: "PakBusiness Store"

    var invoiceAmountInput by remember { mutableStateOf("100000") }
    var isFiler by remember { mutableStateOf(true) }
    var taxTypeSelection by remember { mutableStateOf("GST_18") } // "GST_18", "PRA_16", "SRB_15", "EXEMPT"
    var applyWht236G by remember { mutableStateOf(true) }

    // Annual Income Tax Estimator
    var annualSalesInput by remember { mutableStateOf("5000000") }
    var netProfitMarginPercent by remember { mutableStateOf("15") }

    val amount = invoiceAmountInput.toDoubleOrNull() ?: 0.0

    // GST calculation
    val salesTaxRate = when (taxTypeSelection) {
        "GST_18" -> 18.0
        "PRA_16" -> 16.0
        "SRB_15" -> 15.0
        else -> 0.0
    }
    val salesTaxAmount = (amount * salesTaxRate) / 100.0

    // Advance Withholding Tax 236G / 236H
    val whtRate = if (applyWht236G) {
        if (isFiler) 0.5 else 2.5
    } else 0.0
    val whtAmount = (amount * whtRate) / 100.0

    val grandInvoiceTotal = amount + salesTaxAmount + whtAmount

    // Annual Tax Estimator calculation
    val annualSales = annualSalesInput.toDoubleOrNull() ?: 0.0
    val profitMargin = netProfitMarginPercent.toDoubleOrNull() ?: 15.0
    val estimatedTaxableIncome = (annualSales * profitMargin) / 100.0

    // Pakistan FBR Tax Slabs for Individuals/AOPs (Finance Act)
    val estimatedAnnualTax = remember(estimatedTaxableIncome) {
        when {
            estimatedTaxableIncome <= 600000 -> 0.0
            estimatedTaxableIncome <= 1200000 -> (estimatedTaxableIncome - 600000) * 0.05
            estimatedTaxableIncome <= 2200000 -> 30000.0 + (estimatedTaxableIncome - 1200000) * 0.15
            estimatedTaxableIncome <= 3200000 -> 180000.0 + (estimatedTaxableIncome - 2200000) * 0.25
            estimatedTaxableIncome <= 4100000 -> 430000.0 + (estimatedTaxableIncome - 3200000) * 0.30
            else -> 700000.0 + (estimatedTaxableIncome - 4100000) * 0.35
        }
    }

    fun shareTaxCalculation() {
        val summary = """
*📊 PAKISTAN FBR & SALES TAX CALCULATION*
🏪 *${businessName}*
---------------------------------------
💰 *Base Invoice Value:* $currency ${String.format("%,.0f", amount)}
👤 *Tax Status:* ${if (isFiler) "Active Filer (ATL)" else "Non-Filer (Penal Rate)"}
🏛️ *Sales Tax (${salesTaxRate}%):* $currency ${String.format("%,.0f", salesTaxAmount)}
📌 *Advance WHT u/s 236G/H (${whtRate}%):* $currency ${String.format("%,.0f", whtAmount)}
*TOTAL TAX INVOICE:* $currency ${String.format("%,.0f", grandInvoiceTotal)}
---------------------------------------
*ANNUAL INCOME TAX ESTIMATE:*
Gross Sales: $currency ${String.format("%,.0f", annualSales)}
Estimated Taxable Net Profit: $currency ${String.format("%,.0f", estimatedTaxableIncome)}
*Estimated Annual FBR Tax Liability:* $currency ${String.format("%,.0f", estimatedAnnualTax)}

_Prepared with PakBusiness Suite Tax Helper_
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(intent, "Share Tax Summary with Munshi / Accountant"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FBR & Sales Tax Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "سیلز ٹیکس، ودہولڈنگ ٹیکس و انکم ٹیکس گوشوارے",
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
                    IconButton(onClick = { shareTaxCalculation() }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
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
            // Quick Filer Status Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Taxpayer Status (ATL Active Filer)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFiler) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isFiler = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = if (isFiler) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                Text("Active Filer", color = if (isFiler) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isFiler) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isFiler = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = if (!isFiler) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                Text("Non-Filer", color = if (!isFiler) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Sales Tax & WHT Calculator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Invoice Sales Tax & WHT Calculator", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = invoiceAmountInput,
                        onValueChange = { invoiceAmountInput = it },
                        label = { Text("Base Invoice Value ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Text("Sales Tax Jurisdiction:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = taxTypeSelection == "GST_18",
                            onClick = { taxTypeSelection = "GST_18" },
                            label = { Text("FBR GST 18%", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = taxTypeSelection == "PRA_16",
                            onClick = { taxTypeSelection = "PRA_16" },
                            label = { Text("PRA Punjab 16%", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = taxTypeSelection == "SRB_15",
                            onClick = { taxTypeSelection = "SRB_15" },
                            label = { Text("SRB Sindh 15%", fontSize = 10.sp) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PakEmeraldContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Include WHT Section 236G/H (${if (isFiler) "0.5%" else "2.5%"}):", fontSize = 11.sp)
                        Button(
                            onClick = { applyWht236G = !applyWht236G },
                            colors = ButtonDefaults.buttonColors(containerColor = if (applyWht236G) PakEmeraldPrimary else Color.Gray),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (applyWht236G) "Active" else "Off", fontSize = 10.sp)
                        }
                    }

                    HorizontalDivider()

                    // Calculation Summary Table
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Amount:", fontSize = 12.sp)
                            Text("$currency ${String.format("%,.0f", amount)}", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sales Tax (${salesTaxRate}%):", fontSize = 12.sp)
                            Text("+ $currency ${String.format("%,.0f", salesTaxAmount)}", fontSize = 12.sp, color = Color(0xFF2563EB))
                        }
                        if (applyWht236G) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Advance WHT (${whtRate}%):", fontSize = 12.sp)
                                Text("+ $currency ${String.format("%,.0f", whtAmount)}", fontSize = 12.sp, color = Color(0xFFD97706))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Final Tax Invoice Total:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$currency ${String.format("%,.0f", grandInvoiceTotal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        }
                    }
                }
            }

            // Annual Income Tax Estimator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Annual Income Tax Return Estimator (گوشوارہ)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Estimate annual FBR income tax based on yearly turnover & standard margins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = annualSalesInput,
                            onValueChange = { annualSalesInput = it },
                            label = { Text("Annual Turnover ($currency)") },
                            modifier = Modifier.weight(1.3f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = netProfitMarginPercent,
                            onValueChange = { netProfitMarginPercent = it },
                            label = { Text("Profit %") },
                            modifier = Modifier.weight(0.7f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Taxable Net Profit:", fontSize = 11.sp)
                                Text("$currency ${String.format("%,.0f", estimatedTaxableIncome)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Annual FBR Tax Liability:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                Text("$currency ${String.format("%,.0f", estimatedAnnualTax)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                            Text(
                                text = "Note: Calculated as per Tax Slabs for Business Individuals / AOP under Income Tax Ordinance.",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Share Action Button
            Button(
                onClick = { shareTaxCalculation() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Tax Computation on WhatsApp / Munshi", fontWeight = FontWeight.Bold)
            }
        }
    }
}
