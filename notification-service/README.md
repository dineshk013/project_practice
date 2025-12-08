# Notification Service

Notification Service for RevCart microservices architecture. Handles email notifications for order and payment events.

## Features

- Email notifications for order events (placed, shipped, delivered, cancelled)
- Email notifications for payment events (success, failed, refunded)
- Notification history storage per user
- Asynchronous email sending
- Integration with User Service via Feign client
- REST API for notification management
- MySQL database for persistence
- Kubernetes-ready with health probes and HPA

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Mail (JavaMailSender)
- Spring Cloud OpenFeign
- MySQL 8.0
- Docker
- Kubernetes

## Database Schema

### Notification Entity
```sql
- id (BIGINT, Primary Key)
- user_id (BIGINT, NOT NULL)
- type (VARCHAR, ENUM: ORDER_PLACED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_REFUNDED)
- message (VARCHAR, NOT NULL)
- status (VARCHAR, ENUM: SENT, FAILED, PENDING)
- reference_id (BIGINT, Order/Payment ID)
- recipient_email (VARCHAR)
- failure_reason (VARCHAR)
- created_at (TIMESTAMP)
```

## REST API Endpoints

### 1. Notify Order Event
```bash
POST /api/notifications/order/{orderId}?userId={userId}&eventType={eventType}

# Example: Order Placed
curl -X POST "http://localhost:8086/api/notifications/order/1?userId=1&eventType=PLACED"

# Example: Order Shipped
curl -X POST "http://localhost:8086/api/notifications/order/1?userId=1&eventType=SHIPPED"

# Example: Order Delivered
curl -X POST "http://localhost:8086/api/notifications/order/1?userId=1&eventType=DELIVERED"
```

### 2. Notify Payment Event
```bash
POST /api/notifications/payment/{paymentId}?userId={userId}&orderId={orderId}&status={status}&reason={reason}

# Example: Payment Success
curl -X POST "http://localhost:8086/api/notifications/payment/1?userId=1&orderId=1&status=SUCCESS"

# Example: Payment Failed
curl -X POST "http://localhost:8086/api/notifications/payment/1?userId=1&orderId=1&status=FAILED&reason=Insufficient+funds"
```

### 3. Create Custom Notification
```bash
POST /api/notifications
Content-Type: application/json

{
  "userId": 1,
  "type": "ORDER_PLACED",
  "message": "Your order has been placed successfully",
  "referenceId": 1
}
```

### 4. Get User Notifications
```bash
GET /api/notifications/user/{userId}

# Example
curl http://localhost:8086/api/notifications/user/1
```

### 5. Get Notification by ID
```bash
GET /api/notifications/{id}

# Example
curl http://localhost:8086/api/notifications/1
```

## Email Configuration

### Gmail Setup
1. Enable 2-Factor Authentication in your Google account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Update application.yml or environment variables:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

### Environment Variables
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
NOTIFICATION_FROM_EMAIL=noreply@revcart.com
EMAIL_ENABLED=true
```

## Notification Flow

1. **Order Service** calls Notification Service after order creation
2. **Payment Service** calls Notification Service after payment processing
3. Notification Service fetches user email from User Service
4. Creates notification record in database with PENDING status
5. Sends email asynchronously using JavaMailSender
6. Updates notification status to SENT or FAILED
7. Stores failure reason if email sending fails

## Running Locally

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8.0
- User Service running on port 8081

### Steps
1. Start MySQL:
```bash
mysql -u root -p
CREATE DATABASE revcart_notifications;
```

2. Update application.yml with your email credentials

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

4. Service will start on port 8086

## Docker Build

```bash
docker build -t notification-service:latest .
docker run -p 8086:8086 \
  -e DB_HOST=mysql \
  -e USER_SERVICE_URL=http://user-service:8081 \
  -e MAIL_USERNAME=your-email@gmail.com \
  -e MAIL_PASSWORD=your-app-password \
  notification-service:latest
```

## Kubernetes Deployment

### 1. Update Secrets
Edit k8s/deployment.yaml and update email credentials:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: notification-secrets
stringData:
  mail.username: "your-email@gmail.com"
  mail.password: "your-app-password"
```

### 2. Deploy to Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 3. Verify Deployment
```bash
kubectl get pods -l app=notification-service
kubectl get svc notification-service
kubectl logs -f deployment/notification-service
```

### 4. Scale Deployment
```bash
kubectl scale deployment notification-service --replicas=3
```

## Testing

### Test Email Sending
```bash
# Create notification
curl -X POST http://localhost:8086/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "ORDER_PLACED",
    "message": "Test notification",
    "referenceId": 1
  }'

# Check logs for email sending status
```

### Test User Notifications Retrieval
```bash
curl http://localhost:8086/api/notifications/user/1
```

## Integration with Other Services

### Order Service Integration
```java
// After order creation
restTemplate.postForEntity(
    "http://notification-service/api/notifications/order/{orderId}?userId={userId}&eventType=PLACED",
    null,
    ApiResponse.class,
    orderId,
    userId
);
```

### Payment Service Integration
```java
// After payment success
restTemplate.postForEntity(
    "http://notification-service/api/notifications/payment/{paymentId}?userId={userId}&orderId={orderId}&status=SUCCESS",
    null,
    ApiResponse.class,
    paymentId,
    userId,
    orderId
);
```

## Future Enhancements

- Kafka/RabbitMQ integration for event-driven notifications
- SMS notifications via AWS SNS
- Push notifications for mobile apps
- Notification templates with dynamic content
- User notification preferences management
- Retry mechanism for failed email deliveries
- Email delivery tracking and analytics

## Health Checks

- Liveness: http://localhost:8086/actuator/health/liveness
- Readiness: http://localhost:8086/actuator/health/readiness
- Metrics: http://localhost:8086/actuator/metrics

## Troubleshooting

### Email Not Sending
1. Check email credentials in application.yml
2. Verify Gmail App Password is correct
3. Check logs for email sending errors
4. Ensure EMAIL_ENABLED=true

### User Email Not Found
1. Verify User Service is running
2. Check USER_SERVICE_URL configuration
3. Ensure user exists in User Service

### Database Connection Issues
1. Verify MySQL is running
2. Check DB_HOST and DB_PORT
3. Verify database credentials
