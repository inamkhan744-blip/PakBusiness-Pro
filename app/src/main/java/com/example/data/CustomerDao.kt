package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing Customers in the core business ledger & CRM.
 * Adheres to Room best practices: reactive Flow queries and suspend mutations.
 */
@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY name ASC")
    fun getCustomers(businessId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun getActiveCustomers(businessId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(businessId: Long, phone: String): CustomerEntity?

    @Query("""
        SELECT * FROM customers 
        WHERE businessId = :businessId 
        AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchCustomers(businessId: Long, query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND currentBalancePkr > 0 ORDER BY currentBalancePkr DESC")
    fun getCustomersWithReceivableBalance(businessId: Long): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM customers WHERE id = :id AND businessId = :businessId")
    suspend fun deleteCustomerByIdAndBusiness(id: Long, businessId: Long)

    @Query("UPDATE customers SET currentBalancePkr = currentBalancePkr + :deltaBalance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustCustomerBalance(id: Long, deltaBalance: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM customers WHERE businessId = :businessId")
    suspend fun getCustomerCount(businessId: Long): Int

    @Query("SELECT SUM(currentBalancePkr) FROM customers WHERE businessId = :businessId AND currentBalancePkr > 0")
    fun getTotalReceivables(businessId: Long): Flow<Double?>
}
