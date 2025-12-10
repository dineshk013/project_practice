# AWS Deployment - Quick Start Guide

## 🚀 Quick Deployment (30 minutes)

### Prerequisites
- AWS Account with billing enabled
- AWS CLI installed and configured
- Docker installed locally
- Terraform installed (optional, for IaC)

---

## Option 1: Manual Deployment (Beginner Friendly)

### Step 1: Test Locally (5 minutes)

```bash
# 1. Navigate to project
cd Revcart_Microservices

# 2. Create environment file
cp .env.example .env

# 3. Start all services
docker-compose up -d

# 4. Verify services are running
docker-compose ps

# 5. Test application
# Frontend: http://localhost:4200
# Gateway: http://localhost:8080

# 6. Stop services
docker-compose down
```

### Step 2: Create AWS Resources (15 minutes)

#### A. Create VPC (AWS Console)
1. Go to VPC Dashboard
2. Click "Create VPC"
3. Select "VPC and more"
4. Name: `revcart-vpc`
5. IPv4 CIDR: `10.0.0.0/16`
6. Availability Zones: 2
7. Public subnets: 2
8. Private subnets: 2
9. NAT gateways: 1
10. Click "Create VPC"

#### B. Create RDS MySQL (AWS Console)
1. Go to RDS Dashboard
2. Click "Create database"
3. Choose "MySQL"
4. Template: "Free tier" (or "Production" for real use)
5. DB instance identifier: `revcart-mysql`
6. Master username: `admin`
7. Master password: (create strong password)
8. DB instance class: `db.t3.micro`
9. Storage: 20 GB
10. VPC: Select `revcart-vpc`
11. Public access: No
12. Create new security group: `revcart-rds-sg`
13. Click "Create database"
14. **Save endpoint and credentials!**

#### C. Create DocumentDB (AWS Console)
1. Go to DocumentDB Dashboard
2. Click "Create cluster"
3. Cluster identifier: `revcart-docdb`
4. Master username: `admin`
5. Master password: (create strong password)
6. Instance class: `db.t3.medium`
7. Number of instances: 1
8. VPC: Select `revcart-vpc`
9. Create new security group: `revcart-docdb-sg`
10. Click "Create cluster"
11. **Save endpoint and credentials!**

#### D. Create S3 Bucket
```bash
aws s3 mb s3://revcart-assets-YOUR-NAME --region us-east-1
```

#### E. Create ECR Repositories
```bash
# Run this script
for service in user-service product-service cart-service order-service payment-service notification-service delivery-service analytics-service gateway frontend; do
  aws ecr create-repository --repository-name revcart/$service --region us-east-1
done
```

### Step 3: Push Docker Images (5 minutes)

```bash
# 1. Login to ECR
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# 2. Build and push (run from project root)
./aws/scripts/push-to-ecr.sh
```

### Step 4: Create EC2 Instance (5 minutes)

1. Go to EC2 Dashboard
2. Click "Launch Instance"
3. Name: `revcart-server`
4. AMI: Amazon Linux 2
5. Instance type: `t3.medium`
6. Key pair: Create new or select existing
7. Network: Select `revcart-vpc`
8. Subnet: Select private subnet
9. Security group: Create new
   - Allow SSH (22) from your IP
   - Allow HTTP (80) from anywhere
   - Allow 8080-8088 from VPC CIDR
10. Storage: 30 GB
11. Click "Launch instance"

### Step 5: Setup EC2 Instance

```bash
# 1. SSH into instance
ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>

# 2. Install Docker
sudo yum update -y
sudo amazon-linux-extras install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# 3. Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 4. Logout and login again
exit
ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>

# 5. Login to ECR
AWS_ACCOUNT_ID=<YOUR-ACCOUNT-ID>
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# 6. Create docker-compose.yml
cat > docker-compose.yml <<EOF
version: '3.8'
services:
  gateway:
    image: $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/revcart/gateway:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: unless-stopped

  user-service:
    image: $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/revcart/user-service:latest
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://<RDS-ENDPOINT>:3306/revcart_users
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=<RDS-PASSWORD>
    restart: unless-stopped

  # Add other services...

  frontend:
    image: $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/revcart/frontend:latest
    ports:
      - "80:80"
    restart: unless-stopped
EOF

# 7. Start services
docker-compose up -d

# 8. Check status
docker-compose ps
```

---

## Option 2: Automated Deployment with Terraform (Advanced)

### Step 1: Install Terraform

```bash
# Windows (using Chocolatey)
choco install terraform

# Or download from: https://www.terraform.io/downloads
```

### Step 2: Configure Terraform

