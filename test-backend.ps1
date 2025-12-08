# Simple Backend Test Script

Write-Host "Testing Backend Services..." -ForegroundColor Cyan
Write-Host ""

# Test Gateway
Write-Host "1. Testing Gateway (8080)..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test User Service
Write-Host "2. Testing User Service (8081)..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 3
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test Product Service
Write-Host "3. Testing Product Service (8082)..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -TimeoutSec 3
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test if we can get products through gateway
Write-Host ""
Write-Host "4. Testing API through Gateway..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -TimeoutSec 3
    Write-Host " OK" -ForegroundColor Green
    Write-Host "   Found $($response.data.Count) products" -ForegroundColor Cyan
} catch {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Check complete!" -ForegroundColor Cyan
