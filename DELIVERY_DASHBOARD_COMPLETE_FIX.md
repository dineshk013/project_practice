# Delivery Dashboard Complete Fix

## Issues Fixed

### 1. Orders Not Showing in Assigned Section
**Problem**: When admin changes status to OUT_FOR_DELIVERY, orders don't appear in delivery agent's "Assigned" section

**Root Cause**: `getOrdersByDeliveryAgent()` was filtering out all statuses except DELIVERED and CANCELLED, but should only show OUT_FOR_DELIVERY orders

**Fix**: Changed filter to only return OUT_FOR_DELIVERY orders
```java
// Before: Excluded DELIVERED and CANCELLED (showed all others)
.filter(order -> order.getStatus() != Order.OrderStatus.DELIVERED && 
               order.getStatus() != Order.OrderStatus.CANCELLED)

// After: Only show OUT_FOR_DELIVERY
.filter(order -> order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY)
```

### 2. Gateway Routing 404 Error
**Problem**: `/api/delivery/orders/delivered` returned 404

**Root Cause**: Gateway was routing `/api/delivery/**` to delivery-service (8087) instead of order-service (8084)

**Fix**: Updated gateway routing
```yaml
- id: delivery-orders
  uri: http://localhost:8084  # order-service
  order: 1
  predicates:
    - Path=/api/delivery/**
```

### 3. Missing Delivery Agent Notifications
**Problem**: Delivery agents not notified when orders assigned

**Fix**: Added notification when order assigned
```java
if (agentId != null) {
    order.setDeliveryAgentId(agentId);
    notificationServiceClient.notifyOrder(id, agentId, "ASSIGNED");
}
```

## Complete Flow

### Admin Changes Status: PENDING → PACKED
- Order stays in "Pending" section (deliveryAgentId = NULL)
- Count shows in delivery agent dashboard under "Pending: Awaiting pickup"

### Admin Changes Status: PACKED → OUT_FOR_DELIVERY
1. **Auto-assignment triggers**:
   - System finds delivery agent with least active orders
   - Sets `deliveryAgentId` in order
   - Sends notification to delivery agent

2. **Order appears in delivery agent dashboard**:
   - **Assigned section**: Shows order (status = OUT_FOR_DELIVERY)
   - **In Transit section**: Shows same order (OUT_FOR_DELIVERY = in transit)
   - **Pending section**: Order removed (now has deliveryAgentId)

3. **Notifications**:
   - Customer: "Your order is out for delivery"
   - Delivery Agent: "New order assigned to you"

### Delivery Agent Marks: OUT_FOR_DELIVERY → DELIVERED
1. Delivery agent clicks "Mark Delivered"
2. Status changes to DELIVERED
3. Order removed from "Assigned" and "In Transit"
4. Order appears in "Delivered Today" count
5. Customer notified: "Your order has been delivered"

## Status Mapping

### Backend → Frontend
```
PENDING, PAYMENT_SUCCESS, PROCESSING, PACKED, CONFIRMED → 'processing'
OUT_FOR_DELIVERY, SHIPPED → 'in_transit'
DELIVERED → 'delivered'
```

### Dashboard Sections Logic

**Assigned Orders** (`/api/delivery/orders/assigned`):
```sql
WHERE delivery_agent_id = currentUserId 
  AND status = 'OUT_FOR_DELIVERY'
```

**In Transit** (`/api/delivery/orders/in-transit`):
```sql
WHERE delivery_agent_id = currentUserId 
  AND status = 'OUT_FOR_DELIVERY'
```
(Same as Assigned - OUT_FOR_DELIVERY means in transit)

**Pending** (`/api/delivery/orders/pending`):
```sql
WHERE delivery_agent_id IS NULL 
  AND status IN ('PAYMENT_SUCCESS', 'PROCESSING', 'PACKED', 'CONFIRMED')
```

