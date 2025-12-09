# RevCart Monolith to Microservices Migration Guide

## Migration Overview

This guide provides step-by-step instructions for migrating RevCart from a monolithic architecture to microservices.

---

## Phase 1: Preparation (Week 1-2)

### 1.1 Setup AWS Infrastructure
```bash
cd terraform/
terraform init
terraform plan -var-file="prod.tfvars"
terraform apply -var-file="prod.tfvars"
```

### 1.2 Create Service Repositories
```bash
# Create directory structure
mkdir -p services/{user,product,cart,order,payment,notification,delivery,analytics}-service

# Initialize each service
for service in user product cart order payment notification delivery analytics; do
  cd services/${service}-service
  mvn archetype:generate \
    -DgroupId=com.revcart \
    -DartifactId=${service}-service \
    -DarchetypeArtifactId=maven-archetype-quickstart
  cd ../..
done
```

### 1.3 Setup Shared Libraries
```bash
# Create shared module for common DTOs, exceptions, utilities
mkdir -p shared/revcart-common
cd shared/revcart-common
# Add common classes: ApiResponse, exceptions, base entities
```

---

## Phase 2: Extract User Service (Week 3)

### 2.1 Create User Service Structure
```
user-service/
├── src/main/java/com/revcart/user/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── ProfileController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── UserService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── AddressRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   └── Address.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java
│   └── UserServiceApplication.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

### 2.2 Extract Code from Monolith
```bash
# Copy relevant files
cp Backend/src/main/java/com/revcart/controller/AuthController.java \
   services/user-service/src/main/java/com/revcart/user/controller/

cp Backend/src/main/java/com/revcart/service/impl/AuthServiceImpl.java \
   services/user-service/src/main/java/com/revcart/user/service/

# Copy entities
cp Backend/src/main/java/com/revcart/entity/User.java \
   services/user-service/src/main/java/com/revcart/user/entity/
```

### 2.3 Update Dependencies (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>com.revcart</groupId>
        <artifactId>revcart-common</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 2.4 Configure Database Connection
```yaml
# application.yml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/revcart_users
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    
server:
  port: 8081
```

### 2.5 Build and Deploy
```bash
cd services/user-service
mvn clean package
docker build -t revcart-user-service:v1.0 .
docker tag revcart-user-service:v1.0 ${ECR_REPO}/revcart-user-service:v1.0
docker push ${ECR_REPO}/revcart-user-service:v1.0

kubectl apply -f k8s/deployments/user-service-deployment.yaml
```

---

## Phase 3: Extract Product Service (Week 4)

### 3.1 Extract Components
- ProductController, CategoryController
- ProductService, CategoryService
- Product, Category, Inventory entities
- ProductRepository, CategoryRepository

### 3.2 Database Migration
```sql
-- Create separate database
CREATE DATABASE revcart_products;

-- Export data from monolith
mysqldump revcart products categories inventory > products_data.sql

-- Import to new database
mysql revcart_products < products_data.sql
```

### 3.3 Deploy Product Service
```bash
cd services/product-service
mvn clean package
docker build -t revcart-product-service:v1.0 .
docker push ${ECR_REPO}/revcart-product-service:v1.0
kubectl apply -f k8s/deployments/product-service-deployment.yaml
```

---

## Phase 4: Extract Cart Service (Week 5)

### 4.1 Setup Redis Integration
```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: 6379
  cache:
    type: redis
```

### 4.2 Implement Feign Clients
```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable Long id);
}

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PostMapping("/api/users/validate-token")
    TokenValidationResponse validateToken(@RequestBody String token);
}
```

### 4.3 Deploy Cart Service
```bash
cd services/cart-service
mvn clean package
docker build -t revcart-cart-service:v1.0 .
docker push ${ECR_REPO}/revcart-cart-service:v1.0
kubectl apply -f k8s/deployments/cart-service-deployment.yaml
```

---

## Phase 5: Extract Payment Service (Week 6)

### 5.1 Extract Payment Logic
- PaymentController, RazorpayDummyController
- PaymentService, DummyPaymentService
- Payment, PaymentHistory entities

### 5.2 Configure Payment Gateways
```yaml
razorpay:
  key-id: ${RAZORPAY_KEY_ID}
  key-secret: ${RAZORPAY_KEY_SECRET}

stripe:
  api-key: ${STRIPE_API_KEY}
```

### 5.3 Implement Event Publishing
```java
@Service
public class PaymentEventPublisher {
    @Autowired
    private AmazonSNS snsClient;
    
    public void publishPaymentCompleted(PaymentDto payment) {
        PublishRequest request = new PublishRequest()
            .withTopicArn(paymentTopicArn)
            .withMessage(objectMapper.writeValueAsString(payment));
        snsClient.publish(request);
    }
}
```

---

## Phase 6: Extract Order Service (Week 7-8)

### 6.1 Implement Saga Pattern
```java
@Service
public class OrderOrchestrationService {
    
    @Autowired
    private CartServiceClient cartClient;
    @Autowired
    private ProductServiceClient productClient;
    @Autowired
    private PaymentServiceClient paymentClient;
    @Autowired
    private DeliveryServiceClient deliveryClient;
    
    @Transactional
    public OrderDto createOrder(CheckoutRequest request) {
        // 1. Validate cart
        CartDto cart = cartClient.getCart(token);
        
        // 2. Reserve inventory
        productClient.reserveStock(cart.getItems());
        
        // 3. Create order
        Order order = orderRepository.save(newOrder);
        
        // 4. Initiate payment
        PaymentDto payment = paymentClient.initiatePayment(order);
        
        // 5. Publish event
        publishOrderPlaced(order);
        
        return orderMapper.toDto(order);
    }
}
```

### 6.2 Implement Compensation Logic
```java
@Service
public class OrderCompensationService {
    
