# Complete Fix Summary - Cart, CORS, WebSocket, X-User-Id

## ✅ FIXES APPLIED

### 1. Gateway CORS Configuration (application.yml)
**File:** `revcart-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
            allowedHeaders: "*"
            allowedMethods: "*"
            allowCredentials: true
```

**Fix:** Changed from string to array format to prevent duplicate CORS headers

### 2. WebSocket Configuration
**File:** `notification-service/src/main/java/com/revcart/notificationservice/config/WebSocketConfig.java`

```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:4200")
            .withSockJS();
}
```

**Fix:** Explicitly set allowed origin for WebSocket endpoint

### 3. Cart Controller with Logging
**File:** `cart-service/src/main/java/com/revcart/cartservice/controller/CartController.java`

```java
@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("GET /api/cart - X-User-Id: {}", userId);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/items - X-User-Id: {}, productId: {}", userId, request.getProductId());
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        CartDto cart = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Item added to cart"));
    }
}
```

**Fix:** Added debug logging to track X-User-Id header

### 4. Cart Service with Enhanced Logging
**File:** `cart-service/src/main/java/com/revcart/cartservice/service/CartService.java`

```java
@Cacheable(value = "carts", key = "#userId")
public CartDto getCart(Long userId) {
    log.info("CartService.getCart - userId: {}", userId);
    Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> createNewCart(userId));
    log.info("Cart found/created - cartId: {}, userId: {}", cart.getId(), cart.getUserId());
    if (cart.getId() != null) {
        cart.setItems(new ArrayList<>(cartItemRepository.findByCartId(cart.getId())));
        log.info("Loaded {} items from database", cart.getItems().size());
    }
    return toDto(cart);
}

@Transactional
@CacheEvict(value = "carts", key = "#userId")
public CartDto addItem(Long userId, AddToCartRequest request) {
    log.info("CartService.addItem - userId: {}, productId: {}, quantity: {}", 
             userId, request.getProductId(), request.getQuantity());
    
    // Validate product
    ApiResponse<ProductDto> productResponse = productServiceClient.getProductById(request.getProductId());
    if (!productResponse.isSuccess() || productResponse.getData() == null) {
        throw new ResourceNotFoundException("Product not found");
    }
    
    ProductDto product = productResponse.getData();
    if (!product.getActive()) {
        throw new BadRequestException("Product is not available");
    }
    if (product.getStockQuantity() < request.getQuantity()) {
        throw new BadRequestException("Insufficient stock");
    }

    Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> createNewCart(userId));
    log.info("Cart retrieved/created - cartId: {}", cart.getId());

    // Check if item already exists
    CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
            .orElse(null);

    if (existingItem != null) {
        existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(existingItem);
        log.info("Updated existing cart item for user: {}, product: {}", userId, request.getProductId());
    } else {
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProductId(product.getId());
        newItem.setProductName(product.getName());
        newItem.setQuantity(request.getQuantity());
        newItem.setPrice(product.getPrice());
        newItem.setImageUrl(product.getImageUrl());
        cartItemRepository.save(newItem);
        cart.getItems().add(newItem);
        log.info("Added new cart item for user: {}, product: {}", userId, request.getProductId());
    }

    cart.setUpdatedAt(java.time.LocalDateTime.now());
    Cart saved = cartRepository.save(cart);
    return toDto(saved);
}
```

**Fix:** 
- Explicitly loads cart items from database using `cartItemRepository.findByCartId()`
- Saves CartItem before adding to collection
- Added comprehensive logging

