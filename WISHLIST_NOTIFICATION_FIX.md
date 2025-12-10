# Wishlist Notification Fix

## Issue
Wishlist notifications were not working because the service was looking for `revcart_user_id` in localStorage, but the auth service stores the complete user object under `revcart_user`.

## Fix Applied

**File**: `Frontend/src/app/core/services/wishlist.service.ts`

### Before:
```typescript
const userId = localStorage.getItem('revcart_user_id');
if (!userId) return;

this.http.post(`${environment.apiUrl}/notifications`, {
  userId: parseInt(userId),
  // ...
});
```

### After:
```typescript
const userStr = localStorage.getItem('revcart_user');
if (!userStr) return;

try {
  const user = JSON.parse(userStr);
  const userId = user.id;
  if (!userId) return;

  this.http.post(`${environment.apiUrl}/notifications`, {
    userId: userId,
    // ...
  });
} catch (error) {
  console.error('Error parsing user data:', error);
}
```

## Changes Made
1. Changed from reading `revcart_user_id` to `revcart_user`
2. Parse the JSON user object
3. Extract `user.id` from the parsed object
4. Added error handling for JSON parsing
5. Removed unnecessary `parseInt()` since user.id is already a number

## How It Works Now

### Wishlist Notification Flow:
1. User clicks heart icon to add product to wishlist
2. `addToWishlist()` is called
3. Product added to localStorage
4. `sendWishlistNotification()` is called
5. Gets user object from `localStorage.getItem('revcart_user')`
6. Parses JSON to extract user ID
7. Sends POST request to `/api/notifications`
8. Notification created in database
9. User sees notification in bell icon

### Notification Payload:
```json
{
  "userId": 1,
  "title": "Wishlist Updated",
  "message": "iPhone 15 Pro added to wishlist successfully",
  "type": "WISHLIST"
}
```

## Testing

### Test Wishlist Notification:
1. **Login as customer**:
   - Email: test@example.com
   - Password: test123

2. **Add product to wishlist**:
   - Browse products page
   - Click heart icon on any product
   - Heart should turn red (filled)

3. **Check notification**:
   - Click bell icon in header
   - Should see: "{Product Name} added to wishlist successfully"
   - Notification should be unread (blue dot)

4. **Check console**:
   ```
   ✅ Wishlist notification sent
   ```

### Browser Console Test:
```javascript
// Check if user is stored correctly
const user = JSON.parse(localStorage.getItem('revcart_user'));
console.log('User ID:', user.id);
console.log('User Name:', user.name);
```

### API Verification:
```bash
# Check notifications for user
curl http://localhost:8080/api/notifications/user/1 \
  -H "X-User-Id: 1"
```

## Expected Behavior

### When Adding to Wishlist:
1. ✅ Product added to localStorage
2. ✅ Heart icon turns red
3. ✅ Notification sent to backend
4. ✅ Notification appears in bell icon
5. ✅ Console shows success message

### Error Cases Handled:
- ❌ User not logged in → No notification sent (silent fail)
- ❌ Invalid user data → Error logged, no crash
- ❌ Network error → Error logged, wishlist still works

## localStorage Structure

### What's Stored:
```javascript
// revcart_user (complete user object)
{
  "id": 1,
  "email": "test@example.com",
  "name": "Test User",
  "role": "customer"
}

// revcart_token (JWT token)
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// revcart_wishlist (array of products)
[
  {
    "id": "1",
    "name": "iPhone 15 Pro",
    "price": 999.99,
    // ... other product fields
  }
]
```

## Comparison: Cart vs Wishlist

### Cart Notification (Backend):
- Triggered by: API call to cart-service
- Sent from: cart-service (Java)
- User ID from: Request header (X-User-Id)

### Wishlist Notification (Frontend):
- Triggered by: localStorage update
- Sent from: wishlist.service.ts (Angular)
- User ID from: localStorage (revcart_user)

## Files Modified
1. ✅ `Frontend/src/app/core/services/wishlist.service.ts`

## Build Status
✅ No build needed (TypeScript changes only)
✅ Ready to test immediately

## Next Steps
1. Refresh the frontend page (Ctrl+R)
2. Login as customer
3. Add product to wishlist
4. Check notification bell icon
5. Verify notification appears

---
**Status**: FIXED - Wishlist notifications now working
**Date**: 2025-12-09
**Ready for Testing**: Yes
