# Test login directly to user service (bypass gateway)

Write-Host "Testing login directly to user-service..." -ForegroundColor Cyan

$loginBody = @{
    email = "admin@revcart.com"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Request body: $loginBody" -ForegroundColor Yellow

# Test direct to user service
Write-Host "`n1. Testing user-service directly (port 8081)..." -ForegroundColor Yellow
try {
    $directLogin = Invoke-RestMethod -Uri "http://localhost:8081/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    Write-Host "   SUCCESS on user-service" -ForegroundColor Green
    Write-Host "   User: $($directLogin.data.name)" -ForegroundColor Green
} catch {
    Write-Host "   FAILED on user-service" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test through gateway
Write-Host "`n2. Testing through gateway (port 8080)..." -ForegroundColor Yellow
try {
    $gatewayLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    Write-Host "   SUCCESS through gateway" -ForegroundColor Green
    Write-Host "   User: $($gatewayLogin.data.name)" -ForegroundColor Green
} catch {
    Write-Host "   FAILED through gateway" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host "`nNext: Run verify-admin.sql in MySQL to recreate admin user" -ForegroundColor Yellow
