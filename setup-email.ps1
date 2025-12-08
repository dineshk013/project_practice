# RevCart Email Service Setup Script

Write-Host "=== RevCart Email Service Configuration ===" -ForegroundColor Cyan
Write-Host ""

# Set your Gmail credentials
$env:MAIL_USERNAME="mahidineshk@gmail.com"
$env:MAIL_PASSWORD="xsbbzneeteloiplo"

Write-Host "Email credentials configured:" -ForegroundColor Green
Write-Host "  Username: $env:MAIL_USERNAME" -ForegroundColor White
Write-Host "  Password: ****************" -ForegroundColor White
Write-Host ""

Write-Host "Starting user-service with email support..." -ForegroundColor Yellow
Write-Host ""

# Navigate to user-service and start
cd user-service
mvn spring-boot:run
