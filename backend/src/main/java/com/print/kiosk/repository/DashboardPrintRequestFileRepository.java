package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.entity.PrintRequestFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

public interface DashboardPrintRequestFileRepository
        extends Repository<PrintRequestFile, Long> {

    @Query("""
            select count(file)
            from PrintRequestFile file
            where file.printRequest.status = com.print.kiosk.entity.PrintRequestStatus.COMPLETED
              and file.printRequest.completedAt >= :start
              and file.printRequest.completedAt < :end
              and file.fileType in :types
            """)
    long countCompletedFilesByTypes(
            @Param("types") Collection<PrintFileType> types,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}