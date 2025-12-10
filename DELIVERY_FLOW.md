# Delivery Agent Dashboard Flow

## Complete Order Flow: Customer → Admin → Delivery Agent

### 1. Customer Places Order
- Customer completes checkout
- Order created with status: `PENDING`
- Payment processed → status changes to `PAYMENT_SUCCESS`
- `deliveryAgentId` is `NULL` at this point

### 2. Admin Dashboard
- Admin sees all orders in admin dashboard
- Admin can change order status through dropdown
- When admin changes status to `OUT_FOR_DELIVERY`:
  - System automatically assigns first available delivery agent
  - Sets `deliveryAgentId` in order
  - Order now appears in delivery agent's dashboard

### 3. Delivery Agent Dashboard

#### Dashboard Sections:

**Assigned Orders** (`/api/delivery/orders/assigned`)
- Shows all orders assigned to this delivery agent
- Excludes DELIVERED and CANCELLED orders
- Filters: `deliveryAgentId = currentUserId AND status NOT IN (DELIVERED, CANCELLED)`

**In Transit** (`/api/delivery/orders/in-transit`)
- Shows orders currently OUT_FOR_DELIVERY
- Filters: `deliveryAgentId = currentUserId AND status = OUT_FOR_DELIVERY`
- Delivery agent can mark as DELIVERED

**Pending** (`/api/delivery/orders/pending`)
- Shows orders waiting for delivery assignment
- These are orders admin hasn't assigned yet
- Filters: `deliveryAgentId IS NULL AND status IN (PAYMENT_SUCCESS, PROCESSING, PACKED, CONFIRMED)`

**Delivered Today** (`/api/delivery/orders/delivered`)
- Shows orders delivered by this agent
- Filters: `deliveryAgentId = currentUserId AND status = DELIVERED`
- Count shows only today's deliveries

### 4. Status Flow

```
Customer Order → PENDING
              ↓
         PAYMENT_SUCCESS (after payment)
              ↓
         PROCESSING (admin starts processing)
              ↓
         PACKED (admin packs order)
              ↓
         OUT_FOR_DELIVERY (admin assigns → auto-assigns delivery agent)
              ↓
         DELIVERED (delivery agent marks as delivered)
```

### 5. Backend Implementation

#### OrderRepository.java
```java
List<Order> findByDeliveryAgentId(Long agentId);
List<Order> findByDeliveryAgentIdAndStatus(Long agentId, Order.OrderStatus status);
List<Order> findByStatusIn(List<Order.OrderStatus> statuses);
```

#### OrderService.java
```java
// Get all active orders for delivery agent (excluding delivered/cancelled)
getOrdersByDeliveryAgent(agentId)

// Get orders currently out for delivery
getInTransitOrdersByAgent(agentId)

// Get orders waiting for assignment
getPendingDeliveryOrders()

// Get delivered orders by agent
getDeliveredOrdersByAgent(agentId)
```

#### DeliveryController.java
```
GET /api/delivery/orders/assigned     - Active orders for agent
GET /api/delivery/orders/in-transit   - Orders out for delivery
GET /api/delivery/orders/pending      - Orders awaiting assignment
GET /api/delivery/orders/delivered    - Delivered orders by agent
POST /api/delivery/orders/{id}/status - Update delivery status
```

### 6. Frontend Implementation

#### Status Mapping (Backend → Frontend)
```typescript
PENDING, PAYMENT_SUCCESS, PROCESSING, PACKED, CONFIRMED → 'processing'
OUT_FOR_DELIVERY, SHIPPED → 'in_transit'
DELIVERED → 'delivered'
```

#### Status Mapping (Frontend → Backend)
```typescript
'in_transit' → OUT_FOR_DELIVERY
'delivered' → DELIVERED
```

### 7. Auto-Assignment Logic

When admin changes order status to `OUT_FOR_DELIVERY`:
```java
if (status == Order.OrderStatus.OUT_FOR_DELIVERY && order.getDeliveryAgentId() == null) {
    // Fetch delivery agents from user-service
    ApiResponse<List<Object>> agentsResponse = userServiceClient.getDeliveryAgents();
    
    // Assign first available agent
    Long agentId = getFirstAgentId(agentsResponse);
    order.setDeliveryAgentId(agentId);
}
```

### 8. Key Features

✅ **Real-time Updates**: Dashboard refreshes after status changes
✅ **Auto-Assignment**: Delivery agent automatically assigned when status = OUT_FOR_DELIVERY
✅ **Proper Filtering**: Each section shows correct orders based on status and assignment
✅ **Address Display**: Full delivery address shown in dashboard
✅ **Today's Count**: Delivered today count shows only current date deliveries

### 9. Testing Flow

1. **Customer**: Place an order → Status: PENDING
2. **Payment**: Complete payment → Status: PAYMENT_SUCCESS
3. **Admin**: Change status to PROCESSING → Order appears in admin dashboard
4. **Admin**: Change status to OUT_FOR_DELIVERY → Order auto-assigned to delivery agent
5. **Delivery Agent**: See order in "Assigned" and "In Transit" sections
6. **Delivery Agent**: Click "Mark Delivered" → Status: DELIVERED
7. **Delivery Agent**: Order moves to "Delivered Today" count

### 10. Database Schema

```sql
orders table:
- id (PK)
- user_id (customer)
- delivery_agent_id (NULL initially, set when OUT_FOR_DELIVERY)
- status (enum: PENDING, PAYMENT_SUCCESS, PROCESSING, PACKED, OUT_FOR_DELIVERY, DELIVERED, etc.)
- payment_status
- total_amount
- created_at
- updated_at
```

## Rebuild Instructions

After making these changes:

```powershell
# Stop order-service
# Navigate to order-service folder
cd order-service
mvn clean install -DskipTests
mvn spring-boot:run

# Frontend (if needed)
cd Frontend
npm run build
ng serve
```

## API Testing

```bash
# Get assigned orders (as delivery agent)
curl -H "X-User-Id: 3" http://localhost:8080/api/delivery/orders/assigned

# Get in-transit orders
curl -H "X-User-Id: 3" http://localhost:8080/api/delivery/orders/in-transit

# Get pending orders (not yet assigned)
curl http://localhost:8080/api/delivery/orders/pending

# Mark order as delivered
curl -X POST -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED"}' \
  http://localhost:8080/api/delivery/orders/1/status
```
