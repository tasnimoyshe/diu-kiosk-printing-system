package com.print.kiosk.repository;

import com.print.kiosk.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select refreshToken
            from RefreshToken refreshToken
            join fetch refreshToken.userAccount
            where refreshToken.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    boolean existsByTokenHash(String tokenHash);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            update RefreshToken refreshToken
            set refreshToken.revokedAt = :revokedAt
            where refreshToken.userAccount.id = :accountId
              and refreshToken.revokedAt is null
            """)
    int revokeAllActiveByAccountId(
            @Param("accountId") Long accountId,
            @Param("revokedAt") Instant revokedAt
    );
}