package com.skillbridge.skillbridgebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response khi upload file audio thành công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadResponse {
    
    /**
     * URL để truy cập file audio đã upload
     */
    private String audioUrl;
    
    /**
     * Tên file gốc được upload
     */
    private String originalFileName;
    
    /**
     * Tên file đã được lưu trên server (unique)
     */
    private String savedFileName;
    
    /**
     * Kích thước file tính bằng bytes
     */
    private long fileSizeBytes;
    
    /**
     * Kích thước file dưới dạng readable (VD: 1.5 MB)
     */
    private String formattedFileSize;
    
    /**
     * Định dạng file (mp3, wav, m4a, etc.)
     */
    private String fileFormat;
    
    /**
     * MIME type của file
     */
    private String mimeType;
    
    /**
     * Thời gian upload (timestamp)
     */
    private long uploadTimestamp;
    
    /**
     * Message mô tả kết quả upload
     */
    private String message;

    /**
     * Constructor with essential fields
     */
    public AudioUploadResponse(String audioUrl, String originalFileName, long fileSizeBytes, String fileFormat) {
        this.audioUrl = audioUrl;
        this.originalFileName = originalFileName;
        this.fileSizeBytes = fileSizeBytes;
        this.fileFormat = fileFormat;
        this.uploadTimestamp = System.currentTimeMillis();
        this.message = "Upload file thành công";
    }
}