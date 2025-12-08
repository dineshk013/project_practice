# RevCart - Required MySQL Databases

## MySQL Databases (5 databases)

1. **revcart_users** - User Service
2. **revcart_products** - Product Service  
3. **revcart_cart** - Cart Service
4. **revcart_orders** - Order Service
5. **revcart_payments** - Payment Service

## MongoDB Databases (3 databases)

1. **revcart_notifications** - Notification Service
2. **revcart_delivery** - Delivery Service
3. **revcart_analytics** - Analytics Service

---

## Check Your Databases

### MySQL
```sql
-- Show all databases
SHOW DATABASES;

-- Should see:
-- revcart_users
-- revcart_products
-- revcart_cart
-- revcart_orders
-- revcart_payments
```

### MongoDB
```javascript
// Show all databases
show dbs

// Should see:
// revcart_notifications
// revcart_delivery
// revcart_analytics
```

---

## Create Missing MySQL Databases

```sql
CREATE DATABASE IF NOT EXISTS revcart_users;
CREATE DATABASE IF NOT EXISTS revcart_products;
CREATE DATABASE IF NOT EXISTS revcart_cart;
CREATE DATABASE IF NOT EXISTS revcart_orders;
CREATE DATABASE IF NOT EXISTS revcart_payments;

SHOW DATABASES;
```

---

## Create Missing MongoDB Databases

```javascript
// MongoDB databases are created automatically when first used
// But you can verify by connecting to each:

use revcart_notifications
use revcart_delivery
use revcart_analytics

show dbs
```

---

## Quick Check Script (PowerShell)

```powershell
# Check MySQL databases
Write-Host "MySQL Databases:" -ForegroundColor Cyan
mysql -u root -p -e "SHOW DATABASES LIKE 'revcart_%';"

# Check MongoDB databases
Write-Host "`nMongoDB Databases:" -ForegroundColor Cyan
mongosh --eval "db.adminCommand('listDatabases').databases.filter(d => d.name.startsWith('revcart_')).forEach(d => print(d.name))"
```
