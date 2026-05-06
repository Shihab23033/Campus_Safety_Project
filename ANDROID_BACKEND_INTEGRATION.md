# Campus Safety: Android App & Spring Boot Backend Integration Guide

## Overview
This document explains how the Android mobile app communicates with the Spring Boot backend server, written in simple language so anyone can understand.

---

## What Is This Integration?

**In Simple Terms:**
- The **Android app** is like a customer at a restaurant (running on your phone)
- The **Spring Boot backend** is like the kitchen (running on a computer)
- When you order something (tap a button in the app), the order goes to the kitchen (backend)
- The kitchen prepares your order (processes data) and sends it back (returns results)

---

## Architecture Diagram

```
┌─────────────────────────┐
│    Android App          │
│  (Running on phone)     │
│                         │
│  ┌───────────────────┐  │
│  │  User Interface   │  │
│  │  (Screens, UI)    │  │
│  └─────────┬─────────┘  │
│            │            │
│  ┌─────────▼─────────┐  │
│  │  Retrofit Client  │  │──────────HTTP Requests─────────┐
│  │  (API Calls)      │  │                                 │
│  └───────────────────┘  │                                 │
└─────────────────────────┘                                 │
                                                             │
                                                             ▼
                                                  ┌──────────────────────┐
                                                  │ Spring Boot Backend   │
                                                  │ (Running on PC/Server)│
                                                  │                       │
                                                  │ ┌──────────────────┐ │
                                                  │ │  API Endpoints   │ │
                                                  │ │ (/auth, /alerts) │ │
                                                  │ └────────┬─────────┘ │
                                                  │          │           │
                                                  │ ┌────────▼─────────┐ │
                                                  │ │ Database (MySQL) │ │
                                                  │ │                  │ │
                                                  │ └──────────────────┘ │
                                                  └──────────────────────┘
```

---

## How It Works: Step-by-Step

### **Step 1: User Registration**

**Flow:**
1. User enters email & password in Android app
2. App collects the information
3. Android app sends this data to Backend using HTTP POST request
4. Backend receives the data at endpoint `/api/auth/register`
5. Backend validates the data
6. Backend stores user in MySQL database
7. Backend sends a response back (success or error)
8. Android app receives response and shows message to user

**Code Example (What Happens Behind the Scenes):**

```
Android App                          Spring Boot Backend
─────────────────────────────────────────────────────────

1. User taps "Register" button
2. App collects email & password
                                ──HTTP POST──────────>
                              /api/auth/register
                              
                                3. Backend receives data
                                4. Backend validates:
                                   - Is email valid?
                                   - Is password strong?
                                   - Is email already used?
                                   
                                5. Backend encrypts password
                                6. Backend saves to MySQL
                                
                     <──────JSON Response──────
                     {
                       "success": true,
                       "message": "User registered successfully"
                     }
                     
7. App shows "Registration successful"
```

---

### **Step 2: User Login & JWT Token**

**JWT Token = Digital Passport**

Think of JWT like a passport you get after login:
- You prove who you are (login with credentials)
- You get a digital passport (JWT token)
- For every future request, you show this passport
- The backend checks the passport and allows you access

**Flow:**

```
Android App                          Spring Boot Backend
─────────────────────────────────────────────────────────

1. User enters email & password
2. App sends login request
                                ──HTTP POST──────────>
                              /api/auth/login
                              {email, password}
                              
                                3. Backend verifies:
                                   - Find user by email
                                   - Check password matches
                                   
                                4. Backend generates JWT token:
                                   - Token = encoded(userId + roles + timestamp)
                                   - Token expires in 24 hours
                                
                     <──────JSON Response──────
                     {
                       "token": "eyJhbGciOiJIUzUxMiJ9...",
                       "user": {
                         "id": 1,
                         "email": "user@example.com",
                         "roles": ["MEMBER"]
                       }
                     }
                     
5. App saves token in secure storage (EncryptedSharedPreferences)
6. Token is now used for all future requests
```

---

### **Step 3: Making Authenticated Requests**

**What is "Authentication"?**
- Before login: You're unknown (like a stranger)
- After login: You have a token (like a VIP card)
- With token: Backend knows who you are and what you're allowed to do

**Flow When Sending Emergency Alert:**

```
Android App (with JWT token)       Spring Boot Backend
─────────────────────────────────────────────────────────

1. User taps "Send Emergency Alert"
2. App collects location & category
3. App reads JWT token from storage
4. App adds token to request header
                                ──HTTP POST──────────>
                              /api/alerts
                              Header: "Authorization: Bearer <token>"
                              Body: {category, latitude, longitude}
                              
                                5. Backend intercepts request
                                6. Backend checks:
                                   - Is Authorization header present?
                                   - Is token valid?
                                   - Has token expired?
                                   - Extract userId from token
                                   
                                7. Backend validates alert data
                                8. Backend saves alert with userId
                                9. Backend sends notification to responders
                                
                     <──────JSON Response──────
                     {
                       "id": 123,
                       "status": "ACTIVE",
                       "message": "Alert created successfully"
                     }
                     
10. App shows "Emergency alert sent"
11. App updates UI with alert status
```

