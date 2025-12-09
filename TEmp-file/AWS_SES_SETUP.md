# AWS SES Setup for RevCart Email Service

## Why AWS SES?

✅ **Cost-Effective**: $0.10 per 1,000 emails (62,000 free emails/month on EC2)  
✅ **Scalable**: Send millions of emails  
✅ **Reliable**: 99.9% uptime SLA  
✅ **Fast**: Low latency email delivery  
✅ **Integrated**: Works seamlessly with AWS services  
✅ **No Code Changes**: Just update configuration  

---

## Your Code is Already Compatible!

Your `EmailService.java` works with **any SMTP provider** including AWS SES. No code changes needed!

---

## AWS SES Setup Steps

### Step 1: Create AWS Account
1. Go to https://aws.amazon.com
2. Create account or sign in
3. Navigate to **SES (Simple Email Service)**

### Step 2: Verify Email Address (Sandbox Mode)
1. Go to **SES Console** → **Verified identities**
2. Click **Create identity**
3. Select **Email address**
4. Enter your email (e.g., `noreply@revcart.com`)
5. Click **Create identity**
6. Check your email and click verification link

### Step 3: Request Production Access (Remove Sandbox)
**In Sandbox Mode**: Can only send to verified emails  
**In Production**: Can send to any email

1. Go to **SES Console** → **Account dashboard**
2. Click **Request production access**
3. Fill the form:
   - **Mail type**: Transactional
   - **Website URL**: Your website
   - **Use case**: OTP verification and password reset emails
   - **Compliance**: Confirm you have opt-in process
4. Submit (usually approved in 24 hours)

