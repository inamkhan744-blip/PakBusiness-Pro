package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PartyEntity
import com.example.ui.PartiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesaleLedgerScreen(
    viewModel: PartiesViewModel,
    modifier: Modifier = Modifier
) {
    val parties by viewModel.filteredParties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.partyTypeFilter.collectAsState()

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var adjustBalanceTarget by remember { mutableStateOf<PartyEntity?>(null) }

    val totalReceivable = parties.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    val totalPayable = parties.filter { it.currentBalance < 0 }.sumOf { -it.currentBalance }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("wholesale_ledger_screen"),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPartyDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Wholesale & Khata Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Customer Udhaar, Supplier credits & party balances", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("You will get (Lena Hai)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        Text("PKR ${totalReceivable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("You will give (Dena Hai)", fontSize = 11.sp, color = Color(0xFFC62828))
                        Text("PKR ${totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFC62828))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search party by name, phone, city...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "CUSTOMER", "SUPPLIER").forEach { t ->
                    FilterChip(
                        selected = filterType == t,
                        onClick = { viewModel.setPartyTypeFilter(t) },
                        label = { Text(if (t == "ALL") "All Parties" else "${t.lowercase().capitalize()}s") }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(parties) { party ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(party.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${party.partyType} • ${party.phone} ${if (party.city.isNotBlank()) "• ${party.city}" else ""}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val isPositive = party.currentBalance >= 0
                                Text(
                                    text = "PKR ${party.currentBalance.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 14.sp
                                )
                                Button(
                                    onClick = { adjustBalanceTarget = party },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp).padding(top = 2.dp)
                                ) {
                                    Text("Khata Entry", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Party Dialog
    if (showAddPartyDialog) {
        AddPartyDialog(
            onDismiss = { showAddPartyDialog = false },
            onSave = { name, phone, type, city, bal ->
                viewModel.saveParty(name = name, phone = phone, type = type, city = city, initialBalance = bal)
                showAddPartyDialog = false
            }
        )
    }

    // Adjust Balance Dialog
    adjustBalanceTarget?.let { p ->
        AdjustBalanceDialog(
            party = p,
            onDismiss = { adjustBalanceTarget = null },
            onSave = { delta ->
                viewModel.updateBalance(p.id, delta)
                adjustBalanceTarget = null
            }
        )
    }
}

@Composable
private fun AddPartyDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CUSTOMER") }
    var city by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add New Party Khata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Party Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CUSTOMER", "SUPPLIER").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.capitalize()) })
                    }
                }
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City (e.g. Lahore, Karachi)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Opening Balance (PKR)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(onClick = { if (name.isNotBlank()) onSave(name, phone, type, city, balance.toDoubleOrNull() ?: 0.0) }) {
                        Text("Save Party")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustBalanceDialog(
    party: PartyEntity,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var isReceived by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Khata Entry: ${party.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Current Balance: PKR ${party.currentBalance.toInt()}", fontSize = 12.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isReceived, onClick = { isReceived = true }, label = { Text("Vasooli / Received (+)") })
                    FilterChip(selected = !isReceived, onClick = { isReceived = false }, label = { Text("Udhaar / Given (-)") })
                }

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (PKR)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val a = amount.toDoubleOrNull() ?: 0.0
                            val delta = if (isReceived) -a else a
                            onSave(delta)
                        }
                    ) {
                        Text("Record Entry")
                    }
                }
            }
        }
    }
}
