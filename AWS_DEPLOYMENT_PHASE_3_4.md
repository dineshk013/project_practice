# AWS Deployment Guide - Phase 3 & 4

## Phase 3: Database Migration

### Step 3.1: Create RDS MySQL Instance

**File: `aws/terraform/rds.tf`**

```hcl
# DB Subnet Group
resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "revcart-rds-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_1.id, aws_subnet.private_subnet_2.id]

  tags = {
    Name = "revcart-rds-subnet-group"
  }
}

# RDS MySQL Instance
resource "aws_db_instance" "mysql" {
  identifier             = "revcart-mysql"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"  # Free tier eligible
  allocated_storage      = 20
  storage_type           = "gp2"
  storage_encrypted      = true
  
  db_name  = "revcart_users"
  username = "admin"
  password = random_password.rds_password.result
  
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "mon:04:00-mon:05:00"
  
  skip_final_snapshot    = false
  final_snapshot_identifier = "revcart-mysql-final-snapshot"
  
  enabled_cloudwatch_logs_exports = ["error", "general", "slowquery"]
  
  tags = {
    Name = "revcart-mysql"
  }
}

# Random password for RDS
resource "random_password" "rds_password" {
  length  = 16
  special = true
}

# Store password in Secrets Manager
resource "aws_secretsmanager_secret" "rds_password" {
  name = "revcart/rds/password"
}

resource "aws_secretsmanager_secret_version" "rds_password" {
  secret_id     = aws_secretsmanager_secret.rds_password.id
  secret_string = jsonencode({
    username = "admin"
    password = random_password.rds_password.result
    host     = aws_db_instance.mysql.address
    port     = 3306
  })
}

# Output RDS endpoint
output "rds_endpoint" {
  value = aws_db_instance.mysql.endpoint
}
```

### Step 3.2: Create DocumentDB Cluster

**File: `aws/terraform/documentdb.tf`**

```hcl
# DocumentDB Subnet Group
resource "aws_docdb_subnet_group" "docdb_subnet_group" {
  name       = "revcart-docdb-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_1.id, aws_subnet.private_subnet_2.id]

  tags = {
    Name = "revcart-docdb-subnet-group"
  }
}

# DocumentDB Cluster
resource "aws_docdb_cluster" "mongodb" {
  cluster_identifier      = "revcart-docdb"
  engine                  = "docdb"
  master_username         = "admin"
  master_password         = random_password.docdb_password.result
  db_subnet_group_name    = aws_docdb_subnet_group.docdb_subnet_group.name
  vpc_security_group_ids  = [aws_security_group.docdb_sg.id]
  
  backup_retention_period = 7
  preferred_backup_window = "03:00-04:00"
  
  skip_final_snapshot     = false
  final_snapshot_identifier = "revcart-docdb-final-snapshot"
  
  enabled_cloudwatch_logs_exports = ["audit", "profiler"]
  
  tags = {
    Name = "revcart-docdb"
  }
}

# DocumentDB Instance
resource "aws_docdb_cluster_instance" "mongodb_instance" {
  count              = 1
  identifier         = "revcart-docdb-instance-${count.index}"
  cluster_identifier = aws_docdb_cluster.mongodb.id
  instance_class     = "db.t3.medium"
}

# Random password for DocumentDB
resource "random_password" "docdb_password" {
  length  = 16
  special = true
}

# Store password in Secrets Manager
resource "aws_secretsmanager_secret" "docdb_password" {
  name = "revcart/docdb/password"
}

resource "aws_secretsmanager_secret_version" "docdb_password" {
  secret_id     = aws_secretsmanager_secret.docdb_password.id
  secret_string = jsonencode({
    username = "admin"
    password = random_password.docdb_password.result
    host     = aws_docdb_cluster.mongodb.endpoint
    port     = 27017
  })
}

# Output DocumentDB endpoint
output "docdb_endpoint" {
  value = aws_docdb_cluster.mongodb.endpoint
}
```

### Step 3.3: Migrate Data to RDS

**Create migration script: `aws/scripts/migrate-to-rds.sh`**

```bash
#!/bin/bash

# Export local MySQL databases
echo "Exporting local databases..."
docker exec revcart-mysql mysqldump -u root -proot --databases \
  revcart_users revcart_products revcart_cart revcart_orders revcart_payments \
  > local-backup.sql

# Get RDS endpoint from Secrets Manager
RDS_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/rds/password --query SecretString --output text)
RDS_HOST=$(echo $RDS_SECRET | jq -r '.host')
RDS_USER=$(echo $RDS_SECRET | jq -r '.username')
RDS_PASS=$(echo $RDS_SECRET | jq -r '.password')

# Import to RDS
echo "Importing to RDS..."
mysql -h $RDS_HOST -u $RDS_USER -p$RDS_PASS < local-backup.sql

echo "Migration complete!"
```