### Step 4: Create SMTP Credentials
1. Go to **SES Console** → **SMTP settings**
2. Click **Create SMTP credentials**
3. Enter IAM user name: `revcart-ses-smtp`
4. Click **Create**
5. **Download credentials** (you'll see):
   - SMTP Username: `YOUR_SMTP_USERNAME`
   - SMTP Password: `YOUR_SMTP_PASSWORD`

### Step 5: Note Your SMTP Endpoint
Based on your AWS region:
- **US East (N. Virginia)**: `email-smtp.us-east-1.amazonaws.com`
- **US West (Oregon)**: `email-smtp.us-west-2.amazonaws.com`
- **EU (Ireland)**: `email-smtp.eu-west-1.amazonaws.com`
- **Asia Pacific (Mumbai)**: `email-smtp.ap-south-1.amazonaws.com`
- **Asia Pacific (Singapore)**: `email-smtp.ap-southeast-1.amazonaws.com`

Full list: https://docs.aws.amazon.com/ses/latest/dg/regions.html

---

## Configuration for AWS SES

### Option 1: Environment Variables (Recommended)

```bash
# For AWS Deployment (ECS, EKS, EC2)
export MAIL_USERNAME=YOUR_SMTP_USERNAME
export MAIL_PASSWORD=YOUR_SMTP_PASSWORD
export MAIL_HOST=email-smtp.us-east-1.amazonaws.com
```

```powershell
# For Local Testing with AWS SES
$env:MAIL_USERNAME="YOUR_SMTP_USERNAME"
$env:MAIL_PASSWORD="YOUR_SMTP_PASSWORD"
$env:MAIL_HOST="email-smtp.us-east-1.amazonaws.com"
```

### Option 2: Update application.yml

```yaml
spring:
  mail:
    host: ${MAIL_HOST:email-smtp.us-east-1.amazonaws.com}
    port: 587
    username: ${MAIL_USERNAME:YOUR_SMTP_USERNAME}
    password: ${MAIL_PASSWORD:YOUR_SMTP_PASSWORD}
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

### Option 3: AWS Secrets Manager (Production Best Practice)

```yaml
spring:
  mail:
    host: ${MAIL_HOST}
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

Then use AWS Secrets Manager to inject credentials at runtime.

---

## Deployment Configurations

### For AWS ECS (Elastic Container Service)

**Task Definition Environment Variables:**
```json
{
  "environment": [
    {
      "name": "MAIL_HOST",
      "value": "email-smtp.us-east-1.amazonaws.com"
    },
    {
      "name": "MAIL_USERNAME",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:revcart/ses/username"
    },
    {
      "name": "MAIL_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:revcart/ses/password"
    }
  ]
}
```

### For AWS Elastic Beanstalk

**Environment Properties:**
```
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_USERNAME=YOUR_SMTP_USERNAME
MAIL_PASSWORD=YOUR_SMTP_PASSWORD
```

### For AWS EKS (Kubernetes)

**ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: revcart-mail-config
data:
  MAIL_HOST: "email-smtp.us-east-1.amazonaws.com"
```

**Secret:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: revcart-mail-secret
type: Opaque
data:
  MAIL_USERNAME: <base64-encoded-username>
  MAIL_PASSWORD: <base64-encoded-password>
```

---

## Testing AWS SES

### 1. Test from Local Machine

```powershell
# Set AWS SES credentials
$env:MAIL_HOST="email-smtp.us-east-1.amazonaws.com"
$env:MAIL_USERNAME="YOUR_SMTP_USERNAME"
$env:MAIL_PASSWORD="YOUR_SMTP_PASSWORD"

# Start user-service
cd user-service
mvn spring-boot:run

# Test registration
$body = @{
    email = "verified-email@example.com"  # Must be verified in sandbox mode
    password = "test123"
    name = "Test User"
    phone = "1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### 2. Verify Email Sent

Check AWS SES Console:
1. Go to **SES Console** → **Account dashboard**
2. View **Sending statistics**
3. Check **Sent**, **Delivered**, **Bounces**, **Complaints**

---

## AWS SES Pricing

### Free Tier (EC2-Hosted Applications)
- **62,000 emails/month FREE** when sending from EC2
- After that: **$0.10 per 1,000 emails**

### Standard Pricing (Non-EC2)
- **First 1,000 emails**: FREE
- After that: **$0.10 per 1,000 emails**

### Data Transfer
- **First 1 GB/month**: FREE
- After that: **$0.12 per GB**

### Example Costs:
- **10,000 emails/month**: $0.90
- **100,000 emails/month**: $9.90
- **1,000,000 emails/month**: $99.90

**Much cheaper than SendGrid or Mailgun!**

---

## AWS SES Limits

### Sandbox Mode
- Can only send to verified email addresses
- Max 200 emails/day
- Max 1 email/second

### Production Mode (After Approval)
- Can send to any email address
- Initial limit: 50,000 emails/day
- Can request increase up to millions/day
- Max 14 emails/second (can be increased)

---

## Monitoring & Logging

### CloudWatch Metrics
AWS SES automatically sends metrics to CloudWatch:
- **Send**: Total emails sent
- **Delivery**: Successfully delivered
- **Bounce**: Hard/soft bounces
- **Complaint**: Spam complaints
- **Reject**: Rejected by SES

### Set Up Alarms
```bash
# Create CloudWatch alarm for high bounce rate
aws cloudwatch put-metric-alarm \
  --alarm-name revcart-ses-high-bounce-rate \
  --alarm-description "Alert when bounce rate > 5%" \
  --metric-name Reputation.BounceRate \
  --namespace AWS/SES \
  --statistic Average \
  --period 300 \
  --threshold 0.05 \
  --comparison-operator GreaterThanThreshold
```

---

## Domain Verification (Optional but Recommended)

### Benefits:
- Send from any email address on your domain
- Better deliverability
- DKIM signing for authentication
- Custom MAIL FROM domain

### Steps:
1. Go to **SES Console** → **Verified identities**
2. Click **Create identity** → **Domain**
3. Enter your domain: `revcart.com`
4. Enable **DKIM** and **Custom MAIL FROM**
5. Add DNS records to your domain:
   - **TXT record** for verification
   - **CNAME records** for DKIM
   - **MX record** for MAIL FROM

---

## Migration Path

### Phase 1: Development (Now)
```yaml
# Use Gmail for local development
spring:
  mail:
    host: smtp.gmail.com
    username: your-email@gmail.com
    password: your-app-password
```

### Phase 2: Staging (AWS Testing)
```yaml
# Use AWS SES in sandbox mode
spring:
  mail:
    host: email-smtp.us-east-1.amazonaws.com
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### Phase 3: Production (AWS Deployment)
```yaml
# Use AWS SES in production mode
spring:
  mail:
    host: ${MAIL_HOST}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

---

## Best Practices for AWS SES

### 1. Use IAM Roles (Instead of SMTP Credentials)
For EC2/ECS/EKS, use IAM roles instead of SMTP:
```java
// Add AWS SDK dependency
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-ses</artifactId>
</dependency>
```

### 2. Implement Bounce Handling
Set up SNS topics for bounces and complaints:
```yaml
# Configure SNS notifications
aws ses set-identity-notification-topic \
  --identity revcart.com \
  --notification-type Bounce \
  --sns-topic arn:aws:sns:us-east-1:123456789:revcart-bounces
```

### 3. Monitor Reputation
- Keep bounce rate < 5%
- Keep complaint rate < 0.1%
- Remove invalid emails from your list

### 4. Use Configuration Sets
Track email opens, clicks, and delivery:
```java
helper.addHeader("X-SES-CONFIGURATION-SET", "revcart-tracking");
```

---

## Comparison: Gmail vs AWS SES

| Feature | Gmail | AWS SES |
|---------|-------|---------|
| **Cost** | Free (500/day) | $0.10/1000 emails |
| **Limit** | 500 emails/day | 50,000+/day |
| **Reliability** | Good | Excellent (99.9% SLA) |
| **Deliverability** | Good | Excellent |
| **Monitoring** | None | CloudWatch metrics |
| **Scalability** | Limited | Unlimited |
| **Setup** | Easy | Moderate |
| **Best For** | Development | Production |

---

## Quick Switch Command

```powershell
# Switch from Gmail to AWS SES (no code changes!)
$env:MAIL_HOST="email-smtp.us-east-1.amazonaws.com"
$env:MAIL_USERNAME="YOUR_AWS_SMTP_USERNAME"
$env:MAIL_PASSWORD="YOUR_AWS_SMTP_PASSWORD"

# Restart user-service
cd user-service
mvn spring-boot:run
```

---

## Summary

✅ **Your code is ready for AWS SES**  
✅ **No changes needed to EmailService.java**  
✅ **Just update configuration**  
✅ **Works with Gmail now, AWS SES later**  
✅ **Seamless migration path**  

**Start with Gmail for development, switch to AWS SES for production!**
