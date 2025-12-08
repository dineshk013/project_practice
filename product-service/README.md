# Product Service

RevCart Product Microservice - Manages product catalog, categories, and inventory.

## Features

- Product CRUD operations
- Category management
- Inventory tracking
- Stock reservation/release for orders
- Product search
- RESTful API endpoints

## Technology Stack

- Java 17
- Spring Boot 3.2.0
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
java -jar target/product-service-1.0.0.jar

# Or using Maven
mvn spring-boot:run
```

### Using Docker

```bash
# Build image
docker build -t product-service:1.0.0 .

# Run container
docker run -p 8082:8082 \
  -e DB_HOST=localhost \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  product-service:1.0.0
```

## Configuration

Environment variables:

- `DB_HOST` - MySQL host (default: localhost)
- `DB_PORT` - MySQL port (default: 3306)
- `DB_USERNAME` - Database username (default: root)
- `DB_PASSWORD` - Database password (default: root)
- `PORT` - Service port (default: 8082)

## API Endpoints

### Products

```bash
# Get all products
curl http://localhost:8082/api/products

# Get product by ID
curl http://localhost:8082/api/products/1

# Search products
curl http://localhost:8082/api/products/search?keyword=tomato

# Get products by category
curl http://localhost:8082/api/products/category/vegetables

# Create product
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fresh Carrots",
    "description": "Organic carrots",
    "price": 199.2,
    "sku": "SKU-100",
    "active": true,
    "stockQuantity": 100,
    "category": {"id": 1}
  }'

# Update product
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fresh Tomatoes Updated",
    "price": 249.2,
    "sku": "SKU-1",
    "active": true,
    "stockQuantity": 120,
    "category": {"id": 1}
  }'

# Delete product
curl -X DELETE http://localhost:8082/api/products/1

# Check stock
curl http://localhost:8082/api/products/1/stock

# Reserve stock (Internal - Feign)
curl -X PUT http://localhost:8082/api/products/stock/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "ORDER-123",
    "items": [
      {"productId": 1, "quantity": 5},
      {"productId": 2, "quantity": 3}
    ]
  }'

# Release stock (Internal - Feign)
curl -X PUT http://localhost:8082/api/products/stock/release \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "ORDER-123",
    "items": [
      {"productId": 1, "quantity": 5}
    ]
  }'
```

### Categories

```bash
# Get all categories
curl http://localhost:8082/api/categories

# Get category by ID
curl http://localhost:8082/api/categories/1

# Create category
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Snacks",
    "slug": "snacks",
    "description": "Snacks and chips",
    "imageUrl": "https://example.com/snacks.jpg"
  }'

# Update category
curl -X PUT http://localhost:8082/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vegetables Updated",
    "slug": "vegetables",
    "description": "Fresh organic vegetables"
  }'
```

### Health Check

```bash
curl http://localhost:8082/actuator/health
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

# Deploy
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify
kubectl get pods -n revcart
kubectl get svc -n revcart

# Check logs
kubectl logs -f deployment/product-service -n revcart
```

## Testing

Import `postman_collection.json` into Postman for comprehensive API testing.

## Monitoring

- Health endpoint: `/actuator/health`
- Metrics endpoint: `/actuator/metrics`
- Prometheus endpoint: `/actuator/prometheus`

## Architecture

```
product-service/
├── src/main/java/com/revcart/productservice/
│   ├── controller/          # REST controllers
│   ├── service/             # Business logic
│   ├── repository/          # Data access
│   ├── entity/              # JPA entities
│   ├── dto/                 # Data transfer objects
│   ├── exception/           # Custom exceptions
│   └── config/              # Configuration classes
├── src/main/resources/
│   └── application.yml      # Application configuration
├── k8s/                     # Kubernetes manifests
├── Dockerfile               # Container image
├── pom.xml                  # Maven dependencies
└── schema.sql               # Database schema
```

## Port

Service runs on port **8082**

## License

RevCart Project - Internal Use
