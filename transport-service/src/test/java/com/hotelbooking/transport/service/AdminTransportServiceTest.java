package com.hotelbooking.transport.service;

import com.hotelbooking.transport.dto.request.AdminCreateVehicleRequest;
import com.hotelbooking.transport.dto.request.AdminTripRequest;
import com.hotelbooking.transport.entity.Driver;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Seat;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.DriverStatus;
import com.hotelbooking.transport.enums.RouteStatus;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.enums.VehicleStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.DriverRepository;
import com.hotelbooking.transport.repository.RouteRepository;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TripRepository;
import com.hotelbooking.transport.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminTransportServiceTest {
    private AdminAuthorizationService authorizationService;
    private DriverRepository driverRepository;
    private VehicleRepository vehicleRepository;
    private RouteRepository routeRepository;
    private TripRepository tripRepository;
    private SeatRepository seatRepository;
    private TransportBookingRepository bookingRepository;
    private AdminTransportService service;

    @BeforeEach
    void setUp() {
        authorizationService = mock(AdminAuthorizationService.class);
        driverRepository = mock(DriverRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        routeRepository = mock(RouteRepository.class);
        tripRepository = mock(TripRepository.class);
        seatRepository = mock(SeatRepository.class);
        bookingRepository = mock(TransportBookingRepository.class);
        service = new AdminTransportService(authorizationService, driverRepository,
                vehicleRepository, routeRepository, tripRepository,
                seatRepository, bookingRepository);
    }

    @Test
    void vehicleCreationAutomaticallyCreatesEverySeat() {
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(seatRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createVehicle("Bearer admin",
                new AdminCreateVehicleRequest("DHAKA-100", "Volvo", 6));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(captor.capture());
        assertEquals(List.of("A1", "B1", "C1", "D1", "A2", "B2"),
                captor.getValue().stream().map(Seat::getSeatNumber).toList());
    }

    @Test
    void tripCreationRejectsOverlappingDriverOrVehicle() {
        Vehicle vehicle = mock(Vehicle.class);
        Route route = mock(Route.class);
        Driver driver = mock(Driver.class);
        when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getStatus()).thenReturn(VehicleStatus.ACTIVE);
        when(route.getStatus()).thenReturn(RouteStatus.ACTIVE);
        when(driver.getId()).thenReturn(3L);
        when(driver.getStatus()).thenReturn(DriverStatus.ACTIVE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(routeRepository.findById(2L)).thenReturn(Optional.of(route));
        when(driverRepository.findById(3L)).thenReturn(Optional.of(driver));

        OffsetDateTime departure = OffsetDateTime.now().plusDays(2);
        OffsetDateTime arrival = departure.plusHours(5);
        when(tripRepository.countResourceConflicts(null, 1L, 3L, departure, arrival))
                .thenReturn(1L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.createTrip("Bearer admin", new AdminTripRequest(
                        1L, 2L, 3L, departure, arrival,
                        new BigDecimal("1500.00"), TripStatus.SCHEDULED)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void tripWithBookingsCannotBeCancelled() {
        Trip trip = mock(Trip.class);
        when(tripRepository.findDetailedById(9L)).thenReturn(Optional.of(trip));
        when(bookingRepository.existsByTripId(9L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.cancelTrip("Bearer admin", 9L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }
}
