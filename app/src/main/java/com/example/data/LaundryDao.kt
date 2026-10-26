package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LaundryDao {
    // Customers
    @Query("SELECT * FROM laundry_customers WHERE businessId = :businessId ORDER BY name ASC")
    fun getCustomers(businessId: Long): Flow<List<LaundryCustomerEntity>>

    @Query("SELECT * FROM laundry_customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): LaundryCustomerEntity?

    @Query("SELECT COUNT(*) FROM laundry_customers WHERE businessId = :businessId")
    suspend fun getCustomerCount(businessId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: LaundryCustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: LaundryCustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: LaundryCustomerEntity)

    // Orders
    @Query("SELECT * FROM laundry_orders WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getOrders(businessId: Long): Flow<List<LaundryOrderEntity>>

    @Query("SELECT * FROM laundry_orders WHERE businessId = :businessId AND status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(businessId: Long, status: String): Flow<List<LaundryOrderEntity>>

    @Query("SELECT * FROM laundry_orders WHERE businessId = :businessId AND customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersForCustomer(businessId: Long, customerId: Long): Flow<List<LaundryOrderEntity>>

    @Query("SELECT * FROM laundry_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): LaundryOrderEntity?

    @Query("SELECT COUNT(*) FROM laundry_orders WHERE businessId = :businessId")
    suspend fun getOrderCount(businessId: Long): Int

    @Query("SELECT COUNT(*) FROM laundry_orders WHERE businessId = :businessId AND status = :status")
    suspend fun getCountByStatus(businessId: Long, status: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: LaundryOrderEntity): Long

    @Update
    suspend fun updateOrder(order: LaundryOrderEntity)

    @Delete
    suspend fun deleteOrder(order: LaundryOrderEntity)

    @Query("UPDATE laundry_orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE laundry_orders SET status = :status, deliveredAt = :deliveredAt, paymentStatus = :paymentStatus, balanceDue = 0.0, advancePaid = totalAmount WHERE id = :orderId")
    suspend fun markOrderDelivered(orderId: Long, status: String = "DELIVERED", deliveredAt: Long, paymentStatus: String = "PAID")

    @Query("UPDATE laundry_orders SET advancePaid = advancePaid + :additionalPayment, balanceDue = :newBalance, paymentStatus = :paymentStatus WHERE id = :orderId")
    suspend fun updateOrderPayment(orderId: Long, additionalPayment: Double, newBalance: Double, paymentStatus: String)
}
