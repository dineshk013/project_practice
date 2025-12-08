-- Fix payment_status column size
USE revcart_orders;

ALTER TABLE orders MODIFY COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
