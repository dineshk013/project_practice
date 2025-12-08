# 🧪 Payment System Testing Guide

## Quick Test Checklist

### ✅ Test 1: Card Payment Success Flow
```
1. Login to application
2. Add products to cart
3. Go to checkout
4. Select existing address OR add new address
5. Select "Credit/Debit Card" payment method
6. Click "Place Order"
7. ✅ Modal should open (NOT inline form)
8. Enter valid card details:
   - Card: 1234 5678 9012 3456
   - CVV: 123
   - Month: 12
   - Year: 2025
   - Name: John Doe
9. Click "Pay Now"
10. ✅ Should redirect to My Orders page
11. ✅ Cart should be empty
12. ✅ Order should appear in My Orders
```

**Expected Logs:**
```
✅ Order created with ID: X
✅ Payment initiated for order: X
✅ Dummy payment successful: Y for order: X
✅ Payment status updated to PAID for order: X
✅ Delivery assigned for order: X
✅ Notification sent for order: X
✅ Cart cleared for userId: Z after successful payment
```

**Should NOT see:**
```
❌ "Order not found: X"
```

---

### ✅ Test 2: Card Validation Errors

#### Test 2a: Invalid Card Number
```
Card: 1234 (only 4 digits)
Expected: "Card number must be exactly 16 digits"
Backend called: NO
```

#### Test 2b: Invalid CVV
```
CVV: 12 (only 2 digits)
Expected: "CVV must be exactly 3 digits"
Backend called: NO
```

#### Test 2c: Invalid Expiry Month
```
Month: 13
Expected: "Invalid month (01-12)"
Backend called: NO
```

#### Test 2d: Expired Card
```
Year: 2020
Expected: "Card has expired"
Backend called: NO
```

#### Test 2e: Invalid Cardholder Name
```
Name: John123
Expected: "Name must contain only letters"
Backend called: NO
```

---

### ✅ Test 3: COD Payment Flow
```
1. Login to application
2. Add products to cart
3. Go to checkout
4. Select address
5. Select "Cash on Delivery"
6. Click "Place Order"
7. ✅ Should redirect to My Orders immediately (no modal)
8. ✅ Cart should be empty
9. ✅ Order should appear in My Orders
```

**Expected Logs:**
```
✅ Order created with ID: X
✅ COD payment status updated for order: X
✅ Delivery assigned for order: X
✅ Notification sent for order: X
✅ Cart cleared for userId: Z after COD order
```

---

### ✅ Test 4: Prevent Duplicate Submissions
```
1. Go to checkout with items in cart
2. Click "Place Order" button
3. Immediately click "Place Order" again (double-click)
4. ✅ Should only create ONE order
5. ✅ Button should be disabled after first click
```

**Expected Logs:**
```
✅ Only ONE "Order created" log entry
```

---

### ✅ Test 5: Modal Never Freezes
```
1. Go to checkout
2. Select card payment
3. Click "Place Order"
4. Modal opens
5. Enter invalid card details
6. Click "Pay Now"
7. ✅ Error message should appear
8. ✅ "Pay Now" button should be clickable again
9. ✅ Can retry with correct details
10. ✅ Cancel button always works
```

---

### ✅ Test 6: No Card Form on Checkout Page
```
1. Go to checkout
2. Select "Credit/Debit Card" payment method
3. ✅ Should NOT see card number, CVV, expiry fields on page
4. ✅ Should only see payment method radio buttons
5. Click "Place Order"
6. ✅ Modal opens with card entry form
```

---

### ✅ Test 7: Payment Failure Handling
```
To simulate payment failure, temporarily modify PaymentService.processDummyPayment():
- Change status to FAILED
- Return FAILED response

Then test:
1. Go to checkout with card payment
2. Enter valid card details
3. Click "Pay Now"
4. ✅ Error message should appear in modal
5. ✅ Modal should stay open
6. ✅ Cart should NOT be cleared
7. ✅ Delivery should NOT be assigned
8. ✅ Notification should NOT be sent
9. ✅ Can retry payment
```

---

### ✅ Test 8: Delivery Assignment Timing

