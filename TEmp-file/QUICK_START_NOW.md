# 🚀 QUICK START - READY TO TEST NOW

**Email Configured:** ✅ mahidineshk@gmail.com  
**All Services Built:** ✅ order-service, user-service  
**Status:** READY FOR TESTING

---

## ⚡ START TESTING IN 3 STEPS

### STEP 1: Restart Services (5 minutes)

```powershell
# Stop all services
.\stop-all.ps1

# Start all services
.\start-all.ps1

# Wait 2-3 minutes for startup
Start-Sleep -Seconds 180
```

### STEP 2: Test Email (30 seconds)

```bash
curl -X POST "http://localhost:8080/api/users/test-email?email=mahidineshk@gmail.com"
```

**Expected:**
- Response: `{"success": true, "message": "..."}`
- Check your Gmail inbox
- Email subject: "RevCart - Test Email"
- If received: ✅ Email working!

### STEP 3: Test Complete Flow (5 minutes)

**3.1 Register User**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"test123\",\"phone\":\"1234567890\"}"
```

**3.2 Login**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"test123\"}"
```

Save the userId from response (e.g., 15)

**3.3 Add to Cart**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d "{\"productId\":1,\"quantity\":2}"
```

**3.4 Add Address**
```bash
curl -X POST http://localhost:8080/api/profile/addresses \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d "{\"line1\":\"123 Test St\",\"city\":\"Test City\",\"state\":\"Test State\",\"postalCode\":\"12345\",\"country\":\"India\",\"primaryAddress\":true}"
```

Save the addressId from response (e.g., 1)

**3.5 Checkout**
```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -d "{\"addressId\":1,\"paymentMethod\":\"COD\"}"
```

**3.6 Verify Order in Database**
```sql
mysql -u root -pMahidinesh@07 -e "USE revcart_orders; SELECT * FROM orders;"
```

**Expected:** 1 row with order details

---

## ✅ SUCCESS CHECKLIST

After running above tests:

- [ ] Test email received in Gmail
- [ ] User registered successfully
- [ ] User logged in successfully
- [ ] Item added to cart
- [ ] Address created
- [ ] Order placed successfully
- [ ] Order visible in database
- [ ] Order-service logs show: "=== ORDER SAVED ==="

---

## 🌐 FRONTEND TESTING

**Open:** http://localhost:4200

1. **Register/Login**
   - Use any email (e.g., customer@test.com)
   - Password: test123

2. **Browse & Add to Cart**
   - Click products
   - Add 2-3 items to cart
   - Check cart icon shows count

3. **Checkout**
   - Go to cart
   - Click "Proceed to Checkout"
   - Fill address or select existing
   - Choose "Cash on Delivery"
   - Click "Place Order"

4. **Verify My Orders**
   - Should redirect to "My Orders" page
   - Order should appear in list
   - Status: Pending
   - Items listed correctly

---

## 🐛 IF SOMETHING FAILS

### Email Not Received
**Check user-service logs for:**
```
✅ OTP email sent successfully to: mahidineshk@gmail.com
```

**If you see ❌ error:**
- Gmail app password might be revoked
- Generate new one at: https://myaccount.google.com/apppasswords
- Update application.yml and rebuild

### Order Not Saved
**Check order-service logs for:**
```
=== CHECKOUT START === userId: 15
Saving order to database...
=== ORDER SAVED === ID: 1, OrderNumber: ORD-XXX
Order exists in DB after save: true
```

**If "Order exists: false":**
- Check MySQL is running: `net start MySQL80`
- Verify database exists: `SHOW DATABASES LIKE 'revcart_orders';`
- Check for exceptions in logs

### Cart Empty
**Check cart-service logs for:**
```
CartService.addItem - userId: 15, productId: 1
Added new cart item for user: 15, product: 1
```

**If not found:**
- Verify X-User-Id header is sent
- Check database: `SELECT * FROM revcart_carts.cart_items;`

---

## 📊 VERIFY DATABASE STATE

```sql
-- Check cart
USE revcart_carts;
SELECT c.id, c.user_id, COUNT(ci.id) as items 
FROM carts c 
LEFT JOIN cart_items ci ON c.id = ci.cart_id 
GROUP BY c.id;

-- Check orders
USE revcart_orders;
SELECT o.id, o.order_number, o.status, o.total_amount, COUNT(oi.id) as items
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id;

-- Check users
USE revcart_users;
SELECT id, email, name, role FROM users ORDER BY created_at DESC LIMIT 5;
```

---

## 📞 NEXT STEPS

1. ✅ Run STEP 1-3 above
2. ✅ Verify all tests pass
3. ✅ Test frontend UI
4. ✅ Check "My Orders" page shows orders

**Full Test Guide:** See `END_TO_END_TEST_GUIDE.md` for detailed testing

---

**Status:** READY TO TEST  
**Email:** ✅ Configured  
**Services:** ✅ Built  
**Action:** Run STEP 1 now!
