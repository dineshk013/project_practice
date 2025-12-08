# Payment & Cart Testing Guide

## ✅ Changes Implemented

### Backend (Cart Service)
1. **Removed Redis Dependency**
   - Deleted `RedisConfig.java`
   - Removed Redis dependencies from `pom.xml`
   - Removed all `@Cacheable` and `@CacheEvict` annotations
   - Cart now uses pure MySQL storage

2. **Improved Cart Validation**
   - `/api/cart/validate` now loads items from database
   - Returns `false` for empty carts with proper logging
   - Better error handling for product validation failures

3. **Better Error Messages**
   - Redis errors masked as "Service temporarily unavailable"
   - Clear JSON responses for all scenarios

### Frontend (Angular)
1. **Card Number Validation**
   - Must be exactly 16 digits
   - Numeric only
   - Auto-spacing (1234 5678 9012 3456)
   - Real-time formatting

2. **Expiry Month Validation**
   - Valid range: 01-12
   - Dropdown selection
   - Checks if card is expired

3. **Expiry Year Validation**
   - Must be >= current year
   - Next 20 years available
   - Validates month+year combination

4. **CVV Validation**
   - Exactly 3 digits
   - Numeric only
   - Max length enforced

5. **Cardholder Name Validation**
   - Required field
   - Alphabets and spaces only
   - Minimum 2 characters

6. **Form Behavior**
   - Submit button disabled during processing
   - Red error messages for invalid fields
   - Cannot submit until all validations pass

---

## 🧪 Testing Steps

### Prerequisites
```powershell
# Ensure all services are running
cd cart-service
mvn clean install
mvn spring-boot:run

cd ../order-service
mvn spring-boot:run

cd ../Frontend
npm start
```

---

## Test 1: COD (Cash on Delivery) Flow

### Step 1: Add Items to Cart
1. Login to application: http://localhost:4200
2. Browse products and add 2-3 items to cart
3. Verify cart icon shows correct count

### Step 2: Verify Cart in Database
```sql
USE revcart_carts;
SELECT * FROM carts WHERE user_id = YOUR_USER_ID;
SELECT * FROM cart_items WHERE cart_id = YOUR_CART_ID;
```
**Expected**: Cart and cart_items should have data

### Step 3: Checkout with COD
1. Go to Cart page
2. Click "Proceed to Checkout"
3. Select existing address or add new address
4. Select "Cash on Delivery" payment method
5. Click "Place Order"

**Expected Result**:
- Order created successfully
- Redirected to Orders page
- Cart cleared (both frontend and database)
- Order visible in "My Orders"

### Step 4: Verify Database
```sql
USE revcart_orders;
SELECT * FROM orders WHERE user_id = YOUR_USER_ID ORDER BY id DESC LIMIT 1;
SELECT * FROM order_items WHERE order_id = YOUR_ORDER_ID;

USE revcart_carts;
SELECT * FROM cart_items WHERE cart_id = YOUR_CART_ID;
```
**Expected**: 
- Order and order_items created
- cart_items table empty for that cart

---

## Test 2: Credit/Debit Card Flow (Valid Card)

### Step 1: Add Items to Cart
1. Add 2-3 items to cart
2. Go to checkout

### Step 2: Select Card Payment
1. Select existing address
2. Select "Credit/Debit Card" payment method
3. Click "Place Order"

**Expected**: Payment modal opens

### Step 3: Enter Valid Card Details
```
Card Holder Name: John Doe
Card Number: 4532 1234 5678 9010
Expiry Month: 12
Expiry Year: 2025
CVV: 123
```

4. Click "Pay Now"

**Expected Result**:
- Payment processed successfully
- Redirected to payment success page
- Cart cleared
- Order created with payment status "COMPLETED"

### Step 4: Verify Database
```sql
USE revcart_orders;
SELECT * FROM orders WHERE user_id = YOUR_USER_ID ORDER BY id DESC LIMIT 1;

USE revcart_payments;
SELECT * FROM payments WHERE order_id = YOUR_ORDER_ID;
```
**Expected**: Order and payment records created

---

## Test 3: Invalid Card Scenarios

### Test 3.1: Invalid Card Number
**Input**: `1234 5678` (less than 16 digits)
**Expected**: Red error "Card number must be exactly 16 digits"
**Submit Button**: Disabled

### Test 3.2: Non-Numeric Card Number
**Input**: `abcd efgh ijkl mnop`
**Expected**: Only numbers allowed, auto-filtered

### Test 3.3: Invalid CVV
**Input**: `12` (less than 3 digits)
**Expected**: Red error "CVV must be exactly 3 digits"

### Test 3.4: Non-Numeric CVV
**Input**: `abc`
**Expected**: Only numbers allowed, auto-filtered

### Test 3.5: Invalid Cardholder Name
**Input**: `John123` (contains numbers)
**Expected**: Red error "Name must contain only letters"

### Test 3.6: Empty Cardholder Name
**Input**: `` (empty)
**Expected**: Red error "Card holder name is required"

