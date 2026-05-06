# OTP Sending Troubleshooting Guide

## Issue: OTP Not Sent During Registration or Resend OTP Not Working

### Recent Fixes Applied

1. **Added Retry Logic**: Email service now retries up to 3 times with 1-second delays
2. **Better Error Handling**: Registration now fails if OTP email cannot be sent (instead of silently failing)
3. **Improved Logging**: More detailed error messages for debugging

---

## Debugging Steps

### Step 1: Verify Gmail Configuration
The application uses Gmail SMTP for sending emails. Check the following in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=it23033@mbstu.ac.bd
spring.mail.password=mgaf xgpw yqif olpq
```

**⚠️ IMPORTANT:** This is a **Gmail App Password**, not your regular Gmail password.

### Step 2: Check Gmail Account Security
1. Go to https://myaccount.google.com/security
2. Look for "App passwords" section
3. Verify the app password matches what's in `application.properties`
4. If expired or missing, generate a new one:
   - Go to Account → Security → App passwords
   - Select "Mail" and "Windows Computer"
   - Use the generated 16-character password in `application.properties`

### Step 3: Enable Less Secure App Access (if needed)
1. Go to https://myaccount.google.com/lesssecureapps
2. Turn ON "Allow less secure apps"
3. Restart the application

### Step 4: Check Email Configuration
Ensure `MailConfig` properties are set:
```properties
app.mail.from=it23033@mbstu.ac.bd
app.mail.from-name=Campus Safety
```

### Step 5: Verify Network Connectivity
- Check if your server can reach Gmail SMTP:
  ```bash
  telnet smtp.gmail.com 587
  ```
- If fails, you may have firewall/network restrictions

### Step 6: Check Application Logs
Look for these log messages:
- **Success**: `Email sent successfully to: user@email.com after X attempts`
- **Failure**: `Failed to send email to: user@email.com after 3 attempts. Error: [specific error]`

Common errors:
- `SMTPAuthenticationException` → Wrong credentials or app password
- `SMTPSendFailedException` → Sender email not authorized
- `SocketTimeoutException` → Network connectivity issue
- `AuthenticationFailedException` → 2FA or security settings blocking

---

## Testing Email Sending

### Method 1: Test via Registration Endpoint
1. Call `/auth/register` with test email
2. Watch logs for email sending status
3. Check if email arrives (including spam folder)

### Method 2: Create a Test Endpoint (Optional)
Add this to `AuthController.java` for testing:
```java
@PostMapping("/test-email")
public ResponseEntity<ApiResponse<String>> testEmail(@RequestParam String email) {
    try {
        emailService.sendOtpEmail(email, "123456");
        return ResponseEntity.ok(ApiResponse.success("Test email sent successfully"));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(
            ApiResponse.error("Failed to send test email: " + e.getMessage())
        );
    }
}
```

---

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| `SMTPAuthenticationException` | Wrong app password | Regenerate app password from Gmail settings |
| Email never arrives | Email marked as spam | Add sender to contacts; check spam folder |
| Timeout error | Network/firewall blocked | Check firewall; verify SMTP port 587 is open |
| `SendFailedException` | From address not verified | Verify sender email in Gmail account |
| Works locally, fails in production | Environment variables not set | Set `spring.mail.*` properties in production environment |

---

## Database Cleanup (if needed)

If users are stuck with unverified status after credential fixes:

```sql
-- View unverified users
SELECT id, email, is_verified FROM users WHERE is_verified = false;

-- Delete unverified users to allow re-registration (be careful!)
DELETE FROM users WHERE is_verified = false AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
```

---

## Current Configuration Details

- **Email Provider**: Gmail SMTP
- **Port**: 587 (TLS)
- **OTP Length**: 6 digits
- **OTP Expiration**: 10 minutes
- **Email Retries**: 3 attempts with 1-second delays
- **Sender Email**: it23033@mbstu.ac.bd
- **Sender Name**: Campus Safety

---

## Next Steps if Issues Persist

1. **Enable DEBUG logging** in `application.properties`:
   ```properties
   logging.level.org.springframework.mail=DEBUG
   logging.level.com.mbstu.campussafety.service.EmailService=DEBUG
   ```

2. **Check application server logs** for detailed stack traces

3. **Verify credentials** are exactly as provided in Gmail Account → Security → App passwords

4. **Consider using SendGrid/AWS SES** in production instead of Gmail SMTP for more reliability
