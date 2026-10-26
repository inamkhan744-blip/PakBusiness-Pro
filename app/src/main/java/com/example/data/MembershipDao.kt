package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Membership plans and packages in the gym module.
 * Provides multi-tenant reactive queries and CRUD operations.
 */
@Dao
interface MembershipDao {

    @Query("SELECT * FROM memberships WHERE businessId = :businessId ORDER BY pricePkr ASC")
    fun getMemberships(businessId: Long): Flow<List<MembershipEntity>>

    @Query("SELECT * FROM memberships WHERE businessId = :businessId AND isActive = 1 ORDER BY pricePkr ASC")
    fun getActiveMemberships(businessId: Long): Flow<List<MembershipEntity>>

    @Query("SELECT * FROM memberships WHERE id = :id LIMIT 1")
    fun getMembershipById(id: Long): Flow<MembershipEntity?>

    @Query("SELECT * FROM memberships WHERE id = :id LIMIT 1")
    suspend fun getMembershipByIdDirect(id: Long): MembershipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: MembershipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberships(memberships: List<MembershipEntity>)

    @Update
    suspend fun updateMembership(membership: MembershipEntity)

    @Query("DELETE FROM memberships WHERE id = :id")
    suspend fun deleteMembershipById(id: Long)

    @Query("DELETE FROM memberships WHERE id = :id AND businessId = :businessId")
    suspend fun deleteMembershipByIdAndBusiness(id: Long, businessId: Long)

    @Query("SELECT COUNT(*) FROM memberships WHERE businessId = :businessId")
    suspend fun getMembershipCount(businessId: Long): Int
}