```bash
# 1. Navigate to terraform directory
cd aws/terraform

# 2. Create terraform.tfvars
cat > terraform.tfvars <<EOF
aws_region    = "us-east-1"
project_name  = "revcart"
alert_email   = "your-email@example.com"
db_password   = "YourStrongPassword123!"
EOF

# 3. Initialize Terraform
terraform init

# 4. Plan deployment
terraform plan

# 5. Apply (creates all AWS resources)
terraform apply -auto-approve
```

### Step 3: Deploy Application

```bash
# Run deployment script
cd ../scripts
./deploy.sh
```

---

## Cost Estimation

### Free Tier (First 12 months)
- **EC2**: t3.micro (750 hours/month) - FREE
- **RDS**: db.t3.micro (750 hours/month) - FREE
- **S3**: 5 GB storage - FREE
- **Data Transfer**: 15 GB/month - FREE

**Total: $0/month** (within free tier limits)

### After Free Tier / Production Setup

| Service | Configuration | Monthly Cost |
|---------|--------------|--------------|
| EC2 (2x t3.medium) | 2 instances | $60 |
| RDS MySQL (db.t3.micro) | 20 GB storage | $15 |
| DocumentDB (db.t3.medium) | 1 instance | $70 |
| Application Load Balancer | Standard | $20 |
| NAT Gateway | 1 gateway | $35 |
| S3 | 50 GB storage | $1 |
| Data Transfer | 100 GB/month | $9 |
| CloudWatch | Logs + Metrics | $10 |
| **Total** | | **~$220/month** |

### Cost Optimization Tips

1. **Use Spot Instances**: Save up to 90% on EC2
2. **Reserved Instances**: Save 30-60% with 1-year commitment
3. **Auto Scaling**: Scale down during low traffic
4. **S3 Lifecycle Policies**: Move old data to Glacier
5. **CloudWatch Log Retention**: Reduce to 7 days
6. **Use AWS Free Tier**: First year is mostly free

---

## Monitoring & Maintenance

### Check Application Health

```bash
# Check EC2 instance
aws ec2 describe-instances --filters "Name=tag:Name,Values=revcart-server"

# Check RDS status
aws rds describe-db-instances --db-instance-identifier revcart-mysql

# Check DocumentDB status
aws docdb describe-db-clusters --db-cluster-identifier revcart-docdb

# View CloudWatch logs
aws logs tail /aws/ec2/revcart --follow
```

### Update Application

```bash
# 1. Build new images locally
docker-compose build

# 2. Push to ECR
./aws/scripts/push-to-ecr.sh

# 3. SSH to EC2 and pull new images
ssh -i your-key.pem ec2-user@<EC2-IP>
docker-compose pull
docker-compose up -d

# 4. Verify
docker-compose ps
```

### Backup Strategy

```bash
# RDS Automated Backups (enabled by default)
# - Retention: 7 days
# - Backup window: 03:00-04:00 UTC

# Manual RDS Snapshot
aws rds create-db-snapshot \
  --db-instance-identifier revcart-mysql \
  --db-snapshot-identifier revcart-mysql-$(date +%Y%m%d)

# DocumentDB Snapshot
aws docdb create-db-cluster-snapshot \
  --db-cluster-identifier revcart-docdb \
  --db-cluster-snapshot-identifier revcart-docdb-$(date +%Y%m%d)
```

---

## Troubleshooting

### Services Won't Start

```bash
# Check logs
docker-compose logs -f [service-name]

# Check database connectivity
docker exec -it user-service ping <RDS-ENDPOINT>

# Restart service
docker-compose restart [service-name]
```

### High Costs

```bash
# Check cost breakdown
aws ce get-cost-and-usage \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY \
  --metrics BlendedCost

# Stop unused resources
docker-compose down
aws ec2 stop-instances --instance-ids <INSTANCE-ID>
```

### Database Connection Issues

1. Check security group rules
2. Verify VPC configuration
3. Test connection from EC2:
   ```bash
   mysql -h <RDS-ENDPOINT> -u admin -p
   mongosh "mongodb://admin:password@<DOCDB-ENDPOINT>:27017/?ssl=true"
   ```

---

## Next Steps

1. **Setup Domain**: Use Route 53 for custom domain
2. **Enable HTTPS**: Use AWS Certificate Manager
3. **Setup CI/CD**: Use GitHub Actions or AWS CodePipeline
4. **Add Monitoring**: Setup CloudWatch dashboards
5. **Implement Caching**: Add Redis/ElastiCache
6. **Setup CDN**: Use CloudFront for static assets

---

## Support & Resources

- **AWS Documentation**: https://docs.aws.amazon.com/
- **AWS Free Tier**: https://aws.amazon.com/free/
- **AWS Calculator**: https://calculator.aws/
- **AWS Support**: https://console.aws.amazon.com/support/

---

**You're now running on AWS! 🎉**
