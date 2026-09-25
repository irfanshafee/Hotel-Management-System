package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.RoomResponse;
import com.hotelbooking.hotel_booking.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{roomId}")
    ApiResponse<RoomResponse> getRoom(@PathVariable Long roomId) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Room retrieved successfully",
                roomService.getRoomById(roomId));
    }
}
