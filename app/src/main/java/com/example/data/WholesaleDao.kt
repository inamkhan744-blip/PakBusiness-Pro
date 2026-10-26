package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WholesaleDao {

    // --- Parties ---
    @Query("SELECT * FROM wholesale_parties WHERE businessId = :businessId ORDER BY name ASC")
    fun getParties(businessId: Long): Flow<List<WholesalePartyEntity>>

    @Query("SELECT * FROM wholesale_parties WHERE id = :id")
    suspend fun getPartyById(id: Long): WholesalePartyEntity?

    @Query("SELECT * FROM wholesale_parties WHERE businessId = :businessId AND partyType = :partyType ORDER BY name ASC")
    fun getPartiesByType(businessId: Long, partyType: String): Flow<List<WholesalePartyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: WholesalePartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<WholesalePartyEntity>)

    @Update
    suspend fun updateParty(party: WholesalePartyEntity)

    @Delete
    suspend fun deleteParty(party: WholesalePartyEntity)

    @Query("UPDATE wholesale_parties SET currentBalance = currentBalance + :balanceDelta WHERE id = :partyId")
    suspend fun adjustPartyBalance(partyId: Long, balanceDelta: Double)

    // --- Bulk Orders ---
    @Query("SELECT * FROM wholesale_bulk_orders WHERE businessId = :businessId ORDER BY id DESC")
    fun getBulkOrders(businessId: Long): Flow<List<WholesaleBulkOrderEntity>>

    @Query("SELECT * FROM wholesale_bulk_orders WHERE partyId = :partyId ORDER BY id DESC")
    fun getOrdersByParty(partyId: Long): Flow<List<WholesaleBulkOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulkOrder(order: WholesaleBulkOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulkOrders(orders: List<WholesaleBulkOrderEntity>)

    @Update
    suspend fun updateBulkOrder(order: WholesaleBulkOrderEntity)

    @Delete
    suspend fun deleteBulkOrder(order: WholesaleBulkOrderEntity)

    @Query("UPDATE wholesale_bulk_orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)

    // --- Payments / Udhaar Ledger Adjustments ---
    @Query("SELECT * FROM wholesale_payments WHERE businessId = :businessId ORDER BY id DESC")
    fun getPayments(businessId: Long): Flow<List<WholesalePaymentEntity>>

    @Query("SELECT * FROM wholesale_payments WHERE partyId = :partyId ORDER BY id DESC")
    fun getPaymentsByParty(partyId: Long): Flow<List<WholesalePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: WholesalePaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<WholesalePaymentEntity>)

    @Delete
    suspend fun deletePayment(payment: WholesalePaymentEntity)
}
