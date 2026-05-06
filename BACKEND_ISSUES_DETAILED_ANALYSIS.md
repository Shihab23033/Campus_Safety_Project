# Campus Safety Backend - Complete Issue Analysis & Fixes

## Summary
The Spring Boot backend has **critical issues** preventing user registration, OTP sending, OTP verification, and OTP resend from working properly. The root causes are:

1. **Blocking Email Operations** (PRIMARY ISSUE) - Using Thread.sleep() blocks thread pool
2. **In-Memory OTP Storage** - Lost on server restart, not reliable
3. **Poor Transaction Management** - User deletion after email failure can fail
4. **Exposed Credentials** - Email password in plaintext properties file
5. **No Async Email Queue** - All email operations are synchronous and blocking

---

## CRITICAL ISSUES & ROOT CAUSES

### Issue #1: Thread.sleep() Blocking in EmailService ⚠️ CRITICAL

**File**: `src/main/java/com/mbstu/campussafety/service/EmailService.java`

**Problem**:
```java
private void sendSimpleEmailWithRetry(String to, String subject, String body, int retryCount) {
    try {
        // ... send email ...
    } catch (Exception e) {
        if (retryCount < MAX_RETRIES - 1) {
            try {
                Thread.sleep(RETRY_DELAY_MS);  // ❌ BLOCKS THREAD POOL!
                sendSimpleEmailWithRetry(to, subject, body, retryCount + 1);
            } catch (InterruptedException ie) {
                // ...
            }
        }
    }
}
```

**Why This Breaks Everything**:
- Spring's mail sender uses thread pool
- Thread.sleep() blocks the entire thread for 1 second per retry
- Multiple requests → thread starvation → email timeouts
- Default Tomcat thread pool is exhausted → server hangs
- Subsequent requests fail silently or timeout

**Impact**:
- ✗ OTP not sent sometimes
- ✗ Resend OTP times out
- ✗ User registration hangs
- ✗ Server becomes unresponsive

---

### Issue #2: Transaction Management & User Deletion

**File**: `src/main/java/com/mbstu/campussafety/service/AuthService.java`

**Problem**:
```java
public void register(UserRegistrationRequest request) {
    // ... validation ...
    
    User user = User.builder()...build();
    user.setRoles(...);
    userRepository.save(user);  // User saved to DB ✓
    
    try {
        emailService.sendOtpEmail(request.getEmail(), otp);
        log.info("OTP sent successfully");
    } catch (Exception e) {
        // ❌ PROBLEM: Trying to delete after save in @Transactional method
        userRepository.deleteById(user.getId());
        otpStore.remove(request.getEmail());
        throw new RuntimeException(...);
    }
}
```

**Problems**:
1. Save happens first → User exists in DB
2. Email fails → Try to delete
3. But @Transactional may have already committed the save!
4. Delete might also fail → User stuck in DB with no OTP
5. User cannot register again (email already exists) and cannot verify

**Solution**: Reverse order - generate and verify OTP capability BEFORE saving user

---

### Issue #3: In-Memory OTP Storage

**File**: `src/main/java/com/mbstu/campussafety/service/AuthService.java`

**Problem**:
```java
private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
```

**Issues**:
- ✗ OTPs lost on server restart
- ✗ Not shared across multiple server instances (clustering)
- ✗ No persistence/recovery mechanism
- ✗ User generates OTP, server restarts, OTP gone → cannot verify
- ✗ Memory leaks possible if cleanup fails

---

### Issue #4: Email Credentials Exposed

**File**: `src/main/resources/application.properties`

**Problem**:
```properties
spring.mail.password=ogtj hhzu evmx aqhh  # ❌ PLAINTEXT PASSWORD!
```

**Risks**:
- Visible in git history (compromised!)
- Visible in server logs
- Accessible to anyone with file access
- Password in source code = permanent compromise

---

### Issue #5: OTP Verification Requires User Existence Before Verification

**File**: `src/main/java/com/mbstu/campussafety/controller/AuthController.java`

**Current Flow** (BROKEN):
1. User registration → User saved immediately
2. Email failure → User deleted (maybe)
3. User tries to verify OTP → User not found
4. **OR** User saved but OTP send failed → Cannot verify later

---

## DETAILED ANALYSIS OF EACH ENDPOINT

### 1. POST /auth/register

**Current Flow**:
```
Request → Validate email format
        → Check if user exists
        → Create User object
        → Assign MEMBER role
        → SAVE USER TO DB ⚠️ TOO EARLY!
        → Generate OTP (6 digits)
        → Store in memory
        → Try to send email
            ├─ Success → Return 201
            └─ Failure → Delete user (might fail!)
                      → Remove OTP
                      → Throw exception
```

