# Restart Services After Configuration Changes

## Problem
Gateway is returning 500 errors because it's running with old configuration. The new CORS and routing configuration needs to be loaded.

## Solution: Restart Gateway

### Step 1: Stop Gateway
Find the gateway process and stop it:
```powershell
# Find gateway process (PID 10580 in your case)
netstat -ano | findstr :8080

# Stop the process (replace PID with actual process ID)
taskkill /F /PID 10580
```

### Step 2: Start Gateway with New Configuration
```powershell
cd c:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\revcart-gateway
java -jar target\revcart-gateway-1.0.0.jar
```

### Step 3: Verify Gateway is Working
```powershell
# Test products endpoint through gateway
curl http://localhost:8080/api/products?page=0^&size=10

# Should return JSON with products, not 500 error
```

## Services That Need Restart

After rebuilding any service, you MUST restart it:

| Service | Port | Restart Command |
|---------|------|-----------------|
| Gateway | 8080 | `cd revcart-gateway && java -jar target\revcart-gateway-1.0.0.jar` |
| User Service | 8081 | `cd user-service && java -jar target\user-service-1.0.0.jar` |
| Product Service | 8082 | `cd product-service && java -jar target\product-service-1.0.0.jar` |
| Cart Service | 8083 | `cd cart-service && java -jar target\cart-service-1.0.0.jar` |
| Order Service | 8084 | `cd order-service && java -jar target\order-service-1.0.0.jar` |
| Payment Service | 8085 | `cd payment-service && java -jar target\payment-service-1.0.0.jar` |
| Notification Service | 8086 | `cd notification-service && java -jar target\notification-service-1.0.0.jar` |
| Delivery Service | 8087 | `cd delivery-service && java -jar target\delivery-service-1.0.0.jar` |
| Analytics Service | 8088 | `cd analytics-service && java -jar target\analytics-service-1.0.0.jar` |

## Quick Test After Restart

```powershell
# Test gateway routing
curl http://localhost:8080/api/products
curl http://localhost:8080/api/categories

# Both should return JSON data, not 500 errors
```

## Frontend Configuration

Frontend is already correctly configured:
- ✅ `environment.ts` → `apiUrl: 'http://localhost:8080/api'`
- ✅ All services use `${environment.apiUrl}`
- ✅ WebSocket uses gateway: `http://localhost:8080/ws`

No frontend changes needed!
