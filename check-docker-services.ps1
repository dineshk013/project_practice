Write-Host "=== RevCart Docker Services Health Check ===" -ForegroundColor Cyan
Write-Host ""

# Check Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
$dockerRunning = docker info 2>$null
if (-not $dockerRunning) {
    Write-Host "❌ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker is running" -ForegroundColor Green
Write-Host ""

# Check containers
Write-Host "Container Status:" -ForegroundColor Yellow
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Out-String
Write-Host ""

# Test service endpoints
$services = @(
    @{Name="Gateway"; Port=8080; Path="/actuator/health"},
    @{Name="User Service"; Port=8081; Path="/actuator/health"},
    @{Name="Product Service"; Port=8082; Path="/actuator/health"},
    @{Name="Cart Service"; Port=8083; Path="/actuator/health"},
    @{Name="Order Service"; Port=8084; Path="/actuator/health"},
    @{Name="Payment Service"; Port=8085; Path="/actuator/health"},
    @{Name="Notification Service"; Port=8086; Path="/actuator/health"},
    @{Name="Delivery Service"; Port=8087; Path="/actuator/health"},
    @{Name="Analytics Service"; Port=8088; Path="/actuator/health"},
    @{Name="Frontend"; Port=4200; Path="/"}
)

Write-Host "Service Health:" -ForegroundColor Yellow
foreach ($service in $services) {
    $url = "http://localhost:$($service.Port)$($service.Path)"
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $($service.Name) (Port $($service.Port))" -ForegroundColor Green
        } else {
            Write-Host "⚠️  $($service.Name) (Port $($service.Port)) - Status: $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $($service.Name) (Port $($service.Port)) - Not responding" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Check Complete ===" -ForegroundColor Cyan
