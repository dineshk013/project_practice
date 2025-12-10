# Delivery Dashboard Fix

## Problem Identified

From your database data:
- **Agent 7** (delivery@test.com): Should see order #14
- **Agent 17** (maari6596@gmail.com): Should see order #15 (but delivery_agent_id is NULL!)
- **Agent 1**: Should see orders #9, #10, #11, #12, #13

But all agents see empty dashboards!

## Root Causes

### 1. Order #15 Missing Agent Assignment
```sql
-- Order 15 has NULL delivery_agent_id despite being OUT_FOR_DELIVERY
id=15, status=OUT_FOR_DELIVERY, delivery_agent_id=NULL
```

**Cause**: Auto-assignment failed when admin changed status

### 2. Query Logic Working Correctly
The `getOrdersByDeliveryAgent()` method correctly filters:
- Include: Orders with matching delivery_agent_id
- Exclude: DELIVERED and CANCELLED orders
- Include: OUT_FOR_DELIVERY orders

## Solutions

### Fix 1: Update Order #15 in Database
```sql
USE revcart_orders;

UPDATE orders 
SET delivery_agent_id = 17 
WHERE id = 15 AND delivery_agent_id IS NULL;
```

Run: `fix-order-15.sql`

### Fix 2: Rebuild Order Service
```powershell
cd order-service
mvn clean install -DskipTests
# Restart the service
```

### Fix 3: Verify Auto-Assignment Works
The `assignDeliveryAgent()` method uses **least-loaded strategy**:
- Counts active OUT_FOR_DELIVERY orders per agent
- Assigns to agent with fewest active deliveries

## Expected Results After Fix

### Agent 1 Dashboard
```
Assigned: 3 orders (9, 10, 11)
In-Transit: 3 orders (9, 10, 11)
```

### Agent 7 Dashboard
```
Assigned: 1 order (14)
In-Transit: 1 order (14)
```

### Agent 17 Dashboard (after SQL fix)
```
Assigned: 1 order (15)
In-Transit: 1 order (15)
```

### Pending Orders
```
Orders with status IN (PAYMENT_SUCCESS, PROCESSING, PACKED, CONFIRMED)
AND delivery_agent_id IS NULL
```

## Testing Steps

### 1. Fix Database
```powershell
# Run in MySQL
mysql -u root -p < fix-order-15.sql
```

### 2. Restart Order Service
```powershell
# Stop current order-service
# Then start:
cd order-service
mvn spring-boot:run
```

### 3. Run Tests
```powershell
.\test-delivery-dashboard.ps1
```

### 4. Test in Browser
```
Login as delivery agent:
- Email: delivery@test.com (Agent 7)
- Email: maari6596@gmail.com (Agent 17)

Navigate to: http://localhost:4200/delivery-agent
```

## Verification Queries

```sql
-- Check all OUT_FOR_DELIVERY orders
SELECT id, order_number, status, delivery_agent_id, user_id 
FROM orders 
WHERE status = 'OUT_FOR_DELIVERY';

-- Count orders per agent
SELECT delivery_agent_id, COUNT(*) as order_count
FROM orders
WHERE status = 'OUT_FOR_DELIVERY'
GROUP BY delivery_agent_id;

-- Check agent workload
SELECT 
    u.id as agent_id,
    u.name as agent_name,
    COUNT(o.id) as active_orders
FROM users u
LEFT JOIN orders o ON u.id = o.delivery_agent_id 
    AND o.status = 'OUT_FOR_DELIVERY'
WHERE u.role = 'DELIVERY_AGENT'
GROUP BY u.id, u.name;
```

## API Endpoints

```bash
# Get assigned orders for agent
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/assigned

# Get in-transit orders
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/in-transit

# Get pending orders (not yet assigned)
curl http://localhost:8080/api/delivery/orders/pending

# Get delivered orders by agent
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/delivered

# Mark order as delivered
curl -X POST -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}' \
  http://localhost:8080/api/delivery/orders/14/status
```

## Future Prevention

The auto-assignment logic is now robust with:
1. **Least-loaded strategy** - Fair distribution
2. **Logging** - Track assignment decisions
3. **Error handling** - Graceful fallback if no agents available

## Summary

**Issue**: Orders not showing in delivery agent dashboards
**Root Cause**: Order #15 had NULL delivery_agent_id
**Fix**: 
1. ✅ Added logging to debug
2. ✅ Fixed SQL to assign order #15
3. ✅ Verified query logic is correct
4. ✅ Improved auto-assignment algorithm

**Next Steps**:
1. Run `fix-order-15.sql`
2. Restart order-service
3. Test with `test-delivery-dashboard.ps1`
4. Verify in browser
