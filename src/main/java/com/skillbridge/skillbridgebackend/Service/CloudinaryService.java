package com.skillbridge.skillbridgebackend.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Service xử lý upload file audio lên Cloudinary
 */
@Service
@Slf4j
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    private static final String AUDIO_FOLDER = "skillbridge/audio";
    private static final String IMAGE_FOLDER = "skillbridge/images";
    private static final long MAX_AUDIO_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_AUDIO_FORMATS = {"mp3", "wav", "m4a", "aac", "ogg", "flac"};
    private static final String[] ALLOWED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

    /**
     * Upload file ảnh lên Cloudinary
     * @param file File ảnh cần upload
     * @return CloudinaryUploadResult chứa thông tin file đã upload
     */
    public CloudinaryUploadResult uploadImage(MultipartFile file) {
        try {
            // Validate file
            validateImageFile(file);

            // Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = generateUniqueFileName(originalFileName);

            log.info("Uploading image file: {} to Cloudinary", originalFileName);

            // Upload file lên Cloudinary với cấu hình cho image
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", IMAGE_FOLDER + "/" + uniqueFileName,
                            "resource_type", "image",
                            "format", fileExtension,
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );

            log.info("Successfully uploaded image file to Cloudinary: {}", uploadResult.get("public_id"));

            // Tạo response object
            return CloudinaryUploadResult.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .url((String) uploadResult.get("url"))
                    .format((String) uploadResult.get("format"))
                    .resourceType((String) uploadResult.get("resource_type"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .width(uploadResult.get("width") != null ?
                            ((Number) uploadResult.get("width")).intValue() : null)
                    .height(uploadResult.get("height") != null ?
                            ((Number) uploadResult.get("height")).intValue() : null)
                    .originalFileName(originalFileName)
                    .uniqueFileName(uniqueFileName)
                    .uploadTimestamp(System.currentTimeMillis())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading image file to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi upload file lên Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file ảnh từ Cloudinary
     * @param publicId Public ID của file cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteImage(String publicId) {
        try {
            log.info("Deleting image file from Cloudinary: {}", publicId);

            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );

            String result = (String) deleteResult.get("result");
            boolean isDeleted = "ok".equals(result);

            if (isDeleted) {
                log.info("Successfully deleted image file from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete image file from Cloudinary: {}, result: {}", publicId, result);
            }

            return isDeleted;

        } catch (IOException e) {
            log.error("Error deleting image file from Cloudinary: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy URL của file ảnh từ Cloudinary với transformation
     * @param publicId Public ID của file
     * @param width Chiều rộng mong muốn (optional)
     * @param height Chiều cao mong muốn (optional)
     * @return Secure URL của file
     */
    public String getImageUrl(String publicId, Integer width, Integer height) {
        if (width != null && height != null) {
            return cloudinary.url()
                    .resourceType("image")
                    .secure(true)
                    .transformation(new Transformation()
                            .width(width)
                            .height(height)
                            .crop("fill")
                            .quality("auto:good"))
                    .publicId(publicId)
                    .generate();
        } else {
            return cloudinary.url()
                    .resourceType("image")
                    .secure(true)
                    .publicId(publicId)
                    .generate();
        }
    }

    /**
     * Validate file ảnh
     * @param file File cần validate
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Kiểm tra kích thước file (10MB cho ảnh)
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File ảnh quá lớn. Kích thước tối đa là 10MB");
        }

        // Kiểm tra định dạng file
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        boolean isValidFormat = false;
        for (String allowedFormat : ALLOWED_IMAGE_FORMATS) {
            if (allowedFormat.equals(fileExtension)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IllegalArgumentException("Định dạng file không hỗ trợ. Chỉ chấp nhận: jpg, jpeg, png, gif, bmp, webp");
        }

        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File không phải là file ảnh hợp lệ");
        }
    }
    /**
     * Upload file audio lên Cloudinary
     * @param file File audio cần upload
     * @return CloudinaryUploadResult chứa thông tin file đã upload
     */
    public CloudinaryUploadResult uploadAudio(MultipartFile file) {
        try {
            // Validate file
            validateAudioFile(file);

            // Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = generateUniqueFileName(originalFileName);

            log.info("Uploading audio file: {} to Cloudinary", originalFileName);

            // Upload file lên Cloudinary với cấu hình cho audio
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "public_id", AUDIO_FOLDER + "/" + uniqueFileName,
                    "resource_type", "video", // Cloudinary sử dụng "video" cho audio files
                    "format", fileExtension,
                    "quality", "auto",
                    "fetch_format", "auto"
                )
            );

            log.info("Successfully uploaded audio file to Cloudinary: {}", uploadResult.get("public_id"));

            // Tạo response object
            return CloudinaryUploadResult.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .url((String) uploadResult.get("url"))
                    .format((String) uploadResult.get("format"))
                    .resourceType((String) uploadResult.get("resource_type"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .duration(uploadResult.get("duration") != null ? 
                             ((Number) uploadResult.get("duration")).doubleValue() : null)
                    .originalFileName(originalFileName)
                    .uniqueFileName(uniqueFileName)
                    .uploadTimestamp(System.currentTimeMillis())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading audio file to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi upload file lên Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file audio từ Cloudinary
     * @param publicId Public ID của file cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteAudio(String publicId) {
        try {
            log.info("Deleting audio file from Cloudinary: {}", publicId);

            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", "video")
            );

            String result = (String) deleteResult.get("result");
            boolean isDeleted = "ok".equals(result);

            if (isDeleted) {
                log.info("Successfully deleted audio file from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete audio file from Cloudinary: {}, result: {}", publicId, result);
            }

            return isDeleted;

        } catch (IOException e) {
            log.error("Error deleting audio file from Cloudinary: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy URL của file audio từ Cloudinary
     * @param publicId Public ID của file
     * @return Secure URL của file
     */
    public String getAudioUrl(String publicId) {
        return cloudinary.url()
                .resourceType("video")
                .secure(true)
                .publicId(publicId)
                .generate();
    }

    /**
     * Lấy extension của file
     * @param fileName Tên file
     * @return Extension của file
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Validate file audio
     * @param file File cần validate
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là 50MB");
        }

        // Kiểm tra định dạng file
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        boolean isValidFormat = false;
        for (String allowedFormat : ALLOWED_AUDIO_FORMATS) {
            if (allowedFormat.equals(fileExtension)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IllegalArgumentException("Định dạng file không hỗ trợ. Chỉ chấp nhận: mp3, wav, m4a, aac, ogg, flac");
        }

        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("File không phải là file audio hợp lệ");
        }
    }

    /**
     * Tạo tên file unique
     * @param originalFileName Tên file gốc
     * @return Tên file unique
     */
    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        
        // Loại bỏ ký tự đặc biệt và thay thế bằng dấu gạch dưới
        baseName = baseName.replaceAll("[^a-zA-Z0-9]", "_");
        
        // Tạo unique ID
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        return baseName + "_" + uniqueId;
    }

    /**
     * Format file size thành string dễ đọc
     * @param bytes Kích thước file tính bằng bytes
     * @return String format dễ đọc
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Inner class chứa kết quả upload từ Cloudinary
     */
    @lombok.Builder
    @lombok.Data
    public static class CloudinaryUploadResult {
        private String publicId;
        private String secureUrl;
        private String url;
        private String format;
        private String resourceType;
        private Long bytes;
        private Double duration; // Thời lượng audio (seconds)
        private Integer width;   // Chiều rộng cho image
        private Integer height;  // Chiều cao cho image
        private String originalFileName;
        private String uniqueFileName;
        private Long uploadTimestamp;

        public String getFormattedFileSize() {
            if (bytes == null) return "Unknown";
            
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            } else {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
        }

        public String getFormattedDuration() {
            if (duration == null) return "Unknown";
            
            int minutes = (int) (duration / 60);
            int seconds = (int) (duration % 60);
            return String.format("%d:%02d", minutes, seconds);
        }
        
        public String getDimensions() {
            if (width != null && height != null) {
                return width + "x" + height;
            }
            return "Unknown";
        }
    }
}
