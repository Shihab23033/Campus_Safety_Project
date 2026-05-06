# Campus Safety - API Reference

**Base URL**: `http://localhost:8080/api` (development)  
**API Version**: 1.0.0  
**Authentication**: Bearer JWT Token in Authorization header

---

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [User Management](#user-management)
3. [Emergency Alerts](#emergency-alerts)
4. [Location Tracking](#location-tracking)
5. [Chat Messaging](#chat-messaging)
6. [Audio Module](#audio-module)
7. [Notifications](#notifications)
8. [Safe Zones](#safe-zones)

---

## Authentication Endpoints

### Register User
```
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}

Response (201):
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "otpSent": true
  },
  "message": "Registration successful. Check email for OTP."
}
```

### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}

Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  },
  "message": "Login successful"
}
```

### Verify OTP
```
POST /auth/verify-otp
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}

Response (200):
{
  "success": true,
  "message": "Email verified successfully"
}
```

### Resend OTP
```
POST /auth/resend-otp
Content-Type: application/json

{
  "email": "user@example.com"
}

Response (200):
{
  "success": true,
  "message": "OTP sent successfully"
}
```

### Forgot Password
```
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response (200):
{
  "success": true,
  "message": "Password reset link sent to email"
}
```

### Reset Password
```
POST /auth/reset-password
Content-Type: application/json

{
  "email": "user@example.com",
  "resetToken": "reset_token_from_email",
  "newPassword": "NewPassword123!"
}

Response (200):
{
  "success": true,
  "message": "Password reset successfully"
}
```

### Get Current User
```
GET /auth/me
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["MEMBER"]
  },
  "message": "User retrieved successfully"
}
```

### Logout
```
POST /auth/logout
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "Logout successful"
}
```

---

## User Management

### Get All Users
```
GET /users?page=0&size=20
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+1234567890",
      "roles": ["MEMBER"]
    }
  ],
  "message": "Users retrieved successfully"
}
```

### Get User by ID
```
GET /users/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  },
  "message": "User retrieved successfully"
}
```

### Get Users by Role
```
GET /users/role/{role}
Authorization: Bearer {token}

Roles: ADMIN, RESPONDER, MEMBER

Response (200):
{
  "success": true,
  "data": [...],
  "message": "Users retrieved successfully"
}
```

### Update User Profile
```
PUT /users/profile
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "phoneNumber": "+9876543210"
}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "Jane",
    "lastName": "Doe"
  },
  "message": "Profile updated successfully"
}
```

### Register FCM Token
```
POST /users/fcm-token
Authorization: Bearer {token}
Content-Type: application/json

{
  "token": "android_fcm_device_token"
}

Response (200):
{
  "success": true,
  "message": "FCM token registered successfully"
}
```

### Delete User
```
DELETE /users/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

## Emergency Alerts

### Create Emergency Alert
```
POST /alerts
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Medical Emergency",
  "description": "Person collapsed at Science building",
  "category": "Medical Accident",
  "latitude": 23.8103,
  "longitude": 90.3563
}

Categories: Fire, Medical Accident, Security threat, Natural disaster

Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Medical Emergency",
    "category": "Medical Accident",
    "status": "ACTIVE",
    "creator": {...},
    "assignedResponders": [
      {
        "id": 2,
        "email": "responder@example.com",
        "phoneNumber": "+1111111111"
      }
    ],
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "message": "Emergency alert created. Responders assigned automatically."
}
```

### Get Active Emergencies
```
GET /alerts/active
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [...],
  "message": "Active alerts retrieved successfully"
}
```

### Get Alert by ID
```
GET /alerts/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Medical Emergency",
    "description": "...",
    "category": "Medical Accident",
    "status": "ACTIVE",
    "latitude": 23.8103,
    "longitude": 90.3563,
    "creator": {...},
    "assignedResponders": [...],
    "locationHistory": [...],
    "audioFiles": [...],
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "message": "Alert retrieved successfully"
}
```

