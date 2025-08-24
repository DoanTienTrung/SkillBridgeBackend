-- Test script cho Analytics API
-- Chạy backend và test các endpoints sau với Postman hoặc curl

-- 1. Test System Analytics
-- GET http://localhost:8080/analytics/system
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- 2. Test Weekly Activity
-- GET http://localhost:8080/analytics/weekly-activity
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- 3. Test All Lessons Analytics
-- GET http://localhost:8080/analytics/lessons
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- 4. Test Single Lesson Analytics
-- GET http://localhost:8080/analytics/lessons/1?lessonType=LISTENING
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- 5. Test Student Report
-- GET http://localhost:8080/analytics/students/4/report
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- 6. Test All Students Reports
-- GET http://localhost:8080/analytics/students/reports
-- Header: Authorization: Bearer YOUR_JWT_TOKEN

-- Để test, bạn cần:
-- 1. Chạy backend server
-- 2. Đăng nhập với tài khoản TEACHER hoặc ADMIN để lấy JWT token
-- 3. Sử dụng token trong Authorization header
-- 4. Call các endpoints trên

-- Login endpoint để lấy token:
-- POST http://localhost:8080/auth/login
-- Body: {
--   "email": "teacher1@skillbridge.com",
--   "password": "password123"
-- }
