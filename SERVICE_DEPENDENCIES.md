# RevCart Microservices - Service Dependencies & API Contracts

## Feign Client Interfaces

### 1. User Service Client (used by: Cart, Order, Payment, Notification, Delivery)

```java
@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);
    
    @PostMapping("/api/users/validate-token")
    TokenValidationResponse validateToken(@RequestBody String token);
    
    @GetMapping("/api/users/{userId}/addresses/{addressId}")
    AddressDto getAddress(@PathVariable Long userId, @PathVariable Long addressId);
}
```

### 2. Product Service Client (used by: Cart, Order)

```java
@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable Long id);
    
    @GetMapping("/api/products/{id}/stock")
    StockResponse checkStock(@PathVariable Long id);
    
    @PutMapping("/api/products/stock/reserve")
    void reserveStock(@RequestBody StockReservationRequest request);
    
    @PutMapping("/api/products/stock/release")
    void releaseStock(@RequestBody StockReleaseRequest request);
}
```

### 3. Cart Service Client (used by: Order)

```java
@FeignClient(name = "cart-service", url = "${services.cart-service.url}")
public interface CartServiceClient {
    
    @GetMapping("/api/cart")
    CartDto getCart(@RequestHeader("Authorization") String token);
    
    @PostMapping("/api/cart/validate")
    CartValidationResponse validateCart(@RequestBody CartValidationRequest request);
    
    @DeleteMapping("/api/cart/clear")
    void clearCart(@RequestHeader("Authorization") String token);
}
```

### 4. Payment Service Client (used by: Order)

```java
@FeignClient(name = "payment-service", url = "${services.payment-service.url}")
public interface PaymentServiceClient {
    
    @PostMapping("/api/payments/initiate")
    PaymentInitiationResponse initiatePayment(@RequestBody PaymentRequest request);
    
    @PostMapping("/api/payments/verify")
    PaymentVerificationResponse verifyPayment(@RequestBody PaymentVerificationRequest request);
    
    @GetMapping("/api/payments/order/{orderId}")
    PaymentDto getPaymentByOrderId(@PathVariable Long orderId);
}
```

### 5. Order Service Client (used by: Payment, Delivery)

```java
@FeignClient(name = "order-service", url = "${services.order-service.url}")
public interface OrderServiceClient {
    
    @GetMapping("/api/orders/{id}")
    OrderDto getOrderById(@PathVariable Long id);
    
    @PutMapping("/api/orders/{id}/status")
    void updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request);
    
    @GetMapping("/api/orders/user/{userId}")
    List<OrderDto> getOrdersByUserId(@PathVariable Long userId);
}
```

### 6. Delivery Service Client (used by: Order)

```java
@FeignClient(name = "delivery-service", url = "${services.delivery-service.url}")
public interface DeliveryServiceClient {
    
    @PostMapping("/api/delivery/assign")
    DeliveryAssignmentResponse assignDelivery(@RequestBody DeliveryAssignmentRequest request);
    
    @GetMapping("/api/delivery/order/{orderId}")
    DeliveryDto getDeliveryByOrderId(@PathVariable Long orderId);
}
```

### 7. Notification Service Client (used by: All services)

```java
@FeignClient(name = "notification-service", url = "${services.notification-service.url}")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);
}
```

---

## Event Schemas

### User Events

```json
// UserRegistered
{
  "eventType": "UserRegistered",
  "timestamp": "2024-01-15T10:30:00Z",
  "userId": 123,
  "email": "user@example.com",
  "name": "John Doe"
}

// UserProfileUpdated
{
  "eventType": "UserProfileUpdated",
  "timestamp": "2024-01-15T10:30:00Z",
  "userId": 123,
  "updatedFields": ["email", "phone"]
}
```

### Product Events

```json
// ProductCreated
{
  "eventType": "ProductCreated",
  "timestamp": "2024-01-15T10:30:00Z",
  "productId": 456,
  "name": "Fresh Tomatoes",
  "price": 239.2,
  "categoryId": 1
}

// StockLevelLow
{
  "eventType": "StockLevelLow",
  "timestamp": "2024-01-15T10:30:00Z",
  "productId": 456,
  "currentStock": 5,
  "threshold": 10
}
```

### Order Events

```json
// OrderPlaced
{
  "eventType": "OrderPlaced",
  "timestamp": "2024-01-15T10:30:00Z",
  "orderId": 789,
  "userId": 123,
  "totalAmount": 1500.0,
  "items": [
    {"productId": 456, "quantity": 2, "price": 239.2}
  ]
}

// OrderConfirmed
{
  "eventType": "OrderConfirmed",
  "timestamp": "2024-01-15T10:35:00Z",
  "orderId": 789,
  "userId": 123,
  "estimatedDelivery": "2024-01-16T18:00:00Z"
}
```

### Payment Events

```json
// PaymentCompleted
{
  "eventType": "PaymentCompleted",
  "timestamp": "2024-01-15T10:32:00Z",
  "paymentId": 321,
  "orderId": 789,
  "amount": 1500.0,
  "method": "RAZORPAY",
  "transactionId": "pay_xyz123"
}

// PaymentFailed
{
  "eventType": "PaymentFailed",
  "timestamp": "2024-01-15T10:32:00Z",
  "paymentId": 321,
  "orderId": 789,
  "reason": "Insufficient funds"
}
```

### Delivery Events

```json
// DeliveryAssigned
{
  "eventType": "DeliveryAssigned",
  "timestamp": "2024-01-15T10:40:00Z",
  "deliveryId": 555,
  "orderId": 789,
  "agentId": 999,
  "estimatedTime": "2024-01-16T18:00:00Z"
}

// DeliveryCompleted
{
  "eventType": "DeliveryCompleted",
  "timestamp": "2024-01-16T17:45:00Z",
  "deliveryId": 555,
  "orderId": 789,
  "completedAt": "2024-01-16T17:45:00Z"
}
```

