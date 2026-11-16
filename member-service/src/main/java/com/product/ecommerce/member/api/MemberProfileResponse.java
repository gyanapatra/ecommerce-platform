package com.product.ecommerce.member.api;

import com.product.ecommerce.member.domain.MemberProfile;
import java.time.Instant;
import java.util.UUID;

public record MemberProfileResponse(UUID userId, String memberNumber, String firstName,
    String lastName, String phoneNumber, String tier, String status, Instant joinedAt,
    Instant updatedAt) {
  public static MemberProfileResponse from(MemberProfile profile) {
    return new MemberProfileResponse(profile.getUserId(), profile.getMemberNumber(),
        profile.getFirstName(), profile.getLastName(), profile.getPhoneNumber(),
        profile.getTier(), profile.getStatus(), profile.getJoinedAt(), profile.getUpdatedAt());
  }
}