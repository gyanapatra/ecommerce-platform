package com.product.ecommerce.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineEvent(UUID productId, String productName, int quantity,
    BigDecimal unitPrice, BigDecimal lineTotal) {}
