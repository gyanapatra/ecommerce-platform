package com.product.ecommerce.cart.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
  private static final String SECRET = java.util.Base64.getEncoder().encodeToString(new byte[32]);

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticatesValidAccessTokenAndSetsUserIdAsPrincipal() throws Exception {
    UUID userId = UUID.randomUUID();
    JwtAuthenticationFilter filter = filter();
    MockHttpServletRequest request = request(token(userId, Instant.now().plusSeconds(300)));
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(userId.toString(), SecurityContextHolder.getContext().getAuthentication().getName());
    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
  }

  @Test
  void rejectsExpiredAccessToken() throws Exception {
    JwtAuthenticationFilter filter = filter();
    MockHttpServletRequest request = request(token(UUID.randomUUID(), Instant.now().minusSeconds(1)));
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = mock(MockFilterChain.class);

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void rejectsMalformedAccessToken() throws Exception {
    JwtAuthenticationFilter filter = filter();
    MockHttpServletRequest request = request("not-a-jwt");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = mock(MockFilterChain.class);

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    verifyNoInteractions(chain);
  }

  private JwtAuthenticationFilter filter() {
    return new JwtAuthenticationFilter(new MockEnvironment().withProperty("app.jwt.secret", SECRET));
  }

  private MockHttpServletRequest request(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    request.setRequestURI("/api/v1/cart");
    return request;
  }

  private String token(UUID userId, Instant expiration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", "CUSTOMER")
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(Keys.hmacShaKeyFor(new byte[32]), Jwts.SIG.HS256)
        .compact();
  }
}