package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.BookingConfirmationEmailData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "resend")
class ResendBookingConfirmationEmailService implements BookingConfirmationEmailService {
    private final ResendEmailSender resendEmailSender;

    ResendBookingConfirmationEmailService(ResendEmailSender resendEmailSender) {
        this.resendEmailSender = resendEmailSender;
    }

    @Override
    public void sendBookingConfirmationEmail(BookingConfirmationEmailData data) {
        resendEmailSender.send(data.recipientEmail(), "Booking Confirmed - " + data.hotelName(),
                html(data), text(data));
    }

    private String html(BookingConfirmationEmailData data) {
        return "<div style=\"font-family:Arial,sans-serif;color:#1f2937;max-width:600px;margin:auto\">"
                + "<h2 style=\"color:#0f766e\">Booking Confirmed</h2>"
                + "<p>Hello " + escape(data.customerName()) + ",</p>"
                + "<p>Your booking has been successfully confirmed.</p><table style=\"border-collapse:collapse;width:100%\">"
                + row("Hotel", data.hotelName()) + row("Location", location(data)) + row("Room", data.roomNumber())
                + row("Room category", data.roomCategory().name()) + row("Capacity", String.valueOf(data.capacity()))
                + row("Check-in", data.checkIn().toString()) + row("Check-out", data.checkOut().toString())
                + row("Nights", String.valueOf(data.numberOfNights())) + row("Total amount", taka(data.totalAmount()))
                + row("Payment status", "PAID") + row("Booking status", "CONFIRMED")
                + row("Payment method", data.paymentMethod().name())
                + row("Transaction reference", data.transactionReference())
                + row("Booking reference", data.bookingReference().toString())
                + "</table><p>Thank you for booking with us.</p></div>";
    }

    private String text(BookingConfirmationEmailData data) {
        return "BOOKING CONFIRMED\n\nHello " + data.customerName()
                + ",\n\nYour booking has been successfully confirmed.\n\nHotel: " + data.hotelName()
                + "\nLocation: " + location(data) + "\nRoom: " + data.roomNumber()
                + "\nRoom category: " + data.roomCategory() + "\nCapacity: " + data.capacity()
                + "\nCheck-in: " + data.checkIn() + "\nCheck-out: " + data.checkOut()
                + "\nNights: " + data.numberOfNights() + "\nTotal amount: " + taka(data.totalAmount())
                + "\nPayment status: PAID\nBooking status: CONFIRMED\nPayment method: " + data.paymentMethod()
                + "\nTransaction reference: " + data.transactionReference()
                + "\nBooking reference: " + data.bookingReference() + "\n\nThank you for booking with us.";
    }

    private String row(String label, String value) {
        return "<tr><td style=\"padding:8px;border-bottom:1px solid #e5e7eb;font-weight:bold\">"
                + escape(label) + "</td><td style=\"padding:8px;border-bottom:1px solid #e5e7eb\">"
                + escape(value == null ? "-" : value) + "</td></tr>";
    }

    private String location(BookingConfirmationEmailData data) {
        return data.hotelAddress() == null || data.hotelAddress().isBlank()
                ? data.hotelCity() : data.hotelCity() + ", " + data.hotelAddress();
    }

    private String taka(BigDecimal amount) {
        return "৳" + NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
