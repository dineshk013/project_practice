-- Create Admin User
USE revcart_users;

-- Delete existing admin if exists
DELETE FROM addresses WHERE user_id IN (SELECT id FROM users WHERE email = 'admin@revcart.com');
DELETE FROM users WHERE email = 'admin@revcart.com';

-- Insert admin user with BCrypt password for 'admin123'
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

SELECT 'Admin user created successfully!' AS message;
SELECT id, email, name, role, active FROM users WHERE email = 'admin@revcart.com';
