# RevCart AWS Infrastructure Setup

## Infrastructure Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Route 53 (DNS)                           │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │              CloudFront (CDN) + WAF                         │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │          Application Load Balancer (ALB)                    │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │                    EKS Cluster                              │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │  User    │  │ Product  │  │   Cart   │  │  Order   │  │ │
│  │  │ Service  │  │ Service  │  │ Service  │  │ Service  │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │ Payment  │  │Notification│ │ Delivery │  │Analytics │  │ │
│  │  │ Service  │  │  Service   │ │ Service  │  │ Service  │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────┴─────────────────────────────────────┐ │
│  │                  Data Layer                                 │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │ │
│  │  │   RDS    │  │DocumentDB│  │ElastiCache│                │ │
│  │  │  MySQL   │  │ (MongoDB)│  │  (Redis)  │                │ │
│  │  └──────────┘  └──────────┘  └──────────┘                 │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────┴─────────────────────────────────────┐ │
│  │              Messaging & Events                             │ │
│  │  ┌──────────┐  ┌──────────┐                                │ │
│  │  │   SNS    │  │   SQS    │                                │ │
│  │  │ Topics   │  │  Queues  │                                │ │
│  │  └──────────┘  └──────────┘                                │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────┴─────────────────────────────────────┐ │
│  │           Monitoring & Logging                              │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │ │
│  │  │CloudWatch│  │  X-Ray   │  │   ECR    │                 │ │
│  │  │          │  │          │  │          │                 │ │
│  │  └──────────┘  └──────────┘  └──────────┘                 │ │
│  └─────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
```

---

## Terraform Configuration

### 1. VPC Setup

```hcl
# vpc.tf
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "revcart-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["us-east-1a", "us-east-1b", "us-east-1c"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"]
  database_subnets = ["10.0.201.0/24", "10.0.202.0/24", "10.0.203.0/24"]

  enable_nat_gateway   = true
  single_nat_gateway   = false
  enable_dns_hostnames = true
  enable_dns_support   = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = "1"
  }

  tags = {
    Environment = "production"
    Project     = "RevCart"
  }
}
```

### 2. EKS Cluster

```hcl
# eks.tf
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "revcart-cluster"
  cluster_version = "1.28"

  vpc_id                   = module.vpc.vpc_id
  subnet_ids               = module.vpc.private_subnets
  control_plane_subnet_ids = module.vpc.private_subnets

  cluster_endpoint_public_access = true

  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
    aws-ebs-csi-driver = {
      most_recent = true
    }
  }

  eks_managed_node_group_defaults = {
    ami_type       = "AL2_x86_64"
    instance_types = ["t3.medium"]
    
    attach_cluster_primary_security_group = true
    vpc_security_group_ids                = [aws_security_group.eks_nodes.id]
  }

  eks_managed_node_groups = {
    core_services = {
      name = "core-services"
      
      min_size     = 3
      max_size     = 10
      desired_size = 5

      instance_types = ["t3.medium"]
      capacity_type  = "ON_DEMAND"

      labels = {
        workload = "core"
      }

      tags = {
        NodeGroup = "core-services"
      }
    }

    analytics_services = {
      name = "analytics-services"
      
      min_size     = 1
      max_size     = 5
      desired_size = 2

      instance_types = ["t3.large"]
      capacity_type  = "SPOT"

      labels = {
        workload = "analytics"
      }

      taints = [{
        key    = "workload"
        value  = "analytics"
        effect = "NoSchedule"
      }]

      tags = {
        NodeGroup = "analytics-services"
      }
    }
  }

  tags = {
    Environment = "production"
    Project     = "RevCart"
  }
}
```

### 3. RDS MySQL

```hcl
# rds.tf
resource "aws_db_subnet_group" "revcart" {
  name       = "revcart-db-subnet"
  subnet_ids = module.vpc.database_subnets

  tags = {
    Name = "RevCart DB subnet group"
  }
}

resource "aws_security_group" "rds" {
  name        = "revcart-rds-sg"
  description = "Security group for RDS MySQL"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "revcart-rds-sg"
  }
}

