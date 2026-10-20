package com.product.ecommerce.auth.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 7,
                max = 30,
                message = "Password must contain between 7 and 30 characters"
        )
        String password,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName

) {
}