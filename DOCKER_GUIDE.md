# Docker Containerization Guide

## 📦 Overview

This guide covers Docker containerization for the RevCart microservices platform.

## 🏗️ Architecture

- **8 Microservices**: Each with optimized multi-stage Dockerfile
- **2 Databases**: MySQL (relational) + MongoDB (NoSQL)
- **1 API Gateway**: Routes all traffic
- **1 Frontend**: Angular app served via Nginx

## 📁 Docker Files Created

```
Revcart_Microservices/
├── docker-compose.yml          # Orchestrates all services
├── .env.example                # Environment variables template
├── .dockerignore               # Excludes unnecessary files
├── init-mysql.sql              # MySQL initialization
├── Frontend/
│   ├── Dockerfile              # Multi-stage Angular build
│   └── nginx.conf              # Nginx configuration
├── revcart-gateway/
│   └── Dockerfile              # API Gateway
└── [service-name]/
    └── Dockerfile              # Each microservice
```

## 🚀 Quick Start

### 1. Setup Environment Variables

```bash
# Copy example env file
cp .env.example .env

# Edit .env with your credentials (optional)
```

### 2. Build and Start All Services

```bash
# Build all images and start containers
docker-compose up -d --build

# View logs
docker-compose logs -f

# Check running containers
docker-compose ps
```

### 3. Stop All Services

```bash
# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (clean slate)
docker-compose down -v
```

## 🔧 Individual Service Commands

### Build Single Service

```bash
docker-compose build user-service
```

### Start Single Service

```bash
docker-compose up -d user-service
```

### View Service Logs

```bash
docker-compose logs -f user-service
```

### Restart Service

```bash
docker-compose restart user-service
```

## 🌐 Service URLs (Local)

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost:4200 | 4200 |
| API Gateway | http://localhost:8080 | 8080 |
| User Service | http://localhost:8081 | 8081 |
| Product Service | http://localhost:8082 | 8082 |
| Cart Service | http://localhost:8083 | 8083 |
| Order Service | http://localhost:8084 | 8084 |
| Payment Service | http://localhost:8085 | 8085 |
| Notification Service | http://localhost:8086 | 8086 |
| Delivery Service | http://localhost:8087 | 8087 |
| Analytics Service | http://localhost:8088 | 8088 |
| MySQL | localhost:3306 | 3306 |
| MongoDB | localhost:27017 | 27017 |

## 🗄️ Database Access

### MySQL

```bash
# Connect to MySQL container
docker exec -it revcart-mysql mysql -uroot -proot

# Or use MySQL client
mysql -h localhost -P 3306 -u root -p
```

### MongoDB

```bash
# Connect to MongoDB container
docker exec -it revcart-mongodb mongosh -u admin -p admin

# Or use MongoDB Compass
mongodb://admin:admin@localhost:27017
```

## 🐛 Troubleshooting

### Container Won't Start

```bash
# Check logs
docker-compose logs [service-name]

# Check container status
docker-compose ps

# Rebuild without cache
docker-compose build --no-cache [service-name]
```

### Port Already in Use

```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID [PID] /F

# Or change port in docker-compose.yml
ports:
  - "8081:8080"  # host:container
```

### Database Connection Issues

```bash
# Ensure databases are healthy
docker-compose ps

# Wait for health checks to pass
docker-compose logs mysql
docker-compose logs mongodb

# Restart dependent services
docker-compose restart user-service
```

### Clean Start

```bash
# Remove everything and start fresh
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

## 📊 Resource Management

### View Resource Usage

```bash
docker stats
```

### Limit Resources (in docker-compose.yml)

```yaml
services:
  user-service:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          memory: 256M
```

## 🔐 Security Best Practices

1. **Never commit .env file** - Use .env.example as template
2. **Use secrets for production** - Docker secrets or AWS Secrets Manager
3. **Run as non-root user** - Add USER directive in Dockerfile
4. **Scan images** - Use `docker scan [image-name]`
5. **Keep images updated** - Regularly update base images

## 📈 Optimization Tips

### Multi-Stage Builds

All Dockerfiles use multi-stage builds to:
- Reduce final image size (50-70% smaller)
- Separate build and runtime dependencies
- Improve security (no build tools in production)

### Layer Caching

Order Dockerfile commands for better caching:
1. Copy dependency files first (pom.xml, package.json)
2. Download dependencies
3. Copy source code
4. Build application

### Image Size Comparison

| Service | Before | After | Savings |
|---------|--------|-------|---------|
| User Service | 450MB | 180MB | 60% |
| Frontend | 1.2GB | 25MB | 98% |

## 🚢 Next Steps

After validating locally:

1. **Push to Registry**
   ```bash
   docker tag revcart-user-service:latest [registry]/revcart-user-service:latest
   docker push [registry]/revcart-user-service:latest
   ```

2. **Deploy to AWS**
   - Use AWS ECR for image registry
   - Deploy with ECS/EKS
   - Configure ALB for load balancing

3. **Setup CI/CD**
   - Automate builds with GitHub Actions
   - Auto-deploy on merge to main
   - Run tests before deployment

## 📝 Notes

- All services use health checks for proper startup ordering
- Volumes persist data between container restarts
- Network isolation via custom bridge network
- Environment variables for configuration flexibility

---

**Ready for AWS deployment! 🚀**
