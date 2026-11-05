package com.print.kiosk.repository;
import com.print.kiosk.entity.RequestClaim;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;
public interface RequestClaimRepository extends JpaRepository<RequestClaim,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from RequestClaim c where c.printRequest.id=:requestId and c.active=true") Optional<RequestClaim> findActiveByRequestIdForUpdate(@Param("requestId") Long requestId);
    boolean existsByAgent_IdAndActiveTrue(Long agentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) List<RequestClaim> findTop100ByActiveTrueAndLastActivityAtLessThanEqualOrderByLastActivityAtAsc(LocalDateTime cutoff);
}