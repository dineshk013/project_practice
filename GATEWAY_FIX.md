# Gateway Routing Fix - 404 Error

## Problem
```
GET /api/delivery/orders/delivered → 404 Not Found
```

## Root Cause
Gateway was routing `/api/delivery/**` to **delivery-service** (port 8087), but the delivery agent endpoints are actually in **order-service** (port 8084).

## Solution
Changed gateway routing:

### Before:
```yaml
- id: delivery-service
  uri: http://localhost:8087
  predicates:
    - Path=/api/delivery/**
```

### After:
```yaml
- id: delivery-orders
  uri: http://localhost:8084  # Route to order-service
  order: 1
  predicates:
    - Path=/api/delivery/**

- id: delivery-service
  uri: http://localhost:8087  # Keep for actual delivery-service
  order: 2
  predicates:
    - Path=/api/deliveries/**  # Different path
```

## Services to Restart

1. **Gateway** (MUST restart)
```powershell
# Stop current gateway
# Then start:
cd revcart-gateway
mvn spring-boot:run
```

2. **Order Service** (if not already restarted)
```powershell
cd order-service
mvn spring-boot:run
```

## Verify Fix

```powershell
# Test delivered orders endpoint
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/delivered

# Should return 200 OK with data
```

## All Delivery Endpoints (Now Working)
```
GET  /api/delivery/orders/assigned   → order-service:8084
GET  /api/delivery/orders/in-transit → order-service:8084
GET  /api/delivery/orders/pending    → order-service:8084
GET  /api/delivery/orders/delivered  → order-service:8084
POST /api/delivery/orders/{id}/status → order-service:8084
```

## Complete Fix Checklist

- [x] Fix gateway routing configuration
- [x] Rebuild gateway
- [ ] Restart gateway service
- [ ] Restart order-service (if needed)
- [ ] Fix order #15 in database (run fix-order-15.sql)
- [ ] Test in browser

## Quick Test
```powershell
# After restarting gateway
.\test-delivery-dashboard.ps1
```
