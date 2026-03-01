package com.agile.ems.utils;

import com.agile.ems.utils.exceptions.BadRequestException;
import com.agile.ems.utils.exceptions.InternalServerErrorException;
import com.agile.ems.utils.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            BadRequestException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleBadRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationException = (MethodArgumentNotValidException) ex;
            Map<String, String> errors = new HashMap<>();
            for (FieldError fieldError : validationException.getBindingResult().getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }

            log.warn("Validation error: {}", errors);
            ApiResponseDto<Object> response = ApiResponseDto.failure(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation failed",
                    errors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        log.warn("Bad request error: {}", ex.getMessage(), ex);
        ApiResponseDto<Object> response = ApiResponseDto.failure(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Bad request",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleNotFound(RuntimeException ex) {
        log.warn("Resource error: {}", ex.getMessage(), ex);
        ApiResponseDto<Object> response = ApiResponseDto.failure(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Requested resource not found",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleInternalServerError(InternalServerErrorException ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        ApiResponseDto<Object> response = ApiResponseDto.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ApiResponseDto<Object> response = ApiResponseDto.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
