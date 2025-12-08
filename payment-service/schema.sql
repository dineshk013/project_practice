-- Payment Service Database Schema

CREATE DATABASE IF NOT EXISTS revcart_payments;
USE revcart_payments;

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO payments (order_id, user_id, amount, payment_method, status, transaction_id) VALUES
(1, 2, 1195.6, 'RAZORPAY', 'SUCCESS', 'TXN-12345-67890-ABCDE'),
(2, 2, 558.4, 'RAZORPAY', 'SUCCESS', 'TXN-98765-43210-FGHIJ');
