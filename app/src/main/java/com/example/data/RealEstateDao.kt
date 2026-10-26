package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RealEstateDao {

    // Properties
    @Query("SELECT * FROM real_estate_properties WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getProperties(businessId: Long): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM real_estate_properties WHERE businessId = :businessId AND purpose = :purpose ORDER BY createdAt DESC")
    fun getPropertiesByPurpose(businessId: Long, purpose: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM real_estate_properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<PropertyEntity>)

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Query("UPDATE real_estate_properties SET status = :status WHERE id = :id")
    suspend fun updatePropertyStatus(id: Long, status: String)

    @Query("DELETE FROM real_estate_properties WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    @Query("SELECT COUNT(*) FROM real_estate_properties WHERE businessId = :businessId")
    suspend fun getPropertiesCount(businessId: Long): Int

    // Leads
    @Query("SELECT * FROM real_estate_leads WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getLeads(businessId: Long): Flow<List<LeadEntity>>

    @Query("SELECT * FROM real_estate_leads WHERE businessId = :businessId AND status = :status ORDER BY updatedAt DESC")
    fun getLeadsByStatus(businessId: Long, status: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM real_estate_leads WHERE id = :id")
    fun getLeadById(id: Long): Flow<LeadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<LeadEntity>)

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Query("UPDATE real_estate_leads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLeadStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM real_estate_leads WHERE id = :id")
    suspend fun deleteLead(id: Long)

    @Query("SELECT COUNT(*) FROM real_estate_leads WHERE businessId = :businessId")
    suspend fun getLeadsCount(businessId: Long): Int

    // Site Visits
    @Query("SELECT * FROM real_estate_site_visits WHERE businessId = :businessId ORDER BY visitDateString ASC, visitTimeString ASC")
    fun getSiteVisits(businessId: Long): Flow<List<SiteVisitEntity>>

    @Query("SELECT * FROM real_estate_site_visits WHERE businessId = :businessId AND status = :status ORDER BY visitDateString ASC, visitTimeString ASC")
    fun getSiteVisitsByStatus(businessId: Long, status: String): Flow<List<SiteVisitEntity>>

    @Query("SELECT * FROM real_estate_site_visits WHERE businessId = :businessId AND propertyId = :propertyId ORDER BY visitDateString ASC")
    fun getSiteVisitsForProperty(businessId: Long, propertyId: Long): Flow<List<SiteVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteVisit(visit: SiteVisitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteVisits(visits: List<SiteVisitEntity>)

    @Update
    suspend fun updateSiteVisit(visit: SiteVisitEntity)

    @Query("UPDATE real_estate_site_visits SET status = :status, clientFeedback = :feedback WHERE id = :id")
    suspend fun updateSiteVisitStatus(id: Long, status: String, feedback: String)

    @Query("DELETE FROM real_estate_site_visits WHERE id = :id")
    suspend fun deleteSiteVisit(id: Long)

    @Query("SELECT COUNT(*) FROM real_estate_site_visits WHERE businessId = :businessId")
    suspend fun getSiteVisitsCount(businessId: Long): Int
}
