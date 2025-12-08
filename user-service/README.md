# User Service

RevCart User Microservice - Manages user authentication, profiles, and addresses with JWT security.

## Features

- User registration and login
- JWT-based authentication
- Profile management (CRUD)
- Address management (CRUD)
- OTP generation and verification
- Password reset functionality
- Token validation for inter-service communication

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- MySQL 8.0
- Spring Cloud OpenFeign
- Maven
- Docker
- Kubernetes

## Prerequisites

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Docker (optional)
- Kubernetes cluster (for deployment)

## Database Setup

```bash
mysql -u root -p < schema.sql
```

## Build & Run

### Local Development

```bash
# Build
mvn clean package

# Run
java -jar target/user-service-1.0.0.jar

# Or using Maven
mvn spring-boot:run
```

### Using Docker

```bash
# Build image
docker build -t user-service:1.0.0 .

# Run container
docker run -p 8081:8081 \
  -e DB_HOST=localhost \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e JWT_SECRET=YourSecretKey \
  user-service:1.0.0
```

## Configuration

Environment variables:

- `DB_HOST` - MySQL host (default: localhost)
- `DB_PORT` - MySQL port (default: 3306)
- `DB_USERNAME` - Database username (default: root)
- `DB_PASSWORD` - Database password (default: root)
- `JWT_SECRET` - JWT signing secret (required)
- `JWT_EXPIRATION` - Token expiration in ms (default: 86400000 = 24h)
- `PORT` - Service port (default: 8081)

## API Endpoints

### Authentication

```bash
# Register
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe",
    "phone": "1234567890"
  }'

# Login
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Verify OTP
curl -X POST "http://localhost:8081/api/users/verify-otp?email=user@example.com&otp=123456"

# Reset Password
curl -X POST "http://localhost:8081/api/users/reset-password?email=user@example.com&newPassword=newpass123"

# Validate Token (Internal - Feign)
curl -X POST http://localhost:8081/api/users/validate-token \
  -H "Content-Type: application/json" \
  -d '"your-jwt-token"'
```

### Profile Management

```bash
# Get Profile
curl http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Update Profile
curl -X PUT http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "phone": "9876543210"
  }'

# Get User By ID (Internal - Feign)
curl http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Address Management

```bash
# Get All Addresses
curl http://localhost:8081/api/users/addresses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Add Address
curl -X POST http://localhost:8081/api/users/addresses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "street": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "zipCode": "400001",
    "country": "India",
    "isDefault": true
  }'

# Update Address
curl -X PUT http://localhost:8081/api/users/addresses/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "street": "456 Updated Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "zipCode": "400002",
    "country": "India",
    "isDefault": true
  }'

# Delete Address
curl -X DELETE http://localhost:8081/api/users/addresses/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

## JWT Authentication

After login/register, you'll receive a JWT token in the response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe",
      "phone": "1234567890",
      "role": "USER"
    }
  }
}
```

Use this token in the `Authorization` header for protected endpoints:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## Kubernetes Deployment

```bash
# Create namespace
kubectl create namespace revcart

# Create secrets
kubectl create secret generic db-secrets \
  --from-literal=username=admin \
  --from-literal=password=<password> \
  -n revcart

kubectl create secret generic jwt-secrets \
  --from-literal=secret=<jwt-secret> \
  -n revcart

# Deploy
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify
kubectl get pods -n revcart
kubectl get svc -n revcart

# Check logs
kubectl logs -f deployment/user-service -n revcart
```

## Testing

Import `postman_collection.json` into Postman for comprehensive API testing.

## Security

- Passwords are encrypted using BCrypt
- JWT tokens for stateless authentication
- Token expiration: 24 hours (configurable)
- Protected endpoints require valid JWT token
- CORS enabled for frontend integration

## Monitoring

- Health endpoint: `/actuator/health`
- Metrics endpoint: `/actuator/metrics`
- Prometheus endpoint: `/actuator/prometheus`

## Architecture

```
user-service/
├── src/main/java/com/revcart/userservice/
│   ├── controller/          # REST controllers
│   ├── service/             # Business logic
│   ├── repository/          # Data access
│   ├── entity/              # JPA entities
│   ├── dto/                 # Data transfer objects
│   ├── security/            # JWT & authentication
│   ├── config/              # Security configuration
│   └── exception/           # Custom exceptions
├── src/main/resources/
│   └── application.yml      # Application configuration
├── k8s/                     # Kubernetes manifests
├── Dockerfile               # Container image
├── pom.xml                  # Maven dependencies
└── schema.sql               # Database schema
```

## Port

Service runs on port **8081**

## License

RevCart Project - Internal Use
