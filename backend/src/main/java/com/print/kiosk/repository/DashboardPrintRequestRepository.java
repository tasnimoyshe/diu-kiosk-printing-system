package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DashboardPrintRequestRepository
        extends Repository<PrintRequest, Long> {

    long countByStatusIn(Collection<PrintRequestStatus> statuses);

    long countByStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            PrintRequestStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            select coalesce(sum(request.totalDocumentPages), 0)
            from PrintRequest request
            where request.status = com.print.kiosk.entity.PrintRequestStatus.COMPLETED
              and request.printColor = :color
              and request.completedAt >= :start
              and request.completedAt < :end
            """)
    Long sumDocumentPagesByColor(
            @Param("color") PrintColor color,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select coalesce(sum(request.totalPhysicalSheets), 0)
            from PrintRequest request
            where request.status = com.print.kiosk.entity.PrintRequestStatus.COMPLETED
              and request.printColor = :color
              and request.completedAt >= :start
              and request.completedAt < :end
            """)
    Long sumPhysicalSheetsByColor(
            @Param("color") PrintColor color,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select request
            from PrintRequest request
            join fetch request.assignedAgent agent
            where request.status = com.print.kiosk.entity.PrintRequestStatus.COMPLETED
              and request.completedAt >= :start
              and request.completedAt < :end
            """)
    List<PrintRequest> findCompletedWithAssignedAgentBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select count(request)
            from PrintRequest request
            where request.assignedAgent.id = :agentId
              and request.status = com.print.kiosk.entity.PrintRequestStatus.COMPLETED
              and request.completedAt >= :start
              and request.completedAt < :end
            """)
    long countCompletedByAgentBetween(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}