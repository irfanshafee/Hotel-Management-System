package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.RoomResponse;
import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import com.hotelbooking.hotel_booking.exception.HotelNotFoundException;
import com.hotelbooking.hotel_booking.exception.InvalidFilterException;
import com.hotelbooking.hotel_booking.exception.RoomNotFoundException;
import com.hotelbooking.hotel_booking.repository.HotelRepository;
import com.hotelbooking.hotel_booking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<RoomResponse> getRoomsByHotel(
            Long hotelId, Integer capacity, RoomCategory category,
            BigDecimal minPrice, BigDecimal maxPrice) {
        validateFilters(capacity, minPrice, maxPrice);
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
        return roomRepository.findByHotelAndFilters(
                        hotelId, capacity, category, minPrice, maxPrice)
                .stream().map(this::toResponse).toList();
    }

    public RoomResponse getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .map(this::toResponse)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    public List<RoomResponse> getAvailableRooms(
            Long hotelId, LocalDate checkIn, LocalDate checkOut,
            Integer capacity, RoomCategory category,
            BigDecimal minPrice, BigDecimal maxPrice) {
        DateRangeValidator.validate(checkIn, checkOut);
        validateFilters(capacity, minPrice, maxPrice);
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }

        return roomRepository.findAvailableByHotelAndFilters(
                        hotelId, checkIn, checkOut,
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                        capacity, category, minPrice, maxPrice)
                .stream().map(this::toResponse).toList();
    }

    private void validateFilters(Integer capacity, BigDecimal minPrice, BigDecimal maxPrice) {
        if (capacity != null && capacity <= 0) {
            throw new InvalidFilterException("Capacity must be positive");
        }
        if (minPrice != null && minPrice.signum() < 0) {
            throw new InvalidFilterException("Minimum price must not be negative");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new InvalidFilterException("Maximum price must not be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidFilterException("Minimum price must not exceed maximum price");
        }
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(), room.getRoomNumber(), room.getCategory(),
                room.getCapacity(), room.getPrice(), room.getHotel().getId(),
                room.getHotel().getName());
    }
}