**Problems**:
- User saved before email verified → If email fails, user stuck in DB
- Email send uses Thread.sleep() → Blocks and times out
- No async processing → Client waits for email to send
- User cannot retry (email already registered)

**Why Registration Fails**:
1. Email credentials invalid → SMTP auth fails
2. Thread.sleep() exhausts thread pool → Timeout
3. Network issues → Email not sent
4. Exception thrown but user already in DB

---

### 2. POST /auth/verify-otp

**Current Flow**:
```
Request with email + OTP
        → Check if OTP in memory
        → If not found → Error "OTP not found or expired"
        → Check if expired (10 min)
        → Compare OTP string
        → Find user by email
        → Set isVerified = true
        → Save user
        → Remove OTP from memory
```

**Why Verification Never Works**:
1. **OTP not in memory** → Server restarted after registration
2. **User not found** → Was deleted after registration failed
3. **OTP expired** → User couldn't register OTP in first place
4. **Email was never sent** → User trying to verify non-existent OTP

**The Chicken-Egg Problem**:
- Need OTP to verify
- OTP stored only in memory
- Memory lost on restart
- Can't verify after restart

---

### 3. POST /auth/resend-otp

**Same issues as Issue #1**:
- Thread.sleep() blocks
- Email send times out
- OTP not resent

---

## EMAIL CONFIGURATION ISSUES

**File**: `src/main/resources/application.properties`

**Current Config**:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=it23033@mbstu.ac.bd
spring.mail.password=ogtj hhzu evmx aqhh  # ❌ App Password - might be invalid or expired
spring.mail.properties.mail.smtp.connectiontimeout=5000  # Only 5 seconds!
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

**Problems**:
1. **Timeout too short** - 5 seconds is not enough for Gmail SMTP
2. **No connection pooling** - Each email creates new connection
3. **Single-threaded** - All emails serialized
4. **No retry mechanism** - First failure = failure
5. **Gmail App Password** - May have expired or be wrong

---

## HTTP ENDPOINT CONFIGURATION

**File**: `src/main/resources/application.properties`

```properties
server.servlet.context-path=/api
```

**Important**: All endpoints are prefixed with `/api/`

**Actual Endpoints**:
- POST `http://localhost:8080/api/auth/register`
- POST `http://localhost:8080/api/auth/verify-otp`
- POST `http://localhost:8080/api/auth/resend-otp`
- POST `http://localhost:8080/api/auth/login`

(Client might be calling wrong URLs without `/api` prefix!)

---

## OTP VALIDATION ISSUE

**File**: `src/main/java/com/mbstu/campussafety/dto/auth/OtpVerificationRequest.java`

```java
@Pattern(regexp = "\\d{6}", message = "OTP should be 6 digits")
private String otp;
```

**OTP Generated**:
```java
int otp = 100000 + random.nextInt(900000);  // Range: 100000-999999 (6 digits) ✓
return String.valueOf(otp);
```

**This part is CORRECT** ✓

---

## SECURITY VULNERABILITIES

1. **Plain-text password in config** - Critical
2. **No rate limiting** - Users can spam OTP requests
3. **No CAPTCHA** - Bot attacks possible
4. **No email domain validation** - Can register with fake emails
5. **No account lockout** - After failed verifications
6. **OTP too predictable** - Using java.util.Random (not cryptographically secure)

---

## DATABASE CONSTRAINTS

**User Entity**:
```java
@Column(unique = true, nullable = false)
private String email;

@Column(nullable = false)
private Boolean isVerified = false;
```

**Issue**:
- If user registration fails after save, email marked as used
- User cannot re-register with same email
- No way to unregister failed accounts

---

## COMPLETE ROOT CAUSE FLOW

