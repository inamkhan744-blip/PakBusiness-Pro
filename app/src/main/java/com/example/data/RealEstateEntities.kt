package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "real_estate_properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val title: String,
    val purpose: String, // "SALE", "RENT"
    val propertyType: String, // "HOUSE", "PLOT", "FLAT", "COMMERCIAL", "FARMHOUSE"
    val price: Double, // in PKR
    val location: String, // e.g. "DHA Phase 6, Sector C"
    val city: String, // e.g. "Lahore"
    val size: String, // e.g. "10 Marla", "1 Kanal", "5 Marla"
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val status: String = "AVAILABLE", // "AVAILABLE", "UNDER_OFFER", "SOLD", "RENTED"
    val ownerName: String = "",
    val ownerPhone: String = "",
    val description: String = "",
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "real_estate_leads")
data class LeadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val clientName: String,
    val phone: String,
    val email: String = "",
    val requirementType: String = "BUY", // "BUY", "RENT", "INVEST"
    val preferredPropertyType: String = "HOUSE", // "HOUSE", "PLOT", "FLAT", "COMMERCIAL", "ANY"
    val preferredLocation: String = "",
    val preferredSize: String = "",
    val minBudget: Double = 0.0, // in PKR
    val maxBudget: Double = 0.0, // in PKR
    val status: String = "NEW", // "NEW", "CONTACTED", "SITE_VISIT", "NEGOTIATION", "CLOSED_WON", "LOST"
    val notes: String = "",
    val assignedAgent: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "real_estate_site_visits")
data class SiteVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val leadId: Long? = null,
    val clientName: String,
    val clientPhone: String,
    val propertyId: Long,
    val propertyTitle: String,
    val propertyLocation: String,
    val visitDateTime: Long = System.currentTimeMillis(),
    val visitDateString: String, // e.g. "2026-09-22"
    val visitTimeString: String, // e.g. "04:30 PM"
    val agentName: String = "",
    val status: String = "SCHEDULED", // "SCHEDULED", "COMPLETED", "CANCELLED"
    val clientFeedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
