package com.print.kiosk.repository;

import com.print.kiosk.entity.InventoryCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryCategoryRepository
        extends JpaRepository<InventoryCategory, Long> {

    Optional<InventoryCategory> findByNormalizedName(String normalizedName);

    Optional<InventoryCategory> findByNormalizedNameAndDeletedFalse(
            String normalizedName
    );

    boolean existsByNormalizedName(String normalizedName);

    boolean existsByNormalizedNameAndDeletedFalse(
            String normalizedName
    );

    boolean existsByNormalizedNameAndDeletedFalseAndIdNot(
            String normalizedName,
            Long id
    );

    List<InventoryCategory> findAllByDeletedFalseAndArchivedFalseOrderByNameAsc();

    List<InventoryCategory> findAllByDeletedFalseOrderByArchivedAscNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select category from InventoryCategory category where category.id = :id")
    Optional<InventoryCategory> findByIdForUpdate(@Param("id") Long id);
}