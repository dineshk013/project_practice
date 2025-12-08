# RevCart - Stop All Services Script

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Stopping RevCart Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Stop all Java processes (backend services)
Write-Host "Stopping backend services (Java)..." -NoNewline
try {
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        $javaProcesses | Stop-Process -Force
        Write-Host " ✓ Stopped $($javaProcesses.Count) Java process(es)" -ForegroundColor Green
    } else {
        Write-Host " No Java processes running" -ForegroundColor Yellow
    }
} catch {
    Write-Host " ✗ Error stopping Java processes" -ForegroundColor Red
}

# Stop Node.js (frontend)
Write-Host "Stopping frontend (Node.js)..." -NoNewline
try {
    $nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
    if ($nodeProcesses) {
        $nodeProcesses | Stop-Process -Force
        Write-Host " ✓ Stopped $($nodeProcesses.Count) Node process(es)" -ForegroundColor Green
    } else {
        Write-Host " No Node.js processes running" -ForegroundColor Yellow
    }
} catch {
    Write-Host " ✗ Error stopping Node.js processes" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services stopped!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan
