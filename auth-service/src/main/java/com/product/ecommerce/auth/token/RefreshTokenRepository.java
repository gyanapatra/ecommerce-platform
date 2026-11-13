package com.product.ecommerce.auth.token;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT rt
        FROM RefreshToken rt
        WHERE rt.tokenHash = :tokenHash
        """)
    Optional<RefreshToken> findByTokenHashForUpdate(
        @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT rt
        FROM RefreshToken rt
        WHERE rt.familyId = :familyId
        """)
    List<RefreshToken> findAllByFamilyIdForUpdate(
        @Param("familyId") UUID familyId
    );

    List<RefreshToken> findAllByFamilyId(UUID familyId);

    List<RefreshToken> findAllByUser_Id(UUID userId);
}