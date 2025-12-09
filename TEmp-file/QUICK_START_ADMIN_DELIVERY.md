# ⚡ QUICK START - Admin & Delivery Testing

## 🚀 START SERVICES

```powershell
# Terminal 1: Start Order Service
cd order-service
mvn spring-boot:run

# Terminal 2: Start Frontend
cd Frontend
npm start
```

## 🧪 TEST SCENARIOS

### ✅ Test 1: User Orders (My Orders Page)

**Login:**
- Email: `user@example.com`
- Password: `password123`

**Expected:**
- Redirects to `/`
- Can place order
- My Orders page shows orders: http://localhost:4200/orders

**API Call:**
```bash
curl -H "X-User-Id: 1" http://localhost:8080/api/orders/user
```

---

### ✅ Test 2: Admin Dashboard

**Login:**
- Email: `admin@example.com`
- Password: `admin123`

**Expected:**
- Redirects to `/admin`
- Dashboard shows stats (not zeros)
- Can view all orders
- Can update order status

**API Calls:**
```bash
# Get dashboard stats
curl http://localhost:8080/api/admin/dashboard/stats

# Get paginated orders
curl http://localhost:8080/api/admin/orders?page=0&size=10

# Update order status
curl -X POST http://localhost:8080/api/admin/orders/12/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
```

---

### ✅ Test 3: Delivery Agent

**Login:**
- Email: `delivery@example.com`
- Password: `delivery123`

**Expected:**
- Redirects to `/delivery`
- Can see assigned orders
- Can update delivery status

**API Calls:**
```bash
# Get delivery orders
curl http://localhost:8080/api/delivery/orders

# Update delivery status
curl -X POST http://localhost:8080/api/delivery/orders/12/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'
```

---

## 🔍 QUICK VERIFICATION

### Check Backend is Running
```bash
curl http://localhost:8084/actuator/health
# Should return: {"status":"UP"}
```

### Check Database Has Orders
```sql
SELECT COUNT(*) FROM orders;
-- Should return > 0

SELECT * FROM orders WHERE user_id = 1 LIMIT 5;
-- Should show user's orders
```

### Check Frontend Console
```
Open browser console (F12)
Should NOT see:
❌ 500 errors
❌ "Order not found"
❌ Empty responses

Should see:
✅ "Orders retrieved successfully"
✅ Successful API responses
```

---

## 🎯 SUCCESS CHECKLIST

- [ ] Order service builds successfully
- [ ] Frontend starts without errors
- [ ] User can login and see orders
- [ ] Admin can login and see dashboard
- [ ] Admin dashboard shows correct stats
- [ ] Admin can view all orders (paginated)
- [ ] Admin can update order status
- [ ] Delivery agent can login
- [ ] Delivery agent can see orders
- [ ] Delivery agent can update status
- [ ] No 500 errors in console
- [ ] No empty responses

---

## 🐛 TROUBLESHOOTING

### Issue: My Orders page empty
**Solution**: 
1. Check if orders exist in database
2. Check browser console for API errors
3. Verify `/api/orders/user` returns data

### Issue: Admin dashboard shows zeros
**Solution**:
1. Check if orders exist in database
2. Verify `/api/admin/dashboard/stats` returns data
3. Check backend logs for errors

### Issue: 500 error on admin orders
**Solution**:
1. Verify order-service is running
2. Check backend logs
3. Verify database connection

### Issue: Role-based navigation not working
**Solution**:
1. Check localStorage for user role
2. Verify auth.service.ts mapRole() function
3. Check login.component.ts navigation logic

---

## 📊 EXPECTED RESPONSES

### User Orders
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [...]
}
```

### Admin Stats
```json
{
  "totalOrders": 50,
  "totalRevenue": 125000.50,
  "totalProducts": 0,
  "totalUsers": 0
}
```

### Admin Orders (Paginated)
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5
}
```

---

**All systems ready! Start testing! 🚀**
