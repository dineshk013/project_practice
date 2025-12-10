# AWS Deployment Guide - RevCart Microservices

## 🎯 Complete CI/CD Flow

```
Git Push → Jenkins → Build → Test → Docker Images → AWS ECR → EC2 Deployment
```

## 📋 Prerequisites

### 1. AWS Account Setup
- AWS Account with admin access
- AWS CLI installed and configured
- AWS Access Key ID and Secret Access Key

### 2. Tools Required
- Jenkins (installed on server or local)
- Docker
- Git
- Maven 3.8+
- Node.js 18+

## 🚀 Step-by-Step Deployment

### Step 1: AWS Infrastructure Setup

#### 1.1 Create VPC and Subnets
```bash
# Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16 --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=revcart-vpc}]'

# Create Public Subnet
aws ec2 create-subnet --vpc-id <VPC_ID> --cidr-block 10.0.1.0/24 --availability-zone us-east-1a

# Create Internet Gateway
aws ec2 create-internet-gateway --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=revcart-igw}]'

# Attach IGW to VPC
aws ec2 attach-internet-gateway --vpc-id <VPC_ID> --internet-gateway-id <IGW_ID>
```

#### 1.2 Create Security Groups
```bash
# Create Security Group
aws ec2 create-security-group \
  --group-name revcart-sg \
  --description "Security group for RevCart application" \
  --vpc-id <VPC_ID>

# Allow SSH (22)
aws ec2 authorize-security-group-ingress \
  --group-id <SG_ID> \
  --protocol tcp \
  --port 22 \
  --cidr 0.0.0.0/0

# Allow HTTP (80)
aws ec2 authorize-security-group-ingress \
  --group-id <SG_ID> \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

# Allow HTTPS (443)
aws ec2 authorize-security-group-ingress \
  --group-id <SG_ID> \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0

# Allow Application Ports (8080-8088, 4200, 3306, 27017)
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 4200 3306 27017; do
  aws ec2 authorize-security-group-ingress \
    --group-id <SG_ID> \
    --protocol tcp \
    --port $port \
    --cidr 0.0.0.0/0
done
```

#### 1.3 Create RDS MySQL Database
```bash
aws rds create-db-instance \
  --db-instance-identifier revcart-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password <YOUR_PASSWORD> \
  --allocated-storage 20 \
  --vpc-security-group-ids <SG_ID> \
  --db-subnet-group-name <SUBNET_GROUP_NAME> \
  --backup-retention-period 7 \
  --publicly-accessible
```

#### 1.4 Create DocumentDB (MongoDB)
```bash
aws docdb create-db-cluster \
  --db-cluster-identifier revcart-docdb \
  --engine docdb \
  --master-username admin \
  --master-user-password <YOUR_PASSWORD> \
  --vpc-security-group-ids <SG_ID>

aws docdb create-db-instance \
  --db-instance-identifier revcart-docdb-instance \
  --db-instance-class db.t3.medium \
  --engine docdb \
  --db-cluster-identifier revcart-docdb
```

#### 1.5 Create ECR Repositories
```bash
# Create ECR repositories for each service
for service in user-service product-service cart-service order-service payment-service notification-service delivery-service analytics-service gateway frontend; do
  aws ecr create-repository --repository-name revcart-${service} --region us-east-1
done
```

#### 1.6 Launch EC2 Instance
```bash
# Create Key Pair
aws ec2 create-key-pair --key-name revcart-key --query 'KeyMaterial' --output text > revcart-key.pem
chmod 400 revcart-key.pem

# Launch EC2 Instance (t3.large for microservices)
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --count 1 \
  --instance-type t3.large \
  --key-name revcart-key \
  --security-group-ids <SG_ID> \
  --subnet-id <SUBNET_ID> \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=revcart-app}]' \
  --user-data file://ec2-user-data.sh
```

### Step 2: EC2 Instance Setup

#### 2.1 SSH into EC2
```bash
ssh -i revcart-key.pem ec2-user@<EC2_PUBLIC_IP>
```

