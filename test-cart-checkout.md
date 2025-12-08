# Cart & Checkout Testing Guide

## Prerequisites
- Gateway running on port 8080
- Cart-service running on port 8083
- Order-service running on port 8084
- User-service running on port 8081
- Product-service running on port 8082
- MySQL database `revcart_carts` exists

## A) CURL Tests

### 1. Get Empty Cart
```bash
curl -H "X-User-Id: 14" http://localhost:8080/api/cart
```
**Expected Response:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": 1,
    "userId": 14,
    "items": [],
    "totalPrice": 0.0,
    "totalItems": 0
  }
}
```

### 2. Add Item to Cart
```bash
curl -H "X-User-Id: 14" -H "Content-Type: application/json" -X POST http://localhost:8080/api/cart/items -d "{\"productId\":1,\"quantity\":1}"
```
**Expected Response:**
```json
{
  "success": true,
  "message": "Item added to cart",
  "data": {
    "id": 1,
    "userId": 14,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 1,
        "price": 99.99,
        "imageUrl": "..."
      }
    ],
    "totalPrice": 99.99,
    "totalItems": 1
  }
}
```

### 3. Get Cart with Items
```bash
curl -H "X-User-Id: 14" http://localhost:8080/api/cart
```
**Expected:** Cart with 1 item

### 4. Checkout
```bash
curl -H "X-User-Id: 14" -H "Content-Type: application/json" -X POST http://localhost:8080/api/orders/checkout -d "{\"addressId\":1,\"paymentMethod\":\"COD\"}"
```
**Expected Response:**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "userId": 14,
    "totalAmount": 105.98,
    "status": "PENDING",
    ...
  }
}
```

## B) SQL Verification

### 1. Check Carts Table
```sql
USE revcart_carts;
SELECT * FROM carts WHERE user_id = 14;
```
**Expected:** 1 row with cart_id

### 2. Check Cart Items Table
```sql
SELECT * FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = 14);
```
**Expected:** Rows with product_id, quantity, price

### 3. Verify Database Name
```sql
SHOW DATABASES LIKE 'revcart_cart%';
```
**Expected:** Only `revcart_carts` (not `revcart_cart`)

## C) Backend Logs to Check

### Cart-Service Logs
```
GET /api/cart - X-User-Id: 14
CartService.getCart - userId: 14
Cart found/created - cartId: 1, userId: 14
Loaded 0 items from database

POST /api/cart/items - X-User-Id: 14, productId: 1
CartService.addItem - userId: 14, productId: 1, quantity: 1
Cart retrieved/created - cartId: 1
Added new cart item for user: 14, product: 1
```

### Gateway Logs
```
Routing request to http://localhost:8083/api/cart
Headers: X-User-Id=14
```

## D) Frontend Testing

### 1. Login
- Go to http://localhost:4200/auth/login
- Login with valid credentials
- Check localStorage: `revcart_user` and `revcart_token` exist

### 2. Add to Cart
- Browse products
- Click "Add to Cart"
- Check Network tab: POST /api/cart/items with X-User-Id header

### 3. View Cart
- Go to cart page
- Verify items are displayed
- Check Network tab: GET /api/cart returns items

### 4. Checkout
- Go to checkout
- Fill address or select existing
- Click "Place Order"
- Check Network tab:
  - DELETE /api/cart/clear
  - POST /api/cart/items (for each item)
  - POST /api/orders/checkout

## E) WebSocket Testing

### 1. Check WebSocket Connection
Open browser console on http://localhost:4200:
```javascript
// Should see:
WebSocket connection to 'ws://localhost:8080/ws/...' succeeded
STOMP connected
```

### 2. Check CORS Headers
Network tab → /ws/info → Response Headers:
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Credentials: true
```
**Should NOT see:** Multiple values in Access-Control-Allow-Origin

## Success Criteria

✅ GET /api/cart returns 200 with cart data
✅ POST /api/cart/items creates row in cart_items table
✅ POST /api/orders/checkout creates order
✅ WebSocket connects without CORS errors
✅ X-User-Id header is forwarded to all services
✅ Database `revcart_carts` contains cart and cart_items data
✅ Frontend checkout page shows cart items
✅ No 500 errors in cart-service
✅ No "Cart is empty" error during checkout
