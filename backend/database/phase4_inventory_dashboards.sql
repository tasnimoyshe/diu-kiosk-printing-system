USE kiosk_db;

CREATE TABLE IF NOT EXISTS inventory_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    archived BIT(1) NOT NULL DEFAULT b'0',
    deleted BIT(1) NOT NULL DEFAULT b'0',
    merged_into_category_id BIGINT NULL,
    merged_at DATETIME(6) NULL,
    merged_by_agent_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_inventory_categories_normalized_name
        UNIQUE (normalized_name),

    CONSTRAINT fk_inventory_categories_merged_into
        FOREIGN KEY (merged_into_category_id)
        REFERENCES inventory_categories (id),

    CONSTRAINT fk_inventory_categories_merged_by_agent
        FOREIGN KEY (merged_by_agent_id)
        REFERENCES user_accounts (id),

    INDEX idx_inventory_categories_active_name (
        deleted,
        archived,
        normalized_name
    ),

    INDEX idx_inventory_categories_merged_into (
        merged_into_category_id
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    normalized_name VARCHAR(120) NOT NULL,
    current_price DECIMAL(12,2) NOT NULL,
    archived BIT(1) NOT NULL DEFAULT b'0',
    created_by_agent_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_inventory_items_category_name
        UNIQUE (category_id, normalized_name),

    CONSTRAINT fk_inventory_items_category
        FOREIGN KEY (category_id)
        REFERENCES inventory_categories (id),

    CONSTRAINT fk_inventory_items_created_by_agent
        FOREIGN KEY (created_by_agent_id)
        REFERENCES user_accounts (id),

    INDEX idx_inventory_items_category_archived (
        category_id,
        archived
    ),

    INDEX idx_inventory_items_name (
        normalized_name
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_item_price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    previous_price DECIMAL(12,2) NOT NULL,
    new_price DECIMAL(12,2) NOT NULL,
    changed_by_agent_id BIGINT NOT NULL,
    changed_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_inventory_price_history_item
        FOREIGN KEY (item_id)
        REFERENCES inventory_items (id),

    CONSTRAINT fk_inventory_price_history_agent
        FOREIGN KEY (changed_by_agent_id)
        REFERENCES user_accounts (id),

    INDEX idx_inventory_item_price_history_item_changed (
        item_id,
        changed_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    item_name_snapshot VARCHAR(120) NOT NULL,
    category_id_snapshot BIGINT NOT NULL,
    category_name_snapshot VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    agent_account_id BIGINT NOT NULL,
    reason VARCHAR(300) NULL,
    transacted_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_inventory_transactions_item
        FOREIGN KEY (item_id)
        REFERENCES inventory_items (id),

    CONSTRAINT fk_inventory_transactions_agent
        FOREIGN KEY (agent_account_id)
        REFERENCES user_accounts (id),

    INDEX idx_inventory_transactions_item_time (
        item_id,
        transacted_at
    ),

    INDEX idx_inventory_transactions_agent_time (
        agent_account_id,
        transacted_at
    ),

    INDEX idx_inventory_transactions_type_time (
        transaction_type,
        transacted_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_category_merge_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_category_id BIGINT NOT NULL,
    source_category_name VARCHAR(100) NOT NULL,
    destination_category_id BIGINT NOT NULL,
    destination_category_name VARCHAR(100) NOT NULL,
    moved_item_count INT NOT NULL,
    merged_by_agent_id BIGINT NOT NULL,
    merged_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_inventory_category_merge_source
        FOREIGN KEY (source_category_id)
        REFERENCES inventory_categories (id),

    CONSTRAINT fk_inventory_category_merge_destination
        FOREIGN KEY (destination_category_id)
        REFERENCES inventory_categories (id),

    CONSTRAINT fk_inventory_category_merge_agent
        FOREIGN KEY (merged_by_agent_id)
        REFERENCES user_accounts (id),

    INDEX idx_inventory_category_merge_source (
        source_category_id,
        merged_at
    ),

    INDEX idx_inventory_category_merge_destination (
        destination_category_id,
        merged_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'inventory_categories',
      'inventory_items',
      'inventory_item_price_history',
      'inventory_transactions',
      'inventory_category_merge_history'
  )
ORDER BY table_name;