---

## Connection Configuration

### **File Location:** 
`Android_App/app/src/main/java/com/mbstu/campussafety/data/remote/RetrofitClient.java`

### **Configuration Details:**

```java
// For Android Emulator (running on your computer)
private static final String BASE_URL = "http://10.0.2.2:8080/api/";

// Explanation:
// 10.0.2.2 = Special IP that means "host computer" in Android Emulator
// 8080 = Port number where Spring Boot server is running
// /api/ = Context path configured in Spring Boot
```

### **For Different Scenarios:**

```
SCENARIO 1: Android Emulator (on your computer)
└─ Use: http://10.0.2.2:8080/api/
└─ Why: 10.0.2.2 routes to localhost inside emulator

SCENARIO 2: Physical Android Device (same WiFi as backend server)
└─ Use: http://<YOUR_COMPUTER_IP>:8080/api/
└─ Example: http://192.168.1.100:8080/api/
└─ How to find IP: Open Command Prompt, type: ipconfig

SCENARIO 3: Production (deployed to cloud)
└─ Use: https://your-deployed-backend.com/api/
└─ Example: https://campus-safety.render.com/api/
```

---

## How Android App Sends Requests

### **Step 1: Create a Model (Data Container)**

```java
// This is like a form where you fill information
public class LoginRequest {
    String email;
    String password;
}
```

### **Step 2: Define API Service (List of Actions)**

```java
public interface ApiService {
    @POST("auth/login")  // What endpoint to call
    Call<AuthResponse> login(@Body LoginRequest request);  // What data to send & what to expect back
}
```

### **Step 3: Use RetrofitClient (The Messenger)**

```java
// Get API service instance
ApiService apiService = RetrofitClient.getApiService(context);

// Send request
apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
    @Override
    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
        // Success - got response from backend
        AuthResponse result = response.body();
        String token = result.getToken();
        // Save token and show success message
    }
    
    @Override
    public void onFailure(Call<AuthResponse> call, Throwable t) {
        // Failed - couldn't reach backend
        showError("Connection failed: " + t.getMessage());
    }
});
```

---

## JWT Token Authentication Explained

### **How Does Backend Know If Request Is Valid?**

```
Request comes with token in header:
┌─────────────────────────────────────┐
│ Authorization: Bearer <JWT_TOKEN>   │
└─────────────────────────────────────┘
                ↓
        Backend receives it
                ↓
    ┌─────────────────────────────┐
    │ Check 1: Token format valid? │  YES ✓
    └──────────┬──────────────────┘
               ↓
    ┌─────────────────────────────┐
    │ Check 2: Token not expired? │  YES ✓
    └──────────┬──────────────────┘
               ↓
    ┌─────────────────────────────┐
    │ Check 3: Signature valid?   │  YES ✓
    └──────────┬──────────────────┘
               ↓
    ┌─────────────────────────────┐
    │ Check 4: User has permission?
    │ (Check user role)            │  YES ✓
    └──────────┬──────────────────┘
               ↓
        ✓ REQUEST ALLOWED
        Process request and send response
```

### **Automatic Token Addition**

The Android app automatically adds the token to every request using an **Interceptor**:

```java
// This happens AUTOMATICALLY for every request
Request originalRequest = chain.request();
String token = getToken();  // Read from secure storage

if (token != null) {
    // Add token to request header
    requestBuilder.header("Authorization", "Bearer " + token);
}
```

---

## Setting Up the Connection

### **Prerequisites**

