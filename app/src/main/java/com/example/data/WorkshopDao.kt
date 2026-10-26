package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkshopDao {

    // --- Customer Vehicle Records ---
    @Query("SELECT * FROM auto_workshop_vehicles WHERE businessId = :businessId ORDER BY makeAndModel ASC")
    fun getVehicles(businessId: Long): Flow<List<WorkshopVehicleEntity>>

    @Query("SELECT * FROM auto_workshop_vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): WorkshopVehicleEntity?

    @Query("SELECT * FROM auto_workshop_vehicles WHERE businessId = :businessId AND (plateNumber LIKE '%' || :query || '%' OR chassisNumber LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' OR makeAndModel LIKE '%' || :query || '%')")
    fun searchVehicles(businessId: Long, query: String): Flow<List<WorkshopVehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: WorkshopVehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: WorkshopVehicleEntity)

    @Query("DELETE FROM auto_workshop_vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: Long)

    @Query("SELECT COUNT(*) FROM auto_workshop_vehicles WHERE businessId = :businessId")
    suspend fun getVehicleCount(businessId: Long): Int

    // --- Mechanics ---
    @Query("SELECT * FROM auto_workshop_mechanics WHERE businessId = :businessId ORDER BY name ASC")
    fun getMechanics(businessId: Long): Flow<List<WorkshopMechanicEntity>>

    @Query("SELECT * FROM auto_workshop_mechanics WHERE id = :id")
    suspend fun getMechanicById(id: Long): WorkshopMechanicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMechanic(mechanic: WorkshopMechanicEntity): Long

    @Update
    suspend fun updateMechanic(mechanic: WorkshopMechanicEntity)

    @Query("DELETE FROM auto_workshop_mechanics WHERE id = :id")
    suspend fun deleteMechanic(id: Long)

    @Query("SELECT COUNT(*) FROM auto_workshop_mechanics WHERE businessId = :businessId")
    suspend fun getMechanicCount(businessId: Long): Int

    // --- Job Cards ---
    @Query("SELECT * FROM auto_workshop_job_cards WHERE businessId = :businessId ORDER BY promisedDeliveryDate ASC, createdAt DESC")
    fun getJobCards(businessId: Long): Flow<List<WorkshopJobCardEntity>>

    @Query("SELECT * FROM auto_workshop_job_cards WHERE businessId = :businessId AND status = :status ORDER BY promisedDeliveryDate ASC")
    fun getJobCardsByStatus(businessId: Long, status: String): Flow<List<WorkshopJobCardEntity>>

    @Query("SELECT * FROM auto_workshop_job_cards WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getJobCardsForVehicle(vehicleId: Long): Flow<List<WorkshopJobCardEntity>>

    @Query("SELECT * FROM auto_workshop_job_cards WHERE id = :id")
    suspend fun getJobCardById(id: Long): WorkshopJobCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: WorkshopJobCardEntity): Long

    @Update
    suspend fun updateJobCard(jobCard: WorkshopJobCardEntity)

    @Query("DELETE FROM auto_workshop_job_cards WHERE id = :id")
    suspend fun deleteJobCard(id: Long)

    @Query("UPDATE auto_workshop_job_cards SET status = :status WHERE id = :jobCardId")
    suspend fun updateJobCardStatus(jobCardId: Long, status: String)

    @Query("UPDATE auto_workshop_job_cards SET assignedMechanicId = :mechanicId, assignedMechanicName = :mechanicName WHERE id = :jobCardId")
    suspend fun assignMechanic(jobCardId: Long, mechanicId: Long, mechanicName: String)

    @Query("UPDATE auto_workshop_job_cards SET advanceDeposit = :deposit, remainingBalance = :balance WHERE id = :jobCardId")
    suspend fun updateJobCardPayment(jobCardId: Long, deposit: Double, balance: Double)

    @Query("SELECT COUNT(*) FROM auto_workshop_job_cards WHERE businessId = :businessId")
    suspend fun getJobCardCount(businessId: Long): Int
}
