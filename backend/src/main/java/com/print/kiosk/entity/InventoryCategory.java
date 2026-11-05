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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "inventory_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_categories_normalized_name",
                columnNames = "normalized_name"
        ),
        indexes = {
                @Index(
                        name = "idx_inventory_categories_active_name",
                        columnList = "deleted, archived, normalized_name"
                ),
                @Index(
                        name = "idx_inventory_categories_merged_into",
                        columnList = "merged_into_category_id"
                )
        }
)
public class InventoryCategory {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_into_category_id")
    private InventoryCategory mergedIntoCategory;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_by_agent_id")
    private UserAccount mergedByAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now(DHAKA_ZONE);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public InventoryCategory getMergedIntoCategory() {
        return mergedIntoCategory;
    }

    public void setMergedIntoCategory(InventoryCategory mergedIntoCategory) {
        this.mergedIntoCategory = mergedIntoCategory;
    }

    public LocalDateTime getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(LocalDateTime mergedAt) {
        this.mergedAt = mergedAt;
    }

    public UserAccount getMergedByAgent() {
        return mergedByAgent;
    }

    public void setMergedByAgent(UserAccount mergedByAgent) {
        this.mergedByAgent = mergedByAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}