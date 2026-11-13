package com.product.ecommerce.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ReservationRequest(
    @NotNull UUID reservationId, @NotNull @Positive Integer quantity) {}
