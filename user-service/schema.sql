-- User Service Database Schema

CREATE DATABASE IF NOT EXISTS revcart_users;
USE revcart_users;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Addresses Table
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- OTP Tokens Table
CREATE TABLE IF NOT EXISTS otp_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_otp (email, otp, used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO users (email, password, name, phone, role, active) VALUES
('admin@revcart.com', '$2a$10$xQZ9Z9Z9Z9Z9Z9Z9Z9Z9Z.Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z', 'Admin User', '1234567890', 'ADMIN', TRUE),
('user@revcart.com', '$2a$10$xQZ9Z9Z9Z9Z9Z9Z9Z9Z9Z.Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z9Z', 'Test User', '9876543210', 'USER', TRUE);

INSERT INTO addresses (user_id, street, city, state, zip_code, country, is_default) VALUES
(2, '123 Main Street', 'Mumbai', 'Maharashtra', '400001', 'India', TRUE),
(2, '456 Park Avenue', 'Delhi', 'Delhi', '110001', 'India', FALSE);
