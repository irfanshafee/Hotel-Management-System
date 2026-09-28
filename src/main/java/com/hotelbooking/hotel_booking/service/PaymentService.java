package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.PaymentResponse;
import com.hotelbooking.hotel_booking.dto.PaymentSubmissionResult;
import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.entity.Payment;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentMethod;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.PaymentRepository;
import com.hotelbooking.hotel_booking.security.AuthenticatedUserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PaymentService {
    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentProcessor paymentProcessor;
    private final AuthenticatedUserContext authenticatedUserContext;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            PaymentProcessor paymentProcessor,
            AuthenticatedUserContext authenticatedUserContext) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.paymentProcessor = paymentProcessor;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @Transactional
    public PaymentResponse initiatePayment(UUID bookingReference) {
        Booking booking = findOwnedBooking(bookingReference);
        Payment existingPayment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.PAID) {
            throw new ApiException("payment.already-paid");
        }
        requirePendingBooking(booking);
        if (existingPayment != null) {
            if (existingPayment.getStatus() != PaymentStatus.PENDING) {
                throw new ApiException("payment.booking.invalid-status");
            }
            return toResponse(existingPayment);
        }

        BigDecimal amount = calculateBookingAmount(booking);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaidAmount(ZERO_AMOUNT);
        payment.setRemainingAmount(amount);
        payment.setMethod(PaymentMethod.CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(null);

        Payment savedPayment = paymentRepository.save(payment);
        booking.setPayment(savedPayment);
        return toResponse(savedPayment);
    }

    @Transactional
    public PaymentSubmissionResult submitPayment(
            UUID paymentReference, String transactionId) {
        Payment payment = paymentRepository.findByPaymentReferenceAndBookingUserId(
                        paymentReference, currentUserId())
                .orElseThrow(() -> new ApiException("payment.not.found"));
        Booking booking = payment.getBooking();

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new ApiException("payment.already-paid");
        }
        requirePendingBooking(booking);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException("payment.booking.invalid-status");
        }

        if (!paymentProcessor.isSuccessful(transactionId)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setTransactionId(transactionId);
            payment.setPaidAmount(ZERO_AMOUNT);
            payment.setRemainingAmount(payment.getAmount());
            booking.setStatus(BookingStatus.CANCELLED);

            paymentRepository.save(payment);
            bookingRepository.save(booking);
            return new PaymentSubmissionResult(false, toResponse(payment));
        }

        if (paymentRepository.existsByTransactionIdAndStatus(
                transactionId, PaymentStatus.PAID)) {
            throw new ApiException("payment.transaction.duplicate");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(transactionId);
        payment.setPaidAmount(payment.getAmount());
        payment.setRemainingAmount(ZERO_AMOUNT);
        booking.setStatus(BookingStatus.CONFIRMED);

        paymentRepository.save(payment);
        bookingRepository.save(booking);
        return new PaymentSubmissionResult(true, toResponse(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentForBooking(UUID bookingReference) {
        Booking booking = findOwnedBooking(bookingReference);
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ApiException("payment.not.found"));
        return toResponse(payment);
    }

    private Booking findOwnedBooking(UUID bookingReference) {
        return bookingRepository.findByBookingReferenceAndUserId(
                        bookingReference, currentUserId())
                .orElseThrow(() -> new ApiException("booking.not.found"));
    }

    private Long currentUserId() {
        return authenticatedUserContext.getRequiredUser().id();
    }

    private void requirePendingBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ApiException("payment.booking.invalid-status");
        }
    }

    private BigDecimal calculateBookingAmount(Booking booking) {
        long numberOfNights = ChronoUnit.DAYS.between(
                booking.getStartDate(), booking.getEndDate());
        return booking.getRoom().getPrice()
                .multiply(BigDecimal.valueOf(numberOfNights))
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getBooking().getBookingReference(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getPaidAmount(),
                payment.getRemainingAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getBooking().getStatus(),
                payment.getCreatedAt());
    }
}
