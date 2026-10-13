package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.TransportBookingStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transport_booking", schema = "transport")
public class TransportBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "booking_reference", nullable = false, unique = true,
            updatable = false, columnDefinition = "uuid")
    private UUID bookingReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportBookingStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "booked_at", nullable = false, updatable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> seats = new ArrayList<>();

    protected TransportBooking() {
    }

    public TransportBooking(Long userId, Trip trip, BigDecimal totalAmount) {
        this.userId = userId;
        this.trip = trip;
        this.totalAmount = totalAmount;
        this.status = TransportBookingStatus.PENDING;
        this.bookingReference = UUID.randomUUID();
    }

    @PrePersist
    void prepareForInsert() {
        OffsetDateTime now = OffsetDateTime.now();
        if (bookingReference == null) bookingReference = UUID.randomUUID();
        if (bookedAt == null) bookedAt = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Trip getTrip() {
        return trip;
    }

    public UUID getBookingReference() {
        return bookingReference;
    }

    public TransportBookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getBookedAt() {
        return bookedAt;
    }

    public List<BookingSeat> getSeats() {
        return seats;
    }

    public void confirm() {
        status = TransportBookingStatus.CONFIRMED;
        updatedAt = OffsetDateTime.now();
    }

    public void cancel() {
        status = TransportBookingStatus.CANCELLED;
        updatedAt = OffsetDateTime.now();
        seats.forEach(BookingSeat::release);
    }
}
