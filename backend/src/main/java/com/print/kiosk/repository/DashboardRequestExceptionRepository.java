package com.print.kiosk.repository;

import com.print.kiosk.entity.RequestException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DashboardRequestExceptionRepository
        extends Repository<RequestException, Long> {

    @Query("""
            select count(exception)
            from RequestException exception
            where exception.agent.id = :agentId
              and exception.createdAt >= :start
              and exception.createdAt < :end
            """)
    long countByAgentBetween(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}