package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.SeatAllocationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "booking_seat", schema = "transport")
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private TransportBooking booking;

    @Column(name = "trip_id", nullable = false, updatable = false)
    private Long tripId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false)
    private SeatAllocationStatus allocationStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected BookingSeat() {
    }

    public BookingSeat(TransportBooking booking, Trip trip, Seat seat, BigDecimal price) {
        this.booking = booking;
        this.tripId = trip.getId();
        this.seat = seat;
        this.price = price;
        this.allocationStatus = SeatAllocationStatus.ACTIVE;
    }

    @PrePersist
    void prepareForInsert() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TransportBooking getBooking() {
        return booking;
    }

    public Long getTripId() {
        return tripId;
    }

    public Seat getSeat() {
        return seat;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public SeatAllocationStatus getAllocationStatus() {
        return allocationStatus;
    }

    public void release() {
        allocationStatus = SeatAllocationStatus.RELEASED;
    }
}
