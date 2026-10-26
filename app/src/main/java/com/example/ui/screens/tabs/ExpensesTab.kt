package com.example.ui.screens.tabs

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.ExpenseEntity
import com.example.ui.BusinessViewModel
import com.example.ui.screens.gym.GymDateUtils
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.PdfExportUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesTab(
    business: BusinessEntity?,
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val currency = business?.currency ?: "PKR"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Rent",
        "Utilities & Bills",
        "Staff Salaries",
        "Tea & Refreshment",
        "Raw Materials",
        "Maintenance & Repair",
        "Fuel & Transport",
        "Miscellaneous"
    )

    val quickExpensePresets = listOf(
        "Staff Tea & Snacks" to "Tea & Refreshment",
        "Shop Electricity Bill" to "Utilities & Bills",
        "Shop / Hall Rent" to "Rent",
        "Generator Fuel / Petrol" to "Fuel & Transport",
        "Cleaning & Sanitation" to "Maintenance & Repair",
        "Stationery & Bags" to "Raw Materials",
        "Daily Labor Wage" to "Staff Salaries"
    )

    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter) {
        expenses.filter { exp ->
            val matchQuery = searchQuery.isBlank() ||
                    exp.title.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    exp.notes.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategoryFilter == "All" || exp.category.equals(selectedCategoryFilter, ignoreCase = true)
            matchQuery && matchCategory
        }
    }

    // Metrics
    val todayStart = remember {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }

    val todayTotal = remember(expenses, todayStart) {
        expenses.filter { it.date >= todayStart }.sumOf { it.amount }
    }

    val allTotal = remember(expenses) {
        expenses.sumOf { it.amount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("expenses_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Kharcha & Expense Manager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Roznamcha Hisab-Kitab • ${business?.name ?: "Business"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_add_expense")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Kharcha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Metric pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Today's Expense", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    "$currency ${String.format(Locale.getDefault(), "%,.0f", todayTotal)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Expenses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    "$currency ${String.format(Locale.getDefault(), "%,.0f", allTotal)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }

                    // Export / Print Expense Ledger PDF
                    if (expenses.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val expenseItems = filteredExpenses.map { exp ->
                                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp.date))
                                        InvoiceExportItem(
                                            name = "${exp.title} [${exp.category}] ($dateStr)",
                                            quantity = 1.0,
                                            unitPrice = exp.amount,
                                            total = exp.amount
                                        )
                                    }
                                    val exportData = InvoiceExportData(
                                        invoiceNumber = "EXP-${System.currentTimeMillis() % 100000}",
                                        businessName = business?.name ?: "PakBusiness Enterprise",
                                        businessAddress = business?.address ?: "",
                                        businessPhone = business?.phone ?: "",
                                        customerName = "OFFICIAL EXPENSE AUDIT & KHARCHA LEDGER",
                                        customerPhone = "Category Filter: $selectedCategoryFilter | Entries: ${filteredExpenses.size}",
                                        items = expenseItems,
                                        subtotal = filteredExpenses.sumOf { it.amount },
                                        discount = 0.0,
                                        tax = 0.0,
                                        grandTotal = filteredExpenses.sumOf { it.amount },
                                        paymentMethod = "Total Paid Expenses ($currency)",
                                        footerNote = "Certified Kharcha Statement • PakBusiness Pro"
                                    )
                                    PdfExportUtil.printInvoice(context, exportData)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("btn_print_expenses_ledger")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print Kharcha PDF", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val totalExp = filteredExpenses.sumOf { it.amount }
                                    val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                    val summary = buildString {
                                        appendLine("📊 *BUSINESS EXPENSE / KHARCHA AUDIT REPORT*")
                                        appendLine("🏢 *${business?.name ?: "Business"}*")
                                        appendLine("📅 *Report Date:* $todayStr")
                                        appendLine("🔍 *Filter:* $selectedCategoryFilter | *Entries:* ${filteredExpenses.size}")
                                        appendLine("💰 *TOTAL KHARCHA:* $currency ${String.format(Locale.getDefault(), "%,.0f", totalExp)}")
                                        appendLine("────────────────────")
                                        filteredExpenses.take(15).forEachIndexed { idx, exp ->
                                            val d = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(exp.date))
                                            appendLine("${idx + 1}. *${exp.title}* [${exp.category}] ($d): $currency ${String.format(Locale.getDefault(), "%,.0f", exp.amount)}")
                                        }
                                        if (filteredExpenses.size > 15) {
                                            appendLine("... and ${filteredExpenses.size - 15} more records")
                                        }
                                        appendLine("────────────────────")
                                        appendLine("_Generated via PakBusiness Pro ERP_")
                                    }

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, summary)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Expense Report"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("btn_share_expenses_ledger")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Report", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Search & Category Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search expense, category or note...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                }
            }
        }

        // Expense Items List
        if (filteredExpenses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No expenses recorded yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap '+ Add Kharcha' to record daily shop expenses, staff tea, rent or bills.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    currency = currency,
                    onDelete = { viewModel.deleteExpense(expense.id) },
                    onShareWhatsApp = {
                        val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.date))
                        val slip = """
                            *EXPENSE VOUCHER / KHARCHA SLIP*
                            🏢 *${business?.name ?: "Business"}*
                            ────────────────────
                            📌 *Title:* ${expense.title}
                            🏷 *Category:* ${expense.category}
                            💵 *Amount:* $currency ${String.format(Locale.getDefault(), "%,.2f", expense.amount)}
                            💳 *Paid Via:* ${expense.paymentMethod}
                            📅 *Date:* $dateFormatted
                            ${if (expense.notes.isNotBlank()) "📝 *Notes:* ${expense.notes}\n" else ""}────────────────────
                            _Generated by PakBusiness Pro ERP_
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, slip)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Expense Voucher"))
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            currency = currency,
            categories = categories.filter { it != "All" },
            presets = quickExpensePresets,
            onDismiss = { showAddDialog = false },
            onSave = { title, category, amount, method, notes ->
                viewModel.saveExpense(
                    ExpenseEntity(
                        businessId = business?.id ?: 0L,
                        title = title,
                        category = category,
                        amount = amount,
                        paymentMethod = method,
                        notes = notes,
                        date = System.currentTimeMillis()
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseEntity,
    currency: String,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = expense.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = expense.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "• ${expense.paymentMethod}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(expense.date))
                    Text(
                        text = dateFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "-$currency ${String.format(Locale.getDefault(), "%,.0f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFC62828)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    currency: String,
    categories: List<String>,
    presets: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, amount: Double, method: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Miscellaneous") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }

    val paymentMethods = listOf("Cash", "Online / JazzCash", "Bank Transfer", "Card")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record New Kharcha / Expense", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick presets
                Text("Quick Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakEmeraldContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                title = preset.first
                                selectedCategory = preset.second
                            }
                        ) {
                            Text(
                                text = preset.first,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = PakEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title *") },
                    placeholder = { Text("e.g. WAPDA Bill, Chai, Petrol") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Category Chips
                Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                // Payment Method
                Text("Payment Method:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(paymentMethods) { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Bill # (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onSave(title, selectedCategory, amount, paymentMethod, notes)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Kharcha")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
