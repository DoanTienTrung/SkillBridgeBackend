package com.skillbridge.skillbridgebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response của Cloudinary upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryUploadResponse {
    
    private String publicId;
    private String secureUrl;
    private String url;
    private String format;
    private String resourceType;
    private Long fileSizeBytes;
    private String formattedFileSize;
    private Double duration; // Thời lượng audio (seconds)
    private String formattedDuration;
    private Integer width;   // Chiều rộng cho image
    private Integer height;  // Chiều cao cho image
    private String dimensions; // Kích thước ảnh
    private String originalFileName;
    private String uniqueFileName;
    private String mimeType;
    private Long uploadTimestamp;
    private String message;
    
    /**
     * Tạo CloudinaryUploadResponse từ CloudinaryUploadResult
     */
    public static CloudinaryUploadResponse fromCloudinaryResult(
            com.skillbridge.skillbridgebackend.Service.CloudinaryService.CloudinaryUploadResult result,
            String mimeType) {
        
        String message = "video".equals(result.getResourceType()) ? 
                "Upload file audio thành công lên Cloudinary" : 
                "Upload file ảnh thành công lên Cloudinary";
        
        return CloudinaryUploadResponse.builder()
                .publicId(result.getPublicId())
                .secureUrl(result.getSecureUrl())
                .url(result.getUrl())
                .format(result.getFormat())
                .resourceType(result.getResourceType())
                .fileSizeBytes(result.getBytes())
                .formattedFileSize(result.getFormattedFileSize())
                .duration(result.getDuration())
                .formattedDuration(result.getFormattedDuration())
                .width(result.getWidth())
                .height(result.getHeight())
                .dimensions(result.getDimensions())
                .originalFileName(result.getOriginalFileName())
                .uniqueFileName(result.getUniqueFileName())
                .mimeType(mimeType)
                .uploadTimestamp(result.getUploadTimestamp())
                .message(message)
                .build();
    }
}
