package com.product.ecommerce.payment.service;

import com.product.ecommerce.payment.domain.Payment;
import com.product.ecommerce.payment.domain.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentPersistenceService {
  private final PaymentRepository repository;
  public PaymentPersistenceService(PaymentRepository repository) { this.repository = repository; }

  @Transactional(readOnly = true)
  public Optional<Payment> findByIdempotency(UUID userId, UUID idempotencyKey) {
    return repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
  }

  @Transactional(readOnly = true)
  public Optional<Payment> findByOrder(UUID orderId) { return repository.findByOrderId(orderId); }

  @Transactional
  public Payment reserve(UUID userId, UUID orderId, UUID idempotencyKey,
      BigDecimal amount, String currency) {
    Optional<Payment> byKey = repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    if (byKey.isPresent()) {
      if (!byKey.get().getOrderId().equals(orderId)) {
        throw new IllegalArgumentException("Idempotency key is already associated with another order");
      }
      return byKey.get();
    }
    return repository.findByOrderId(orderId)
        .orElseGet(() -> repository.saveAndFlush(new Payment(userId, orderId, idempotencyKey, amount, currency)));
  }

  @Transactional
  public Payment markSucceeded(UUID paymentId, String providerReference) {
    Payment payment = repository.findById(paymentId).orElseThrow();
    payment.markSucceeded(providerReference);
    return payment;
  }

  @Transactional(readOnly = true)
  public Optional<Payment> findOwned(UUID paymentId, UUID userId) {
    return repository.findByIdAndUserId(paymentId, userId);
  }
}