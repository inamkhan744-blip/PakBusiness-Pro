package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GymRepository(private val gymDao: GymDao) {

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getMembers(businessId: Long): Flow<List<GymMemberEntity>> =
        gymDao.getMembersByBusiness(businessId)

    fun getMemberById(id: Long): Flow<GymMemberEntity?> =
        gymDao.getMemberById(id)

    suspend fun getMemberByIdDirect(id: Long): GymMemberEntity? =
        gymDao.getMemberByIdDirect(id)

    suspend fun saveMember(member: GymMemberEntity): Long {
        return if (member.id == 0L) {
            gymDao.insertMember(member)
        } else {
            gymDao.updateMember(member)
            member.id
        }
    }

    suspend fun deleteMember(id: Long) {
        gymDao.deleteMemberById(id)
    }

    suspend fun toggleCheckIn(member: GymMemberEntity): Boolean {
        val now = System.currentTimeMillis()
        val todayStr = getTodayDateString()

        return if (!member.isCheckedIn) {
            // Check in
            gymDao.updateCheckInStatus(member.id, isCheckedIn = true, checkInTime = now)
            gymDao.insertCheckIn(
                GymCheckInEntity(
                    memberId = member.id,
                    memberName = member.name,
                    businessId = member.businessId,
                    checkInTime = now,
                    checkOutTime = null,
                    dateString = todayStr
                )
            )
            true
        } else {
            // Check out
            gymDao.updateCheckInStatus(member.id, isCheckedIn = false, checkInTime = member.lastCheckInTime)
            val openCheckIn = gymDao.getOpenCheckIn(member.id, todayStr)
            if (openCheckIn != null) {
                gymDao.updateCheckIn(openCheckIn.copy(checkOutTime = now))
            } else {
                // In case open check-in wasn't found from today, record checkout
                gymDao.insertCheckIn(
                    GymCheckInEntity(
                        memberId = member.id,
                        memberName = member.name,
                        businessId = member.businessId,
                        checkInTime = member.lastCheckInTime ?: now,
                        checkOutTime = now,
                        dateString = todayStr
                    )
                )
            }
            false
        }
    }

    fun getTodayCheckIns(businessId: Long): Flow<List<GymCheckInEntity>> {
        return gymDao.getTodayCheckIns(businessId, getTodayDateString())
    }

    fun getMemberCheckIns(memberId: Long): Flow<List<GymCheckInEntity>> {
        return gymDao.getCheckInsForMember(memberId)
    }

    // --- Payments ---
    fun getPayments(businessId: Long): Flow<List<GymPaymentEntity>> =
        gymDao.getPayments(businessId)

    suspend fun recordPayment(payment: GymPaymentEntity): Long =
        gymDao.insertPayment(payment)

    // --- Lockers ---
    fun getLockers(businessId: Long): Flow<List<GymLockerEntity>> =
        gymDao.getLockers(businessId)

    suspend fun saveLocker(locker: GymLockerEntity): Long {
        return if (locker.id == 0L) {
            gymDao.insertLocker(locker)
        } else {
            gymDao.updateLocker(locker)
            locker.id
        }
    }

    suspend fun deleteLocker(id: Long) =
        gymDao.deleteLocker(id)

    suspend fun ensureInitialGymData(businessId: Long) {
        // Disabled: user requested clean database without fake entries
    }
}
