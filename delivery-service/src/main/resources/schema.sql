CREATE DATABASE IF NOT EXISTS revcart_delivery;
USE revcart_delivery;

CREATE TABLE IF NOT EXISTS deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    agent_id BIGINT,
    status VARCHAR(30) NOT NULL,
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS delivery_tracking_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    location VARCHAR(255),
    message VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_delivery_id (delivery_id),
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Sample data
INSERT INTO deliveries (order_id, user_id, agent_id, status, estimated_delivery_date) VALUES
(1, 1, 101, 'ASSIGNED', DATE_ADD(NOW(), INTERVAL 3 DAY)),
(2, 2, 102, 'OUT_FOR_DELIVERY', DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO delivery_tracking_logs (delivery_id, status, location, message) VALUES
(1, 'ASSIGNED', 'Warehouse', 'Delivery agent assigned'),
(2, 'ASSIGNED', 'Warehouse', 'Delivery agent assigned'),
(2, 'PICKED_UP', 'Warehouse', 'Package picked up by delivery agent'),
(2, 'OUT_FOR_DELIVERY', 'City Center', 'Package is out for delivery');
