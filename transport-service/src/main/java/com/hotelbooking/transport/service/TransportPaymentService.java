package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.dto.response.TransportPaymentResponse;
import com.hotelbooking.transport.dto.response.TransportPaymentSubmissionResult;
import com.hotelbooking.transport.entity.TransportBooking;
import com.hotelbooking.transport.entity.TransportPayment;
import com.hotelbooking.transport.enums.TransportBookingStatus;
import com.hotelbooking.transport.enums.TransportPaymentStatus;
import com.hotelbooking.transport.exception.ApiException;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TransportPaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransportPaymentService {
    private final UserIdentityService userIdentityService;
    private final TransportBookingRepository bookingRepository;
    private final TransportPaymentRepository paymentRepository;
    private final TransportPaymentProcessor paymentProcessor;

    public TransportPaymentService(UserIdentityService userIdentityService,
                                   TransportBookingRepository bookingRepository,
                                   TransportPaymentRepository paymentRepository,
                                   TransportPaymentProcessor paymentProcessor) {
        this.userIdentityService = userIdentityService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.paymentProcessor = paymentProcessor;
    }

    @Transactional
    public TransportPaymentResponse initiate(String authorization, UUID bookingReference) {
        VerifiedUser user = userIdentityService.verifyUser(authorization);
        TransportBooking booking = ownedBooking(bookingReference, user.id());
        TransportPayment existing = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        if (existing != null && existing.getStatus() == TransportPaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment has already been completed");
        }
        requirePending(booking);
        if (existing != null) {
            if (existing.getStatus() != TransportPaymentStatus.PENDING) {
                throw new ApiException(HttpStatus.CONFLICT, "Payment cannot be retried for this booking");
            }
            return response(existing);
        }
        return response(paymentRepository.save(new TransportPayment(booking)));
    }

    @Transactional
    public TransportPaymentSubmissionResult submit(
            String authorization, UUID paymentReference, String transactionId) {
        VerifiedUser user = userIdentityService.verifyUser(authorization);
        TransportPayment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
        TransportBooking booking = payment.getBooking();
        if (!booking.getUserId().equals(user.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        if (payment.getStatus() == TransportPaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment has already been completed");
        }
        requirePending(booking);
        if (payment.getStatus() != TransportPaymentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment cannot be submitted");
        }
        if (!paymentProcessor.isSuccessful(transactionId)) {
            payment.markFailed(transactionId);
            booking.cancel();
            return new TransportPaymentSubmissionResult(false, response(payment));
        }
        if (paymentRepository.existsByTransactionIdAndStatus(
                transactionId, TransportPaymentStatus.PAID)) {
            throw new ApiException(HttpStatus.CONFLICT, "Transaction ID has already been used");
        }
        payment.markPaid(transactionId);
        booking.confirm();
        return new TransportPaymentSubmissionResult(true, response(payment));
    }

    @Transactional(readOnly = true)
    public TransportPaymentResponse getForBooking(String authorization, UUID bookingReference) {
        VerifiedUser user = userIdentityService.verifyUser(authorization);
        TransportBooking booking = ownedBooking(bookingReference, user.id());
        TransportPayment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
        return response(payment);
    }

    private TransportBooking ownedBooking(UUID reference, Long userId) {
        return bookingRepository.findByBookingReferenceAndUserId(reference, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transport booking not found"));
    }

    private void requirePending(TransportBooking booking) {
        if (booking.getStatus() != TransportBookingStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Booking is not awaiting payment");
        }
    }

    private TransportPaymentResponse response(TransportPayment payment) {
        return new TransportPaymentResponse(payment.getPaymentReference(),
                payment.getBooking().getBookingReference(), payment.getTransactionId(),
                payment.getAmount(), payment.getPaidAmount(), payment.getRemainingAmount(),
                payment.getMethod(), payment.getStatus(), payment.getBooking().getStatus(),
                payment.getCreatedAt());
    }
}
