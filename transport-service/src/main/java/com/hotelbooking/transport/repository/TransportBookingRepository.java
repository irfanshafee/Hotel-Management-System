package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.TransportBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportBookingRepository
        extends JpaRepository<TransportBooking, Long> {

    boolean existsByTripId(Long tripId);

    @EntityGraph(attributePaths = {
            "trip", "trip.vehicle", "trip.route", "seats", "seats.seat"
    })
    List<TransportBooking> findByUserIdOrderByBookedAtDesc(Long userId);

    @EntityGraph(attributePaths = {
            "trip", "trip.vehicle", "trip.route", "seats", "seats.seat"
    })
    Optional<TransportBooking> findByBookingReferenceAndUserId(
            UUID bookingReference, Long userId);
}
