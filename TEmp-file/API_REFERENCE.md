# 📡 COMPLETE API REFERENCE
## RevCart Microservices - Backend Endpoints & Frontend Calls

**Base URL:** `http://localhost:8080` (API Gateway)  
**Frontend:** `http://localhost:4200`

---

## 🔐 AUTHENTICATION & USER SERVICE (Port 8081)

### Backend Endpoints

#### POST /api/users/register
**Register new user**
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

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "phone": "1234567890",
      "role": "USER"
    }
  }
}
```

**Frontend Call:**
```typescript
// auth.service.ts
signup(data: SignupData): Observable<User> {
  return this.httpClient.post<ApiResponse<BackendAuthResponse>>(
    `${this.apiUrl}/register`, 
    data
  ).pipe(
    map(response => {
      const user = response.data.user;
      localStorage.setItem('revcart_user', JSON.stringify(user));
      localStorage.setItem('revcart_token', response.data.token);
      return user;
    })
  );
}
```

---

#### POST /api/users/login
**User login**
```bash
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "role": "USER"
    }
  }
}
```

**Frontend Call:**
```typescript
// auth.service.ts
login(credentials: LoginCredentials): Observable<User> {
  return this.httpClient.post<ApiResponse<BackendAuthResponse>>(
    `${this.apiUrl}/login`,
    credentials
  ).pipe(
    map(response => {
      const user = response.data.user;
      localStorage.setItem('revcart_user', JSON.stringify(user));
      localStorage.setItem('revcart_token', response.data.token);
      return user;
    })
  );
}
```

---

#### POST /api/users/test-email
**Test email configuration**
```bash
POST http://localhost:8080/api/users/test-email?email=test@example.com
```

**Response:**
```json
{
  "success": true,
  "message": "Check your inbox...",
  "data": "Test email sent to test@example.com"
}
```

---

#### POST /api/users/resend-otp
**Resend OTP**
```bash
POST http://localhost:8080/api/users/resend-otp?email=john@example.com
```

---

#### POST /api/users/verify-otp
**Verify OTP**
```bash
POST http://localhost:8080/api/users/verify-otp?email=john@example.com&otp=123456
```

---

#### GET /api/profile/addresses
**Get user addresses**
```bash
GET http://localhost:8080/api/profile/addresses
Headers:
  X-User-Id: 1
  Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "id": 1,
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA",
      "primaryAddress": true
    }
  ]
}
```

**Frontend Call:**
```typescript
// profile.service.ts or checkout.component.ts
getAddresses(): Observable<AddressDto[]> {
  return this.http.get<ApiResponse<AddressDto[]>>(
    `${environment.apiUrl}/profile/addresses`
  ).pipe(
    map(response => response.data || [])
  );
}
```

---

#### POST /api/profile/addresses
**Add new address**
```bash
POST http://localhost:8080/api/profile/addresses
Headers:
  X-User-Id: 1
  Content-Type: application/json

{
  "line1": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "primaryAddress": true
}
```

**Frontend Call:**
```typescript
// checkout.component.ts
addAddress(address: AddressDto): Observable<AddressDto> {
  return this.http.post<ApiResponse<AddressDto>>(
    `${environment.apiUrl}/profile/addresses`,
    address
  ).pipe(
    map(response => response.data)
  );
}
```

---

## 🛍️ PRODUCT SERVICE (Port 8082)

### Backend Endpoints

#### GET /api/products
**Get all products (paginated)**
```bash
GET http://localhost:8080/api/products?page=0&size=20
```

**Response:**
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product Name",
        "description": "Product description",
        "price": 99.99,
        "categoryId": 1,
        "categoryName": "Electronics",
        "stockQuantity": 100,
        "availableQuantity": 100,
        "imageUrl": "https://...",
        "active": true
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "page": 0,
    "size": 20
  }
}
```

**Frontend Call:**
```typescript
// product.service.ts
getProducts(page: number = 0, size: number = 20): Observable<Product[]> {
  return this.httpClient.get<ApiResponse<PagedResponse<ProductDto>>>(
    `${this.apiUrl}?page=${page}&size=${size}`
  ).pipe(
    map(response => response.data.content.map(this.mapToProduct))
  );
}
```