---

## Shared DTOs

### Common Response Wrapper
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

### User DTOs
```java
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
}

public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

### Product DTOs
```java
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private CategoryDto category;
    private Integer stockQuantity;
}

public class StockReservationRequest {
    private List<StockItem> items;
    private String reservationId;
}
```

### Order DTOs
```java
public class OrderDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private Double totalAmount;
    private List<OrderItemDto> items;
    private AddressDto deliveryAddress;
    private LocalDateTime createdAt;
}

public class OrderItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
}
```

### Payment DTOs
```java
public class PaymentDto {
    private Long id;
    private Long orderId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
}
```

---

## Circuit Breaker Configuration (Resilience4j)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        
      productService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        
      paymentService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 30
        waitDurationInOpenState: 30s
        
  retry:
    instances:
      userService:
        maxAttempts: 3
        waitDuration: 1s
        
      productService:
        maxAttempts: 3
        waitDuration: 1s
```

---

## API Gateway Configuration (AWS API Gateway / Kong)

```yaml
routes:
  - name: user-service
    paths: ["/api/users"]
    methods: ["GET", "POST", "PUT", "DELETE"]
    strip_path: false
    service:
      url: http://user-service.revcart.svc.cluster.local
    plugins:
      - name: jwt
      - name: rate-limiting
        config:
          minute: 100
          
  - name: product-service
    paths: ["/api/products", "/api/categories"]
    methods: ["GET", "POST", "PUT", "DELETE"]
    service:
      url: http://product-service.revcart.svc.cluster.local
    plugins:
      - name: rate-limiting
        config:
          minute: 200
          
  - name: order-service
    paths: ["/api/orders"]
    methods: ["GET", "POST", "PUT"]
    service:
      url: http://order-service.revcart.svc.cluster.local
    plugins:
      - name: jwt
      - name: rate-limiting
        config:
          minute: 50
```

---

## Service Configuration Template

```yaml
# application.yml (common for all services)
spring:
  application:
    name: ${SERVICE_NAME}
  
  cloud:
    aws:
      region:
        static: us-east-1
      credentials:
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}

server:
  port: ${PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      cloudwatch:
        namespace: RevCart
        batchSize: 20

logging:
  level:
    root: INFO
    com.revcart: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# Feign clients
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
  circuitbreaker:
    enabled: true

# Service URLs
services:
  user-service:
    url: http://user-service.revcart.svc.cluster.local
  product-service:
    url: http://product-service.revcart.svc.cluster.local
  cart-service:
    url: http://cart-service.revcart.svc.cluster.local
  order-service:
    url: http://order-service.revcart.svc.cluster.local
  payment-service:
    url: http://payment-service.revcart.svc.cluster.local
  notification-service:
    url: http://notification-service.revcart.svc.cluster.local
  delivery-service:
    url: http://delivery-service.revcart.svc.cluster.local
  analytics-service:
    url: http://analytics-service.revcart.svc.cluster.local
```

---

## Database Schema Distribution

### RDS MySQL (User Service)
```sql
-- users, addresses, admins, otp_tokens
```

### RDS MySQL (Product Service)
```sql
-- products, categories, inventory
```

### RDS MySQL (Cart Service)
```sql
-- carts, cart_items
```

### RDS MySQL (Order Service)
```sql
-- orders, order_items
```

### RDS MySQL (Payment Service)
```sql
-- payments, payment_history
```

### MongoDB (Notification Service)
```javascript
// notifications collection
{
  _id: ObjectId,
  userId: Long,
  type: String,
  title: String,
  message: String,
  read: Boolean,
  createdAt: ISODate
}
```

### MongoDB (Delivery Service)
```javascript
// delivery_tracking_logs collection
{
  _id: ObjectId,
  orderId: Long,
  agentId: Long,
  status: String,
  location: {
    lat: Double,
    lng: Double
  },
  timestamp: ISODate
}
```

### MongoDB (Analytics Service)
```javascript
// activity_history collection
{
  _id: ObjectId,
  userId: Long,
  action: String,
  entityType: String,
  entityId: Long,
  metadata: Object,
  timestamp: ISODate
}
```

---

## Testing Strategy

### Unit Tests (per service)
- Service layer: Business logic
- Controller layer: Request/response handling
- Repository layer: Data access

### Integration Tests
- Feign client communication
- Database operations
- Event publishing/consuming

### Contract Tests (Pact)
- Consumer-driven contracts between services
- Verify API compatibility

### End-to-End Tests
- Complete user flows (registration → order → payment → delivery)
- Run against staging environment

### Load Tests (Gatling)
```scala
scenario("Order Checkout Flow")
  .exec(http("Login").post("/api/users/login"))
  .exec(http("Add to Cart").post("/api/cart/items"))
  .exec(http("Checkout").post("/api/orders/checkout"))
  .exec(http("Payment").post("/api/payments/capture"))
```

---

## Monitoring & Alerting

### Service-Level Indicators (SLIs)
- **Availability**: Uptime percentage
- **Latency**: Response time (p50, p95, p99)
- **Throughput**: Requests per second
- **Error Rate**: Failed requests percentage

### Service-Level Objectives (SLOs)
- Availability: 99.9% uptime
- Latency (p95): < 500ms
- Error Rate: < 1%

### Alerts
```yaml
# CloudWatch Alarms
- HighErrorRate:
    threshold: 5%
    period: 5 minutes
    
- HighLatency:
    metric: p99
    threshold: 2000ms
    period: 5 minutes
    
- LowAvailability:
    threshold: 99%
    period: 15 minutes
```
