package com.product.ecommerce.member.domain;

import com.product.ecommerce.member.api.MemberProfileRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "member_profiles")
public class MemberProfile {
  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "member_number", nullable = false, unique = true, length = 40)
  private String memberNumber;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "phone_number", length = 32)
  private String phoneNumber;

  @Column(nullable = false, length = 24)
  private String tier;

  @Column(nullable = false, length = 24)
  private String status;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected MemberProfile() {}

  public MemberProfile(UUID userId, MemberProfileRequest request) {
    this.userId = userId;
    this.memberNumber = "MEM-" + UUID.randomUUID();
    this.tier = "STANDARD";
    this.status = "ACTIVE";
    this.joinedAt = Instant.now();
    update(request);
  }

  public void update(MemberProfileRequest request) {
    firstName = request.firstName().trim();
    lastName = request.lastName().trim();
    phoneNumber = request.phoneNumber() == null || request.phoneNumber().isBlank()
        ? null : request.phoneNumber().trim();
    updatedAt = Instant.now();
  }

  public UUID getUserId() { return userId; }
  public String getMemberNumber() { return memberNumber; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getPhoneNumber() { return phoneNumber; }
  public String getTier() { return tier; }
  public String getStatus() { return status; }
  public Instant getJoinedAt() { return joinedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}