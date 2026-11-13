package com.product.ecommerce.auth.controller;

import com.product.ecommerce.auth.service.AuthService;
import com.product.ecommerce.auth.user.dto.LoginRequest;
import com.product.ecommerce.auth.user.dto.LoginResponse;
import com.product.ecommerce.auth.user.dto.RegisterRequest;
import com.product.ecommerce.auth.user.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.product.ecommerce.auth.user.dto.RefreshTokenRequest;
import com.product.ecommerce.auth.user.dto.LogoutRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

    RegisterResponse response = authService.register(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {

    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {

    authService.logout(request.refreshToken());

    return ResponseEntity.noContent().build();
  }
}