### Get User Alert History
```
GET /alerts/user/history?page=0&size=20
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 45,
    "totalPages": 3
  },
  "message": "Alert history retrieved successfully"
}
```

### Update Alert Status
```
PUT /alerts/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "RESOLVED"
}

Status: ACTIVE, RESOLVED, CANCELLED

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "RESOLVED",
    "resolvedAt": "2024-01-15T11:45:00Z"
  },
  "message": "Alert status updated successfully"
}
```

### Assign Responder
```
POST /alerts/{id}/assign-responder
Authorization: Bearer {token}
Content-Type: application/json

{
  "responderId": 2
}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "assignedResponders": [...]
  },
  "message": "Responder assigned successfully"
}
```

### Get Alert History with Date Range
```
GET /alerts/history?startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [...],
  "message": "Alert history retrieved successfully"
}
```

---

## Location Tracking

### Update Location
```
POST /locations/update
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

latitude=23.8103&longitude=90.3563&accuracy=5.0

Response (200):
{
  "success": true,
  "message": "Location updated successfully"
}
```

### Get Current Location
```
GET /locations/current
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "latitude": 23.8103,
    "longitude": 90.3563,
    "accuracy": 5.0,
    "timestamp": "2024-01-15T10:45:00Z"
  },
  "message": "Current location retrieved"
}
```

### Get Location History
```
GET /locations/history?startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [
    {
      "latitude": 23.8103,
      "longitude": 90.3563,
      "accuracy": 5.0,
      "timestamp": "2024-01-15T10:45:00Z"
    }
  ],
  "message": "Location history retrieved"
}
```

### Get Safe Zones
```
GET /locations/safe-zones
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Medical Center",
      "latitude": 23.8100,
      "longitude": 90.3560,
      "radius": 500,
      "description": "Campus hospital and medical facilities"
    }
  ],
  "message": "Safe zones retrieved successfully"
}
```

### Check If In Safe Zone
```
GET /locations/in-safe-zone
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": true,
  "message": "Safe zone check completed"
}
```

---

## Safe Zones (Admin Only)

### Create Safe Zone
```
POST /safe-zones
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "Medical Center",
  "latitude": 23.8100,
  "longitude": 90.3560,
  "radius": 500,
  "description": "Campus hospital"
}

Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Medical Center",
    "latitude": 23.8100,
    "longitude": 90.3560,
    "radius": 500
  },
  "message": "Safe zone created successfully"
}
```

### Update Safe Zone
```
PUT /safe-zones/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "Updated Name",
  "radius": 600
}

Response (200):
{
  "success": true,
  "data": {...},
  "message": "Safe zone updated successfully"
}
```

### Delete Safe Zone
```
DELETE /safe-zones/{id}
Authorization: Bearer {admin_token}

Response (200):
{
  "success": true,
  "message": "Safe zone deleted successfully"
}
```

### Get Safe Zone by ID
```
GET /safe-zones/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {...},
  "message": "Safe zone retrieved successfully"
}
```

---

## Chat Messaging

### WebSocket Connection
```
WebSocket URL: ws://localhost:8080/ws/chat
Or with SockJS fallback: http://localhost:8080/ws/chat

STOMP:
CONNECT
accept-version:1.0,1.1,1.2
```

### Send One-to-One Message (WebSocket)
```
SEND
destination:/app/chat/send
content-type:application/json

{
  "recipientId": 2,
  "message": "Are you safe?",
  "timestamp": "2024-01-15T10:45:00Z"
}

Receive at: /topic/messages
```

### Send Group Message (WebSocket)
```
SEND
destination:/app/chat/group/send
content-type:application/json

{
  "groupChatId": 1,
  "message": "Emergency team gathering at main gate",
  "timestamp": "2024-01-15T10:45:00Z"
}

Receive at: /topic/group-messages
```