#### 2.2 Install Docker and Docker Compose
```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install AWS CLI
sudo yum install aws-cli -y

# Logout and login again for docker group to take effect
exit
```

### Step 3: Jenkins Setup

#### 3.1 Install Jenkins
```bash
# On Jenkins server (can be same EC2 or separate)
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key
sudo yum install jenkins java-17-openjdk-devel -y
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

#### 3.2 Configure Jenkins

1. **Access Jenkins**: http://<JENKINS_IP>:8080
2. **Install Plugins**:
   - Docker Pipeline
   - AWS Steps
   - SSH Agent
   - Git
   - Maven Integration
   - NodeJS

3. **Configure Credentials**:
   - AWS Credentials (Access Key ID & Secret)
   - Docker Hub Credentials
   - EC2 SSH Key
   - GitHub Token

4. **Configure Tools**:
   - JDK 17
   - Maven 3.8+
   - NodeJS 18+
   - Docker

#### 3.3 Create Jenkins Pipeline Job

1. New Item → Pipeline
2. Name: `revcart-deployment`
3. Pipeline Definition: Pipeline script from SCM
4. SCM: Git
5. Repository URL: `<YOUR_GIT_REPO>`
6. Script Path: `Jenkinsfile`

### Step 4: Environment Configuration

#### 4.1 Update .env file
```bash
# On EC2 instance
cat > .env << 'EOF'
# MySQL Configuration (RDS)
MYSQL_ROOT_PASSWORD=<YOUR_RDS_PASSWORD>
MYSQL_USERNAME=admin
DB_HOST=<RDS_ENDPOINT>
DB_PORT=3306

# MongoDB Configuration (DocumentDB)
MONGO_USERNAME=admin
MONGO_PASSWORD=<YOUR_DOCDB_PASSWORD>
MONGO_HOST=<DOCDB_ENDPOINT>
MONGO_PORT=27017

# Application Configuration
SPRING_PROFILES_ACTIVE=production
AWS_REGION=us-east-1
EOF
```

#### 4.2 Update docker-compose.yml for Production
```yaml
# Remove database containers (using RDS/DocumentDB)
# Update service environment variables to use RDS/DocumentDB
```

### Step 5: Deploy Application

#### 5.1 Manual Deployment (First Time)
```bash
# On EC2 instance
cd /home/ec2-user

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Pull images
docker-compose pull

# Start services
docker-compose up -d

# Check status
docker-compose ps
```

#### 5.2 Automated Deployment via Jenkins
```bash
# On your local machine
git add .
git commit -m "Deploy to production"
git push origin main

# Jenkins will automatically:
# 1. Checkout code
# 2. Build all services
# 3. Run tests
# 4. Build Docker images
# 5. Push to ECR
# 6. Deploy to EC2
# 7. Run health checks
```

### Step 6: Database Migration

#### 6.1 Export Local Data
```bash
# Export from local MySQL
mysqldump -u root -p revcart > revcart_backup.sql

# Export from local MongoDB
mongodump --db revcart --out ./mongo_backup
```

#### 6.2 Import to AWS
```bash
# Import to RDS MySQL
mysql -h <RDS_ENDPOINT> -u admin -p revcart < revcart_backup.sql

# Import to DocumentDB
mongorestore --host <DOCDB_ENDPOINT>:27017 --username admin --password <PASSWORD> --ssl --sslCAFile rds-combined-ca-bundle.pem ./mongo_backup
```

### Step 7: Configure Load Balancer (Optional)

#### 7.1 Create Application Load Balancer
```bash
aws elbv2 create-load-balancer \
  --name revcart-alb \
  --subnets <SUBNET_ID_1> <SUBNET_ID_2> \
  --security-groups <SG_ID>

# Create Target Group
aws elbv2 create-target-group \
  --name revcart-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id <VPC_ID> \
  --health-check-path /actuator/health

