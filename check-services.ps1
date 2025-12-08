# RevCart - Service Health Check Script

$services = @(
    @{Name="Gateway"; Port=8080},
    @{Name="User Service"; Port=8081},
    @{Name="Product Service"; Port=8082},
    @{Name="Cart Service"; Port=8083},
    @{Name="Order Service"; Port=8084},
    @{Name="Payment Service"; Port=8085},
    @{Name="Notification Service"; Port=8086},
    @{Name="Delivery Service"; Port=8087},
    @{Name="Analytics Service"; Port=8088}
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RevCart Services Health Check" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$runningCount = 0
$totalCount = $services.Count

foreach ($service in $services) {
    Write-Host "Checking $($service.Name) (Port $($service.Port))... " -NoNewline
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "RUNNING" -ForegroundColor Green
            $runningCount++
        } else {
            Write-Host "UNHEALTHY" -ForegroundColor Red
        }
    } catch {
        Write-Host "NOT RUNNING" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Status: $runningCount/$totalCount services running" -ForegroundColor $(if ($runningCount -eq $totalCount) { "Green" } else { "Yellow" })
Write-Host "========================================`n" -ForegroundColor Cyan

# Check Frontend
Write-Host "Checking Frontend (Port 4200)... " -NoNewline
try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:4200" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "RUNNING" -ForegroundColor Green
    }
} catch {
    Write-Host "NOT RUNNING" -ForegroundColor Red
}

Write-Host ""

if ($runningCount -eq $totalCount) {
    Write-Host "All services are healthy!" -ForegroundColor Green
    Write-Host "Frontend: http://localhost:4200" -ForegroundColor Cyan
    Write-Host "Gateway:  http://localhost:8080" -ForegroundColor Cyan
} else {
    Write-Host "Some services are not running" -ForegroundColor Yellow
    Write-Host "Check the terminal windows for error messages" -ForegroundColor Yellow
}

Write-Host ""
