package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    @Query("SELECT * FROM gym_members WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getMembersByBusiness(businessId: Long): Flow<List<GymMemberEntity>>

    @Query("SELECT * FROM gym_members WHERE id = :id LIMIT 1")
    fun getMemberById(id: Long): Flow<GymMemberEntity?>

    @Query("SELECT * FROM gym_members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdDirect(id: Long): GymMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GymMemberEntity): Long

    @Update
    suspend fun updateMember(member: GymMemberEntity)

    @Delete
    suspend fun deleteMember(member: GymMemberEntity)

    @Query("DELETE FROM gym_members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    @Query("UPDATE gym_members SET isCheckedIn = :isCheckedIn, lastCheckInTime = :checkInTime WHERE id = :id")
    suspend fun updateCheckInStatus(id: Long, isCheckedIn: Boolean, checkInTime: Long?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: GymCheckInEntity): Long

    @Update
    suspend fun updateCheckIn(checkIn: GymCheckInEntity)

    @Query("SELECT * FROM gym_checkins WHERE businessId = :businessId AND dateString = :dateString ORDER BY checkInTime DESC")
    fun getTodayCheckIns(businessId: Long, dateString: String): Flow<List<GymCheckInEntity>>

    @Query("SELECT * FROM gym_checkins WHERE memberId = :memberId ORDER BY checkInTime DESC")
    fun getCheckInsForMember(memberId: Long): Flow<List<GymCheckInEntity>>

    @Query("SELECT * FROM gym_checkins WHERE memberId = :memberId AND dateString = :dateString AND checkOutTime IS NULL ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getOpenCheckIn(memberId: Long, dateString: String): GymCheckInEntity?

    @Query("SELECT COUNT(*) FROM gym_members WHERE businessId = :businessId")
    suspend fun getMemberCount(businessId: Long): Int

    // Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: GymPaymentEntity): Long

    @Query("SELECT * FROM gym_payments WHERE businessId = :businessId ORDER BY paymentDate DESC")
    fun getPayments(businessId: Long): Flow<List<GymPaymentEntity>>

    @Query("SELECT * FROM gym_payments WHERE memberId = :memberId ORDER BY paymentDate DESC")
    fun getPaymentsForMember(memberId: Long): Flow<List<GymPaymentEntity>>

    // Lockers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocker(locker: GymLockerEntity): Long

    @Update
    suspend fun updateLocker(locker: GymLockerEntity)

    @Query("SELECT * FROM gym_lockers WHERE businessId = :businessId ORDER BY lockerNumber ASC")
    fun getLockers(businessId: Long): Flow<List<GymLockerEntity>>

    @Query("DELETE FROM gym_lockers WHERE id = :id")
    suspend fun deleteLocker(id: Long)
}
