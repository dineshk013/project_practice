# RevCart - Complete Startup Guide

## 🚀 Quick Start - Run Frontend & Backend

---

## Prerequisites

### Required Software
- ✅ **Java 17** (for backend services)
- ✅ **Maven 3.8+** (for building services)
- ✅ **MySQL 8.0** (for transactional data)
- ✅ **MongoDB 7.0** (for notifications, delivery, analytics)
- ✅ **Node.js 18+** (for Angular frontend)
- ✅ **npm** (comes with Node.js)

### Check Installations
```powershell
# Check Java
java -version

# Check Maven
mvn -version

# Check MySQL
mysql --version

# Check MongoDB
mongod --version

# Check Node.js
node --version

# Check npm
npm --version
```

---

## Step 1: Start Databases

### Start MySQL
```powershell
# If MySQL is installed as Windows Service
net start MySQL80

# Or start manually if installed standalone
# Navigate to MySQL bin folder and run:
mysqld --console
```

### Start MongoDB
```powershell
# If MongoDB is installed as Windows Service
net start MongoDB

# Or start manually
mongod --dbpath="C:\data\db"
```

### Verify Databases
```powershell
# Test MySQL connection
mysql -u root -p
# Enter password and type: SHOW DATABASES;

# Test MongoDB connection
mongosh
# Type: show dbs
```

---

## Step 2: Setup Databases

### Create MySQL Databases
```sql
-- Connect to MySQL
mysql -u root -p

-- Create databases
CREATE DATABASE IF NOT EXISTS revcart_users;
CREATE DATABASE IF NOT EXISTS revcart_products;
CREATE DATABASE IF NOT EXISTS revcart_cart;
CREATE DATABASE IF NOT EXISTS revcart_orders;
CREATE DATABASE IF NOT EXISTS revcart_payments;

-- Verify
SHOW DATABASES;
```

### Import Schemas (Optional - if tables don't exist)
```powershell
# Navigate to project root
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices

# Import user service schema
mysql -u root -p revcart_users < user-service\schema.sql

# Import product service schema
mysql -u root -p revcart_products < product-service\schema.sql

# Import cart service schema
mysql -u root -p revcart_cart < cart-service\schema.sql

# Import order service schema
mysql -u root -p revcart_orders < order-service\schema.sql

# Import payment service schema
mysql -u root -p revcart_payments < payment-service\schema.sql
```

### Create MongoDB Databases
```javascript
// Connect to MongoDB
mongosh

// Create databases (they'll be created automatically on first use)
use revcart_notifications
use revcart_delivery
use revcart_analytics

// Verify
show dbs
```

---

## Step 3: Start Backend Services (in order)

### Terminal 1: Gateway (Port 8080) - START FIRST
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\revcart-gateway
mvn spring-boot:run
```
**Wait for**: "Started RevcartGatewayApplication"

### Terminal 2: User Service (Port 8081)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\user-service
mvn spring-boot:run
```
**Wait for**: "Started UserServiceApplication"

### Terminal 3: Product Service (Port 8082)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\product-service
mvn spring-boot:run
```
**Wait for**: "Started ProductServiceApplication"

### Terminal 4: Cart Service (Port 8083)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\cart-service
mvn spring-boot:run
```
**Wait for**: "Started CartServiceApplication"

### Terminal 5: Order Service (Port 8084)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\order-service
mvn spring-boot:run
```
**Wait for**: "Started OrderServiceApplication"

### Terminal 6: Payment Service (Port 8085)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\payment-service
mvn spring-boot:run
```
**Wait for**: "Started PaymentServiceApplication"

### Terminal 7: Notification Service (Port 8086)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\notification-service
mvn spring-boot:run
```
**Wait for**: "Started NotificationServiceApplication"

### Terminal 8: Delivery Service (Port 8087)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\delivery-service
mvn spring-boot:run
```
**Wait for**: "Started DeliveryServiceApplication"

### Terminal 9: Analytics Service (Port 8088)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\analytics-service
mvn spring-boot:run
```
**Wait for**: "Started AnalyticsServiceApplication"

---

## Step 4: Verify Backend Services

### Health Check Script (PowerShell)
```powershell
# Save this as check-services.ps1
$services = @(
    @{Name="Gateway"; Port=8080},
    @{Name="User Service"; Port=8081},
    @{Name="Product Service"; Port=8082},
    @{Name="Cart Service"; Port=8083},
    @{Name="Order Service"; Port=8084},
    @{Name="Payment Service"; Port=8085},
    @{Name="Notification Service"; Port=8086},
    @{Name="Delivery Service"; Port=8087},
    @{Name="Analytics Service"; Port=8088}
)

Write-Host "`n=== RevCart Services Health Check ===" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Name) (Port $($service.Port)): " -NoNewline
            Write-Host "RUNNING" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ $($service.Name) (Port $($service.Port)): " -NoNewline
        Write-Host "NOT RUNNING" -ForegroundColor Red
    }
}

Write-Host ""
```

### Run Health Check
```powershell
# Run the script
.\check-services.ps1

# Or manually check each service
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
curl http://localhost:8087/actuator/health
curl http://localhost:8088/actuator/health
```

---

## Step 5: Start Frontend

### Terminal 10: Angular Frontend (Port 4200)
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\Frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start
```

**Wait for**: "Angular Live Development Server is listening on localhost:4200"

