package com.product.ecommerce.payment.service;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.payment.api.PaymentRequest;
import com.product.ecommerce.payment.api.PaymentResponse;
import com.product.ecommerce.payment.client.OrderClient;
import com.product.ecommerce.payment.client.OrderSnapshot;
import com.product.ecommerce.payment.domain.Payment;
import com.product.ecommerce.payment.domain.PaymentStatus;
import com.product.ecommerce.payment.exception.PaymentConflictException;
import com.product.ecommerce.payment.provider.PaymentProvider;
import com.product.ecommerce.payment.provider.PaymentReceipt;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
  private final PaymentPersistenceService persistence;
  private final OrderClient orderClient;
  private final PaymentProvider provider;
  private final String currency;

  public PaymentService(PaymentPersistenceService persistence, OrderClient orderClient,
      PaymentProvider provider, @Value("${app.payment.currency:INR}") String currency) {
    this.persistence = persistence;
    this.orderClient = orderClient;
    this.provider = provider;
    this.currency = currency;
  }

  public PaymentResponse create(UUID userId, UUID idempotencyKey, PaymentRequest request,
      String authorization) {
    Payment existingKey = persistence.findByIdempotency(userId, idempotencyKey).orElse(null);
    if (existingKey != null) {
      if (!existingKey.getOrderId().equals(request.orderId())) {
        throw new PaymentConflictException("Idempotency key is already associated with another order");
      }
      return settle(existingKey, authorization);
    }

    OrderSnapshot order = orderClient.getOrder(authorization, request.orderId());
    if (!order.userId().equals(userId)) throw new ResourceNotFoundException("Order not found");
    if (!"PLACED".equals(order.status()) && !"PAYMENT_PENDING".equals(order.status())) {
      throw new PaymentConflictException("Only placed orders can be paid");
    }
    BigDecimal amount = order.totalAmount();
    if (amount == null || amount.signum() <= 0) {
      throw new PaymentConflictException("Order total must be greater than zero");
    }

    Payment payment;
    try {
      payment = persistence.reserve(userId, order.id(), idempotencyKey, amount, currency);
    } catch (DataIntegrityViolationException exception) {
      payment = persistence.findByIdempotency(userId, idempotencyKey)
          .or(() -> persistence.findByOrder(order.id()))
          .orElseThrow(() -> exception);
    } catch (IllegalArgumentException exception) {
      throw new PaymentConflictException(exception.getMessage());
    }
    if (!payment.getUserId().equals(userId)) throw new ResourceNotFoundException("Order not found");
    if (!payment.getIdempotencyKey().equals(idempotencyKey)) {
      throw new PaymentConflictException("A payment is already associated with this order");
    }
    return settle(payment, authorization);
  }

  public PaymentResponse get(UUID userId, UUID paymentId) {
    return persistence.findOwned(paymentId, userId).map(PaymentResponse::from)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
  }

  private PaymentResponse settle(Payment payment, String authorization) {
    if (payment.getStatus() == PaymentStatus.SUCCEEDED) return PaymentResponse.from(payment);
    orderClient.beginPayment(authorization, payment.getOrderId());
    PaymentReceipt receipt = provider.charge(payment.getIdempotencyKey(), payment.getAmount(), payment.getCurrency());
    orderClient.confirmPayment(authorization, payment.getOrderId());
    return PaymentResponse.from(persistence.markSucceeded(payment.getId(), receipt.reference()));
  }
}

