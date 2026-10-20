package com.product.ecommerce.auth.controller;

import com.product.ecommerce.auth.dto.AuthMeResponse;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class MeController {

    @GetMapping("/me")
    public AuthMeResponse getCurrentUser(
            Authentication authentication) {

        return new AuthMeResponse(
                authentication.getName(),
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority())
                        .orElse("ROLE_USER"));
    }
}