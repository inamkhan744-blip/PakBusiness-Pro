package com.example.ui.screens.features

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.data.CustomerEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

data class MarketingTemplate(
    val id: String,
    val title: String,
    val urduTitle: String,
    val iconColor: Color,
    val messageBody: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppMarketingScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val businessId = business?.id ?: 1L
    val businessName = business?.name ?: "PakBusiness Store"
    val businessPhone = business?.phone ?: "0300-1234567"

    val customerDao = remember(businessId) { AppDatabase.getDatabase(context).customerDao() }
    val dbCustomers by customerDao.getCustomers(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    // Mock initial customers fallback if DB is empty
    val customersList = remember(dbCustomers) {
        if (dbCustomers.isNotEmpty()) {
            dbCustomers
        } else {
            listOf(
                CustomerEntity(id = 1, businessId = businessId, name = "Haji Muhammad Aslam", phone = "0300-1234567", currentBalancePkr = 14500.0),
                CustomerEntity(id = 2, businessId = businessId, name = "Chaudhry Tariq Mehmood", phone = "0321-9876543", currentBalancePkr = 0.0),
                CustomerEntity(id = 3, businessId = businessId, name = "Malik Rizwan", phone = "0333-5551234", currentBalancePkr = 28000.0),
                CustomerEntity(id = 4, businessId = businessId, name = "Sheikh Imran Ahmed", phone = "0345-4447890", currentBalancePkr = 0.0)
            )
        }
    }

    val templates = remember {
        listOf(
            MarketingTemplate(
                id = "1",
                title = "Eid & Festive Discount",
                urduTitle = "عید اسپیشل رعایت",
                iconColor = Color(0xFF16A34A),
                messageBody = "Assalam-o-Alaikum {name}! 🌙\n{business_name} ki taraf se Eid Mubarak! Tamaam items par Flat 20% Discount chal raha hai. Aaj hi visit karein ya WhatsApp par order farmayein! Shukriya."
            ),
            MarketingTemplate(
                id = "2",
                title = "New Stock Arrival",
                urduTitle = "نیا مال آ گیا ہے",
                iconColor = Color(0xFF0284C7),
                messageBody = "Muaziz {name} Sahab! 📦\n{business_name} par naya premium stock dastyab ho chuka hai. Behtareen quality aur wholesale rates par hasil karne ke liye foran rabta karein: {phone}."
            ),
            MarketingTemplate(
                id = "3",
                title = "Udhar Recovery Reminder",
                urduTitle = "بقایا جات کی یاد دہانی",
                iconColor = Color(0xFFD97706),
                messageBody = "Assalam-o-Alaikum {name} Sahab! 🤝\nAap ke khata mein {business_name} ka kul baqaya Rs. {balance} wajib-ul-ada hai. Baraye meherbani jald az jald adayegi ya Raast / EasyPaisa par transfer farmayein. Shukriya!"
            ),
            MarketingTemplate(
                id = "4",
                title = "VIP Loyalty Bonus",
                urduTitle = "وی آئی پی انعامی رعایت",
                iconColor = Color(0xFF7C3AED),
                messageBody = "Hello {name}! ⭐\nAap hamaray ba-aitemad customer hain. Aap ke liye aglay order par Extra 10% Cash Discount coupon dastyab hai. Code: VIP-CUSTOMER. Visit {business_name} today!"
            )
        )
    }

    var selectedTemplate by remember { mutableStateOf(templates[0]) }
    var customMessageText by remember { mutableStateOf(selectedTemplate.messageBody) }
    var selectedAudienceFilter by remember { mutableStateOf("ALL") } // "ALL", "DEBTORS", "CLEAR"

    val sentRecipients = remember { mutableStateListOf<Long>() }

    val filteredCustomers = remember(customersList, selectedAudienceFilter) {
        when (selectedAudienceFilter) {
            "DEBTORS" -> customersList.filter { it.currentBalancePkr > 0 }
            "CLEAR" -> customersList.filter { it.currentBalancePkr <= 0 }
            else -> customersList
        }
    }

    fun launchWhatsAppSingle(customer: CustomerEntity) {
        val resolvedMessage = customMessageText
            .replace("{name}", customer.name)
            .replace("{business_name}", businessName)
            .replace("{phone}", businessPhone)
            .replace("{balance}", String.format("%,.0f", customer.currentBalancePkr))

        val cleanPhone = customer.phone.replace(Regex("[^0-9]"), "")
        val formattedNumber = if (cleanPhone.startsWith("0")) "92" + cleanPhone.substring(1) else cleanPhone

        val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(resolvedMessage)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
            if (!sentRecipients.contains(customer.id)) {
                sentRecipients.add(customer.id)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed or error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WhatsApp Marketing Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "واٹس ایپ پروموشن و براڈکاسٹ پیغامات",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PakEmeraldDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero KPI Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Audience", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${customersList.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Udhar Debtors", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${customersList.count { it.currentBalancePkr > 0 }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Broadcast Sent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sentRecipients.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                }
            }

            // Template Selector Row
            item {
                Text(
                    text = "1. Select Campaign Template",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { tmpl ->
                        val isSelected = selectedTemplate.id == tmpl.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) tmpl.iconColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) tmpl.iconColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedTemplate = tmpl
                                    customMessageText = tmpl.messageBody
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(tmpl.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) tmpl.iconColor else MaterialTheme.colorScheme.onSurface)
                                Text(tmpl.urduTitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Message Composer & Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. Message Content & Variables", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("{name}, {balance}", fontSize = 10.sp, color = PakEmeraldPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = customMessageText,
                            onValueChange = { customMessageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6
                        )

                        // WhatsApp Chat Bubble Preview
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCF8C6), // WhatsApp bubble green
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Live WhatsApp Preview:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF075E54))
                                Spacer(modifier = Modifier.height(2.dp))
                                val previewSample = customMessageText
                                    .replace("{name}", "Haji Aslam")
                                    .replace("{business_name}", businessName)
                                    .replace("{phone}", businessPhone)
                                    .replace("{balance}", "14,500")
                                Text(previewSample, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Audience Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. Choose Audience (${filteredCustomers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedAudienceFilter == "ALL",
                            onClick = { selectedAudienceFilter = "ALL" },
                            label = { Text("All", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedAudienceFilter == "DEBTORS",
                            onClick = { selectedAudienceFilter = "DEBTORS" },
                            label = { Text("Udhar Debtors", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = selectedAudienceFilter == "CLEAR",
                            onClick = { selectedAudienceFilter = "CLEAR" },
                            label = { Text("Cleared", fontSize = 10.sp) }
                        )
                    }
                }
            }

            // Customers List for Dispatch
            items(filteredCustomers, key = { it.id }) { customer ->
                val isSent = sentRecipients.contains(customer.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSent) Color(0xFF16A34A).copy(alpha = 0.2f) else PakEmeraldContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isSent) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSent) Color(0xFF16A34A) else PakEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column {
                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("📞 ${customer.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (customer.currentBalancePkr > 0) {
                                    Text("Baqaya Udhar: Rs. ${String.format("%,.0f", customer.currentBalancePkr)}", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = { launchWhatsAppSingle(customer) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSent) Color(0xFF16A34A) else Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSent) "Sent" else "Send",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
