package com.hotelbooking.hotel_booking.exception;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            InvalidCredentialsException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() == null
                        ? "Invalid value for " + error.getField()
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Validation failed" : message);
    }

    @ExceptionHandler({HotelNotFoundException.class, RoomNotFoundException.class,
            BookingNotFoundException.class})
    ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidFilterException.class)
    ResponseEntity<ApiResponse<Void>> handleInvalidFilter(InvalidFilterException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        String message = "Invalid value for parameter: " + exception.getName();
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        String message = "Missing required parameter: " + exception.getParameterName();
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnreadableRequest() {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNoResourceFound() {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodNotSupported() {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported() {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media type not supported");
    }

    @ExceptionHandler({RoomUnavailableException.class, BookingCancellationException.class})
    ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpectedException() {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status, String message) {
        ApiResponse<Void> body = new ApiResponse<>(status.value(), message, null);
        return ResponseEntity.status(status).body(body);
    }
}
