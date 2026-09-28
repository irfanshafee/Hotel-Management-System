package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.BookingResponse;
import com.hotelbooking.hotel_booking.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.service.ApiSuccessMessageCatalog;
import com.hotelbooking.hotel_booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ApiSuccessMessageCatalog successMessages;

    public BookingController(
            BookingService bookingService, ApiSuccessMessageCatalog successMessages) {
        this.bookingService = bookingService;
        this.successMessages = successMessages;
    }

    @PostMapping
    ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(),
                        successMessages.get("booking.created"), response));
    }

    @GetMapping("/my")
    ApiResponse<List<BookingResponse>> getMyBookings() {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("bookings.retrieved"),
                bookingService.getMyBookings());
    }

    @GetMapping("/{bookingId}")
    ApiResponse<BookingResponse> getBooking(
            @PathVariable Long bookingId) {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("booking.retrieved"),
                bookingService.getMyBooking(bookingId));
    }

    @PatchMapping("/{bookingId}/cancel")
    ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long bookingId) {
        return new ApiResponse<>(HttpStatus.OK.value(), successMessages.get("booking.cancelled"),
                bookingService.cancelBooking(bookingId));
    }
}
