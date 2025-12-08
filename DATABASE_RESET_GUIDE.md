# Database Reset Guide

## Quick Reset (Automated)

### Option 1: PowerShell Script (Recommended)
```powershell
.\reset-databases.ps1
```
This will:
- Drop and recreate all MySQL databases
- Drop all MongoDB databases
- Prompt for confirmation before proceeding

---

## Manual Reset

### MySQL Databases

#### Method 1: Using SQL Script
```powershell
mysql -uroot -proot < reset-mysql-databases.sql
```

#### Method 2: MySQL Command Line
```powershell
mysql -uroot -proot
```

Then execute:
```sql
DROP DATABASE IF EXISTS user_service;
CREATE DATABASE user_service;

DROP DATABASE IF EXISTS product_service;
CREATE DATABASE product_service;

DROP DATABASE IF EXISTS cart_service;
CREATE DATABASE cart_service;

DROP DATABASE IF EXISTS order_service;
CREATE DATABASE order_service;

DROP DATABASE IF EXISTS payment_service;
CREATE DATABASE payment_service;
```

#### Method 3: MySQL Workbench
1. Open MySQL Workbench
2. Connect to localhost
3. Right-click each database → Drop Schema
4. Create new schemas with same names

---

### MongoDB Databases

#### Method 1: Mongosh Command Line
```powershell
mongosh
```

Then execute:
```javascript
use notification_service;
db.dropDatabase();

use delivery_service;
db.dropDatabase();

use analytics_service;
db.dropDatabase();
```

#### Method 2: MongoDB Compass
1. Open MongoDB Compass
2. Connect to localhost:27017
3. Click each database → Drop Database

---

## After Reset

### 1. Stop All Services
```powershell
.\stop-all.ps1
```

### 2. Start All Services
```powershell
.\start-all.ps1
```

### 3. Wait for Initialization
- Services will auto-create tables/collections
- Wait 2-3 minutes for all services to start
- Check logs for "Started [ServiceName]Application"

### 4. Verify Services
```powershell
.\check-services.ps1
```

### 5. Create Fresh Data
- Register new users
- Add products
- Create orders
- Test all functionality

---

## What Gets Deleted

### MySQL (Transactional Data)
- **user_service**: All users, addresses, authentication data
- **product_service**: All products, categories, inventory
- **cart_service**: All shopping carts
- **order_service**: All orders, order items
- **payment_service**: All payment records

### MongoDB (Unstructured Data)
- **notification_service**: All notifications
- **delivery_service**: All delivery records
- **analytics_service**: All analytics data

---

## What Doesn't Get Deleted

- Application code
- Configuration files
- Logs
- Frontend localStorage (clear browser cache manually)

---

## Clear Frontend Data

### Clear Browser Storage
1. Open browser DevTools (F12)
2. Go to Application tab
3. Clear:
   - Local Storage
   - Session Storage
   - Cookies

Or simply:
```
Ctrl + Shift + Delete → Clear browsing data
```

---

## Troubleshooting

### MySQL Access Denied
```powershell
# Update password in script
$mysqlPassword = "your_password"
```

### MongoDB Connection Failed
```powershell
# Check if MongoDB is running
net start MongoDB

# Or check service status
Get-Service MongoDB
```

### Tables Not Created After Reset
- Ensure services are running
- Check application.properties for correct database URLs
- Look for errors in service logs
- Verify `spring.jpa.hibernate.ddl-auto=update` is set

### Services Won't Start
- Check if databases exist: `SHOW DATABASES;` in MySQL
- Verify database credentials in application.properties
- Check port availability (8080-8088)

---

## Database Credentials

### MySQL
- Host: localhost
- Port: 3306
- Username: root
- Password: root

### MongoDB
- Host: localhost
- Port: 27017
- No authentication (default)

---

## Safety Tips

1. **Backup First** (if needed):
   ```powershell
   mysqldump -uroot -proot --all-databases > backup.sql
   mongodump --out=mongodb_backup
   ```

2. **Stop Services Before Reset**: Prevents connection errors

3. **Confirm Twice**: The script asks for confirmation

4. **Test After Reset**: Run through complete user flow

---

## Quick Commands Reference

```powershell
# Full reset and restart
.\reset-databases.ps1
.\stop-all.ps1
.\start-all.ps1
.\check-services.ps1

# MySQL only
mysql -uroot -proot < reset-mysql-databases.sql

# MongoDB only
mongosh --eval "use notification_service; db.dropDatabase();"
mongosh --eval "use delivery_service; db.dropDatabase();"
mongosh --eval "use analytics_service; db.dropDatabase();"
```

---

## Expected Results

After successful reset:
- ✅ All databases empty
- ✅ Services start without errors
- ✅ Tables/collections auto-created
- ✅ No old data visible in UI
- ✅ Can register new users
- ✅ Can create new orders

---

**⚠️ WARNING**: This operation is irreversible. All data will be permanently deleted.
