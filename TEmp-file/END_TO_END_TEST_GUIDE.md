# 🧪 END-TO-END TEST GUIDE
## RevCart Microservices - Complete System Validation

**Last Updated:** 2024-12-08  
**Status:** READY FOR TESTING

---

## 🎯 WHAT WAS FIXED

### ✅ Order Persistence
- **Fixed:** Transaction rollback from Feign client failures
- **Added:** Comprehensive logging at every step
- **Changed:** Post-order operations (payment, delivery, notifications) are now non-blocking
- **Result:** Orders now save to `revcart_orders.orders` table

### ✅ Frontend Order Service
- **Fixed:** Response handling to use `ApiResponse<T>` format
- **Changed:** `getUserOrders()` now reads `response.data`
- **Result:** "My Orders" page will display orders correctly

### ✅ Email OTP Service
- **Added:** Enhanced logging with ✅/❌ indicators
- **Added:** Test email endpoint: `POST /api/users/test-email?email=your@email.com`
- **Added:** Resend OTP endpoint: `POST /api/users/resend-otp?email=your@email.com`
- **Result:** Clear error messages if SMTP fails

### ✅ WebSocket & Notifications
- **Already Fixed:** CORS configuration (no duplicates)
- **Already Working:** Notification persistence to MongoDB
- **Already Working:** WebSocket sends notifications to UI

---

## 📋 PRE-TEST CHECKLIST

### 1. Environment Setup

```powershell
# Set email credentials (REQUIRED for OTP testing)
set MAIL_USERNAME=your-real-email@gmail.com
set MAIL_PASSWORD=your-16-char-app-password

# Verify they're set
echo %MAIL_USERNAME%
echo %MAIL_PASSWORD%
```

### 2. Database Cleanup (Optional - Fresh Start)

```sql
-- Clean up old test data
USE revcart_carts;
DELETE FROM cart_items;
DELETE FROM carts;

USE revcart_orders;
DELETE FROM order_items;
DELETE FROM orders;

-- Verify clean state
SELECT COUNT(*) FROM revcart_carts.cart_items;
SELECT COUNT(*) FROM revcart_orders.orders;
```

### 3. Start All Services

```powershell
# Stop any running services
.\stop-all.ps1

# Start all services
.\start-all.ps1

# Wait 2-3 minutes for all services to start
Start-Sleep -Seconds 180

# Check health
.\check-services.ps1
```

Expected output:
```
✅ Gateway (8080) - Running
✅ User Service (8081) - Running
✅ Product Service (8082) - Running
✅ Cart Service (8083) - Running
✅ Order Service (8084) - Running
✅ Payment Service (8085) - Running
✅ Notification Service (8086) - Running
✅ Delivery Service (8087) - Running
✅ Analytics Service (8088) - Running
✅ Frontend (4200) - Running
```

---

## 🧪 TEST SEQUENCE

### TEST 1: Email Configuration ⚡ CRITICAL

**Purpose:** Verify SMTP is configured correctly

**Steps:**
```bash
# Test email endpoint
curl -X POST "http://localhost:8080/api/users/test-email?email=your-email@gmail.com"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Check your inbox. If you don't receive it, verify MAIL_USERNAME and MAIL_PASSWORD env vars.",
  "data": "Test email sent to your-email@gmail.com"
}
```

**Verify:**
- [ ] Check your email inbox
- [ ] Email received within 30 seconds
- [ ] Subject: "RevCart - Test Email"
- [ ] Body: "If you received this, your email configuration is working correctly!"

**If Email NOT Received:**
1. Check user-service logs for errors
2. Look for: "✅ Test email sent successfully" or "❌ Failed to send test email"
3. Verify MAIL_USERNAME and MAIL_PASSWORD are set correctly
4. Ensure Gmail App Password is used (not regular password)

---

### TEST 2: User Registration with OTP

**Purpose:** Test complete registration flow with email verification

**Steps:**

