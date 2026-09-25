package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(BookingStatus status);

    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);

    @Query("""
            select count(booking) from Booking booking
            where booking.room.id = :roomId
              and booking.status in :blockingStatuses
              and booking.startDate < :checkOut
              and booking.endDate > :checkIn
            """)
    long countOverlappingActiveBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("blockingStatuses") List<BookingStatus> blockingStatuses);
}
