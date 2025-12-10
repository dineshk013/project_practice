pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-east-1'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        ECR_REPO_PREFIX = 'revcart'
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        AWS_CREDENTIALS = credentials('aws-credentials')
        EC2_HOST = "${EC2_PUBLIC_IP}"
        EC2_USER = 'ec2-user'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code from Git...'
                checkout scm
            }
        }
        
        stage('Build Backend Services') {
            parallel {
                stage('Build User Service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Product Service') {
                    steps {
                        dir('product-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Cart Service') {
                    steps {
                        dir('cart-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Order Service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Payment Service') {
                    steps {
                        dir('payment-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Delivery Service') {
                    steps {
                        dir('delivery-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Analytics Service') {
                    steps {
                        dir('analytics-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Gateway') {
                    steps {
                        dir('revcart-gateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('Frontend') {
                    sh 'npm ci'
                    sh 'npm run build --configuration=production'
                }
            }
        }
        
        stage('Run Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        sh 'mvn test -f user-service/pom.xml'
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        dir('Frontend') {
                            sh 'npm test -- --watch=false --browsers=ChromeHeadless'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    echo '🐳 Building Docker images...'
                    sh 'docker-compose build'
                }
            }
        }
        
        stage('Build Complete') {
            steps {
                echo '✅ Build and tests completed successfully!'
                echo '📦 Docker images built locally'
                echo ''
                echo '🚀 Next steps (manual):'
                echo '1. Tag images for ECR:'
                echo '   docker tag revcart_microservices-user-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest'
                echo ''
                echo '2. Push to ECR:'
                echo '   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com'
                echo '   docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/revcart-user-service:latest'
                echo ''
                echo '3. Deploy on EC2:'
                echo '   ssh ec2-user@<EC2_IP>'
                echo '   docker-compose pull'
                echo '   docker-compose up -d'
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
            // Send notification (Slack, Email, etc.)
        }
        failure {
            echo '❌ Pipeline failed!'
            // Send failure notification
        }
        always {
            cleanWs()
        }
    }
}
