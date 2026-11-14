package com.product.ecommerce.notification.api;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException exception,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
        Instant.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(), request.getRequestURI(), null, Map.of()));
  }
}
