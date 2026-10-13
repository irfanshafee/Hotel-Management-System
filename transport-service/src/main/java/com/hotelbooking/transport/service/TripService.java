package com.hotelbooking.transport.service;

import com.hotelbooking.transport.dto.request.TripSearchRequest;
import com.hotelbooking.transport.dto.response.SeatResponse;
import com.hotelbooking.transport.dto.response.TripResponse;
import com.hotelbooking.transport.dto.response.TripSeatAvailabilityResponse;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
public class TripService {
    private static final ZoneId SERVICE_TIME_ZONE = ZoneId.of("Asia/Dhaka");

    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;

    public TripService(TripRepository tripRepository, SeatRepository seatRepository) {
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional(readOnly = true)
    public List<TripResponse> searchTrips(TripSearchRequest request) {
        var dayStart = request.date().atStartOfDay(SERVICE_TIME_ZONE).toOffsetDateTime();
        var nextDayStart = request.date().plusDays(1)
                .atStartOfDay(SERVICE_TIME_ZONE).toOffsetDateTime();

        return tripRepository.searchScheduledTrips(
                        request.origin().trim(), request.destination().trim(),
                        dayStart, nextDayStart, TripStatus.SCHEDULED)
                .stream()
                .map(this::toTripResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripSeatAvailabilityResponse getSeatAvailability(Long tripId) {
        Trip trip = tripRepository.findDetailedById(tripId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "Trip not found with id: " + tripId));
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Seats are not available for a trip in status " + trip.getStatus());
        }

        Vehicle vehicle = trip.getVehicle();
        List<SeatResponse> seats = seatRepository.findAvailabilityForTrip(
                        trip.getId(), vehicle.getId())
                .stream()
                .map(seat -> new SeatResponse(
                        seat.getSeatId(), seat.getSeatNumber(),
                        seat.getAvailable(), trip.getFare()))
                .toList();

        return new TripSeatAvailabilityResponse(
                trip.getId(), vehicle.getId(), vehicle.getBusNumber(),
                vehicle.getName(), seats);
    }

    private TripResponse toTripResponse(Trip trip) {
        Route route = trip.getRoute();
        Vehicle vehicle = trip.getVehicle();
        return new TripResponse(
                trip.getId(), route.getId(), route.getOrigin(), route.getDestination(),
                vehicle.getId(), vehicle.getBusNumber(), vehicle.getName(),
                vehicle.getTotalSeats(), trip.getDepartureTime(), trip.getArrivalTime(),
                trip.getFare(), trip.getStatus());
    }
}
