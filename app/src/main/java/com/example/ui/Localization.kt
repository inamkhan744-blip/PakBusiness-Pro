package com.example.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage(val code: String, val title: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    URDU("ur", "Urdu", "اردو"),
    HINDI("hi", "Hindi", "हिंदी")
}

object Localization {
    fun getString(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.URDU -> URDU_STRINGS[key] ?: ENGLISH_STRINGS[key] ?: key
            AppLanguage.HINDI -> HINDI_STRINGS[key] ?: ENGLISH_STRINGS[key] ?: key
            AppLanguage.ENGLISH -> ENGLISH_STRINGS[key] ?: key
        }
    }

    fun formatLiveDateTime(lang: AppLanguage): String {
        val now = Date()
        return when (lang) {
            AppLanguage.URDU -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH).format(now)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(now)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(now)
                val urduDay = when (dayFormat) {
                    "Monday" -> "پیر"
                    "Tuesday" -> "منگل"
                    "Wednesday" -> "بدھ"
                    "Thursday" -> "جمعرات"
                    "Friday" -> "جمعہ مبارک"
                    "Saturday" -> "ہفتہ"
                    "Sunday" -> "اتوار"
                    else -> dayFormat
                }
                "$urduDay، $dateFormat • $timeFormat"
            }
            AppLanguage.HINDI -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH).format(now)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(now)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(now)
                val hindiDay = when (dayFormat) {
                    "Monday" -> "सोमवार"
                    "Tuesday" -> "मंगलवार"
                    "Wednesday" -> "बुधवार"
                    "Thursday" -> "गुरुवार"
                    "Friday" -> "शुक्रवार"
                    "Saturday" -> "शनिवार"
                    "Sunday" -> "रविवार"
                    else -> dayFormat
                }
                "$hindiDay, $dateFormat • $timeFormat"
            }
            AppLanguage.ENGLISH -> {
                SimpleDateFormat("EEEE, dd MMM yyyy • hh:mm a", Locale.getDefault()).format(now)
            }
        }
    }

    private val ENGLISH_STRINGS = mapOf(
        "app_title" to "PakBusiness Pro",
        "business_selector" to "Business Selector",
        "select_business_to_open" to "Select Business to Open",
        "choose_store_description" to "Choose a store, clinic, workshop, gym, or academy to open its exclusive modules, POS, inventory, staff, and financial records.",
        "switch_business" to "Switch Business",
        "add_another_business" to "Add Another Business",
        "registered_stores" to "Registered Stores",
        "open_this_business" to "Open This Business",
        "open_active_dashboard" to "Open Active Dashboard",
        "active_badge" to "ACTIVE",
        "dashboard" to "Dashboard",
        "pos" to "POS Terminal",
        "inventory" to "Inventory",
        "customers" to "Customers & Khata",
        "expenses" to "Daily Kharcha",
        "staff" to "Staff & Haziri",
        "reports" to "Day-End & Reports",
        "settings" to "Settings",
        "offline" to "Offline",
        "offline_database" to "100% Offline SQLite Database",
        "day_mode" to "Day Mode (Light)",
        "night_mode" to "Night Mode (Dark)",
        "language" to "Language",
        "business_profile_logo" to "Business Profile & Logo",
        "choose_logo" to "Select Store Logo / Icon",
        "business_name" to "Business Name",
        "business_type" to "Business Type",
        "tagline" to "Tagline / Slogan",
        "owner_name" to "Owner Name",
        "phone_number" to "Phone / WhatsApp",
        "address" to "Address / Location",
        "currency" to "Currency",
        "save_business" to "Save & Launch Dashboard",
        "search_hint" to "Search store by name, type, city..."
    )

    private val URDU_STRINGS = mapOf(
        "app_title" to "پاک بزنس پرو",
        "business_selector" to "کاروبار منتخب کریں",
        "select_business_to_open" to "اپنا کاروبار منتخب کریں",
        "choose_store_description" to "اپنے اسٹور، کلینک، ورکشاپ، جم یا اکیڈمی کا خصوصی ڈیش بورڈ، پی او ایس، کھاتہ اور اسٹاک کھولیں۔",
        "switch_business" to "کاروبار تبدیل کریں",
        "add_another_business" to "نیا کاروبار شامل کریں",
        "registered_stores" to "رجسٹرڈ دکانیں",
        "open_this_business" to "یہ کاروبار کھولیں",
        "open_active_dashboard" to "ایکٹو ڈیش بورڈ کھولیں",
        "active_badge" to "فعال",
        "dashboard" to "ڈیش بورڈ",
        "pos" to "پی او ایس بلنگ",
        "inventory" to "اسٹاک اور مال",
        "customers" to "گاہک کھاتہ اور ادھار",
        "expenses" to "روزانہ کا خرچہ",
        "staff" to "ملازمین اور حاضری",
        "reports" to "دن کا اختتام اور رپورٹس",
        "settings" to "کاروباری سیٹنگز",
        "offline" to "آف لائن",
        "offline_database" to "100% محفوظ لوکل آف لائن ڈیٹا بیس",
        "day_mode" to "ڈے موڈ (لائٹ)",
        "night_mode" to "نائٹ موڈ (ڈارک)",
        "language" to "زبان منتخب کریں",
        "business_profile_logo" to "کاروبار کی پروفائل اور لوگو",
        "choose_logo" to "کاروباری لوگو / نشان منتخب کریں",
        "business_name" to "کاروبار / دکان کا نام",
        "business_type" to "کاروبار کی قسم",
        "tagline" to "نعرہ / سلوگن",
        "owner_name" to "مالک کا نام",
        "phone_number" to "فون / واٹس ایپ نمبر",
        "address" to "دکان یا دفتر کا پتہ",
        "currency" to "کرنسی (روپیہ)",
        "save_business" to "محفوظ کریں اور ڈیش بورڈ کھولیں",
        "search_hint" to "نام، قسم یا شہر سے تلاش کریں..."
    )

    private val HINDI_STRINGS = mapOf(
        "app_title" to "पाक बिजनेस प्रो",
        "business_selector" to "व्यापार चुनें",
        "select_business_to_open" to "खोलने के लिए व्यापार चुनें",
        "choose_store_description" to "अपनी दुकान, क्लिनिक, वर्कशॉप, जिम या अकादमी का विशेष डैशबोर्ड, बिलिंग, खाता और स्टॉक खोलें।",
        "switch_business" to "व्यापार बदलें",
        "add_another_business" to "नया व्यापार जोड़ें",
        "registered_stores" to "पंजीकृत दुकानें",
        "open_this_business" to "यह व्यापार खोलें",
        "open_active_dashboard" to "सक्रिय डैशबोर्ड खोलें",
        "active_badge" to "सक्रिय",
        "dashboard" to "डैशबोर्ड",
        "pos" to "पीओएस बिलिंग",
        "inventory" to "इन्वेंट्री / स्टॉक",
        "customers" to "ग्राहक और उधार खाता",
        "expenses" to "दैनिक खर्च",
        "staff" to "स्टाफ और उपस्थिति",
        "reports" to "रिपोर्ट्स और मुनाफा",
        "settings" to "सेटिंग्स",
        "offline" to "ऑफ़लाइन",
        "offline_database" to "100% सुरक्षित स्थानीय ऑफ़लाइन डेटाबेस",
        "day_mode" to "डे मोड (लाइट)",
        "night_mode" to "नाइट मोड (डार्क)",
        "language" to "भाषा चुनें",
        "business_profile_logo" to "व्यापार प्रोफाइल और लोगो",
        "choose_logo" to "दुकान का लोगो / चिह्न चुनें",
        "business_name" to "व्यापार / दुकान का नाम",
        "business_type" to "व्यापार का प्रकार",
        "tagline" to "टैगलाइन / स्लोगन",
        "owner_name" to "मालिक का नाम",
        "phone_number" to "फ़ोन / व्हाट्सएप नंबर",
        "address" to "दुकान या कार्यालय का पता",
        "currency" to "मुद्रा (रुपया)",
        "save_business" to "सुरक्षित करें और डैशबोर्ड खोलें",
        "search_hint" to "नाम, प्रकार या शहर से खोजें..."
    )
}
