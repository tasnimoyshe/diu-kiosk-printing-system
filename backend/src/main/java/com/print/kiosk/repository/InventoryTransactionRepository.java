package com.print.kiosk.repository;

import com.print.kiosk.entity.InventoryTransaction;
import com.print.kiosk.entity.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    @Query("select coalesce(sum(transaction.quantity), 0) from InventoryTransaction transaction where transaction.item.id = :itemId")
    Long sumQuantityByItemId(@Param("itemId") Long itemId);

    @Query("select coalesce(sum(transaction.totalAmount), 0) from InventoryTransaction transaction where transaction.item.id = :itemId")
    BigDecimal sumAmountByItemId(@Param("itemId") Long itemId);

    @Query("""
            select coalesce(sum(transaction.totalAmount), 0)
            from InventoryTransaction transaction
            where transaction.transactedAt >= :start
              and transaction.transactedAt < :end
            """)
    BigDecimal sumAmountBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select coalesce(sum(transaction.totalAmount), 0)
            from InventoryTransaction transaction
            where transaction.agent.id = :agentId
              and transaction.transactedAt >= :start
              and transaction.transactedAt < :end
            """)
    BigDecimal sumAmountByAgentBetween(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByAgent_IdAndTransactedAtGreaterThanEqualAndTransactedAtLessThan(
            Long agentId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            select
                transaction.categoryNameSnapshot as label,
                coalesce(sum(transaction.quantity), 0) as netQuantity,
                coalesce(sum(transaction.totalAmount), 0) as netAmount
            from InventoryTransaction transaction
            where transaction.transactedAt >= :start
              and transaction.transactedAt < :end
            group by transaction.categoryNameSnapshot
            order by sum(transaction.totalAmount) desc, transaction.categoryNameSnapshot asc
            """)
    List<CategorySalesProjection> summarizeByCategory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select
                transaction.itemNameSnapshot as label,
                coalesce(sum(transaction.quantity), 0) as netQuantity,
                coalesce(sum(transaction.totalAmount), 0) as netAmount
            from InventoryTransaction transaction
            where transaction.transactedAt >= :start
              and transaction.transactedAt < :end
            group by transaction.itemNameSnapshot
            order by sum(transaction.totalAmount) desc, transaction.itemNameSnapshot asc
            """)
    List<ItemSalesProjection> summarizeByItem(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(
            value = """
                    select transaction
                    from InventoryTransaction transaction
                    join fetch transaction.item item
                    join fetch transaction.agent agent
                    where (:itemId is null or item.id = :itemId)
                      and (:categoryId is null or transaction.categoryIdSnapshot = :categoryId)
                      and (:transactionType is null or transaction.transactionType = :transactionType)
                      and (:agentId is null or agent.id = :agentId)
                      and (:start is null or transaction.transactedAt >= :start)
                      and (:end is null or transaction.transactedAt < :end)
                    order by transaction.transactedAt desc, transaction.id desc
                    """,
            countQuery = """
                    select count(transaction)
                    from InventoryTransaction transaction
                    join transaction.item item
                    join transaction.agent agent
                    where (:itemId is null or item.id = :itemId)
                      and (:categoryId is null or transaction.categoryIdSnapshot = :categoryId)
                      and (:transactionType is null or transaction.transactionType = :transactionType)
                      and (:agentId is null or agent.id = :agentId)
                      and (:start is null or transaction.transactedAt >= :start)
                      and (:end is null or transaction.transactedAt < :end)
                    """
    )
    Page<InventoryTransaction> search(
            @Param("itemId") Long itemId,
            @Param("categoryId") Long categoryId,
            @Param("transactionType") InventoryTransactionType transactionType,
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}