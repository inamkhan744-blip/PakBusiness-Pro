package com.example.ui.screens.features

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

enum class LoyaltyTier(val title: String, val badgeColor: Color, val discountPct: Int) {
    BRONZE("Bronze Member", Color(0xFFCD7F32), 2),
    SILVER("Silver VIP", Color(0xFF94A3B8), 5),
    GOLD("Gold Privilege", Color(0xFFEAB308), 8),
    PLATINUM("Platinum Royal", Color(0xFF8B5CF6), 12)
}

data class LoyaltyMember(
    val id: String,
    val name: String,
    val phone: String,
    var points: Int,
    var lifetimeSpend: Double,
    val tier: LoyaltyTier
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLoyaltyScreen(
    business: BusinessEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val currency = business?.currency ?: "PKR"

    val members = remember {
        mutableStateListOf(
            LoyaltyMember("1", "Haji Muhammad Aslam", "0300-1234567", 1450, 145000.0, LoyaltyTier.GOLD),
            LoyaltyMember("2", "Chaudhry Tariq Mehmood", "0321-9876543", 2800, 280000.0, LoyaltyTier.PLATINUM),
            LoyaltyMember("3", "Malik Rizwan Traders", "0333-5551234", 450, 45000.0, LoyaltyTier.SILVER),
            LoyaltyMember("4", "Sheikh Imran Ahmed", "0345-4447890", 85, 8500.0, LoyaltyTier.BRONZE)
        )
    }

    var memberToRedeem by remember { mutableStateOf<LoyaltyMember?>(null) }
    var pointsToRedeemInput by remember { mutableStateOf("100") }
    var memberToAddBonus by remember { mutableStateOf<LoyaltyMember?>(null) }
    var bonusPointsInput by remember { mutableStateOf("50") }

    fun sendLoyaltyWhatsApp(member: LoyaltyMember) {
        val msg = """
            *مبروک! ${member.name} صاحب!*
            
            آپ *${business?.name ?: "PakBusiness"}* کے معزز *${member.tier.title}* بن چکے ہیں!
            
            🌟 *آپ کے انعامی پوائنٹس:* ${member.points} Points
            🎁 *کیش ویلیو:* $currency ${member.points}
            🏷️ *خصوصی ڈسکاؤنٹ:* ${member.tier.discountPct}% تمام خریداریوں پر!
            
            اگلی خریداری پر اپنے پوائنٹس کیش ڈسکاؤنٹ میں تبدیل کروائیں۔ شکریہ!
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, msg)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Send Loyalty Tier Card via WhatsApp"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Customer Loyalty & Rewards (انعامی کلب)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Points, VIP Tiers, and cash discount rewards", fontSize = 11.sp, color = PakEmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_loyalty_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("customer_loyalty_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rewards Program Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = PakGoldSecondary, modifier = Modifier.size(24.dp))
                                Text("Customer Loyalty Club (VIP Rewards)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PakEmeraldDark)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = PakGoldSecondary) {
                                Text("1 Pt = 1 PKR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text(
                            text = "Reward returning customers with 1 Point per PKR 100 spent. Points convert directly into checkout cash discounts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Club Points", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${members.sumOf { it.points }} Pts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PakEmeraldDark)
                            }
                            Column {
                                Text("Voucher Value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${members.sumOf { it.points }}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF16A34A))
                            }
                            Column {
                                Text("VIP Members", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${members.size} Enrolled", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Member Cards
            item {
                Text(
                    text = "LOYALTY MEMBERS & POINTS LEDGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(members) { member ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = CircleShape, color = member.tier.badgeColor.copy(alpha = 0.2f), modifier = Modifier.size(42.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = member.tier.badgeColor, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(member.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(shape = RoundedCornerShape(4.dp), color = member.tier.badgeColor.copy(alpha = 0.15f)) {
                                        Text(
                                            "${member.tier.title} (${member.tier.discountPct}% Off)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = member.tier.badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${member.points} Points", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PakEmeraldPrimary)
                                Text("Value: $currency ${member.points}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Actions: Redeem Points, Add Bonus, Send WhatsApp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    memberToRedeem = member
                                    pointsToRedeemInput = (member.points.coerceAtMost(200)).toString()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Redeem", fontSize = 11.sp, color = Color(0xFFD97706))
                            }

                            OutlinedButton(
                                onClick = {
                                    memberToAddBonus = member
                                    bonusPointsInput = "50"
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = PakEmeraldPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+Bonus", fontSize = 11.sp, color = PakEmeraldPrimary)
                            }

                            Button(
                                onClick = { sendLoyaltyWhatsApp(member) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Redeem Points Dialog
        if (memberToRedeem != null) {
            val mem = memberToRedeem!!
            AlertDialog(
                onDismissRequest = { memberToRedeem = null },
                title = { Text("Redeem Points: ${mem.name}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Available Balance: ${mem.points} Points ($currency ${mem.points})")
                        OutlinedTextField(
                            value = pointsToRedeemInput,
                            onValueChange = { pointsToRedeemInput = it },
                            label = { Text("Points to Redeem for Discount") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val pts = pointsToRedeemInput.toIntOrNull() ?: 0
                            if (pts > 0 && pts <= mem.points) {
                                mem.points -= pts
                                memberToRedeem = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Text("Redeem Voucher")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToRedeem = null }) { Text("Cancel") }
                }
            )
        }

        // Add Bonus Points Dialog
        if (memberToAddBonus != null) {
            val mem = memberToAddBonus!!
            AlertDialog(
                onDismissRequest = { memberToAddBonus = null },
                title = { Text("Reward Bonus Points to ${mem.name}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bonusPointsInput,
                            onValueChange = { bonusPointsInput = it },
                            label = { Text("Bonus Points") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val pts = bonusPointsInput.toIntOrNull() ?: 0
                            if (pts > 0) {
                                mem.points += pts
                                memberToAddBonus = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                    ) {
                        Text("Award Points")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToAddBonus = null }) { Text("Cancel") }
                }
            )
        }
    }
}
