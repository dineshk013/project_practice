USE revcart_orders;
UPDATE orders SET delivery_agent_id = 7 WHERE id = 10;
SELECT id, status, delivery_agent_id, order_number FROM orders WHERE id = 10;