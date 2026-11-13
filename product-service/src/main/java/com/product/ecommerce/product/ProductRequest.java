package com.product.ecommerce.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 64) String sku,
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Size(max = 80) String category,
    @Size(max = 4000) String description,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
    Boolean active) {}