# Register EC2 instance
aws elbv2 register-targets \
  --target-group-arn <TG_ARN> \
  --targets Id=<EC2_INSTANCE_ID>

# Create Listener
aws elbv2 create-listener \
  --load-balancer-arn <ALB_ARN> \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=<TG_ARN>
```

### Step 8: Configure Auto Scaling (Optional)

```bash
# Create Launch Template
aws ec2 create-launch-template \
  --launch-template-name revcart-template \
  --version-description "RevCart v1" \
  --launch-template-data file://launch-template.json

# Create Auto Scaling Group
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name revcart-asg \
  --launch-template LaunchTemplateName=revcart-template \
  --min-size 1 \
  --max-size 3 \
  --desired-capacity 2 \
  --vpc-zone-identifier "<SUBNET_ID_1>,<SUBNET_ID_2>" \
  --target-group-arns <TG_ARN>

# Create Scaling Policies
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name revcart-asg \
  --policy-name scale-up \
  --scaling-adjustment 1 \
  --adjustment-type ChangeInCapacity
```

### Step 9: Monitoring & Logging

#### 9.1 CloudWatch Logs
```bash
# Install CloudWatch agent on EC2
sudo yum install amazon-cloudwatch-agent -y

# Configure CloudWatch agent
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json
```

#### 9.2 CloudWatch Alarms
```bash
# CPU Utilization Alarm
aws cloudwatch put-metric-alarm \
  --alarm-name revcart-high-cpu \
  --alarm-description "Alert when CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --dimensions Name=InstanceId,Value=<EC2_INSTANCE_ID> \
  --evaluation-periods 2
```

## 🔐 Security Best Practices

1. **Use AWS Secrets Manager** for sensitive data
2. **Enable VPC Flow Logs** for network monitoring
3. **Use IAM roles** instead of access keys where possible
4. **Enable MFA** for AWS console access
5. **Regular security patches** on EC2 instances
6. **Use HTTPS** with SSL certificates (AWS Certificate Manager)
7. **Implement WAF** for application protection

## 📊 Cost Optimization

- Use **Reserved Instances** for predictable workloads
- Enable **Auto Scaling** to scale down during low traffic
- Use **S3 Lifecycle Policies** for old logs
- Monitor costs with **AWS Cost Explorer**
- Use **Spot Instances** for non-critical workloads

## 🧪 Testing Deployment

```bash
# Test Gateway
curl http://<EC2_PUBLIC_IP>:8080/actuator/health

# Test Frontend
curl http://<EC2_PUBLIC_IP>:4200

# Test API
curl http://<EC2_PUBLIC_IP>:8080/api/products
```

## 📝 Useful Commands

```bash
# View logs
docker-compose logs -f <service-name>

# Restart service
docker-compose restart <service-name>

# Scale service
docker-compose up -d --scale user-service=3

# Check resource usage
docker stats

# Clean up
docker system prune -af
```

## 🆘 Troubleshooting

### Issue: Services not starting
```bash
# Check logs
docker-compose logs

# Check disk space
df -h

# Check memory
free -m
```

### Issue: Database connection failed
```bash
# Test RDS connection
mysql -h <RDS_ENDPOINT> -u admin -p

# Check security group rules
aws ec2 describe-security-groups --group-ids <SG_ID>
```

### Issue: Images not pulling from ECR
```bash
# Re-login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ECR_REGISTRY>

# Check ECR permissions
aws ecr get-repository-policy --repository-name revcart-user-service
```

## 🎉 Success!

Your RevCart application is now deployed on AWS with complete CI/CD pipeline!

**Access URLs:**
- Frontend: http://<EC2_PUBLIC_IP>:4200
- API Gateway: http://<EC2_PUBLIC_IP>:8080
- Health Check: http://<EC2_PUBLIC_IP>:8080/actuator/health

---

**Next Steps:**
1. Configure custom domain with Route 53
2. Setup SSL certificate with ACM
3. Implement CloudFront CDN
4. Setup backup and disaster recovery
5. Implement monitoring dashboards
