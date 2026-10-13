package com.hotelbooking.transport.service;

import com.hotelbooking.transport.client.VerifiedUser;
import com.hotelbooking.transport.entity.TransportBooking;
import com.hotelbooking.transport.entity.TransportPayment;
import com.hotelbooking.transport.enums.TransportBookingStatus;
import com.hotelbooking.transport.enums.TransportPaymentStatus;
import com.hotelbooking.transport.repository.TransportBookingRepository;
import com.hotelbooking.transport.repository.TransportPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransportPaymentServiceTest {
    private UserIdentityService identityService;
    private TransportBookingRepository bookingRepository;
    private TransportPaymentRepository paymentRepository;
    private TransportPaymentProcessor processor;
    private TransportPaymentService service;
    private TransportBooking booking;
    private TransportPayment payment;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        identityService = mock(UserIdentityService.class);
        bookingRepository = mock(TransportBookingRepository.class);
        paymentRepository = mock(TransportPaymentRepository.class);
        processor = mock(TransportPaymentProcessor.class);
        service = new TransportPaymentService(
                identityService, bookingRepository, paymentRepository, processor);
        booking = mock(TransportBooking.class);
        payment = mock(TransportPayment.class);
        paymentId = UUID.randomUUID();
        when(identityService.verifyUser("Bearer token"))
                .thenReturn(new VerifiedUser(7L, "User", "user@example.com", "USER"));
        when(paymentRepository.findByPaymentReference(paymentId)).thenReturn(Optional.of(payment));
        when(payment.getBooking()).thenReturn(booking);
        when(payment.getStatus()).thenReturn(TransportPaymentStatus.PENDING);
        when(booking.getUserId()).thenReturn(7L);
        when(booking.getStatus()).thenReturn(TransportBookingStatus.PENDING);
    }

    @Test
    void successfulTransactionConfirmsBooking() {
        when(processor.isSuccessful("S 1234ABcd")).thenReturn(true);

        var result = service.submit("Bearer token", paymentId, "S 1234ABcd");

        assertTrue(result.successful());
        verify(payment).markPaid("S 1234ABcd");
        verify(booking).confirm();
    }

    @Test
    void invalidTransactionCancelsBookingAndReleasesItsSeats() {
        when(processor.isSuccessful("invalid")).thenReturn(false);

        var result = service.submit("Bearer token", paymentId, "invalid");

        assertFalse(result.successful());
        verify(payment).markFailed("invalid");
        verify(booking).cancel();
    }
}
