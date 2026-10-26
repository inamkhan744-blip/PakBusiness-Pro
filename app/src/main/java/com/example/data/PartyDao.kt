package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing parties, customers, suppliers, and ledger accounts.
 * Supports multi-tenant isolation by businessId.
 */
@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE businessId = :businessId ORDER BY name ASC")
    fun getParties(businessId: Long): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND partyType = :partyType ORDER BY name ASC")
    fun getPartiesByType(businessId: Long, partyType: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    fun getPartyById(id: Long): Flow<PartyEntity?>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyByIdDirect(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchParties(businessId: Long, query: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND currentBalance > 0 ORDER BY currentBalance DESC")
    fun getPartiesWithReceivables(businessId: Long): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND currentBalance < 0 ORDER BY currentBalance ASC")
    fun getPartiesWithPayables(businessId: Long): Flow<List<PartyEntity>>

    @Query("SELECT SUM(currentBalance) FROM parties WHERE businessId = :businessId AND currentBalance > 0")
    fun getTotalReceivables(businessId: Long): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM parties WHERE businessId = :businessId AND currentBalance < 0")
    fun getTotalPayables(businessId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Query("DELETE FROM parties WHERE id = :id")
    suspend fun deleteParty(id: Long)

    @Query("DELETE FROM parties WHERE id = :id AND businessId = :businessId")
    suspend fun deletePartyByIdAndBusiness(id: Long, businessId: Long)

    @Query("UPDATE parties SET currentBalance = currentBalance + :balanceDelta, updatedAt = :updatedAt WHERE id = :partyId")
    suspend fun updatePartyBalance(partyId: Long, balanceDelta: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM parties WHERE businessId = :businessId")
    suspend fun getPartyCount(businessId: Long): Int
}
