package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BusinessEntity::class,
        PartyEntity::class,
        InventoryItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ExpenseEntity::class,
        StaffEntity::class,
        StaffAttendanceEntity::class,
        GymMemberEntity::class,
        GymCheckInEntity::class,
        GymPaymentEntity::class,
        GymLockerEntity::class,
        MembershipEntity::class,
        PatientEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class,
        PrescriptionEntity::class,
        MedicineEntity::class,
        PharmacySaleEntity::class,
        RestaurantTableEntity::class,
        RestaurantMenuItemEntity::class,
        RestaurantOrderEntity::class,
        RestaurantOrderItemEntity::class,
        StudentEntity::class,
        SchoolClassEntity::class,
        FeeVoucherEntity::class,
        StudentAttendanceEntity::class,
        StudentFeeEntity::class,
        SchoolStaffEntity::class,
        ExamResultEntity::class,
        PropertyEntity::class,
        LeadEntity::class,
        SiteVisitEntity::class,
        SalonServiceEntity::class,
        StylistEntity::class,
        SalonAppointmentEntity::class,
        HotelRoomEntity::class,
        HotelBookingEntity::class,
        HotelGuestEntity::class,
        TailorCustomerEntity::class,
        TailorMeasurementEntity::class,
        TailorOrderEntity::class,
        BakeryItemEntity::class,
        BakeryCakeOrderEntity::class,
        ElectronicsProductEntity::class,
        RepairTicketEntity::class,
        RepairJobEntity::class,
        WorkshopVehicleEntity::class,
        WorkshopMechanicEntity::class,
        WorkshopJobCardEntity::class,
        LaundryCustomerEntity::class,
        LaundryOrderEntity::class,
        WholesalePartyEntity::class,
        WholesaleBulkOrderEntity::class,
        WholesalePaymentEntity::class,
        ProductEntity::class,
        CustomerEntity::class
    ],
    version = 26,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun businessDao(): BusinessDao
    abstract fun partyDao(): PartyDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun staffDao(): StaffDao
    abstract fun gymDao(): GymDao
    abstract fun membershipDao(): MembershipDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun patientDao(): PatientDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun schoolDao(): SchoolDao
    abstract fun realEstateDao(): RealEstateDao
    abstract fun salonDao(): SalonDao
    abstract fun hotelDao(): HotelDao
    abstract fun tailorDao(): TailorDao
    abstract fun bakeryDao(): BakeryDao
    abstract fun electronicsDao(): ElectronicsDao
    abstract fun workshopDao(): WorkshopDao
    abstract fun laundryDao(): LaundryDao
    abstract fun wholesaleDao(): WholesaleDao
    abstract fun clinicDao(): ClinicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pakbusiness_pro.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabaseInstance() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {}
                INSTANCE = null
            }
        }
    }
}