**2.1 Register New User**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Customer",
    "email": "test-customer@example.com",
    "password": "test123",
    "phone": "1234567890"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 15,
      "email": "test-customer@example.com",
      "name": "Test Customer",
      "role": "USER"
    }
  }
}
```

**Verify:**
- [ ] Response status: 201 Created
- [ ] Token received
- [ ] User ID received

**2.2 Request OTP (Optional - if implementing email verification)**
```bash
curl -X POST "http://localhost:8080/api/users/resend-otp?email=test-customer@example.com"
```

**Verify:**
- [ ] OTP email received
- [ ] OTP is 6 digits
- [ ] Email has nice HTML formatting

---

### TEST 3: Login as Customer

**Purpose:** Verify authentication and token generation

**Steps:**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test-customer@example.com",
    "password": "test123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 15,
      "email": "test-customer@example.com",
      "name": "Test Customer",
      "role": "USER"
    }
  }
}
```

**Save for next tests:**
```powershell
$TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
$USER_ID = 15
```

**Verify:**
- [ ] Response status: 200 OK
- [ ] Token received
- [ ] User details correct

---

### TEST 4: Add Products to Cart

**Purpose:** Verify cart persistence to database

**Steps:**

**4.1 Get Products**
```bash
curl http://localhost:8080/api/products
```

**4.2 Add First Product to Cart**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Item added to cart",
  "data": {
    "id": 1,
    "userId": 15,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99,
        "imageUrl": "..."
      }
    ],
    "totalPrice": 199.98,
    "totalItems": 2
  }
}
```

**4.3 Verify in Database**
```sql
USE revcart_carts;
SELECT * FROM carts WHERE user_id = 15;
SELECT * FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = 15);
```

**Expected:**
- [ ] 1 row in `carts` table
- [ ] 1 row in `cart_items` table
- [ ] `product_id = 1`, `quantity = 2`

**4.4 Add Second Product**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d '{
    "productId": 2,
    "quantity": 1
  }'
```

**4.5 Get Cart**
```bash
curl -H "X-User-Id: 15" http://localhost:8080/api/cart
```

**Expected:**
- [ ] Cart has 2 items
- [ ] Total items: 3 (2 + 1)
- [ ] Total price calculated correctly

---

### TEST 5: Add Delivery Address

**Purpose:** Create address for checkout

**Steps:**
```bash
curl -X POST http://localhost:8080/api/profile/addresses \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d '{
    "line1": "123 Test Street",
    "city": "Test City",
    "state": "Test State",
    "postalCode": "12345",
    "country": "India",
    "primaryAddress": true
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Address added successfully",
  "data": {
    "id": 1,
    "line1": "123 Test Street",
    "city": "Test City",
    "state": "Test State",
    "postalCode": "12345",
    "country": "India",
    "primaryAddress": true
  }
}
```

**Save address ID:**
```powershell
$ADDRESS_ID = 1
```

**Verify:**
- [ ] Response status: 201 Created
- [ ] Address ID received

---

### TEST 6: Checkout & Order Creation ⚡ CRITICAL

**Purpose:** Verify order persistence to database

**Steps:**

**6.1 Place Order**
```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d '{
    "addressId": 1,
    "paymentMethod": "COD"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "userId": 15,
    "orderNumber": "ORD-1733598123456",
    "status": "PENDING",
    "totalAmount": 299.97,
    "paymentStatus": "COD",
    "paymentMethod": "COD",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Product 2",
        "quantity": 1,
        "price": 100.00
      }
    ],
    "createdAt": "2024-12-08T02:15:23"
  }
}
```

**6.2 Check Order Service Logs**
Look for these log messages:
```
=== CHECKOUT START === userId: 15, addressId: 1, paymentMethod: COD
Fetching cart for userId: 15
Cart fetched: 2 items, total: 299.97
Address found: Test City, Test State
Creating order entity for userId: 15
Order entity created with 2 items
Saving order to database...
=== ORDER SAVED === ID: 1, OrderNumber: ORD-1733598123456
Order exists in DB after save: true
COD payment status updated for order: ORD-1733598123456
Cart cleared for userId: 15
=== CHECKOUT COMPLETE === OrderID: 1, OrderNumber: ORD-1733598123456
```

