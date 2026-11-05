USE kiosk_db;

-- =========================================================
-- PHASE 1 — AUTHENTICATION
-- Database-backed refresh tokens
-- =========================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_account_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_refresh_tokens_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_accounts (id)
        ON DELETE CASCADE,

    INDEX idx_refresh_tokens_user_account (
        user_account_id
    ),

    INDEX idx_refresh_tokens_expires_at (
        expires_at
    ),

    INDEX idx_refresh_tokens_revoked_at (
        revoked_at
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Verification
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'refresh_tokens';
