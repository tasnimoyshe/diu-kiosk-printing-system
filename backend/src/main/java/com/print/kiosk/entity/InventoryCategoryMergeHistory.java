package com.print.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "inventory_category_merge_history",
        indexes = {
                @Index(
                        name = "idx_inventory_category_merge_source",
                        columnList = "source_category_id, merged_at"
                ),
                @Index(
                        name = "idx_inventory_category_merge_destination",
                        columnList = "destination_category_id, merged_at"
                )
        }
)
public class InventoryCategoryMergeHistory {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_category_id", nullable = false)
    private InventoryCategory sourceCategory;

    @Column(name = "source_category_name", nullable = false, length = 100)
    private String sourceCategoryName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_category_id", nullable = false)
    private InventoryCategory destinationCategory;

    @Column(name = "destination_category_name", nullable = false, length = 100)
    private String destinationCategoryName;

    @Column(name = "moved_item_count", nullable = false)
    private int movedItemCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merged_by_agent_id", nullable = false)
    private UserAccount mergedByAgent;

    @Column(name = "merged_at", nullable = false, updatable = false)
    private LocalDateTime mergedAt;

    @PrePersist
    public void beforeInsert() {
        if (mergedAt == null) {
            mergedAt = LocalDateTime.now(DHAKA_ZONE);
        }
    }

    public Long getId() {
        return id;
    }

    public InventoryCategory getSourceCategory() {
        return sourceCategory;
    }

    public void setSourceCategory(InventoryCategory sourceCategory) {
        this.sourceCategory = sourceCategory;
    }

    public String getSourceCategoryName() {
        return sourceCategoryName;
    }

    public void setSourceCategoryName(String sourceCategoryName) {
        this.sourceCategoryName = sourceCategoryName;
    }

    public InventoryCategory getDestinationCategory() {
        return destinationCategory;
    }

    public void setDestinationCategory(InventoryCategory destinationCategory) {
        this.destinationCategory = destinationCategory;
    }

    public String getDestinationCategoryName() {
        return destinationCategoryName;
    }

    public void setDestinationCategoryName(String destinationCategoryName) {
        this.destinationCategoryName = destinationCategoryName;
    }

    public int getMovedItemCount() {
        return movedItemCount;
    }

    public void setMovedItemCount(int movedItemCount) {
        this.movedItemCount = movedItemCount;
    }

    public UserAccount getMergedByAgent() {
        return mergedByAgent;
    }

    public void setMergedByAgent(UserAccount mergedByAgent) {
        this.mergedByAgent = mergedByAgent;
    }

    public LocalDateTime getMergedAt() {
        return mergedAt;
    }
}