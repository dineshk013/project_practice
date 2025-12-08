CREATE DATABASE IF NOT EXISTS revcart_notifications;
USE revcart_notifications;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_id BIGINT,
    recipient_email VARCHAR(255),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
);

-- Sample data
INSERT INTO notifications (user_id, type, message, status, reference_id, recipient_email) VALUES
(1, 'ORDER_PLACED', 'Your order has been placed successfully. Order ID: 1', 'SENT', 1, 'user1@example.com'),
(1, 'PAYMENT_SUCCESS', 'Payment successful for Order ID: 1. Payment ID: 1', 'SENT', 1, 'user1@example.com'),
(2, 'ORDER_SHIPPED', 'Your order has been shipped. Order ID: 2', 'SENT', 2, 'user2@example.com');
