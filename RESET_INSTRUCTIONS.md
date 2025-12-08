# Quick Database Reset Instructions

## ✅ MongoDB Reset (Already Done!)
Your MongoDB databases were successfully reset:
- notification_service ✓
- delivery_service ✓
- analytics_service ✓

## 🔧 MySQL Reset (Choose One Method)

### Method 1: MySQL Workbench (Easiest)
1. Open MySQL Workbench
2. Connect to localhost
3. Click "SQL" tab and paste:
```sql
DROP DATABASE IF EXISTS revcart_users;
CREATE DATABASE revcart_users;

DROP DATABASE IF EXISTS revcart_products;
CREATE DATABASE revcart_products;

DROP DATABASE IF EXISTS revcart_cart;
CREATE DATABASE revcart_cart;

DROP DATABASE IF EXISTS revcart_orders;
CREATE DATABASE revcart_orders;

DROP DATABASE IF EXISTS revcart_payments;
CREATE DATABASE revcart_payments;
```
4. Click Execute (⚡ icon)

### Method 2: Command Line
```powershell
# Navigate to MySQL bin directory
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"

# Run MySQL
.\mysql.exe -uroot -proot

# Then paste these commands:
DROP DATABASE IF EXISTS revcart_users;
CREATE DATABASE revcart_users;
DROP DATABASE IF EXISTS revcart_products;
CREATE DATABASE revcart_products;
DROP DATABASE IF EXISTS revcart_cart;
CREATE DATABASE revcart_cart;
DROP DATABASE IF EXISTS revcart_orders;
CREATE DATABASE revcart_orders;
DROP DATABASE IF EXISTS revcart_payments;
CREATE DATABASE revcart_payments;
exit;
```

### Method 3: Using SQL File
```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
.\mysql.exe -uroot -proot < "C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices\reset-mysql-databases.sql"
```

## 🚀 After Reset

1. **Start all services:**
```powershell
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices
.\start-all.ps1
```

2. **Wait 2-3 minutes** for services to create tables

3. **Verify services:**
```powershell
.\check-services.ps1
```

4. **Clear browser data:**
   - Press F12 → Application → Clear Storage → Clear site data
   - Or just use Incognito/Private mode

5. **Start fresh:**
   - Register new users
   - Add products
   - Create orders

## ✅ What's Reset

### MySQL (Empty now)
- ✓ revcart_users
- ✓ revcart_products
- ✓ revcart_cart
- ✓ revcart_orders
- ✓ revcart_payments

### MongoDB (Already reset)
- ✓ notification_service
- ✓ delivery_service
- ✓ analytics_service

---

**Note**: Services will automatically create all tables when they start up!
