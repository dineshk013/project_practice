-- Order Service Database Schema

CREATE DATABASE IF NOT EXISTS revcart_orders;
USE revcart_orders;

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DOUBLE NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    address_street VARCHAR(500),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_zip_code VARCHAR(20),
    address_country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    image_url VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO orders (user_id, order_number, status, total_amount, payment_status, payment_method, 
                    address_street, address_city, address_state, address_zip_code, address_country) VALUES
(2, 'ORD-1234567890', 'CONFIRMED', 1195.6, 'COMPLETED', 'RAZORPAY', 
 '123 Main Street', 'Mumbai', 'Maharashtra', '400001', 'India');

INSERT INTO order_items (order_id, product_id, product_name, quantity, price, image_url) VALUES
(1, 1, 'Fresh Tomatoes', 2, 239.2, 'https://images.unsplash.com/photo-1582284540020-8acbe03f4924?w=600'),
(1, 2, 'Organic Bananas', 3, 159.2, 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400'),
(1, 3, 'Fresh Milk', 2, 279.2, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400');
