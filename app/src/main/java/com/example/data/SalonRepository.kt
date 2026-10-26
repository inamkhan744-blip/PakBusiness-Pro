package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalonRepository(private val salonDao: SalonDao) {

    // --- Services ---
    fun getServices(businessId: Long): Flow<List<SalonServiceEntity>> =
        salonDao.getServices(businessId)

    fun getServicesByCategory(businessId: Long, category: String): Flow<List<SalonServiceEntity>> =
        salonDao.getServicesByCategory(businessId, category)

    suspend fun saveService(service: SalonServiceEntity): Long {
        return if (service.id == 0L) {
            salonDao.insertService(service)
        } else {
            salonDao.updateService(service)
            service.id
        }
    }

    suspend fun deleteService(id: Long) = salonDao.deleteService(id)

    // --- Stylists ---
    fun getStylists(businessId: Long): Flow<List<StylistEntity>> =
        salonDao.getStylists(businessId)

    suspend fun saveStylist(stylist: StylistEntity): Long {
        return if (stylist.id == 0L) {
            salonDao.insertStylist(stylist)
        } else {
            salonDao.updateStylist(stylist)
            stylist.id
        }
    }

    suspend fun deleteStylist(id: Long) = salonDao.deleteStylist(id)

    // --- Appointments ---
    fun getAppointments(businessId: Long): Flow<List<SalonAppointmentEntity>> =
        salonDao.getAppointments(businessId)

    fun getAppointmentsByDate(businessId: Long, date: String): Flow<List<SalonAppointmentEntity>> =
        salonDao.getAppointmentsByDate(businessId, date)

    fun getAppointmentsByStatus(businessId: Long, status: String): Flow<List<SalonAppointmentEntity>> =
        salonDao.getAppointmentsByStatus(businessId, status)

    suspend fun saveAppointment(appointment: SalonAppointmentEntity): Long {
        return if (appointment.id == 0L) {
            salonDao.insertAppointment(appointment)
        } else {
            salonDao.updateAppointment(appointment)
            appointment.id
        }
    }

    suspend fun updateAppointmentStatus(
        id: Long,
        status: String,
        paymentStatus: String = "PAID",
        stylistId: Long? = null,
        commissionAmount: Double = 0.0
    ) {
        val existing = salonDao.getAppointmentByIdDirect(id)
        val wasAlreadyCompleted = existing?.status == "COMPLETED"
        salonDao.updateAppointmentStatus(id, status, paymentStatus)

        // If newly completed, award commission to stylist
        if (status == "COMPLETED" && !wasAlreadyCompleted) {
            val targetStylistId = stylistId ?: existing?.stylistId
            val targetCommission = if (commissionAmount > 0) commissionAmount else (existing?.commissionAmount ?: 0.0)
            if (targetStylistId != null && targetStylistId > 0L && targetCommission > 0.0) {
                salonDao.addCommissionToStylist(targetStylistId, targetCommission)
            }
        }
    }

    suspend fun deleteAppointment(id: Long) = salonDao.deleteAppointment(id)

    // --- Initial Sample Data Seeding ---
    suspend fun seedSampleSalonDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake salon entries
        val servicesCount = salonDao.getServicesCount(businessId)
        if (servicesCount > 0) return

        // 1. Initial Services (Hair, Skin, Spa)
        val sampleServices = listOf(
            SalonServiceEntity(
                businessId = businessId,
                name = "Keratin Hair Smoothing & Gloss",
                category = "HAIR",
                price = 12500.0,
                durationMinutes = 120,
                description = "Deep nourishing keratin protein therapy with heat sealing and high gloss finish."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Signature Hair Cut & Blowdry",
                category = "HAIR",
                price = 2500.0,
                durationMinutes = 45,
                description = "Layered style cut, customized shampoo cleanse, and salon blowout."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Balayage & Hair Highlights",
                category = "HAIR",
                price = 15000.0,
                durationMinutes = 150,
                description = "Hand-painted caramel / blonde dimension tones with ammonia-free toner."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "HydraFacial Deep Glow Treatment",
                category = "SKIN",
                price = 7500.0,
                durationMinutes = 60,
                description = "Vortex cleansing, dead-skin extraction, and hyaluronic acid hydration."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Bridal Skin Brightening Polish",
                category = "SKIN",
                price = 5500.0,
                durationMinutes = 50,
                description = "Herbal fruit scrub with vitamin C serum and 24K gold peel-off mask."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Acne Clarifying Derma Care",
                category = "SKIN",
                price = 4800.0,
                durationMinutes = 45,
                description = "Salicylic acid purification, pore vacuum, and blue LED light therapy."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Aromatherapy Full Body Massage",
                category = "SPA",
                price = 8500.0,
                durationMinutes = 75,
                description = "Lavender and eucalyptus essential oil therapeutic relaxing massage."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Moroccan Bath & Body Scrub",
                category = "SPA",
                price = 9500.0,
                durationMinutes = 80,
                description = "Traditional steam bath, black soap kessa exfoliation, and rose water wrap."
            ),
            SalonServiceEntity(
                businessId = businessId,
                name = "Deluxe Manicure & Pedicure Spa",
                category = "SPA",
                price = 3800.0,
                durationMinutes = 60,
                description = "Dead sea salt foot soak, paraffin wax softening, cuticle care and nail polish."
            )
        )
        salonDao.insertServices(sampleServices)

        // 2. Initial Stylists with Commission Tracking
        val sampleStylists = listOf(
            StylistEntity(
                businessId = businessId,
                name = "Ayesha Malik",
                phone = "+92 300 8472910",
                specialty = "Hair Specialist & Colorist",
                commissionPercentage = 25.0,
                totalEarnedCommission = 34500.0,
                totalServicesCompleted = 14,
                isAvailable = true
            ),
            StylistEntity(
                businessId = businessId,
                name = "Zubair Khan",
                phone = "+92 321 5592813",
                specialty = "Master Hair Stylist & Cuts",
                commissionPercentage = 20.0,
                totalEarnedCommission = 21000.0,
                totalServicesCompleted = 19,
                isAvailable = true
            ),
            StylistEntity(
                businessId = businessId,
                name = "Sara Ahmed",
                phone = "+92 333 4910284",
                specialty = "Skin & Facial Expert",
                commissionPercentage = 25.0,
                totalEarnedCommission = 28750.0,
                totalServicesCompleted = 11,
                isAvailable = true
            ),
            StylistEntity(
                businessId = businessId,
                name = "Farzana Bibi",
                phone = "+92 345 7712903",
                specialty = "Spa & Massage Therapist",
                commissionPercentage = 30.0,
                totalEarnedCommission = 38250.0,
                totalServicesCompleted = 15,
                isAvailable = true
            )
        )
        salonDao.insertStylists(sampleStylists)

        // Query inserted stylists to get valid IDs for sample appointments
        val insertedStylists = sampleStylists
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val sampleAppointments = listOf(
            SalonAppointmentEntity(
                businessId = businessId,
                clientName = "Fatima Noor",
                clientPhone = "+92 301 2345678",
                serviceId = 1L,
                serviceName = "HydraFacial Deep Glow Treatment",
                serviceCategory = "SKIN",
                stylistId = 3L,
                stylistName = "Sara Ahmed",
                appointmentDate = todayStr,
                appointmentTime = "11:30 AM",
                status = "COMPLETED",
                servicePrice = 7500.0,
                discount = 500.0,
                finalPrice = 7000.0,
                commissionPercentage = 25.0,
                commissionAmount = 1750.0,
                paymentStatus = "PAID",
                notes = "Client requested extra attention on T-zone"
            ),
            SalonAppointmentEntity(
                businessId = businessId,
                clientName = "Zainab Tariq",
                clientPhone = "+92 322 9876543",
                serviceId = 2L,
                serviceName = "Keratin Hair Smoothing & Gloss",
                serviceCategory = "HAIR",
                stylistId = 1L,
                stylistName = "Ayesha Malik",
                appointmentDate = todayStr,
                appointmentTime = "02:00 PM",
                status = "IN_PROGRESS",
                servicePrice = 12500.0,
                discount = 0.0,
                finalPrice = 12500.0,
                commissionPercentage = 25.0,
                commissionAmount = 3125.0,
                paymentStatus = "PAID",
                notes = "Pre-bridal package discount eligible"
            ),
            SalonAppointmentEntity(
                businessId = businessId,
                clientName = "Mariam Aslam",
                clientPhone = "+92 334 5511223",
                serviceId = 3L,
                serviceName = "Aromatherapy Full Body Massage",
                serviceCategory = "SPA",
                stylistId = 4L,
                stylistName = "Farzana Bibi",
                appointmentDate = todayStr,
                appointmentTime = "04:30 PM",
                status = "SCHEDULED",
                servicePrice = 8500.0,
                discount = 500.0,
                finalPrice = 8000.0,
                commissionPercentage = 30.0,
                commissionAmount = 2400.0,
                paymentStatus = "PENDING",
                notes = "Prefers lavender aroma oils"
            )
        )
        salonDao.insertAppointments(sampleAppointments)
    }
}
