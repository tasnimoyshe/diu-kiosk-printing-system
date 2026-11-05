package com.print.kiosk.repository;

import com.print.kiosk.entity.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from InventoryItem item join fetch item.category where item.id = :id")
    Optional<InventoryItem> findByIdForUpdate(@Param("id") Long id);

    @Query("select item from InventoryItem item join fetch item.category where item.id = :id")
    Optional<InventoryItem> findDetailedById(@Param("id") Long id);

    boolean existsByCategory_IdAndNormalizedName(
            Long categoryId,
            String normalizedName
    );

    boolean existsByCategory_IdAndNormalizedNameAndIdNot(
            Long categoryId,
            String normalizedName,
            Long id
    );

    long countByCategory_IdAndArchivedFalse(Long categoryId);

    long countByCategory_Id(Long categoryId);

    List<InventoryItem> findAllByCategory_IdOrderByNameAsc(Long categoryId);

    @Query(
            value = """
                    select item
                    from InventoryItem item
                    join fetch item.category category
                    where category.deleted = false
                      and (:categoryId is null or category.id = :categoryId)
                      and (:includeArchived = true or item.archived = false)
                      and (
                            :search is null
                            or lower(item.name) like lower(concat('%', :search, '%'))
                            or lower(category.name) like lower(concat('%', :search, '%'))
                      )
                    order by category.name asc, item.name asc
                    """,
            countQuery = """
                    select count(item)
                    from InventoryItem item
                    join item.category category
                    where category.deleted = false
                      and (:categoryId is null or category.id = :categoryId)
                      and (:includeArchived = true or item.archived = false)
                      and (
                            :search is null
                            or lower(item.name) like lower(concat('%', :search, '%'))
                            or lower(category.name) like lower(concat('%', :search, '%'))
                      )
                    """
    )
    Page<InventoryItem> search(
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            @Param("includeArchived") boolean includeArchived,
            Pageable pageable
    );
}