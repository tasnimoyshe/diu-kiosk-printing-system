USE kiosk_db;

-- =========================================================
-- PHASE 3 — QUEUE, PAYMENT AND PRINTING
-- MySQL 8 compatible and safe to rerun
-- =========================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS p3_add_column_if_missing$$
CREATE PROCEDURE p3_add_column_if_missing(
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
        SET @p3_sql = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD COLUMN `', p_column_name,
            '` ', p_column_definition
        );

        PREPARE p3_statement FROM @p3_sql;
        EXECUTE p3_statement;
        DEALLOCATE PREPARE p3_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS p3_add_index_if_missing$$
CREATE PROCEDURE p3_add_index_if_missing(
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
        SET @p3_sql = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD INDEX `', p_index_name,
            '` (', p_index_columns, ')'
        );

        PREPARE p3_statement FROM @p3_sql;
        EXECUTE p3_statement;
        DEALLOCATE PREPARE p3_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS p3_add_foreign_key_if_missing$$
CREATE PROCEDURE p3_add_foreign_key_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_constraint_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @p3_sql = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD CONSTRAINT `', p_constraint_name,
            '` ', p_constraint_definition
        );

        PREPARE p3_statement FROM @p3_sql;
        EXECUTE p3_statement;
        DEALLOCATE PREPARE p3_statement;
    END IF;
END$$

DELIMITER ;

-- =========================================================
-- EXTEND PRINT REQUESTS
-- =========================================================

CALL p3_add_column_if_missing(
    'print_requests',
    'payment_status',
    'VARCHAR(20) NOT NULL DEFAULT ''PENDING'''
);

CALL p3_add_column_if_missing(
    'print_requests',
    'assigned_agent_id',
    'BIGINT NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'claimed_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'claim_last_activity_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'printing_started_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'completed_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'paused_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'reopened_at',
    'DATETIME(6) NULL'
);

CALL p3_add_column_if_missing(
    'print_requests',
    'version',
    'BIGINT NOT NULL DEFAULT 0'
);

SET SQL_SAFE_UPDATES = 0;

UPDATE print_requests
SET payment_status = 'PAID'
WHERE status IN (
    'PAID',
    'CLAIMED',
    'DOWNLOADING',
    'PRINTING',
    'COMPLETED'
)
AND payment_status = 'PENDING';

SET SQL_SAFE_UPDATES = 1;

CALL p3_add_index_if_missing(
    'print_requests',
    'idx_print_requests_status_payment_created',
    '`status`, `payment_status`, `created_at`'
);

CALL p3_add_index_if_missing(
    'print_requests',
    'idx_print_requests_assigned_agent',
    '`assigned_agent_id`'
);

CALL p3_add_index_if_missing(
    'print_requests',
    'idx_print_requests_claim_activity',
    '`claim_last_activity_at`'
);

CALL p3_add_foreign_key_if_missing(
    'print_requests',
    'fk_print_requests_assigned_agent',
    'FOREIGN KEY (`assigned_agent_id`) REFERENCES `user_accounts` (`id`)'
);

-- Binary data can be deleted later while metadata remains.
ALTER TABLE print_request_files
    MODIFY COLUMN file_data LONGBLOB NULL;

-- =========================================================
-- PAYMENT RECORDS
-- =========================================================

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    failure_reason VARCHAR(255) NULL,
    external_transaction_id VARCHAR(120) NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_payment_records_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    INDEX idx_payment_records_request (
        print_request_id
    ),

    INDEX idx_payment_records_status_created (
        status,
        created_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- CASH PAYMENT PINS
-- =========================================================

CREATE TABLE IF NOT EXISTS cash_payment_pins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    payment_record_id BIGINT NOT NULL,
    pin_hash VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    blocked_until DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_cash_payment_pins_payment_record
        UNIQUE (payment_record_id),

    CONSTRAINT fk_cash_pins_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    CONSTRAINT fk_cash_pins_payment
        FOREIGN KEY (payment_record_id)
        REFERENCES payment_records (id),

    INDEX idx_cash_pins_request_created (
        print_request_id,
        created_at
    ),

    INDEX idx_cash_pins_hash_active (
        pin_hash,
        expires_at,
        used_at,
        revoked_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- REQUEST CLAIMS
-- =========================================================

CREATE TABLE IF NOT EXISTS request_claims (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    agent_account_id BIGINT NOT NULL,
    claimed_at DATETIME(6) NOT NULL,
    last_activity_at DATETIME(6) NOT NULL,
    released_at DATETIME(6) NULL,
    release_reason VARCHAR(30) NULL,
    release_note VARCHAR(255) NULL,
    active BIT NOT NULL DEFAULT b'1',
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_request_claims_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    CONSTRAINT fk_request_claims_agent
        FOREIGN KEY (agent_account_id)
        REFERENCES user_accounts (id),

    INDEX idx_request_claims_request_active (
        print_request_id,
        active
    ),

    INDEX idx_request_claims_agent_active (
        agent_account_id,
        active
    ),

    INDEX idx_request_claims_activity (
        active,
        last_activity_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- REQUEST EXCEPTIONS
-- =========================================================

CREATE TABLE IF NOT EXISTS request_exceptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    agent_account_id BIGINT NOT NULL,
    exception_type VARCHAR(40) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    resolved_by_account_id BIGINT NULL,
    resolution_note VARCHAR(500) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_request_exceptions_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    CONSTRAINT fk_request_exceptions_agent
        FOREIGN KEY (agent_account_id)
        REFERENCES user_accounts (id),

    INDEX idx_request_exceptions_request (
        print_request_id,
        created_at
    ),

    INDEX idx_request_exceptions_unresolved (
        resolved_at,
        created_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- FILE DOWNLOAD AUDIT EVENTS
-- =========================================================

CREATE TABLE IF NOT EXISTS file_download_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    print_request_id BIGINT NOT NULL,
    print_request_file_id BIGINT NOT NULL,
    agent_account_id BIGINT NOT NULL,
    downloaded_at DATETIME(6) NOT NULL,
    byte_count BIGINT NOT NULL,
    client_ip VARCHAR(64) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_download_events_request
        FOREIGN KEY (print_request_id)
        REFERENCES print_requests (id),

    CONSTRAINT fk_download_events_file
        FOREIGN KEY (print_request_file_id)
        REFERENCES print_request_files (id),

    CONSTRAINT fk_download_events_agent
        FOREIGN KEY (agent_account_id)
        REFERENCES user_accounts (id),

    INDEX idx_file_download_events_request (
        print_request_id,
        downloaded_at
    ),

    INDEX idx_file_download_events_agent (
        agent_account_id,
        downloaded_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Remove temporary helpers.
DROP PROCEDURE IF EXISTS p3_add_column_if_missing;
DROP PROCEDURE IF EXISTS p3_add_index_if_missing;
DROP PROCEDURE IF EXISTS p3_add_foreign_key_if_missing;

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
      'payment_status',
      'assigned_agent_id',
      'claimed_at',
      'claim_last_activity_at',
      'printing_started_at',
      'completed_at',
      'paused_at',
      'reopened_at',
      'version'
  )
ORDER BY column_name;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'payment_records',
      'cash_payment_pins',
      'request_claims',
      'request_exceptions',
      'file_download_events'
  )
ORDER BY table_name;
