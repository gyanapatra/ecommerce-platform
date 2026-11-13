package com.product.ecommerce.inventory;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StockAdjustmentRequest(@NotNull UUID adjustmentId, @NotNull Integer quantityDelta) {}
