# Test Gateway Routing

Write-Host "=== Testing API Gateway Routing ===" -ForegroundColor Cyan
Write-Host ""

$loginBody = @{
    email = "admin@revcart.com"
    password = "admin123"
} | ConvertTo-Json

# Test 1: Through Gateway
Write-Host "1. Testing through Gateway (port 8080)..." -ForegroundColor Yellow
try {
    $gatewayResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    $data = $gatewayResponse.Content | ConvertFrom-Json
    Write-Host "   SUCCESS: Gateway routing works!" -ForegroundColor Green
    Write-Host "   Response: $($data.message)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
    
    if ($statusCode -eq 400 -and $errorBody.message -eq "Invalid email or password") {
        Write-Host "   Gateway routing WORKS (forwarded to user-service)" -ForegroundColor Green
        Write-Host "   But: $($errorBody.message)" -ForegroundColor Yellow
        Write-Host "   This means: Admin user doesn't exist in database" -ForegroundColor Yellow
    } else {
        Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 2: Direct to User-Service
Write-Host "2. Testing direct to User-Service (port 8081)..." -ForegroundColor Yellow
try {
    $directResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    $data = $directResponse.Content | ConvertFrom-Json
    Write-Host "   SUCCESS: User-service works!" -ForegroundColor Green
    Write-Host "   Response: $($data.message)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
    
    if ($statusCode -eq 400 -and $errorBody.message -eq "Invalid email or password") {
        Write-Host "   User-service WORKS" -ForegroundColor Green
        Write-Host "   But: $($errorBody.message)" -ForegroundColor Yellow
    } else {
        Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Conclusion ===" -ForegroundColor Cyan
Write-Host "Gateway routing is CORRECT if both tests show same error." -ForegroundColor White
Write-Host ""
Write-Host "To fix login, run this SQL in MySQL:" -ForegroundColor Yellow
Write-Host ""
Write-Host "USE revcart_users;" -ForegroundColor White
Write-Host "DELETE FROM users WHERE email = 'admin@revcart.com';" -ForegroundColor White
Write-Host "INSERT INTO users (email, password, name, phone, role, active, created_at, updated_at)" -ForegroundColor White
Write-Host "VALUES ('admin@revcart.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', '9999999999', 'ADMIN', 1, NOW(), NOW());" -ForegroundColor White
