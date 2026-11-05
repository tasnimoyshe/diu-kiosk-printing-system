USE kiosk_db;

-- =========================================================
-- PHASE 2 — PRICING, PRINT REQUESTS AND FILES
-- MySQL 8 compatible and safe to rerun
-- =========================================================

CREATE TABLE IF NOT EXISTS printing_prices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(30) NOT NULL,
    color_single_sided DECIMAL(12,2) NOT NULL,
    black_white_single_sided DECIMAL(12,2) NOT NULL,
    color_double_sided DECIMAL(12,2) NOT NULL,
    black_white_double_sided DECIMAL(12,2) NOT NULL,
    revision BIGINT NOT NULL,
    updated_by_account_id BIGINT NULL,
    updated_by_agent_id VARCHAR(50) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    row_version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_printing_prices_config_key
        UNIQUE (config_key)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

INSERT INTO printing_prices (
    config_key,
    color_single_sided,
    black_white_single_sided,
    color_double_sided,
    black_white_double_sided,
    revision,
    created_at,
    updated_at,
    row_version
)
SELECT
    'GLOBAL',
    10.00,
    2.00,
    18.00,
    3.50,
    1,
    NOW(6),
    NOW(6),
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM printing_prices
    WHERE config_key = 'GLOBAL'
);

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    price_type VARCHAR(40) NOT NULL,
    previous_price DECIMAL(12,2) NOT NULL,
    new_price DECIMAL(12,2) NOT NULL,
    pricing_revision BIGINT NOT NULL,
    changed_by_account_id BIGINT NOT NULL,
    changed_by_agent_id VARCHAR(50) NOT NULL,
    changed_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_price_history_changed_at (
        changed_at
    ),

    INDEX idx_price_history_revision (
        pricing_revision
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- IDEMPOTENT MIGRATION HELPERS
-- =========================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS p2_add_column_if_missing$$
CREATE PROCEDURE p2_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @p2_sql = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD COLUMN `', p_column_name,
            '` ', p_column_definition
        );

        PREPARE p2_statement FROM @p2_sql;
        EXECUTE p2_statement;
        DEALLOCATE PREPARE p2_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS p2_add_index_if_missing$$
CREATE PROCEDURE p2_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_columns TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @p2_sql = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD INDEX `', p_index_name,
            '` (', p_index_columns, ')'
        );

        PREPARE p2_statement FROM @p2_sql;
        EXECUTE p2_statement;
        DEALLOCATE PREPARE p2_statement;
    END IF;
END$$

DELIMITER ;

-- =========================================================
-- EXTEND EXISTING PRINT REQUESTS TABLE
-- Add as nullable first so old rows can be backfilled safely.
-- =========================================================

CALL p2_add_column_if_missing(
    'print_requests',
    'pricing_config_id',
    'BIGINT NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'pricing_revision',
    'BIGINT NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'applied_rate',
    'DECIMAL(12,2) NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'total_document_pages',
    'BIGINT NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'total_physical_sheets',
    'BIGINT NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'calculated_at',
    'DATETIME(6) NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'expires_at',
    'DATETIME(6) NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'cancelled_at',
    'DATETIME(6) NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'cancellation_reason',
    'VARCHAR(200) NULL'
);

CALL p2_add_column_if_missing(
    'print_requests',
    'updated_at',
    'DATETIME(6) NULL'
);

SET SQL_SAFE_UPDATES = 0;

UPDATE print_requests
SET
    pricing_config_id = COALESCE(
        pricing_config_id,
        (
            SELECT id
            FROM printing_prices
            WHERE config_key = 'GLOBAL'
            LIMIT 1
        )
    ),
    pricing_revision = COALESCE(
        pricing_revision,
        1
    ),
    applied_rate = COALESCE(
        applied_rate,
        0.00
    ),
    total_document_pages = COALESCE(
        total_document_pages,
        0
    ),
    total_physical_sheets = COALESCE(
        total_physical_sheets,
        0
    ),
    calculated_at = COALESCE(
        calculated_at,
        created_at,
        NOW(6)
    ),
    expires_at = COALESCE(
        expires_at,
        DATE_ADD(
            COALESCE(created_at, NOW(6)),
            INTERVAL 12 HOUR
        )
    ),
    updated_at = COALESCE(
        updated_at,
        created_at,
        NOW(6)
    );

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE print_requests
    MODIFY COLUMN pricing_config_id BIGINT NOT NULL,
    MODIFY COLUMN pricing_revision BIGINT NOT NULL,
    MODIFY COLUMN applied_rate DECIMAL(12,2) NOT NULL,
    MODIFY COLUMN total_document_pages BIGINT NOT NULL,
    MODIFY COLUMN total_physical_sheets BIGINT NOT NULL,
    MODIFY COLUMN calculated_at DATETIME(6) NOT NULL,
    MODIFY COLUMN expires_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

CALL p2_add_index_if_missing(
    'print_requests',
    'idx_print_requests_status_created',
    '`status`, `created_at`'
);

CALL p2_add_index_if_missing(
    'print_requests',
    'idx_print_requests_expires_at',
    '`expires_at`'
);

-- =========================================================
-- FILE STORAGE
-- =========================================================

CREATE TABLE IF NOT EXISTS print_request_files (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    page_count INT NOT NULL,
    physical_sheets_per_copy INT NOT NULL,
    total_physical_sheets BIGINT NOT NULL,
    copies INT NOT NULL,
    applied_rate DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    calculated_at DATETIME(6) NOT NULL,
    uploaded_at DATETIME(6) NOT NULL,
    deletion_eligible_at DATETIME(6) NULL,
    binary_deleted_at DATETIME(6) NULL,
    file_data LONGBLOB NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_print_request_files_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    INDEX idx_print_request_files_request (
        print_request_id
    ),

    INDEX idx_print_request_files_deletion (
        deletion_eligible_at,
        binary_deleted_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- STATUS HISTORY
-- =========================================================

CREATE TABLE IF NOT EXISTS print_request_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    actor_type VARCHAR(20) NOT NULL,
    actor_account_id BIGINT NULL,
    reason VARCHAR(255) NULL,
    changed_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_print_request_status_history_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    INDEX idx_print_request_status_history_request_changed (
        print_request_id,
        changed_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Remove temporary helpers.
DROP PROCEDURE IF EXISTS p2_add_column_if_missing;
DROP PROCEDURE IF EXISTS p2_add_index_if_missing;

-- =========================================================
-- VERIFICATION
-- =========================================================

SELECT
    column_name,
    column_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'print_requests'
  AND column_name IN (
      'pricing_config_id',
      'pricing_revision',
      'applied_rate',
      'total_document_pages',
      'total_physical_sheets',
      'calculated_at',
      'expires_at',
      'updated_at'
  )
ORDER BY column_name;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'printing_prices',
      'price_history',
      'print_request_files',
      'print_request_status_history'
  )
ORDER BY table_name;
