package com.hotelbooking.hotel_booking.entity;

import com.hotelbooking.hotel_booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(
        name = "uk_bookings_booking_uuid", columnNames = "booking_uuid"))
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.NONE)
    @Column(name = "booking_uuid", nullable = false, updatable = false,
            columnDefinition = "uuid")
    private UUID bookingUuid = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "booking")
    private Payment payment;

    @PrePersist
    void prepareForInsert() {
        if (bookingUuid == null) bookingUuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
