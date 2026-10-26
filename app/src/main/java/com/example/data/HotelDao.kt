package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {
    // --- Room Operations ---
    @Query("SELECT * FROM hotel_rooms WHERE businessId = :businessId ORDER BY roomNumber ASC")
    fun getRooms(businessId: Long): Flow<List<HotelRoomEntity>>

    @Query("SELECT * FROM hotel_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: Long): HotelRoomEntity?

    @Query("SELECT COUNT(*) FROM hotel_rooms WHERE businessId = :businessId")
    suspend fun getRoomCount(businessId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: HotelRoomEntity): Long

    @Update
    suspend fun updateRoom(room: HotelRoomEntity)

    @Query("UPDATE hotel_rooms SET status = :status, currentBookingId = :bookingId WHERE id = :roomId")
    suspend fun updateRoomStatus(roomId: Long, status: String, bookingId: Long?)

    @Query("DELETE FROM hotel_rooms WHERE id = :id")
    suspend fun deleteRoom(id: Long)

    // --- Booking Operations ---
    @Query("SELECT * FROM hotel_bookings WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getBookings(businessId: Long): Flow<List<HotelBookingEntity>>

    @Query("SELECT * FROM hotel_bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): HotelBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: HotelBookingEntity): Long

    @Update
    suspend fun updateBooking(booking: HotelBookingEntity)

    @Query("UPDATE hotel_bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: String)

    @Query("DELETE FROM hotel_bookings WHERE id = :id")
    suspend fun deleteBooking(id: Long)

    // --- Guest Operations ---
    @Query("SELECT * FROM hotel_guests WHERE businessId = :businessId ORDER BY totalStays DESC")
    fun getGuests(businessId: Long): Flow<List<HotelGuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: HotelGuestEntity): Long

    @Query("SELECT * FROM hotel_guests WHERE businessId = :businessId AND cnic = :cnic LIMIT 1")
    suspend fun getGuestByCnic(businessId: Long, cnic: String): HotelGuestEntity?

    @Update
    suspend fun updateGuest(guest: HotelGuestEntity)

    // --- Hotel Workflow Transactions ---
    @Transaction
    suspend fun checkInGuest(bookingId: Long, roomId: Long) {
        updateBookingStatus(bookingId, "CHECKED_IN")
        updateRoomStatus(roomId, "BOOKED", bookingId)
    }

    @Transaction
    suspend fun checkOutGuest(bookingId: Long, roomId: Long) {
        updateBookingStatus(bookingId, "CHECKED_OUT")
        updateRoomStatus(roomId, "CLEANING", null)
    }

    @Transaction
    suspend fun markRoomCleaned(roomId: Long) {
        updateRoomStatus(roomId, "AVAILABLE", null)
    }

    @Transaction
    suspend fun cancelBooking(bookingId: Long, roomId: Long) {
        updateBookingStatus(bookingId, "CANCELLED")
        val room = getRoomById(roomId)
        if (room != null && room.currentBookingId == bookingId) {
            updateRoomStatus(roomId, "AVAILABLE", null)
        }
    }
}
