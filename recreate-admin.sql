-- Recreate admin user with correct hash
USE revcart_users;

-- Delete existing admin
DELETE FROM addresses WHERE user_id IN (SELECT id FROM users WHERE LOWER(email) = 'admin@revcart.com');
DELETE FROM users WHERE LOWER(email) = 'admin@revcart.com';

-- Insert admin with verified BCrypt hash for "admin123"
INSERT INTO users (email, password, name, phone, role, active, created_at, updated_at)
VALUES (
    'admin@revcart.com',
    '$2a$10$dXJ3SW6G7P37LKLsOMufOeWIaqukjcChmMqrgM4.Qr3OPUgKqUaGC',
    'Admin User',
    '9999999999',
    'ADMIN',
    1,
    NOW(),
    NOW()
);

-- Verify insertion
SELECT id, email, name, role, active, 
       SUBSTRING(password, 1, 20) as password_hash_start
FROM users 
WHERE email = 'admin@revcart.com';
