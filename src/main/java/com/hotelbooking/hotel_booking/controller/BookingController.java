package com.hotelbooking.hotel_booking.controller;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import com.hotelbooking.hotel_booking.dto.BookingResponse;
import com.hotelbooking.hotel_booking.dto.CreateBookingRequest;
import com.hotelbooking.hotel_booking.exception.InvalidCredentialsException;
import com.hotelbooking.hotel_booking.security.AuthenticatedUser;
import com.hotelbooking.hotel_booking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
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

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            HttpServletRequest httpRequest) {
        AuthenticatedUser user = authenticatedUser(httpRequest);
        BookingResponse response = bookingService.createBooking(user.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Booking created successfully", response));
    }

    @GetMapping("/my")
    ApiResponse<List<BookingResponse>> getMyBookings(HttpServletRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Bookings retrieved successfully",
                bookingService.getMyBookings(authenticatedUser(request).id()));
    }

    @GetMapping("/{bookingId}")
    ApiResponse<BookingResponse> getBooking(
            @PathVariable Long bookingId, HttpServletRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Booking retrieved successfully",
                bookingService.getMyBooking(authenticatedUser(request).id(), bookingId));
    }

    @PatchMapping("/{bookingId}/cancel")
    ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long bookingId, HttpServletRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Booking cancelled successfully",
                bookingService.cancelBooking(authenticatedUser(request).id(), bookingId));
    }

    private AuthenticatedUser authenticatedUser(HttpServletRequest request) {
        Object user = request.getAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE);
        if (user instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        throw new InvalidCredentialsException();
    }
}
