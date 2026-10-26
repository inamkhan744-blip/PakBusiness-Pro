package com.example.ui.screens.pharmacy

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.MedicineEntity
import com.example.data.PharmacyCartItem
import com.example.data.PharmacySaleEntity

enum class PharmacySubTab(val title: String, val icon: ImageVector) {
    QUICK_SALE("Quick Sale", Icons.Default.PointOfSale),
    INVENTORY("Medicines Stock", Icons.Default.Medication),
    ALERTS("Expiry & Alerts", Icons.Default.Warning),
    SALES_LOG("Sales History", Icons.Default.ReceiptLong),
    SUPPLIERS("Racks & Suppliers", Icons.Default.LocalShipping)
}

@Composable
fun PharmacyModuleScreen(
    business: BusinessEntity?,
    medicines: List<MedicineEntity>,
    sales: List<PharmacySaleEntity>,
    onSaveMedicine: (MedicineEntity) -> Unit,
    onDeleteMedicine: (Long) -> Unit,
    onUpdateStock: (Long, Int) -> Unit,
    onCompleteSale: (
        cartItems: List<PharmacyCartItem>,
        customerName: String,
        customerPhone: String,
        doctorPrescriber: String,
        discount: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (PharmacySaleEntity) -> Unit
    ) -> Unit,
    onDeleteSale: (Long) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = PharmacySubTab.values()

    val now = remember { System.currentTimeMillis() }
    val expiredCount = remember(medicines) { medicines.count { it.isExpired(now) } }
    val expiringCount = remember(medicines) { medicines.count { it.isExpiringSoon(30, now) } }
    val lowStockCount = remember(medicines) { medicines.count { it.isLowStock() } }
    val totalAlerts = expiredCount + expiringCount + lowStockCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Module Top Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0D9488), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pharmacy & Medical Store",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${business?.name ?: "PakBusiness Pro"} • Rx & Med POS",
                                fontSize = 11.sp,
                                color = Color(0xFF0D9488),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Alerts indicator chip
                    if (totalAlerts > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (expiredCount > 0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (expiredCount > 0) Color(0xFFDC2626) else Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$totalAlerts Alerts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (expiredCount > 0) Color(0xFFDC2626) else Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }

                // Sub Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D9488),
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFF0D9488),
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, subTab ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = subTab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color(0xFF0D9488) else Color(0xFF64748B)
                                    )
                                    Text(
                                        text = subTab.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF0D9488) else Color(0xFF64748B)
                                    )
                                    if (subTab == PharmacySubTab.ALERTS && totalAlerts > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (expiredCount > 0) Color(0xFFDC2626) else Color(0xFFD97706)
                                        ) {
                                            Text(
                                                text = "$totalAlerts",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("pharmacy_subtab_${subTab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Sub-screen Body with Crossfade
        Crossfade(
            targetState = tabs[selectedTabIndex],
            modifier = Modifier.fillMaxSize(),
            label = "pharmacy_subtab_crossfade"
        ) { currentTab ->
            when (currentTab) {
                PharmacySubTab.QUICK_SALE -> {
                    QuickSaleView(
                        medicines = medicines,
                        onCompleteSale = onCompleteSale
                    )
                }
                PharmacySubTab.INVENTORY -> {
                    MedicineInventoryView(
                        medicines = medicines,
                        onSaveMedicine = onSaveMedicine,
                        onDeleteMedicine = onDeleteMedicine,
                        onUpdateStock = onUpdateStock
                    )
                }
                PharmacySubTab.ALERTS -> {
                    ExpiryAndStockAlertsView(
                        medicines = medicines,
                        onEditMedicine = onSaveMedicine,
                        onUpdateStock = onUpdateStock
                    )
                }
                PharmacySubTab.SALES_LOG -> {
                    PharmacySalesLogView(
                        business = business,
                        sales = sales,
                        onDeleteSale = onDeleteSale
                    )
                }
                PharmacySubTab.SUPPLIERS -> {
                    SuppliersAndRacksView(
                        medicines = medicines
                    )
                }
            }
        }
    }
}
