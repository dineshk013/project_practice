# Test active users endpoint directly
Write-Host "Testing active users endpoint..." -ForegroundColor Cyan

# Test the endpoint
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/count/active" -Method Get
Write-Host "Active users count: $response" -ForegroundColor Green

# Test dashboard stats
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard/stats" -Method Get
Write-Host "Dashboard stats:" -ForegroundColor Yellow
$stats | ConvertTo-Json -Depth 3
