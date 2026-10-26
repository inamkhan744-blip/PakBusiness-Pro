package com.example.ui.screens.tools

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

@Composable
fun PakBusinessToolsScreen(
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }

    val toolTitles = listOf(
        "Cash Galla",
        "Maund / KG",
        "Gold / Tola",
        "Fabric Gaz/M",
        "GST / Tax",
        "Zakat 2.5%",
        "Dalali / Comm.",
        "Milk / Doodh",
        "Saria / Cement",
        "Profit Margin"
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Tools Sub-Tab Row with horizontal scrolling for rich Pakistani trade tools
        ScrollableTabRow(
            selectedTabIndex = selectedToolIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PakEmeraldPrimary,
            edgePadding = 8.dp
        ) {
            toolTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolIndex == index,
                    onClick = { selectedToolIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedToolIndex == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        when (selectedToolIndex) {
            0 -> CashDenominationCalculator(appLanguage)
            1 -> MandiMaundCalculator(appLanguage)
            2 -> GoldJewelryCalculator(appLanguage)
            3 -> FabricMeasurementCalculator(appLanguage)
            4 -> GstTaxCalculator(appLanguage)
            5 -> ZakatCalculator(appLanguage)
            6 -> CommissionBrokerageCalculator(appLanguage)
            7 -> MilkDairyCalculator(appLanguage)
            8 -> ConstructionMaterialCalculator(appLanguage)
            9 -> ProfitMarginMarkupCalculator(appLanguage)
        }
    }
}

/**
 * 1. Pakistani Galla Cash Denomination Counter (روپوں کی گنتی)
 */
@Composable
fun CashDenominationCalculator(appLanguage: AppLanguage) {
    var count5000 by remember { mutableStateOf("") }
    var count1000 by remember { mutableStateOf("") }
    var count500 by remember { mutableStateOf("") }
    var count100 by remember { mutableStateOf("") }
    var count50 by remember { mutableStateOf("") }
    var count20 by remember { mutableStateOf("") }
    var count10 by remember { mutableStateOf("") }

    val total5000 = (count5000.toLongOrNull() ?: 0L) * 5000L
    val total1000 = (count1000.toLongOrNull() ?: 0L) * 1000L
    val total500 = (count500.toLongOrNull() ?: 0L) * 500L
    val total100 = (count100.toLongOrNull() ?: 0L) * 100L
    val total50 = (count50.toLongOrNull() ?: 0L) * 50L
    val total20 = (count20.toLongOrNull() ?: 0L) * 20L
    val total10 = (count10.toLongOrNull() ?: 0L) * 10L

    val grandTotal = total5000 + total1000 + total500 + total100 + total50 + total20 + total10

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Cash Counted / کل نقدی",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "₨ ${"%,d".format(grandTotal)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DenominationRow("Rs. 5,000", count5000, total5000) { count5000 = it }
                    DenominationRow("Rs. 1,000", count1000, total1000) { count1000 = it }
                    DenominationRow("Rs. 500", count500, total500) { count500 = it }
                    DenominationRow("Rs. 100", count100, total100) { count100 = it }
                    DenominationRow("Rs. 50", count50, total50) { count50 = it }
                    DenominationRow("Rs. 20", count20, total20) { count20 = it }
                    DenominationRow("Rs. 10", count10, total10) { count10 = it }
                }
            }
        }
    }
}

