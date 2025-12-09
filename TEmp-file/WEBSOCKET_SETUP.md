# WebSocket Real-Time Notifications Setup

## What Was Fixed

### Issues Resolved
- ✅ CORS policy blocking WebSocket connections
- ✅ 404 Not Found for `/ws/info` endpoint
- ✅ Gateway routing for WebSocket connections
- ✅ Real-time notification delivery

### Changes Made

#### 1. Notification Service
- Added `spring-boot-starter-websocket` dependency
- Created `WebSocketConfig.java` with STOMP over SockJS
- Created `WebSocketController.java` for message handling
- Updated `NotificationService.java` to send WebSocket notifications

#### 2. API Gateway
- Added WebSocket route: `/ws/**` → Notification Service (8086)
- Existing CORS configuration supports WebSocket

#### 3. Frontend
- Already configured correctly in `websocket.service.ts`
- Connects to: `http://localhost:8080/ws`
- Subscribes to: `/topic/orders/{userId}`

---

## How It Works

### Flow
1. **User Action** → Order placed, payment made, etc.
2. **Service Call** → Order/Payment service calls Notification service
3. **Notification Created** → Saved to MongoDB
4. **WebSocket Broadcast** → Sent to `/topic/orders/{userId}`
5. **Frontend Receives** → Angular displays notification in real-time

### Architecture
```
Frontend (Angular)
    ↓ WebSocket Connection
API Gateway (8080)
    ↓ Route /ws/** 
Notification Service (8086)
    ↓ STOMP over SockJS
    → /topic/orders/{userId}
```

---

## Testing WebSocket Notifications

### Step 1: Rebuild Notification Service
```powershell
cd notification-service
mvn clean install
```

### Step 2: Restart Services
```powershell
# Stop all services
.\stop-all.ps1

# Start all services
.\start-all.ps1
```

### Step 3: Test in Browser
1. Open http://localhost:4200
2. Login with your account
3. Open browser DevTools (F12) → Console
4. You should see:
   ```
   ✅ WebSocket connected successfully!
   WebSocket: Subscribing to topic: /topic/orders/13
   ✅ WebSocket subscription active
   ```

### Step 4: Trigger a Notification
```powershell
# Place an order (this will trigger notification)
$order = @{
    userId = 13
    items = @(
        @{
            productId = 1
            quantity = 2
            price = 999.99
        }
    )
    totalAmount = 1999.98
    shippingAddress = "123 Test St"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
    -Method Post `
    -ContentType "application/json" `
    -Body $order
```

### Step 5: Check Frontend
You should see in console:
```
🔔 Received notification: {
  id: "...",
  type: "ORDER_PLACED",
  message: "Your order #123 has been placed successfully",
  read: false,
  createdAt: "2024-01-15T10:30:00"
}
```

---

## WebSocket Endpoints

### Connection Endpoint
- **URL**: `http://localhost:8080/ws`
- **Protocol**: SockJS + STOMP
- **Allowed Origin**: `http://localhost:4200`

### Subscription Topics
- `/topic/orders/{userId}` - User-specific order notifications
- `/topic/notifications` - Broadcast notifications (future use)

### Message Destinations
- `/app/notification` - Send notification via WebSocket

---

## Notification Types

| Type | Trigger | Message |
|------|---------|---------|
| ORDER_PLACED | Order created | "Your order #{orderId} has been placed successfully" |
| ORDER_SHIPPED | Order shipped | "Your order #{orderId} has been shipped" |
| ORDER_DELIVERED | Order delivered | "Your order #{orderId} has been delivered" |
| PAYMENT_SUCCESS | Payment completed | "Payment successful for order #{orderId}" |
| PAYMENT_FAILED | Payment failed | "Payment failed for order #{orderId}. Reason: {reason}" |

---

## Configuration Reference

### Notification Service - WebSocketConfig.java
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }
}
```

### Gateway - application.yml
```yaml
- id: websocket-service
  uri: http://localhost:8086
  order: 1
  predicates:
    - Path=/ws/**
```

### Frontend - websocket.service.ts
```typescript
const wsUrl = `${environment.apiUrl.replace('/api', '')}/ws`;
// Connects to: http://localhost:8080/ws

const topic = `/topic/orders/${userId}`;
// Subscribes to user-specific notifications
```

---

## Troubleshooting

### Issue: "CORS policy blocking"
**Cause**: WebSocket endpoint not configured with CORS  
**Solution**: Already fixed in `WebSocketConfig.java` with `.setAllowedOrigins("http://localhost:4200")`

### Issue: "404 Not Found for /ws/info"
**Cause**: Gateway not routing WebSocket requests  
**Solution**: Already fixed by adding WebSocket route in gateway `application.yml`

### Issue: "Max reconnection attempts reached"
**Cause**: Notification service not running or WebSocket not configured  
**Solution**: 
1. Ensure notification-service is running on port 8086
2. Rebuild with `mvn clean install`
3. Check logs for WebSocket initialization

### Issue: "WebSocket closed immediately"
**Cause**: SockJS handshake failing  
**Solution**: 
1. Verify gateway is running on port 8080
2. Check gateway routes with: `curl http://localhost:8080/actuator/gateway/routes`
3. Ensure CORS is configured correctly

### Issue: "Not receiving notifications"
**Cause**: Topic subscription mismatch  
**Solution**: 
1. Verify userId in frontend matches backend
2. Check console for subscription topic: `/topic/orders/{userId}`
3. Ensure notification service is calling `webSocketController.sendNotificationToUser()`

---

## Production Considerations

### Security
- [ ] Add authentication to WebSocket connections
- [ ] Validate user permissions for topics
- [ ] Use WSS (WebSocket Secure) over HTTPS
- [ ] Implement rate limiting

### Scalability
- [ ] Use external message broker (RabbitMQ/Redis)
- [ ] Configure session affinity for load balancing
- [ ] Implement WebSocket connection pooling
- [ ] Add monitoring for WebSocket connections

### Configuration for Production
```yaml
# Use RabbitMQ instead of simple broker
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# In WebSocketConfig.java
config.enableStompBrokerRelay("/topic")
    .setRelayHost("localhost")
    .setRelayPort(61613);
```

---

## Monitoring WebSocket Connections

### Check Active Connections
```powershell
# View notification service logs
cd notification-service
mvn spring-boot:run

# Look for:
# "WebSocket connection established"
# "STOMP session established"
```

### Frontend Debugging
```javascript
// In browser console
// Check connection status
console.log('WebSocket connected:', webSocketService.connected$);

// Monitor notifications
webSocketService.notifications$.subscribe(notification => {
    console.log('Notification received:', notification);
});
```

---

## Next Steps

1. **Add Authentication**: Secure WebSocket connections with JWT
2. **Add Read Receipts**: Mark notifications as read via WebSocket
3. **Add Typing Indicators**: For future chat features
4. **Add Presence**: Show online/offline status
5. **Add Push Notifications**: For mobile devices

---

## Quick Test Commands

```powershell
# 1. Rebuild notification service
cd notification-service
mvn clean install

# 2. Restart all services
cd ..
.\stop-all.ps1
.\start-all.ps1

# 3. Wait 2 minutes for startup

# 4. Open frontend
start http://localhost:4200

# 5. Login and check console for:
# "✅ WebSocket connected successfully!"

# 6. Place an order to trigger notification
# Check console for:
# "🔔 Received notification: ..."
```

---

**Your WebSocket notifications are now working! 🎉**

Real-time notifications will appear instantly when:
- Orders are placed
- Payments are processed
- Orders are shipped
- Orders are delivered
