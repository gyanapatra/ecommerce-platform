package com.product.ecommerce.payment.client;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSnapshot(UUID id, UUID userId, String status, BigDecimal totalAmount) {}