**6.3 Verify in Database**
```sql
USE revcart_orders;

-- Check order
SELECT * FROM orders WHERE user_id = 15;

-- Check order items
SELECT * FROM order_items WHERE order_id = (SELECT id FROM orders WHERE user_id = 15 LIMIT 1);

-- Verify cart is cleared
USE revcart_carts;
SELECT * FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = 15);
```

**Expected:**
- [ ] 1 row in `orders` table
- [ ] 2 rows in `order_items` table
- [ ] 0 rows in `cart_items` table (cart cleared)
- [ ] Order status: PENDING
- [ ] Payment status: COD

**If Order NOT in Database:**
1. Check order-service logs for errors
2. Look for "ORDER NOT FOUND IN DB AFTER SAVE!"
3. Check for exceptions after "Saving order to database..."
4. Verify MySQL connection is working

---

### TEST 7: Get User Orders

**Purpose:** Verify "My Orders" page will work

**Steps:**
```bash
curl -H "X-User-Id: 15" http://localhost:8080/api/orders
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 15,
      "orderNumber": "ORD-1733598123456",
      "status": "PENDING",
      "totalAmount": 299.97,
      "paymentStatus": "COD",
      "items": [...],
      "createdAt": "2024-12-08T02:15:23"
    }
  ]
}
```

**Verify:**
- [ ] Response status: 200 OK
- [ ] Array contains 1 order
- [ ] Order details match what was created

---

### TEST 8: Frontend UI Testing

**Purpose:** Verify complete flow in browser

**Steps:**

**8.1 Open Frontend**
```
http://localhost:4200
```

**8.2 Register/Login**
- [ ] Click "Sign Up" or "Login"
- [ ] Enter credentials
- [ ] Successfully logged in
- [ ] Redirected to home page

**8.3 Browse Products**
- [ ] Products displayed
- [ ] Images loading
- [ ] Prices showing

**8.4 Add to Cart**
- [ ] Click "Add to Cart" on 2-3 products
- [ ] Cart icon shows count
- [ ] Click cart icon
- [ ] Cart page shows items

**8.5 Checkout**
- [ ] Click "Proceed to Checkout"
- [ ] Fill/select delivery address
- [ ] Select payment method: COD
- [ ] Click "Place Order"
- [ ] Success message shown
- [ ] Redirected to "My Orders" page

**8.6 Verify My Orders Page**
- [ ] Order appears in list
- [ ] Order number displayed
- [ ] Status: Pending
- [ ] Items listed correctly
- [ ] Total amount correct

**8.7 Check Browser Console**
- [ ] No CORS errors
- [ ] No 403 Forbidden errors
- [ ] WebSocket connected (if notifications enabled)
- [ ] No red error messages

---

### TEST 9: WebSocket Notifications (Optional)

**Purpose:** Verify real-time notifications

**Steps:**

**9.1 Open Browser DevTools**
- Press F12
- Go to Console tab

**9.2 Check WebSocket Connection**
Look for:
```
WebSocket connection established
STOMP connected
Subscribed to /topic/user/15
```

**9.3 Place Another Order**
- Add items to cart
- Checkout
- Watch console for notification

**Expected:**
- [ ] Notification received via WebSocket
- [ ] Message: "Your order #X has been placed successfully"
- [ ] Notification appears in UI (bell icon)

**9.4 Verify in MongoDB**
```javascript
// In MongoDB Compass or mongo shell
use revcart_notifications
db.notifications.find({ userId: 15 })
```

**Expected:**
- [ ] Notification document exists
- [ ] Type: "ORDER_PLACED"
- [ ] Message contains order ID
- [ ] `read: false`

---

### TEST 10: Admin Dashboard (Optional)

**Purpose:** Verify admin can see orders

**Steps:**

**10.1 Login as Admin**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@revcart.com",
    "password": "admin123"
  }'
