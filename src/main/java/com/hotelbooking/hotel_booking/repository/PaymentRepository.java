package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.Payment;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByIdAndBookingUserId(Long paymentId, Long userId);

    boolean existsByTransactionIdAndStatus(
            String transactionId, PaymentStatus status);
}
