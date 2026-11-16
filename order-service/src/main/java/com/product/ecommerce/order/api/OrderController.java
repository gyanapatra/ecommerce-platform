package com.product.ecommerce.order.api;

import com.product.ecommerce.common.response.ApiResponse;
import com.product.ecommerce.order.service.OrderService;
import com.product.ecommerce.order.service.PaymentServiceAuthorization;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
  private final OrderService orderService;
  private final PaymentServiceAuthorization paymentAuthorization;

  public OrderController(OrderService orderService, PaymentServiceAuthorization paymentAuthorization) {
    this.orderService = orderService;
    this.paymentAuthorization = paymentAuthorization;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> checkout(
      Principal principal,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @RequestHeader("Idempotency-Key") UUID idempotencyKey) {
    OrderResponse order = orderService.checkout(userId(principal), idempotencyKey, authorization);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order placed", order));
  }

  @GetMapping
  public ApiResponse<List<OrderResponse>> list(Principal principal) {
    return ApiResponse.success("Orders retrieved", orderService.getOrders(userId(principal)));
  }

  @GetMapping("/{orderId}")
  public ApiResponse<OrderResponse> get(Principal principal, @PathVariable UUID orderId) {
    return ApiResponse.success("Order retrieved", orderService.getOrder(userId(principal), orderId));
  }

  @PostMapping("/{orderId}/payment-start")
  public ApiResponse<OrderResponse> beginPayment(Principal principal,
      @PathVariable UUID orderId,
      @RequestHeader("X-Payment-Service-Key") String paymentServiceKey) {
    if (!paymentAuthorization.isValid(paymentServiceKey)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment service authorization required");
    }
    return ApiResponse.success("Order marked payment pending", orderService.beginPayment(userId(principal), orderId));
  }
  @PostMapping("/{orderId}/payment-confirmation")
  public ApiResponse<OrderResponse> confirmPayment(Principal principal,
      @PathVariable UUID orderId,
      @RequestHeader("X-Payment-Service-Key") String paymentServiceKey) {
    if (!paymentAuthorization.isValid(paymentServiceKey)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment service authorization required");
    }
    return ApiResponse.success("Order marked paid", orderService.confirmPayment(userId(principal), orderId));
  }

  @PostMapping("/{orderId}/cancel")
  public ApiResponse<OrderResponse> cancel(Principal principal, @PathVariable UUID orderId) {
    return ApiResponse.success("Order cancelled", orderService.cancel(userId(principal), orderId));
  }

  private UUID userId(Principal principal) { return UUID.fromString(principal.getName()); }
}
