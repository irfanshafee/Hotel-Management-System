package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "resend")
class ResendPasswordResetTokenDelivery implements PasswordResetTokenDelivery {
    private final ResendEmailSender resendEmailSender;
    private final String frontendBaseUrl;

    ResendPasswordResetTokenDelivery(ResendEmailSender resendEmailSender,
                                     @org.springframework.beans.factory.annotation.Value("${app.password-reset.frontend-base-url}") String frontendBaseUrl) {
        this.resendEmailSender = resendEmailSender;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/$", "");
    }
    @Override public void deliver(User user, String rawToken) {
        String url = frontendBaseUrl + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String html = "<p>Hello,</p><p>We received a request to reset your Hotel Booking account password.</p>" +
                "<p><a href=\"" + url + "\">Reset Password</a></p><p>This link expires in 30 minutes.</p>" +
                "<p>If you did not request this, you can safely ignore this email.</p>";
        String text = "Hello,\n\nWe received a request to reset your Hotel Booking account password.\n\n"
                + "Reset Password: " + url + "\n\nThis link expires in 30 minutes.\n"
                + "If you did not request this, you can safely ignore this email.";
        resendEmailSender.send(user.getEmail(), "Reset your Hotel Booking password", html, text);
    }
}
