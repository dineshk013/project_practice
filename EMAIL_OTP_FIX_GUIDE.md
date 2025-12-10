# Email OTP Verification Fix Guide

## Issues Found and Fixed

### 1. **Database Role Enum Mismatch**
**Problem**: Existing users have `USER` role but enum was changed to `CUSTOMER`
**Solution**: Run the SQL script to update existing roles

```sql
USE revcart_users;
UPDATE users SET role = 'CUSTOMER' WHERE role = 'USER';
```

### 2. **Email Configuration Issues**
**Problem**: SMTP configuration had incorrect YAML structure and short timeouts
**Solution**: Updated `application.yml` with proper configuration

**Fixed Configuration:**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:mahidineshk@gmail.com}
    password: ${MAIL_PASSWORD:qzrxujdvtpoesbhr}
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          ssl:
            trust: smtp.gmail.com
          connectiontimeout: 10000
          timeout: 10000
          writetimeout: 10000
```

## Steps to Fix Email OTP

### Step 1: Update Database Roles
Run the SQL script:
```bash
mysql -u root -p < fix_user_roles.sql
```

Or manually:
```sql
USE revcart_users;
UPDATE users SET role = 'CUSTOMER' WHERE role = 'USER';
SELECT id, email, role FROM users;
```

### Step 2: Verify Gmail App Password
1. Go to Google Account Settings
2. Navigate to Security > 2-Step Verification > App Passwords
3. Generate a new App Password for "Mail"
4. Update the password in `application.yml` or set environment variable:
   ```bash
   set MAIL_PASSWORD=your-16-char-app-password
   ```

### Step 3: Test Email Service
```bash
curl -X POST "http://localhost:8081/api/users/test-email?email=your-email@gmail.com"
```

Expected response:
```json
{
  "success": true,
  "message": "Check your inbox...",
  "data": "Test email sent to your-email@gmail.com"
}
```

### Step 4: Test OTP Generation
```bash
curl -X POST "http://localhost:8081/api/users/resend-otp?email=your-email@gmail.com"
```

Expected response:
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "data": null
}
```

## Common Issues and Solutions

### Issue 1: "No enum constant User.Role.USER"
**Cause**: Database has old `USER` role values
**Fix**: Run `fix_user_roles.sql` script

### Issue 2: Email takes too long or times out
**Cause**: SMTP timeout too short or network issues
**Fix**: 
- Increased timeouts to 10000ms
- Added SSL trust for smtp.gmail.com
- Check firewall/antivirus blocking port 587

### Issue 3: "Authentication failed"
**Cause**: Invalid Gmail App Password
**Fix**:
1. Ensure 2-Step Verification is enabled on Gmail
2. Generate new App Password (not regular password)
3. Use 16-character app password without spaces

### Issue 4: Email not received
**Cause**: Email in spam or Gmail blocking
**Fix**:
1. Check spam/junk folder
2. Add mahidineshk@gmail.com to contacts
3. Check Gmail "Less secure app access" settings
4. Verify App Password is correct

## Testing Checklist

- [ ] Database roles updated (USER → CUSTOMER)
- [ ] User service rebuilt with new configuration
- [ ] User service restarted
- [ ] Test email endpoint works
- [ ] OTP email received in inbox
- [ ] OTP verification works
- [ ] Registration flow completes successfully

## Email Configuration Best Practices

1. **Use Environment Variables** for sensitive data:
   ```bash
   set MAIL_USERNAME=your-email@gmail.com
   set MAIL_PASSWORD=your-app-password
   ```

2. **Enable Async Email Sending** (already configured with @Async)

3. **Add Retry Logic** for failed emails (future enhancement)

4. **Monitor Email Logs** in application logs:
   ```
   ✅ OTP email sent successfully to: user@example.com
   ❌ Failed to send OTP email to: user@example.com
   ```

## Verification Steps

1. **Register a new user**
2. **Check email for OTP** (should arrive within 30 seconds)
3. **Enter OTP in verification page**
4. **Confirm account is activated**

## Support

If issues persist:
1. Check application logs for detailed error messages
2. Verify Gmail account settings
3. Test with different email provider (if available)
4. Ensure no firewall blocking SMTP port 587