```

**10.2 Get All Orders**
```bash
curl http://localhost:8080/api/orders/all
```

**Expected:**
- [ ] Array of all orders
- [ ] Includes customer orders

**10.3 Frontend Admin Dashboard**
- [ ] Login as admin
- [ ] Navigate to `/admin`
- [ ] See products list
- [ ] See orders list (if implemented)

---

## 🐛 TROUBLESHOOTING

### Issue: Order Not Saved

**Symptoms:**
- Checkout returns 200 OK
- But `SELECT * FROM orders` shows empty

**Debug Steps:**
1. Check order-service logs
2. Look for "=== ORDER SAVED ===" message
3. If missing, look for exceptions before it
4. Check "Order exists in DB after save: false"

**Common Causes:**
- Feign client failure causing rollback (FIXED)
- Database connection issue
- Transaction not committing

**Solution:**
```sql
-- Test direct insert
USE revcart_orders;
INSERT INTO orders (user_id, order_number, status, total_amount, payment_status, payment_method, created_at, updated_at)
VALUES (15, 'TEST-001', 'PENDING', 99.99, 'COD', 'COD', NOW(), NOW());

SELECT * FROM orders WHERE order_number = 'TEST-001';
```

If direct insert works, issue is in application code.

---

### Issue: Cart Empty After Adding Items

**Symptoms:**
- Add to cart returns 200 OK
- But `SELECT * FROM cart_items` shows empty

**Debug Steps:**
1. Check cart-service logs
2. Look for "CartService.addItem - userId: X, productId: Y"
3. Check for "Added new cart item" or "Updated existing cart item"

**Solution:**
Already fixed in CartService. Ensure you're using latest build.

---

### Issue: Email Not Received

**Symptoms:**
- Test email endpoint returns success
- But no email in inbox

**Debug Steps:**
1. Check user-service logs
2. Look for "✅ OTP email sent successfully" or "❌ Failed to send OTP email"
3. Check SMTP error messages

**Common Causes:**
- MAIL_USERNAME not set
- MAIL_PASSWORD not set or wrong
- Using regular password instead of App Password
- Gmail blocking less secure apps

**Solution:**
```powershell
# Verify env vars
echo %MAIL_USERNAME%
echo %MAIL_PASSWORD%

# Generate new App Password
# https://myaccount.google.com/apppasswords

# Restart user-service
cd user-service
mvn spring-boot:run
```

---

### Issue: WebSocket 403 Forbidden

**Symptoms:**
- Browser console: "Failed to load resource: 403"
- "/ws/info" returns 403

**Debug Steps:**
1. Check browser Network tab
2. Look at /ws/info response headers
3. Check for duplicate Access-Control-Allow-Origin

**Solution:**
Already fixed in WebSocketConfig. Ensure:
- Gateway CORS uses array format
- WebSocket has `.setAllowedOrigins("http://localhost:4200")`

---

## ✅ SUCCESS CRITERIA

System is fully working when:

- [ ] Test email received
- [ ] User can register
- [ ] User can login
- [ ] Products displayed
- [ ] Add to cart works
- [ ] `cart_items` table has rows
- [ ] Checkout succeeds
- [ ] `orders` table has rows
- [ ] `order_items` table has rows
- [ ] "My Orders" page shows orders
- [ ] No CORS errors in console
- [ ] WebSocket connected
- [ ] Notifications received
- [ ] MongoDB has notification documents

---

## 📊 FINAL VERIFICATION QUERIES

Run these to verify everything:

```sql
-- Cart data
USE revcart_carts;
SELECT 
    c.id as cart_id,
    c.user_id,
    COUNT(ci.id) as item_count,
    SUM(ci.quantity * ci.price) as total
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
GROUP BY c.id, c.user_id;

-- Order data
USE revcart_orders;
SELECT 
    o.id,
    o.user_id,
    o.order_number,
    o.status,
    o.payment_status,
    o.total_amount,
    COUNT(oi.id) as item_count,
    o.created_at
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id
ORDER BY o.created_at DESC;

-- User data
USE revcart_users;
SELECT id, email, name, role, active, created_at
FROM users
ORDER BY created_at DESC
LIMIT 10;
```

---

**Test Status:** READY  
**Next Action:** Run tests in sequence and report results
