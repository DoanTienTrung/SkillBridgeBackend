# JWT Implementation Test Guide

## 🔐 JWT Functionality Overview

SkillBridge application now has complete JWT authentication implementation:

### **Features Implemented:**
- ✅ JWT Token Generation with user info
- ✅ JWT Token Validation and Parsing
- ✅ Protected endpoints with role-based access
- ✅ Token info extraction endpoint
- ✅ Helper utilities for controllers

---

## 🧪 Testing JWT Endpoints

### **1. Register New User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User",
  "school": "Test University",
  "major": "Computer Science",
  "academicYear": "2023"
}'
```

### **2. Login and Get JWT Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{
  "email": "test@example.com",
  "password": "password123"
}'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJTVFVERU5UIiwiZnVsbE5hbWUiOiJUZXN0IFVzZXIiLCJpc0FjdGl2ZSI6dHJ1ZSwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTY5MjUyODAwMCwiZXhwIjoxNjkyNjE0NDAwfQ.signature",
    "user": {
      "id": 1,
      "email": "test@example.com",
      "fullName": "Test User",
      "role": "STUDENT"
    }
  }
}
```

### **3. Test Protected Endpoints**

**Get Current User Info:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get JWT Token Info:**
```bash
curl -X GET http://localhost:8080/api/auth/token-info \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Token Info Response:**
```json
{
  "success": true,
  "message": "Token info retrieved successfully",
  "data": {
    "email": "test@example.com",
    "userId": 1,
    "role": "STUDENT",
    "fullName": "Test User",
    "isActive": true,
    "issuedAt": "2023-08-20T10:00:00",
    "expiresAt": "2023-08-21T10:00:00"
  }
}
```

---

## 🎯 Swagger UI Testing

### **Access Swagger UI:**
```
http://localhost:8080/api/swagger-ui.html
```

### **JWT Authentication in Swagger:**

1. **Login** via `/auth/login` endpoint
2. **Copy the JWT token** from response
3. **Click "Authorize" button** in Swagger UI
4. **Enter token** (without "Bearer " prefix)
5. **Test protected endpoints**

---

## 🔑 JWT Token Structure

**JWT Payload includes:**
```json
{
  "sub": "user@example.com",        // User email
  "userId": 1,                      // User ID
  "role": "STUDENT",                // User role
  "fullName": "User Name",          // User full name
  "isActive": true,                 // Account status
  "iat": 1692528000,               // Issued at
  "exp": 1692614400                // Expires at
}
```

**Token Expiration:** 24 hours (86400000 ms)

---

## 🛡️ Security Features

### **Protected Endpoints:**
- `/auth/me` - Get current user info
- `/auth/token-info` - Get JWT token details
- `/users/profile` - User profile management
- `/listening-lessons` (POST, PUT) - Lesson management (Teacher/Admin only)

### **Role-Based Access:**
- **STUDENT** - Basic user access
- **TEACHER** - Can create/manage lessons + student access
- **ADMIN** - Full system access

### **Error Responses:**
```json
{
  "success": false,
  "message": "Unauthorized",
  "error": "Access denied. Please provide valid authentication token."
}
```

---

## 🔧 Development Usage

### **In Controllers:**
```java
@Autowired
private JwtHelper jwtHelper;

// Get current user
User currentUser = jwtHelper.getCurrentUser();

// Check roles
if (jwtHelper.isAdmin()) {
    // Admin logic
}

// Get user ID
Long userId = jwtHelper.getCurrentUserId();
```

### **Manual Token Extraction:**
```java
String token = jwtHelper.extractTokenFromRequest(request);
String email = jwtUtil.getEmailFromToken(token);
Long userId = jwtUtil.getUserIdFromToken(token);
```

---

## ⚡ Quick Test Script

```bash
#!/bin/bash

# 1. Register user
echo "1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{"email": "testjwt@example.com", "password": "password123", "fullName": "JWT Test User"}')

echo $REGISTER_RESPONSE

# 2. Login and extract token
echo -e "\n2. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "testjwt@example.com", "password": "password123"}')

echo $LOGIN_RESPONSE

# Extract token (requires jq)
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')

# 3. Test protected endpoint
echo -e "\n3. Testing protected endpoint..."
curl -s -X GET http://localhost:8080/api/auth/me \
-H "Authorization: Bearer $TOKEN"

# 4. Test token info
echo -e "\n4. Getting token info..."
curl -s -X GET http://localhost:8080/api/auth/token-info \
-H "Authorization: Bearer $TOKEN"
```

---

## 🎉 JWT Implementation Complete!

**All JWT functionality is now working:**
- ✅ Token generation with user data
- ✅ Token validation and parsing
- ✅ Protected endpoints
- ✅ Role-based access control
- ✅ Helper utilities
- ✅ Swagger integration
- ✅ Error handling