### Step 3.4: Migrate Data to DocumentDB

**Create migration script: `aws/scripts/migrate-to-docdb.sh`**

```bash
#!/bin/bash

# Export local MongoDB databases
echo "Exporting local MongoDB databases..."
docker exec revcart-mongodb mongodump \
  --username admin \
  --password admin \
  --authenticationDatabase admin \
  --out /backup

docker cp revcart-mongodb:/backup ./mongo-backup

# Get DocumentDB endpoint from Secrets Manager
DOCDB_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/docdb/password --query SecretString --output text)
DOCDB_HOST=$(echo $DOCDB_SECRET | jq -r '.host')
DOCDB_USER=$(echo $DOCDB_SECRET | jq -r '.username')
DOCDB_PASS=$(echo $DOCDB_SECRET | jq -r '.password')

# Download DocumentDB certificate
wget https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem

# Import to DocumentDB
echo "Importing to DocumentDB..."
mongorestore \
  --host $DOCDB_HOST:27017 \
  --username $DOCDB_USER \
  --password $DOCDB_PASS \
  --ssl \
  --sslCAFile global-bundle.pem \
  ./mongo-backup

echo "Migration complete!"
```

### Step 3.5: Update Application Configuration

**Update each service's `application.yml` to use AWS Secrets Manager:**

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  # For MongoDB services
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

**Create script to fetch secrets: `aws/scripts/fetch-secrets.sh`**

```bash
#!/bin/bash

# Fetch RDS credentials
RDS_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/rds/password --query SecretString --output text)
export DB_URL="jdbc:mysql://$(echo $RDS_SECRET | jq -r '.host'):3306/revcart_users"
export DB_USERNAME=$(echo $RDS_SECRET | jq -r '.username')
export DB_PASSWORD=$(echo $RDS_SECRET | jq -r '.password')

# Fetch DocumentDB credentials
DOCDB_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/docdb/password --query SecretString --output text)
export MONGODB_URI="mongodb://$(echo $DOCDB_SECRET | jq -r '.username'):$(echo $DOCDB_SECRET | jq -r '.password')@$(echo $DOCDB_SECRET | jq -r '.host'):27017/?ssl=true&replicaSet=rs0&readPreference=secondaryPreferred"

echo "Secrets loaded!"
```

---

## Phase 4: Scaling & Observability

### Step 4.1: Create ECR Repositories

```bash
# Create ECR repositories for each service
aws ecr create-repository --repository-name revcart/user-service
aws ecr create-repository --repository-name revcart/product-service
aws ecr create-repository --repository-name revcart/cart-service
aws ecr create-repository --repository-name revcart/order-service
aws ecr create-repository --repository-name revcart/payment-service
aws ecr create-repository --repository-name revcart/notification-service
aws ecr create-repository --repository-name revcart/delivery-service
aws ecr create-repository --repository-name revcart/analytics-service
aws ecr create-repository --repository-name revcart/gateway
aws ecr create-repository --repository-name revcart/frontend
```

### Step 4.2: Push Images to ECR

**Create script: `aws/scripts/push-to-ecr.sh`**

```bash
#!/bin/bash

AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

# Build and push each service
services=("user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service" "delivery-service" "analytics-service" "gateway" "frontend")

for service in "${services[@]}"; do
  echo "Building and pushing $service..."
  
  # Build image
  docker build -t revcart/$service:latest ./$service
  
  # Tag for ECR
  docker tag revcart/$service:latest $ECR_REGISTRY/revcart/$service:latest
  
  # Push to ECR
  docker push $ECR_REGISTRY/revcart/$service:latest
  
  echo "$service pushed successfully!"
done

echo "All images pushed to ECR!"
```

### Step 4.3: Create Application Load Balancer

**File: `aws/terraform/alb.tf`**

