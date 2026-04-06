ALTER TABLE purchase_orders 
ADD COLUMN stock_updated_at DATETIME DEFAULT NULL AFTER total_amount;
