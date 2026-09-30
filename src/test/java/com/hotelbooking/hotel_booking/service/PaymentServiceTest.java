package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.PaymentSubmissionResult;
import com.hotelbooking.hotel_booking.entity.Booking;
import com.hotelbooking.hotel_booking.entity.Payment;
import com.hotelbooking.hotel_booking.entity.Room;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.BookingStatus;
import com.hotelbooking.hotel_booking.enums.PaymentStatus;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.BookingRepository;
import com.hotelbooking.hotel_booking.repository.PaymentRepository;
import com.hotelbooking.hotel_booking.security.AuthenticatedUser;
import com.hotelbooking.hotel_booking.security.AuthenticatedUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {
    private PaymentRepository paymentRepository;
    private BookingRepository bookingRepository;
    private PaymentProcessor paymentProcessor;
    private AuthenticatedUserContext authenticatedUserContext;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        bookingRepository = mock(BookingRepository.class);
        paymentProcessor = mock(PaymentProcessor.class);
        authenticatedUserContext = mock(AuthenticatedUserContext.class);
        paymentService = new PaymentService(
                paymentRepository, bookingRepository, paymentProcessor, authenticatedUserContext);
        useAuthenticatedUser(1L);
    }

    @Test
    void initiationCalculatesAmountFromNightlyRoomPrice() {
        Booking booking = pendingBooking();
        UUID bookingUuid = booking.getBookingUuid();
        when(bookingRepository.findByBookingUuidAndUserId(bookingUuid, 1L))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(25L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(10L);
            return payment;
        });

        var response = paymentService.initiatePayment(bookingUuid);

        assertEquals(new BigDecimal("15000.00"), response.amount());
        assertEquals(new BigDecimal("0.00"), response.paidAmount());
        assertEquals(new BigDecimal("15000.00"), response.remainingAmount());
        assertEquals(PaymentStatus.PENDING, response.paymentStatus());
        assertEquals(bookingUuid, response.bookingId());
        assertNotNull(response.paymentId());
    }

    @Test
    void validTransactionPaysAndConfirmsBooking() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByPaymentUuidAndBookingUserId(
                payment.getPaymentUuid(), 1L))
                .thenReturn(Optional.of(payment));
        when(paymentProcessor.isSuccessful("S 6789ABcd")).thenReturn(true);
        when(paymentRepository.existsByTransactionIdAndStatus(
                "S 6789ABcd", PaymentStatus.PAID)).thenReturn(false);

        PaymentSubmissionResult result = paymentService.submitPayment(
                payment.getPaymentUuid(), "S 6789ABcd");

        assertTrue(result.successful());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, payment.getBooking().getStatus());
        assertEquals(payment.getAmount(), payment.getPaidAmount());
        assertEquals(new BigDecimal("0.00"), payment.getRemainingAmount());
        assertEquals("S 6789ABcd", payment.getTransactionId());
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(payment.getBooking());
    }

    @Test
    void invalidTransactionFailsAndCancelsBookingWithoutThrowing() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByPaymentUuidAndBookingUserId(
                payment.getPaymentUuid(), 1L))
                .thenReturn(Optional.of(payment));
        when(paymentProcessor.isSuccessful("ABC123")).thenReturn(false);

        PaymentSubmissionResult result = paymentService.submitPayment(
                payment.getPaymentUuid(), "ABC123");

        assertFalse(result.successful());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(BookingStatus.CANCELLED, payment.getBooking().getStatus());
        assertEquals(new BigDecimal("0.00"), payment.getPaidAmount());
        assertEquals(payment.getAmount(), payment.getRemainingAmount());
        assertEquals("ABC123", payment.getTransactionId());
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(payment.getBooking());
    }

    @Test
    void duplicateSuccessfulTransactionDoesNotChangeSecondPaymentOrBooking() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByPaymentUuidAndBookingUserId(
                payment.getPaymentUuid(), 1L))
                .thenReturn(Optional.of(payment));
        when(paymentProcessor.isSuccessful("S ABCD1234")).thenReturn(true);
        when(paymentRepository.existsByTransactionIdAndStatus(
                "S ABCD1234", PaymentStatus.PAID)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> paymentService.submitPayment(
                        payment.getPaymentUuid(), "S ABCD1234"));

        assertEquals("payment.transaction.duplicate", exception.getMessageKey());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(BookingStatus.PENDING, payment.getBooking().getStatus());
        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void foreignBookingIsReportedAsNotFound() {
        useAuthenticatedUser(2L);
        UUID reference = UUID.randomUUID();
        when(bookingRepository.findByBookingUuidAndUserId(reference, 2L))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> paymentService.initiatePayment(reference));

        assertEquals("booking.not.found", exception.getMessageKey());
    }

    @Test
    void foreignPaymentIsReportedAsNotFound() {
        useAuthenticatedUser(2L);
        UUID reference = UUID.randomUUID();
        when(paymentRepository.findByPaymentUuidAndBookingUserId(reference, 2L))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> paymentService.submitPayment(reference, "S 6789ABcd"));

        assertEquals("payment.not.found", exception.getMessageKey());
    }

    @Test
    void retrievesPaymentForOwnedBookingReference() {
        Payment payment = pendingPayment();
        Booking booking = payment.getBooking();
        UUID reference = booking.getBookingUuid();
        when(bookingRepository.findByBookingUuidAndUserId(reference, 1L))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.of(payment));

        var response = paymentService.getPaymentForBooking(reference);

        assertEquals(reference, response.bookingId());
        assertEquals(payment.getPaymentUuid(), response.paymentId());
    }

    private Booking pendingBooking() {
        User user = new User();
        user.setId(1L);

        Room room = new Room();
        room.setId(3L);
        room.setPrice(new BigDecimal("3000.00"));

        Booking booking = new Booking();
        booking.setId(25L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(LocalDate.of(2026, 11, 10));
        booking.setEndDate(LocalDate.of(2026, 11, 15));
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }

    private Payment pendingPayment() {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setBooking(pendingBooking());
        payment.setAmount(new BigDecimal("15000.00"));
        payment.setPaidAmount(new BigDecimal("0.00"));
        payment.setRemainingAmount(new BigDecimal("15000.00"));
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private void useAuthenticatedUser(Long userId) {
        when(authenticatedUserContext.getRequiredUser()).thenReturn(
                new AuthenticatedUser(userId, "Test User", "test@example.com", null));
    }
}
