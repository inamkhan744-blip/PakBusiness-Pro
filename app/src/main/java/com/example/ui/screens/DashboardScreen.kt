package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.AppLanguage
import com.example.ui.BottomNavTab
import com.example.ui.BusinessCategory
import com.example.ui.Localization
import com.example.ui.resolveBusinessCategory
import com.example.ui.screens.tabs.DashboardTab
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import kotlinx.coroutines.delay

/**
 * Main Dashboard Composable screen that dynamically implements the two-step flow:
 * 1. Business Selector (when isSelectingBusiness is true or activeBusiness is null):
 *    Shows a list of ALL businesses added by the user with their logos, names, types,
 *    and a button to open that specific business.
 * 2. Active Business Dashboard (when a business is selected and isSelectingBusiness is false):
 *    Renders the dedicated dashboard for ONLY that business.
 */
@Composable
fun DashboardScreen(
    activeBusiness: BusinessEntity?,
    allBusinesses: List<BusinessEntity> = emptyList(),
    isSelectingBusiness: Boolean = false,
    onSelectBusiness: (BusinessEntity) -> Unit = {},
    onAddNewBusiness: () -> Unit = {},
    onBackToSelector: () -> Unit = {},
    onNavigateTab: (BottomNavTab) -> Unit = {},
    onOpenTenantSwitcher: () -> Unit = {},
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isSelectingBusiness || activeBusiness == null) {
        BusinessSelectorView(
            businesses = allBusinesses,
            activeBusiness = activeBusiness,
            onSelectBusiness = onSelectBusiness,
            onAddNewBusiness = onAddNewBusiness,
            appLanguage = appLanguage,
            onSelectLanguage = onSelectLanguage,
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
            modifier = modifier
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // Active Business Top Banner with Logo, Date/Time & Switch Business Button
            var liveTime by remember { mutableStateOf(Localization.formatLiveDateTime(appLanguage)) }
            LaunchedEffect(appLanguage) {
                while (true) {
                    liveTime = Localization.formatLiveDateTime(appLanguage)
                    delay(10000)
                }
            }

            Surface(
                color = PakEmeraldDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val category = resolveBusinessCategory(activeBusiness.type)
                            val logoIcon = getLogoIconForId(activeBusiness.logoUri, category)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                    .border(1.5.dp, PakGoldSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = logoIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = activeBusiness.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${activeBusiness.type} • ₨ ${activeBusiness.currency}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onBackToSelector,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_switch_business_header")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Localization.getString("switch_business", appLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Live Date & Time pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = liveTime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "100% Offline",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }

            // Render the dedicated dashboard for ONLY the active business
            DashboardTab(
                business = activeBusiness,
                onNavigateTab = onNavigateTab,
                onOpenTenantSwitcher = onBackToSelector,
                onOpenVoiceKhata = { onNavigateTab(BottomNavTab.VOICE_KHATA) },
                onOpenBarcodeStudio = { onNavigateTab(BottomNavTab.BARCODE_STUDIO) },
                onOpenSuppliers = { onNavigateTab(BottomNavTab.SUPPLIERS) },
                onOpenLoyalty = { onNavigateTab(BottomNavTab.LOYALTY) },
                onOpenBusinessHealth = { onNavigateTab(BottomNavTab.HEALTH_SCORE) },
                onOpenQuotations = { onNavigateTab(BottomNavTab.QUOTATIONS) },
                onOpenDigitalCard = { onNavigateTab(BottomNavTab.DIGITAL_CARD) },
                onOpenWhatsAppMarketing = { onNavigateTab(BottomNavTab.WHATSAPP_MARKETING) },
                onOpenChequeRegister = { onNavigateTab(BottomNavTab.CHEQUE_REGISTER) },
                onOpenTaxCalculator = { onNavigateTab(BottomNavTab.TAX_FBR) },
                onOpenWarrantyRepairs = { onNavigateTab(BottomNavTab.WARRANTY_REPAIRS) },
                onOpenStaffCommission = { onNavigateTab(BottomNavTab.STAFF_COMMISSION) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Backward compatibility overload for DashboardScreen.
 */
@Composable
fun DashboardScreen(
    business: BusinessEntity?,
    onNavigateTab: (BottomNavTab) -> Unit,
    onOpenTenantSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardScreen(
        activeBusiness = business,
        allBusinesses = if (business != null) listOf(business) else emptyList(),
        isSelectingBusiness = false,
        onSelectBusiness = {},
        onAddNewBusiness = {},
        onBackToSelector = onOpenTenantSwitcher,
        onNavigateTab = onNavigateTab,
        onOpenTenantSwitcher = onOpenTenantSwitcher,
        modifier = modifier
    )
}

/**
 * Step 1: Business Selector Screen
 * Displays a list of all businesses added by the user with their logo, name, type, and owner.
 * Tapping a card or clicking "Open This Business" sets that business as activeBusiness
 * and navigates to that business's dedicated dashboard.
 */
@Composable
fun BusinessSelectorView(
    businesses: List<BusinessEntity>,
    activeBusiness: BusinessEntity?,
    onSelectBusiness: (BusinessEntity) -> Unit,
    onAddNewBusiness: () -> Unit,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var liveClock by remember { mutableStateOf(Localization.formatLiveDateTime(appLanguage)) }

    LaunchedEffect(appLanguage) {
        while (true) {
            liveClock = Localization.formatLiveDateTime(appLanguage)
            delay(10000)
        }
    }

    val filteredBusinesses = remember(businesses, searchQuery) {
        if (searchQuery.isBlank()) {
            businesses
        } else {
            val query = searchQuery.trim().lowercase()
            businesses.filter {
                it.name.lowercase().contains(query) ||
                it.type.lowercase().contains(query) ||
                it.ownerName.lowercase().contains(query) ||
                it.address.lowercase().contains(query)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("business_selector_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                text = Localization.getString("app_title", appLanguage).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Day / Night Mode Shortcut
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { onToggleDarkMode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = if (isDarkMode) Color(0xFFFFD54F) else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isDarkMode) "Day" else "Night",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Language Switch Shortcut
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    val next = when (appLanguage) {
                                        AppLanguage.ENGLISH -> AppLanguage.URDU
                                        AppLanguage.URDU -> AppLanguage.HINDI
                                        AppLanguage.HINDI -> AppLanguage.ENGLISH
                                    }
                                    onSelectLanguage(next)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = appLanguage.nativeName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = Localization.getString("select_business_to_open", appLanguage),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = Localization.getString("choose_store_description", appLanguage),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    // Live Date & Time pill inside hero
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Current Time",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = liveClock,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onAddNewBusiness,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PakGoldSecondary,
                            contentColor = PakEmeraldDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_selector_add_business")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString("add_another_business", appLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Search Bar if more than 2 businesses exist
        if (businesses.size > 2) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(Localization.getString("search_hint", appLanguage)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PakEmeraldPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Localization.getString("registered_stores", appLanguage)} (${filteredBusinesses.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any store to open its dashboard",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty State if no businesses added yet
        if (filteredBusinesses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(PakEmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching business found" else "No Businesses Registered Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = if (searchQuery.isNotBlank()) "Try searching with a different name or type." else "Add your first store, clinic, workshop, gym, or restaurant to get started.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (searchQuery.isBlank()) {
                            Button(
                                onClick = onAddNewBusiness,
                                colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.getString("add_another_business", appLanguage))
                            }
                        }
                    }
                }
            }
        } else {
            // List of Businesses
            items(filteredBusinesses, key = { it.id }) { business ->
                val category = resolveBusinessCategory(business.type)
                val icon = getLogoIconForId(business.logoUri, category)
                val color = getCategoryColor(category)
                val isCurrentlyActive = business.id == activeBusiness?.id

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentlyActive) PakEmeraldContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isCurrentlyActive) 2.dp else 1.dp,
                        color = if (isCurrentlyActive) PakEmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentlyActive) 3.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBusiness(business) }
                        .testTag("business_card_${business.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Logo Icon in Circle with Border
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color.copy(alpha = 0.15f), CircleShape)
                                    .border(1.5.dp, color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Business Name, Tagline & Category
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = business.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (business.tagline.isNotBlank()) {
                                    Text(
                                        text = business.tagline,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = color.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = business.type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Active Indicator Badge
                            if (isCurrentlyActive) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PakEmeraldPrimary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = Localization.getString("active_badge", appLanguage),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Metadata Row: Owner, Currency
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Owner info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (business.ownerName.isNotBlank()) business.ownerName else "Owner Profile",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Phone info
                            if (business.phone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = PakEmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = business.phone,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Address Row if present
                        if (business.address.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = business.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Open Business Action Button
                        Button(
                            onClick = { onSelectBusiness(business) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentlyActive) PakEmeraldPrimary else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_business_${business.id}")
                        ) {
                            Text(
                                text = if (isCurrentlyActive) Localization.getString("open_active_dashboard", appLanguage) else Localization.getString("open_this_business", appLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Add Another Business Card at bottom
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, PakEmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddNewBusiness() }
                    .testTag("btn_bottom_add_business")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PakEmeraldContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Business",
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Localization.getString("add_another_business", appLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create a separate profile for another store, branch, clinic, workshop, or academy",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun getLogoIconForId(logoId: String, fallbackCategory: BusinessCategory): ImageVector {
    val found = LOGO_PRESETS.find { it.id == logoId }
    return found?.icon ?: getCategoryIcon(fallbackCategory)
}

/**
 * Category Icon Resolver Helper
 */
fun getCategoryIcon(category: BusinessCategory): ImageVector = when (category) {
    BusinessCategory.WHOLESALE -> Icons.Default.LocalShipping
    BusinessCategory.GYM -> Icons.Default.FitnessCenter
    BusinessCategory.PHARMACY -> Icons.Default.LocalPharmacy
    BusinessCategory.HOSPITAL -> Icons.Default.LocalHospital
    BusinessCategory.RESTAURANT -> Icons.Default.Restaurant
    BusinessCategory.BAKERY -> Icons.Default.Cake
    BusinessCategory.LAUNDRY -> Icons.Default.LocalLaundryService
    BusinessCategory.WORKSHOP -> Icons.Default.DirectionsCar
    BusinessCategory.ELECTRONICS -> Icons.Default.Devices
    BusinessCategory.TAILOR -> Icons.Default.Checkroom
    BusinessCategory.SCHOOL -> Icons.Default.School
    BusinessCategory.HOTEL -> Icons.Default.Hotel
    BusinessCategory.REAL_ESTATE -> Icons.Default.Apartment
    BusinessCategory.SALON -> Icons.Default.ContentCut
    BusinessCategory.GENERAL -> Icons.Default.PointOfSale
}

/**
 * Category Color Resolver Helper
 */
fun getCategoryColor(category: BusinessCategory): Color = when (category) {
    BusinessCategory.WHOLESALE -> Color(0xFF2E7D32)
    BusinessCategory.GYM -> Color(0xFFC2185B)
    BusinessCategory.PHARMACY -> Color(0xFF00897B)
    BusinessCategory.HOSPITAL -> Color(0xFF0284C7)
    BusinessCategory.RESTAURANT -> Color(0xFFE65100)
    BusinessCategory.BAKERY -> Color(0xFFD84315)
    BusinessCategory.LAUNDRY -> Color(0xFF0097A7)
    BusinessCategory.WORKSHOP -> Color(0xFF1565C0)
    BusinessCategory.ELECTRONICS -> Color(0xFF00838F)
    BusinessCategory.TAILOR -> Color(0xFF7B1FA2)
    BusinessCategory.SCHOOL -> Color(0xFF388E3C)
    BusinessCategory.HOTEL -> Color(0xFF4527A0)
    BusinessCategory.REAL_ESTATE -> Color(0xFF455A64)
    BusinessCategory.SALON -> Color(0xFFAD1457)
    BusinessCategory.GENERAL -> Color(0xFF00695C)
}
