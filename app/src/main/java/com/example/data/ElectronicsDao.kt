package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectronicsDao {

    // --- Product Inventory with IMEI & Warranty ---
    @Query("SELECT * FROM electronics_products WHERE businessId = :businessId ORDER BY name ASC")
    fun getProducts(businessId: Long): Flow<List<ElectronicsProductEntity>>

    @Query("SELECT * FROM electronics_products WHERE businessId = :businessId AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(businessId: Long, category: String): Flow<List<ElectronicsProductEntity>>

    @Query("SELECT * FROM electronics_products WHERE businessId = :businessId AND (imei1 LIKE '%' || :query || '%' OR imei2 LIKE '%' || :query || '%' OR serialNumber LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%')")
    fun searchProducts(businessId: Long, query: String): Flow<List<ElectronicsProductEntity>>

    @Query("SELECT * FROM electronics_products WHERE id = :id")
    suspend fun getProductById(id: Long): ElectronicsProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ElectronicsProductEntity): Long

    @Update
    suspend fun updateProduct(product: ElectronicsProductEntity)

    @Query("DELETE FROM electronics_products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("UPDATE electronics_products SET stockQuantity = :newStock WHERE id = :id")
    suspend fun updateProductStock(id: Long, newStock: Int)

    @Query("SELECT COUNT(*) FROM electronics_products WHERE businessId = :businessId")
    suspend fun getProductCount(businessId: Long): Int

    // --- Repair Tickets ---
    @Query("SELECT * FROM electronics_repair_tickets WHERE businessId = :businessId ORDER BY expectedDeliveryDate ASC, createdAt DESC")
    fun getRepairTickets(businessId: Long): Flow<List<RepairTicketEntity>>

    @Query("SELECT * FROM electronics_repair_tickets WHERE businessId = :businessId AND status = :status ORDER BY expectedDeliveryDate ASC")
    fun getRepairTicketsByStatus(businessId: Long, status: String): Flow<List<RepairTicketEntity>>

    @Query("SELECT * FROM electronics_repair_tickets WHERE id = :id")
    suspend fun getRepairTicketById(id: Long): RepairTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairTicket(ticket: RepairTicketEntity): Long

    @Update
    suspend fun updateRepairTicket(ticket: RepairTicketEntity)

    @Query("DELETE FROM electronics_repair_tickets WHERE id = :id")
    suspend fun deleteRepairTicket(id: Long)

    @Query("UPDATE electronics_repair_tickets SET status = :status WHERE id = :ticketId")
    suspend fun updateRepairTicketStatus(ticketId: Long, status: String)

    @Query("UPDATE electronics_repair_tickets SET advancePaid = :advance, remainingBalance = :balance WHERE id = :ticketId")
    suspend fun updateRepairTicketPayment(ticketId: Long, advance: Double, balance: Double)

    @Query("SELECT COUNT(*) FROM electronics_repair_tickets WHERE businessId = :businessId")
    suspend fun getRepairTicketCount(businessId: Long): Int

    // --- Repair Jobs ---
    @Query("SELECT * FROM repair_jobs WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getRepairJobs(businessId: Long): Flow<List<RepairJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairJob(job: RepairJobEntity): Long

    @Query("UPDATE repair_jobs SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
