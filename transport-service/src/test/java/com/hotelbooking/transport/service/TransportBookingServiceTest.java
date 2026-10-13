package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.dto.request.CreateTransportBookingRequest;
import com.hotelbooking.transport.entity.BookingSeat;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Seat;
import com.hotelbooking.transport.entity.TransportBooking;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.enums.TransportBookingStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.BookingSeatRepository;
import com.hotelbooking.transport.repository.SeatAvailabilityView;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransportBookingServiceTest {
    private UserIdentityService userIdentityService;
    private TripRepository tripRepository;
    private SeatRepository seatRepository;
    private TransportBookingRepository bookingRepository;
    private BookingSeatRepository bookingSeatRepository;
    private TransportBookingService service;
    private Trip trip;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        userIdentityService = mock(UserIdentityService.class);
        tripRepository = mock(TripRepository.class);
        seatRepository = mock(SeatRepository.class);
        bookingRepository = mock(TransportBookingRepository.class);
        bookingSeatRepository = mock(BookingSeatRepository.class);
        service = new TransportBookingService(
                userIdentityService, tripRepository, seatRepository,
                bookingRepository, bookingSeatRepository);

        vehicle = mock(Vehicle.class);
        when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getBusNumber()).thenReturn("DHAKA-METRO-B-1001");
        trip = mock(Trip.class);
        Route route = route();
        when(trip.getId()).thenReturn(10L);
        when(trip.getVehicle()).thenReturn(vehicle);
        when(trip.getRoute()).thenReturn(route);
        when(trip.getFare()).thenReturn(new BigDecimal("1200.00"));
        when(trip.getStatus()).thenReturn(TripStatus.SCHEDULED);
        when(trip.getDepartureTime())
                .thenReturn(OffsetDateTime.parse("2027-01-01T06:00:00+06:00"));
        when(userIdentityService.verifyUser("Bearer token"))
                .thenReturn(new VerifiedUser(7L, "User", "user@example.com", "USER"));
        when(tripRepository.findDetailedById(10L)).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any(TransportBooking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingSeatRepository.saveAllAndFlush(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsAtomicMultiSeatBookingAndCalculatesTotalOnServer() {
        Seat seatA1 = seat(1L, "A1");
        Seat seatA2 = seat(2L, "A2");
        SeatAvailabilityView availableA1 = availability(1L, true);
        SeatAvailabilityView availableA2 = availability(2L, true);
        when(seatRepository.findByIdIn(anyList())).thenReturn(List.of(seatA1, seatA2));
        when(seatRepository.findAvailabilityForTrip(10L, 1L))
                .thenReturn(List.of(availableA1, availableA2));

        var response = service.createBooking("Bearer token",
                new CreateTransportBookingRequest(10L, List.of(1L, 2L)));

        assertEquals(7L, response.userId());
        assertEquals(TransportBookingStatus.PENDING, response.status());
        assertEquals(new BigDecimal("2400.00"), response.totalAmount());
        assertEquals(2, response.seats().size());
        verify(bookingSeatRepository).saveAllAndFlush(anyList());
    }

    @Test
    void rejectsDuplicateSeatIdsBeforePersistence() {
        ApiException exception = assertThrows(ApiException.class,
                () -> service.createBooking("Bearer token",
                        new CreateTransportBookingRequest(10L, List.of(1L, 1L))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsSeatAlreadyShownAsUnavailable() {
        Seat seatA1 = seat(1L, "A1");
        SeatAvailabilityView unavailableA1 = availability(1L, false);
        when(seatRepository.findByIdIn(anyList())).thenReturn(List.of(seatA1));
        when(seatRepository.findAvailabilityForTrip(10L, 1L))
                .thenReturn(List.of(unavailableA1));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.createBooking("Bearer token",
                        new CreateTransportBookingRequest(10L, List.of(1L))));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void databaseUniqueViolationBecomesConflict() {
        Seat seatA1 = seat(1L, "A1");
        SeatAvailabilityView availableA1 = availability(1L, true);
        when(seatRepository.findByIdIn(anyList())).thenReturn(List.of(seatA1));
        when(seatRepository.findAvailabilityForTrip(10L, 1L))
                .thenReturn(List.of(availableA1));
        when(bookingSeatRepository.saveAllAndFlush(anyList()))
                .thenThrow(new DataIntegrityViolationException("concurrent allocation"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.createBooking("Bearer token",
                        new CreateTransportBookingRequest(10L, List.of(1L))));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    private Seat seat(Long id, String number) {
        Seat seat = mock(Seat.class);
        when(seat.getId()).thenReturn(id);
        when(seat.getSeatNumber()).thenReturn(number);
        when(seat.getVehicle()).thenReturn(vehicle);
        return seat;
    }

    private SeatAvailabilityView availability(Long id, boolean available) {
        SeatAvailabilityView view = mock(SeatAvailabilityView.class);
        when(view.getSeatId()).thenReturn(id);
        when(view.getAvailable()).thenReturn(available);
        return view;
    }

    private Route route() {
        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn("Dhaka");
        when(route.getDestination()).thenReturn("Chattogram");
        return route;
    }
}
