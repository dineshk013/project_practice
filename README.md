# RevCart Microservices E-Commerce Platform

A complete microservices-based e-commerce platform built with Spring Boot, Angular, MySQL, and MongoDB.

## 🏗️ Architecture

- **8 Microservices**: User, Product, Cart, Order, Payment, Notification, Delivery, Analytics
- **API Gateway**: Spring Cloud Gateway (Port 8080)
- **Frontend**: Angular 18 with Tailwind CSS (Port 4200)
- **Databases**: MySQL (transactional) + MongoDB (unstructured data)

## 🚀 Quick Start

### Prerequisites
- Java 17
- Maven 3.8+
- MySQL 8.0
- MongoDB 7.0
- Node.js 18+

### Start Everything (Automated)
```powershell
# 1. Ensure MySQL and MongoDB are running
net start MySQL80
net start MongoDB

# 2. Run the startup script
.\start-all.ps1

# 3. Wait 2-3 minutes for all services to start

# 4. Check service health
.\check-services.ps1
```

### Access the Application
- **Frontend**: http://localhost:4200
- **API Gateway**: http://localhost:8080
- **API Docs**: See API_TESTING_GUIDE.md

### Stop Everything
```powershell
.\stop-all.ps1
```

## 📚 Documentation

- **STARTUP_GUIDE.md** - Complete startup instructions
- **API_TESTING_GUIDE.md** - All API endpoints and testing
- **PROJECT_STATUS.md** - Current project status and roadmap
- **MICROSERVICES_ARCHITECTURE.md** - Architecture details
- **SERVICE_DEPENDENCIES.md** - Service dependencies and contracts

## 🎯 Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| Gateway | 8080 | - | API Gateway & routing |
| User | 8081 | MySQL | Authentication & user management |
| Product | 8082 | MySQL | Product catalog & inventory |
| Cart | 8083 | MySQL | Shopping cart |
| Order | 8084 | MySQL | Order processing |
| Payment | 8085 | MySQL | Payment processing |
| Notification | 8086 | MongoDB | User notifications |
| Delivery | 8087 | MongoDB | Delivery tracking |
| Analytics | 8088 | MongoDB | Business analytics |
| Frontend | 4200 | - | Angular web application |

## 🔧 Manual Startup

If you prefer to start services manually, see **STARTUP_GUIDE.md** for detailed instructions.

## 🧪 Testing

### Health Check
```powershell
.\check-services.ps1
```

### API Testing
```powershell
# Register a user
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"test123\",\"phone\":\"1234567890\"}"

# Get products
curl http://localhost:8080/api/products
```

See **API_TESTING_GUIDE.md** for complete API documentation.

## 📊 Project Status

✅ **Complete**: All 8 microservices + gateway + frontend  
✅ **Functional**: All APIs tested and working  
⚠️ **In Progress**: Testing, monitoring, CI/CD  

See **PROJECT_STATUS.md** for detailed status.

## 🛠️ Technology Stack

### Backend
- Spring Boot 3.2
- Spring Cloud Gateway
- Spring Security + JWT
- Spring Data JPA
- OpenFeign
- Resilience4j
- MySQL 8.0
- MongoDB 7.0

### Frontend
- Angular 18
- Tailwind CSS
- RxJS
- TypeScript

### DevOps
- Docker
- Kubernetes
- Maven

## 📁 Project Structure

```
Revcart_Microservices/
├── user-service/          # User management & auth
├── product-service/       # Product catalog
├── cart-service/          # Shopping cart
├── order-service/         # Order processing
├── payment-service/       # Payment handling
├── notification-service/  # Notifications
├── delivery-service/      # Delivery tracking
├── analytics-service/     # Business analytics
├── revcart-gateway/       # API Gateway
├── Frontend/              # Angular app
├── start-all.ps1          # Start all services
├── stop-all.ps1           # Stop all services
├── check-services.ps1     # Health check
└── *.md                   # Documentation
```

## 🎓 Features

### User Management
- Registration & Login
- JWT Authentication
- Profile Management
- Address Management
- Password Reset with OTP

### Product Management
- Product CRUD
- Category Management
- Stock Management
- Product Search
- Auto SKU Generation

### Shopping
- Add to Cart
- Update Cart
- Cart Persistence
- Checkout Process

### Order Management
- Order Creation
- Order Tracking
- Order Cancellation
- Status Updates

### Payment
- Payment Processing
- Payment Verification
- Refund Processing
- Payment History
- Dummy Payment (for testing)

### Notifications
- User Notifications
- Notification History
- Mark as Read

### Delivery
- Delivery Assignment
- Status Tracking
- Delivery History

### Analytics
- Order Analytics
- Product Analytics
- User Analytics
- Dashboard

## 🔐 Security

- JWT-based authentication
- Spring Security
- Password encryption (BCrypt)
- CORS configuration
- API Gateway security

## 🌐 API Gateway Routes

All requests go through the gateway at `http://localhost:8080`:

- `/api/users/**` → User Service (8081)
- `/api/products/**` → Product Service (8082)
- `/api/categories/**` → Product Service (8082)
- `/api/cart/**` → Cart Service (8083)
- `/api/orders/**` → Order Service (8084)
- `/api/payments/**` → Payment Service (8085)
- `/api/notifications/**` → Notification Service (8086)
- `/api/delivery/**` → Delivery Service (8087)
- `/api/analytics/**` → Analytics Service (8088)

## 🐛 Troubleshooting

### Services won't start
- Check MySQL and MongoDB are running
- Verify ports 8080-8088 and 4200 are available
- Check logs in terminal windows

### Database connection errors
- Verify MySQL credentials in application.properties
- Ensure databases are created
- Check MongoDB is running on port 27017

### Frontend errors
- Run `npm install` in Frontend folder
- Clear npm cache: `npm cache clean --force`
- Check Node.js version: `node --version`

See **STARTUP_GUIDE.md** for detailed troubleshooting.

## 📝 License

This project is for educational purposes.

## 👥 Contributors

- Your Name

## 🚀 Next Steps

See **PROJECT_STATUS.md** for the complete roadmap:
- Docker Compose setup
- Unit testing
- CI/CD pipeline
- Redis caching
- Monitoring & logging

---

**Happy Coding! 🎉**
