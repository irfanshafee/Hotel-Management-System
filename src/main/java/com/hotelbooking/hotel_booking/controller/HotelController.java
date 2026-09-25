package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.HotelResponse;
import com.hotelbooking.hotel_booking.dto.RoomResponse;
import com.hotelbooking.hotel_booking.enums.RoomCategory;
import com.hotelbooking.hotel_booking.service.HotelService;
import com.hotelbooking.hotel_booking.service.RoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService hotelService;
    private final RoomService roomService;

    public HotelController(HotelService hotelService, RoomService roomService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @GetMapping
    ApiResponse<List<HotelResponse>> getAllHotels() {
        return new ApiResponse<>(HttpStatus.OK.value(), "Hotels retrieved successfully",
                hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}")
    ApiResponse<HotelResponse> getHotel(@PathVariable Long hotelId) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Hotel retrieved successfully",
                hotelService.getHotelById(hotelId));
    }

    @GetMapping("/search")
    ApiResponse<List<HotelResponse>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Hotels retrieved successfully",
                hotelService.searchHotels(city, name));
    }

    @GetMapping("/{hotelId}/rooms")
    ApiResponse<List<RoomResponse>> getHotelRooms(
            @PathVariable Long hotelId,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) RoomCategory category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Rooms retrieved successfully",
                roomService.getRoomsByHotel(hotelId, capacity, category, minPrice, maxPrice));
    }

    @GetMapping("/{hotelId}/rooms/available")
    ApiResponse<List<RoomResponse>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) RoomCategory category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                "Available rooms retrieved successfully",
                roomService.getAvailableRooms(
                        hotelId, checkIn, checkOut, capacity, category, minPrice, maxPrice));
    }
}
