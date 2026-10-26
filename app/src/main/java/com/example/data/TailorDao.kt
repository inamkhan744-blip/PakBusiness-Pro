package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TailorDao {

    // Customers
    @Query("SELECT * FROM tailor_customers WHERE businessId = :businessId ORDER BY name ASC")
    fun getCustomers(businessId: Long): Flow<List<TailorCustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: TailorCustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: TailorCustomerEntity)

    @Query("DELETE FROM tailor_customers WHERE id = :id")
    suspend fun deleteCustomer(id: Long)

    // Measurements
    @Query("SELECT * FROM tailor_measurements WHERE businessId = :businessId ORDER BY customerName ASC")
    fun getAllMeasurements(businessId: Long): Flow<List<TailorMeasurementEntity>>

    @Query("SELECT * FROM tailor_measurements WHERE businessId = :businessId AND customerId = :customerId ORDER BY updatedAt DESC")
    fun getMeasurementsForCustomer(businessId: Long, customerId: Long): Flow<List<TailorMeasurementEntity>>

    @Query("SELECT * FROM tailor_measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): TailorMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: TailorMeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: TailorMeasurementEntity)

    @Query("DELETE FROM tailor_measurements WHERE id = :id")
    suspend fun deleteMeasurement(id: Long)

    // Orders
    @Query("SELECT * FROM tailor_orders WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getAllOrders(businessId: Long): Flow<List<TailorOrderEntity>>

    @Query("SELECT * FROM tailor_orders WHERE businessId = :businessId AND status = :status ORDER BY deliveryDate ASC")
    fun getOrdersByStatus(businessId: Long, status: String): Flow<List<TailorOrderEntity>>

    @Query("SELECT * FROM tailor_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): TailorOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TailorOrderEntity): Long

    @Update
    suspend fun updateOrder(order: TailorOrderEntity)

    @Query("DELETE FROM tailor_orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)

    @Query("UPDATE tailor_orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE tailor_orders SET advancePaid = :advance, remainingBalance = :balance WHERE id = :orderId")
    suspend fun updateOrderPayment(orderId: Long, advance: Double, balance: Double)

    @Query("SELECT COUNT(*) FROM tailor_orders WHERE businessId = :businessId")
    suspend fun getOrderCount(businessId: Long): Int

    @Query("SELECT COUNT(*) FROM tailor_customers WHERE businessId = :businessId")
    suspend fun getCustomerCount(businessId: Long): Int
}
