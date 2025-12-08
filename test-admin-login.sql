-- Diagnostic: Check admin user details
USE revcart_users;

SELECT 
    id,
    email,
    LOWER(email) as email_lowercase,
    name,
    role,
    active,
    LENGTH(password) as password_length,
    SUBSTRING(password, 1, 10) as password_prefix
FROM users 
WHERE LOWER(email) = 'admin@revcart.com';

-- If no results, check all users
SELECT id, email, name, role, active FROM users;
