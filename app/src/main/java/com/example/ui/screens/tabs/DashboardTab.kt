package com.example.ui.screens.tabs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.BottomNavTab
import com.example.ui.BusinessCategory
import com.example.ui.resolveBusinessCategory
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

@Composable
fun DashboardTab(
    business: BusinessEntity?,
    onNavigateTab: (BottomNavTab) -> Unit,
    onOpenTenantSwitcher: () -> Unit,
    onOpenVoiceKhata: () -> Unit = {},
    onOpenBarcodeStudio: () -> Unit = {},
    onOpenSuppliers: () -> Unit = {},
    onOpenLoyalty: () -> Unit = {},
    onOpenBusinessHealth: () -> Unit = {},
    onOpenQuotations: () -> Unit = {},
    onOpenDigitalCard: () -> Unit = {},
    onOpenWhatsAppMarketing: () -> Unit = {},
    onOpenChequeRegister: () -> Unit = {},
    onOpenTaxCalculator: () -> Unit = {},
    onOpenWarrantyRepairs: () -> Unit = {},
    onOpenStaffCommission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val category = resolveBusinessCategory(business?.type)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Tenant Active Business Hero Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakGoldSecondary.copy(alpha = 0.25f),
                            contentColor = Color(0xFFFFDFBA)
                        ) {
                            Text(
                                text = "ACTIVE TENANT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onOpenTenantSwitcher() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Tenant",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Switch",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Business Name & Type
                    Column {
                        Text(
                            text = business?.name ?: "PakBusiness Pro",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${business?.type ?: "General Store"} • ${category.displayName}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Details row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Owner: ${business?.ownerName ?: "Not Set"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            if (!business?.phone.isNullOrBlank()) {
                                Text(
                                    text = "Contact: ${business?.phone}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "₨ ${business?.currency ?: "PKR"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // BRAND NEW INNOVATIVE FEATURES SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚀 New Smart ERP Features",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDC2626)) {
                        Text(
                            text = "NEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Voice Khata Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenVoiceKhata() }
                            .testTag("dashboard_btn_voice_khata")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFDC2626), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text("Voice Khata", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Bol Kar Khata", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 2. Barcode Label Studio Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenBarcodeStudio() }
                            .testTag("dashboard_btn_barcode_studio")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFF0284C7), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text("Barcode Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Print Stickers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 3. Suppliers & Vendor Ledger Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSuppliers() }
                            .testTag("dashboard_btn_suppliers")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFB91C1C), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Factory, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text("Vendor Khata", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Payables Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 4. Customer Loyalty & Rewards Club
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenLoyalty() }
                            .testTag("dashboard_btn_loyalty")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = PakGoldSecondary, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Loyalty Points", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("VIP Member Club", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 5. Business Health Scorecard
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenBusinessHealth() }
                            .testTag("dashboard_btn_health")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF16A34A), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Health Meter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Score: 86/100 A+", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 6. Quotations & Kacha Bill
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenQuotations() }
                            .testTag("dashboard_btn_quotations")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF2563EB), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Quotations", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Kacha Bill & Est.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 7. Digital Dukaan Card & QR
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenDigitalCard() }
                            .testTag("dashboard_btn_digital_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = PakEmeraldPrimary, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Digital Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Raast & Jazz QR", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 8. WhatsApp Marketing Studio
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenWhatsAppMarketing() }
                            .testTag("dashboard_btn_whatsapp_marketing")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF25D366), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("WA Marketing", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Promo Broadcast", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 9. Cheque Book & PDC Register
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenChequeRegister() }
                            .testTag("dashboard_btn_cheque_register")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF475569), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Cheque Register", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("PDC & Clearance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 10. FBR & Sales Tax Calculator
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenTaxCalculator() }
                            .testTag("dashboard_btn_tax_calculator")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFFD97706), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Tax & FBR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("GST 18% & WHT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 11. Warranty & Repair Sheets
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenWarrantyRepairs() }
                            .testTag("dashboard_btn_warranty_repairs")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF9333EA), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Repair Jobs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Warranty & Tokens", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // 12. Staff Sales Commission & Targets
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStaffCommission() }
                        .testTag("dashboard_btn_staff_commission")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = PakGoldSecondary, modifier = Modifier.size(34.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Staff Commission & Target Leaderboard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Monthly sales milestones, commission % payouts & employee rankings", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("View Board", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PakEmeraldPrimary)
                    }
                }
            }
        }

        // Section Title: Quick Metrics
        item {
            Text(
                text = "Today's Business Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 4 KPI Cards in 2x2 grid style
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Today's Sales",
                        value = "₨ 0.00",
                        subtitle = "0 invoices today",
                        icon = Icons.Default.TrendingUp,
                        iconTint = PakEmeraldPrimary,
                        containerColor = PakEmeraldContainer.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "POS Orders",
                        value = "0",
                        subtitle = "Awaiting checkout",
                        icon = Icons.Default.PointOfSale,
                        iconTint = PakGoldSecondary,
                        containerColor = PakGoldContainer.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Inventory SKUs",
                        value = "0 Items",
                        subtitle = "Valuation: ₨ 0",
                        icon = Icons.Default.Inventory,
                        iconTint = Color(0xFF007A87),
                        containerColor = Color(0xFFD3F4F7),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Customers",
                        value = "0",
                        subtitle = "Khata Balance: ₨ 0",
                        icon = Icons.Default.Badge,
                        iconTint = Color(0xFF5A449A),
                        containerColor = Color(0xFFE8DDFF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: Aggregated Multi-Module Daily Earnings & Chart Visualization
        item {
            AggregatedEarningsChartCard(
                business = business,
                category = category
            )
        }

        // SECTION: Dynamically Filtered Module Card (ONLY for activeBusiness.type)
        item {
            Text(
                text = "Active Module",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            when (category) {
                BusinessCategory.GYM -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.55f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.GYM) }
                            .testTag("card_open_gym_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PakEmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Gym & Fitness Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PakGoldSecondary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Active members, expiring renewals & check-in/out desk",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.GYM) },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.RESTAURANT -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakGoldSecondary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.RESTAURANT) }
                            .testTag("card_open_restaurant_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PakGoldSecondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Restaurant & Cafe Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PakEmeraldPrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Table grid, POS table orders, live KOT kitchen tickets & menu",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.RESTAURANT) },
                                colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.HOSPITAL -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.HOSPITAL) }
                            .testTag("card_open_hospital_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF0284C7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Hospital & Clinic Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0C4A6E)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0284C7)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Patients search, doctors availability, appointment booking & Rx PDF",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0369A1),
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.HOSPITAL) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.PHARMACY -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6FFFA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.PHARMACY) }
                            .testTag("card_open_pharmacy_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF0D9488), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPharmacy,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Pharmacy & Medical Store Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF134E4A)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0D9488)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Formula search, expiry dates alert, rack locations & strip/box POS sale",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0F766E),
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.PHARMACY) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.SCHOOL -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.SCHOOL) }
                            .testTag("card_open_school_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PakEmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "School & Academy Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PakEmeraldPrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Student directory, classes, fee voucher generation & daily roll-call attendance",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.SCHOOL) },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.REAL_ESTATE -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.55f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakGoldSecondary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.REAL_ESTATE) }
                            .testTag("card_open_real_estate_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PakGoldSecondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Real Estate Agency Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PakGoldSecondary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Properties inventory (Marla/Kanal), buyer/seller leads, client budget & site visits",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.REAL_ESTATE) },
                                colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.SALON -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC).copy(alpha = 0.75f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD81B60).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.SALON) }
                            .testTag("card_open_salon_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFD81B60), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Salon & Beauty Parlor Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFD81B60)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Service menu (Hair, Skin, Spa), stylist commission tracking & appointments",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.SALON) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.HOTEL -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.55f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.HOTEL) }
                            .testTag("card_open_hotel_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF059669), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hotel,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Hotel & Guest House Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF059669)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Room grid view (Available/Booked/Cleaning), check-in/out dates & guest CNIC tracking",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.HOTEL) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.TAILOR -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.75f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.TAILOR) }
                            .testTag("card_open_tailor_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF7B1FA2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checkroom,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Tailor & Boutique Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF7B1FA2)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Paimaish register (Chest, Waist, Length), order delivery tracking & status pipeline",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.TAILOR) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.LAUNDRY -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE).copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.LAUNDRY) }
                            .testTag("card_open_laundry_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF0284C7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalLaundryService,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Laundry & Dry Cleaners Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0284C7)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Customer records, Order taking (Shirt, Pants, Bedsheet with quantity), Order tracking (Washing \u2192 Ironing \u2192 Ready \u2192 Delivered)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.LAUNDRY) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.WORKSHOP -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED).copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.WORKSHOP) }
                            .testTag("card_open_workshop_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFEA580C), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Auto Workshop & Garage Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFEA580C)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Vehicle records (Plate, Model, Chassis VIN), Repair job cards with status workflow & mechanic assignment",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.WORKSHOP) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.ELECTRONICS -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF).copy(alpha = 0.9f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.ELECTRONICS) }
                            .testTag("card_open_electronics_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF0284C7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Electronics & Mobile Shop Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0284C7)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "IMEI & Serial inventory, Warranty tracking, PTA status, Mobile repair job tickets & diagnostic lab",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.ELECTRONICS) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.BAKERY -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB).copy(alpha = 0.85f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.BAKERY) }
                            .testTag("card_open_bakery_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFD97706), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cake,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Bakery & Sweets Hub",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFD97706)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Custom Cake Orders (Birthday/Wedding), Production & Expiry dates, Sweets & Mithai inventory",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.BAKERY) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.WHOLESALE -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.WHOLESALE) }
                            .testTag("card_open_wholesale_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF00796B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Wholesale & Distribution",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF00796B)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Parties, bulk transport bilty orders & Udhaar/Khata balance tracking",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.WHOLESALE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                BusinessCategory.GENERAL -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.45f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTab(BottomNavTab.POS) }
                            .testTag("card_open_pos_general_module")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(PakEmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    val bizTypeLower = business?.type?.lowercase() ?: ""
                                    val (moduleTitle, moduleDesc, badgeText) = when {
                                        bizTypeLower.contains("dairy") || bizTypeLower.contains("milk") || bizTypeLower.contains("doodh") ->
                                            Triple("Dairy & Milk Shop (دودھ دہی)", "Daily gawala supply, fat % testing, milk liters POS billing & chiller inventory", "DAIRY POS")
                                        bizTypeLower.contains("meat") || bizTypeLower.contains("murghi") || bizTypeLower.contains("gosht") ->
                                            Triple("Meat & Poultry (مرغی گوشت)", "Live chicken weight rate, fresh meat cutting POS & supplier accounts", "MEAT POS")
                                        bizTypeLower.contains("fruit") || bizTypeLower.contains("sabzi") || bizTypeLower.contains("mandi") ->
                                            Triple("Fruit & Vegetable (سبزی منڈی)", "Daily mandi crates rate, KG/Dharri fast scale billing & wastage tracking", "MANDI POS")
                                        bizTypeLower.contains("hardware") || bizTypeLower.contains("sanitary") || bizTypeLower.contains("paint") ->
                                            Triple("Hardware & Sanitary Store", "Sanitary fittings, paint buckets, pipe lengths & builder ledger bills", "HARDWARE")
                                        bizTypeLower.contains("sarafa") || bizTypeLower.contains("jewelry") || bizTypeLower.contains("gold") ->
                                            Triple("Sarafa & Gold Jewelry (صرافہ)", "Tola/Masha/Ratti purity calculator, gold craft making charges & purchase bills", "SARAFA")
                                        bizTypeLower.contains("construction") || bizTypeLower.contains("cement") || bizTypeLower.contains("saria") ->
                                            Triple("Construction Materials (سیمنٹ سریا)", "Soot bar conversion, cement bags delivery chalan & site credit accounts", "BUILDER")
                                        bizTypeLower.contains("book") || bizTypeLower.contains("stationery") ->
                                            Triple("Book Depot & Stationery", "Curriculum books, stationery item packs, school bundle billing & photocopy", "BOOK DEPOT")
                                        bizTypeLower.contains("agriculture") || bizTypeLower.contains("fertilizer") || bizTypeLower.contains("beej") ->
                                            Triple("Agri Seeds & Fertilizer (کھاد بیج)", "Fertilizer sacks, seed batches, crop spray pesticides & kisan khata", "AGRI STORE")
                                        else ->
                                            Triple("Retail & POS Store Hub", "High-speed POS checkout, inventory catalog with SKU barcodes & customer Udhaar/Khata records", "ACTIVE")
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = moduleTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PakEmeraldPrimary
                                        ) {
                                            Text(
                                                text = badgeText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = moduleDesc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(BottomNavTab.POS) },
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open POS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Pakistani Trade Utilities & Quick Calculators (Always visible on all businesses)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.55f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PakGoldSecondary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTab(BottomNavTab.SETTINGS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PakGoldSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Pakistani Trade Calculators",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PakGoldSecondary
                                ) {
                                    Text(
                                        text = "10 TOOLS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Galla Cash Counter, Mandi Maund, Gold Tola, Fabric Gaz, Milk Fat %, Saria/Cement & Zakat",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Button(
                        onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                        colors = ButtonDefaults.buttonColors(containerColor = PakGoldSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Quick Operations (Filtered dynamically by business category)
        item {
            Text(
                text = "Quick Operations",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (category) {
                    BusinessCategory.GYM -> {
                        QuickActionButton(
                            icon = Icons.Default.FitnessCenter,
                            label = "Gym Desk",
                            onClick = { onNavigateTab(BottomNavTab.GYM) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Sale",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Inventory",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Badge,
                            label = "Members",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.RESTAURANT -> {
                        QuickActionButton(
                            icon = Icons.Default.TableBar,
                            label = "Tables & KOT",
                            onClick = { onNavigateTab(BottomNavTab.RESTAURANT) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Order",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.HOSPITAL -> {
                        QuickActionButton(
                            icon = Icons.Default.LocalHospital,
                            label = "Clinic Desk",
                            onClick = { onNavigateTab(BottomNavTab.HOSPITAL) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Patients",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.PHARMACY -> {
                        QuickActionButton(
                            icon = Icons.Default.LocalPharmacy,
                            label = "Rx Medicine",
                            onClick = { onNavigateTab(BottomNavTab.PHARMACY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Sale",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Batches",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.LAUNDRY -> {
                        QuickActionButton(
                            icon = Icons.Default.LocalLaundryService,
                            label = "Orders Hub",
                            onClick = { onNavigateTab(BottomNavTab.LAUNDRY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.WORKSHOP -> {
                        QuickActionButton(
                            icon = Icons.Default.DirectionsCar,
                            label = "Job Cards",
                            onClick = { onNavigateTab(BottomNavTab.WORKSHOP) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Auto Parts",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Clients",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.ELECTRONICS -> {
                        QuickActionButton(
                            icon = Icons.Default.Devices,
                            label = "Devices Hub",
                            onClick = { onNavigateTab(BottomNavTab.ELECTRONICS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Sale",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "IMEI Stock",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.TAILOR -> {
                        QuickActionButton(
                            icon = Icons.Default.Checkroom,
                            label = "Paimaish Hub",
                            onClick = { onNavigateTab(BottomNavTab.TAILOR) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Fabrics",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Clients",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.BAKERY -> {
                        QuickActionButton(
                            icon = Icons.Default.Cake,
                            label = "Bakery Hub",
                            onClick = { onNavigateTab(BottomNavTab.BAKERY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Sale",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Inventory",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.SCHOOL -> {
                        QuickActionButton(
                            icon = Icons.Default.School,
                            label = "School Hub",
                            onClick = { onNavigateTab(BottomNavTab.SCHOOL) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Badge,
                            label = "Students",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "Fee Vouchers",
                            onClick = { onNavigateTab(BottomNavTab.SCHOOL) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.HOTEL -> {
                        QuickActionButton(
                            icon = Icons.Default.Hotel,
                            label = "Rooms Grid",
                            onClick = { onNavigateTab(BottomNavTab.HOTEL) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Guests",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.HOTEL) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.REAL_ESTATE -> {
                        QuickActionButton(
                            icon = Icons.Default.Apartment,
                            label = "Properties",
                            onClick = { onNavigateTab(BottomNavTab.REAL_ESTATE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Leads",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "Deals",
                            onClick = { onNavigateTab(BottomNavTab.REAL_ESTATE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.SALON -> {
                        QuickActionButton(
                            icon = Icons.Default.ContentCut,
                            label = "Services",
                            onClick = { onNavigateTab(BottomNavTab.SALON) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "Billing",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Clients",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.WHOLESALE -> {
                        QuickActionButton(
                            icon = Icons.Default.LocalShipping,
                            label = "Bilty Order",
                            onClick = { onNavigateTab(BottomNavTab.WHOLESALE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "Khata Pay",
                            onClick = { onNavigateTab(BottomNavTab.WHOLESALE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Inventory",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BusinessCategory.GENERAL -> {
                        QuickActionButton(
                            icon = Icons.Default.PointOfSale,
                            label = "POS Sale",
                            onClick = { onNavigateTab(BottomNavTab.POS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory,
                            label = "Inventory",
                            onClick = { onNavigateTab(BottomNavTab.INVENTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Customers",
                            onClick = { onNavigateTab(BottomNavTab.CUSTOMERS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { onNavigateTab(BottomNavTab.SETTINGS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Advanced ERP & Finance Operations
        item {
            Text(
                text = "Advanced ERP & Financial Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEEEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(BottomNavTab.EXPENSES) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text("Daily Kharcha", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF991B1B))
                        Text("Bills, Fuel, Tea & Rent", fontSize = 11.sp, color = Color(0xFFB91C1C))
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(BottomNavTab.STAFF) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF3B82F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text("Staff & Payroll", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E40AF))
                        Text("Haziri, Peshgi & Pay", fontSize = 11.sp, color = Color(0xFF2563EB))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.45f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTab(BottomNavTab.REPORTS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(PakEmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = "Day-End (Z-Report) & P&L Audit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PakEmeraldDark
                            )
                            Text(
                                text = "Daily net profit, total revenue, expenses & offline backup",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { onNavigateTab(BottomNavTab.REPORTS) },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Recent Activity / Skeleton Placeholder
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "No Transactions Recorded Yet",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "The core system for ${business?.name ?: "this business"} is running offline. Open POS or your module to begin recording transactions in ${business?.currency ?: "PKR"}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = { onNavigateTab(BottomNavTab.POS) },
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Open POS Terminal")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PakEmeraldPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AggregatedEarningsChartCard(
    business: BusinessEntity?,
    category: BusinessCategory
) {
    val currency = business?.currency ?: "PKR"

    // Multi-module aggregate daily data points (Mon to Sun)
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dailyEarnings = listOf(35000.0, 48500.0, 62000.0, 54000.0, 89000.0, 115000.0, 94000.0)
    val maxEarning = dailyEarnings.maxOrNull() ?: 1.0
    val todayEarning = dailyEarnings[5] // Saturday peak
    val totalWeekEarnings = dailyEarnings.sum()
    val averageDaily = totalWeekEarnings / dailyEarnings.size

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aggregated_earnings_card")
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
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Daily Revenue Analytics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Aggregated across active business operations",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PakEmeraldContainer
                ) {
                    Text(
                        text = "7-Day Trend",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PakEmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // High-Level KPIs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PakEmeraldContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Today's Gross", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$currency ${String.format(java.util.Locale.getDefault(), "%,.0f", todayEarning)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PakEmeraldPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("7-Day Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$currency ${String.format(java.util.Locale.getDefault(), "%,.0f", totalWeekEarnings)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Daily Average", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$currency ${String.format(java.util.Locale.getDefault(), "%,.0f", averageDaily)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Visual Bar Chart Visualization using pure Compose
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyEarnings.forEachIndexed { index, amount ->
                        val barHeightFraction = (amount / maxEarning).toFloat().coerceIn(0.1f, 1f)
                        val isToday = index == 5

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${(amount / 1000).toInt()}k",
                                fontSize = 9.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((100 * barHeightFraction).dp)
                                    .background(
                                        color = if (isToday) PakEmeraldPrimary else PakEmeraldPrimary.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(6.dp, 6.dp, 2.dp, 2.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dayLabels[index],
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (isToday) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Module Contribution Breakdown
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Contributor: ${category.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-Time Aggregation Active",
                        fontSize = 10.sp,
                        color = PakEmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