resource "aws_db_instance" "revcart_mysql" {
  identifier     = "revcart-mysql"
  engine         = "mysql"
  engine_version = "8.0.35"
  instance_class = "db.t3.medium"

  allocated_storage     = 100
  max_allocated_storage = 500
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = "revcart"
  username = "admin"
  password = var.db_password

  multi_az               = true
  db_subnet_group_name   = aws_db_subnet_group.revcart.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "mon:04:00-mon:05:00"

  enabled_cloudwatch_logs_exports = ["error", "general", "slowquery"]

  deletion_protection = true
  skip_final_snapshot = false
  final_snapshot_identifier = "revcart-mysql-final-snapshot"

  tags = {
    Name        = "revcart-mysql"
    Environment = "production"
  }
}

# Read Replica for Analytics
resource "aws_db_instance" "revcart_mysql_replica" {
  identifier     = "revcart-mysql-replica"
  replicate_source_db = aws_db_instance.revcart_mysql.identifier
  instance_class = "db.t3.medium"

  publicly_accessible = false
  skip_final_snapshot = true

  tags = {
    Name        = "revcart-mysql-replica"
    Environment = "production"
    Purpose     = "analytics"
  }
}
```

### 4. DocumentDB (MongoDB)

```hcl
# documentdb.tf
resource "aws_docdb_subnet_group" "revcart" {
  name       = "revcart-docdb-subnet"
  subnet_ids = module.vpc.database_subnets

  tags = {
    Name = "RevCart DocumentDB subnet group"
  }
}

resource "aws_security_group" "docdb" {
  name        = "revcart-docdb-sg"
  description = "Security group for DocumentDB"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = 27017
    to_port         = 27017
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "revcart-docdb-sg"
  }
}

resource "aws_docdb_cluster" "revcart" {
  cluster_identifier      = "revcart-docdb"
  engine                  = "docdb"
  master_username         = "admin"
  master_password         = var.docdb_password
  backup_retention_period = 7
  preferred_backup_window = "03:00-04:00"
  skip_final_snapshot     = false
  final_snapshot_identifier = "revcart-docdb-final-snapshot"

  db_subnet_group_name   = aws_docdb_subnet_group.revcart.name
  vpc_security_group_ids = [aws_security_group.docdb.id]

  enabled_cloudwatch_logs_exports = ["audit", "profiler"]

  tags = {
    Name        = "revcart-docdb"
    Environment = "production"
  }
}

resource "aws_docdb_cluster_instance" "revcart" {
  count              = 2
  identifier         = "revcart-docdb-${count.index}"
  cluster_identifier = aws_docdb_cluster.revcart.id
  instance_class     = "db.t3.medium"

  tags = {
    Name        = "revcart-docdb-instance-${count.index}"
    Environment = "production"
  }
}
```

### 5. ElastiCache Redis

```hcl
# elasticache.tf
resource "aws_elasticache_subnet_group" "revcart" {
  name       = "revcart-redis-subnet"
  subnet_ids = module.vpc.private_subnets

  tags = {
    Name = "RevCart Redis subnet group"
  }
}

resource "aws_security_group" "redis" {
  name        = "revcart-redis-sg"
  description = "Security group for Redis"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_nodes.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "revcart-redis-sg"
  }
}

resource "aws_elasticache_replication_group" "revcart" {
  replication_group_id       = "revcart-redis"
  replication_group_description = "Redis cluster for RevCart"
  engine                     = "redis"
  engine_version             = "7.0"
  node_type                  = "cache.t3.medium"
  num_cache_clusters         = 2
  parameter_group_name       = "default.redis7"
  port                       = 6379

  subnet_group_name          = aws_elasticache_subnet_group.revcart.name
  security_group_ids         = [aws_security_group.redis.id]

  automatic_failover_enabled = true
  multi_az_enabled          = true

  snapshot_retention_limit = 5
  snapshot_window         = "03:00-05:00"

  tags = {
    Name        = "revcart-redis"
    Environment = "production"
  }
}
```

### 6. SNS Topics & SQS Queues

```hcl
# messaging.tf
# SNS Topics
resource "aws_sns_topic" "user_events" {
  name = "revcart-user-events"
  
  tags = {
    Service = "user-service"
  }
}

resource "aws_sns_topic" "product_events" {
  name = "revcart-product-events"
  
  tags = {
    Service = "product-service"
  }
}

