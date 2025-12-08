# Test API Integration Script

Write-Host "=== Testing RevCart API Integration ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Products API
Write-Host "1. Testing Products API..." -ForegroundColor Yellow
try {
    $products = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -ErrorAction Stop
    if ($products.success -and $products.data) {
        Write-Host "   SUCCESS: Products API working" -ForegroundColor Green
        Write-Host "   Found $($products.data.Count) products" -ForegroundColor Green
    } else {
        Write-Host "   FAILED: Invalid response structure" -ForegroundColor Red
    }
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Categories API
Write-Host "2. Testing Categories API..." -ForegroundColor Yellow
try {
    $categories = Invoke-RestMethod -Uri "http://localhost:8080/api/categories" -ErrorAction Stop
    if ($categories.success -and $categories.data) {
        Write-Host "   SUCCESS: Categories API working" -ForegroundColor Green
        Write-Host "   Found $($categories.data.Count) categories" -ForegroundColor Green
    } else {
        Write-Host "   FAILED: Invalid response structure" -ForegroundColor Red
    }
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Login API
Write-Host "3. Testing Login API..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "admin@revcart.com"
        password = "admin123"
    } | ConvertTo-Json

    $login = Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    if ($login.success -and $login.data.token) {
        Write-Host "   SUCCESS: Login API working" -ForegroundColor Green
        Write-Host "   User: $($login.data.name) ($($login.data.role))" -ForegroundColor Green
    } else {
        Write-Host "   FAILED: Invalid response structure" -ForegroundColor Red
    }
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Make sure admin user exists in database" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Register API
Write-Host "4. Testing Register API..." -ForegroundColor Yellow
try {
    $registerBody = @{
        email = "testuser_$(Get-Random)@example.com"
        password = "test123"
        name = "Test User"
        phone = "1234567890"
    } | ConvertTo-Json

    $register = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop

    if ($register.success -and $register.data.token) {
        Write-Host "   SUCCESS: Register API working" -ForegroundColor Green
        Write-Host "   New user: $($register.data.name)" -ForegroundColor Green
    } else {
        Write-Host "   FAILED: Invalid response structure" -ForegroundColor Red
    }
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. If all tests pass, refresh frontend (Ctrl+F5)" -ForegroundColor White
Write-Host "2. Login with admin@revcart.com / admin123" -ForegroundColor White
Write-Host "3. Products should load from database" -ForegroundColor White
