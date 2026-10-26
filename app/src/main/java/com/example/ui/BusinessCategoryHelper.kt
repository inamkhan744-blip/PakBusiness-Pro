package com.example.ui

/**
 * Helper definitions to dynamically categorize business types and filter
 * modules, dashboard cards, and navigation tabs according to activeBusiness.type.
 */
enum class BusinessCategory(val displayName: String) {
    GYM("Gym & Fitness"),
    PHARMACY("Pharmacy & Healthcare"),
    HOSPITAL("Hospital & Clinic"),
    RESTAURANT("Restaurant & Cafe"),
    WHOLESALE("Wholesale & Distribution"),
    BAKERY("Bakery & Sweets"),
    LAUNDRY("Laundry & Dry Cleaners"),
    WORKSHOP("Auto Workshop & Garage"),
    ELECTRONICS("Electronics & Mobile"),
    TAILOR("Tailor & Boutique"),
    SCHOOL("School & Academy"),
    HOTEL("Hotel & Guest House"),
    REAL_ESTATE("Real Estate Agency"),
    SALON("Salon & Beauty Parlor"),
    GENERAL("Retail & General Store")
}

fun resolveBusinessCategory(type: String?): BusinessCategory {
    val clean = type?.lowercase()?.trim() ?: return BusinessCategory.GENERAL
    return when {
        clean.contains("wholesale") || clean.contains("distribut") || clean.contains("whole-sale") ||
        clean.contains("wholesaler") || clean.contains("whole seller") || clean.contains("trader") ||
        clean.contains("distribution") || clean.contains("mandi") || clean.contains("agency supply") -> BusinessCategory.WHOLESALE

        clean.contains("gym") || clean.contains("fitness") || clean.contains("workout") -> BusinessCategory.GYM
        clean.contains("pharmacy") || clean.contains("medical store") || clean.contains("medicine") || clean.contains("chemist") || clean.contains("drug") -> BusinessCategory.PHARMACY
        clean.contains("hospital") || clean.contains("clinic") || clean.contains("doctor") || clean.contains("dental") -> BusinessCategory.HOSPITAL
        clean.contains("healthcare") -> {
            if (clean.contains("pharmacy")) BusinessCategory.PHARMACY else BusinessCategory.HOSPITAL
        }
        clean.contains("restaurant") || clean.contains("cafe") || clean.contains("food") || clean.contains("dining") || clean.contains("dhaba") || clean.contains("coffee") -> BusinessCategory.RESTAURANT
        clean.contains("bakery") || clean.contains("sweet") || clean.contains("mithai") || clean.contains("cake") -> BusinessCategory.BAKERY
        clean.contains("laundry") || clean.contains("dry clean") || clean.contains("dhobi") || clean.contains("wash") -> BusinessCategory.LAUNDRY
        clean.contains("workshop") || clean.contains("auto") || clean.contains("garage") || clean.contains("mechanic") -> BusinessCategory.WORKSHOP
        clean.contains("electronic") || clean.contains("mobile") || clean.contains("phone") || clean.contains("repair") -> BusinessCategory.ELECTRONICS
        clean.contains("tailor") || clean.contains("boutique") || clean.contains("garment") || clean.contains("textile") || clean.contains("stitching") -> BusinessCategory.TAILOR
        clean.contains("school") || clean.contains("academy") || clean.contains("college") || clean.contains("education") || clean.contains("tuition") -> BusinessCategory.SCHOOL
        clean.contains("hotel") || clean.contains("guest house") || clean.contains("hostel") || clean.contains("motel") || clean.contains("inn") || clean.contains("resort") -> BusinessCategory.HOTEL
        clean.contains("real estate") || clean.contains("property") || clean.contains("estate") || clean.contains("realtor") -> BusinessCategory.REAL_ESTATE
        clean.contains("salon") || clean.contains("parlor") || clean.contains("parlour") || clean.contains("beauty") || clean.contains("barber") || clean.contains("hair") -> BusinessCategory.SALON
        else -> BusinessCategory.GENERAL
    }
}

fun getTabsForBusiness(type: String?): List<BottomNavTab> {
    return when (resolveBusinessCategory(type)) {
        BusinessCategory.WHOLESALE -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.WHOLESALE,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.GYM -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.GYM,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.PHARMACY -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.PHARMACY,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.HOSPITAL -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.HOSPITAL,
            BottomNavTab.POS,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.RESTAURANT -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.RESTAURANT,
            BottomNavTab.POS,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.BAKERY -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.BAKERY,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.LAUNDRY -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.LAUNDRY,
            BottomNavTab.POS,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.WORKSHOP -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.WORKSHOP,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.ELECTRONICS -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.ELECTRONICS,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.TAILOR -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.TAILOR,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.SCHOOL -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.SCHOOL,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.HOTEL -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.HOTEL,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.REAL_ESTATE -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.REAL_ESTATE,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.SALON -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.SALON,
            BottomNavTab.POS,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
        BusinessCategory.GENERAL -> listOf(
            BottomNavTab.DASHBOARD,
            BottomNavTab.POS,
            BottomNavTab.INVENTORY,
            BottomNavTab.CUSTOMERS,
            BottomNavTab.SETTINGS
        )
    }
}
