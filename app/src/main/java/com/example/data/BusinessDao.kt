package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY createdAt ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE isActive = 1 LIMIT 1")
    fun getActiveBusiness(): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Query("SELECT COUNT(*) FROM businesses")
    suspend fun getBusinessCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("UPDATE businesses SET isActive = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE businesses SET isActive = 1 WHERE id = :id")
    suspend fun setActiveFlag(id: Long)

    @Transaction
    suspend fun switchActiveBusiness(id: Long) {
        clearActiveFlag()
        setActiveFlag(id)
    }

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteBusinessById(id: Long)
}
