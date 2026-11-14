package com.product.ecommerce.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetCartItemQuantityRequest(@NotNull @Positive Integer quantity) {}