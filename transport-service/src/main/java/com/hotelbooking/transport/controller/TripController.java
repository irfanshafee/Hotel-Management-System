package com.hotelbooking.transport.controller;

import com.hotelbooking.transport.dto.request.TripSearchRequest;
import com.hotelbooking.transport.dto.response.ApiResponse;
import com.hotelbooking.transport.dto.response.TripResponse;
import com.hotelbooking.transport.dto.response.TripSeatAvailabilityResponse;
import com.hotelbooking.transport.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    ApiResponse<List<TripResponse>> searchTrips(
            @Valid @ModelAttribute TripSearchRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Trips retrieved",
                tripService.searchTrips(request));
    }

    @GetMapping("/{tripId}/seats")
    ApiResponse<TripSeatAvailabilityResponse> getSeatAvailability(
            @PathVariable Long tripId) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Seat availability retrieved",
                tripService.getSeatAvailability(tripId));
    }
}
