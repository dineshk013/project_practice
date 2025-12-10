# Simple Manual Deployment Guide

## 🎯 Complete Flow

```
Git Push → Jenkins Build & Test → YOU Push to ECR → YOU Deploy on EC2
```

## 📋 What Jenkins Does (Automatic)

1. ✅ Pulls code from Git
2. ✅ Builds all 9 microservices + frontend
3. ✅ Runs tests
4. ✅ Creates Docker images locally
5. ✅ Stops (doesn't push anywhere)

## 🚀 What YOU Do (Manual)

### Step 1: After Jenkins Completes, Push Images to ECR

```bash
# On Jenkins server or your local machine (where images were built)

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Tag and push all images
docker tag revcart_microservices-gateway:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-gateway:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-gateway:latest

docker tag revcart_microservices-user-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest

docker tag revcart_microservices-product-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-product-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-product-service:latest

docker tag revcart_microservices-cart-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-cart-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-cart-service:latest

docker tag revcart_microservices-order-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-order-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-order-service:latest

docker tag revcart_microservices-payment-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-payment-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-payment-service:latest

docker tag revcart_microservices-notification-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-notification-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-notification-service:latest

docker tag revcart_microservices-delivery-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-delivery-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-delivery-service:latest

docker tag revcart_microservices-analytics-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-analytics-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-analytics-service:latest

docker tag revcart_microservices-frontend:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-frontend:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-frontend:latest
```

**Or use this script:**

```bash
# Create push-to-ecr.sh
cat > push-to-ecr.sh << 'EOF'
#!/bin/bash
AWS_ACCOUNT_ID="<YOUR_AWS_ACCOUNT_ID>"
AWS_REGION="us-east-1"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

# Tag and push all services
for service in gateway user-service product-service cart-service order-service payment-service notification-service delivery-service analytics-service frontend; do
  echo "Pushing ${service}..."
  docker tag revcart_microservices-${service}:latest ${ECR_REGISTRY}/revcart-${service}:latest
  docker push ${ECR_REGISTRY}/revcart-${service}:latest
done

echo "✅ All images pushed to ECR!"
EOF

chmod +x push-to-ecr.sh
./push-to-ecr.sh
```

### Step 2: Deploy on EC2

```bash
# SSH to EC2
ssh -i revcart-key.pem ec2-user@<EC2_PUBLIC_IP>

# Navigate to app directory
cd ~/revcart

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Pull latest images
docker-compose pull

# Restart services
docker-compose up -d --force-recreate

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

**Or use deployment script on EC2:**

```bash
# Create deploy.sh on EC2
cat > ~/revcart/deploy.sh << 'EOF'
#!/bin/bash
AWS_ACCOUNT_ID="<YOUR_AWS_ACCOUNT_ID>"
AWS_REGION="us-east-1"

echo "🔐 Logging into ECR..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

echo "📥 Pulling latest images..."
docker-compose pull

echo "🔄 Restarting services..."
docker-compose up -d --force-recreate

echo "🧹 Cleaning up..."
docker image prune -af

echo "✅ Deployment complete!"
docker-compose ps
EOF

chmod +x ~/revcart/deploy.sh
./deploy.sh
```

## 📝 Complete Workflow Summary

### Every Time You Deploy:

1. **Push code to Git**
   ```bash
   git add .
   git commit -m "New feature"
   git push origin main
   ```

2. **Wait for Jenkins to complete** (builds & tests)

3. **Push images to ECR** (on Jenkins server or local)
   ```bash
   ./push-to-ecr.sh
   ```

4. **Deploy on EC2** (SSH to EC2)
   ```bash
   ssh ec2-user@<EC2_IP>
   cd ~/revcart
   ./deploy.sh
   ```

5. **Verify**
   ```bash
   curl http://<EC2_IP>:8080/actuator/health
   curl http://<EC2_IP>:4200
   ```

## 🎯 Quick Reference

### Check Jenkins Build Status
- Jenkins URL: http://<JENKINS_IP>:8080
- Job: revcart-deployment

### Push to ECR
```bash
./push-to-ecr.sh
```

### Deploy on EC2
```bash
ssh ec2-user@<EC2_IP>
cd ~/revcart && ./deploy.sh
```

### View Logs on EC2
```bash
docker-compose logs -f
```

### Rollback on EC2
```bash
docker-compose down
docker-compose up -d
```

## ✅ That's It!

Simple 4-step deployment:
1. Git push
2. Jenkins builds
3. You push to ECR
4. You deploy on EC2

Full control at every step! 🎉
