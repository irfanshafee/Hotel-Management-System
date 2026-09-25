package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    List<Room> findByCapacity(Integer capacity);

    List<Room> findByCategory(RoomCategory category);

    @Query("""
            select room from Room room
            where room.hotel.id = :hotelId
              and (:capacity is null or room.capacity = :capacity)
              and (:category is null or room.category = :category)
              and (:minPrice is null or room.price >= :minPrice)
              and (:maxPrice is null or room.price <= :maxPrice)
            order by room.roomNumber
            """)
    List<Room> findByHotelAndFilters(
            @Param("hotelId") Long hotelId,
            @Param("capacity") Integer capacity,
            @Param("category") RoomCategory category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            select room from Room room
            where room.hotel.id = :hotelId
              and (:capacity is null or room.capacity = :capacity)
              and (:category is null or room.category = :category)
              and (:minPrice is null or room.price >= :minPrice)
              and (:maxPrice is null or room.price <= :maxPrice)
              and not exists (
                  select booking.id from Booking booking
                  where booking.room = room
                    and booking.status in :blockingStatuses
                    and booking.startDate < :checkOut
                    and booking.endDate > :checkIn
              )
            order by room.roomNumber
            """)
    List<Room> findAvailableByHotelAndFilters(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("blockingStatuses") List<BookingStatus> blockingStatuses,
            @Param("capacity") Integer capacity,
            @Param("category") RoomCategory category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
