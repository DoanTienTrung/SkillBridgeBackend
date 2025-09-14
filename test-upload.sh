#!/bin/bash

# Test script cho Cloudinary upload API
# Sử dụng: ./test-upload.sh <path-to-audio-file> <jwt-token>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <audio-file-path> <jwt-token>"
    echo "Example: $0 test.mp3 eyJhbGciOiJIUzUxMiJ9..."
    exit 1
fi

AUDIO_FILE=$1
JWT_TOKEN=$2
API_URL="http://localhost:8080/api/audio/upload"

echo "🎵 Testing Cloudinary Upload API"
echo "📁 File: $AUDIO_FILE"
echo "🌐 URL: $API_URL"
echo "🔑 Token: ${JWT_TOKEN:0:20}..."
echo ""

# Test upload
echo "🚀 Starting upload..."
curl -X POST "$API_URL" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "file=@$AUDIO_FILE" \
  -w "\n📊 Response Time: %{time_total}s\n📏 Response Size: %{size_download} bytes\n🔗 HTTP Code: %{http_code}\n" \
  -v

echo ""
echo "✅ Test completed!"
