# ============================================
# RevCart Complete System Test Suite
# ============================================
# Tests all critical flows end-to-end
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RevCart System Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$userId = 14
$testEmail = "test@example.com"
$testResults = @()

# ============================================
# Helper Functions
# ============================================

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "Testing: $Name..." -NoNewline
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        Write-Host " PASS" -ForegroundColor Green
        return @{ Success = $true; Response = $response }
    }
    catch {
        Write-Host " FAIL" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

# ============================================
# Test 1: Service Health Checks
# ============================================

Write-Host "`n[1] Service Health Checks" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$services = @(
    @{ Name = "Gateway"; Port = 8080 },
    @{ Name = "User Service"; Port = 8081 },
    @{ Name = "Product Service"; Port = 8082 },
    @{ Name = "Cart Service"; Port = 8083 },
    @{ Name = "Order Service"; Port = 8084 },
    @{ Name = "Payment Service"; Port = 8085 },
    @{ Name = "Notification Service"; Port = 8086 },
    @{ Name = "Delivery Service"; Port = 8087 },
    @{ Name = "Analytics Service"; Port = 8088 }
)

foreach ($service in $services) {
    $url = "http://localhost:$($service.Port)/actuator/health"
    $result = Test-Endpoint -Name $service.Name -Method "GET" -Url $url
    $testResults += @{ Test = "Health: $($service.Name)"; Result = $result.Success }
}

# ============================================
# Test 2: User Registration & Login
# ============================================

Write-Host "`n[2] User Authentication" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Register user
$registerBody = @{
    name = "Test User"
    email = $testEmail
    password = "test123"
    phone = "1234567890"
} | ConvertTo-Json

$result = Test-Endpoint `
    -Name "User Registration" `
    -Method "POST" `
    -Url "$baseUrl/api/users/register" `
    -Body $registerBody

$testResults += @{ Test = "User Registration"; Result = $result.Success }

# Login
$loginBody = @{
    email = $testEmail
    password = "test123"
} | ConvertTo-Json

$result = Test-Endpoint `
    -Name "User Login" `
    -Method "POST" `
    -Url "$baseUrl/api/users/login" `
    -Body $loginBody

$testResults += @{ Test = "User Login"; Result = $result.Success }

if ($result.Success) {
    $token = $result.Response.data.token
    $userId = $result.Response.data.user.id
    Write-Host "  Token obtained: $($token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "  User ID: $userId" -ForegroundColor Gray
}

# ============================================
# Test 3: Product Catalog
# ============================================

Write-Host "`n[3] Product Catalog" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Get products
$result = Test-Endpoint `
    -Name "Get Products" `
    -Method "GET" `
    -Url "$baseUrl/api/products"

$testResults += @{ Test = "Get Products"; Result = $result.Success }

if ($result.Success -and $result.Response.data.content) {
    $productId = $result.Response.data.content[0].id
    Write-Host "  Product ID for testing: $productId" -ForegroundColor Gray
}
else {
    $productId = 1
    Write-Host "  Using default product ID: $productId" -ForegroundColor Gray
}

# Get product by ID
$result = Test-Endpoint `
    -Name "Get Product by ID" `
    -Method "GET" `
    -Url "$baseUrl/api/products/$productId"

$testResults += @{ Test = "Get Product by ID"; Result = $result.Success }

# ============================================
# Test 4: Cart Operations
# ============================================

Write-Host "`n[4] Cart Operations" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$headers = @{
    "X-User-Id" = $userId.ToString()
}

# Get empty cart
$result = Test-Endpoint `
    -Name "Get Empty Cart" `
    -Method "GET" `
    -Url "$baseUrl/api/cart" `
    -Headers $headers

$testResults += @{ Test = "Get Empty Cart"; Result = $result.Success }

# Add item to cart
$addToCartBody = @{
    productId = $productId
    quantity = 2
} | ConvertTo-Json

$result = Test-Endpoint `
    -Name "Add Item to Cart" `
    -Method "POST" `
    -Url "$baseUrl/api/cart/items" `
    -Headers $headers `
    -Body $addToCartBody

$testResults += @{ Test = "Add Item to Cart"; Result = $result.Success }

if ($result.Success) {
    $cartItemId = $result.Response.data.items[0].id
    Write-Host "  Cart Item ID: $cartItemId" -ForegroundColor Gray
}

# Get cart with items
$result = Test-Endpoint `
    -Name "Get Cart with Items" `
    -Method "GET" `
    -Url "$baseUrl/api/cart" `
    -Headers $headers

$testResults += @{ Test = "Get Cart with Items"; Result = $result.Success }

if ($result.Success) {
    $itemCount = $result.Response.data.totalItems
    Write-Host "  Cart has $itemCount items" -ForegroundColor Gray
}

# Get cart count
$result = Test-Endpoint `
    -Name "Get Cart Count" `
    -Method "GET" `
    -Url "$baseUrl/api/cart/count" `
    -Headers $headers

$testResults += @{ Test = "Get Cart Count"; Result = $result.Success }

# ============================================
# Test 5: Address Management
# ============================================

Write-Host "`n[5] Address Management" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Add address
$addressBody = @{
    line1 = "123 Test Street"
    city = "Test City"
    state = "Test State"
    postalCode = "12345"
    country = "India"
    primaryAddress = $true
} | ConvertTo-Json

$result = Test-Endpoint `
    -Name "Add Address" `
    -Method "POST" `
    -Url "$baseUrl/api/profile/addresses" `
    -Headers $headers `
    -Body $addressBody

$testResults += @{ Test = "Add Address"; Result = $result.Success }

if ($result.Success) {
    $addressId = $result.Response.data.id
    Write-Host "  Address ID: $addressId" -ForegroundColor Gray
}
else {
    $addressId = 1
}

# Get addresses
$result = Test-Endpoint `
    -Name "Get Addresses" `
    -Method "GET" `
    -Url "$baseUrl/api/profile/addresses" `
    -Headers $headers

$testResults += @{ Test = "Get Addresses"; Result = $result.Success }

# ============================================
# Test 6: Checkout & Order Creation
# ============================================

Write-Host "`n[6] Checkout & Order Creation" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Checkout
$checkoutBody = @{
    addressId = $addressId
    paymentMethod = "COD"
} | ConvertTo-Json

$result = Test-Endpoint `
    -Name "Checkout" `
    -Method "POST" `
    -Url "$baseUrl/api/orders/checkout" `
    -Headers $headers `
    -Body $checkoutBody

$testResults += @{ Test = "Checkout"; Result = $result.Success }

if ($result.Success) {
    $orderId = $result.Response.data.id
    $orderNumber = $result.Response.data.orderNumber
    Write-Host "  Order ID: $orderId" -ForegroundColor Gray
    Write-Host "  Order Number: $orderNumber" -ForegroundColor Gray
}

# ============================================
# Test 7: Order Retrieval
# ============================================

Write-Host "`n[7] Order Retrieval" -ForegroundColor Yellow
Write-Host "----------------------------------------"

# Get user orders
$result = Test-Endpoint `
    -Name "Get User Orders" `
    -Method "GET" `
    -Url "$baseUrl/api/orders" `
    -Headers $headers

$testResults += @{ Test = "Get User Orders"; Result = $result.Success }

if ($result.Success) {
    $orderCount = $result.Response.data.Count
    Write-Host "  User has $orderCount orders" -ForegroundColor Gray
}

# Get order by ID
if ($orderId) {
    $result = Test-Endpoint `
        -Name "Get Order by ID" `
        -Method "GET" `
        -Url "$baseUrl/api/orders/$orderId" `
        -Headers $headers
    
    $testResults += @{ Test = "Get Order by ID"; Result = $result.Success }
}

# ============================================
# Test 8: Database Verification
# ============================================

Write-Host "`n[8] Database Verification" -ForegroundColor Yellow
Write-Host "----------------------------------------"

Write-Host "Checking database tables..." -NoNewline

try {
    # Check cart_items
    $cartItemsCount = mysql -u root -pMahidinesh@07 -e "USE revcart_carts; SELECT COUNT(*) FROM cart_items;" -N 2>$null
    Write-Host " PASS" -ForegroundColor Green
    Write-Host "  cart_items count: $cartItemsCount" -ForegroundColor Gray
    
    # Check orders
    $ordersCount = mysql -u root -pMahidinesh@07 -e "USE revcart_orders; SELECT COUNT(*) FROM orders;" -N 2>$null
    Write-Host "  orders count: $ordersCount" -ForegroundColor Gray
    
    $testResults += @{ Test = "Database Verification"; Result = $true }
}
catch {
    Write-Host " FAIL" -ForegroundColor Red
    Write-Host "  MySQL command not available or connection failed" -ForegroundColor Red
    $testResults += @{ Test = "Database Verification"; Result = $false }
}

# ============================================
# Test 9: WebSocket Connection
# ============================================

Write-Host "`n[9] WebSocket Connection" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$result = Test-Endpoint `
    -Name "WebSocket Info Endpoint" `
    -Method "GET" `
    -Url "$baseUrl/ws/info"

$testResults += @{ Test = "WebSocket Info"; Result = $result.Success }

# ============================================
# Test 10: Notifications
# ============================================

Write-Host "`n[10] Notifications" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$result = Test-Endpoint `
    -Name "Get Notifications" `
    -Method "GET" `
    -Url "$baseUrl/api/notifications" `
    -Headers $headers

$testResults += @{ Test = "Get Notifications"; Result = $result.Success }

# ============================================
# Test Summary
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Result -eq $true }).Count
$failCount = ($testResults | Where-Object { $_.Result -eq $false }).Count
$totalCount = $testResults.Count

Write-Host "`nTotal Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red

Write-Host "`nDetailed Results:" -ForegroundColor White
foreach ($test in $testResults) {
    $status = if ($test.Result) { "PASS" } else { "FAIL" }
    $color = if ($test.Result) { "Green" } else { "Red" }
    Write-Host "  [$status] $($test.Test)" -ForegroundColor $color
}

# ============================================
# Recommendations
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Recommendations" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($failCount -eq 0) {
    Write-Host "`n✅ All tests passed! System is working correctly." -ForegroundColor Green
}
else {
    Write-Host "`n⚠️  Some tests failed. Check the following:" -ForegroundColor Yellow
    Write-Host "  1. Ensure all services are running" -ForegroundColor White
    Write-Host "  2. Check service logs for errors" -ForegroundColor White
    Write-Host "  3. Verify database connections" -ForegroundColor White
    Write-Host "  4. Review CRITICAL_FIXES_PACKAGE.md" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
