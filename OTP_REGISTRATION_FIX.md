# OTP Registration and Verification Fix

## Issues Fixed

### Issue 1: OTP Not Sent During Registration
**Problem**: Users were not receiving OTP email after clicking "Sign Up" button. OTP was only sent when clicking "Resend OTP".

**Root Cause**: The `register()` method in `AuthService` was not calling `generateOtp()` after user registration.

**Fix**: Added `generateOtp(email)` call immediately after saving the user in the registration flow.

```java
User saved = userRepository.save(user);
log.info("User registered: {}", saved.getEmail());

// Generate and send OTP immediately after registration
generateOtp(email);

String token = jwtTokenProvider.generateToken(saved.getEmail(), saved.getId());
return new AuthResponse(token, toDto(saved));
```

### Issue 2: OTP Verification Failed
**Problem**: Even with correct OTP, verification was failing with "Verification failed. Please try again."

**Root Causes**:
1. Multiple OTPs existed for same email (old OTPs not invalidated)
2. Email case sensitivity issues
3. OTP whitespace not trimmed

**Fixes Applied**:

#### 1. Invalidate Old OTPs Before Generating New One
```java
// Invalidate all previous OTPs for this email
otpTokenRepository.findAll().stream()
        .filter(token -> token.getEmail().equalsIgnoreCase(normalizedEmail) && !token.getUsed())
        .forEach(token -> {
            token.setUsed(true);
            otpTokenRepository.save(token);
        });
```

#### 2. Normalize Email in All OTP Operations
```java
String normalizedEmail = email.trim().toLowerCase();
```

#### 3. Trim OTP Input During Verification
```java
OtpToken otpToken = otpTokenRepository.findByEmailAndOtpAndUsedFalse(normalizedEmail, otp.trim())
```

## Changes Made

### File: `AuthService.java`

**Method: `register()`**
- Added `generateOtp(email)` call after user registration
- Ensures OTP is sent immediately when user signs up

**Method: `generateOtp()`**
- Added email normalization (trim + lowercase)
- Added logic to invalidate all previous unused OTPs for the email
- Ensures only one valid OTP exists per email at any time

**Method: `verifyOtp()`**
- Added email normalization
- Added OTP trimming
- Ensures case-insensitive and whitespace-tolerant verification

## Testing Steps

### 1. Test Registration Flow
```bash
# Register a new user
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "test123",
    "phone": "1234567890",
    "role": "CUSTOMER"
  }'
```

**Expected Result**:
- User registered successfully
- OTP email sent immediately
- Email arrives within 30 seconds

### 2. Test OTP Verification
```bash
# Verify OTP
curl -X POST "http://localhost:8081/api/users/verify-otp?email=test@example.com&otp=123456"
```

**Expected Result**:
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": null
}
```

### 3. Test Resend OTP
```bash
# Resend OTP
curl -X POST "http://localhost:8081/api/users/resend-otp?email=test@example.com"
```

**Expected Result**:
- Old OTP invalidated
- New OTP generated and sent
- Only new OTP works for verification

## User Flow

### Registration Flow (Fixed)
1. User fills registration form
2. User clicks "Sign Up" button
3. ✅ **Backend sends OTP email immediately**
4. User redirected to OTP verification page
5. User receives OTP in email (within 30 seconds)
6. User enters OTP
7. ✅ **OTP verification succeeds**
8. User account activated

### Resend OTP Flow (Fixed)
1. User clicks "Resend OTP" button
2. ✅ **Backend invalidates old OTP**
3. ✅ **Backend generates new OTP**
4. New OTP email sent
5. User enters new OTP
6. ✅ **Verification succeeds with new OTP**

## Benefits

1. ✅ **Immediate OTP Delivery**: Users receive OTP right after registration
2. ✅ **Single Valid OTP**: Only one OTP is valid at a time per email
3. ✅ **Case Insensitive**: Works with any email case (Test@Example.com = test@example.com)
4. ✅ **Whitespace Tolerant**: Trims whitespace from email and OTP
5. ✅ **Better UX**: No need to click "Resend OTP" after registration

## Verification Checklist

- [x] OTP sent immediately after registration
- [x] OTP email received in inbox
- [x] OTP verification works with correct OTP
- [x] Old OTPs invalidated when new OTP generated
- [x] Email case insensitive
- [x] OTP whitespace trimmed
- [x] Error messages clear and helpful

## Additional Notes

### OTP Expiration
- OTPs expire after 5 minutes
- Expired OTPs cannot be used
- User must request new OTP if expired

### Security Features
- OTPs are 6-digit random numbers
- Each OTP can only be used once
- Old OTPs automatically invalidated
- Email normalization prevents duplicate accounts

### Email Configuration
- SMTP: smtp.gmail.com:587
- TLS/STARTTLS enabled
- Timeout: 10 seconds
- Async email sending for better performance
