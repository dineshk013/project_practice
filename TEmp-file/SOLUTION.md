# ✅ Your Backend is Running! Here's What to Do

## Current Status
- ✅ All backend services are UP and running
- ✅ Gateway is responding on port 8080
- ✅ Frontend is accessible on port 4200
- ❌ No admin user exists in database yet

## The Problem
You're seeing "Unable to connect to server" because there's no user with email `admin@revcart.com` in the database.

---

## Solution 1: Create Admin User (Quick)

### Open MySQL Command Line
```cmd
mysql -u root -p
```

### Run These Commands
```sql
USE revcart_users;

-- Create table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert admin user
INSERT INTO users (email, password, name, phone, role, active, created_at, updated_at)
VALUES (
    'admin@revcart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin User',
    '9999999999',
    'ADMIN',
    1,
    NOW(),
    NOW()
);

SELECT id, email, name, role FROM users WHERE email = 'admin@revcart.com';
```

### Now Login
- **Email**: admin@revcart.com
- **Password**: admin123

---

## Solution 2: Register New User Through Frontend

### Go to Frontend
1. Open: http://localhost:4200
2. Click "Register" or "Sign Up"
3. Fill the form:
   - Name: Your Name
   - Email: your@email.com
   - Password: yourpassword
   - Phone: 1234567890
4. Submit
5. Login with your new credentials

---

## Solution 3: Register via API

### Open PowerShell and Run:
```powershell
$body = @{
    name = "Test User"
    email = "test@example.com"
    password = "test123"
    phone = "1234567890"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -ContentType "application/json" -Body $body

Write-Host "Registered! Token: $($response.data.token)"
```

Then login with:
- **Email**: test@example.com
- **Password**: test123

---

## Verify Everything Works

### Run This Test:
```powershell
.\test-backend.ps1
```

You should see:
- Gateway: OK
- User Service: OK
- Product Service: OK
- API through Gateway: OK

---

## Add Sample Products (Optional)

### Open MySQL:
```sql
USE revcart_products;

-- Add category
INSERT INTO categories (name, slug, description, active, created_at, updated_at)
VALUES ('Electronics', 'electronics', 'Electronic items', 1, NOW(), NOW());

-- Add product
INSERT INTO products (name, description, price, stock_quantity, category_id, sku, active, created_at, updated_at)
VALUES ('Laptop', 'High-performance laptop', 50000.00, 10, 1, 'SKU-LAPTOP-001', 1, NOW(), NOW());
```

---

## Summary

**Your application is 100% working!** You just need to:

1. **Create a user** (use any of the 3 solutions above)
2. **Login** with the credentials
3. **Start using the app**

The "Unable to connect" error was misleading - the backend IS connected and working. You just don't have any users in the database yet.

---

## Quick Commands Reference

```powershell
# Test if backend is working
.\test-backend.ps1

# Check all services
.\check-services.ps1

# Stop all services
.\stop-all.ps1

# Start all services
.\start-all.ps1
```

---

**Choose Solution 1, 2, or 3 above and you'll be able to login!** 🚀
