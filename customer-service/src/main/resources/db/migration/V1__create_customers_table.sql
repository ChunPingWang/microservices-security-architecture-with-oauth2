-- Customer Service Database Schema
-- Version: V1
-- Description: Create customers table

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for email lookup (login)
CREATE INDEX idx_customers_email ON customers(email);

-- Index for status queries
CREATE INDEX idx_customers_status ON customers(status);

-- Comments
COMMENT ON TABLE customers IS '客戶帳號資料表';
COMMENT ON COLUMN customers.id IS '客戶唯一識別碼';
COMMENT ON COLUMN customers.email IS '電子郵件（登入帳號）';
COMMENT ON COLUMN customers.password_hash IS 'BCrypt 加密後的密碼';
COMMENT ON COLUMN customers.name IS '客戶姓名';
COMMENT ON COLUMN customers.status IS '帳號狀態：ACTIVE, LOCKED, SUSPENDED, PENDING_VERIFICATION';
COMMENT ON COLUMN customers.failed_login_attempts IS '連續登入失敗次數';
COMMENT ON COLUMN customers.locked_until IS '帳號鎖定到期時間';
COMMENT ON COLUMN customers.created_at IS '建立時間';
COMMENT ON COLUMN customers.updated_at IS '最後更新時間';
