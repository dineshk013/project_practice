-- ============================================
-- RevCart Database Diagnostic Script
-- ============================================

-- 1. Check if cart exists for user
USE revcart_carts;

SELECT 'CARTS TABLE' as 'TABLE_NAME';
SELECT * FROM carts WHERE user_id = 14;

-- 2. Check cart_items table
SELECT 'CART_ITEMS TABLE' as 'TABLE_NAME';
SELECT * FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = 14);

-- 3. Check table structure
SELECT 'CART_ITEMS STRUCTURE' as 'INFO';
DESCRIBE cart_items;

-- 4. Check if there are ANY cart_items
SELECT 'ALL CART_ITEMS COUNT' as 'INFO';
SELECT COUNT(*) as total_items FROM cart_items;

-- 5. Check orders
USE revcart_orders;

SELECT 'ORDERS TABLE' as 'TABLE_NAME';
SELECT * FROM orders WHERE user_id = 14 ORDER BY created_at DESC LIMIT 5;

-- 6. Check order_items
SELECT 'ORDER_ITEMS TABLE' as 'TABLE_NAME';
SELECT oi.* FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.user_id = 14
ORDER BY o.created_at DESC
LIMIT 10;

-- ============================================
-- EXPECTED RESULTS:
-- ============================================
-- 1. carts table should have 1 row for user_id = 14
-- 2. cart_items should have rows with cart_id matching the cart.id
-- 3. If cart_items is empty, items are not being saved
-- 4. orders table should have orders after checkout
-- 5. order_items should have items for each order
-- ============================================