```hcl
# Application Load Balancer
resource "aws_lb" "revcart_alb" {
  name               = "revcart-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]

  enable_deletion_protection = false
  enable_http2              = true

  tags = {
    Name = "revcart-alb"
  }
}

# Target Group for Gateway
resource "aws_lb_target_group" "gateway_tg" {
  name     = "revcart-gateway-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.revcart_vpc.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  tags = {
    Name = "revcart-gateway-tg"
  }
}

# Target Group for Frontend
resource "aws_lb_target_group" "frontend_tg" {
  name     = "revcart-frontend-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.revcart_vpc.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = "/health"
    matcher             = "200"
  }

  tags = {
    Name = "revcart-frontend-tg"
  }
}

# HTTP Listener
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.revcart_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend_tg.arn
  }
}

# Listener Rule for API
resource "aws_lb_listener_rule" "api" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway_tg.arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }
}

# Output ALB DNS
output "alb_dns_name" {
  value = aws_lb.revcart_alb.dns_name
}
```

### Step 4.4: Create EC2 Launch Template & Auto Scaling

**File: `aws/terraform/ec2-autoscaling.tf`**

```hcl
# IAM Role for EC2
resource "aws_iam_role" "ec2_role" {
  name = "revcart-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })
}

# Attach policies
resource "aws_iam_role_policy_attachment" "ecr_read" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "secrets_read" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

resource "aws_iam_role_policy_attachment" "cloudwatch" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

# Instance Profile
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "revcart-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# Launch Template
resource "aws_launch_template" "revcart_lt" {
  name_prefix   = "revcart-"
  image_id      = "ami-0c55b159cbfafe1f0"  # Amazon Linux 2 AMI (update for your region)
  instance_type = "t3.medium"

  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_profile.name
  }

  vpc_security_group_ids = [aws_security_group.ec2_sg.id]

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    aws_region     = var.aws_region
    ecr_registry   = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "revcart-instance"
    }
  }
}

# Auto Scaling Group
resource "aws_autoscaling_group" "revcart_asg" {
  name                = "revcart-asg"
  vpc_zone_identifier = [aws_subnet.private_subnet_1.id, aws_subnet.private_subnet_2.id]
  target_group_arns   = [aws_lb_target_group.gateway_tg.arn, aws_lb_target_group.frontend_tg.arn]
  
  min_size         = 2
  max_size         = 6
  desired_capacity = 2

  launch_template {
    id      = aws_launch_template.revcart_lt.id
    version = "$Latest"
  }

  health_check_type         = "ELB"
  health_check_grace_period = 300

  tag {
    key                 = "Name"
    value               = "revcart-instance"
    propagate_at_launch = true
  }
}

# Auto Scaling Policies
resource "aws_autoscaling_policy" "scale_up" {
  name                   = "revcart-scale-up"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.revcart_asg.name
}

resource "aws_autoscaling_policy" "scale_down" {
  name                   = "revcart-scale-down"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.revcart_asg.name
}
```

**File: `aws/terraform/user-data.sh`**

```bash
#!/bin/bash

# Update system
yum update -y

# Install Docker
amazon-linux-extras install docker -y
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install AWS CLI
yum install -y aws-cli jq

# Login to ECR
aws ecr get-login-password --region ${aws_region} | docker login --username AWS --password-stdin ${ecr_registry}

# Fetch secrets
RDS_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/rds/password --region ${aws_region} --query SecretString --output text)
DOCDB_SECRET=$(aws secretsmanager get-secret-value --secret-id revcart/docdb/password --region ${aws_region} --query SecretString --output text)

# Export environment variables
export DB_URL="jdbc:mysql://$(echo $RDS_SECRET | jq -r '.host'):3306/revcart_users"
export DB_USERNAME=$(echo $RDS_SECRET | jq -r '.username')
export DB_PASSWORD=$(echo $RDS_SECRET | jq -r '.password')
export MONGODB_URI="mongodb://$(echo $DOCDB_SECRET | jq -r '.username'):$(echo $DOCDB_SECRET | jq -r '.password')@$(echo $DOCDB_SECRET | jq -r '.host'):27017/?ssl=true"

# Pull and run containers
docker pull ${ecr_registry}/revcart/gateway:latest
docker pull ${ecr_registry}/revcart/frontend:latest
docker pull ${ecr_registry}/revcart/user-service:latest
# ... pull all services

# Run containers with docker-compose
cat > /home/ec2-user/docker-compose.yml <<EOF
version: '3.8'
services:
  gateway:
    image: ${ecr_registry}/revcart/gateway:latest
    ports:
      - "8080:8080"
    environment:
      - DB_URL=$DB_URL
      - DB_USERNAME=$DB_USERNAME
      - DB_PASSWORD=$DB_PASSWORD
  # ... other services
EOF

cd /home/ec2-user
docker-compose up -d

# Install CloudWatch agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
rpm -U ./amazon-cloudwatch-agent.rpm

# Configure CloudWatch agent
cat > /opt/aws/amazon-cloudwatch-agent/etc/config.json <<EOF
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/log/docker.log",
            "log_group_name": "/aws/ec2/revcart",
            "log_stream_name": "{instance_id}/docker"
          }
        ]
      }
    }
  },
  "metrics": {
    "namespace": "RevCart",
    "metrics_collected": {
      "cpu": {
        "measurement": [{"name": "cpu_usage_idle"}],
        "totalcpu": false
      },
      "mem": {
        "measurement": [{"name": "mem_used_percent"}]
      }
    }
  }
}
EOF

# Start CloudWatch agent
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json
```

