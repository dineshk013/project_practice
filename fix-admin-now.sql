USE revcart_users;

-- Check if admin exists
SELECT id, email, name, role, active FROM users WHERE email = 'admin@revcart.com';

-- Delete and recreate admin with correct password
DELETE FROM users WHERE email = 'admin@revcart.com';

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

-- Verify admin created
SELECT id, email, name, role, active FROM users WHERE email = 'admin@revcart.com';
