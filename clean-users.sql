-- Clean and reset revcart_users database
-- Run this in MySQL Workbench or command line

USE revcart_users;

-- Disable safe update mode
SET SQL_SAFE_UPDATES = 0;

-- Delete in correct order (child tables first, then parent)
DELETE FROM addresses;
DELETE FROM users;

-- Reset auto increment
ALTER TABLE addresses AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Insert admin user
INSERT INTO users (email, password, name, phone, role, active, created_at, updated_at)
VALUES (
    'admin@revcart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin User',
    '9999999999',
    'ADMIN',
    1,
    NOW(),
    NOW()
);

-- Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- Verify
SELECT * FROM users;
