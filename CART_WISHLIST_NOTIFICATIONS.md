# Cart & Wishlist Notifications Implementation

## Overview
Added real-time notifications when users add products to cart or wishlist.

## Implementation Status

### ✅ Cart Notifications - COMPLETED
Users now receive notifications when products are added to cart.

### ⚠️ Wishlist Notifications - NOT IMPLEMENTED
Wishlist service doesn't exist in the current architecture. This feature can be added when wishlist functionality is implemented.

## Changes Made

### 1. Created NotificationServiceClient for Cart Service
**File**: `cart-service/src/main/java/com/revcart/cartservice/client/NotificationServiceClient.java`

```java
@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8086}")
public interface NotificationServiceClient {
    @PostMapping("/api/notifications")
    ApiResponse<Object> createNotification(@RequestBody Map<String, Object> request);
}
```

### 2. Updated CartService
**File**: `cart-service/src/main/java/com/revcart/cartservice/service/CartService.java`

**Added**:
- Import for NotificationServiceClient
- Dependency injection for notificationServiceClient
- `sendCartNotification()` method
- Notification call in `addItem()` method

**Key Code**:
```java
// In addItem() method - after saving cart
sendCartNotification(userId, product.getName(), "added to cart");

// New helper method
private void sendCartNotification(Long userId, String productName, String action) {
    try {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("title", "Cart Updated");
        request.put("message", productName + " " + action + " successfully");
        request.put("type", "CART");
        notificationServiceClient.createNotification(request);
        log.info("Cart notification sent to user: {}", userId);
    } catch (Exception e) {
        log.error("Failed to send cart notification: {}", e.getMessage());
    }
}
```

## How It Works

### Cart Notification Flow:
1. User clicks "Add to Cart" on a product
2. Frontend calls `POST /api/cart/items`
3. Cart Service validates product and adds to cart
4. Cart Service calls Notification Service
5. Notification created with:
   - Title: "Cart Updated"
   - Message: "{Product Name} added to cart successfully"
   - Type: "CART"
6. User sees notification in notification panel

### Example Notification:
```json
{
  "userId": 1,
  "title": "Cart Updated",
  "message": "iPhone 15 Pro added to cart successfully",
  "type": "CART",
  "read": false
}
```

## Testing

### Test Cart Notification:
1. **Start Services**:
   ```powershell
   # Start cart-service
   cd cart-service
   mvn spring-boot:run
   
   # Ensure notification-service is running
   cd notification-service
   mvn spring-boot:run
   ```

2. **Add Product to Cart**:
   - Login as customer
   - Browse products
   - Click "Add to Cart" on any product
   - Check notification bell icon

3. **Verify Notification**:
   - Click notification bell
   - Should see: "{Product Name} added to cart successfully"
   - Notification should be unread (blue dot)

4. **Check Logs**:
   ```
   Cart notification sent to user: 1
   ```

### API Test:
```bash
# Add to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "productId": 1,
    "quantity": 1
  }'

# Check notifications
curl http://localhost:8080/api/notifications/user/1 \
  -H "X-User-Id: 1"
```

## Future Enhancement: Wishlist Notifications

When wishlist service is implemented, follow the same pattern:

### 1. Create WishlistService
```java
@Service
public class WishlistService {
    private final NotificationServiceClient notificationServiceClient;
    
    public void addToWishlist(Long userId, Long productId) {
        // Add to wishlist logic
        
        // Send notification
        sendWishlistNotification(userId, productName, "added to wishlist");
    }
    
    private void sendWishlistNotification(Long userId, String productName, String action) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("title", "Wishlist Updated");
        request.put("message", productName + " " + action + " successfully");
        request.put("type", "WISHLIST");
        notificationServiceClient.createNotification(request);
    }
}
```

### 2. Create Wishlist Endpoints
```java
@PostMapping("/api/wishlist/items")
public ResponseEntity<ApiResponse<WishlistDto>> addToWishlist(
    @RequestHeader("X-User-Id") Long userId,
    @RequestBody AddToWishlistRequest request) {
    // Implementation
}
```

## Notification Types

Current notification types in the system:
- **ORDER** - Order placed, shipped, delivered
- **PAYMENT** - Payment success, failed, refunded
- **CART** - Product added to cart ✅ NEW
- **WISHLIST** - Product added to wishlist (future)
- **DELIVERY** - Delivery assigned, status updates

## Build Status
✅ cart-service: BUILD SUCCESS (5.896s)

## Files Modified

1. **New File**:
   - `cart-service/src/main/java/com/revcart/cartservice/client/NotificationServiceClient.java`

2. **Modified**:
   - `cart-service/src/main/java/com/revcart/cartservice/service/CartService.java`

## Configuration

No additional configuration needed. The notification service URL is already configured:
```properties
notification.service.url=http://localhost:8086
```

## Error Handling

Notification failures are logged but don't block cart operations:
```java
catch (Exception e) {
    log.error("Failed to send cart notification: {}", e.getMessage());
}
```

This ensures cart functionality works even if notification service is down.

## Next Steps

1. **Restart cart-service** to apply changes
2. **Test cart notifications** by adding products
3. **Implement wishlist service** (if needed) with similar notification pattern
4. **Add more notification types**:
   - Product back in stock
   - Price drop alerts
   - Cart abandonment reminders

---
**Status**: Cart notifications implemented and ready to use
**Wishlist**: Pending wishlist service implementation
**Date**: 2025-12-09
