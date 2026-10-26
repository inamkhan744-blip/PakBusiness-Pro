package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {

    // --- Medicines Inventory ---

    @Query("SELECT * FROM pharmacy_medicines WHERE businessId = :businessId ORDER BY name ASC")
    fun getMedicines(businessId: Long): Flow<List<MedicineEntity>>

    @Query("""
        SELECT * FROM pharmacy_medicines 
        WHERE businessId = :businessId 
        AND (name LIKE '%' || :query || '%' 
             OR genericName LIKE '%' || :query || '%' 
             OR batchNumber LIKE '%' || :query || '%'
             OR rackNumber LIKE '%' || :query || '%'
             OR supplierName LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchMedicines(businessId: Long, query: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM pharmacy_medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM pharmacy_medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)

    @Query("UPDATE pharmacy_medicines SET quantity = :newQuantity WHERE id = :id")
    suspend fun updateStock(id: Long, newQuantity: Int)

    @Query("UPDATE pharmacy_medicines SET quantity = CASE WHEN quantity - :qty < 0 THEN 0 ELSE quantity - :qty END WHERE id = :id")
    suspend fun deductStock(id: Long, qty: Int)

    @Query("SELECT COUNT(*) FROM pharmacy_medicines WHERE businessId = :businessId")
    suspend fun getMedicineCount(businessId: Long): Int

    // --- Pharmacy Sales ---

    @Query("SELECT * FROM pharmacy_sales WHERE businessId = :businessId ORDER BY saleDate DESC")
    fun getSales(businessId: Long): Flow<List<PharmacySaleEntity>>

    @Query("SELECT * FROM pharmacy_sales WHERE id = :id")
    suspend fun getSaleById(id: Long): PharmacySaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: PharmacySaleEntity): Long

    @Query("DELETE FROM pharmacy_sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("SELECT COUNT(*) FROM pharmacy_sales WHERE businessId = :businessId")
    suspend fun getSalesCount(businessId: Long): Int
}
