package com.hotelbooking.hotel_booking.exception;

import com.hotelbooking.hotel_booking.dto.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private final ExceptionMessageCatalog messageCatalog;

    public GlobalExceptionHandler(ExceptionMessageCatalog messageCatalog) {
        this.messageCatalog = messageCatalog;
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
        var resolved = messageCatalog.resolve(
                exception.getMessageKey(), exception.getMessageArguments());
        return buildResponse(resolved.status(), resolved.message());
    }

    @ExceptionHandler({
            OptimisticLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    ResponseEntity<ApiResponse<Void>> handleOptimisticLockFailure() {
        return response("room.unavailable");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() == null
                        ? message("parameter.invalid", error.getField())
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST,
                message.isBlank() ? message("validation.failed") : message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return response("parameter.invalid", exception.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        return response("parameter.missing", exception.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnreadableRequest() {
        return response("request.body.malformed");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNoResourceFound() {
        return response("resource.not.found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodNotSupported() {
        return response("http.method.not-allowed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported() {
        return response("media.type.unsupported");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpectedException() {
        return response("internal.server.error");
    }

    private ResponseEntity<ApiResponse<Void>> response(String key, Object... arguments) {
        var resolved = messageCatalog.resolve(key, arguments);
        return buildResponse(resolved.status(), resolved.message());
    }

    private String message(String key, Object... arguments) {
        return messageCatalog.resolve(key, arguments).message();
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status, String message) {
        ApiResponse<Void> body = new ApiResponse<>(status.value(), message, null);
        return ResponseEntity.status(status).body(body);
    }
}