**Delivered Today** (`/api/delivery/orders/delivered`):
```sql
WHERE delivery_agent_id = currentUserId 
  AND status = 'DELIVERED'
  AND DATE(updated_at) = CURDATE()
```

## Services to Restart

### 1. Gateway (MUST)
```powershell
cd revcart-gateway
mvn spring-boot:run
```

### 2. Order Service (MUST)
```powershell
cd order-service
mvn spring-boot:run
```

### 3. Frontend (Optional - if cached)
```
Hard refresh: Ctrl + Shift + R
```

## Testing Steps

### 1. Fix Existing Orders (if needed)
```sql
-- Fix orders that have OUT_FOR_DELIVERY but no agent
USE revcart_orders;

UPDATE orders 
SET delivery_agent_id = (
    SELECT id FROM revcart_users.users 
    WHERE role = 'DELIVERY_AGENT' 
    LIMIT 1
)
WHERE status = 'OUT_FOR_DELIVERY' 
  AND delivery_agent_id IS NULL;
```

### 2. Test New Order Flow
1. **Customer**: Place order and complete payment
2. **Admin**: Login to admin dashboard
3. **Admin**: Change order status: PENDING → PROCESSING → PACKED
4. **Verify**: Order shows in delivery agent "Pending" section (count = 3)
5. **Admin**: Change status to OUT_FOR_DELIVERY
6. **Verify**: 
   - Order disappears from "Pending"
   - Order appears in "Assigned" section
   - Order appears in "In Transit" section
   - Delivery agent receives notification
7. **Delivery Agent**: Click "Mark Delivered"
8. **Verify**:
   - Order removed from "Assigned" and "In Transit"
   - "Delivered Today" count increases
   - Customer receives notification

### 3. Test Multiple Agents
```sql
-- Check agent workload
SELECT 
    u.id,
    u.name,
    COUNT(o.id) as active_orders
FROM revcart_users.users u
LEFT JOIN orders o ON u.id = o.delivery_agent_id 
    AND o.status = 'OUT_FOR_DELIVERY'
WHERE u.role = 'DELIVERY_AGENT'
GROUP BY u.id, u.name;
```

## API Endpoints

```bash
# Get assigned orders
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/assigned

# Get in-transit orders
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/in-transit

# Get pending orders (not assigned yet)
curl http://localhost:8080/api/delivery/orders/pending

# Get delivered orders
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/delivered

# Mark as delivered
curl -X POST -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}' \
  http://localhost:8080/api/delivery/orders/14/status
```

## Key Changes Summary

| File | Change | Purpose |
|------|--------|---------|
| OrderService.java | Filter only OUT_FOR_DELIVERY in getOrdersByDeliveryAgent() | Show correct orders in Assigned section |
| OrderService.java | Add notification when agent assigned | Notify delivery agent of new order |
| gateway/application.yml | Route /api/delivery/** to order-service | Fix 404 errors |

## Verification Checklist

- [ ] Gateway restarted
- [ ] Order-service restarted
- [ ] Existing OUT_FOR_DELIVERY orders have delivery_agent_id
- [ ] Admin can change status to OUT_FOR_DELIVERY
- [ ] Order auto-assigns to delivery agent
- [ ] Order appears in delivery agent "Assigned" section
- [ ] Order appears in delivery agent "In Transit" section
- [ ] Pending count shows orders without agent
- [ ] Delivery agent can mark as delivered
- [ ] Delivered count increases
- [ ] Notifications sent to delivery agent
- [ ] Notifications sent to customer

## Expected Behavior

✅ **Pending → Packed**: Shows in "Pending" (count 3)
✅ **Packed → Out for Delivery**: Auto-assigns agent, shows in "Assigned" and "In Transit"
✅ **Out for Delivery → Delivered**: Moves to "Delivered Today" count
✅ **Notifications**: Sent to both customer and delivery agent
✅ **Load Balancing**: Orders distributed evenly among agents
