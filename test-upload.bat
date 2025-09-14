@echo off
REM Test script cho Cloudinary upload API (Windows)
REM Sử dụng: test-upload.bat <path-to-audio-file> <jwt-token>

if "%~2"=="" (
    echo Usage: %0 ^<audio-file-path^> ^<jwt-token^>
    echo Example: %0 test.mp3 eyJhbGciOiJIUzUxMiJ9...
    exit /b 1
)

set AUDIO_FILE=%1
set JWT_TOKEN=%2
set API_URL=http://localhost:8080/api/audio/upload

echo 🎵 Testing Cloudinary Upload API
echo 📁 File: %AUDIO_FILE%
echo 🌐 URL: %API_URL%
echo 🔑 Token: %JWT_TOKEN:~0,20%...
echo.

echo 🚀 Starting upload...
curl -X POST "%API_URL%" -H "Authorization: Bearer %JWT_TOKEN%" -F "file=@%AUDIO_FILE%" -w "Response Time: %%{time_total}s HTTP Code: %%{http_code}" -v

echo.
echo ✅ Test completed!
pause
