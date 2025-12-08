# RevCart API Gateway

API Gateway for RevCart microservices architecture using Spring Cloud Gateway. Routes all client requests to appropriate backend microservices with JWT authentication, circuit breaker, and load balancing.

## Features

- Centralized routing to 8 microservices
- JWT token validation for secure endpoints
- Circuit breaker with fallback responses
- CORS enabled for frontend integration
- Request logging and monitoring
- Health checks and metrics
- Kubernetes-ready with HPA

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Cloud Gateway
- Resilience4j Circuit Breaker
- JJWT for JWT validation
- Docker
- Kubernetes

## Architecture

```
Client (Frontend/Mobile)
        ↓
   API Gateway (Port 8080)
        ↓
   ┌────┴────┬────────┬────────┬────────┬────────┬────────┬────────┐
   ↓         ↓        ↓        ↓        ↓        ↓        ↓        ↓
User(8081) Product Cart   Order   Payment Notify Delivery Analytics
          (8082)  (8083) (8084)  (8085)  (8086)  (8087)   (8088)
```

## Route Mappings

| Path | Target Service | Port |
|------|---------------|------|
| /api/users/** | user-service | 8081 |
| /api/products/** | product-service | 8082 |
| /api/categories/** | product-service | 8082 |
| /api/cart/** | cart-service | 8083 |
| /api/orders/** | order-service | 8084 |
| /api/payments/** | payment-service | 8085 |
| /api/notifications/** | notification-service | 8086 |
| /api/delivery/** | delivery-service | 8087 |
| /api/analytics/** | analytics-service | 8088 |

## Public Endpoints (No Authentication)

- POST /api/users/register
- POST /api/users/login
- GET /actuator/**

All other endpoints require JWT token in Authorization header.

## Running Locally

### Prerequisites
- Java 17
- Maven 3.9+
- All backend microservices running

### Steps
1. Build:
```bash
mvn clean install
```

2. Run:
```bash
mvn spring-boot:run
```

3. Gateway will start on port 8080

## Testing with JWT

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "phone": "1234567890"
  }'
```

### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

Response:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {...}
  }
}
```

### 3. Access Protected Endpoints
```bash
# Get Products (requires JWT)
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get User Profile (requires JWT)
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get Cart (requires JWT)
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get Orders (requires JWT)
curl http://localhost:8080/api/orders/user/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get Analytics Dashboard (requires JWT)
curl http://localhost:8080/api/analytics/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Error Responses

### Unauthorized (401)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "path": "/api/products",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Service Unavailable (503)
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "product-service is currently unavailable. Please try again later.",
  "path": "/fallback/product-service",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Circuit Breaker Configuration

- Sliding Window Size: 10 requests
- Minimum Calls: 5
- Failure Rate Threshold: 50%
- Wait Duration in Open State: 10 seconds
- Half-Open State Calls: 3

## Docker Build

```bash
docker build -t revcart-gateway:latest .
docker run -p 8080:8080 \
  -e USER_SERVICE_URL=http://user-service:8081 \
  -e PRODUCT_SERVICE_URL=http://product-service:8082 \
  -e CART_SERVICE_URL=http://cart-service:8083 \
  -e ORDER_SERVICE_URL=http://order-service:8084 \
  -e PAYMENT_SERVICE_URL=http://payment-service:8085 \
  -e NOTIFICATION_SERVICE_URL=http://notification-service:8086 \
  -e DELIVERY_SERVICE_URL=http://delivery-service:8087 \
  -e ANALYTICS_SERVICE_URL=http://analytics-service:8088 \
  -e JWT_SECRET=your-secret-key \
  revcart-gateway:latest
```

## Kubernetes Deployment

### 1. Deploy Gateway
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 2. Verify Deployment
```bash
kubectl get pods -l app=revcart-gateway
kubectl get svc revcart-gateway
kubectl logs -f deployment/revcart-gateway
```

### 3. Access Gateway
```bash
# Get LoadBalancer IP
kubectl get svc revcart-gateway

# Test endpoint
curl http://<EXTERNAL-IP>/api/products
```

## Health Checks

- Liveness: http://localhost:8080/actuator/health/liveness
- Readiness: http://localhost:8080/actuator/health/readiness
- Gateway Routes: http://localhost:8080/actuator/gateway/routes
- Metrics: http://localhost:8080/actuator/metrics

## Configuration

### JWT Secret
Set via environment variable:
```bash
export JWT_SECRET=your-secret-key-here
```

### Service URLs
Configure in application.yml or via environment variables:
```yaml
USER_SERVICE_URL: http://user-service:8081
PRODUCT_SERVICE_URL: http://product-service:8082
...
```

## Postman Collection

### Import Collection
Create a new collection with these requests:

1. **Register User**
   - Method: POST
   - URL: http://localhost:8080/api/users/register
   - Body: JSON with name, email, password, phone

2. **Login**
   - Method: POST
   - URL: http://localhost:8080/api/users/login
   - Body: JSON with email, password
   - Save token from response

3. **Get Products**
   - Method: GET
   - URL: http://localhost:8080/api/products
   - Headers: Authorization: Bearer {{token}}

4. **Add to Cart**
   - Method: POST
   - URL: http://localhost:8080/api/cart/add
   - Headers: Authorization: Bearer {{token}}
   - Body: JSON with productId, quantity

5. **Checkout**
   - Method: POST
   - URL: http://localhost:8080/api/orders/checkout
   - Headers: Authorization: Bearer {{token}}
   - Body: JSON with addressId, paymentMethod

6. **Get Analytics**
   - Method: GET
   - URL: http://localhost:8080/api/analytics/dashboard
   - Headers: Authorization: Bearer {{token}}

## Troubleshooting

### 401 Unauthorized
- Verify JWT token is valid
- Check Authorization header format: "Bearer <token>"
- Ensure JWT secret matches across services

### 503 Service Unavailable
- Check if backend service is running
- Verify service URL configuration
- Review circuit breaker status

### CORS Issues
- Verify CORS configuration in GlobalCorsConfig
- Check allowed origins match frontend URL

## Future Enhancements

- Rate limiting per user/IP
- Request/response transformation
- API versioning support
- OAuth2 integration
- Request caching
- API documentation with Swagger
- Distributed tracing with Zipkin
- Advanced monitoring with Prometheus/Grafana