    public void compensateFailedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        
        // Release inventory
        productClient.releaseStock(order.getItems());
        
        // Cancel payment
        paymentClient.refund(order.getPaymentId());
        
        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

---

## Phase 7: Extract Notification Service (Week 9)

### 7.1 Setup MongoDB Connection
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_HOST}:27017/revcart_notifications
```

### 7.2 Implement Event Consumers
```java
@Service
public class NotificationEventConsumer {
    
    @SqsListener("${sqs.notification.queue}")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Send order confirmation email
        mailService.sendOrderConfirmation(event.getUserId(), event.getOrderId());
        
        // Save notification
        notificationRepository.save(notification);
    }
    
    @SqsListener("${sqs.notification.queue}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Send payment receipt
        mailService.sendPaymentReceipt(event.getUserId(), event.getPaymentId());
    }
}
```

### 7.3 Setup WebSocket
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications").withSockJS();
    }
}
```

---

## Phase 8: Extract Delivery & Analytics Services (Week 10)

### 8.1 Delivery Service
- Track delivery status
- Location updates
- Delivery agent management

### 8.2 Analytics Service
- Consume all events
- Aggregate metrics
- Generate reports

---

## Phase 9: API Gateway Setup (Week 11)

### 9.1 Deploy Ingress Controller
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/aws/deploy.yaml
```

### 9.2 Configure Ingress Rules
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: revcart-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.revcart.com
    http:
      paths:
      - path: /api/users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
```

---

## Phase 10: Cutover & Decommission (Week 12)

### 10.1 Traffic Migration
```bash
# Route 10% traffic to microservices
kubectl set image deployment/user-service user-service=revcart-user-service:v1.0

# Monitor metrics
kubectl top pods -n revcart

# Gradually increase traffic: 25% → 50% → 75% → 100%
```

### 10.2 Data Synchronization
```bash
# Run dual-write mode for 1 week
# Verify data consistency
# Stop monolith writes
```

### 10.3 Decommission Monolith
```bash
# Stop monolith application
kubectl delete deployment revcart-monolith

# Archive database
mysqldump revcart > revcart_monolith_archive.sql

# Keep backup for 30 days
```

---

## Rollback Plan

### If Issues Occur
1. **Immediate**: Route traffic back to monolith
2. **Database**: Restore from backup
3. **Services**: Rollback to previous version
```bash
kubectl rollout undo deployment/user-service
```

---

## Testing Checklist

### Unit Tests
- [ ] All services have 80%+ code coverage
- [ ] Mock external dependencies

### Integration Tests
- [ ] Feign client communication
- [ ] Database operations
- [ ] Event publishing/consuming

### End-to-End Tests
- [ ] User registration → Login
- [ ] Browse products → Add to cart
- [ ] Checkout → Payment → Order confirmation
- [ ] Delivery tracking

### Performance Tests
- [ ] Load test with 1000 concurrent users
- [ ] API response time < 500ms (p95)
- [ ] Database query time < 100ms

### Security Tests
- [ ] JWT token validation
- [ ] SQL injection prevention
- [ ] XSS protection
- [ ] CORS configuration

---

## Monitoring Checklist

### CloudWatch Dashboards
- [ ] Service health metrics
- [ ] API latency (p50, p95, p99)
- [ ] Error rates
- [ ] Database connections
- [ ] Cache hit rates

### Alerts
- [ ] High CPU usage (>80%)
- [ ] High memory usage (>85%)
- [ ] High error rate (>5%)
- [ ] Payment failures
- [ ] Database connection pool exhaustion

---

## Post-Migration Tasks

### Week 13-14: Optimization
- Fine-tune resource limits
- Optimize database queries
- Implement caching strategies
- Review and optimize costs

### Week 15-16: Documentation
- Update API documentation
- Create runbooks for operations
- Document troubleshooting procedures
- Train team on new architecture

---

## Success Metrics

| Metric | Before (Monolith) | Target (Microservices) |
|--------|-------------------|------------------------|
| Deployment Time | 30 minutes | 5 minutes per service |
| API Response Time (p95) | 800ms | <500ms |
| Availability | 99.5% | 99.9% |
| Time to Market (new features) | 2 weeks | 3-5 days |
| Scalability | Vertical only | Horizontal per service |

---

## Common Issues & Solutions

### Issue 1: Distributed Transactions
**Problem**: Order creation involves multiple services
**Solution**: Implement Saga pattern with compensation logic

### Issue 2: Data Consistency
**Problem**: Eventual consistency across services
**Solution**: Use event sourcing and CQRS where needed

### Issue 3: Service Discovery
**Problem**: Services need to find each other
**Solution**: Use Kubernetes DNS (service-name.namespace.svc.cluster.local)

### Issue 4: Debugging Distributed Systems
**Problem**: Tracing requests across services
**Solution**: Implement distributed tracing with AWS X-Ray

### Issue 5: Configuration Management
**Problem**: Managing configs across services
**Solution**: Use Kubernetes ConfigMaps and Secrets

---

## Conclusion

This migration transforms RevCart from a monolithic application to a scalable, resilient microservices architecture. The phased approach minimizes risk and allows for continuous validation at each step.

**Estimated Timeline**: 12-16 weeks
**Team Size**: 4-6 developers
**Risk Level**: Medium (with proper testing and rollback plans)
