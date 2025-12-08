# Analytics Service

Analytics Service for RevCart microservices architecture. Aggregates data from Order, Product, and User services to provide business intelligence and insights.

## Features

- Real-time analytics aggregation from multiple microservices
- Order analytics (total orders, revenue, average order value)
- Product analytics (top selling products, low stock alerts)
- User analytics (total users, new users, returning customers)
- Scheduled analytics refresh every 60 minutes
- Metrics caching in MySQL database
- REST API for dashboard and reporting
- Kubernetes-ready with health probes and HPA

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Scheduling
- MySQL 8.0
- Docker
- Kubernetes

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Analytics Service                         │
│                      (Port 8088)                             │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│Order Service │    │Product Service│   │User Service  │
│  (Port 8084) │    │  (Port 8082)  │   │ (Port 8081)  │
└──────────────┘    └──────────────┘    └──────────────┘
```

## Database Schema

### AnalyticsMetric Entity
```sql
- id (BIGINT, Primary Key)
- metric_name (VARCHAR(100), NOT NULL)
- metric_value (DOUBLE, NOT NULL)
- last_updated (TIMESTAMP, AUTO UPDATE)
- period (VARCHAR(20), ENUM: DAILY, WEEKLY, MONTHLY, REALTIME)
```

## REST API Endpoints

### 1. Get Order Analytics Summary
```bash
GET /api/analytics/orders/summary

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "totalOrders": 150,
    "totalRevenue": 45000.0,
    "avgOrderValue": 300.0,
    "ordersToday": 12,
    "ordersThisWeek": 45,
    "ordersThisMonth": 150
  },
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/orders/summary
```

### 2. Get Product Analytics Summary
```bash
GET /api/analytics/products/summary

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "topSellingProducts": [
      {
        "id": 1,
        "name": "Laptop",
        "stock": 50,
        "price": 999.99
      }
    ],
    "lowStockProducts": [
      {
        "id": 5,
        "name": "Mouse",
        "stock": 5,
        "price": 19.99
      }
    ],
    "totalProducts": 200
  },
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/products/summary
```

### 3. Get User Analytics Summary
```bash
GET /api/analytics/users/summary

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "totalUsers": 500,
    "newUsersThisMonth": 50,
    "returningCustomers": 450
  },
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/users/summary
```

### 4. Get Top Selling Products
```bash
GET /api/analytics/top-products