---

#### GET /api/products/{id}
**Get product by ID**
```bash
GET http://localhost:8080/api/products/1
```

**Response:**
```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Product Name",
    "price": 99.99,
    "stockQuantity": 100,
    "imageUrl": "https://..."
  }
}
```

**Frontend Call:**
```typescript
// product.service.ts
getProductById(id: string): Observable<Product> {
  return this.httpClient.get<ApiResponse<ProductDto>>(
    `${this.apiUrl}/${id}`
  ).pipe(
    map(response => this.mapToProduct(response.data))
  );
}
```

---

#### POST /api/products
**Create product (Admin)**
```bash
POST http://localhost:8080/api/products
Headers:
  Authorization: Bearer <admin-token>
  Content-Type: application/json

{
  "name": "New Product",
  "description": "Description",
  "price": 99.99,
  "categoryId": 1,
  "stockQuantity": 100,
  "imageUrl": "https://..."
}
```

**Frontend Call:**
```typescript
// admin-products.component.ts
createProduct(product: ProductDto): Observable<Product> {
  return this.http.post<ApiResponse<ProductDto>>(
    `${environment.apiUrl}/products`,
    product
  ).pipe(
    map(response => response.data)
  );
}
```

---

#### GET /api/categories
**Get all categories**
```bash
GET http://localhost:8080/api/categories
```

**Response:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic items",
      "active": true
    }
  ]
}
```

**Frontend Call:**
```typescript
// category.service.ts
getCategories(): Observable<Category[]> {
  return this.http.get<ApiResponse<CategoryDto[]>>(
    `${environment.apiUrl}/categories`
  ).pipe(
    map(response => response.data || [])
  );
}
```

---

## 🛒 CART SERVICE (Port 8083)

### Backend Endpoints

#### GET /api/cart
**Get user cart**
```bash
GET http://localhost:8080/api/cart
Headers:
  X-User-Id: 1
```

**Response:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99,
        "imageUrl": "https://..."
      }
    ],
    "totalPrice": 199.98,
    "totalItems": 2
  }
}
```

**Frontend Call:**
```typescript
// cart.service.ts
loadCartFromBackend(): void {
  this.httpClient.get<ApiResponse<CartDto>>(
    `${this.apiUrl}`
  ).subscribe({
    next: (response) => {
      const cart = response.data;
      if (cart && cart.items) {
        this.itemsSignal.set(cart.items.map(this.mapToCartItem));
      }
    }
  });
}
```

---

#### POST /api/cart/items
**Add item to cart**
```bash
POST http://localhost:8080/api/cart/items
Headers:
  X-User-Id: 1
  Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Item added to cart",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [...],
    "totalPrice": 199.98,
    "totalItems": 2
  }
}
```

**Frontend Call:**
```typescript
// checkout.component.ts
addItemToCart(productId: number, quantity: number): Observable<any> {
  return this.http.post(
    `${environment.apiUrl}/cart/items`,
    { productId, quantity },
    { headers: { 'X-User-Id': this.userId.toString() } }
  );
}
```

---

#### PUT /api/cart/items/{itemId}
**Update cart item quantity**
```bash
PUT http://localhost:8080/api/cart/items/1?quantity=3
Headers:
  X-User-Id: 1
```

---

#### DELETE /api/cart/items/{itemId}
**Remove item from cart**
```bash
DELETE http://localhost:8080/api/cart/items/1
Headers:
  X-User-Id: 1
```

---

#### DELETE /api/cart/clear
**Clear entire cart**
```bash
DELETE http://localhost:8080/api/cart/clear
Headers:
  X-User-Id: 1
```

**Frontend Call:**
```typescript
// checkout.component.ts
clearCart(): Observable<any> {
  return this.http.delete(
    `${environment.apiUrl}/cart/clear`,
    { headers: { 'X-User-Id': this.userId.toString() } }
  );
}
```

---

#### GET /api/cart/count
**Get cart item count**
```bash
GET http://localhost:8080/api/cart/count
Headers:
  X-User-Id: 1
```

**Response:**
```json
{
  "success": true,
  "message": "Cart count retrieved",
  "data": 5
}
```

---

## 📦 ORDER SERVICE (Port 8084)