1. **Spring Boot Backend**
   - [ ] Running on your computer (http://localhost:8080)
   - [ ] MySQL database configured and running
   - [ ] Roles table has: MEMBER, RESPONDER, ADMIN

2. **Android App**
   - [ ] Built and deployed on emulator or physical device
   - [ ] Same WiFi network (for physical device)
   - [ ] Required permissions in AndroidManifest.xml:
     - `INTERNET` - to make HTTP requests
     - `ACCESS_FINE_LOCATION` - for GPS location
     - `ACCESS_NETWORK_STATE` - to check network status

### **Step-by-Step Setup**

**Step 1: Start MySQL Database**
```
1. Open MySQL Workbench or command line
2. Start MySQL service
3. Create database: campus_safety
4. Verify it's running
```

**Step 2: Start Spring Boot Backend**
```
1. Open Terminal in SpringBoot_server folder
2. Run: .\gradlew bootRun
3. Verify message: "Started CampusSafetyApplication"
4. Check: http://localhost:8080/api/swagger-ui.html
5. All endpoints should be listed
```

**Step 3: Configure Android App**
```
1. Open Android Studio
2. Go to RetrofitClient.java
3. Set BASE_URL based on your scenario:
   - Emulator: http://10.0.2.2:8080/api/
   - Physical device: http://<PC_IP>:8080/api/
4. Save and rebuild app
```

**Step 4: Run Android App**
```
1. Click "Run" in Android Studio
2. Select emulator or device
3. App opens and displays login screen
4. Try logging in with a test account
```

---

## Testing the Connection

### **Test 1: Check Backend Is Running**

**On Your Computer:**
1. Open browser
2. Go to: `http://localhost:8080/api/swagger-ui.html`
3. You should see API documentation
4. All endpoints should be listed

### **Test 2: Test Registration from Android App**

**In Android App:**
1. Open app
2. Tap "Register" button
3. Fill in: email, password, name
4. Tap "Register"

**What to Expect:**
- Success: "Registration successful. Please verify your email"
- Error: Check backend console for error message

### **Test 3: Test Login**

**In Android App:**
1. Tap "Login" button
2. Enter email & password
3. Tap "Login"

**What to Expect:**
- Success: App shows home screen
- Error: "Invalid credentials" or "User not found"

### **Common Connection Issues & Solutions**

| Issue | Cause | Solution |
|-------|-------|----------|
| "Failed to connect to localhost" | Backend not running | Start Spring Boot backend |
| "Connection timeout" | Wrong IP address | Check BASE_URL is correct |
| "Invalid token" | Token expired | Login again to get new token |
| "Unauthorized 401" | No token in request | Logout and login again |
| "User not found" | Email not registered | Register first before login |
| "CORS error" | Backend CORS config wrong | Check SecurityConfig.java |

---

## Important Endpoints Reference

### **Authentication Endpoints**
```
POST   /api/auth/register          - User registration
POST   /api/auth/login             - User login (get JWT token)
POST   /api/auth/verify-otp        - Verify email with OTP
POST   /api/auth/forgot-password   - Request password reset
POST   /api/auth/reset-password    - Reset password with token
GET    /api/auth/me                - Get current user profile
```

### **Emergency Alert Endpoints**
```
POST   /api/alerts                 - Create new emergency alert
GET    /api/alerts                 - Get all active alerts
GET    /api/alerts/{id}            - Get specific alert details
PUT    /api/alerts/{id}/status     - Update alert status
```

### **Location Endpoints**
```
POST   /api/locations/update       - Send user location update
GET    /api/locations/current      - Get current user location
GET    /api/locations/history      - Get location history
```

### **Chat Endpoints**
```
POST   /api/chat/messages          - Send message
GET    /api/chat/conversations     - Get chat history
```

---

## Security Notes

### **Never Share Your Token**
- JWT token is like your password
- If someone gets your token, they can access your account
- Token is stored in **EncryptedSharedPreferences** (encrypted storage on Android)

### **Token Expiration**
- Token expires after 24 hours
- User must login again to get new token
- This is a security feature

### **HTTPS in Production**
- For development: HTTP (http://localhost:8080) is fine
- For production: Must use HTTPS (https://...) 
- HTTPS encrypts all communication

---

## Troubleshooting Checklist

- [ ] MySQL is running and database exists
- [ ] Spring Boot backend is running (`http://localhost:8080/api/swagger-ui.html` works)
- [ ] Android app has INTERNET permission
- [ ] BASE_URL is correct in RetrofitClient.java
- [ ] Token is being saved after login
- [ ] Backend logs show incoming requests
- [ ] CORS is configured for your app's domain

---

## What Happens Behind the Scenes (Technical Deep Dive)

### **When User Registers:**

1. **Android App (Client-Side)**
   - Validates input locally
   - Creates JSON body: `{"email": "...", "password": "..."}`
   - Sends POST request to `/api/auth/register`

2. **Network (HTTP Protocol)**
   - Request travels over internet to backend server
   - Headers: `Content-Type: application/json`, `Accept: application/json`

3. **Spring Boot Backend (Server-Side)**
   - Receives request in `AuthController.register()`
   - Deserialization: JSON → Java object
   - Validation: Checks email format, password strength
   - Business Logic: `AuthService.register()` called
   - Database Operation: Data saved to MySQL via `UserRepository`
   - Response Created: `{"success": true, "message": "..."}`
   - Serialization: Java object → JSON

4. **Network (HTTP Response)**
   - Response travels back to Android app
   - Status Code: 201 (Created) or 200 (OK) or 400 (Bad Request)

5. **Android App (Client-Side)**
   - Receives response
   - Deserialization: JSON → Java object
   - UI Update: Shows success/error message to user

---

## Summary

**In One Sentence:**
The Android app communicates with the Spring Boot backend by sending HTTP requests with JWT tokens, and the backend processes these requests, accesses the database, and sends back responses.

**The Key Components:**
1. **RetrofitClient** - Sets up connection to backend
2. **ApiService** - Defines available endpoints
3. **JWT Token** - Used for authentication
4. **Interceptor** - Automatically adds token to all requests
5. **EncryptedSharedPreferences** - Securely stores token on Android
6. **Spring Boot Backend** - Processes requests and manages data

---

## Next Steps

1. **Test the connection** using the testing steps above
2. **Make your first API call** - Try register/login
3. **Implement more features** - Add more endpoints as needed
4. **Deploy to production** - When ready, deploy backend to cloud service (Render, Railway)
5. **Update Android app** - Change BASE_URL to production URL

---

**Need Help?**
- Check backend logs: Terminal where backend is running
- Check Android logs: Logcat in Android Studio
- Check API documentation: http://localhost:8080/api/swagger-ui.html
- Check network requests: Use Postman/Insomnia to test backend directly
