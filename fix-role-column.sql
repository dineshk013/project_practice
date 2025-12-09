-- Fix role column size to accommodate DELIVERY_AGENT
USE revcart_users;

ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL;
