package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository pattern implementation for Customers CRM and Khata ledger operations.
 */
class CustomerRepository(
    private val customerDao: CustomerDao
) {
    fun getCustomers(businessId: Long): Flow<List<CustomerEntity>> =
        customerDao.getCustomers(businessId)

    fun getActiveCustomers(businessId: Long): Flow<List<CustomerEntity>> =
        customerDao.getActiveCustomers(businessId)

    fun getCustomerById(id: Long): Flow<CustomerEntity?> =
        customerDao.getCustomerById(id)

    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getCustomerByIdDirect(id)
    }

    suspend fun getCustomerByPhone(businessId: Long, phone: String): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getCustomerByPhone(businessId, phone)
    }

    fun searchCustomers(businessId: Long, query: String): Flow<List<CustomerEntity>> =
        customerDao.searchCustomers(businessId, query)

    fun getCustomersWithReceivableBalance(businessId: Long): Flow<List<CustomerEntity>> =
        customerDao.getCustomersWithReceivableBalance(businessId)

    suspend fun insertCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun insertCustomers(customers: List<CustomerEntity>) = withContext(Dispatchers.IO) {
        customerDao.insertCustomers(customers)
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun deleteCustomerById(id: Long) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomerById(id)
    }

    suspend fun adjustCustomerBalance(id: Long, deltaBalance: Double) = withContext(Dispatchers.IO) {
        customerDao.adjustCustomerBalance(id, deltaBalance)
    }

    suspend fun getCustomerCount(businessId: Long): Int = withContext(Dispatchers.IO) {
        customerDao.getCustomerCount(businessId)
    }

    fun getTotalReceivables(businessId: Long): Flow<Double?> =
        customerDao.getTotalReceivables(businessId)
}
