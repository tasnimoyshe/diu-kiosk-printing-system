package com.print.kiosk.repository;
import com.print.kiosk.entity.CashPaymentPin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;
public interface CashPaymentPinRepository extends JpaRepository<CashPaymentPin,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from CashPaymentPin p join fetch p.paymentRecord where p.printRequest.id=:requestId and p.usedAt is null and p.revokedAt is null order by p.createdAt desc") List<CashPaymentPin> findActiveByRequestForUpdate(@Param("requestId") Long requestId);
    @Query("select count(p)>0 from CashPaymentPin p where p.pinHash=:pinHash and p.usedAt is null and p.revokedAt is null and p.expiresAt>:now") boolean existsActivePinHash(@Param("pinHash") String pinHash,@Param("now") LocalDateTime now);
}