#### For Card Payment:
```
Expected Flow:
1. Order created → Status: PENDING
2. Payment initiated
3. Payment success
4. Payment status updated to PAID
5. Delivery assigned ← ONLY AFTER PAYMENT SUCCESS
6. Notification sent ← ONLY AFTER PAYMENT SUCCESS
7. Cart cleared ← ONLY AFTER PAYMENT SUCCESS
```

#### For COD Payment:
```
Expected Flow:
1. Order created → Status: PENDING
2. Payment status set to COD
3. Delivery assigned ← IMMEDIATELY
4. Notification sent ← IMMEDIATELY
5. Cart cleared ← IMMEDIATELY
```

---

## API Testing with cURL

### Test Dummy Payment Endpoint
```bash
curl -X POST http://localhost:8080/api/payments/dummy \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 1234.56,
    "paymentMethod": "RAZORPAY"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "status": "SUCCESS",
    "paymentId": "TXN-uuid-here",
    "message": "Payment processed successfully"
  }
}
```

---

### Test Payment Status Update
```bash
curl -X POST "http://localhost:8080/api/orders/1/payment-status?status=COMPLETED" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Payment status updated",
  "data": null
}
```

---

## Browser Console Testing

### Check for Errors
```javascript
// Open browser console (F12)
// Should NOT see:
❌ "Order not found"
❌ "404 Not Found"
❌ "Payment service error"

// Should see:
✅ "Processing payment: {orderId: X, userId: Y, amount: Z, paymentMethod: 'RAZORPAY'}"
✅ Successful API responses
```

---

## Database Verification

### Check Payment Record
```sql
-- After successful payment
SELECT * FROM payments WHERE order_id = 1;

-- Should show:
-- status: SUCCESS
-- transaction_id: TXN-uuid
-- payment_method: RAZORPAY
```

### Check Order Status
```sql
-- After successful payment
SELECT * FROM orders WHERE id = 1;

-- Should show:
-- payment_status: PAID
-- status: PENDING (or PROCESSING)
```

### Check Delivery Assignment
```sql
-- After successful payment
SELECT * FROM deliveries WHERE order_id = 1;

-- Should exist with:
-- status: ASSIGNED
-- estimated_delivery: 3 days from now
```

---

## Performance Testing

### Test Response Times
```
✅ Order creation: < 2 seconds
✅ Payment processing: < 1 second
✅ Modal open/close: Instant
✅ Page redirect: < 500ms
```

---

## Edge Cases to Test

### 1. Empty Cart
```
1. Go to checkout with empty cart
2. Click "Place Order"
3. ✅ Should show error: "Your cart is empty"
```

### 2. Invalid Address
```
1. Select "Add New Address"
2. Leave fields empty
3. Click "Place Order"
4. ✅ Should show error: "Please fill in all required address fields"
```

### 3. Network Failure
```
1. Disconnect network
2. Try to place order
3. ✅ Should show error message
4. ✅ Should not freeze UI
```

### 4. Session Timeout
```
1. Start checkout
2. Wait for session to expire
3. Try to place order
4. ✅ Should redirect to login
```

---

## Success Criteria Summary

| Test | Expected Result | Status |
|------|----------------|--------|
| Card form removed from checkout | ✅ Only modal has card form | |
| Card validation before backend | ✅ Errors shown, no API call | |
| No duplicate submissions | ✅ Only one order created | |
| Modal never freezes | ✅ Can always retry/cancel | |
| No "Order not found" error | ✅ Clean logs | |
| Payment failure handled | ✅ Error shown, can retry | |
| Delivery after payment success | ✅ Correct timing | |
| Notification after payment success | ✅ Correct timing | |
| Cart cleared after success | ✅ Empty cart | |
| Redirect to My Orders | ✅ Correct page | |

---

## Troubleshooting

### Issue: Modal doesn't open
**Check:**
- Payment method is "card"
- Order was created successfully
- No console errors

### Issue: Payment fails
**Check:**
- Payment service is running (port 8085)
- Database connection is working
- Check payment service logs

### Issue: Delivery not assigned
**Check:**
- Payment status is "COMPLETED"
- Delivery service is running (port 8087)
- Check order service logs for "Delivery assigned"

### Issue: Cart not cleared
**Check:**
- Payment was successful
- Cart service is running (port 8083)
- Check order service logs for "Cart cleared"

---

**Happy Testing! 🎉**
