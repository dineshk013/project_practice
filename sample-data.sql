-- Sample Data for RevCart
-- Run this after creating databases

-- ============================================
-- PRODUCTS DATABASE
-- ============================================
USE revcart_products;

-- Insert Categories
INSERT INTO categories (id, created_at, updated_at, description, image_url, name, slug, active)
VALUES
(1, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=400', 'Vegetables', 'vegetables', 1),
(2, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=400', 'Fruits', 'fruits', 1),
(3, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=400', 'Dairy', 'dairy', 1),
(4, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400', 'Bakery', 'bakery', 1),
(5, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400', 'Meat', 'meat', 1),
(6, NOW(), NOW(), NULL, 'https://images.unsplash.com/photo-1437418747212-8d9709afab22?w=400', 'Beverages', 'beverages', 1);

-- Insert Products
INSERT INTO products (id, created_at, updated_at, active, brand, description, discount, highlights, image_url, name, price, sku, tag, stock_quantity, category_id)
VALUES
(1, NOW(), NOW(), 1, NULL, 'Fresh, juicy tomatoes perfect for salads and cooking', NULL, NULL, 'https://images.unsplash.com/photo-1582284540020-8acbe03f4924?w=600', 'Fresh Tomatoes', 239.2, 'SKU-1', 'NONE', 50, 1),
(2, NOW(), NOW(), 1, NULL, 'Ripe organic bananas, naturally sweet', NULL, NULL, 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400', 'Organic Bananas', 159.2, 'SKU-2', 'NONE', 100, 2),
(3, NOW(), NOW(), 1, NULL, 'Farm-fresh whole milk', NULL, NULL, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400', 'Fresh Milk', 279.2, 'SKU-3', 'NONE', 30, 3),
(4, NOW(), NOW(), 1, NULL, 'Freshly baked whole wheat bread', NULL, NULL, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400', 'Whole Wheat Bread', 199.2, 'SKU-4', 'NONE', 40, 4),
(5, NOW(), NOW(), 1, NULL, 'Fresh, skinless chicken breast', NULL, NULL, 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400', 'Chicken Breast', 719.2, 'SKU-5', 'NONE', 25, 5),
(6, NOW(), NOW(), 1, NULL, 'Crisp iceberg lettuce for sandwiches and salads', NULL, NULL, 'https://images.unsplash.com/photo-1657411658285-2742c4c5ed1d?w=600', 'Crisp Lettuce', 143.2, 'SKU-6', 'NONE', 60, 1),
(7, NOW(), NOW(), 1, NULL, 'Sweet red bell peppers, crunchy and colorful', NULL, NULL, 'https://images.unsplash.com/photo-1608737637507-9aaeb9f4bf30?w=600', 'Red Bell Peppers', 263.2, 'SKU-7', 'NONE', 45, 1),
(8, NOW(), NOW(), 1, NULL, 'Tender baby spinach leaves, washed and ready', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1701699257548-8261a687236f?w=600', 'Baby Spinach', 239.2, 'SKU-8', 'NONE', 35, 1),
(9, NOW(), NOW(), 1, NULL, 'Starchy russet potatoes, great for baking and mashing', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1723600901806-8a98c9ebc094?w=600', 'Russet Potatoes', 79.2, 'SKU-9', 'NONE', 80, 1),
(10, NOW(), NOW(), 1, NULL, 'Sweet organic carrots, perfect for snacking', NULL, NULL, 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600', 'Carrots (Organic)', 199.2, 'SKU-10', 'NONE', 70, 1),
(11, NOW(), NOW(), 1, NULL, 'Crisp and tart green apples, ideal for pies', NULL, NULL, 'https://images.unsplash.com/photo-1577028300036-aa112c18d109?w=600', 'Green Apples', 183.2, 'SKU-11', 'NONE', 55, 2),
(12, NOW(), NOW(), 1, NULL, 'Sweet and juicy strawberries, locally sourced', NULL, NULL, 'https://images.unsplash.com/photo-1582472138480-e84227671cd4?w=600', 'Strawberries (Pint)', 319.2, 'SKU-12', 'NONE', 40, 2),
(13, NOW(), NOW(), 1, NULL, 'Fresh blueberries, great for smoothies', NULL, NULL, 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400', 'Blueberries (Pint)', 359.2, 'SKU-13', 'NONE', 30, 2),
(14, NOW(), NOW(), 1, NULL, 'Sweet seedless grapes, snack-ready', NULL, NULL, 'https://images.unsplash.com/photo-1574871866887-911cff04aef1?w=600', 'Seedless Grapes', 287.2, 'SKU-14', 'NONE', 50, 2),
(15, NOW(), NOW(), 1, NULL, 'Juicy ripe mangoes, fragrant and sweet', NULL, NULL, 'https://images.unsplash.com/photo-1732472581875-89ff83f18439?w=600', 'Mango (Ripe)', 119.2, 'SKU-15', 'NONE', 45, 2),
(16, NOW(), NOW(), 1, NULL, 'Thick plain Greek yogurt, high in protein', NULL, NULL, 'https://images.unsplash.com/photo-1571212515416-fef01fc43637?w=600', 'Greek Yogurt (Plain)', 479.2, 'SKU-16', 'NONE', 25, 3),
(17, NOW(), NOW(), 1, NULL, 'Aged cheddar cheese, sharp and flavorful', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1760605911334-090ac301cc15?w=600', 'Cheddar Cheese Block', 519.2, 'SKU-17', 'NONE', 20, 3),
(18, NOW(), NOW(), 1, NULL, 'Creamy salted butter for baking and cooking', NULL, NULL, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=400', 'Butter (Salted)', 319.2, 'SKU-18', 'NONE', 35, 3),
(19, NOW(), NOW(), 1, NULL, 'Free-range eggs, medium size', NULL, NULL, 'https://images.unsplash.com/photo-1551218808-94e220e084d2?w=400', 'Eggs (Dozen)', 239.2, 'SKU-19', 'NONE', 60, 3),
(20, NOW(), NOW(), 1, NULL, 'Light and creamy cottage cheese', NULL, NULL, 'https://images.unsplash.com/photo-1661349008073-136bed6e6788?w=600', 'Cottage Cheese', 359.2, 'SKU-20', 'NONE', 30, 3),
(21, NOW(), NOW(), 1, NULL, 'Artisan sourdough with a crisp crust', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1664640733898-d5c3f71f44e1?w=600', 'Sourdough Loaf', 319.2, 'SKU-21', 'NONE', 25, 4),
(22, NOW(), NOW(), 1, NULL, 'Buttery, flaky croissants baked fresh daily', NULL, NULL, 'https://images.unsplash.com/photo-1506084868230-bb9d95c24759?w=400', 'Croissants (Pack of 4)', 439.2, 'SKU-22', 'NONE', 20, 4),
(23, NOW(), NOW(), 1, NULL, 'Chewy bagels, assorted flavors', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1720070416636-0e5ef67d3862?w=600', 'Bagels (6-pack)', 399.2, 'SKU-23', 'NONE', 30, 4),
(24, NOW(), NOW(), 1, NULL, 'Rich chocolate muffins with chocolate chips', NULL, NULL, 'https://images.unsplash.com/photo-1586111893496-8f91022df73a?w=600', 'Chocolate Muffins (2)', 239.2, 'SKU-24', 'NONE', 40, 4),
(25, NOW(), NOW(), 1, NULL, 'Moist banana bread packed with banana flavor', NULL, NULL, 'https://images.unsplash.com/photo-1642068151095-e44d35b7ad8a?w=600', 'Banana Bread Loaf', 343.2, 'SKU-25', 'NONE', 15, 4),
(26, NOW(), NOW(), 1, NULL, 'Fresh ground beef, ideal for burgers', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1668616816953-a02cd1a44027?w=600', 'Ground Beef (80/20)', 639.2, 'SKU-26', 'NONE', 30, 5),
(27, NOW(), NOW(), 1, NULL, 'Boneless pork chops, tender cut', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1722686483348-2324b28215d2?w=600', 'Pork Chops', 559.2, 'SKU-27', 'NONE', 25, 5),
(28, NOW(), NOW(), 1, NULL, 'Fresh wild-caught salmon fillet', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1726768907990-d3cbc8efdee5?w=600', 'Salmon Fillet', 1039.2, 'SKU-28', 'NONE', 15, 5),
(29, NOW(), NOW(), 1, NULL, 'Smoked bacon, thick cut', NULL, NULL, 'https://images.unsplash.com/photo-1694983361629-0363ab0d1b49?w=600', 'Bacon (Smoked)', 479.2, 'SKU-29', 'NONE', 35, 5),
(30, NOW(), NOW(), 1, NULL, 'Thin-sliced deli turkey, low sodium', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1664392048940-3e08720a4207?w=600', 'Turkey Slices (Deli)', 399.2, 'SKU-30', 'NONE', 20, 5),
(31, NOW(), NOW(), 1, NULL, 'Fresh squeezed orange juice, no added sugar', NULL, NULL, 'https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=400', 'Orange Juice (Fresh)', 319.2, 'SKU-31', 'NONE', 40, 6),
(32, NOW(), NOW(), 1, NULL, 'Natural sparkling water, crisp and refreshing', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1687354256687-b5ee47c043c1?w=600', 'Sparkling Water (6-pack)', 359.2, 'SKU-32', 'NONE', 50, 6),
(33, NOW(), NOW(), 1, NULL, 'Smooth cold brew coffee, ready to drink', NULL, NULL, 'https://images.unsplash.com/photo-1504753793650-d4a2b783c15e?w=600', 'Cold Brew Coffee (12 oz)', 279.2, 'SKU-33', 'NONE', 30, 6),
(34, NOW(), NOW(), 1, NULL, 'Assorted herbal tea bags for relaxation', NULL, NULL, 'https://plus.unsplash.com/premium_photo-1731696604013-52ccf4c49bd9?w=600', 'Herbal Tea Assortment', 479.2, 'SKU-34', 'NONE', 25, 6),
(35, NOW(), NOW(), 1, NULL, 'Unsweetened almond milk, dairy-free', NULL, NULL, 'https://images.unsplash.com/photo-1601436423474-51738541c1b1?w=600', 'Almond Milk (Unsweetened)', 279.2, 'SKU-35', 'NONE', 35, 6);

-- Verify
SELECT COUNT(*) as category_count FROM categories;
SELECT COUNT(*) as product_count FROM products;

-- ============================================
-- USERS DATABASE
-- ============================================
USE revcart_users;

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert admin user (password: admin123)
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
)
ON DUPLICATE KEY UPDATE
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ADMIN';

-- Verify
SELECT id, email, name, role FROM users WHERE email = 'admin@revcart.com';

-- ============================================
-- DONE!
-- ============================================
SELECT 'Sample data loaded successfully!' as status;
