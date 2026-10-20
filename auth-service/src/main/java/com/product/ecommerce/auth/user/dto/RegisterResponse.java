package com.product.ecommerce.auth.user.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}