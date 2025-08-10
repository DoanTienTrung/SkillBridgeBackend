# Database Setup for SkillBridge Backend

## Bước 1: Tạo database
```sql
-- Kết nối MySQL với user root
mysql -u root -p

-- Tạo database
CREATE DATABASE IF NOT EXISTS skillbridge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Kiểm tra database đã tạo
SHOW DATABASES;

-- Sử dụng database
USE skillbridge_db;

-- Exit MySQL
exit;
```

## Bước 2: Kiểm tra connection string
File: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/skillbridge_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Admin@123
```

## Bước 3: Test connection
```bash
mysql -h localhost -P 3306 -u root -pAdmin@123 -e "USE skillbridge_db; SELECT 'Connection OK' as status;"
```

## Lỗi thường gặp:

### 1. "Access denied for user 'root'"
- Đổi password trong application.properties
- Hoặc reset MySQL root password

### 2. "Unknown database 'skillbridge_db'"
- Chạy lại lệnh CREATE DATABASE ở trên

### 3. "Connection refused"
- Khởi động MySQL service:
  - Windows: `net start mysql80` (hoặc tên service MySQL của bạn)
  - XAMPP: Start MySQL trong XAMPP Control Panel

### 4. Port 3306 đã sử dụng
- Check port: `netstat -an | findstr :3306`
- Change port trong application.properties nếu cần