### 5. Angular Auth Interceptor
**File:** `Frontend/src/app/core/interceptors/auth.interceptor.ts`

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('revcart_token') : null;
  const userStr = typeof localStorage !== 'undefined' ? localStorage.getItem('revcart_user') : null;
  const user = userStr ? JSON.parse(userStr) : null;

  const headers: any = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (user && user.id) {
    headers['X-User-Id'] = user.id.toString();
  }

  let clonedRequest = req;
  if (Object.keys(headers).length > 0) {
    clonedRequest = req.clone({ setHeaders: headers });
  }

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('revcart_token');
          localStorage.removeItem('revcart_user');
        }
        authService.logout();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        router.navigate(['/']);
      }
      return throwError(() => error);
    })
  );
};
```

**Fix:** Always sends X-User-Id header when user is logged in (not just when token exists)

### 6. Angular Cart Service
**File:** `Frontend/src/app/core/services/cart.service.ts`

```typescript
loadCartFromBackend(): void {
  if (!isPlatformBrowser(this.platformId)) return;
  
  const token = localStorage.getItem('revcart_token');
  if (!token) return;

  this.httpClient.get<any>(`${this.apiUrl}`).subscribe({
    next: (response) => {
      const cart = response.data || response;
      if (cart && cart.items) {
        const items: CartItem[] = cart.items.map((item: any) => ({
          id: item.productId?.toString() || item.id?.toString(),
          name: item.productName || item.name,
          price: item.price,
          quantity: item.quantity,
          image: item.imageUrl || item.image,
          unit: item.unit || 'unit',
          availableQuantity: item.availableQuantity || 0
        }));
        this.itemsSignal.set(items);
        this.saveCartToStorage();
      }
    },
    error: (err) => console.warn('Failed to load cart from backend:', err)
  });
}
```

**Fix:** Added method to load cart from backend with proper response.data handling

### 7. Angular Checkout Component
**File:** `Frontend/src/app/features/checkout/checkout.component.ts`

```typescript
private addItemsSequentially(items: any[], index: number, resolve: () => void, reject: (err: any) => void): void {
    if (index >= items.length) {
        resolve();
        return;
    }

    const user = this.authService.user();
    if (!user || !user.id) {
        reject(new Error('User not authenticated'));
        return;
    }

    const headers = { 'X-User-Id': user.id.toString() };
    const item = items[index];
    const productId = parseInt(item.id, 10);
    const cartItemRequest = {
        productId: productId,
        quantity: item.quantity || 1
    };

    this.http.post(`${environment.apiUrl}/cart/items`, cartItemRequest, { headers }).subscribe({
        next: () => this.addItemsSequentially(items, index + 1, resolve, reject),
        error: (err) => {
            if (err.status === 404 || err.status === 400 || err.status === 500) {
                this.addItemsSequentially(items, index + 1, resolve, reject);
            } else {
                this.addItemsSequentially(items, index + 1, resolve, reject);
            }
        }
    });
}
```

**Fix:** Changed endpoint from `/cart` to `/cart/items` to match backend API

### 8. User Model Type Fix
**File:** `Frontend/src/app/core/models/user.model.ts`

```typescript
export interface User {
  id: number;  // Changed from string to number
  email: string;
  name: string;
  phone?: string;
  role: 'customer' | 'admin' | 'delivery_agent';
  avatar?: string;
  createdAt?: Date;
}
```

**Fix:** Changed id type from string to number to match backend

### 9. Auth Service Fix
**File:** `Frontend/src/app/core/services/auth.service.ts`

```typescript
const user: User = {
  id: authData.user.id,  // Store as number, not String()
  email: authData.user.email,
  name: authData.user.name,
  role: this.mapRole(authData.user.role)
};
```

**Fix:** Store user.id as number instead of converting to string

## 🔄 RESTART REQUIRED

```powershell
# 1. Restart Gateway
cd revcart-gateway
mvn spring-boot:run

# 2. Restart Cart Service
cd cart-service
mvn spring-boot:run

# 3. Restart Notification Service (for WebSocket)
cd notification-service
mvn spring-boot:run

# 4. Restart Frontend
cd Frontend
npm start
```

## 🧪 VERIFICATION

See `test-cart-checkout.md` for complete testing steps.

### Quick Verification

1. **Test X-User-Id Header:**
```bash
curl -H "X-User-Id: 14" http://localhost:8080/api/cart
```
Expected: 200 OK with cart data

2. **Test Add to Cart:**
```bash
curl -H "X-User-Id: 14" -H "Content-Type: application/json" -X POST http://localhost:8080/api/cart/items -d '{"productId":1,"quantity":1}'
```
Expected: 201 Created with cart containing item

3. **Verify Database:**
```sql
USE revcart_carts;
SELECT * FROM cart_items;
```
Expected: Rows in cart_items table

4. **Test WebSocket:**
Open http://localhost:4200 and check browser console
Expected: No CORS errors, WebSocket connected

## 📊 SUCCESS CRITERIA

✅ Cart API returns 200 (not 500)
✅ cart_items table contains data
✅ X-User-Id header forwarded correctly
✅ WebSocket connects without CORS errors
✅ Checkout creates order successfully
✅ No "Cart is empty" error
✅ Only one Access-Control-Allow-Origin header
✅ Database `revcart_carts` used (not `revcart_cart`)