resource "aws_sns_topic" "order_events" {
  name = "revcart-order-events"
  
  tags = {
    Service = "order-service"
  }
}

resource "aws_sns_topic" "payment_events" {
  name = "revcart-payment-events"
  
  tags = {
    Service = "payment-service"
  }
}

resource "aws_sns_topic" "delivery_events" {
  name = "revcart-delivery-events"
  
  tags = {
    Service = "delivery-service"
  }
}

# SQS Queues
resource "aws_sqs_queue" "notification_service" {
  name                      = "revcart-notification-service-queue"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 345600
  receive_wait_time_seconds = 10
  visibility_timeout_seconds = 30

  tags = {
    Service = "notification-service"
  }
}

resource "aws_sqs_queue" "analytics_service" {
  name                      = "revcart-analytics-service-queue"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 345600
  receive_wait_time_seconds = 10

  tags = {
    Service = "analytics-service"
  }
}

# SNS to SQS Subscriptions
resource "aws_sns_topic_subscription" "user_events_to_notification" {
  topic_arn = aws_sns_topic.user_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.notification_service.arn
}

resource "aws_sns_topic_subscription" "order_events_to_analytics" {
  topic_arn = aws_sns_topic.order_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.analytics_service.arn
}

# SQS Queue Policies
resource "aws_sqs_queue_policy" "notification_service" {
  queue_url = aws_sqs_queue.notification_service.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = "*"
        Action = "sqs:SendMessage"
        Resource = aws_sqs_queue.notification_service.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = [
              aws_sns_topic.user_events.arn,
              aws_sns_topic.order_events.arn,
              aws_sns_topic.payment_events.arn,
              aws_sns_topic.delivery_events.arn
            ]
          }
        }
      }
    ]
  })
}
```

### 7. ECR Repositories

```hcl
# ecr.tf
locals {
  services = [
    "user-service",
    "product-service",
    "cart-service",
    "order-service",
    "payment-service",
    "notification-service",
    "delivery-service",
    "analytics-service"
  ]
}

resource "aws_ecr_repository" "services" {
  for_each = toset(local.services)
  
  name                 = "revcart-${each.value}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Service = each.value
  }
}

resource "aws_ecr_lifecycle_policy" "services" {
  for_each = toset(local.services)
  
  repository = aws_ecr_repository.services[each.key].name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v"]
          countType     = "imageCountMoreThan"
          countNumber   = 10
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Remove untagged images after 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
```

### 8. IAM Roles & Policies

```hcl
# iam.tf
# EKS Node Role
resource "aws_iam_role" "eks_nodes" {
  name = "revcart-eks-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_nodes.name
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_nodes.name
}

resource "aws_iam_role_policy_attachment" "eks_container_registry_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_nodes.name
}

# Service Account for SNS/SQS Access
resource "aws_iam_role" "service_messaging" {
  name = "revcart-service-messaging-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRoleWithWebIdentity"
        Effect = "Allow"
        Principal = {
          Federated = module.eks.oidc_provider_arn
        }
      }
    ]
  })
}

resource "aws_iam_policy" "service_messaging" {
  name = "revcart-service-messaging-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sns:Publish",
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "service_messaging" {
  policy_arn = aws_iam_policy.service_messaging.arn
  role       = aws_iam_role.service_messaging.name
}

# CloudWatch Logs Policy
resource "aws_iam_policy" "cloudwatch_logs" {
  name = "revcart-cloudwatch-logs-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "arn:aws:logs:*:*:*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cloudwatch_logs" {
  policy_arn = aws_iam_policy.cloudwatch_logs.arn
  role       = aws_iam_role.eks_nodes.name
}
```

### 9. CloudWatch Log Groups

```hcl
# cloudwatch.tf
resource "aws_cloudwatch_log_group" "services" {
  for_each = toset(local.services)
  
  name              = "/aws/eks/revcart/${each.value}"
  retention_in_days = 7

  tags = {
    Service = each.value
  }
}

# CloudWatch Dashboard
resource "aws_cloudwatch_dashboard" "revcart" {
  dashboard_name = "RevCart-Services"

  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/EKS", "cluster_failed_node_count", { stat = "Average" }],
            [".", "cluster_node_count", { stat = "Average" }]
          ]
          period = 300
          stat   = "Average"
          region = "us-east-1"
          title  = "EKS Cluster Health"
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", { stat = "Average" }],
            [".", "DatabaseConnections", { stat = "Sum" }]
          ]
          period = 300
          stat   = "Average"
          region = "us-east-1"
          title  = "RDS Performance"
        }
      }
    ]
  })
}
```

### 10. Secrets Manager

```hcl
# secrets.tf
resource "aws_secretsmanager_secret" "db_credentials" {
  name = "revcart/db/credentials"
  
  tags = {
    Environment = "production"
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  
  secret_string = jsonencode({
    username = "admin"
    password = var.db_password
    host     = aws_db_instance.revcart_mysql.address
    port     = 3306
    database = "revcart"
  })
}

resource "aws_secretsmanager_secret" "jwt_secret" {
  name = "revcart/jwt/secret"
  
  tags = {
    Environment = "production"
  }
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id = aws_secretsmanager_secret.jwt_secret.id
  
  secret_string = jsonencode({
    secret = var.jwt_secret
  })
}

resource "aws_secretsmanager_secret" "payment_keys" {
  name = "revcart/payment/keys"
  
  tags = {
    Environment = "production"
  }
}

resource "aws_secretsmanager_secret_version" "payment_keys" {
  secret_id = aws_secretsmanager_secret.payment_keys.id
  
  secret_string = jsonencode({
    razorpay_key_id     = var.razorpay_key_id
    razorpay_key_secret = var.razorpay_key_secret
    stripe_api_key      = var.stripe_api_key
  })
}
```

---

## Cost Estimation (Monthly)

### Compute (EKS)
- EKS Control Plane: $73
- EC2 Instances (5 x t3.medium): ~$150
- EC2 Instances (2 x t3.large Spot): ~$50
- **Subtotal: $273**

### Database
- RDS MySQL (db.t3.medium Multi-AZ): ~$120
- RDS Read Replica: ~$60
- DocumentDB (2 x db.t3.medium): ~$200
- ElastiCache Redis (2 nodes): ~$80
- **Subtotal: $460**

### Storage
- EBS Volumes (500GB): ~$50
- RDS Storage (100GB): ~$12
- Backup Storage: ~$20
- **Subtotal: $82**

### Networking
- ALB: ~$25
- Data Transfer: ~$50
- NAT Gateway (3 AZs): ~$100
- **Subtotal: $175**

### Messaging
- SNS: ~$5
- SQS: ~$5
- **Subtotal: $10**

### Monitoring & Logs
- CloudWatch Logs: ~$20
- CloudWatch Metrics: ~$10
- X-Ray: ~$5
- **Subtotal: $35**

### Container Registry
- ECR Storage: ~$10

### **Total Estimated Monthly Cost: ~$1,045**

---

## Deployment Commands

### Initialize Terraform
```bash
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

### Configure kubectl
```bash
aws eks update-kubeconfig --name revcart-cluster --region us-east-1
```

### Deploy Services
```bash
kubectl apply -f k8s/namespaces/
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/
kubectl apply -f k8s/hpa/
```

### Verify Deployment
```bash
kubectl get pods -n revcart
kubectl get svc -n revcart
kubectl get ingress -n revcart
```

---

## Scaling Strategy

### Horizontal Pod Autoscaler (HPA)
- CPU threshold: 70%
- Memory threshold: 80%
- Min replicas: 2
- Max replicas: 10

### Cluster Autoscaler
- Automatically adds/removes nodes based on pod requirements
- Scale down delay: 10 minutes

### Database Scaling
- RDS: Vertical scaling (instance type upgrade)
- DocumentDB: Add read replicas
- Redis: Add cache nodes

---

## Disaster Recovery

### Backup Strategy
- RDS: Automated daily backups (7-day retention)
- DocumentDB: Daily snapshots (7-day retention)
- EKS: etcd backups via Velero

### Recovery Procedures
1. Restore RDS from snapshot
2. Restore DocumentDB from snapshot
3. Redeploy services from ECR images
4. Update DNS records if needed

### Multi-Region (Future)
- Primary: us-east-1
- Secondary: us-west-2
- Cross-region replication for databases
- Route53 failover routing
