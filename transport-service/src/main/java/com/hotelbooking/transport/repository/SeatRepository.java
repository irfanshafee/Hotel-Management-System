package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query(value = """
            select seat.id as "seatId",
                   seat.seat_number as "seatNumber",
                   not exists (
                       select 1
                       from transport.booking_seat allocation
                       where allocation.trip_id = :tripId
                         and allocation.seat_id = seat.id
                         and allocation.allocation_status = 'ACTIVE'
                   ) as "available"
            from transport.seat seat
            where seat.vehicle_id = :vehicleId
            order by seat.seat_number
            """, nativeQuery = true)
    List<SeatAvailabilityView> findAvailabilityForTrip(
            @Param("tripId") Long tripId,
            @Param("vehicleId") Long vehicleId);

    List<Seat> findByIdIn(List<Long> ids);
}
