-- Product Service Database Schema

CREATE DATABASE IF NOT EXISTS revcart_products;
USE revcart_products;

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Products Table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    image_url VARCHAR(500),
    sku VARCHAR(100) NOT NULL UNIQUE,
    brand VARCHAR(255),
    highlights TEXT,
    active BOOLEAN DEFAULT TRUE,
    category_id BIGINT,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category (category_id),
    INDEX idx_sku (sku),
    INDEX idx_active (active),
    INDEX idx_stock (stock_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO categories (name, slug, description, image_url) VALUES
('Vegetables', 'vegetables', 'Fresh vegetables', 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=400'),
('Fruits', 'fruits', 'Fresh fruits', 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=400'),
('Dairy', 'dairy', 'Dairy products', 'https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=400'),
('Bakery', 'bakery', 'Bakery items', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400'),
('Meat', 'meat', 'Meat products', 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400'),
('Beverages', 'beverages', 'Beverages', 'https://images.unsplash.com/photo-1437418747212-8d9709afab22?w=400');

INSERT INTO products (name, description, price, image_url, sku, brand, active, category_id, stock_quantity) VALUES
('Fresh Tomatoes', 'Fresh, juicy tomatoes perfect for salads and cooking', 239.2, 'https://images.unsplash.com/photo-1582284540020-8acbe03f4924?w=600', 'SKU-1', NULL, 1, 1, 100),
('Organic Bananas', 'Ripe organic bananas, naturally sweet', 159.2, 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400', 'SKU-2', NULL, 1, 2, 150),
('Fresh Milk', 'Farm-fresh whole milk', 279.2, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400', 'SKU-3', NULL, 1, 3, 80),
('Whole Wheat Bread', 'Freshly baked whole wheat bread', 199.2, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400', 'SKU-4', NULL, 1, 4, 50),
('Chicken Breast', 'Fresh, skinless chicken breast', 719.2, 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400', 'SKU-5', NULL, 1, 5, 30);
