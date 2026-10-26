package com.example.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val dao: BusinessDao) {

    val allBusinesses: Flow<List<BusinessEntity>> = dao.getAllBusinesses()
    val activeBusiness: Flow<BusinessEntity?> = dao.getActiveBusiness()

    suspend fun getBusinessCount(): Int = dao.getBusinessCount()

    suspend fun createBusiness(
        name: String,
        type: String,
        ownerName: String,
        phone: String,
        address: String,
        currency: String = "PKR",
        logoUri: String = "",
        tagline: String = "",
        setAsActive: Boolean = true
    ): Long {
        if (setAsActive) {
            dao.clearActiveFlag()
        }
        val business = BusinessEntity(
            name = name.trim(),
            type = type.trim(),
            ownerName = ownerName.trim(),
            phone = phone.trim(),
            address = address.trim(),
            currency = currency.trim(),
            logoUri = logoUri.trim(),
            tagline = tagline.trim(),
            isActive = setAsActive
        )
        val newId = dao.insertBusiness(business)
        if (setAsActive) {
            dao.setActiveFlag(newId)
        }
        return newId
    }

    suspend fun switchActiveBusiness(id: Long) {
        dao.switchActiveBusiness(id)
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        dao.updateBusiness(business)
    }

    suspend fun deleteBusiness(id: Long) {
        dao.deleteBusinessById(id)
    }
}