### Backend Endpoints

#### POST /api/orders/checkout
**Place order (checkout)**
```bash
POST http://localhost:8080/api/orders/checkout
Headers:
  X-User-Id: 1
  Content-Type: application/json

{
  "addressId": 1,
  "paymentMethod": "COD"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "orderNumber": "ORD-1733598123456",
    "status": "PENDING",
    "totalAmount": 199.98,
    "paymentStatus": "COD",
    "paymentMethod": "COD",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99
      }
    ],
    "deliveryAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001"
    },
    "createdAt": "2024-12-08T02:15:23"
  }
}
```

**Frontend Call:**
```typescript
// checkout.component.ts
placeOrder(addressId: number, paymentMethod: string): Observable<Order> {
  return this.http.post<ApiResponse<OrderDto>>(
    `${environment.apiUrl}/orders/checkout`,
    { addressId, paymentMethod },
    { headers: { 'X-User-Id': this.userId.toString() } }
  ).pipe(
    map(response => response.data)
  );
}
```

---

#### GET /api/orders
**Get user orders**
```bash
GET http://localhost:8080/api/orders
Headers:
  X-User-Id: 1
```

**Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-1733598123456",
      "status": "PENDING",
      "totalAmount": 199.98,
      "items": [...],
      "createdAt": "2024-12-08T02:15:23"
    }
  ]
}
```

**Frontend Call:**
```typescript
// order.service.ts
getUserOrders(userId: string): Observable<Order[]> {
  return this.httpClient.get<ApiResponse<BackendOrderDto[]>>(
    this.apiUrl
  ).pipe(
    map(response => {
      const orders = response.data || [];
      return orders.map(this.mapBackendOrderToFrontend);
    })
  );
}
```

---

#### GET /api/orders/{id}
**Get order by ID**
```bash
GET http://localhost:8080/api/orders/1
```

**Response:**
```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-1733598123456",
    "status": "PENDING",
    "totalAmount": 199.98,
    "items": [...]
  }
}
```

**Frontend Call:**
```typescript
// order.service.ts
getOrderById(orderId: string): Observable<Order> {
  return this.httpClient.get<ApiResponse<BackendOrderDto>>(
    `${this.apiUrl}/${orderId}`
  ).pipe(
    map(response => this.mapBackendOrderToFrontend(response.data))
  );
}
```

---

#### POST /api/orders/{id}/cancel
**Cancel order**
```bash
POST http://localhost:8080/api/orders/1/cancel
Headers:
  X-User-Id: 1
```

**Frontend Call:**
```typescript
// order.service.ts
cancelOrder(orderId: string): Observable<boolean> {
  return this.httpClient.post<ApiResponse<any>>(
    `${this.apiUrl}/${orderId}/cancel`,
    null
  ).pipe(
    map(response => response.success)
  );
}
```

---

#### GET /api/orders/all
**Get all orders (Admin)**
```bash
GET http://localhost:8080/api/orders/all
Headers:
  Authorization: Bearer <admin-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "orderNumber": "ORD-1733598123456",
    "status": "PENDING",
    "totalAmount": 199.98
  }
]
```

**Frontend Call:**
```typescript
// order.service.ts
getAllOrders(): Observable<Order[]> {
  return this.httpClient.get<BackendOrderDto[]>(
    `${this.apiUrl}/all`
  ).pipe(
    map(orders => orders.map(this.mapBackendOrderToFrontend))
  );
}
```

---

## 💳 PAYMENT SERVICE (Port 8085)

### Backend Endpoints

#### POST /api/payments/initiate
**Initiate payment**
```bash
POST http://localhost:8080/api/payments/initiate
Content-Type: application/json

{
  "orderId": 1,
  "userId": 1,
  "amount": 199.98,
  "paymentMethod": "RAZORPAY"
}
```

---

#### POST /api/payments/dummy
**Process dummy payment (for testing)**
```bash
POST http://localhost:8080/api/payments/dummy
Content-Type: application/json

