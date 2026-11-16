package com.product.ecommerce.payment.api;

import com.product.ecommerce.common.exception.BusinessException;
import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.payment.exception.DownstreamServiceException;
import com.product.ecommerce.payment.exception.PaymentConflictException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(PaymentConflictException.class)
  ResponseEntity<Map<String, String>> conflict(PaymentConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(DownstreamServiceException.class)
  ResponseEntity<Map<String, String>> downstream(DownstreamServiceException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", "Order Service is unavailable"));
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<Map<String, String>> downstreamRejected(BusinessException exception) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, String>> invalidRequest(MethodArgumentNotValidException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", "Invalid payment request"));
  }
}
