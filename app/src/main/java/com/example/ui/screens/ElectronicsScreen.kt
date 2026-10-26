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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
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
import com.example.data.RepairJobEntity
import com.example.ui.ElectronicsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicsScreen(
    viewModel: ElectronicsViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.filteredJobs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    var showNewJobDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("electronics_screen"),
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewJobDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Repair Job")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Electronics & Mobile Repair Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Manage repair job cards, IMEI, status updates & delivery", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by customer, IMEI, model, token...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "RECEIVED", "IN_REPAIR", "READY", "DELIVERED").forEach { s ->
                    FilterChip(
                        selected = statusFilter == s,
                        onClick = { viewModel.setStatusFilter(s) },
                        label = { Text(s.replace("_", " "), fontSize = 11.sp) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobs) { job ->
                    RepairJobItemCard(
                        job = job,
                        onUpdateStatus = { newStatus -> viewModel.updateJobStatus(job.id, newStatus) }
                    )
                }
            }
        }
    }

    if (showNewJobDialog) {
        NewRepairJobDialog(
            onDismiss = { showNewJobDialog = false },
            onSave = { name, phone, brand, model, imei, prob, cost, adv ->
                viewModel.createRepairJob(name, phone, brand, model, imei, prob, cost, adv)
                showNewJobDialog = false
            }
        )
    }
}

@Composable
private fun RepairJobItemCard(job: RepairJobEntity, onUpdateStatus: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${job.deviceBrand} ${job.deviceModel}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Token: ${job.tokenNumber} • Customer: ${job.customerName}", fontSize = 11.sp, color = Color.Gray)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (job.status) {
                        "RECEIVED" -> Color(0xFFE1F5FE)
                        "IN_REPAIR" -> Color(0xFFFFF9C4)
                        "READY" -> Color(0xFFE8F5E9)
                        else -> Color(0xFFEEEEEE)
                    }
                ) {
                    Text(
                        text = job.status.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (job.status) {
                            "RECEIVED" -> Color(0xFF0277BD)
                            "IN_REPAIR" -> Color(0xFFF57F17)
                            "READY" -> Color(0xFF2E7D32)
                            else -> Color.DarkGray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Issue: ${job.problemDescription}", fontSize = 12.sp)
            Text("Estimated: PKR ${job.estimatedCostPkr.toInt()} • Advance: PKR ${job.advancePaidPkr.toInt()}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("IN_REPAIR", "READY", "DELIVERED").forEach { st ->
                    if (job.status != st) {
                        Button(
                            onClick = { onUpdateStatus(st) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(st.replace("_", " "), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewRepairJobDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Samsung") }
    var model by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("3000") }
    var advance by remember { mutableStateOf("500") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("New Repair Job Ticket", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Customer Phone") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = imei, onValueChange = { imei = it }, label = { Text("IMEI / Serial (Optional)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = problem, onValueChange = { problem = it }, label = { Text("Fault Description") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Estimated Cost (PKR)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = advance, onValueChange = { advance = it }, label = { Text("Advance Paid (PKR)") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && model.isNotBlank()) {
                                    onSave(name, phone, brand, model, imei, problem, cost.toDoubleOrNull() ?: 3000.0, advance.toDoubleOrNull() ?: 0.0)
                                }
                            }
                        ) {
                            Text("Create Ticket")
                        }
                    }
                }
            }
        }
    }
}