### Typing Indicator (WebSocket)
```
SEND
destination:/app/chat/typing
content-type:application/json

{
  "recipientId": 2,
  "isTyping": true
}

Receive at: /topic/typing
```

### Create Group Chat
```
POST /chat/group/{alertId}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Emergency Alert #1",
    "participants": [...],
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "message": "Group chat created successfully"
}
```

### Get Unread Message Count
```
GET /chat/unread/count
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": 5,
  "message": "Unread message count retrieved"
}
```

### Mark Message as Read
```
PUT /chat/{messageId}/read
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "Message marked as read"
}
```

---

## Audio Module

### Upload Audio File
```
POST /audio/upload/{alertId}
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [binary audio file]

Supported formats: mp3, wav, aac, m4a, flac
Max size: 50 MB

Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "fileName": "audio_123456.mp3",
    "fileSize": 2048576,
    "mimeType": "audio/mpeg",
    "duration": 180,
    "uploadedAt": "2024-01-15T10:45:00Z"
  },
  "message": "Audio file uploaded successfully"
}
```

### Download Audio File
```
GET /audio/{id}
Authorization: Bearer {token}

Response (200):
Header: Content-Disposition: attachment; filename="audio_123456.mp3"
Header: Content-Type: audio/mpeg
Body: [binary audio data]
```

### Get Audio Metadata
```
GET /audio/{id}/metadata
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "fileName": "audio_123456.mp3",
    "fileSize": 2048576,
    "mimeType": "audio/mpeg",
    "duration": 180,
    "uploadedAt": "2024-01-15T10:45:00Z"
  },
  "message": "Audio metadata retrieved"
}
```

### Get Alert Audio Files
```
GET /audio/alert/{alertId}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": [...],
  "message": "Audio files retrieved successfully"
}
```

### Delete Audio File
```
DELETE /audio/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "Audio file deleted successfully"
}
```

---

## Notifications

### Get User Notifications
```
GET /notifications?page=0&size=20
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Emergency Alert",
        "message": "Medical emergency reported near Library",
        "type": "ALERT",
        "isRead": false,
        "createdAt": "2024-01-15T10:45:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  },
  "message": "Notifications retrieved successfully"
}
```

### Get Unread Notification Count
```
GET /notifications/unread/count
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": 5,
  "message": "Unread notification count retrieved"
}
```

### Mark Notification as Read
```
PUT /notifications/{id}/read
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "Notification marked as read"
}
```

### Delete Notification
```
DELETE /notifications/{id}
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "message": "Notification deleted successfully"
}
```

### Send Test Notification (Admin Only)
```
POST /notifications/send
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "userId": 1,
  "title": "Test Notification",
  "message": "This is a test notification",
  "type": "TEST"
}

Response (200):
{
  "success": true,
  "data": {...},
  "message": "Notification sent successfully"
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "Human readable error message"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| UNAUTHORIZED | 401 | Missing or invalid token |
| FORBIDDEN | 403 | User lacks required permissions |
| NOT_FOUND | 404 | Resource not found |
| CONFLICT | 409 | Resource already exists |
| INVALID_INPUT | 400 | Request validation failed |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |
| INVALID_TOKEN | 401 | Token expired or tampered |
| USER_NOT_FOUND | 404 | User doesn't exist |
| EMAIL_ALREADY_EXISTS | 409 | Email already registered |

---

## Authentication

### How to Use Bearer Token
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjA3OTAxMjAwLCJleHAiOjE2MDc5ODc2MDB9.signature
```

### Token Expiration
- Tokens expire after 24 hours
- Request `/auth/login` again to get a new token
- Token contains: userId, email, firstName, lastName, roles

---

## Rate Limiting

- API limits: 100 requests per minute per user
- Response header: `X-RateLimit-Remaining`

---

**API Version**: 1.0.0  
**Last Updated**: 2024  
**Support**: api-support@campussafety.com
