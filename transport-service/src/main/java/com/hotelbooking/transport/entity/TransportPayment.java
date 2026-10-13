package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.TransportPaymentMethod;
import com.hotelbooking.transport.enums.TransportPaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transport_payment", schema = "transport")
public class TransportPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payment_reference", nullable = false, unique = true, updatable = false)
    private UUID paymentReference;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private TransportBooking booking;
    @Column(name = "transaction_id") private String transactionId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2) private BigDecimal paidAmount;
    @Column(name = "remaining_amount", nullable = false, precision = 12, scale = 2) private BigDecimal remainingAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TransportPaymentMethod method;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TransportPaymentStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

    protected TransportPayment() {}

    public TransportPayment(TransportBooking booking) {
        this.paymentReference = UUID.randomUUID();
        this.booking = booking;
        this.amount = booking.getTotalAmount();
        this.paidAmount = new BigDecimal("0.00");
        this.remainingAmount = booking.getTotalAmount();
        this.method = TransportPaymentMethod.CARD;
        this.status = TransportPaymentStatus.PENDING;
    }

    @PrePersist void prepareForInsert() {
        if (paymentReference == null) paymentReference = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public void markPaid(String transactionId) {
        this.transactionId = transactionId; status = TransportPaymentStatus.PAID;
        paidAmount = amount; remainingAmount = new BigDecimal("0.00");
    }

    public void markFailed(String transactionId) {
        this.transactionId = transactionId; status = TransportPaymentStatus.FAILED;
        paidAmount = new BigDecimal("0.00"); remainingAmount = amount;
    }

    public UUID getPaymentReference() { return paymentReference; }
    public TransportBooking getBooking() { return booking; }
    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public TransportPaymentMethod getMethod() { return method; }
    public TransportPaymentStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
