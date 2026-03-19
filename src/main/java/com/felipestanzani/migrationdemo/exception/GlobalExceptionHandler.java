package com.felipestanzani.migrationdemo.exception;

import com.felipestanzani.migrationdemo.dto.ErrorResponse;
import com.felipestanzani.migrationdemo.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_TYPE_BLANK = "about:blank";

    @ExceptionHandler({MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, _) -> existing
                ));

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), validationErrors);

        ValidationErrorResponse response = new ValidationErrorResponse(
                PROBLEM_TYPE_BLANK,
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed. Check the validationErrors field for details.",
                request.getRequestURI(),
                LocalDateTime.now(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Product not found: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                PROBLEM_TYPE_BLANK,
                "Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                new HashMap<>()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        Map<String, Object> details = new HashMap<>();
        details.put("method", ex.getMethod());
        details.put("supportedMethods", ex.getSupportedHttpMethods());

        ErrorResponse response = new ErrorResponse(
                PROBLEM_TYPE_BLANK,
                "Method Not Allowed",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                details
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception occurred for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                PROBLEM_TYPE_BLANK,
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                LocalDateTime.now(),
                new HashMap<>()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
