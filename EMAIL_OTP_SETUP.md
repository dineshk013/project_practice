## Production-Ready Email OTP Service Setup

## What Was Added

### 1. Dependencies
- `spring-boot-starter-mail` - Spring Mail support

### 2. New Files
- `EmailService.java` - Production-ready email service with HTML templates
- Email configuration in `application.yml`

### 3. Features
- ✅ Professional HTML email templates
- ✅ Async email sending (non-blocking)
- ✅ OTP verification emails
- ✅ Password reset emails
- ✅ 5-minute OTP expiry
- ✅ Branded RevCart design
- ✅ Mobile-responsive emails

---

## Gmail Setup (Recommended for Production)

### Step 1: Enable 2-Factor Authentication
1. Go to https://myaccount.google.com/security
2. Enable "2-Step Verification"

### Step 2: Generate App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" and "Other (Custom name)"
3. Enter "RevCart" as the name
4. Click "Generate"
5. **Copy the 16-character password** (e.g., `abcd efgh ijkl mnop`)

### Step 3: Configure User Service

**Option A: Environment Variables (Recommended)**
```bash
# Windows PowerShell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-char-app-password"

# Then start user-service
cd user-service
mvn spring-boot:run
```

**Option B: Update application.yml**
```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: abcdefghijklmnop  # Your 16-char app password (no spaces)
```

---

## Alternative Email Providers

### SendGrid (Recommended for High Volume)
```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: YOUR_SENDGRID_API_KEY
```

### AWS SES (Best for AWS Deployments)
```yaml
spring:
  mail:
    host: email-smtp.us-east-1.amazonaws.com
    port: 587
    username: YOUR_SMTP_USERNAME
    password: YOUR_SMTP_PASSWORD
```

### Mailgun
```yaml
spring:
  mail:
    host: smtp.mailgun.org
    port: 587
    username: postmaster@your-domain.mailgun.org
    password: YOUR_MAILGUN_PASSWORD
```

---

## Testing the Email Service

### 1. Start User Service
```powershell
cd user-service
mvn clean install
mvn spring-boot:run
```

### 2. Test Registration with OTP
```powershell
# Register a new user
$register = @{
    email = "your-real-email@gmail.com"
    password = "test123"
    name = "Test User"
    phone = "1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $register
```

### 3. Check Your Email
You should receive an email like this:

```
Subject: RevCart - Email Verification OTP

Hello Test User!

Thank you for registering with RevCart...

Your OTP Code: 123456
Valid for 5 minutes
```

### 4. Test Password Reset
```powershell
# Request password reset
Invoke-RestMethod -Uri "http://localhost:8081/api/users/reset-password?email=your-email@gmail.com" `
    -Method Post
```

---

## Email Templates

### Registration OTP Email
- **Subject**: RevCart - Email Verification OTP
- **Design**: Green gradient header with RevCart branding
- **Content**: Welcome message, OTP code, validity info
- **Security**: Warnings about not sharing OTP

### Password Reset Email
- **Subject**: RevCart - Password Reset OTP
- **Design**: Red gradient header (security alert)
- **Content**: Reset instructions, OTP code
- **Security**: Alert if user didn't request reset

---

## Configuration Reference

### Full application.yml Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

### Environment Variables
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
```

---

## Troubleshooting

### Issue: "Authentication failed"
**Solution**: 
- Verify 2FA is enabled on Gmail
- Generate new App Password
- Remove spaces from app password
- Use the 16-character password, not your Gmail password

### Issue: "Connection timeout"
**Solution**:
- Check firewall allows port 587
- Verify internet connection
- Try port 465 with SSL instead

### Issue: "Email not received"
**Solution**:
- Check spam/junk folder
- Verify email address is correct
- Check user-service logs for errors
- Test with a different email provider

### Issue: "535-5.7.8 Username and Password not accepted"
**Solution**:
- You're using Gmail password instead of App Password
- Generate App Password from https://myaccount.google.com/apppasswords

---

## Production Deployment Checklist

- [ ] Use environment variables for credentials
- [ ] Never commit passwords to Git
- [ ] Use dedicated email service (SendGrid/AWS SES)
- [ ] Set up email monitoring/alerts
- [ ] Configure rate limiting
- [ ] Add email delivery tracking
- [ ] Set up bounce handling
- [ ] Configure SPF/DKIM/DMARC records
- [ ] Test email deliverability
- [ ] Monitor email sending quotas

---

## Gmail Sending Limits

- **Free Gmail**: 500 emails/day
- **Google Workspace**: 2000 emails/day
- **SendGrid Free**: 100 emails/day
- **SendGrid Paid**: 40,000+ emails/month

For production with high volume, use SendGrid or AWS SES.

---

## Security Best Practices

1. **Never log OTP codes in production**
2. **Use HTTPS for all API calls**
3. **Implement rate limiting on OTP generation**
4. **Set short OTP expiry (5 minutes)**
5. **Invalidate OTP after use**
6. **Monitor for suspicious activity**
7. **Use environment variables for credentials**
8. **Rotate app passwords regularly**

---

## Quick Start Commands

```powershell
# 1. Set environment variables
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password"

# 2. Rebuild user-service
cd user-service
mvn clean install

# 3. Start user-service
mvn spring-boot:run

# 4. Test registration
$body = @{
    email = "test@gmail.com"
    password = "test123"
    name = "Test User"
    phone = "1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

# 5. Check your email for OTP!
```

---

## Support

If you encounter issues:
1. Check user-service logs for errors
2. Verify Gmail App Password is correct
3. Test with a simple email first
4. Check firewall/antivirus settings
5. Try alternative email provider

**Your OTP emails are now production-ready!** 🎉