```
User clicks "Register"
    ↓
[AuthController.register()] 
    ↓
[AuthService.register()]
    ├─ Check if email exists ✓
    ├─ Create User object ✓
    ├─ Assign role ✓
    ├─ Save user to DB ✓
    ├─ Generate OTP ✓
    ├─ Store OTP in memory ✓
    ├─ Call emailService.sendOtpEmail()
    │   ├─ [EmailService.sendSimpleEmail()]
    │   │   └─ [EmailService.sendSimpleEmailWithRetry()]
    │   │       ├─ Create SimpleMailMessage ✓
    │   │       ├─ mailSender.send(message)
    │   │       │   ├─ Connect to Gmail SMTP
    │   │       │   ├─ Auth fails OR timeout
    │   │       │   └─ Throw exception ✗
    │   │       ├─ Catch exception
    │   │       ├─ If retry < 3:
    │   │       │   └─ Thread.sleep(1000) ⚠️ BLOCKS THREAD!
    │   │       │   └─ Recursive call (stack growing)
    │   │       │   └─ Another failure
    │   │       │       └─ Another sleep...
    │   │       │           └─ Another failure
    │   │       │               └─ Another sleep (3 seconds total)
    │   │       └─ Throw RuntimeException
    │   └─ Exception bubbles up
    ├─ Catch exception in register()
    ├─ Try to deleteById(user.getId())
    │   └─ DELETE succeeds OR fails
    ├─ Throw RuntimeException to client
    └─ Client gets error

Meanwhile:
    - Gmail SMTP still waiting for response
    - Thread pool thread BLOCKED
    - Next request arrives
    - No thread available
    - Request queues
    - More requests come in
    - Thread pool exhausted
    - Server becomes unresponsive
    - All subsequent requests timeout
```

---

## SUMMARY OF ALL ISSUES

| # | Issue | Severity | Component | Impact |
|---|-------|----------|-----------|--------|
| 1 | Thread.sleep() blocking | 🔴 CRITICAL | EmailService | Threads blocked, server hangs |
| 2 | User saved before email | 🔴 CRITICAL | AuthService | Can't delete failed registrations |
| 3 | In-memory OTP storage | 🟠 HIGH | AuthService | OTP lost on restart |
| 4 | Email password exposed | 🟠 HIGH | Config | Security breach |
| 5 | Short timeouts (5s) | 🟠 HIGH | Config | Email send times out |
| 6 | No async email | 🟡 MEDIUM | EmailService | Slow responses |
| 7 | No rate limiting | 🟡 MEDIUM | AuthController | Spam attacks |
| 8 | No email validation | 🟡 MEDIUM | AuthService | Fake emails |
| 9 | Predictable OTP | 🟡 MEDIUM | AuthService | Security risk |
| 10 | No account recovery | 🟡 MEDIUM | AuthService | User stuck in DB |

---

## FILES AFFECTED

```
src/main/java/com/mbstu/campussafety/
├── service/
│   ├── AuthService.java          ← PRIMARY FIX NEEDED
│   ├── EmailService.java         ← PRIMARY FIX NEEDED
│   └── UserService.java          ← Verify implementation
├── controller/
│   └── AuthController.java       ← Might need rate limiting
├── config/
│   ├── MailConfig.java           ← Needs env variables
│   ├── SecurityConfig.java       ← OK
│   └── (need email executor config)
├── entity/
│   └── User.java                 ← OK (add email_verified_at column)
└── dto/auth/
    ├── UserRegistrationRequest.java
    ├── OtpVerificationRequest.java
    └── (others)

src/main/resources/
└── application.properties         ← NEEDS SECURING

Database Migrations needed:
- Add email_verified_at column (for OTP timestamp)
- Add otp_attempts counter (for rate limiting)
- Create otp_table if storing in DB
```

---

## Next Steps

**IMMEDIATE (Priority 1)**:
1. Remove Thread.sleep() from EmailService
2. Implement async email sending
3. Increase email timeouts (30 seconds)
4. Move email credentials to environment variables

**SHORT TERM (Priority 2)**:
1. Move OTP storage to database or Redis
2. Implement proper transaction management
3. Add rate limiting to OTP endpoints
4. Add email verification token

**LONG TERM (Priority 3)**:
1. Implement CAPTCHA
2. Add account lockout mechanism
3. Use cryptographically secure random for OTP
4. Add email domain validation
5. Implement email queue system (RabbitMQ)

---

## How to Test Each Issue

### Test 1: Thread Blocking
```bash
# While registration is processing, try another request
curl http://localhost:8080/api/auth/register &
curl http://localhost:8080/api/auth/register &  # Should hang!
```

### Test 2: OTP Not Sent
```bash
# Check logs for Thread.sleep messages and timeout errors
tail -f logs/application.log | grep -E "(Thread|sleep|timeout|WARN|ERROR)"
```

### Test 3: Server Restart = Lost OTP
```bash
# Register user, restart server, try to verify OTP
# → OTP not found error
```

---

## Temporary Workarounds

1. **Gmail App Password Issue**:
   - Generate new [Google App Password](https://myaccount.google.com/apppasswords)
   - Update application.properties

2. **Email Timeouts**:
   - Increase timeouts to 30 seconds in application.properties
   - Check Gmail account for security alerts

3. **User Stuck in DB**:
   - Delete user manually from database
   - Then retry registration

4. **OTP Lost After Restart**:
   - Don't restart server during testing
   - Or store OTP in database manually
