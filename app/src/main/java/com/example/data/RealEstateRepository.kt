package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealEstateRepository(private val realEstateDao: RealEstateDao) {

    // Properties
    fun getProperties(businessId: Long): Flow<List<PropertyEntity>> =
        realEstateDao.getProperties(businessId)

    fun getPropertiesByPurpose(businessId: Long, purpose: String): Flow<List<PropertyEntity>> =
        realEstateDao.getPropertiesByPurpose(businessId, purpose)

    fun getPropertyById(id: Long): Flow<PropertyEntity?> =
        realEstateDao.getPropertyById(id)

    suspend fun saveProperty(property: PropertyEntity): Long =
        if (property.id == 0L) {
            realEstateDao.insertProperty(property)
        } else {
            realEstateDao.updateProperty(property)
            property.id
        }

    suspend fun updatePropertyStatus(id: Long, status: String) =
        realEstateDao.updatePropertyStatus(id, status)

    suspend fun deleteProperty(id: Long) =
        realEstateDao.deleteProperty(id)

    // Leads
    fun getLeads(businessId: Long): Flow<List<LeadEntity>> =
        realEstateDao.getLeads(businessId)

    fun getLeadsByStatus(businessId: Long, status: String): Flow<List<LeadEntity>> =
        realEstateDao.getLeadsByStatus(businessId, status)

    fun getLeadById(id: Long): Flow<LeadEntity?> =
        realEstateDao.getLeadById(id)

    suspend fun saveLead(lead: LeadEntity): Long =
        if (lead.id == 0L) {
            realEstateDao.insertLead(lead)
        } else {
            realEstateDao.updateLead(lead)
            lead.id
        }

    suspend fun updateLeadStatus(id: Long, status: String) =
        realEstateDao.updateLeadStatus(id, status, System.currentTimeMillis())

    suspend fun deleteLead(id: Long) =
        realEstateDao.deleteLead(id)

    // Site Visits
    fun getSiteVisits(businessId: Long): Flow<List<SiteVisitEntity>> =
        realEstateDao.getSiteVisits(businessId)

    fun getSiteVisitsByStatus(businessId: Long, status: String): Flow<List<SiteVisitEntity>> =
        realEstateDao.getSiteVisitsByStatus(businessId, status)

    suspend fun saveSiteVisit(visit: SiteVisitEntity): Long =
        if (visit.id == 0L) {
            realEstateDao.insertSiteVisit(visit)
        } else {
            realEstateDao.updateSiteVisit(visit)
            visit.id
        }

    suspend fun updateSiteVisitStatus(id: Long, status: String, feedback: String = "") =
        realEstateDao.updateSiteVisitStatus(id, status, feedback)

    suspend fun deleteSiteVisit(id: Long) =
        realEstateDao.deleteSiteVisit(id)

    suspend fun seedSampleRealEstateDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake real estate entries
        withContext(Dispatchers.IO) {
            val count = realEstateDao.getPropertiesCount(businessId)
            if (count > 0) return@withContext

            val sampleProperties = listOf(
                PropertyEntity(
                    businessId = businessId,
                    title = "10 Marla Brand New Designer Spanish Villa",
                    purpose = "SALE",
                    propertyType = "HOUSE",
                    price = 47500000.0, // 4.75 Crore
                    location = "Phase 6, Block D",
                    city = "DHA Lahore",
                    size = "10 Marla",
                    bedrooms = 5,
                    bathrooms = 6,
                    status = "AVAILABLE",
                    ownerName = "Malik Tariq Mehmood",
                    ownerPhone = "0300-4567891",
                    description = "Architect designed, solid ash wood doors, Spanish tile flooring, Grohe fittings, dirty kitchen, servant quarter.",
                    isFeatured = true
                ),
                PropertyEntity(
                    businessId = businessId,
                    title = "1 Kanal Prime Corner Residential Plot",
                    purpose = "SALE",
                    propertyType = "PLOT",
                    price = 32000000.0, // 3.20 Crore
                    location = "Sector C, Overseas Block",
                    city = "Bahria Town Rawalpindi",
                    size = "1 Kanal",
                    bedrooms = 0,
                    bathrooms = 0,
                    status = "AVAILABLE",
                    ownerName = "Chaudhry Waseem Akram",
                    ownerPhone = "0321-7890123",
                    description = "Corner plot facing 80ft boulevard, adjacent to commercial hub and central mosque. Ready for immediate construction.",
                    isFeatured = true
                ),
                PropertyEntity(
                    businessId = businessId,
                    title = "3-Bed Luxury Executive Apartment with Sea Breeze",
                    purpose = "RENT",
                    propertyType = "FLAT",
                    price = 185000.0, // 185k / month
                    location = "Block 4, Marine Promenade",
                    city = "Clifton Karachi",
                    size = "2200 Sq Ft",
                    bedrooms = 3,
                    bathrooms = 4,
                    status = "AVAILABLE",
                    ownerName = "Dr. Farooq Siddiqui",
                    ownerPhone = "0333-2145678",
                    description = "Fully furnished with standby generator, 2 dedicated car parking slots, 24/7 security guard, sea facing balcony.",
                    isFeatured = false
                ),
                PropertyEntity(
                    businessId = businessId,
                    title = "5 Marla Solid Construction Double Story House",
                    purpose = "RENT",
                    propertyType = "HOUSE",
                    price = 75000.0, // 75k / month
                    location = "Sector M-1, Ring Road Interchange",
                    city = "Lake City Lahore",
                    size = "5 Marla",
                    bedrooms = 3,
                    bathrooms = 3,
                    status = "AVAILABLE",
                    ownerName = "Haji Abdul Rasheed",
                    ownerPhone = "0301-8923456",
                    description = "Double story, 2 separate electricity meters, solar setup installed, walking distance from park and commercial area.",
                    isFeatured = false
                ),
                PropertyEntity(
                    businessId = businessId,
                    title = "Ground Floor Commercial Showroom Plaza",
                    purpose = "SALE",
                    propertyType = "COMMERCIAL",
                    price = 85000000.0, // 8.5 Crore
                    location = "Main Markaz Sector G-11",
                    city = "Islamabad",
                    size = "1500 Sq Ft",
                    bedrooms = 0,
                    bathrooms = 1,
                    status = "UNDER_OFFER",
                    ownerName = "Sardar Bilal Khan",
                    ownerPhone = "0345-9876543",
                    description = "Front face on 120ft double road. High rental yield potential (current expected rent 4.5 Lakh/mo). Clear LDA/CDA documents.",
                    isFeatured = true
                )
            )
            realEstateDao.insertProperties(sampleProperties)

            val sampleLeads = listOf(
                LeadEntity(
                    businessId = businessId,
                    clientName = "Engr. Usman Qureshi",
                    phone = "0302-8765432",
                    email = "usman.qureshi@gmail.com",
                    requirementType = "BUY",
                    preferredPropertyType = "HOUSE",
                    preferredLocation = "DHA Phase 5 or Phase 6 Lahore",
                    preferredSize = "10 Marla",
                    minBudget = 40000000.0, // 4 Crore
                    maxBudget = 50000000.0, // 5 Crore
                    status = "SITE_VISIT",
                    notes = "Looking for brand new modern construction with 4-5 beds for family. Cash buyer.",
                    assignedAgent = "Hamza Real Estate Consultant"
                ),
                LeadEntity(
                    businessId = businessId,
                    clientName = "Kashif Naveed (Overseas Pakistani)",
                    phone = "0312-3456789",
                    email = "kashif.dubai@yahoo.com",
                    requirementType = "INVEST",
                    preferredPropertyType = "PLOT",
                    preferredLocation = "Bahria Town Rawalpindi / Islamabad",
                    preferredSize = "1 Kanal",
                    minBudget = 25000000.0,
                    maxBudget = 35000000.0,
                    status = "NEGOTIATION",
                    notes = "Brother will inspect on his behalf in Rawalpindi. Seeking corner plot.",
                    assignedAgent = "Ali Raza"
                ),
                LeadEntity(
                    businessId = businessId,
                    clientName = "Syed Murtaza Hussain",
                    phone = "0331-5544332",
                    email = "murtaza.hussain@fintech.pk",
                    requirementType = "RENT",
                    preferredPropertyType = "FLAT",
                    preferredLocation = "Clifton or DHA Phase 8 Karachi",
                    preferredSize = "3 Bed / 2000 Sq Ft",
                    minBudget = 150000.0,
                    maxBudget = 220000.0,
                    status = "CONTACTED",
                    notes = "Relocating from Islamabad next month. Needs standby generator backup and gated parking.",
                    assignedAgent = "Zainab Malik"
                ),
                LeadEntity(
                    businessId = businessId,
                    clientName = "Sheikh Imran Munir",
                    phone = "0300-1122334",
                    email = "",
                    requirementType = "BUY",
                    preferredPropertyType = "COMMERCIAL",
                    preferredLocation = "Islamabad G-11 or Blue Area",
                    preferredSize = "1000 - 2000 Sq Ft",
                    minBudget = 70000000.0,
                    maxBudget = 90000000.0,
                    status = "NEW",
                    notes = "Corporate showroom for electronics franchise. Looking for high footfall location.",
                    assignedAgent = "Hamza Real Estate Consultant"
                )
            )
            realEstateDao.insertLeads(sampleLeads)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            val tomorrowStr = dateFormat.format(Date(System.currentTimeMillis() + 86400000L))

            val sampleVisits = listOf(
                SiteVisitEntity(
                    businessId = businessId,
                    leadId = 1L,
                    clientName = "Engr. Usman Qureshi",
                    clientPhone = "0302-8765432",
                    propertyId = 1L,
                    propertyTitle = "10 Marla Brand New Designer Spanish Villa",
                    propertyLocation = "Phase 6, Block D, DHA Lahore",
                    visitDateTime = System.currentTimeMillis() + 86400000L,
                    visitDateString = tomorrowStr,
                    visitTimeString = "04:30 PM",
                    agentName = "Hamza Real Estate Consultant",
                    status = "SCHEDULED",
                    clientFeedback = "Interested in seeing master bedroom and basement finish."
                ),
                SiteVisitEntity(
                    businessId = businessId,
                    leadId = 2L,
                    clientName = "Kashif Naveed (Brother visiting)",
                    clientPhone = "0312-3456789",
                    propertyId = 2L,
                    propertyTitle = "1 Kanal Prime Corner Residential Plot",
                    propertyLocation = "Sector C, Overseas Block, Bahria Town Rawalpindi",
                    visitDateTime = System.currentTimeMillis() - 86400000L,
                    visitDateString = todayStr,
                    visitTimeString = "11:00 AM",
                    agentName = "Ali Raza",
                    status = "COMPLETED",
                    clientFeedback = "Liked the open corner location, discussing token money advance with owner."
                ),
                SiteVisitEntity(
                    businessId = businessId,
                    leadId = 3L,
                    clientName = "Syed Murtaza Hussain",
                    clientPhone = "0331-5544332",
                    propertyId = 3L,
                    propertyTitle = "3-Bed Luxury Executive Apartment with Sea Breeze",
                    propertyLocation = "Block 4, Marine Promenade, Clifton Karachi",
                    visitDateTime = System.currentTimeMillis() + 172800000L,
                    visitDateString = tomorrowStr,
                    visitTimeString = "06:00 PM",
                    agentName = "Zainab Malik",
                    status = "SCHEDULED",
                    clientFeedback = "Family wants to inspect building lift and security."
                )
            )
            realEstateDao.insertSiteVisits(sampleVisits)
        }
    }
}
