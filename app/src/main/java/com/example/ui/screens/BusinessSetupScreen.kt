package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.Localization
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

/**
 * Complete list of Pakistani Businesses (Chota Bara Sara Karobar)
 */
val PAKISTAN_BUSINESS_TYPES = listOf(
    "General Store & Kiryana",
    "Super Mart & Grocery",
    "Milk Shop & Dairy (Doodh Dahi)",
    "Meat & Poultry (Murghi Gosht)",
    "Fruit & Vegetable (Sabzi Mandi)",
    "Restaurant, Dhaba & Fast Food",
    "Bakery & Sweets (Mithai)",
    "Pharmacy & Medical Store",
    "Hospital & Clinic / Doctor",
    "Mobile & Electronics Shop & Repair",
    "Auto Workshop & Mechanic / Tyre",
    "Clothing, Fabrics & Garments",
    "Tailor & Boutique (Darzi)",
    "Wholesale & Distribution / Mandi Arthi",
    "School, College & Tuition Academy",
    "Gym & Fitness Club",
    "Salon, Parlour & Barber Shop",
    "Hotel, Guest House & Hostel",
    "Real Estate & Property Dealer",
    "Laundry & Dry Cleaners (Dhobi)",
    "Hardware, Paint & Sanitary",
    "Sarafa & Jewelry / Gold Smith",
    "Book Depot, Stationery & Photostat",
    "Fertilizer, Seeds & Agriculture (Khaad Beej)",
    "Construction Material & Cement / Saria",
    "Services & Consulting"
)

/**
 * Preset logo options for quick logo/avatar selection
 */
data class LogoPreset(val id: String, val title: String, val icon: ImageVector)

val LOGO_PRESETS = listOf(
    LogoPreset("store", "Storefront", Icons.Default.Storefront),
    LogoPreset("mart", "Super Mart", Icons.Default.ShoppingCart),
    LogoPreset("dairy", "Dairy / Milk", Icons.Default.LocalDrink),
    LogoPreset("dining", "Restaurant", Icons.Default.Restaurant),
    LogoPreset("bakery", "Bakery", Icons.Default.Cake),
    LogoPreset("pharmacy", "Pharmacy", Icons.Default.LocalPharmacy),
    LogoPreset("hospital", "Hospital", Icons.Default.LocalHospital),
    LogoPreset("mobile", "Mobile", Icons.Default.Devices),
    LogoPreset("car", "Workshop", Icons.Default.DirectionsCar),
    LogoPreset("cloth", "Clothing", Icons.Default.Checkroom),
    LogoPreset("scissors", "Tailor", Icons.Default.ContentCut),
    LogoPreset("school", "Academy", Icons.Default.School),
    LogoPreset("gym", "Gym", Icons.Default.FitnessCenter),
    LogoPreset("hotel", "Hotel", Icons.Default.Hotel),
    LogoPreset("property", "Property", Icons.Default.Apartment),
    LogoPreset("delivery", "Wholesale", Icons.Default.LocalShipping),
    LogoPreset("gold", "Jewelry", Icons.Default.MonetizationOn),
    LogoPreset("book", "Books", Icons.Default.MenuBook),
    LogoPreset("farm", "Agriculture", Icons.Default.Agriculture),
    LogoPreset("build", "Hardware", Icons.Default.Build)
)

