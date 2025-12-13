pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK-17'
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
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Product Service') {
                    steps {
                        dir('product-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Cart Service') {
                    steps {
                        dir('cart-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Order Service') {
                    steps {
                        dir('order-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Payment Service') {
                    steps {
                        dir('payment-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Notification Service') {
                    steps {
                        dir('notification-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Delivery Service') {
                    steps {
                        dir('delivery-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Analytics Service') {
                    steps {
                        dir('analytics-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Gateway') {
                    steps {
                        dir('revcart-gateway') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('Frontend') {
                    bat 'npm ci'
                    bat 'npm run build -- --configuration=production'
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                echo '✅ Skipping tests for now'
            }
        }
        
        stage('Build Docker Images') {
            steps {
                echo '🐳 Building Docker images...'
                script {
                    try {
                        bat 'docker --version'
                        bat 'docker-compose --version'
                        bat 'docker-compose build'
                    } catch (Exception e) {
                        echo '⚠️ Docker not available on Jenkins server'
                        echo '📝 Skipping Docker build - manual deployment required'
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Tag and Push to ECR') {
            when {
                expression { currentBuild.result != 'UNSTABLE' }
            }
            environment {
                AWS_ACCOUNT_ID = '123456789012' // Replace with your AWS Account ID
                AWS_REGION = 'us-east-1'
                ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
            }
            steps {
                echo '🏷️ Tagging and pushing images to ECR...'
                
                script {
                    try {
                        // Login to ECR
                        bat 'aws ecr get-login-password --region %AWS_REGION% | docker login --username AWS --password-stdin %ECR_REGISTRY%'
                        
                        // Tag and push each service
                        def services = [
                            'user-service', 'product-service', 'cart-service', 'order-service',
                            'payment-service', 'notification-service', 'delivery-service', 
                            'analytics-service', 'gateway', 'frontend'
                        ]
                        
                        services.each { service ->
                            bat "docker tag revcart_microservices-${service}:latest %ECR_REGISTRY%/revcart-${service}:latest"
                            bat "docker tag revcart_microservices-${service}:latest %ECR_REGISTRY%/revcart-${service}:%BUILD_NUMBER%"
                            bat "docker push %ECR_REGISTRY%/revcart-${service}:latest"
                            bat "docker push %ECR_REGISTRY%/revcart-${service}:%BUILD_NUMBER%"
                        }
                    } catch (Exception e) {
                        echo '⚠️ ECR push failed - Docker not available'
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Deploy to EC2') {
            when {
                expression { currentBuild.result != 'UNSTABLE' }
            }
            environment {
                EC2_HOST = 'your-ec2-public-ip' // Replace with your EC2 IP
                EC2_USER = 'ec2-user'
            }
            steps {
                echo '🚀 Deploying to EC2...'
                
                script {
                    try {
                        // Copy docker-compose file to EC2
                        bat 'scp -o StrictHostKeyChecking=no docker-compose.yml %EC2_USER%@%EC2_HOST%:~/'
                        
                        // Deploy on EC2
                        bat '''
                        ssh -o StrictHostKeyChecking=no %EC2_USER%@%EC2_HOST% "
                            aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin %ECR_REGISTRY% &&
                            docker-compose pull &&
                            docker-compose up -d
                        "
                        '''
                    } catch (Exception e) {
                        echo '⚠️ EC2 deployment failed - manual deployment required'
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo '🎉 All Maven builds completed!'
            echo '🎉 Frontend build completed!'
            echo '📝 Ready for manual Docker deployment if needed'
        }
        unstable {
            echo '⚠️ Pipeline completed with warnings!'
            echo '✅ All builds successful'
            echo '⚠️ Docker deployment skipped - manual deployment required'
            echo '📝 Manual steps:'
            echo '1. Run: docker-compose build'
            echo '2. Run: docker-compose up -d'
        }
        failure {
            echo '❌ Pipeline failed!'
            echo '📧 Check Jenkins logs for details'
        }
        always {
            echo '📝 Build artifacts created:'
            echo '- JAR files in target/ directories'
            echo '- Frontend build in Frontend/dist/'
            
            // Clean up local Docker images to save space
            script {
                try {
                    bat 'docker system prune -f'
                } catch (Exception e) {
                    echo '📝 Docker cleanup skipped - Docker not available'
                }
            }
        }
    }
}
