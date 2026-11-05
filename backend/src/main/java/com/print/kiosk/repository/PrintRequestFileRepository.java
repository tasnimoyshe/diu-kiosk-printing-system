package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintRequestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrintRequestFileRepository
        extends JpaRepository<PrintRequestFile, Long> {

    @Query("""
            select
                file.id as id,
                file.originalFileName as originalFileName,
                file.fileType as fileType,
                file.mimeType as mimeType,
                file.fileSizeBytes as fileSizeBytes,
                file.pageCount as pageCount,
                file.physicalSheetsPerCopy as physicalSheetsPerCopy,
                file.totalPhysicalSheets as totalPhysicalSheets,
                file.copies as copies,
                file.appliedRate as appliedRate,
                file.subtotal as subtotal,
                file.calculatedAt as calculatedAt,
                file.uploadedAt as uploadedAt
            from PrintRequestFile file
            where file.printRequest.id = :requestId
            order by file.id asc
            """)
    List<PrintRequestFileMetadataProjection>
    findMetadataByPrintRequestId(
            @Param("requestId") Long requestId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            update PrintRequestFile file
            set file.deletionEligibleAt = :eligibleAt
            where file.printRequest.id = :requestId
              and file.deletionEligibleAt is null
            """)
    int markDeletionEligibleByRequestId(
            @Param("requestId") Long requestId,
            @Param("eligibleAt") LocalDateTime eligibleAt
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            update PrintRequestFile file
            set file.deletionEligibleAt = null
            where file.printRequest.id = :requestId
              and file.binaryDeletedAt is null
            """)
    int clearDeletionEligibilityByRequestId(
            @Param("requestId") Long requestId
    );

    @Query("""
            select file
            from PrintRequestFile file
            join fetch file.printRequest
            where file.id = :fileId
              and file.printRequest.id = :requestId
            """)
    Optional<PrintRequestFile> findByIdAndRequestId(
            @Param("fileId") Long fileId,
            @Param("requestId") Long requestId
    );

    List<PrintRequestFile>
    findTop100ByDeletionEligibleAtLessThanEqualAndBinaryDeletedAtIsNullAndFileDataIsNotNullOrderByDeletionEligibleAtAsc(
            LocalDateTime now
    );

    boolean existsByPrintRequest_IdAndFileDataIsNull(
            Long requestId
    );

    List<PrintRequestFile>
    findByPrintRequest_IdOrderByIdAsc(
            Long requestId
    );

    long countByPrintRequest_Id(
            Long requestId
    );
}