@Composable
fun DenominationRow(label: String, countStr: String, rowTotal: Long, onCountChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.width(75.dp))
        OutlinedTextField(
            value = countStr,
            onValueChange = { onCountChange(it.filter { ch -> ch.isDigit() }) },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = "₨ ${"%,d".format(rowTotal)}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = PakEmeraldPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 2. Mandi Maund (Mann) / KG Calculator (من اور کلو حساب)
 */
@Composable
fun MandiMaundCalculator(appLanguage: AppLanguage) {
    var ratePerMaund by remember { mutableStateOf("4500") }
    var maunds by remember { mutableStateOf("10") }
    var extraKgs by remember { mutableStateOf("15") }

    val rate = ratePerMaund.toDoubleOrNull() ?: 0.0
    val m = maunds.toDoubleOrNull() ?: 0.0
    val k = extraKgs.toDoubleOrNull() ?: 0.0

    val totalKgs = (m * 40.0) + k
    val ratePerKg = rate / 40.0
    val totalAmount = totalKgs * ratePerKg

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Mandi Bill / من حساب بل", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.2f".format(totalAmount)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Weight: ${"%.1f".format(totalKgs)} KG (${"%.2f".format(totalKgs / 40.0)} Mann)",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ratePerMaund,
                        onValueChange = { ratePerMaund = it },
                        label = { Text("Rate Per Maund / من ریٹ (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = maunds,
                            onValueChange = { maunds = it },
                            label = { Text("Maunds (من)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = extraKgs,
                            onValueChange = { extraKgs = it },
                            label = { Text("Extra KGs (کلو)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakEmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rate per KG: ₨ ${"%.2f".format(ratePerKg)} • 1 Mann = 40 KG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PakEmeraldDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Gold & Jewelry Tola/Masha/Ratti Calculator (سونا چاندی ریٹ)
 */
@Composable
fun GoldJewelryCalculator(appLanguage: AppLanguage) {
    var goldRatePerTola by remember { mutableStateOf("275000") }
    var tolas by remember { mutableStateOf("1") }
    var mashas by remember { mutableStateOf("3") }
    var rattis by remember { mutableStateOf("4") }
    var makingCharges by remember { mutableStateOf("5000") }

    val rate = goldRatePerTola.toDoubleOrNull() ?: 0.0
    val t = tolas.toDoubleOrNull() ?: 0.0
    val m = mashas.toDoubleOrNull() ?: 0.0
    val r = rattis.toDoubleOrNull() ?: 0.0
    val making = makingCharges.toDoubleOrNull() ?: 0.0

    // 1 Tola = 12 Masha = 96 Ratti = 11.664 Grams
    val totalTolas = t + (m / 12.0) + (r / 96.0)
    val goldValue = totalTolas * rate
    val grandTotal = goldValue + making
    val totalGrams = totalTolas * 11.664

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Gold Bill / سونا بل", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.0f".format(grandTotal)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Weight: ${"%.3f".format(totalTolas)} Tola (${"%.2f".format(totalGrams)} Grams)",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goldRatePerTola,
                        onValueChange = { goldRatePerTola = it },
                        label = { Text("Gold Rate per Tola / تولہ ریٹ (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = tolas,
                            onValueChange = { tolas = it },
                            label = { Text("Tola (تولہ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mashas,
                            onValueChange = { mashas = it },
                            label = { Text("Masha (ماشہ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rattis,
                            onValueChange = { rattis = it },
                            label = { Text("Ratti (رتی)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = makingCharges,
                        onValueChange = { makingCharges = it },
                        label = { Text("Making Charges / جڑائی اجرت (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 4. Fabric Gaz to Meter & Price Calculator (گز اور میٹر حساب)
 */
@Composable
fun FabricMeasurementCalculator(appLanguage: AppLanguage) {
    var ratePerGaz by remember { mutableStateOf("450") }
    var gazQuantity by remember { mutableStateOf("4.5") }

    val rate = ratePerGaz.toDoubleOrNull() ?: 0.0
    val gaz = gazQuantity.toDoubleOrNull() ?: 0.0

    // 1 Gaz = 0.9144 Meters, 1 Meter = 1.0936 Gaz
    val meters = gaz * 0.9144
    val totalAmount = gaz * rate
    val ratePerMeter = rate / 0.9144

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Fabric Bill / کپڑے کا بل", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.2f".format(totalAmount)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${"%.2f".format(gaz)} Gaz = ${"%.2f".format(meters)} Meters",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ratePerGaz,
                        onValueChange = { ratePerGaz = it },
                        label = { Text("Rate Per Gaz / گز ریٹ (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gazQuantity,
                        onValueChange = { gazQuantity = it },
                        label = { Text("Quantity in Gaz / کل گز") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakEmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rate per Meter: ₨ ${"%.2f".format(ratePerMeter)} • 1 Gaz = 0.9144 Meter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PakEmeraldDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. FBR GST & Tax Calculator (سیلز ٹیکس حساب)
 */
@Composable
fun GstTaxCalculator(appLanguage: AppLanguage) {
    var amountStr by remember { mutableStateOf("10000") }
    var taxRateStr by remember { mutableStateOf("18") } // FBR Standard GST 18%
    var isTaxInclusive by remember { mutableStateOf(false) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = taxRateStr.toDoubleOrNull() ?: 0.0

    val taxAmount = if (isTaxInclusive) {
        amount - (amount / (1.0 + (rate / 100.0)))
    } else {
        amount * (rate / 100.0)
    }

    val finalTotal = if (isTaxInclusive) amount else amount + taxAmount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Final Invoice Total / مکمل بل ٹیکس کے ساتھ", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.2f".format(finalTotal)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GST Tax: ₨ ${"%,.2f".format(taxAmount)} (${rate}%)",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Base Amount / رقم (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taxRateStr,
                        onValueChange = { taxRateStr = it },
                        label = { Text("GST Tax % (e.g. 18% Standard, 5% Services)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 6. Annual Islamic Zakat & Business Wealth Calculator (زکوٰۃ 2.5% حساب)
 */
@Composable
fun ZakatCalculator(appLanguage: AppLanguage) {
    var cashOnHand by remember { mutableStateOf("150000") }
    var bankBalance by remember { mutableStateOf("500000") }
    var goldSilverValue by remember { mutableStateOf("300000") }
    var tradeStockValue by remember { mutableStateOf("750000") }
    var shortTermLiabilities by remember { mutableStateOf("200000") }

    val cash = cashOnHand.toDoubleOrNull() ?: 0.0
    val bank = bankBalance.toDoubleOrNull() ?: 0.0
    val gold = goldSilverValue.toDoubleOrNull() ?: 0.0
    val stock = tradeStockValue.toDoubleOrNull() ?: 0.0
    val debts = shortTermLiabilities.toDoubleOrNull() ?: 0.0

    val grossZakatable = cash + bank + gold + stock
    val netZakatable = (grossZakatable - debts).coerceAtLeast(0.0)
    val zakatPayable = netZakatable * 0.025 // 2.5% rate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Zakat Due (2.5% of Net Wealth) / واجب الادا زکوٰۃ", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.0f".format(zakatPayable)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net Zakatable Wealth: ₨ ${"%,.0f".format(netZakatable)}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Zakatable Business & Personal Assets", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = cashOnHand,
                        onValueChange = { cashOnHand = it },
                        label = { Text("Cash on Hand / Galla (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bankBalance,
                        onValueChange = { bankBalance = it },
                        label = { Text("Bank Accounts Balance (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = goldSilverValue,
                        onValueChange = { goldSilverValue = it },
                        label = { Text("Gold / Silver Jewelry Market Value (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tradeStockValue,
                        onValueChange = { tradeStockValue = it },
                        label = { Text("Business Trading Inventory / Stock for Sale (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Deductible Liabilities & Debts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)

                    OutlinedTextField(
                        value = shortTermLiabilities,
                        onValueChange = { shortTermLiabilities = it },
                        label = { Text("Current Debts / Supplier Udhaar Payable (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 7. Brokerage, Commission & Mandi Dalali Calculator (دلاّلی و کمیشن حساب)
 */
@Composable
fun CommissionBrokerageCalculator(appLanguage: AppLanguage) {
    var dealAmountStr by remember { mutableStateOf("500000") }
    var commissionRateStr by remember { mutableStateOf("2.0") } // Standard 1-2% in property, auto, mandi
    var deductTds by remember { mutableStateOf(false) }
    var tdsRateStr by remember { mutableStateOf("10.0") } // FBR WHT on commission 10%

    val dealAmount = dealAmountStr.toDoubleOrNull() ?: 0.0
    val rate = commissionRateStr.toDoubleOrNull() ?: 0.0
    val tdsRate = tdsRateStr.toDoubleOrNull() ?: 0.0

    val grossCommission = dealAmount * (rate / 100.0)
    val withholdingTax = if (deductTds) grossCommission * (tdsRate / 100.0) else 0.0
    val netCommission = grossCommission - withholdingTax

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Net Brokerage Commission / خالص کمیشن", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(
                        text = "₨ ${"%,.2f".format(netCommission)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gross: ₨ ${"%,.0f".format(grossCommission)} (${rate}%) ${if (deductTds) "| WHT: ₨ ${"%,.0f".format(withholdingTax)}" else ""}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = dealAmountStr,
                        onValueChange = { dealAmountStr = it },
                        label = { Text("Deal / Transaction Total Amount (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commissionRateStr,
                        onValueChange = { commissionRateStr = it },
                        label = { Text("Commission Rate % (e.g. 1%, 2% Real Estate / Mandi)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deduct FBR WHT / Advance Tax on Commission", fontSize = 13.sp)
                        androidx.compose.material3.Switch(
                            checked = deductTds,
                            onCheckedChange = { deductTds = it }
                        )
                    }

                    if (deductTds) {
                        OutlinedTextField(
                            value = tdsRateStr,
                            onValueChange = { tdsRateStr = it },
                            label = { Text("WHT Tax % (e.g. 10% Filer, 20% Non-Filer)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 8. Dairy & Milk Shop Calculator (دودھ اور دہی کا حساب)
 * Fat LR test / per litre conversion / water deduction / daily supply total
 */
@Composable
fun MilkDairyCalculator(appLanguage: AppLanguage) {
    var litersStr by remember { mutableStateOf("40") }
    var ratePerLiterStr by remember { mutableStateOf("210") }
    var fatPercentStr by remember { mutableStateOf("6.0") }
    var baseFatPercentStr by remember { mutableStateOf("6.0") }
    var deductionPercentStr by remember { mutableStateOf("0") }

    val liters = litersStr.toDoubleOrNull() ?: 0.0
    val ratePerLiter = ratePerLiterStr.toDoubleOrNull() ?: 0.0
    val fat = fatPercentStr.toDoubleOrNull() ?: 6.0
    val baseFat = (baseFatPercentStr.toDoubleOrNull() ?: 6.0).coerceAtLeast(0.1)
    val deduction = deductionPercentStr.toDoubleOrNull() ?: 0.0

    // Adjusted rate based on fat content formula (standard Pakistani gawala formula)
    val fatMultiplier = (fat / baseFat).coerceIn(0.5, 1.8)
    val effectiveRate = ratePerLiter * fatMultiplier
    val netLiters = (liters * (1.0 - (deduction / 100.0))).coerceAtLeast(0.0)
    val totalAmount = netLiters * effectiveRate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Milk Bill / کل رقم",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "₨ ${"%,.0f".format(totalAmount)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net: ${"%.1f".format(netLiters)} Liters • Effective Rate: ₨ ${"%.1f".format(effectiveRate)}/L",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = litersStr,
                        onValueChange = { litersStr = it },
                        label = { Text("Milk Quantity (Liters / کلو)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ratePerLiterStr,
                        onValueChange = { ratePerLiterStr = it },
                        label = { Text("Base Rate per Liter (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fatPercentStr,
                            onValueChange = { fatPercentStr = it },
                            label = { Text("Measured Fat %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = baseFatPercentStr,
                            onValueChange = { baseFatPercentStr = it },
                            label = { Text("Standard Base Fat %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = deductionPercentStr,
                        onValueChange = { deductionPercentStr = it },
                        label = { Text("Kharaba / Water Deduction %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 9. Construction Materials & Cement/Saria Calculator (سریے اور سیمنٹ کا حساب)
 * Converts Saria length/soot (سووت) to KG/Tonnes, and Bag requirement
 */
@Composable
fun ConstructionMaterialCalculator(appLanguage: AppLanguage) {
    var sariaLengthFeetStr by remember { mutableStateOf("100") }
    var sootSizeStr by remember { mutableStateOf("4") } // 4 soot = 1/2 inch
    var sariaRatePerKgStr by remember { mutableStateOf("265") }

    var cementBagsStr by remember { mutableStateOf("50") }
    var cementRatePerBagStr by remember { mutableStateOf("1350") }

    val lengthFeet = sariaLengthFeetStr.toDoubleOrNull() ?: 0.0
    val soot = sootSizeStr.toDoubleOrNull() ?: 4.0
    val sariaRatePerKg = sariaRatePerKgStr.toDoubleOrNull() ?: 0.0

    val cementBags = cementBagsStr.toIntOrNull() ?: 0
    val cementRatePerBag = cementRatePerBagStr.toDoubleOrNull() ?: 0.0

    // Standard formula: Weight per foot = (soot * soot) / 24 lbs = approx ((soot * soot) / 53) KG
    val sariaWeightKg = (lengthFeet * (soot * soot)) / 53.0
    val sariaTotalCost = sariaWeightKg * sariaRatePerKg
    val cementTotalCost = cementBags * cementRatePerBag
    val grandTotal = sariaTotalCost + cementTotalCost

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF475569)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Material Cost / کل خرچہ",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "₨ ${"%,.0f".format(grandTotal)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saria: ${"%.1f".format(sariaWeightKg)} KG (₨ ${"%,.0f".format(sariaTotalCost)}) • Cement: ₨ ${"%,.0f".format(cementTotalCost)}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Steel / Saria (سریا) Estimation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sariaLengthFeetStr,
                            onValueChange = { sariaLengthFeetStr = it },
                            label = { Text("Total Length (Feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sootSizeStr,
                            onValueChange = { sootSizeStr = it },
                            label = { Text("Bar Size (Soot / سوتر e.g 3,4,6)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = sariaRatePerKgStr,
                        onValueChange = { sariaRatePerKgStr = it },
                        label = { Text("Saria Rate per KG (₨)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    Text("Cement (سیمنٹ) Bags", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cementBagsStr,
                            onValueChange = { cementBagsStr = it },
                            label = { Text("Cement Bags (بوریاں)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cementRatePerBagStr,
                            onValueChange = { cementRatePerBagStr = it },
                            label = { Text("Rate per Bag (₨)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 10. Profit Margin & Markup Calculator (منافع اور مارک اپ کیلکولیٹر)
 */
@Composable
fun ProfitMarginMarkupCalculator(appLanguage: AppLanguage) {
    var costPriceStr by remember { mutableStateOf("1000") }
    var targetMarkupPercentStr by remember { mutableStateOf("25") }

    val cost = costPriceStr.toDoubleOrNull() ?: 0.0
    val markupPercent = targetMarkupPercentStr.toDoubleOrNull() ?: 0.0

    val profitAmount = cost * (markupPercent / 100.0)
    val sellingPrice = cost + profitAmount
    val profitMarginPercent = if (sellingPrice > 0) (profitAmount / sellingPrice) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Recommended Sale Price / فروخت قیمت",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "₨ ${"%,.0f".format(sellingPrice)}",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net Profit: ₨ ${"%,.0f".format(profitAmount)} • Margin: ${"%.1f".format(profitMarginPercent)}%",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("Cost / Purchase Price (خرید قیمت)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetMarkupPercentStr,
                        onValueChange = { targetMarkupPercentStr = it },
                        label = { Text("Target Profit Markup % (e.g. 20%, 30%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
