-- Product Service Database Schema
-- Version: V1
-- Description: Create products and categories tables

-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id UUID REFERENCES categories(id),
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for parent category lookup
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- Index for active categories
CREATE INDEX idx_categories_active ON categories(active);

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    sku VARCHAR(50) UNIQUE,
    price DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TWD',
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    category_id UUID NOT NULL REFERENCES categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for SKU lookup
CREATE INDEX idx_products_sku ON products(sku);

-- Index for category filtering
CREATE INDEX idx_products_category_id ON products(category_id);

-- Index for status filtering
CREATE INDEX idx_products_status ON products(status);

-- Full text search index for product search
CREATE INDEX idx_products_name_search ON products USING gin(to_tsvector('simple', name));

-- Composite index for category + status queries
CREATE INDEX idx_products_category_status ON products(category_id, status);

-- Comments
COMMENT ON TABLE categories IS '商品分類表';
COMMENT ON COLUMN categories.id IS '分類唯一識別碼';
COMMENT ON COLUMN categories.name IS '分類名稱';
COMMENT ON COLUMN categories.description IS '分類描述';
COMMENT ON COLUMN categories.parent_id IS '父分類 ID（NULL 表示根分類）';
COMMENT ON COLUMN categories.display_order IS '顯示順序';
COMMENT ON COLUMN categories.active IS '是否啟用';

COMMENT ON TABLE products IS '商品資料表';
COMMENT ON COLUMN products.id IS '商品唯一識別碼';
COMMENT ON COLUMN products.name IS '商品名稱';
COMMENT ON COLUMN products.description IS '商品描述';
COMMENT ON COLUMN products.sku IS '庫存單位編碼';
COMMENT ON COLUMN products.price IS '商品價格';
COMMENT ON COLUMN products.currency IS '貨幣代碼';
COMMENT ON COLUMN products.stock_quantity IS '總庫存數量';
COMMENT ON COLUMN products.reserved_quantity IS '已保留數量';
COMMENT ON COLUMN products.category_id IS '所屬分類 ID';
COMMENT ON COLUMN products.status IS '商品狀態：ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED';
COMMENT ON COLUMN products.image_url IS '商品圖片 URL';

-- Insert default categories
INSERT INTO categories (id, name, description, parent_id, display_order, active, created_at, updated_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', '電子產品', '電腦、手機、配件等電子產品', NULL, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000002', '服飾配件', '男女服裝、鞋子、配件', NULL, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000003', '居家生活', '家具、家電、居家用品', NULL, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000004', '食品飲料', '零食、飲料、生鮮食品', NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
