# RevCart - Quick Start Guide

## ✅ Your Services Are Running!

Based on the check, your backend services are UP and running. The issue is that you need to:
1. Create an admin user in the database
2. Or register a new user through the frontend

---

## Option 1: Create Admin User (Recommended)

### Step 1: Run SQL Script
```powershell
# Connect to MySQL
mysql -u root -p

# Run the admin creation script
source create-admin.sql

# Or manually:
USE revcart_users;

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
```

### Step 2: Login
- **Email**: admin@revcart.com
- **Password**: admin123

---

## Option 2: Register New User

### Through Frontend (http://localhost:4200)
1. Click "Register" or "Sign Up"
2. Fill in the form:
   - Name: Your Name
   - Email: your@email.com
   - Password: yourpassword
   - Phone: 1234567890
3. Click Submit
4. Login with your credentials

### Through API (PowerShell)
```powershell
$body = @{
    name = "Test User"
    email = "test@example.com"
    password = "test123"
    phone = "1234567890"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

Write-Host "User registered successfully!"
Write-Host "Token: $($response.data.token)"
```

---

## Verify Backend is Working

### Run Test Script
```powershell
.\test-backend.ps1
```

### Manual Tests
```powershell
# Test Gateway
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"

# Test User Service
Invoke-RestMethod -Uri "http://localhost:8081/actuator/health"

# Test Products (should return empty list or products)
Invoke-RestMethod -Uri "http://localhost:8080/api/products"
```

---

## Add Sample Products

### Through API
```powershell
# First, create a category
$category = @{
    name = "Electronics"
    description = "Electronic items"
} | ConvertTo-Json

$catResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/categories" `
    -Method Post `
    -ContentType "application/json" `
    -Body $category

# Then, create a product
$product = @{
    name = "Laptop"
    description = "High-performance laptop"
    price = 50000.00
    stockQuantity = 10
    categoryId = 1
    imageUrl = "https://example.com/laptop.jpg"
    active = $true
} | ConvertTo-Json

$prodResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/products" `
    -Method Post `
    -ContentType "application/json" `
    -Body $product

Write-Host "Product created successfully!"
```

### Through SQL
```sql
USE revcart_products;

-- Insert category
INSERT INTO categories (name, slug, description, active, created_at, updated_at)
VALUES ('Electronics', 'electronics', 'Electronic items', 1, NOW(), NOW());

-- Insert product
INSERT INTO products (name, description, price, stock_quantity, category_id, sku, active, created_at, updated_at)
VALUES (
    'Laptop',
    'High-performance laptop',
    50000.00,
    10,
    1,
    'SKU-LAPTOP-001',
    1,
    NOW(),
    NOW()
);
```

---

## Access URLs

### Frontend
- **URL**: http://localhost:4200
- **Features**: Browse products, register, login, cart, checkout

### Backend API Gateway
- **URL**: http://localhost:8080
- **Health**: http://localhost:8080/actuator/health

### Individual Services
- User Service: http://localhost:8081
- Product Service: http://localhost:8082
- Cart Service: http://localhost:8083
- Order Service: http://localhost:8084
- Payment Service: http://localhost:8085
- Notification Service: http://localhost:8086
- Delivery Service: http://localhost:8087
- Analytics Service: http://localhost:8088

---

## Common Issues & Solutions

### Issue: "Unable to connect to server"

**Cause**: Backend services not fully started or database connection issue

**Solution**:
```powershell
# 1. Check if services are running
.\test-backend.ps1

# 2. Check MySQL is running
net start MySQL80

# 3. Check MongoDB is running
net start MongoDB

# 4. Restart services if needed
.\stop-all.ps1
.\start-all.ps1
```

### Issue: "Invalid credentials"

**Cause**: User doesn't exist in database

**Solution**:
1. Create admin user using `create-admin.sql`
2. Or register a new user through frontend
3. Or use the registration API

### Issue: Frontend shows blank page

**Cause**: Frontend not started or build error

**Solution**:
```powershell
cd Frontend
npm install
npm start
```

### Issue: Products not showing

**Cause**: No products in database

**Solution**:
1. Add products using SQL script above
2. Or use the API to create products
3. Or create through admin panel (if available)

---

## Complete E2E Test Flow

### 1. Register User
```powershell
$register = @{
    name = "John Doe"
    email = "john@example.com"
    password = "password123"
    phone = "1234567890"
} | ConvertTo-Json

$user = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $register

$token = $user.data.token
$userId = $user.data.user.id
Write-Host "User ID: $userId"
Write-Host "Token: $token"
```

### 2. Add Address
```powershell
$address = @{
    street = "123 Main St"
    city = "Mumbai"
    state = "Maharashtra"
    zipCode = "400001"
    country = "India"
} | ConvertTo-Json

$headers = @{
    "X-User-Id" = $userId
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/users/addresses" `
    -Method Post `
    -Headers $headers `
    -Body $address
```

### 3. Browse Products
```powershell
$products = Invoke-RestMethod -Uri "http://localhost:8080/api/products"
Write-Host "Found $($products.data.Count) products"
```

### 4. Add to Cart
```powershell
$cartItem = @{
    productId = 1
    quantity = 2
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/cart/items" `
    -Method Post `
    -Headers $headers `
    -Body $cartItem
```

### 5. Checkout
```powershell
$checkout = @{
    paymentMethod = "COD"
    addressId = 1
} | ConvertTo-Json

$order = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/checkout" `
    -Method Post `
    -Headers $headers `
    -Body $checkout

Write-Host "Order created: $($order.data.id)"
```

---

## Next Steps

1. ✅ **Backend is running** - All services are UP
2. ✅ **Frontend is accessible** - http://localhost:4200
3. ⚠️ **Create admin user** - Run `create-admin.sql`
4. ⚠️ **Add sample data** - Products, categories
5. ✅ **Test the application** - Register, browse, cart, checkout

---

## Quick Commands

```powershell
# Test backend
.\test-backend.ps1

# Check all services
.\check-services.ps1

# Stop all services
.\stop-all.ps1

# Start all services
.\start-all.ps1

# Create admin user
mysql -u root -p < create-admin.sql
```

---

## Success Checklist

- [x] MySQL running
- [x] MongoDB running
- [x] Backend services started (9 services)
- [x] Gateway responding (port 8080)
- [x] Frontend accessible (port 4200)
- [ ] Admin user created
- [ ] Sample products added
- [ ] Can login successfully
- [ ] Can browse products
- [ ] Can add to cart
- [ ] Can checkout

---

**Your backend is working! Just need to create users and add data.** 🎉
