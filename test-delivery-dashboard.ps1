# Test Delivery Agent Dashboard
Write-Host "=== Testing Delivery Agent Dashboard ===" -ForegroundColor Cyan

# Test Agent 7
Write-Host "`n1. Testing Agent 7 (ID: 7)" -ForegroundColor Yellow
Write-Host "Assigned Orders:" -ForegroundColor Green
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/assigned | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`nIn-Transit Orders:" -ForegroundColor Green
curl -H "X-User-Id: 7" http://localhost:8080/api/delivery/orders/in-transit | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Test Agent 17
Write-Host "`n2. Testing Agent 17 (ID: 17)" -ForegroundColor Yellow
Write-Host "Assigned Orders:" -ForegroundColor Green
curl -H "X-User-Id: 17" http://localhost:8080/api/delivery/orders/assigned | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`nIn-Transit Orders:" -ForegroundColor Green
curl -H "X-User-Id: 17" http://localhost:8080/api/delivery/orders/in-transit | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Test Agent 1
Write-Host "`n3. Testing Agent 1 (ID: 1)" -ForegroundColor Yellow
Write-Host "Assigned Orders:" -ForegroundColor Green
curl -H "X-User-Id: 1" http://localhost:8080/api/delivery/orders/assigned | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`nIn-Transit Orders:" -ForegroundColor Green
curl -H "X-User-Id: 1" http://localhost:8080/api/delivery/orders/in-transit | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Pending Orders
Write-Host "`n4. Pending Orders (Not Yet Assigned)" -ForegroundColor Yellow
curl http://localhost:8080/api/delivery/orders/pending | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
