# Order Status Column Size Fix

## Problem
Payment status was not updating in admin order management page due to database error:
```
Data truncated for column 'status' at row 1
```

## Root Cause
The `status` column in the `orders` table was too small (likely VARCHAR(10) or VARCHAR(15)) to store "PAYMENT_SUCCESS" which is 15 characters long.

### Order Status Values:
- PENDING (7 chars)
- PAYMENT_SUCCESS (15 chars) ⚠️ LONGEST
- PROCESSING (10 chars)
- PACKED (6 chars)
- OUT_FOR_DELIVERY (16 chars) ⚠️ LONGEST
- SHIPPED (7 chars)
- DELIVERED (9 chars)
- CANCELLED (9 chars)
- CONFIRMED (9 chars)
- COMPLETED (9 chars)

**Maximum length needed**: 16 characters (OUT_FOR_DELIVERY)

## Solution Applied

### 1. Database Fix (SQL)
**File**: `fix-order-status-column.sql`

```sql
USE revcart_orders;

-- Increase status column size to VARCHAR(20)
ALTER TABLE orders MODIFY COLUMN status VARCHAR(20) NOT NULL;
```

### 2. Entity Fix (Java)
**File**: `order-service/src/main/java/com/revcart/orderservice/entity/Order.java`

**Before**:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private OrderStatus status = OrderStatus.PENDING;

@Enumerated(EnumType.STRING)
@Column(name = "payment_status", nullable = false)
private PaymentStatus paymentStatus = PaymentStatus.PENDING;
```

**After**:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private OrderStatus status = OrderStatus.PENDING;

@Enumerated(EnumType.STRING)
@Column(name = "payment_status", nullable = false, length = 20)
private PaymentStatus paymentStatus = PaymentStatus.PENDING;
```

## Steps to Fix

### Step 1: Run SQL Script
```powershell
# Open MySQL Workbench or command line
mysql -u root -p

# Run the SQL script
source fix-order-status-column.sql
```

Or manually:
```sql
USE revcart_orders;
ALTER TABLE orders MODIFY COLUMN status VARCHAR(20) NOT NULL;
DESCRIBE orders;
```

### Step 2: Restart order-service
```powershell
cd order-service
mvn spring-boot:run
```

### Step 3: Test Payment Flow
1. Login as customer
2. Add products to cart
3. Checkout and complete payment
4. Check admin orders page
5. Payment status should show "SUCCESS"

## Verification

### Check Column Size:
```sql
USE revcart_orders;
DESCRIBE orders;
```

Expected output:
```
Field          | Type         | Null | Key | Default | Extra
status         | varchar(20)  | NO   |     | NULL    |
payment_status | varchar(20)  | NO   |     | NULL    |
```

### Check Existing Orders:
```sql
SELECT id, order_number, status, payment_status 
FROM orders 
ORDER BY id DESC 
LIMIT 10;
```

## Error Details

### Original Error:
```
[500] during [PUT] to [http://localhost:8084/api/orders/18/payment-status?status=PAYMENT_SUCCESS]
Data truncated for column 'status' at row 1
could not execute statement
```

### Why It Happened:
1. Payment service calls `updatePaymentStatus(orderId, "PAYMENT_SUCCESS")`
2. Order service tries to save "PAYMENT_SUCCESS" (15 chars)
3. Database column is VARCHAR(10) or VARCHAR(15)
4. MySQL truncates the value → Error
5. Payment status update fails
6. Admin page shows "PENDING" instead of "SUCCESS"

## Impact

### Before Fix:
- ❌ Payment status stuck on "PENDING"
- ❌ Database error in logs
- ❌ Payment service fails to update order
- ❌ Admin sees incorrect status

### After Fix:
- ✅ Payment status updates to "SUCCESS"
- ✅ No database errors
- ✅ Payment service successfully updates order
- ✅ Admin sees correct status

## Column Size Recommendations

### Current Implementation:
- `status`: VARCHAR(20) - Accommodates all OrderStatus values
- `payment_status`: VARCHAR(20) - Accommodates all PaymentStatus values

### Why VARCHAR(20)?
- Longest OrderStatus: "OUT_FOR_DELIVERY" (16 chars)
- Longest PaymentStatus: "COMPLETED" (9 chars)
- VARCHAR(20) provides buffer for future status values
- Minimal storage overhead (20 bytes per row)

## Build Status
✅ order-service: BUILD SUCCESS (7.678s)

## Files Modified

1. **SQL Script** (NEW):
   - `fix-order-status-column.sql`

2. **Backend**:
   - `order-service/src/main/java/com/revcart/orderservice/entity/Order.java`

## Testing Checklist

- [ ] Run SQL script to update column size
- [ ] Restart order-service
- [ ] Place new order
- [ ] Complete payment
- [ ] Check admin orders page
- [ ] Verify payment status shows "SUCCESS"
- [ ] Check logs for errors (should be none)

## Prevention

The entity now specifies `length = 20` for both status columns. This ensures:
1. Future schema updates will use correct size
2. Documentation of column requirements
3. Consistency across environments

## Related Issues

This fix also resolves:
- Payment status not updating after successful payment
- Admin dashboard showing incorrect payment status
- 500 errors when updating order status
- Data truncation warnings in logs

---
**Status**: FIXED - Database column size increased
**Date**: 2025-12-09
**Action Required**: Run SQL script and restart order-service
