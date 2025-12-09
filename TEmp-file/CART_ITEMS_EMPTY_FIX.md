# Cart Items Empty - Complete Fix

## Current Situation

- ✅ Cart exists in `carts` table (user_id = 14, cart_id = 1)
- ❌ `cart_items` table is EMPTY
- ❌ Orders page shows "No orders yet"

## Root Cause

The `POST /api/cart/items` endpoint is being called but cart items are not being saved to the database.

## Diagnostic Steps

### 1. Run SQL Diagnostic
```sql
-- Run this in MySQL Workbench
source C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\CHECK_CART_DATABASE.sql
```

### 2. Check Browser Console
After adding item to cart, you should see:
```
🔑 Sending to backend: {productId: 1, quantity: 1}
✅ Cart synced to backend: {success: true, ...}
📦 Cart from backend: {data: {items: [...]}}
```

### 3. Check Cart-Service Logs
Should show:
```
CartService.addItem - userId: 14, productId: 1, quantity: 1
Cart retrieved/created - cartId: 1
Added new cart item for user: 14, product: 1
```

### 4. Check Network Tab
- Look for `POST http://localhost:8080/api/cart/items`
- Status should be 201 Created
- Response should show cart with items

## Possible Issues & Fixes

### Issue 1: Product Service Not Responding

If cart-service can't fetch product details, it throws an exception and doesn't save the item.

**Check logs for:**
```
Product not found
Product is not available
Insufficient stock
```

**Fix**: Ensure product-service is running and products exist:
```sql
USE revcart_products;
SELECT * FROM products WHERE id = 1;
```

### Issue 2: Transaction Rollback

If there's an exception after saving cart_item, the transaction rolls back.

**Fix**: Check cart-service logs for exceptions after "Added new cart item"

### Issue 3: Redis Cache Issue

Redis might be caching empty cart and not updating.

**Temporary Fix**: Disable Redis caching:
```yaml
# cart-service/application.yml
spring:
  cache:
    type: none
```

Then restart cart-service.

### Issue 4: Database Connection Issue

**Check**: Can cart-service write to database?
```sql
-- Check if cart-service can write
USE revcart_carts;
SHOW GRANTS FOR 'root'@'localhost';
```

## Complete Fix Steps

### Step 1: Restart Services with Logging

```powershell
# Stop all services
taskkill /F /IM java.exe

# Start cart-service with debug logging
cd c:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\cart-service
java -jar target\cart-service-1.0.0.jar

# Start gateway
cd ..\revcart-gateway
java -jar target\revcart-gateway-1.0.0.jar

# Start order-service
cd ..\order-service
java -jar target\order-service-1.0.0.jar
```

### Step 2: Test Manually via curl

```powershell
# Add item to cart
curl -X POST http://localhost:8080/api/cart/items ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 14" ^
  -d "{\"productId\": 1, \"quantity\": 2}"

# Check cart
curl -H "X-User-Id: 14" http://localhost:8080/api/cart

# Check database
# Run: SELECT * FROM revcart_carts.cart_items;
```

### Step 3: Test via Frontend

1. Clear browser cache and localStorage
2. Login again
3. Add product to cart
4. Check browser console for logs
5. Check database immediately:
```sql
SELECT * FROM revcart_carts.cart_items;
```

### Step 4: Place Order

1. Go to checkout
2. Fill address
3. Click "Place Order"
4. Check order-service logs for:
```
Cart fetched: X items, total: Y
ORDER SAVED === ID: Z
```

5. Check database:
```sql
SELECT * FROM revcart_orders.orders ORDER BY created_at DESC LIMIT 1;
SELECT * FROM revcart_orders.order_items WHERE order_id = <order_id>;
```

## Expected Flow

```
1. User clicks "Add to Cart"
   ↓
2. Frontend: addToCart() → sendCartItemToBackend()
   ↓
3. POST /api/cart/items with X-User-Id: 14
   ↓
4. Gateway routes to cart-service
   ↓
5. Cart-service: addItem()
   - Fetch product from product-service
   - Create/update cart_item
   - Save to database
   ↓
6. Response: {success: true, data: {items: [...]}}
   ↓
7. Frontend: loadCartFromBackend()
   ↓
8. Cart updated in UI and localStorage
```

## If Still Not Working

### Check Product Service

```powershell
# Test product service directly
curl http://localhost:8080/api/products/1

# Should return product details
```

### Check Cart Service Feign Client

The cart-service calls product-service via Feign. Check if it's configured correctly:

```yaml
# cart-service/application.yml
services:
  product-service:
    url: http://localhost:8080  # Should be gateway URL
```

### Enable SQL Logging

```yaml
# cart-service/application.yml
spring:
  jpa:
    show-sql: true
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

This will show actual SQL INSERT statements.

## Success Criteria

✅ Browser console shows: "✅ Cart synced to backend"
✅ Cart-service logs show: "Added new cart item for user: 14"
✅ Database query returns rows: `SELECT * FROM cart_items;`
✅ GET /api/cart returns items in response
✅ Checkout creates order with items
✅ Orders page displays the order

## Quick Test Script

```powershell
# 1. Clear everything
mysql -u root -p -e "DELETE FROM revcart_carts.cart_items; DELETE FROM revcart_carts.carts WHERE user_id = 14;"

# 2. Add item via curl
curl -X POST http://localhost:8080/api/cart/items -H "Content-Type: application/json" -H "X-User-Id: 14" -d "{\"productId\": 1, \"quantity\": 2}"

# 3. Check database
mysql -u root -p -e "SELECT * FROM revcart_carts.cart_items;"

# If this works, the backend is fine. Issue is in frontend sync.
# If this fails, check cart-service logs for errors.
```

---

**Next Step**: Run the diagnostic SQL script and check cart-service logs when adding items.
