# Manual EC2 Deployment Guide

## 🎯 Flow Overview

```
Git Push → Jenkins → Build → Test → Push to ECR → Manual Pull on EC2 → Run Containers
```

## 📋 Prerequisites on EC2

1. EC2 instance running (t3.large recommended)
2. Docker and Docker Compose installed
3. AWS CLI configured
4. Security groups allowing ports: 22, 80, 443, 8080-8088, 4200, 3306, 27017

## 🚀 Step-by-Step Manual Deployment

### Step 1: Jenkins Builds and Pushes to ECR

When you push code to Git, Jenkins automatically:
1. ✅ Pulls code from Git
2. ✅ Builds all microservices
3. ✅ Runs tests
4. ✅ Creates Docker images
5. ✅ Pushes images to AWS ECR

**You don't need to do anything for this step!**

### Step 2: SSH into EC2 Instance

```bash
# From your local machine
ssh -i revcart-key.pem ec2-user@<EC2_PUBLIC_IP>
```

### Step 3: Setup EC2 (First Time Only)

```bash
# Create application directory
mkdir -p ~/revcart
cd ~/revcart

# Copy docker-compose.yml from your repo
# Option 1: Clone repo
git clone <YOUR_REPO_URL> .

# Option 2: Copy file directly
scp -i revcart-key.pem docker-compose.yml ec2-user@<EC2_IP>:~/revcart/
scp -i revcart-key.pem .env ec2-user@<EC2_IP>:~/revcart/
```

### Step 4: Update docker-compose.yml for ECR

```bash
# Edit docker-compose.yml to use ECR images
nano docker-compose.yml
```

Update image names to ECR format:

```yaml
services:
  gateway:
    image: <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-gateway:latest
    # ... rest of config

  user-service:
    image: <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest
    # ... rest of config

  # Repeat for all services
```

### Step 5: Login to AWS ECR

```bash
# Get your AWS account ID
aws sts get-caller-identity --query Account --output text

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

### Step 6: Pull Latest Images from ECR

```bash
# Pull all images
docker-compose pull

# Or pull specific service
docker pull <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest
```

### Step 7: Start/Restart Services

```bash
# First time deployment
docker-compose up -d

# Update deployment (after new Jenkins build)
docker-compose pull
docker-compose up -d --force-recreate

# Or restart specific service
docker-compose restart user-service
```

### Step 8: Verify Deployment

```bash
# Check running containers
docker-compose ps

# Check logs
docker-compose logs -f

# Check specific service logs
docker-compose logs -f gateway

# Test health
curl http://localhost:8080/actuator/health
curl http://localhost:4200
```

## 🔄 Regular Deployment Workflow

### When Jenkins Completes Build:

1. **Jenkins notifies** you that images are pushed to ECR
2. **SSH to EC2**:
   ```bash
   ssh -i revcart-key.pem ec2-user@<EC2_IP>
   cd ~/revcart
   ```

3. **Pull latest images**:
   ```bash
   docker-compose pull
   ```

4. **Restart services**:
   ```bash
   docker-compose up -d --force-recreate
   ```

5. **Verify**:
   ```bash
   docker-compose ps
   curl http://localhost:8080/actuator/health
   ```

## 📝 Useful Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-service

# Last 100 lines
docker-compose logs --tail=100 gateway
```

### Restart Services
```bash
# All services
docker-compose restart

# Specific service
docker-compose restart user-service

# Force recreate (after image update)
docker-compose up -d --force-recreate user-service
```

### Check Resource Usage
```bash
# Container stats
docker stats

# Disk usage
docker system df

# Detailed disk usage
docker system df -v
```

### Clean Up
```bash
# Remove stopped containers
docker-compose down

# Remove old images
docker image prune -a

# Full cleanup (careful!)
docker system prune -af --volumes
```

### Update Specific Service
```bash
# Pull latest image for one service
docker pull <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest

# Restart that service
docker-compose up -d --force-recreate user-service
```

## 🔧 Troubleshooting

### Issue: Cannot pull images from ECR
```bash
# Re-login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Check ECR repository exists
aws ecr describe-repositories --region us-east-1
```

### Issue: Service not starting
```bash
# Check logs
docker-compose logs <service-name>

# Check if port is already in use
netstat -tulpn | grep <PORT>

# Restart service
docker-compose restart <service-name>
```

### Issue: Out of disk space
```bash
# Check disk usage
df -h

# Clean up Docker
docker system prune -af

# Remove old images
docker image prune -a
```

### Issue: Database connection failed
```bash
# Check if databases are running
docker-compose ps mysql mongodb

# Restart databases
docker-compose restart mysql mongodb

# Check database logs
docker-compose logs mysql
docker-compose logs mongodb
```

## 🎯 Quick Deployment Script

Create a deployment script for easy updates:

```bash
# Create deploy.sh
cat > ~/revcart/deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "🔐 Logging into ECR..."
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

echo "📥 Pulling latest images..."
docker-compose pull

echo "🔄 Restarting services..."
docker-compose up -d --force-recreate

echo "🧹 Cleaning up old images..."
docker image prune -af

echo "✅ Deployment complete!"
echo ""
echo "📊 Container status:"
docker-compose ps

echo ""
echo "🏥 Health check:"
sleep 10
curl -f http://localhost:8080/actuator/health && echo "✅ Gateway healthy" || echo "❌ Gateway unhealthy"
curl -f http://localhost:4200 && echo "✅ Frontend healthy" || echo "❌ Frontend unhealthy"
EOF

chmod +x ~/revcart/deploy.sh
```

Now you can deploy with one command:
```bash
./deploy.sh
```

## 📊 Monitoring

### Check Service Health
```bash
# Gateway health
curl http://localhost:8080/actuator/health

# All services health
for port in 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "Checking port $port..."
  curl -f http://localhost:$port/actuator/health || echo "Service on $port is down"
done
```

### Monitor Logs in Real-time
```bash
# All services
docker-compose logs -f

# Multiple specific services
docker-compose logs -f gateway user-service product-service
```

### Check Resource Usage
```bash
# CPU and Memory
docker stats --no-stream

# Continuous monitoring
docker stats
```

## 🎉 Success!

Your application is now deployed on EC2!

**Access URLs:**
- Frontend: http://<EC2_PUBLIC_IP>:4200
- API Gateway: http://<EC2_PUBLIC_IP>:8080
- Health Check: http://<EC2_PUBLIC_IP>:8080/actuator/health

**Deployment Flow:**
1. ✅ Push code to Git
2. ✅ Jenkins builds and pushes to ECR (automatic)
3. ✅ SSH to EC2 and run `./deploy.sh` (manual)
4. ✅ Application updated!

---

**Pro Tip:** Set up a cron job to auto-pull and deploy:
```bash
# Edit crontab
crontab -e

# Add line to check for updates every hour
0 * * * * cd ~/revcart && ./deploy.sh >> ~/deploy.log 2>&1
```
