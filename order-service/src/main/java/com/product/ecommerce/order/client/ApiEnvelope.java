package com.product.ecommerce.order.client;

import java.time.Instant;

public record ApiEnvelope<T>(boolean success, String message, T data, Instant timestamp) {}