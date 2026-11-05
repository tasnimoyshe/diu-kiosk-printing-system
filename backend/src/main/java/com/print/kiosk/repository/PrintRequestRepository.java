package com.print.kiosk.repository;

import com.print.kiosk.entity.PaymentStatus;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PrintRequestRepository extends JpaRepository<PrintRequest, Long> {

    Optional<PrintRequest> findByTrackingNumber(String trackingNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from PrintRequest request where request.trackingNumber = :trackingNumber")
    Optional<PrintRequest> findByTrackingNumberForUpdate(
            @Param("trackingNumber") String trackingNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select request
            from PrintRequest request
            left join fetch request.assignedAgent
            where request.id = :requestId
            """)
    Optional<PrintRequest> findByIdForUpdate(
            @Param("requestId") Long requestId
    );

    boolean existsByTrackingNumber(String trackingNumber);

    long countByTrackingNumberStartingWith(String dailyPrefix);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PrintRequest> findTop100ByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
            PrintRequestStatus status,
            LocalDateTime expiresAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select request
            from PrintRequest request
            where request.status = com.print.kiosk.entity.PrintRequestStatus.PAID
              and request.paymentStatus = com.print.kiosk.entity.PaymentStatus.PAID
              and request.assignedAgent is null
            order by request.createdAt asc
            """)
    List<PrintRequest> findNextPaidAvailableForClaim(Pageable pageable);

    @Query(
            value = """
                    select request
                    from PrintRequest request
                    left join request.assignedAgent agent
                    where request.status in :statuses
                      and (:paymentStatus is null or request.paymentStatus = :paymentStatus)
                      and (:requestStatus is null or request.status = :requestStatus)
                      and (
                            :search is null
                            or lower(request.fullName) like lower(concat('%', :search, '%'))
                            or lower(request.registrationNumber) like lower(concat('%', :search, '%'))
                            or lower(request.trackingNumber) like lower(concat('%', :search, '%'))
                            or lower(coalesce(agent.agentId, '')) like lower(concat('%', :search, '%'))
                      )
                    order by
                      case
                        when request.status in (
                            com.print.kiosk.entity.PrintRequestStatus.CLAIMED,
                            com.print.kiosk.entity.PrintRequestStatus.DOWNLOADING,
                            com.print.kiosk.entity.PrintRequestStatus.PRINTING
                        ) then 0
                        when request.paymentStatus = com.print.kiosk.entity.PaymentStatus.PAID then 1
                        else 2
                      end,
                      coalesce(request.claimedAt, request.createdAt),
                      request.createdAt
                    """,
            countQuery = """
                    select count(request)
                    from PrintRequest request
                    left join request.assignedAgent agent
                    where request.status in :statuses
                      and (:paymentStatus is null or request.paymentStatus = :paymentStatus)
                      and (:requestStatus is null or request.status = :requestStatus)
                      and (
                            :search is null
                            or lower(request.fullName) like lower(concat('%', :search, '%'))
                            or lower(request.registrationNumber) like lower(concat('%', :search, '%'))
                            or lower(request.trackingNumber) like lower(concat('%', :search, '%'))
                            or lower(coalesce(agent.agentId, '')) like lower(concat('%', :search, '%'))
                      )
                    """
    )
    Page<PrintRequest> findActiveQueue(
            @Param("statuses") Collection<PrintRequestStatus> statuses,
            @Param("search") String search,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("requestStatus") PrintRequestStatus requestStatus,
            Pageable pageable
    );
}