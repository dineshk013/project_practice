# Simple MySQL Reset Script
Write-Host "Resetting MySQL databases..." -ForegroundColor Cyan

$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$sqlFile = "reset-mysql-databases.sql"

if (Test-Path $mysqlPath) {
    & $mysqlPath -uroot -proot < $sqlFile
    Write-Host "[OK] MySQL databases reset successfully" -ForegroundColor Green
} else {
    Write-Host "[FAIL] MySQL not found at: $mysqlPath" -ForegroundColor Red
    Write-Host "Run this command manually:" -ForegroundColor Yellow
    Write-Host 'mysql -uroot -proot < reset-mysql-databases.sql' -ForegroundColor White
}
