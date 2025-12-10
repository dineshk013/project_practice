# OTP Verification Test

## Database Record
```
id: 2
created_at: 2025-12-09 15:43:45.757135
email: kdhineshofficial@gmail.com
expires_at: 2025-12-09 15:48:45.757135
otp: 977223
used: 0
```

## Test Command
```bash
curl -X POST "http://localhost:8081/api/users/verify-otp?email=kdhineshofficial@gmail.com&otp=977223"
```

## Expected Response (Success)
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": null
}
```

## Expected Response (If Expired)
```json
{
  "success": false,
  "message": "OTP has expired"
}
```

## Troubleshooting Steps

### 1. Check if OTP is expired
Current time must be before `2025-12-09 15:48:45`

### 2. Check if OTP is already used
Run SQL:
```sql
SELECT * FROM otp_tokens WHERE email = 'kdhineshofficial@gmail.com' ORDER BY created_at DESC LIMIT 5;
```

### 3. Generate new OTP
```bash
curl -X POST "http://localhost:8081/api/users/resend-otp?email=kdhineshofficial@gmail.com"
```

### 4. Check application logs
Look for:
- "Attempting OTP verification for email: ..."
- "OTP not found or already used..."
- "OTP expired for email: ..."
- "✅ OTP verified successfully for: ..."

## Common Issues

### Issue: "Invalid or expired OTP"
**Causes**:
1. OTP already used (used = 1)
2. Email case mismatch
3. OTP has whitespace
4. Wrong OTP entered

**Solution**:
- Generate new OTP with resend-otp endpoint
- Ensure email is lowercase
- Trim OTP before sending

### Issue: "OTP has expired"
**Cause**: Current time > expires_at

**Solution**:
- Generate new OTP (valid for 5 minutes)

### Issue: 500 Internal Server Error
**Causes**:
1. Database connection issue
2. Email not found in users table
3. Unexpected exception

**Solution**:
- Check application logs for stack trace
- Verify user exists in database
- Check database connectivity
