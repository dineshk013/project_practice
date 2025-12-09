# RevCart Microservices API Testing Guide

## Prerequisites
- All services running (user, product, cart, order, payment, notification, delivery, analytics, gateway)
- Gateway running on port 8080
- Postman or curl installed

---

## 1. USER SERVICE (Port 8081)

### Register User
```bash
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

### Login
```bash
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

# Save the token from response
```

### Get Profile
```bash
GET http://localhost:8080/api/users/profile
X-User-Id: 1
```

### Add Address
```bash
POST http://localhost:8080/api/users/addresses
X-User-Id: 1
Content-Type: application/json

{
  "street": "123 Main St",
  "city": "Mumbai",
  "state": "Maharashtra",
  "zipCode": "400001",
  "country": "India"
}
```

### Get Addresses
```bash
GET http://localhost:8080/api/users/addresses
X-User-Id: 1
```

---

## 2. PRODUCT SERVICE (Port 8082)

### Get All Products
```bash
GET http://localhost:8080/api/products
```

### Get Product by ID
```bash
GET http://localhost:8080/api/products/1
```

### Create Product (Admin)
```bash
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "name": "Fresh Apples",
  "description": "Organic red apples",
  "price": 150.0,
  "stock": 100,
  "categoryId": 1,
  "imageUrl": "https://example.com/apple.jpg"
}
```

### Update Product
```bash
PUT http://localhost:8080/api/products/1
Content-Type: application/json

{
  "name": "Fresh Apples - Updated",
  "description": "Premium organic red apples",
  "price": 180.0,
  "stockQuantity": 80,
  "categoryId": 1,
  "imageUrl": "https://example.com/apple.jpg",
  "active": true
}
```

### Get All Categories
```bash
GET http://localhost:8080/api/categories
```

### Create Category
```bash
POST http://localhost:8080/api/categories
Content-Type: application/json

{
  "name": "Fruits",
  "description": "Fresh fruits"
}
```

---

## 3. CART SERVICE (Port 8083)

### Add Item to Cart
```bash
POST http://localhost:8080/api/cart/items
X-User-Id: 1
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

### Get Cart
```bash
GET http://localhost:8080/api/cart
X-User-Id: 1
```

### Update Cart Item
```bash
PUT http://localhost:8080/api/cart/items/1?quantity=3
X-User-Id: 1
```

### Remove Item from Cart
```bash
DELETE http://localhost:8080/api/cart/items/1
X-User-Id: 1
```

### Get Cart Count
```bash
GET http://localhost:8080/api/cart/count
X-User-Id: 1
```

### Clear Cart
```bash
DELETE http://localhost:8080/api/cart/clear
X-User-Id: 1
```

---

## 4. ORDER SERVICE (Port 8084)

### Checkout (Create Order)
```bash
POST http://localhost:8080/api/orders/checkout
X-User-Id: 1
Content-Type: application/json

{
  "paymentMethod": "COD",
  "addressId": 1
}
```

### Get User Orders
```bash
GET http://localhost:8080/api/orders
X-User-Id: 1
```

### Get Order by ID
```bash
GET http://localhost:8080/api/orders/1
```

### Update Order Status
```bash
PUT http://localhost:8080/api/orders/1/status?status=CONFIRMED
```

### Cancel Order
```bash
POST http://localhost:8080/api/orders/1/cancel
X-User-Id: 1
```

---

## 5. PAYMENT SERVICE (Port 8085)

### Initiate Payment
```bash
POST http://localhost:8080/api/payments/initiate
Content-Type: application/json

{
  "orderId": 1,
  "userId": 1,
  "amount": 500.0,
  "paymentMethod": "RAZORPAY"
}
```

### Verify Payment
```bash
POST http://localhost:8080/api/payments/verify
Content-Type: application/json

{
  "paymentId": 1,
  "orderId": 1,
  "transactionId": "TXN123456",
  "status": "SUCCESS"
}
```

### Get Payment by Order ID
```bash
GET http://localhost:8080/api/payments/order/1
```

### Refund Payment
```bash
POST http://localhost:8080/api/payments/refund/1
Content-Type: application/json

{
  "reason": "Product damaged"
}

Note: Use orderId (not paymentId) in the URL
```

---

## 6. NOTIFICATION SERVICE (Port 8086)

### Get User Notifications
```bash
GET http://localhost:8080/api/notifications/user/1
```

