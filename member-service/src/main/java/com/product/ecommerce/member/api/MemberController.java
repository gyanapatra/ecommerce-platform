package com.product.ecommerce.member.api;

import com.product.ecommerce.common.response.ApiResponse;
import com.product.ecommerce.member.service.MemberProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
public class MemberController {
  private final MemberProfileService service;
  public MemberController(MemberProfileService service) { this.service = service; }

  @GetMapping
  public ApiResponse<MemberProfileResponse> get(Principal principal) {
    return ApiResponse.success("Member profile retrieved", service.get(userId(principal)));
  }

  @PutMapping
  public ApiResponse<MemberProfileResponse> upsert(Principal principal,
      @Valid @RequestBody MemberProfileRequest request) {
    return ApiResponse.success("Member profile saved", service.upsert(userId(principal), request));
  }

  private UUID userId(Principal principal) { return UUID.fromString(principal.getName()); }
}