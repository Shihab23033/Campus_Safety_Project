# Campus Safety Integration - Quick Reference Card

## What Did I Do? (30-Second Version)

✅ Connected Android app to Spring Boot backend
✅ Updated Android to find backend at: `http://10.0.2.2:8080/api/`
✅ Updated backend to accept requests from Android
✅ Created 3 documentation guides
✅ Verified everything builds correctly

---

## How Does It Work? (Simplest Explanation)

```
You (Android Phone)          Internet            Backend Server
      ↓                          ↓                      ↓
   "I want to login"
      ↓
   [Create login request]
      ↓
   [Find backend address]
      ├─ Where is backend?
      ├─ Answer: 10.0.2.2:8080/api
      ├─ That's my computer on port 8080
      ↓
   [Send request to backend]
      ────────────────HTTP REQUEST──────────────────>
                                                       ↓
                                                  [Receive request]
                                                       ↓
                                                  [Check if valid]
                                                       ↓
                                                  [Verify password]
                                                       ↓
                                                  [Save to database]
                                                       ↓
                                                  [Generate token]
                                                       ↓
      <──────────────HTTP RESPONSE─────────────────────
      ↓
   [Receive response]
      ↓
   [Save token]
      ↓
   "Login successful!"
```

---

## Start Backend (Copy-Paste)

```bash
cd d:\Codes\Campus_Safety_Project\SpringBoot_server
.\gradlew bootRun
```

**Wait for this message:**
```
Started CampusSafetyApplication in 5.234 seconds
```

---

## Check Backend Working

**In Browser, open:**
```
http://localhost:8080/api/swagger-ui.html
```

If you see API list → Backend is working ✓

---

## Run Android App

1. Open Android Studio
2. Click Run
3. Select Emulator or Device
4. App opens → Try register/login

---

## What Got Changed

| What | Where | Why |
|------|-------|-----|
| Base URL | `RetrofitClient.java` | Point to local backend |
| CORS Settings | `SecurityConfig.java` | Allow Android to connect |
| Documentation | 3 new files | Help developers understand |

---

## The Key Settings

### In Android App:
```java
private static final String BASE_URL = "http://10.0.2.2:8080/api/";
//                                       └─ Your computer
//                                              └─ Port number
//                                                    └─ API path
```

### In Backend:
```java
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
// Allows ALL requests (development only)
// In production: Change to specific apps/websites only
```

---

## Test Login (Android App)

1. Register first:
   - Email: `test@gmail.com`
   - Password: `Test@1234`
   - Name: `Test User`

2. Click "Register"

3. Then Login:
   - Email: `test@gmail.com`
   - Password: `Test@1234`

4. Click "Login"

**Success = You're connected! ✓**

---

## If Something Breaks

| Problem | Solution |
|---------|----------|
| "Can't connect to 10.0.2.2" | Start backend: `.\gradlew bootRun` |
| "User not found" | Register first before login |
| "Wrong credentials" | Check email/password spelling |
| "Timeout" | Check if backend is running |
| "CORS error" | Backend CORS already fixed |

---

## Key Files Created

1. **ANDROID_BACKEND_INTEGRATION.md**
   - Complete technical guide
   - How requests/responses work
   - Security details
   - Troubleshooting

2. **DEVELOPMENT_SETUP.md**
   - Quick start guide
   - Step-by-step setup
   - Testing instructions

3. **INTEGRATION_PROCEDURE.md**
   - Visual diagrams
   - What I changed
   - Different network setups

---

## Important URLs

| What | URL |
|------|-----|
| Backend API | `http://localhost:8080/api` |
| API Documentation | `http://localhost:8080/api/swagger-ui.html` |
| Android Emulator Backend | `http://10.0.2.2:8080/api/` |

---

## Network Options

### 1. Android Emulator (DEFAULT) ✓
```
BASE_URL: http://10.0.2.2:8080/api/
Status: Already configured
```

### 2. Physical Phone (Same WiFi)
```
1. Find PC IP: ipconfig
2. Update BASE_URL: http://192.168.1.100:8080/api/
3. Rebuild and run app
```

### 3. Production (Cloud Server)
```
1. Deploy backend to Render/Railway
2. Get URL: https://campus-safety.render.com/api/
3. Update BASE_URL
4. Rebuild and deploy app
```

---

## Build Status

✅ **SUCCESSFUL**

```
BUILD SUCCESSFUL in 26s
5 actionable tasks: 4 executed, 1 up-to-date
```

No errors, ready to use!

---

## One-Minute Test

```
1. Start backend:          .\gradlew bootRun
   Wait for: "Started CampusSafetyApplication"

2. Open browser:           http://localhost:8080/api/swagger-ui.html
   Expect: API documentation page

3. Run Android app:        Android Studio → Run
   Expect: App opens on emulator

4. Click "Register":       Fill email/password/name
   Expect: Success message

5. Click "Login":          Use registered email/password
   Expect: Login successful, home screen shows
```

**All 5 steps work = Integration is perfect! ✓**

---

## Remember

- **10.0.2.2** = "Your computer" from emulator's perspective
- **8080** = The port where backend listens
- **/api/** = Path prefix for all endpoints
- **JWT Token** = Digital passport after login
- **CORS** = Security rules for who can talk to backend

---

## Next: Add More Features

Once connection works:

1. Test emergency alerts endpoint
2. Test location tracking
3. Test real-time chat
4. Test notifications
5. Deploy to production

---

## Documentation Files

Read in this order:

1. **INTEGRATION_PROCEDURE.md** ← Start here (visual guide)
2. **DEVELOPMENT_SETUP.md** ← Then this (step-by-step setup)
3. **ANDROID_BACKEND_INTEGRATION.md** ← Finally this (technical details)

---

## Summary

✅ Android app → finds backend at `http://10.0.2.2:8080/api/`
✅ Backend → accepts requests from Android
✅ Token → automatically added to all requests
✅ Communication → HTTP JSON requests/responses
✅ Build → compiles successfully
✅ Ready → for development and testing

**You're all set! Start developing! 🚀**