{
  "orderId": 1,
  "amount": 199.98
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "paymentId": "PAY-123456",
    "status": "COMPLETED"
  }
}
```

**Frontend Call:**
```typescript
// payment.service.ts
processDummyPayment(orderId: number, amount: number): Observable<any> {
  return this.http.post<ApiResponse<PaymentDto>>(
    `${environment.apiUrl}/payments/dummy`,
    { orderId, amount }
  );
}
```

---

## 🔔 NOTIFICATION SERVICE (Port 8086)

### Backend Endpoints

#### GET /api/notifications
**Get user notifications**
```bash
GET http://localhost:8080/api/notifications
Headers:
  X-User-Id: 1
```

**Response:**
```json
[
  {
    "id": "abc123",
    "userId": 1,
    "type": "ORDER_PLACED",
    "message": "Your order #1 has been placed successfully",
    "read": false,
    "createdAt": "2024-12-08T02:15:23"
  }
]
```

**Frontend Call:**
```typescript
// notification.service.ts
getNotifications(): Observable<Notification[]> {
  return this.http.get<Notification[]>(
    `${environment.apiUrl}/notifications`
  );
}
```

---

#### PUT /api/notifications/{id}/read
**Mark notification as read**
```bash
PUT http://localhost:8080/api/notifications/abc123/read
```

---

### WebSocket Connection

**Endpoint:** `ws://localhost:8080/ws`

**Frontend WebSocket Setup:**
```typescript
// notification.service.ts
connect(): void {
  const socket = new SockJS('http://localhost:8080/ws');
  this.stompClient = Stomp.over(socket);
  
  this.stompClient.connect({}, () => {
    const userId = this.authService.user()?.id;
    this.stompClient.subscribe(`/topic/user/${userId}`, (message) => {
      const notification = JSON.parse(message.body);
      this.notificationsSignal.update(n => [notification, ...n]);
    });
  });
}
```

---

## 📊 COMMON PATTERNS

### Request Headers

**All authenticated requests:**
```typescript
headers: {
  'Authorization': `Bearer ${token}`,
  'X-User-Id': userId.toString(),
  'Content-Type': 'application/json'
}
```

### Response Format

**Success:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

### Frontend Interceptor

**auth.interceptor.ts** automatically adds headers:
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('revcart_token');
  const userStr = localStorage.getItem('revcart_user');
  const user = userStr ? JSON.parse(userStr) : null;

  const headers: any = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (user?.id) headers['X-User-Id'] = user.id.toString();

  const clonedRequest = req.clone({ setHeaders: headers });
  return next(clonedRequest);
};
```

---

## 🔗 COMPLETE FLOW EXAMPLE

### User Registration → Login → Add to Cart → Checkout

```typescript
// 1. Register
this.authService.signup({
  name: 'John Doe',
  email: 'john@example.com',
  password: 'password123',
  phone: '1234567890'
}).subscribe(user => {
  // User registered, token saved
  
  // 2. Get products
  this.productService.getProducts().subscribe(products => {
    
    // 3. Add to cart
    this.http.post(`${apiUrl}/cart/items`, {
      productId: products[0].id,
      quantity: 2
    }).subscribe(() => {
      
      // 4. Add address
      this.http.post(`${apiUrl}/profile/addresses`, {
        line1: '123 Main St',
        city: 'New York',
        state: 'NY',
        postalCode: '10001',
        country: 'USA',
        primaryAddress: true
      }).subscribe(address => {
        
        // 5. Checkout
        this.http.post(`${apiUrl}/orders/checkout`, {
          addressId: address.data.id,
          paymentMethod: 'COD'
        }).subscribe(order => {
          console.log('Order placed:', order.data.orderNumber);
          
          // 6. View orders
          this.orderService.getUserOrders(user.id).subscribe(orders => {
            console.log('My orders:', orders);
          });
        });
      });
    });
  });
});
```

---

## 📝 NOTES

1. **All requests go through API Gateway** at `http://localhost:8080`
2. **X-User-Id header is required** for cart, order, profile, notification endpoints
3. **Authorization header is required** for authenticated endpoints
4. **Frontend interceptor automatically adds** both headers
5. **Response format is consistent** across all services: `ApiResponse<T>`
6. **WebSocket uses SockJS + STOMP** for real-time notifications

---

**Total Endpoints:** 40+  
**Services:** 8 microservices  
**Gateway:** Spring Cloud Gateway  
**Frontend:** Angular 18