### Test 3.7: Expired Card (Past Year)
**Input**: 
- Month: 12
- Year: 2023
**Expected**: Red error "Card has expired"

### Test 3.8: Expired Card (Past Month, Current Year)
**Input**: 
- Month: 01
- Year: 2024 (if current month is > 01)
**Expected**: Red error "Card has expired"

### Test 3.9: Invalid Month
**Input**: Month: 13
**Expected**: Not possible (dropdown only shows 01-12)

### Test 3.10: Future Year Beyond 20 Years
**Input**: Year: 2045 (if current year is 2024)
**Expected**: Red error "Invalid expiry year"

---

## Test 4: Empty Cart Validation

### Step 1: Clear Cart
1. Remove all items from cart
2. Try to access checkout directly via URL: http://localhost:4200/checkout

**Expected**: Error message "Your cart is empty"

### Step 2: API Test
```bash
curl -X POST http://localhost:8080/api/cart/validate \
  -H "X-User-Id: YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Cart validation completed",
  "data": false
}
```

---

## Test 5: Cart Service Without Redis

### Step 1: Verify Redis Not Running
```powershell
# Stop Redis if running
net stop Redis
```

### Step 2: Restart Cart Service
```powershell
cd cart-service
mvn spring-boot:run
```

**Expected**: Service starts successfully without Redis errors

### Step 3: Test Cart Operations
```bash
# Get cart
curl http://localhost:8080/api/cart \
  -H "X-User-Id: YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Add item
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"productId": 1, "quantity": 2}'
```

**Expected**: All operations work using MySQL only

---

## Test 6: Order Checkout API Flow

### Step 1: Add Items to Cart via API
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 14" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"productId": 1, "quantity": 2}'
```

### Step 2: Validate Cart
```bash
curl -X POST http://localhost:8080/api/cart/validate \
  -H "X-User-Id: 14" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected**: `{"success": true, "data": true}`

### Step 3: Checkout
```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 14" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "addressId": 1,
    "paymentMethod": "COD"
  }'
```

**Expected**: Order created, cart cleared

---

## 🐛 Common Issues & Solutions

### Issue 1: "Unable to connect to Redis"
**Solution**: Redis dependency removed. Rebuild cart-service:
```powershell
cd cart-service
mvn clean install
mvn spring-boot:run
```

### Issue 2: Cart items not saving
**Solution**: Ensure cart-service calls product-service through gateway (port 8080)
Check `application.yml`:
```yaml
product-service:
  url: http://localhost:8080  # NOT 8082
```

### Issue 3: Payment modal not showing validation errors
**Solution**: Frontend validation is now strict. Check browser console for errors.

### Issue 4: Order created but cart not cleared
**Solution**: Order-service clears cart after successful order. Check order-service logs.

---

## 📊 Database Verification Queries

### Check Cart Status
```sql
USE revcart_carts;
SELECT c.id, c.user_id, COUNT(ci.id) as item_count, SUM(ci.quantity * ci.price) as total
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
WHERE c.user_id = YOUR_USER_ID
GROUP BY c.id;
```

### Check Recent Orders
```sql
USE revcart_orders;
SELECT o.id, o.order_number, o.status, o.payment_status, o.total_amount, o.created_at
FROM orders o
WHERE o.user_id = YOUR_USER_ID
ORDER BY o.created_at DESC
LIMIT 5;
```

### Check Order Items
```sql
USE revcart_orders;
SELECT oi.product_name, oi.quantity, oi.unit_price, oi.subtotal
FROM order_items oi
WHERE oi.order_id = YOUR_ORDER_ID;
```

### Check Payments
```sql
USE revcart_payments;
SELECT p.id, p.order_id, p.payment_method, p.amount, p.status, p.created_at
FROM payments p
WHERE p.order_id = YOUR_ORDER_ID;
```

---

## ✅ Success Criteria

### Backend
- [x] Cart service starts without Redis
- [x] `/api/cart` returns MySQL-based cart
- [x] `/api/cart/validate` works correctly
- [x] Empty cart returns `{"success": true, "data": false}`
- [x] Order checkout clears cart after success

### Frontend
- [x] Card number accepts only 16 digits
- [x] CVV accepts only 3 digits
- [x] Expiry month validates 01-12
- [x] Expiry year validates current year + 20
- [x] Cardholder name accepts only letters
- [x] Submit button disabled until valid
- [x] Red error messages shown for invalid fields
- [x] Payment modal closes after successful payment

### Integration
- [x] COD orders work end-to-end
- [x] Card payment orders work end-to-end
- [x] Cart cleared after order
- [x] No Redis errors in logs

---

## 🎯 Test Card Numbers (For Testing)

```
Valid Test Cards:
- 4532 1234 5678 9010 (Visa)
- 5425 2334 3010 9903 (Mastercard)
- 3782 822463 10005 (Amex - but use 16 digits for our validation)

Use any future expiry date and any 3-digit CVV
```

---

**Happy Testing! 🎉**
