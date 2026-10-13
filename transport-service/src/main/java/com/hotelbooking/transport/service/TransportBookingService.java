package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.dto.request.CreateTransportBookingRequest;
import com.hotelbooking.transport.dto.response.BookedSeatResponse;
import com.hotelbooking.transport.dto.response.TransportBookingResponse;
import com.hotelbooking.transport.entity.BookingSeat;
import com.hotelbooking.transport.entity.Route;
import com.hotelbooking.transport.entity.Seat;
import com.hotelbooking.transport.entity.TransportBooking;
import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.entity.Vehicle;
import com.hotelbooking.transport.enums.TripStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.BookingSeatRepository;
import com.hotelbooking.transport.repository.SeatAvailabilityView;
import com.hotelbooking.transport.repository.SeatRepository;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TripRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransportBookingService {
    private final UserIdentityService userIdentityService;
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final TransportBookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public TransportBookingService(
            UserIdentityService userIdentityService,
            TripRepository tripRepository,
            SeatRepository seatRepository,
            TransportBookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository) {
        this.userIdentityService = userIdentityService;
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    @Transactional
    public TransportBookingResponse createBooking(
            String authorizationHeader, CreateTransportBookingRequest request) {
        VerifiedUser user = userIdentityService.verifyUser(authorizationHeader);
        Trip trip = findBookableTrip(request.tripId());
        List<Long> requestedSeatIds = distinctSeatIds(request.seatIds());
        List<Seat> seats = loadAndValidateSeats(trip, requestedSeatIds);
        ensureSeatsCurrentlyAvailable(trip, requestedSeatIds);

        BigDecimal total = trip.getFare()
                .multiply(BigDecimal.valueOf(seats.size()));
        TransportBooking booking = bookingRepository.save(
                new TransportBooking(user.id(), trip, total));
        List<BookingSeat> allocations = seats.stream()
                .map(seat -> new BookingSeat(booking, trip, seat, trip.getFare()))
                .toList();

        try {
            List<BookingSeat> savedAllocations =
                    bookingSeatRepository.saveAllAndFlush(allocations);
            booking.getSeats().addAll(savedAllocations);
            return toResponse(booking);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "One or more selected seats are no longer available");
        }
    }

    @Transactional(readOnly = true)
    public List<TransportBookingResponse> getMyBookings(String authorizationHeader) {
        VerifiedUser user = userIdentityService.verifyUser(authorizationHeader);
        return bookingRepository.findByUserIdOrderByBookedAtDesc(user.id())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Trip findBookableTrip(Long tripId) {
        Trip trip = tripRepository.findDetailedById(tripId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "Trip not found with id: " + tripId));
        if (trip.getStatus() != TripStatus.SCHEDULED
                || !trip.getDepartureTime().isAfter(OffsetDateTime.now())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Trip is not available for booking");
        }
        return trip;
    }

    private List<Long> distinctSeatIds(List<Long> seatIds) {
        Set<Long> uniqueIds = new HashSet<>(seatIds);
        if (uniqueIds.size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Seat ids must not contain duplicates");
        }
        return List.copyOf(uniqueIds);
    }

    private List<Seat> loadAndValidateSeats(Trip trip, List<Long> requestedSeatIds) {
        List<Seat> seats = seatRepository.findByIdIn(requestedSeatIds);
        if (seats.size() != requestedSeatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "One or more selected seats do not exist");
        }
        Long tripVehicleId = trip.getVehicle().getId();
        boolean wrongVehicle = seats.stream()
                .anyMatch(seat -> !tripVehicleId.equals(seat.getVehicle().getId()));
        if (wrongVehicle) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Every selected seat must belong to the trip vehicle");
        }
        return seats.stream()
                .sorted(Comparator.comparing(Seat::getSeatNumber))
                .toList();
    }

    private void ensureSeatsCurrentlyAvailable(Trip trip, List<Long> seatIds) {
        Map<Long, SeatAvailabilityView> availability =
                seatRepository.findAvailabilityForTrip(
                                trip.getId(), trip.getVehicle().getId())
                        .stream()
                        .collect(Collectors.toMap(
                                SeatAvailabilityView::getSeatId,
                                Function.identity()));
        boolean unavailable = seatIds.stream()
                .anyMatch(id -> availability.get(id) == null
                        || !availability.get(id).getAvailable());
        if (unavailable) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "One or more selected seats are not available");
        }
    }

    private TransportBookingResponse toResponse(TransportBooking booking) {
        Trip trip = booking.getTrip();
        Route route = trip.getRoute();
        Vehicle vehicle = trip.getVehicle();
        List<BookedSeatResponse> seats = new ArrayList<>(booking.getSeats()).stream()
                .sorted(Comparator.comparing(allocation ->
                        allocation.getSeat().getSeatNumber()))
                .map(allocation -> new BookedSeatResponse(
                        allocation.getSeat().getId(),
                        allocation.getSeat().getSeatNumber(),
                        allocation.getPrice()))
                .toList();
        return new TransportBookingResponse(
                booking.getBookingReference(), booking.getUserId(), trip.getId(),
                route.getOrigin(), route.getDestination(), vehicle.getBusNumber(),
                trip.getDepartureTime(), booking.getStatus(), booking.getTotalAmount(),
                booking.getBookedAt(), seats);
    }
}