Response:
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Laptop",
      "stock": 50,
      "price": 999.99
    },
    {
      "id": 2,
      "name": "Smartphone",
      "stock": 100,
      "price": 699.99
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/top-products
```

### 5. Get Low Stock Products
```bash
GET /api/analytics/low-stock

Response:
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 5,
      "name": "Mouse",
      "stock": 5,
      "price": 19.99
    },
    {
      "id": 8,
      "name": "Keyboard",
      "stock": 8,
      "price": 49.99
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/low-stock
```

### 6. Get Complete Dashboard
```bash
GET /api/analytics/dashboard

Response:
{
  "success": true,
  "message": "Success",
  "data": {
    "orderAnalytics": {
      "totalOrders": 150,
      "totalRevenue": 45000.0,
      "avgOrderValue": 300.0,
      "ordersToday": 12,
      "ordersThisWeek": 45,
      "ordersThisMonth": 150
    },
    "productAnalytics": {
      "topSellingProducts": [...],
      "lowStockProducts": [...],
      "totalProducts": 200
    },
    "userAnalytics": {
      "totalUsers": 500,
      "newUsersThisMonth": 50,
      "returningCustomers": 450
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}

# Example
curl http://localhost:8088/api/analytics/dashboard
```

## Analytics Metrics

### Order Metrics
- **totalOrders**: Total number of orders placed
- **totalRevenue**: Sum of all order amounts
- **avgOrderValue**: Average order value (totalRevenue / totalOrders)
- **ordersToday**: Orders placed today
- **ordersThisWeek**: Orders placed in the last 7 days
- **ordersThisMonth**: Orders placed in the last 30 days

### Product Metrics
- **topSellingProducts**: Top 5 products by stock (highest stock first)
- **lowStockProducts**: Products with stock < 10
- **totalProducts**: Total number of products

### User Metrics
- **totalUsers**: Total registered users
- **newUsersThisMonth**: Users registered in the last 30 days
- **returningCustomers**: Total users minus new users

## Scheduled Analytics Refresh

Analytics are automatically refreshed every 60 minutes using Spring @Scheduled:

```java
@Scheduled(fixedRate = 3600000, initialDelay = 10000)
public void refreshAnalytics()
```

- Initial delay: 10 seconds after startup
- Refresh interval: 60 minutes (configurable via `analytics.cache.ttl-minutes`)
- Metrics stored in `analytics_metrics` table for caching

## Running Locally

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8.0
- Order Service running on port 8084
- Product Service running on port 8082
- User Service running on port 8081

### Steps
1. Start MySQL:
```bash
mysql -u root -p
CREATE DATABASE revcart_analytics;
```

2. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

3. Service will start on port 8088

## Docker Build

```bash
docker build -t analytics-service:latest .
docker run -p 8088:8088 \
  -e DB_HOST=mysql \
  -e ORDER_SERVICE_URL=http://order-service:8084 \
  -e PRODUCT_SERVICE_URL=http://product-service:8082 \
  -e USER_SERVICE_URL=http://user-service:8081 \
  analytics-service:latest
```

## Kubernetes Deployment

### 1. Deploy to Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 2. Verify Deployment
```bash
kubectl get pods -l app=analytics-service
kubectl get svc analytics-service
kubectl logs -f deployment/analytics-service
```

### 3. Scale Deployment
```bash
kubectl scale deployment analytics-service --replicas=3
```

## Testing

### Test Order Analytics
```bash
curl http://localhost:8088/api/analytics/orders/summary | jq
```

### Test Product Analytics
```bash
curl http://localhost:8088/api/analytics/products/summary | jq
```

### Test User Analytics
```bash
curl http://localhost:8088/api/analytics/users/summary | jq
```

### Test Dashboard
```bash
curl http://localhost:8088/api/analytics/dashboard | jq
```

### Test Top Products
```bash
curl http://localhost:8088/api/analytics/top-products | jq
```

### Test Low Stock
```bash
curl http://localhost:8088/api/analytics/low-stock | jq
```

## Data Flow

1. **Analytics Service** calls Feign clients to fetch data from:
   - Order Service: GET /api/orders
   - Product Service: GET /api/products
   - User Service: GET /api/users

2. **Aggregation**: Computes metrics from raw data

3. **Caching**: Stores computed metrics in MySQL `analytics_metrics` table

4. **Scheduled Refresh**: Updates metrics every 60 minutes

5. **API Response**: Returns cached or real-time computed analytics

## Health Checks

- Liveness: http://localhost:8088/actuator/health/liveness
- Readiness: http://localhost:8088/actuator/health/readiness
- Metrics: http://localhost:8088/actuator/metrics

## Configuration

### Cache TTL
Adjust analytics refresh interval in application.yml:
```yaml
analytics:
  cache:
    ttl-minutes: 60  # Refresh every 60 minutes
```

### Feign Timeouts
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

## Future Enhancements

- Real-time streaming analytics with Kafka
- Advanced reporting with date range filters
- Export analytics to CSV/PDF
- Predictive analytics using ML models
- Revenue forecasting
- Customer segmentation
- Product recommendation analytics
- Sales trends and patterns
- Geographic analytics
- Time-series analysis

## Troubleshooting

### Analytics Computation Failed
- Verify Order/Product/User services are running
- Check Feign client URLs in application.yml
- Review logs for specific service failures

### Stale Data
- Check scheduled task is running
- Verify `analytics.cache.ttl-minutes` configuration
- Manually trigger refresh by restarting service

### Database Connection Issues
- Verify MySQL is running
- Check DB_HOST and DB_PORT
- Verify database credentials
