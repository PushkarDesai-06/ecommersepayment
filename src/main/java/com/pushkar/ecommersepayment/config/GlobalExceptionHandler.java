package com.pushkar.ecommersepayment.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
    log.error("Runtime exception: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    log.error("Unexpected exception: {}", ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("message", "An unexpected error occurred"));
  }
}
