package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.RoomResponse;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
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
    private final ApiSuccessMessageCatalog successMessages;

    public RoomController(RoomService roomService, ApiSuccessMessageCatalog successMessages) {
        this.roomService = roomService;
        this.successMessages = successMessages;
    }

    @GetMapping("/{roomId}")
    ApiResponse<RoomResponse> getRoom(@PathVariable Long roomId) {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("room.retrieved"),
                roomService.getRoomById(roomId));
    }
}
