package com.product.ecommerce.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, UUID> {}