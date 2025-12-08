-- Reset All MySQL Databases
-- Execute this in MySQL Workbench or command line: mysql -uroot -proot < reset-mysql-databases.sql

-- User Service Database
DROP DATABASE IF EXISTS user_service;
CREATE DATABASE user_service;

-- Product Service Database
DROP DATABASE IF EXISTS product_service;
CREATE DATABASE product_service;

-- Cart Service Database
DROP DATABASE IF EXISTS cart_service;
CREATE DATABASE cart_service;

-- Order Service Database
DROP DATABASE IF EXISTS order_service;
CREATE DATABASE order_service;

-- Payment Service Database
DROP DATABASE IF EXISTS payment_service;
CREATE DATABASE payment_service;

-- Show all databases
SHOW DATABASES;
