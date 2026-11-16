package com.product.ecommerce.member.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.product.ecommerce.member.api.MemberProfileRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemberProfileTest {
  @Test
  void createsAndUpdatesMemberOwnedProfileFields() {
    MemberProfile profile = new MemberProfile(UUID.randomUUID(),
        new MemberProfileRequest("  Ada ", " Lovelace ", " 12345 "));

    assertEquals("Ada", profile.getFirstName());
    assertEquals("Lovelace", profile.getLastName());
    assertEquals("12345", profile.getPhoneNumber());
    assertEquals("STANDARD", profile.getTier());
    assertEquals("ACTIVE", profile.getStatus());
    assertNotNull(profile.getMemberNumber());

    profile.update(new MemberProfileRequest("Ada", "Lovelace", " "));
    assertEquals(null, profile.getPhoneNumber());
  }
}
