-- Reset admin password to: admin123
-- BCrypt hash for "admin123"
USE revcart_users;

UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'admin@revcart.com';

SELECT id, email, name, role FROM users WHERE email = 'admin@revcart.com';