fun getBusinessTypeIcon(type: String): ImageVector {
    val clean = type.lowercase()
    return when {
        clean.contains("gym") || clean.contains("fitness") -> Icons.Default.FitnessCenter
        clean.contains("hospital") || clean.contains("clinic") || clean.contains("doctor") -> Icons.Default.LocalHospital
        clean.contains("pharmacy") || clean.contains("medical") -> Icons.Default.LocalPharmacy
        clean.contains("restaurant") || clean.contains("dhaba") || clean.contains("cafe") || clean.contains("food") -> Icons.Default.Restaurant
        clean.contains("bakery") || clean.contains("sweet") || clean.contains("mithai") -> Icons.Default.Cake
        clean.contains("school") || clean.contains("academy") || clean.contains("tuition") -> Icons.Default.School
        clean.contains("salon") || clean.contains("parlour") || clean.contains("barber") -> Icons.Default.ContentCut
        clean.contains("hotel") || clean.contains("guest") || clean.contains("hostel") -> Icons.Default.Hotel
        clean.contains("real estate") || clean.contains("property") -> Icons.Default.Apartment
        clean.contains("tailor") || clean.contains("boutique") || clean.contains("darzi") -> Icons.Default.ContentCut
        clean.contains("clothing") || clean.contains("garment") || clean.contains("fabric") -> Icons.Default.Checkroom
        clean.contains("laundry") || clean.contains("dry clean") -> Icons.Default.LocalLaundryService
        clean.contains("workshop") || clean.contains("mechanic") || clean.contains("tyre") -> Icons.Default.DirectionsCar
        clean.contains("electronic") || clean.contains("mobile") -> Icons.Default.Devices
        clean.contains("wholesale") || clean.contains("distribut") || clean.contains("mandi") -> Icons.Default.LocalShipping
        clean.contains("dairy") || clean.contains("milk") || clean.contains("doodh") -> Icons.Default.LocalDrink
        clean.contains("meat") || clean.contains("murghi") || clean.contains("gosht") -> Icons.Default.LocalDining
        clean.contains("hardware") || clean.contains("sanitary") || clean.contains("paint") -> Icons.Default.Build
        clean.contains("construction") || clean.contains("cement") || clean.contains("saria") -> Icons.Default.Construction
        clean.contains("sarafa") || clean.contains("jewelry") || clean.contains("gold") -> Icons.Default.MonetizationOn
        clean.contains("book") || clean.contains("stationery") -> Icons.Default.MenuBook
        clean.contains("agriculture") || clean.contains("fertilizer") || clean.contains("beej") || clean.contains("khaad") -> Icons.Default.Agriculture
        clean.contains("mart") || clean.contains("grocery") -> Icons.Default.ShoppingCart
        else -> Icons.Default.Storefront
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BusinessSetupScreen(
    onSaveBusiness: (name: String, type: String, owner: String, phone: String, address: String, currency: String, logoUri: String, tagline: String) -> Unit,
    canCancel: Boolean = false,
    onCancel: () -> Unit = {},
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    var businessName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PAKISTAN_BUSINESS_TYPES[0]) }
    var customType by remember { mutableStateOf("") }
    var isCustomTypeSelected by remember { mutableStateOf(false) }

    var selectedLogoId by remember { mutableStateOf("store") }
    var tagline by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val currency by remember { mutableStateOf("PKR") }

    var nameError by remember { mutableStateOf(false) }
    var ownerError by remember { mutableStateOf(false) }
    var typeSearchQuery by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val filteredBusinessTypes = remember(typeSearchQuery) {
        if (typeSearchQuery.isBlank()) {
            PAKISTAN_BUSINESS_TYPES
        } else {
            val q = typeSearchQuery.trim().lowercase()
            PAKISTAN_BUSINESS_TYPES.filter { it.lowercase().contains(q) }
        }
    }

    val selectedLogoPreset = LOGO_PRESETS.find { it.id == selectedLogoId } ?: LOGO_PRESETS[0]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (canCancel) Localization.getString("add_another_business", appLanguage) else Localization.getString("business_profile_logo", appLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "PakBusiness Pro • 100% Offline Multi-Tenant Suite",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (canCancel) {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 4.dp)
                                .size(36.dp)
                                .background(PakEmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Day / Night Mode Toggle
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("setup_dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Day Mode" else "Night Mode",
                            tint = if (isDarkMode) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Language Quick Switcher
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PakEmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                val nextLang = when (appLanguage) {
                                    AppLanguage.ENGLISH -> AppLanguage.URDU
                                    AppLanguage.URDU -> AppLanguage.HINDI
                                    AppLanguage.HINDI -> AppLanguage.ENGLISH
                                }
                                onSelectLanguage(nextLang)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = PakEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = appLanguage.nativeName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PakEmeraldPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Profile Preview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Logo Avatar Box
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                .border(2.dp, PakGoldSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedLogoPreset.icon,
                                contentDescription = "Business Logo",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (businessName.isNotBlank()) businessName else "Your Business Name",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            if (tagline.isNotBlank()) {
                                Text(
                                    text = tagline,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFDFBA)
                                )
                            }
                            Text(
                                text = "${if (isCustomTypeSelected && customType.isNotBlank()) customType else selectedType} • ${if (ownerName.isNotBlank()) ownerName else "Owner Profile"}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakGoldSecondary.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "₨ $currency",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Step 1: Choose Business Logo / Avatar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. ${Localization.getString("choose_logo", appLanguage)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PakEmeraldPrimary
                )
                Text(
                    text = "Select an official emblem icon for your store profile, POS receipts, and invoice header:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(LOGO_PRESETS, key = { it.id }) { preset ->
                        val isSelected = selectedLogoId == preset.id
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PakEmeraldContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PakEmeraldPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { selectedLogoId = preset.id }
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = preset.title,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = preset.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Business Profile Information
            Text(
                text = "2. Business Profile Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PakEmeraldPrimary
            )

            // Business Name
            OutlinedTextField(
                value = businessName,
                onValueChange = {
                    businessName = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("${Localization.getString("business_name", appLanguage)} *") },
                placeholder = { Text("e.g. Al-Madina Kiryana Store, Lahore") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = PakEmeraldPrimary)
                },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Business name is required", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("business_name_input")
            )

            // Tagline / Slogan
            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text(Localization.getString("tagline", appLanguage)) },
                placeholder = { Text("e.g. Baa Etemad Karobar / Quality Guaranteed") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Step 3: Select Business Type (All 25 Pakistani Business Categories)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "3. ${Localization.getString("business_type", appLanguage)} (${PAKISTAN_BUSINESS_TYPES.size} Pakistani Categories)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PakEmeraldPrimary
                )
                Text(
                    text = "Select which business you run. The app will configure specialized tabs, billing, inventory, and reports for this exact category:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Search Type
                OutlinedTextField(
                    value = typeSearchQuery,
                    onValueChange = { typeSearchQuery = it },
                    label = { Text("Filter business categories...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredBusinessTypes.forEach { type ->
                        val isSelected = (!isCustomTypeSelected && selectedType == type)
                        val icon = getBusinessTypeIcon(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomTypeSelected = false
                                selectedType = type
                            },
                            label = { Text(type, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary,
                                selectedLeadingIconColor = PakEmeraldPrimary
                            )
                        )
                    }

                    FilterChip(
                        selected = isCustomTypeSelected,
                        onClick = { isCustomTypeSelected = true },
                        label = { Text("Other / Custom", fontSize = 12.sp) },
                        leadingIcon = if (isCustomTypeSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PakEmeraldContainer,
                            selectedLabelColor = PakEmeraldPrimary
                        )
                    )
                }

                AnimatedVisibility(visible = isCustomTypeSelected) {
                    OutlinedTextField(
                        value = customType,
                        onValueChange = { customType = it },
                        label = { Text("Specify Custom Business Type") },
                        placeholder = { Text("e.g. Printing Press / Event Management") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            // Step 4: Owner & Contact Information
            Text(
                text = "4. Owner & Contact Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PakEmeraldPrimary
            )

            // Owner Name
            OutlinedTextField(
                value = ownerName,
                onValueChange = {
                    ownerName = it
                    if (it.isNotBlank()) ownerError = false
                },
                label = { Text("${Localization.getString("owner_name", appLanguage)} *") },
                placeholder = { Text("e.g. Inam Khan / Sheikh Muhammad") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                isError = ownerError,
                supportingText = {
                    if (ownerError) {
                        Text("Owner name is required", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_name_input")
            )

            // Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(Localization.getString("phone_number", appLanguage)) },
                placeholder = { Text("e.g. 0300 1234567") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input")
            )

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(Localization.getString("address", appLanguage)) },
                placeholder = { Text("e.g. Shop #14, Main Anarkali Bazaar, Lahore") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                },
                maxLines = 2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_input")
            )

            // Currency & Local Storage Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PakEmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = Localization.getString("offline_database", appLanguage) + ". PKR (₨) currency standard.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save & Launch Button
            Button(
                onClick = {
                    var hasError = false
                    if (businessName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (ownerName.isBlank()) {
                        ownerError = true
                        hasError = true
                    }

                    if (!hasError) {
                        val finalType = if (isCustomTypeSelected && customType.isNotBlank()) {
                            customType.trim()
                        } else {
                            selectedType
                        }
                        onSaveBusiness(
                            businessName.trim(),
                            finalType,
                            ownerName.trim(),
                            phone.trim(),
                            address.trim(),
                            currency,
                            selectedLogoId,
                            tagline.trim()
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PakEmeraldPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_business_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getString("save_business", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
