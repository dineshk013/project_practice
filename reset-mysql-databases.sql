-- Reset All MySQL Databases
-- Execute this in MySQL Workbench or command line: mysql -uroot -proot < reset-mysql-databases.sql

-- User Service Database
DROP DATABASE IF EXISTS revcart_users;
CREATE DATABASE revcart_users;

-- Product Service Database
DROP DATABASE IF EXISTS revcart_products;
CREATE DATABASE revcart_products;

-- Cart Service Database
DROP DATABASE IF EXISTS revcart_cart;
CREATE DATABASE revcart_cart;

-- Order Service Database
DROP DATABASE IF EXISTS revcart_orders;
CREATE DATABASE revcart_orders;

-- Payment Service Database
DROP DATABASE IF EXISTS revcart_payments;
CREATE DATABASE revcart_payments;

-- Show all databases
SHOW DATABASES;