### Step 4.5: Create CloudWatch Alarms

**File: `aws/terraform/cloudwatch.tf`**

```hcl
# CPU Utilization Alarm (Scale Up)
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "revcart-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "70"
  alarm_description   = "This metric monitors ec2 cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.scale_up.arn, aws_sns_topic.alerts.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.revcart_asg.name
  }
}

# CPU Utilization Alarm (Scale Down)
resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "revcart-cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "30"
  alarm_description   = "This metric monitors ec2 cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.scale_down.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.revcart_asg.name
  }
}

# Memory Utilization Alarm
resource "aws_cloudwatch_metric_alarm" "memory_high" {
  alarm_name          = "revcart-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "mem_used_percent"
  namespace           = "RevCart"
  period              = "120"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors memory utilization"
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

# Unhealthy Host Alarm
resource "aws_cloudwatch_metric_alarm" "unhealthy_hosts" {
  alarm_name          = "revcart-unhealthy-hosts"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = "60"
  statistic           = "Average"
  threshold           = "0"
  alarm_description   = "Alert when unhealthy hosts detected"
  alarm_actions       = [aws_sns_topic.alerts.arn]

  dimensions = {
    TargetGroup  = aws_lb_target_group.gateway_tg.arn_suffix
    LoadBalancer = aws_lb.revcart_alb.arn_suffix
  }
}

# SNS Topic for Alerts
resource "aws_sns_topic" "alerts" {
  name = "revcart-alerts"
}

resource "aws_sns_topic_subscription" "email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "revcart_logs" {
  name              = "/aws/ec2/revcart"
  retention_in_days = 7
}
```

### Step 4.6: Deploy Everything

**Create deployment script: `aws/scripts/deploy.sh`**

```bash
#!/bin/bash

echo "Starting AWS deployment..."

# 1. Initialize Terraform
cd aws/terraform
terraform init

# 2. Plan deployment
terraform plan -out=tfplan

# 3. Apply infrastructure
terraform apply tfplan

# 4. Get outputs
ALB_DNS=$(terraform output -raw alb_dns_name)
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
DOCDB_ENDPOINT=$(terraform output -raw docdb_endpoint)

echo "Infrastructure deployed!"
echo "ALB DNS: $ALB_DNS"
echo "RDS Endpoint: $RDS_ENDPOINT"
echo "DocumentDB Endpoint: $DOCDB_ENDPOINT"

# 5. Migrate databases
cd ../scripts
./migrate-to-rds.sh
./migrate-to-docdb.sh

# 6. Build and push images
./push-to-ecr.sh

echo "Deployment complete!"
echo "Access your application at: http://$ALB_DNS"
```

---

## Summary Checklist

### Phase 1: Docker ✅
- [ ] Create Dockerfiles for all services
- [ ] Create docker-compose.yml
- [ ] Test locally with `docker-compose up`
- [ ] Verify all services are healthy

### Phase 2: AWS Infrastructure ✅
- [ ] Create VPC and subnets
- [ ] Configure security groups
- [ ] Create S3 bucket
- [ ] Setup IAM roles

### Phase 3: Database Migration ✅
- [ ] Create RDS MySQL instance
- [ ] Create DocumentDB cluster
- [ ] Migrate data from local to AWS
- [ ] Update application configs

### Phase 4: Scaling & Observability ✅
- [ ] Create ECR repositories
- [ ] Push Docker images to ECR
- [ ] Setup Application Load Balancer
- [ ] Configure Auto Scaling
- [ ] Setup CloudWatch monitoring
- [ ] Create CloudWatch alarms

### Final Steps
- [ ] Test application on AWS
- [ ] Configure domain name (Route 53)
- [ ] Setup SSL certificate (ACM)
- [ ] Configure CI/CD pipeline
- [ ] Setup backup strategy
- [ ] Document runbooks

---

**Your application is now production-ready on AWS! 🚀**
