# RevCart - Start All Services Script
# Run this from the project root directory

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RevCart Microservices Startup" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check if MySQL is running
Write-Host "Checking MySQL..." -NoNewline
try {
    $mysqlCheck = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue | Where-Object {$_.Status -eq "Running"}
    if ($mysqlCheck) {
        Write-Host " ✓ Running" -ForegroundColor Green
    } else {
        Write-Host " ✗ Not Running" -ForegroundColor Red
        Write-Host "Please start MySQL first: net start MySQL80" -ForegroundColor Yellow
        exit
    }
} catch {
    Write-Host " ? Cannot verify" -ForegroundColor Yellow
}

# Check if MongoDB is running
Write-Host "Checking MongoDB..." -NoNewline
try {
    $mongoCheck = Get-Service -Name "MongoDB" -ErrorAction SilentlyContinue | Where-Object {$_.Status -eq "Running"}
    if ($mongoCheck) {
        Write-Host " ✓ Running" -ForegroundColor Green
    } else {
        Write-Host " ✗ Not Running" -ForegroundColor Red
        Write-Host "Please start MongoDB first: net start MongoDB" -ForegroundColor Yellow
        exit
    }
} catch {
    Write-Host " ? Cannot verify" -ForegroundColor Yellow
}

Write-Host "`nStarting services..." -ForegroundColor Cyan
Write-Host "This will open 10 terminal windows`n" -ForegroundColor Yellow

# Start Gateway (MUST START FIRST)
Write-Host "[1/10] Starting Gateway (Port 8080)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\revcart-gateway'; Write-Host 'Starting Gateway...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 15

# Start User Service
Write-Host "[2/10] Starting User Service (Port 8081)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\user-service'; Write-Host 'Starting User Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Product Service
Write-Host "[3/10] Starting Product Service (Port 8082)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\product-service'; Write-Host 'Starting Product Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Cart Service
Write-Host "[4/10] Starting Cart Service (Port 8083)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\cart-service'; Write-Host 'Starting Cart Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Order Service
Write-Host "[5/10] Starting Order Service (Port 8084)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\order-service'; Write-Host 'Starting Order Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Payment Service
Write-Host "[6/10] Starting Payment Service (Port 8085)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\payment-service'; Write-Host 'Starting Payment Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Notification Service
Write-Host "[7/10] Starting Notification Service (Port 8086)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\notification-service'; Write-Host 'Starting Notification Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Delivery Service
Write-Host "[8/10] Starting Delivery Service (Port 8087)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\delivery-service'; Write-Host 'Starting Delivery Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Analytics Service
Write-Host "[9/10] Starting Analytics Service (Port 8088)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\analytics-service'; Write-Host 'Starting Analytics Service...' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Sleep -Seconds 8

# Start Frontend
Write-Host "[10/10] Starting Frontend (Port 4200)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\Frontend'; Write-Host 'Starting Angular Frontend...' -ForegroundColor Cyan; npm start"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services are starting!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nPlease wait 2-3 minutes for all services to be ready" -ForegroundColor Yellow
Write-Host "`nAccess URLs:" -ForegroundColor Cyan
Write-Host "  Frontend:  http://localhost:4200" -ForegroundColor White
Write-Host "  Gateway:   http://localhost:8080" -ForegroundColor White
Write-Host "  Services:  http://localhost:8081-8088" -ForegroundColor White
Write-Host "`nTo check service health, run:" -ForegroundColor Cyan
Write-Host "  .\check-services.ps1" -ForegroundColor White
Write-Host ""
