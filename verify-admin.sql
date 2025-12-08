-- Verify admin user exists
USE revcart_users;

SELECT id, email, name, role, active, password 
FROM users 
WHERE email = 'admin@revcart.com';

-- If no results, create admin user
-- If password doesn't match, update it

-- Delete existing admin if any
DELETE FROM users WHERE email = 'admin@revcart.com';

-- Create fresh admin user
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

-- Verify
SELECT id, email, name, role, active 
FROM users 
WHERE email = 'admin@revcart.com';
