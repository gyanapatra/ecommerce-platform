package com.product.ecommerce.order.client;

import java.util.UUID;

public record CartLineSnapshot(UUID productId, int quantity) {}