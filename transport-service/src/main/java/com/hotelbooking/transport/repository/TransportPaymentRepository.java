package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.TransportPayment;
import com.hotelbooking.transport.enums.TransportPaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransportPaymentRepository extends JpaRepository<TransportPayment, Long> {
    @EntityGraph(attributePaths = {"booking", "booking.seats"})
    Optional<TransportPayment> findByPaymentReference(UUID paymentReference);
    Optional<TransportPayment> findByBookingId(Long bookingId);
    boolean existsByTransactionIdAndStatus(String transactionId, TransportPaymentStatus status);
}
