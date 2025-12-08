-- Fix Admin Password
-- BCrypt hash for password: admin123
-- Generated using: new BCryptPasswordEncoder().encode("admin123")

USE revcart_users;

UPDATE users 
SET password = '$2a$10$dXJ3SW6G7P37LKLsOMufOeWIaqukjcChmMqrgM4.Qr3OPUgKqUaGC'
WHERE email = 'admin@revcart.com';

SELECT 'Admin password updated successfully!' AS message;
SELECT id, email, name, role, active FROM users WHERE email = 'admin@revcart.com';
