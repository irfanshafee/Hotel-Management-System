package com.hotelbooking.transport.service;

import com.hotelbooking.transport.dto.request.TripSearchRequest;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.SeatAvailabilityView;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceTest {
    private TripRepository tripRepository;
    private SeatRepository seatRepository;
    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        seatRepository = mock(SeatRepository.class);
        tripService = new TripService(tripRepository, seatRepository);
    }

    @Test
    void searchUsesDhakaDayBoundariesAndMapsTrip() {
        LocalDate date = LocalDate.of(2027, 1, 1);
        OffsetDateTime departure = OffsetDateTime.parse("2027-01-01T06:00:00+06:00");
        OffsetDateTime arrival = OffsetDateTime.parse("2027-01-01T12:00:00+06:00");
        Vehicle vehicle = vehicle();
        Route route = route();
        Trip trip = mock(Trip.class);
        when(trip.getId()).thenReturn(10L);
        when(trip.getVehicle()).thenReturn(vehicle);
        when(trip.getRoute()).thenReturn(route);
        when(trip.getDepartureTime()).thenReturn(departure);
        when(trip.getArrivalTime()).thenReturn(arrival);
        when(trip.getFare()).thenReturn(new BigDecimal("1200.00"));
        when(trip.getStatus()).thenReturn(TripStatus.SCHEDULED);
        when(tripRepository.searchScheduledTrips(
                "Dhaka", "Chattogram",
                OffsetDateTime.parse("2027-01-01T00:00:00+06:00"),
                OffsetDateTime.parse("2027-01-02T00:00:00+06:00"),
                TripStatus.SCHEDULED)).thenReturn(List.of(trip));

        var result = tripService.searchTrips(
                new TripSearchRequest(" Dhaka ", " Chattogram ", date));

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().tripId());
        assertEquals("DHAKA-METRO-B-1001", result.getFirst().busNumber());
        assertEquals("Dhaka", result.getFirst().origin());
        assertEquals(new BigDecimal("1200.00"), result.getFirst().fare());
    }

    @Test
    void seatAvailabilityUsesTripAndVehicleAndReturnsFareAsPrice() {
        Trip trip = mock(Trip.class);
        Vehicle vehicle = vehicle();
        SeatAvailabilityView seat = mock(SeatAvailabilityView.class);
        when(trip.getId()).thenReturn(10L);
        when(trip.getVehicle()).thenReturn(vehicle);
        when(trip.getStatus()).thenReturn(TripStatus.SCHEDULED);
        when(trip.getFare()).thenReturn(new BigDecimal("1200.00"));
        when(seat.getSeatId()).thenReturn(5L);
        when(seat.getSeatNumber()).thenReturn("A1");
        when(seat.getAvailable()).thenReturn(false);
        when(tripRepository.findDetailedById(10L)).thenReturn(Optional.of(trip));
        when(seatRepository.findAvailabilityForTrip(10L, 1L))
                .thenReturn(List.of(seat));

        var result = tripService.getSeatAvailability(10L);

        verify(seatRepository).findAvailabilityForTrip(10L, 1L);
        assertEquals("DHAKA-METRO-B-1001", result.busNumber());
        assertEquals(1, result.seats().size());
        assertFalse(result.seats().getFirst().available());
        assertEquals(new BigDecimal("1200.00"), result.seats().getFirst().price());
    }

    @Test
    void missingTripReturnsNotFound() {
        when(tripRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class, () -> tripService.getSeatAvailability(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    private Vehicle vehicle() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getBusNumber()).thenReturn("DHAKA-METRO-B-1001");
        when(vehicle.getName()).thenReturn("Travel Express 1");
        when(vehicle.getTotalSeats()).thenReturn(20);
        return vehicle;
    }

    private Route route() {
        Route route = mock(Route.class);
        when(route.getId()).thenReturn(1L);
        when(route.getOrigin()).thenReturn("Dhaka");
        when(route.getDestination()).thenReturn("Chattogram");
        return route;
    }
}
