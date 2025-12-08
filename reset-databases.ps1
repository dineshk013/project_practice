# Reset All Databases - Fresh Start
# This script will delete all data from MySQL and MongoDB databases

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DATABASE RESET SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "WARNING: This will DELETE ALL DATA from:" -ForegroundColor Red
Write-Host "  - MySQL databases (user_service, product_service, cart_service, order_service, payment_service)" -ForegroundColor Yellow
Write-Host "  - MongoDB databases (notification_service, delivery_service, analytics_service)" -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Are you sure you want to continue? Type 'YES' to confirm"

if ($confirmation -ne "YES") {
    Write-Host "Operation cancelled." -ForegroundColor Green
    exit
}

Write-Host ""
Write-Host "Starting database reset..." -ForegroundColor Cyan

# MySQL Configuration
$mysqlUser = "root"
$mysqlPassword = "root"
$mysqlHost = "localhost"
$mysqlPort = "3306"
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# MongoDB Configuration
$mongoHost = "localhost"
$mongoPort = "27017"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 1: Resetting MySQL Databases" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$mysqlDatabases = @(
    "revcart_users",
    "revcart_products", 
    "revcart_cart",
    "revcart_orders",
    "revcart_payments"
)

foreach ($db in $mysqlDatabases) {
    Write-Host ""
    Write-Host "Resetting MySQL database: $db" -ForegroundColor Yellow
    
    # Drop and recreate database
    $sqlCommands = @"
DROP DATABASE IF EXISTS $db;
CREATE DATABASE $db;
"@
    
    $sqlCommands | & $mysqlPath -u$mysqlUser -p$mysqlPassword -h$mysqlHost -P$mysqlPort 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] $db reset successfully" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Failed to reset $db" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 2: Resetting MongoDB Databases" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$mongoDatabases = @(
    "notification_service",
    "delivery_service",
    "analytics_service"
)

foreach ($db in $mongoDatabases) {
    Write-Host ""
    Write-Host "Resetting MongoDB database: $db" -ForegroundColor Yellow
    
    # Drop database using mongosh
    $mongoCommand = "use $db`; db.dropDatabase()`;"
    
    echo $mongoCommand | mongosh --quiet --host $mongoHost --port $mongoPort 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] $db reset successfully" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Failed to reset $db" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DATABASE RESET COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "All databases have been reset to fresh state." -ForegroundColor Green
Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. Restart all microservices (they will auto-create tables)" -ForegroundColor White
Write-Host "2. Use .\start-all.ps1 to start all services" -ForegroundColor White
Write-Host "3. Wait for services to initialize (2-3 minutes)" -ForegroundColor White
Write-Host "4. Register new users and start fresh" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
