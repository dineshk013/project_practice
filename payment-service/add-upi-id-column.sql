-- Add upi_id column to payments table
USE revcart_payments;

ALTER TABLE payments ADD COLUMN upi_id VARCHAR(100) NULL AFTER failure_reason;
