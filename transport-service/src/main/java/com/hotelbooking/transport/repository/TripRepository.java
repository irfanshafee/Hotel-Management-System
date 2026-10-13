package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.Trip;
import com.hotelbooking.transport.enums.TripStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("""
            select trip from Trip trip
            join fetch trip.vehicle vehicle
            join fetch trip.route route
            join fetch trip.driver driver
            where lower(route.origin) = lower(:origin)
              and lower(route.destination) = lower(:destination)
              and trip.departureTime >= :dayStart
              and trip.departureTime < :nextDayStart
              and trip.status = :status
            order by trip.departureTime
            """)
    List<Trip> searchScheduledTrips(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("dayStart") OffsetDateTime dayStart,
            @Param("nextDayStart") OffsetDateTime nextDayStart,
            @Param("status") TripStatus status);

    @EntityGraph(attributePaths = {"vehicle", "route", "driver"})
    Optional<Trip> findDetailedById(Long id);

    @Query("""
            select count(trip) from Trip trip
            where (:excludeTripId is null or trip.id <> :excludeTripId)
              and trip.status <> com.hotelbooking.transport.enums.TripStatus.CANCELLED
              and (trip.vehicle.id = :vehicleId or trip.driver.id = :driverId)
              and trip.departureTime < :arrivalTime
              and trip.arrivalTime > :departureTime
            """)
    long countResourceConflicts(
            @Param("excludeTripId") Long excludeTripId,
            @Param("vehicleId") Long vehicleId,
            @Param("driverId") Long driverId,
            @Param("departureTime") OffsetDateTime departureTime,
            @Param("arrivalTime") OffsetDateTime arrivalTime);
}
