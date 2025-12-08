-- Create test admin user
-- Email: test@admin.com
-- Password: test123

USE revcart_users;

-- Delete if exists
DELETE FROM users WHERE email = 'test@admin.com';

-- Insert new admin (password: test123)
INSERT INTO users (email, password, name, phone, role, active, created_at, updated_at)
VALUES (
    'test@admin.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test Admin',
    '1234567890',
    'ADMIN',
    1,
    NOW(),
    NOW()
);

-- Verify
SELECT id, email, name, role FROM users WHERE email = 'test@admin.com';