### Access Frontend
Open browser and navigate to:
```
http://localhost:4200
```

---

## Step 6: Test the Application

### Test Backend API
```powershell
# Test gateway
curl http://localhost:8080/actuator/health

# Test user registration
curl -X POST http://localhost:8080/api/users/register `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"test123\",\"phone\":\"1234567890\"}'

# Test product listing
curl http://localhost:8080/api/products
```

### Test Frontend
1. Open http://localhost:4200
2. Navigate through the pages
3. Try registration/login
4. Browse products
5. Add items to cart

---

## 🔧 Troubleshooting

### Service Won't Start

**Problem**: Port already in use
```
Error: Port 8081 is already in use
```

**Solution**: Kill the process using the port
```powershell
# Find process using port
netstat -ano | findstr :8081

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Database Connection Error

**Problem**: Cannot connect to MySQL
```
Error: Communications link failure
```

**Solution**: 
1. Check MySQL is running: `net start MySQL80`
2. Verify credentials in application.properties
3. Check port 3306 is not blocked

**Problem**: Cannot connect to MongoDB
```
Error: MongoTimeoutException
```

**Solution**:
1. Check MongoDB is running: `net start MongoDB`
2. Verify MongoDB is listening on port 27017
3. Check connection string in application.properties

### Frontend Build Error

**Problem**: npm install fails
```
Error: ENOENT: no such file or directory
```

**Solution**:
```powershell
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -r node_modules
rm package-lock.json

# Reinstall
npm install
```

### Gateway Returns 503

**Problem**: Gateway returns Service Unavailable
```
503 Service Unavailable
```

**Solution**:
1. Check all backend services are running
2. Verify service URLs in gateway application.yml
3. Check service health endpoints

---

## 📋 Service Startup Checklist

### Before Starting
- [ ] MySQL is running
- [ ] MongoDB is running
- [ ] All databases are created
- [ ] Java 17 is installed
- [ ] Maven is installed
- [ ] Node.js is installed

### Backend Services (Start in Order)
- [ ] Gateway (8080) - START FIRST
- [ ] User Service (8081)
- [ ] Product Service (8082)
- [ ] Cart Service (8083)
- [ ] Order Service (8084)
- [ ] Payment Service (8085)
- [ ] Notification Service (8086)
- [ ] Delivery Service (8087)
- [ ] Analytics Service (8088)

### Frontend
- [ ] npm install completed
- [ ] Frontend (4200) started

### Verification
- [ ] All health checks return 200 OK
- [ ] Frontend loads at http://localhost:4200
- [ ] Can register a new user
- [ ] Can view products
- [ ] Can add items to cart

---

## 🎯 Quick Commands Reference

### Start All Services (PowerShell Script)
Create `start-all.ps1`:
```powershell
# Start Gateway
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd revcart-gateway; mvn spring-boot:run"
Start-Sleep -Seconds 10

# Start User Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd user-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Product Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd product-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Cart Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd cart-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Order Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd order-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Payment Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd payment-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Notification Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd notification-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Delivery Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd delivery-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Analytics Service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd analytics-service; mvn spring-boot:run"
Start-Sleep -Seconds 5

# Start Frontend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Frontend; npm start"

Write-Host "All services are starting..." -ForegroundColor Green
Write-Host "Wait 2-3 minutes for all services to be ready" -ForegroundColor Yellow
Write-Host "Frontend will be available at: http://localhost:4200" -ForegroundColor Cyan
```

### Stop All Services
```powershell
# Kill all Java processes (backend services)
Get-Process -Name "java" | Stop-Process -Force

# Kill Node.js (frontend)
Get-Process -Name "node" | Stop-Process -Force
```

---

## 🌐 Access URLs

### Frontend
- **Application**: http://localhost:4200

### Backend Services
- **Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Cart Service**: http://localhost:8083
- **Order Service**: http://localhost:8084
- **Payment Service**: http://localhost:8085
- **Notification Service**: http://localhost:8086
- **Delivery Service**: http://localhost:8087
- **Analytics Service**: http://localhost:8088

### Health Endpoints
- **Gateway Health**: http://localhost:8080/actuator/health
- **User Service Health**: http://localhost:8081/actuator/health
- (Same pattern for all services)

### API Documentation
- See **API_TESTING_GUIDE.md** for complete API documentation

---

## 💡 Tips

1. **Start Gateway First**: Always start the gateway before other services
2. **Wait Between Services**: Give each service 5-10 seconds to start
3. **Check Logs**: Watch terminal output for errors
4. **Database First**: Ensure MySQL and MongoDB are running before starting services
5. **Frontend Last**: Start frontend after all backend services are up
6. **Use Health Checks**: Verify each service is healthy before testing

---

## 🎉 Success Indicators

You'll know everything is working when:
- ✅ All 9 backend services show "Started" in logs
- ✅ All health endpoints return `{"status":"UP"}`
- ✅ Frontend loads without errors
- ✅ You can register a user successfully
- ✅ You can view products
- ✅ You can add items to cart
- ✅ You can complete checkout

---

## 📞 Need Help?

If you encounter issues:
1. Check the **Troubleshooting** section above
2. Review service logs in terminal windows
3. Verify database connections
4. Ensure all ports are available
5. Check **API_TESTING_GUIDE.md** for API testing

**Happy Testing! 🚀**
