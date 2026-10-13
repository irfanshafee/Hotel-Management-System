package com.hotelbooking.hotel_booking.dto;

import com.hotelbooking.hotel_booking.enums.PaymentMethod;
import com.hotelbooking.hotel_booking.enums.RoomCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Immutable, non-sensitive data captured before the confirmation transaction closes. */
public record BookingConfirmationEmailData(
        String recipientEmail, String customerName, UUID bookingReference,
        String hotelName, String hotelCity, String hotelAddress, String roomNumber,
        RoomCategory roomCategory, Integer capacity, LocalDate checkIn, LocalDate checkOut,
        long numberOfNights, BigDecimal totalAmount, PaymentMethod paymentMethod,
        String transactionReference) {
}
