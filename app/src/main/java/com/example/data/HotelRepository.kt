package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HotelRepository(private val hotelDao: HotelDao) {

    fun getRooms(businessId: Long): Flow<List<HotelRoomEntity>> =
        hotelDao.getRooms(businessId)

    suspend fun saveRoom(room: HotelRoomEntity): Long {
        return if (room.id == 0L) {
            hotelDao.insertRoom(room)
        } else {
            hotelDao.updateRoom(room)
            room.id
        }
    }

    suspend fun deleteRoom(id: Long) = hotelDao.deleteRoom(id)

    suspend fun markRoomCleaned(roomId: Long) = hotelDao.markRoomCleaned(roomId)

    suspend fun updateRoomStatus(roomId: Long, status: String, bookingId: Long? = null) =
        hotelDao.updateRoomStatus(roomId, status, bookingId)

    fun getBookings(businessId: Long): Flow<List<HotelBookingEntity>> =
        hotelDao.getBookings(businessId)

    suspend fun saveBooking(booking: HotelBookingEntity): Long {
        val bookingId = if (booking.id == 0L) {
            hotelDao.insertBooking(booking)
        } else {
            hotelDao.updateBooking(booking)
            booking.id
        }

        // Keep or update guest registry
        updateGuestRegistry(booking)

        // If booking is confirmed or checked in, update room status
        if (booking.status == "CHECKED_IN") {
            hotelDao.updateRoomStatus(booking.roomId, "BOOKED", bookingId)
        } else if (booking.status == "CHECKED_OUT") {
            hotelDao.updateRoomStatus(booking.roomId, "CLEANING", null)
        }
        return bookingId
    }

    suspend fun checkInGuest(bookingId: Long, roomId: Long) {
        hotelDao.checkInGuest(bookingId, roomId)
    }

    suspend fun checkOutGuest(bookingId: Long, roomId: Long) {
        hotelDao.checkOutGuest(bookingId, roomId)
    }

    suspend fun cancelBooking(bookingId: Long, roomId: Long) {
        hotelDao.cancelBooking(bookingId, roomId)
    }

    suspend fun deleteBooking(id: Long) = hotelDao.deleteBooking(id)

    fun getGuests(businessId: Long): Flow<List<HotelGuestEntity>> =
        hotelDao.getGuests(businessId)

    suspend fun saveGuest(guest: HotelGuestEntity): Long {
        return if (guest.id == 0L) {
            hotelDao.insertGuest(guest)
        } else {
            hotelDao.updateGuest(guest)
            guest.id
        }
    }

    private suspend fun updateGuestRegistry(booking: HotelBookingEntity) {
        val existingGuest = hotelDao.getGuestByCnic(booking.businessId, booking.guestCnic)
        if (existingGuest != null) {
            hotelDao.updateGuest(
                existingGuest.copy(
                    name = booking.guestName,
                    phone = booking.guestPhone,
                    city = booking.guestCity,
                    totalStays = existingGuest.totalStays + 1,
                    totalSpent = existingGuest.totalSpent + booking.totalAmount,
                    lastStayDate = booking.checkInDate
                )
            )
        } else {
            hotelDao.insertGuest(
                HotelGuestEntity(
                    businessId = booking.businessId,
                    name = booking.guestName,
                    phone = booking.guestPhone,
                    cnic = booking.guestCnic,
                    city = booking.guestCity,
                    totalStays = 1,
                    totalSpent = booking.totalAmount,
                    lastStayDate = booking.checkInDate
                )
            )
        }
    }

    suspend fun seedSampleHotelDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake hotel entries
        val roomCount = hotelDao.getRoomCount(businessId)
        if (roomCount > 0) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 2)
        val twoDaysLaterStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = sdf.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        // Seed Rooms
        val room101Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "101",
                roomType = "Deluxe Double",
                floor = "1st Floor",
                pricePerNight = 8500.0,
                status = "AVAILABLE",
                amenities = "AC, King Bed, Free Wi-Fi, Smart TV, Hot Water Geyser"
            )
        )

        val room102Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "102",
                roomType = "Executive Suite",
                floor = "1st Floor",
                pricePerNight = 14000.0,
                status = "BOOKED",
                amenities = "AC, Mini-Bar, Balcony, Safe, Free Breakfast, Wi-Fi"
            )
        )

        val room103Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "103",
                roomType = "Standard Single",
                floor = "1st Floor",
                pricePerNight = 5500.0,
                status = "CLEANING",
                amenities = "AC, Single Bed, Wi-Fi, Hot Shower, Tea Kettle"
            )
        )

        val room201Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "201",
                roomType = "Family Suite",
                floor = "2nd Floor",
                pricePerNight = 18000.0,
                status = "AVAILABLE",
                amenities = "2 Bedrooms, Lounge, Refrigerator, Microwave, 2 Smart TVs"
            )
        )

        val room202Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "202",
                roomType = "Deluxe Twin",
                floor = "2nd Floor",
                pricePerNight = 9000.0,
                status = "AVAILABLE",
                amenities = "Twin Beds, Mountain View, AC, Wi-Fi, Room Service"
            )
        )

        val room203Id = hotelDao.insertRoom(
            HotelRoomEntity(
                businessId = businessId,
                roomNumber = "203",
                roomType = "Standard Double",
                floor = "2nd Floor",
                pricePerNight = 7000.0,
                status = "BOOKED",
                amenities = "AC, Double Bed, Wi-Fi, Work Desk, Intercom"
            )
        )

        // Seed Bookings
        val booking1Id = hotelDao.insertBooking(
            HotelBookingEntity(
                businessId = businessId,
                roomId = room102Id,
                roomNumber = "102",
                roomType = "Executive Suite",
                guestName = "Tariq Mehmood",
                guestPhone = "+92 300 4589211",
                guestCnic = "35201-8974512-3",
                guestCity = "Lahore",
                checkInDate = todayStr,
                checkOutDate = twoDaysLaterStr,
                numberOfNights = 2,
                pricePerNight = 14000.0,
                totalAmount = 28000.0,
                advancePaid = 14000.0,
                remainingAmount = 14000.0,
                status = "CHECKED_IN",
                specialRequests = "High floor, late check-out requested"
            )
        )
        hotelDao.updateRoomStatus(room102Id, "BOOKED", booking1Id)

        val booking2Id = hotelDao.insertBooking(
            HotelBookingEntity(
                businessId = businessId,
                roomId = room203Id,
                roomNumber = "203",
                roomType = "Standard Double",
                guestName = "Dr. Asma Bilal",
                guestPhone = "+92 321 8765432",
                guestCnic = "42101-5678912-4",
                guestCity = "Karachi",
                checkInDate = todayStr,
                checkOutDate = tomorrowStr,
                numberOfNights = 1,
                pricePerNight = 7000.0,
                totalAmount = 7000.0,
                advancePaid = 7000.0,
                remainingAmount = 0.0,
                status = "CONFIRMED",
                specialRequests = "Arriving at 06:00 PM via flight"
            )
        )
        hotelDao.updateRoomStatus(room203Id, "BOOKED", booking2Id)

        hotelDao.insertBooking(
            HotelBookingEntity(
                businessId = businessId,
                roomId = room103Id,
                roomNumber = "103",
                roomType = "Standard Single",
                guestName = "Kamran Abbasi",
                guestPhone = "+92 333 1122334",
                guestCnic = "61101-1234987-5",
                guestCity = "Islamabad",
                checkInDate = yesterdayStr,
                checkOutDate = todayStr,
                numberOfNights = 1,
                pricePerNight = 5500.0,
                totalAmount = 5500.0,
                advancePaid = 5500.0,
                remainingAmount = 0.0,
                status = "CHECKED_OUT",
                specialRequests = "Key returned at 11:30 AM"
            )
        )

        // Seed Guests Directory
        hotelDao.insertGuest(
            HotelGuestEntity(
                businessId = businessId,
                name = "Tariq Mehmood",
                phone = "+92 300 4589211",
                cnic = "35201-8974512-3",
                city = "Lahore",
                totalStays = 3,
                totalSpent = 64000.0,
                lastStayDate = todayStr
            )
        )

        hotelDao.insertGuest(
            HotelGuestEntity(
                businessId = businessId,
                name = "Dr. Asma Bilal",
                phone = "+92 321 8765432",
                cnic = "42101-5678912-4",
                city = "Karachi",
                totalStays = 2,
                totalSpent = 22000.0,
                lastStayDate = todayStr
            )
        )

        hotelDao.insertGuest(
            HotelGuestEntity(
                businessId = businessId,
                name = "Kamran Abbasi",
                phone = "+92 333 1122334",
                cnic = "61101-1234987-5",
                city = "Islamabad",
                totalStays = 1,
                totalSpent = 5500.0,
                lastStayDate = yesterdayStr
            )
        )
    }
}
