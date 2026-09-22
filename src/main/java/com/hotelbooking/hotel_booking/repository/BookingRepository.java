package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(BookingStatus status);
}
