@echo off
echo ===============================================
echo   SkillBridge Backend Startup Script
echo ===============================================

cd /d "C:\Finall\CNPM_BookApp\Demo\SkillBridge-English\skillbridge-backend\skillbridge-backend"

echo Checking if Maven is installed...
mvn --version
if %errorlevel% neq 0 (
    echo ERROR: Maven not found! Please install Maven first.
    pause
    exit /b 1
)

echo.
echo Checking if MySQL is running...
mysql -u root -p -e "SELECT 1;" 2>nul
if %errorlevel% neq 0 (
    echo WARNING: Cannot connect to MySQL. Make sure MySQL is running with:
    echo   - Host: localhost:3306
    echo   - Username: root
    echo   - Password: Admin@123
    echo   - Database: skillbridge_db
    echo.
    echo Continue anyway? (Y/N)
    set /p continue=
    if /i "%continue%" neq "Y" exit /b 1
)

echo.
echo Starting SkillBridge Backend...
echo Server will start on: http://localhost:8080/api
echo Swagger UI: http://localhost:8080/api/swagger-ui.html
echo.

mvn spring-boot:run

pause
