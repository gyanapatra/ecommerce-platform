package com.product.ecommerce.member.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final SecretKey signingKey;

  public JwtAuthenticationFilter(Environment environment) {
    byte[] keyBytes = Decoders.BASE64.decode(environment.getRequiredProperty("app.jwt.secret"));
    if (keyBytes.length < 32) throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
    signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getRequestURI().startsWith("/actuator/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      String token = authorization.substring(7).trim();
      Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      String userId = claims.getSubject();
      String role = claims.get("role", String.class);
      if (userId == null || role == null || role.isBlank() || claims.getIssuedAt() == null
          || claims.getExpiration() == null || claims.getIssuedAt().after(new Date())) {
        throw new IllegalArgumentException("Invalid JWT");
      }
      UUID.fromString(userId);
      String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
      var authentication = UsernamePasswordAuthenticationToken.authenticated(userId, null,
          List.of(new SimpleGrantedAuthority(authority)));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (RuntimeException exception) {
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    filterChain.doFilter(request, response);
  }
}