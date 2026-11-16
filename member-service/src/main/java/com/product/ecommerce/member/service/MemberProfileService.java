package com.product.ecommerce.member.service;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.member.api.MemberProfileRequest;
import com.product.ecommerce.member.api.MemberProfileResponse;
import com.product.ecommerce.member.domain.MemberProfile;
import com.product.ecommerce.member.domain.MemberProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberProfileService {
  private final MemberProfileRepository repository;

  public MemberProfileService(MemberProfileRepository repository) { this.repository = repository; }

  @Transactional(readOnly = true)
  public MemberProfileResponse get(UUID userId) {
    return repository.findById(userId).map(MemberProfileResponse::from)
        .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
  }

  @Transactional
  public MemberProfileResponse upsert(UUID userId, MemberProfileRequest request) {
    MemberProfile profile = repository.findById(userId)
        .map(existing -> { existing.update(request); return existing; })
        .orElseGet(() -> new MemberProfile(userId, request));
    return MemberProfileResponse.from(repository.save(profile));
  }
}