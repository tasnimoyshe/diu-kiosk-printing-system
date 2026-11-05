package com.print.kiosk.repository;

import com.print.kiosk.entity.AgentIdCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentIdCardRepository
        extends JpaRepository<AgentIdCard, Long> {

    Optional<AgentIdCard> findByUserAccountId(Long userAccountId);

    boolean existsByUserAccountId(Long userAccountId);
}