### Mark as Read
```bash
PUT http://localhost:8080/api/notifications/1/read
```

---

## 7. DELIVERY SERVICE (Port 8087)

### Assign Delivery
```bash
POST http://localhost:8080/api/delivery/assign
Content-Type: application/json

{
  "orderId": 1,
  "userId": 1,
  "agentId": 101,
  "estimatedDelivery": "2025-12-10T10:00:00"
}
```

### Update Delivery Status
```bash
PUT http://localhost:8080/api/delivery/1/status
Content-Type: application/json

{
  "status": "PICKED_UP",
  "notes": "Package picked up from warehouse"
}
```

### Get Delivery by Order ID
```bash
GET http://localhost:8080/api/delivery/1
```

### Get User Deliveries
```bash
GET http://localhost:8080/api/delivery/user/1
```

### Track Delivery
```bash
GET http://localhost:8080/api/delivery/1/track
```

---

## 8. ANALYTICS SERVICE (Port 8088)

### Get Order Summary
```bash
GET http://localhost:8080/api/analytics/orders/summary
```

### Get Product Summary
```bash
GET http://localhost:8080/api/analytics/products/summary
```

### Get User Summary
```bash
GET http://localhost:8080/api/analytics/users/summary
```

### Get Top Products
```bash
GET http://localhost:8080/api/analytics/top-products
```

### Get Low Stock Products
```bash
GET http://localhost:8080/api/analytics/low-stock
```

### Get Dashboard
```bash
GET http://localhost:8080/api/analytics/dashboard
```

---

## Complete E2E Test Flow

### Step 1: Register & Login
```bash
# Register
POST http://localhost:8080/api/users/register
{"name": "Test User", "email": "test@example.com", "password": "test123", "phone": "9876543210"}

# Login
POST http://localhost:8080/api/users/login
{"email": "test@example.com", "password": "test123"}
```

### Step 2: Add Address
```bash
POST http://localhost:8080/api/users/addresses
X-User-Id: 1
{"street": "123 Test St", "city": "Mumbai", "state": "MH", "zipCode": "400001", "country": "India"}
```

### Step 3: Browse Products
```bash
GET http://localhost:8080/api/products
```

### Step 4: Add to Cart
```bash
POST http://localhost:8080/api/cart/items
X-User-Id: 1
{"productId": 1, "quantity": 2}
```

### Step 5: View Cart
```bash
GET http://localhost:8080/api/cart
X-User-Id: 1
```

### Step 6: Checkout
```bash
POST http://localhost:8080/api/orders/checkout
X-User-Id: 1
{"paymentMethod": "COD", "addressId": 1}
```

### Step 7: View Orders
```bash
GET http://localhost:8080/api/orders
X-User-Id: 1
```

### Step 8: Track Delivery
```bash
GET http://localhost:8080/api/delivery/1
```

### Step 9: View Analytics
```bash
GET http://localhost:8080/api/analytics/dashboard
```

---

## Health Checks

### Check All Services
```bash
# Gateway
GET http://localhost:8080/actuator/health

# User Service
GET http://localhost:8081/actuator/health

# Product Service
GET http://localhost:8082/actuator/health

# Cart Service
GET http://localhost:8083/actuator/health

# Order Service
GET http://localhost:8084/actuator/health

# Payment Service
GET http://localhost:8085/actuator/health

# Notification Service
GET http://localhost:8086/actuator/health

# Delivery Service
GET http://localhost:8087/actuator/health

# Analytics Service
GET http://localhost:8088/actuator/health
```

---

## Quick Test Script (PowerShell)

```powershell
# Test all health endpoints
$services = @(8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088)
foreach ($port in $services) {
    Write-Host "Testing port $port..."
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing
        Write-Host "Port $port: OK" -ForegroundColor Green
    } catch {
        Write-Host "Port $port: FAILED" -ForegroundColor Red
    }
}
```

---

## Expected Response Codes

- **200 OK**: Successful GET/PUT/DELETE
- **201 Created**: Successful POST (resource created)
- **400 Bad Request**: Invalid input
- **401 Unauthorized**: Missing/invalid token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## Notes

- Replace `X-User-Id: 1` with actual user ID from login
- For authenticated endpoints, add `Authorization: Bearer {token}` header
- All timestamps are in ISO 8601 format
- All amounts are in INR (Indian Rupees)
