package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    List<Room> findByCapacity(Integer capacity);

    List<Room> findByCategory(RoomCategory category);
}
