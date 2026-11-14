package com.product.ecommerce.order.api;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.order.exception.DownstreamServiceException;
import com.product.ecommerce.order.exception.OrderConflictException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(OrderConflictException.class)
  public ResponseEntity<Map<String, String>> conflict(OrderConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(DownstreamServiceException.class)
  public ResponseEntity<Map<String, String>> downstream(DownstreamServiceException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> invalidRequest(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .orElse("Request validation failed");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }
}