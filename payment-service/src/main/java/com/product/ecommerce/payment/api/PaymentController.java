package com.product.ecommerce.payment.api;

import com.product.ecommerce.common.response.ApiResponse;
import com.product.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
  private final PaymentService service;
  public PaymentController(PaymentService service) { this.service = service; }

  @PostMapping
  public ApiResponse<PaymentResponse> create(Principal principal,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @RequestHeader("Idempotency-Key") UUID idempotencyKey,
      @Valid @RequestBody PaymentRequest request) {
    return ApiResponse.success("Payment processed", service.create(userId(principal), idempotencyKey, request, authorization));
  }

  @GetMapping("/{paymentId}")
  public ApiResponse<PaymentResponse> get(Principal principal, @PathVariable UUID paymentId) {
    return ApiResponse.success("Payment retrieved", service.get(userId(principal), paymentId));
  }

  private UUID userId(Principal principal) { return UUID.fromString(principal.getName()); }
}