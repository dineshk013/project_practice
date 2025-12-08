# Test Email OTP Service

Write-Host "=== Testing RevCart Email OTP Service ===" -ForegroundColor Cyan
Write-Host ""

# Prompt for email
$testEmail = Read-Host "Enter your email address to test OTP"

Write-Host ""
Write-Host "Registering user and sending OTP email..." -ForegroundColor Yellow

# Register user
$registerBody = @{
    email = $testEmail
    password = "test123"
    name = "Test User"
    phone = "1234567890"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    Write-Host ""
    Write-Host "SUCCESS! User registered!" -ForegroundColor Green
    Write-Host "Check your email ($testEmail) for OTP!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Email Details:" -ForegroundColor Cyan
    Write-Host "  Subject: RevCart - Email Verification OTP" -ForegroundColor White
    Write-Host "  From: RevCart" -ForegroundColor White
    Write-Host "  OTP: 6-digit code (valid for 5 minutes)" -ForegroundColor White
    Write-Host ""
    Write-Host "If you don't see it, check your spam/junk folder!" -ForegroundColor Yellow
    
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Details: $($error.message)" -ForegroundColor Yellow
    }
}
