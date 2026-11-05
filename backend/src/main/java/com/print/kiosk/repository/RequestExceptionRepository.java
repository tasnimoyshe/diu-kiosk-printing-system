package com.print.kiosk.repository;
import com.print.kiosk.entity.RequestException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RequestExceptionRepository extends JpaRepository<RequestException,Long>{ Page<RequestException> findByResolvedAtIsNullOrderByCreatedAtAsc(Pageable pageable); List<RequestException> findByPrintRequest_IdAndResolvedAtIsNull(Long requestId); }