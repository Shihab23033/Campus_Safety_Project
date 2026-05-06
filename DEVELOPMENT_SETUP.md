# Campus Safety - Development Setup Guide

## Quick Start (5 Minutes)

### Prerequisites
- Java 17 or higher
- MySQL 8.0+
- Android Studio (for Android app)
- Git

---

## Backend Setup (Spring Boot)

### 1. Database Setup

```bash
# Open MySQL and run these commands:

CREATE DATABASE campus_safety;
USE campus_safety;

# Create roles (backend will create tables automatically)
# You'll need to insert these via the app or database

INSERT INTO roles (name, description) VALUES 
('MEMBER', 'Regular user who can report emergencies'),
('RESPONDER', 'First responder who helps in emergencies'),
('ADMIN', 'Administrator with full access');
```

### 2. Start Spring Boot Backend

```bash
# Navigate to SpringBoot_server folder
cd d:\Codes\Campus_Safety_Project\SpringBoot_server

# Start the server
.\gradlew bootRun

# You should see:
# "Started CampusSafetyApplication in X.XXX seconds"
# Backend running at: http://localhost:8080/api
```

### 3. Verify Backend is Working

Open browser and go to:
```
http://localhost:8080/api/swagger-ui.html
```

You should see API documentation with all endpoints.

---

## Android App Setup

### 1. Open Android Project

```bash
# Open Android_App folder in Android Studio
File → Open → Select Android_App folder
```

### 2. Configure Base URL

**File:** `Android_App/app/src/main/java/com/mbstu/campussafety/data/remote/RetrofitClient.java`

```java
// Choose based on your setup:

// Option 1: Android Emulator (Default)
private static final String BASE_URL = "http://10.0.2.2:8080/api/";

// Option 2: Physical Device (Replace with your PC IP)
// Find your PC IP: Open Command Prompt and type: ipconfig
// Look for "IPv4 Address" (usually 192.168.x.x)
private static final String BASE_URL = "http://192.168.1.100:8080/api/";

// Option 3: Production Server
private static final String BASE_URL = "https://your-deployed-backend.com/api/";
```

### 3. Build and Run

```bash
# In Android Studio:
# 1. Click Build → Make Project
# 2. Click Run → Run 'app'
# 3. Select emulator or physical device
# 4. App opens
```

---

## Testing the Integration

### Test 1: Check Backend API

```bash
# Open browser or Postman
GET http://localhost:8080/api/safe-zones

# Should return empty array or list of safe zones
```

### Test 2: Register New User (From Android App)

1. Open Android app
2. Click "Register"
3. Enter:
   - Email: `test@example.com`
   - Password: `Test@1234`
   - First Name: `Test`
   - Last Name: `User`
4. Click "Register"

**Expected Result:** 
- Success message: "Registration successful"
- Check backend console for confirmation

### Test 3: Login (From Android App)

1. Click "Login"
2. Enter:
   - Email: `test@example.com`
   - Password: `Test@1234`
3. Click "Login"

**Expected Result:**
- Success: Shows home/dashboard screen
- Token saved and ready for authenticated requests

---

## Troubleshooting

### Problem: "Failed to connect to 10.0.2.2:8080"

**Cause:** Backend not running

**Solution:**
```bash
1. Open terminal
2. Navigate to SpringBoot_server
3. Run: .\gradlew bootRun
4. Wait for "Started CampusSafetyApplication"
5. Try login again
```

### Problem: "Network timeout"

**Cause:** Wrong IP address or backend not accessible

**Solution:**
```
1. Check backend is running: http://localhost:8080/api/swagger-ui.html
2. If using physical device, ensure same WiFi
3. Check your PC IP: ipconfig
4. Update BASE_URL in RetrofitClient.java
5. Rebuild and run Android app
```

### Problem: "User not found" on login

**Cause:** User not registered yet

**Solution:**
1. Make sure you registered first
2. Check email is correct (case-sensitive)
3. Check password is correct

### Problem: "Unauthorized 401" after login

**Cause:** Token not being sent or invalid

**Solution:**
1. Check token is being saved after login
2. Try logging out and logging in again
3. Check backend logs for token validation errors

### Problem: "CORS error" (Web browser only)

**Cause:** CORS not configured

**Solution:**
- Not applicable to Android app (Android doesn't use CORS)
- If testing backend with browser, update SecurityConfig.java

---

## Connection Flow Diagram

```
┌──────────────────────┐
│   Android Device     │
│   or Emulator        │
└──────────┬───────────┘
           │
           │ (HTTP Request)
           │ /api/auth/login
           │ {"email": "...", "password": "..."}
           │
           ▼
┌──────────────────────┐
│ Spring Boot Backend  │
│ localhost:8080       │
│                      │
│ AuthController       │
│ ↓                    │
│ AuthService          │
│ ↓                    │
│ UserRepository       │
│ ↓                    │
│ MySQL Database       │
└──────────┬───────────┘
           │
           │ (HTTP Response)
           │ {"token": "JWT...", "user": {...}}
           │
           ▼
┌──────────────────────┐
│   Android Device     │
│   (Token saved)      │
└──────────────────────┘
```

---

## Important Endpoints

```
Authentication:
POST   /api/auth/register          Register new user
POST   /api/auth/login             Login and get JWT token
POST   /api/auth/verify-otp        Verify email
POST   /api/auth/resend-otp        Resend OTP
GET    /api/auth/me                Get current user

Emergency:
POST   /api/alerts                 Create emergency alert
GET    /api/alerts                 Get all alerts
GET    /api/alerts/{id}            Get specific alert
PUT    /api/alerts/{id}/status     Update alert status

Location:
POST   /api/locations/update       Send location
GET    /api/locations/current      Get current location
GET    /api/safe-zones             Get all safe zones

Chat:
POST   /api/chat/send              Send message
GET    /api/chat/conversations     Get chat history
```

---

## Environment Variables

For production, use environment variables instead of hardcoding:

**Backend (.env file):**
```
DB_URL=jdbc:mysql://localhost:3306/campus_safety
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=very-long-secret-key-minimum-256-characters
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
```

**Android (BuildConfig.java):**
```java
public class BuildConfig {
    public static final String API_BASE_URL = BuildConfig.DEBUG 
        ? "http://10.0.2.2:8080/api/"  // Development
        : "https://your-production-api.com/api/";  // Production
}
```

---

## Next Steps

1. ✅ Start backend server
2. ✅ Open Android app  
3. ✅ Register new user
4. ✅ Login with credentials
5. ✅ Test emergency alert creation
6. ✅ Test real-time chat
7. ✅ Deploy to production (Render/Railway)

---

## Production Deployment

### Deploy Backend to Render

1. Push code to GitHub
2. Go to render.com
3. Create new "Web Service"
4. Connect GitHub repository
5. Configure environment variables
6. Deploy

### Update Android App

1. Change BASE_URL to Render URL
2. Rebuild APK
3. Publish to Google Play Store

---

## Getting Help

- **Backend Logs:** Check terminal where `.\gradlew bootRun` is running
- **Android Logs:** View in Android Studio → Logcat
- **API Testing:** Use Postman to test endpoints directly
- **Database:** Use MySQL Workbench to view data

---

## Quick Commands

```bash
# Start backend
cd SpringBoot_server
.\gradlew bootRun

# Build backend
.\gradlew build -x test

# Check Java version
java -version

# Check if MySQL running
mysql -u root -p

# Stop backend
Ctrl + C (in terminal)
```

---

Good luck! 🚀
