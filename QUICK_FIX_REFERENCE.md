# ⚡ QUICK FIX REFERENCE CARD

## 🎯 WHAT WAS FIXED

| Issue | Solution |
|-------|----------|
| Orders not showing in My Orders | Added `/api/orders/user` endpoint + fixed response format |
| Orders not showing in Admin Dashboard | Created `AdminOrderController` with pagination |
| Field name mismatches | Added `@JsonProperty` dual support in DTOs |
| Status mapping incomplete | Updated frontend to map all backend statuses |
| Missing pagination | Added `getAllOrdersPaged()` method |

---

## 📁 FILES TO REBUILD

### Backend (Order Service)
```bash
cd order-service
mvn clean install
mvn spring-boot:run
```

**Modified Files**:
- `OrderDto.java`
- `AddressDto.java`
- `OrderItemDto.java`
- `OrderController.java`
- `AdminOrderController.java` (NEW)
- `OrderService.java`

### Frontend
```bash
cd Frontend
npm install
npm start
```

**Modified Files**:
- `order.service.ts`

---

## 🧪 QUICK TEST

### 1. Test My Orders
```bash
# After placing order, check:
GET http://localhost:8080/api/orders/user
Headers: X-User-Id: 1

# Should return orders with both deliveryAddress AND shippingAddress
```

### 2. Test Admin Dashboard
```bash
# Check paginated orders:
GET http://localhost:8080/api/admin/orders?page=0&size=20

# Should return:
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": X,
  "totalPages": Y
}
```

### 3. Test Status Update
```bash
# Update order status:
POST http://localhost:8080/api/admin/orders/12/status
{
  "status": "SHIPPED"
}

# Should return updated order
```

---

## 🔍 VERIFY IN UI

### My Orders Page
1. Login as user
2. Go to http://localhost:4200/orders
3. Should see all orders
4. Click on order to see details

### Admin Dashboard
1. Login as admin
2. Go to http://localhost:4200/admin/orders
3. Should see paginated orders
4. Update status via dropdown
5. View order details

---

## 📊 KEY ENDPOINTS

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/orders/user` | GET | Get user orders |
| `/api/admin/orders` | GET | Get paginated orders |
| `/api/admin/orders/{id}/status` | POST | Update order status |
| `/api/orders/{id}` | GET | Get order details |

---

## ✅ SUCCESS INDICATORS

### Backend Logs
```
✅ "ORDER SAVED === ID: X"
✅ "Payment status updated to COMPLETED and order status to CONFIRMED"
✅ "Delivery assigned for order: X"
✅ "Notification sent for order: X"
```

### Database
```sql
-- Orders should have:
SELECT * FROM orders WHERE status = 'CONFIRMED' AND payment_status = 'COMPLETED';

-- Items should exist:
SELECT * FROM order_items WHERE order_id = X;
```

### UI
```
✅ My Orders shows order
✅ Admin Dashboard shows order
✅ Status updates work
✅ Notifications appear
```

---

## 🚨 IF STILL NOT WORKING

### 1. Clear Browser Cache
```
Ctrl + Shift + Delete → Clear cache
```

### 2. Restart Services
```powershell
.\stop-all.ps1
.\start-all.ps1
```

### 3. Check Logs
```
Order Service: Check port 8084 logs
API Gateway: Check port 8080 logs
Frontend: Check browser console (F12)
```

### 4. Verify Database
```sql
-- Check if order exists
SELECT * FROM orders WHERE id = X;

-- Check if items exist
SELECT * FROM order_items WHERE order_id = X;

-- Check payment
SELECT * FROM payments WHERE order_id = X;
```

---

## 📞 DEBUGGING COMMANDS

```bash
# Check order service health
curl http://localhost:8084/actuator/health

# Test user orders endpoint
curl -H "X-User-Id: 1" http://localhost:8080/api/orders/user

# Test admin orders endpoint
curl http://localhost:8080/api/admin/orders?page=0&size=20

# Check specific order
curl http://localhost:8080/api/orders/12
```

---

## 🎯 EXPECTED BEHAVIOR

### After Placing Order:
1. Order created with status `PENDING`
2. Payment processed → status `CONFIRMED`, paymentStatus `COMPLETED`
3. Delivery assigned
4. Notification sent
5. Cart cleared
6. Order appears in My Orders
7. Order appears in Admin Dashboard

### After Status Update:
1. Admin updates status to `SHIPPED`
2. Order status changes in database
3. User receives notification
4. UI reflects new status

---

**Quick Reference Complete! Use this for rapid troubleshooting! ⚡**
