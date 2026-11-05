package com.print.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "inventory_transactions",
        indexes = {
                @Index(
                        name = "idx_inventory_transactions_item_time",
                        columnList = "item_id, transacted_at"
                ),
                @Index(
                        name = "idx_inventory_transactions_agent_time",
                        columnList = "agent_account_id, transacted_at"
                ),
                @Index(
                        name = "idx_inventory_transactions_type_time",
                        columnList = "transaction_type, transacted_at"
                )
        }
)
public class InventoryTransaction {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(name = "item_name_snapshot", nullable = false, length = 120)
    private String itemNameSnapshot;

    @Column(name = "category_id_snapshot", nullable = false)
    private Long categoryIdSnapshot;

    @Column(name = "category_name_snapshot", nullable = false, length = 100)
    private String categoryNameSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private InventoryTransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_account_id", nullable = false)
    private UserAccount agent;

    @Column(name = "reason", length = 300)
    private String reason;

    @Column(name = "transacted_at", nullable = false, updatable = false)
    private LocalDateTime transactedAt;

    @PrePersist
    public void beforeInsert() {
        if (transactedAt == null) {
            transactedAt = LocalDateTime.now(DHAKA_ZONE);
        }
    }

    public Long getId() {
        return id;
    }

    public InventoryItem getItem() {
        return item;
    }

    public void setItem(InventoryItem item) {
        this.item = item;
    }

    public String getItemNameSnapshot() {
        return itemNameSnapshot;
    }

    public void setItemNameSnapshot(String itemNameSnapshot) {
        this.itemNameSnapshot = itemNameSnapshot;
    }

    public Long getCategoryIdSnapshot() {
        return categoryIdSnapshot;
    }

    public void setCategoryIdSnapshot(Long categoryIdSnapshot) {
        this.categoryIdSnapshot = categoryIdSnapshot;
    }

    public String getCategoryNameSnapshot() {
        return categoryNameSnapshot;
    }

    public void setCategoryNameSnapshot(String categoryNameSnapshot) {
        this.categoryNameSnapshot = categoryNameSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public InventoryTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(InventoryTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public UserAccount getAgent() {
        return agent;
    }

    public void setAgent(UserAccount agent) {
        this.agent = agent;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTransactedAt() {
        return transactedAt;
    }
}