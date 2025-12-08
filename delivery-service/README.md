# Delivery Service

Delivery Service for RevCart microservices architecture. Handles delivery agent assignment, order tracking, and delivery status updates.

## Features

- Assign delivery agents to orders
- Track delivery status (ASSIGNED → PICKED_UP → OUT_FOR_DELIVERY → DELIVERED)
- Store delivery tracking logs with timestamps and location
- Notify Order Service and Notification Service on status changes
- REST API for delivery agents and customers
- MySQL database for persistence
- Kubernetes-ready with health probes and HPA

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Cloud OpenFeign
- MySQL 8.0
- Docker
- Kubernetes

## Database Schema

### Delivery Entity
```sql
- id (BIGINT, Primary Key)
- order_id (BIGINT, UNIQUE, NOT NULL)
- user_id (BIGINT, NOT NULL)
- agent_id (BIGINT)
- status (VARCHAR, ENUM: ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED)
- estimated_delivery_date (TIMESTAMP)
- actual_delivery_date (TIMESTAMP)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### DeliveryTrackingLog Entity
```sql
- id (BIGINT, Primary Key)
- delivery_id (BIGINT, Foreign Key)
- status (VARCHAR, ENUM)
- location (VARCHAR)
- message (VARCHAR)
- timestamp (TIMESTAMP)
```

## REST API Endpoints

### 1. Assign Delivery Agent
```bash
POST /api/delivery/assign
Content-Type: application/json

{
  "orderId": 1,
  "userId": 1,
  "agentId": 101,
  "estimatedDeliveryDate": "2024-01-15T10:00:00"
}

# Example
curl -X POST http://localhost:8087/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "agentId": 101,
    "estimatedDeliveryDate": "2024-01-15T10:00:00"
  }'
```

### 2. Update Delivery Status
```bash
PUT /api/delivery/{orderId}/status
Content-Type: application/json

{
  "status": "PICKED_UP",
  "location": "Warehouse",
  "message": "Package picked up by delivery agent"
}

# Example
curl -X PUT http://localhost:8087/api/delivery/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "OUT_FOR_DELIVERY",
    "location": "City Center",
    "message": "Package is out for delivery"
  }'
```

### 3. Get Delivery Information
```bash
GET /api/delivery/{orderId}

# Example
curl http://localhost:8087/api/delivery/1
```

### 4. Get Tracking History
```bash
GET /api/delivery/{orderId}/track

# Example
curl http://localhost:8087/api/delivery/1/track
```

### 5. Get User Deliveries
```bash
GET /api/delivery/user/{userId}

# Example
curl http://localhost:8087/api/delivery/user/1
```

## Delivery Status Flow

```
ASSIGNED → PICKED_UP → OUT_FOR_DELIVERY → DELIVERED
```

1. **ASSIGNED** - Delivery agent assigned to order
2. **PICKED_UP** - Package picked up from warehouse
3. **OUT_FOR_DELIVERY** - Package is out for delivery
4. **DELIVERED** - Package delivered to customer

## Integration with Other Services

### Order Service Integration
- Updates order status to SHIPPED when delivery is assigned
- Updates order status to DELIVERED when delivery is completed

### Notification Service Integration
- Sends notification when delivery is assigned (ORDER_SHIPPED)
- Sends notification when delivery is completed (ORDER_DELIVERED)

### Fault Tolerance
- All Feign client calls wrapped in try-catch blocks
- Delivery operations continue even if notifications fail
- Errors logged for monitoring

## Running Locally

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8.0
- Order Service running on port 8084
- Notification Service running on port 8086

### Steps
1. Start MySQL:
```bash
mysql -u root -p
CREATE DATABASE revcart_delivery;
```

2. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

3. Service will start on port 8087

## Docker Build

```bash
docker build -t delivery-service:latest .
docker run -p 8087:8087 \
  -e DB_HOST=mysql \
  -e ORDER_SERVICE_URL=http://order-service:8084 \
  -e NOTIFICATION_SERVICE_URL=http://notification-service:8086 \
  delivery-service:latest
```

## Kubernetes Deployment

### 1. Deploy to Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 2. Verify Deployment
```bash
kubectl get pods -l app=delivery-service
kubectl get svc delivery-service
kubectl logs -f deployment/delivery-service
```

### 3. Scale Deployment
```bash
kubectl scale deployment delivery-service --replicas=3
```

## Testing

### Test Assign Delivery
```bash
curl -X POST http://localhost:8087/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "agentId": 101,
    "estimatedDeliveryDate": "2024-01-15T10:00:00"
  }'
```

### Test Update Status
```bash
curl -X PUT http://localhost:8087/api/delivery/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PICKED_UP",
    "location": "Warehouse",
    "message": "Package picked up"
  }'
```

### Test Get Tracking History
```bash
curl http://localhost:8087/api/delivery/1/track
```

## Health Checks

- Liveness: http://localhost:8087/actuator/health/liveness
- Readiness: http://localhost:8087/actuator/health/readiness
- Metrics: http://localhost:8087/actuator/metrics

## Future Enhancements

- Real-time GPS tracking for delivery agents
- Estimated time of arrival (ETA) calculation
- Delivery route optimization
- Proof of delivery (signature/photo)
- Delivery agent mobile app integration
- Customer delivery preferences
- Delivery slot booking
- Failed delivery handling and retry logic

## Troubleshooting

### Delivery Not Found
- Verify order exists in Order Service
- Check if delivery was assigned for the order

### Status Update Failed
- Verify delivery exists for the order
- Check status transition is valid
- Ensure status enum value is correct

### Database Connection Issues
- Verify MySQL is running
- Check DB_HOST and DB_PORT
- Verify database credentials
